#!/bin/bash
# синхронізація локальних файлів з Google Drive НАПРЯМУ через `rclone copyto`,
# Включає пункт: для *.db - PRAGMA wal_checkpoint + sqlite .backup + integrity_check.
#
# Чому так: `cp -p` через rclone mount падає з
#   "cp: preserving permissions ...: Input/output error",
# бо chmod/chown на Drive не підтримуються, а копіювання живого SQLite через
# мережеву ФС ризикує усіченим файлом. Тут мережею ходить тільки rclone
# з ретраями і чексумами, а SQLite пишемо/читаємо через backup API.
#
# Вимагає: rclone (обов'язково), sqlite3 (бажано для *.db, інакше fallback на cp).
# Використання:
#   ./files_sync_rclone.sh [--yes] [--dry-run]
#   --yes      - не питати, перезаписувати новішим автоматично
#   --dry-run  - тільки показати що б робив, нічого не качати

set -u

####### init: кольори
if [[ -t 1 ]]; then
  C_RESET=$'\033[0m'; C_BOLD=$'\033[1m'; C_DIM=$'\033[2m'
  C_RED=$'\033[31m'; C_GREEN=$'\033[32m'; C_YELLOW=$'\033[33m'; C_CYAN=$'\033[36m'
else
  C_RESET=''; C_BOLD=''; C_DIM=''; C_RED=''; C_GREEN=''; C_YELLOW=''; C_CYAN=''
fi

echo "------------------------------------------------------------"
echo "files_sync_rclone v1.00.00.002  2026-09-17 "
echo "------------------------------------------------------------"

####### config
RCLONE_REMOTE="gdrive:"          # rclone remote (див. `rclone listremotes`)
REMOTE_DIR="remdir"              # підпапка на Drive (без початкового /)
RCLONE_OPTS=(                    # стабільні опції для Drive
  --retries 3
  --low-level-retries 10
  --retries-sleep 2s
  --transfers 1
  --checkers 1
  --contimeout 15s
  --timeout 60s
  --drive-acknowledge-abuse
)
MTIME_TOLERANCE_SEC=2            # якщо mtime відрізняються менше - вважаємо однаковими за часом
MAX_ATTEMPTS=3                   # ретраї навколо rclone copyto

# Пари "локальний файл|ім'я на Drive". Доповніть під себе:
pairs=(
  "db/scheduler.db|scheduler.db"
)

AUTO_YES=0; DRY_RUN=0
for arg in "$@"; do
  case "$arg" in
    --yes) AUTO_YES=1;;
    --dry-run) DRY_RUN=1;;
    -h|--help) echo "Використання: $0 [--yes] [--dry-run]"; exit 0;;
    *) echo "${C_RED}Невідомий аргумент: $arg${C_RESET}" >&2; exit 2;;
  esac
done

copied=0; skipped=0; equal=0; failed=0
TMP_FILES=()
cleanup() { for f in "${TMP_FILES[@]:-}"; do [[ -n "$f" && -e "$f" ]] && rm -f -- "$f"; done }
trap cleanup EXIT

####### checks
command -v rclone >/dev/null 2>&1 || { echo "${C_RED}✗ rclone не знайдено в PATH${C_RESET}" >&2; exit 3; }
HAVE_SQLITE=1
command -v sqlite3 >/dev/null 2>&1 || { HAVE_SQLITE=0; echo "${C_YELLOW}⚠ sqlite3 не знайдено - *.db копіюватимуться як звичайні файли (менш безпечно)${C_RESET}"; }
rclone listremotes 2>/dev/null | grep -qxF "$RCLONE_REMOTE" || {
  echo "${C_RED}✗ remote '$RCLONE_REMOTE' не знайдено. Доступні:${C_RESET}" >&2
  rclone listremotes >&2 || true
  exit 3
}

####### helpers
is_db() { [[ "$1" == *.db || "$1" == *.sqlite || "$1" == *.sqlite3 ]]; }

sqlite_checkpoint() { # $1=db ; best-effort, не фейлимо все через WAL
  (( HAVE_SQLITE )) || return 0
  sqlite3 -- "$1" "PRAGMA wal_checkpoint(TRUNCATE);" >/dev/null 2>&1 || true
}

sqlite_integrity_ok() { # $1=db -> 0 якщо 'ok'
  (( HAVE_SQLITE )) || return 0  # без sqlite3 нічого перевірити
  [[ "$(sqlite3 -- "$1" "PRAGMA integrity_check;" 2>/dev/null)" == "ok" ]]
}

sqlite_backup_to() { # $1=src $2=dst_tmp -> 0/1
  (( HAVE_SQLITE )) || return 1
  sqlite_checkpoint "$1"
  rm -f -- "$2"
  # .backup через CLI: екрануємо одинарні лапки в шляху
  local esc_dst="${2//\'/\'\'}"
  sqlite3 -- "$1" ".backup main '$esc_dst'" 2>/dev/null
}

remote_path() { printf "%s%s/%s" "$RCLONE_REMOTE" "$REMOTE_DIR" "$1"; }

# Повертає "epoch size" для файла на Drive, або нічого якщо нема.
# Використовує lsjson + python3 (точно), fallback - rclone lsl.
remote_stat() { # $1 = ім'я файла на Drive
  local rp; rp="$(remote_path "$1")"
  local json epoch size
  if command -v python3 >/dev/null 2>&1 && json="$(rclone lsjson --files-only --include "${rp##*/}" -- "${rp%/*}" 2>/dev/null)" && [[ -n "$json" && "$json" != "[]" ]]; then
    RP_JSON="$json" RP_NAME="${rp##*/}" python3 -c "
import json, os, datetime
items = json.loads(os.environ['RP_JSON'])
name = os.environ['RP_NAME']
for it in items:
    if it.get('Name') == name:
        mt = it.get('ModTime', '')
        try:
            dt = datetime.datetime.fromisoformat(mt.replace('Z', '+00:00'))
            print(int(dt.timestamp()), it.get('Size', -1))
        except Exception:
            pass
        break
" 2>/dev/null && return 0
  fi
  # fallback: rclone lsl -> "size YYYY-MM-DD HH:MM:SS.mmm path"
  local line lsize ldate ltime
  line="$(rclone lsl --max-depth 1 -- "$rp" 2>/dev/null | head -n1)"
  [[ -z "$line" ]] && return 1
  lsize="$(awk '{print $1}' <<<"$line")"
  ldate="$(awk '{print $2}' <<<"$line")"
  ltime="$(awk '{print $3}' <<<"$line" | cut -d'.' -f1)"
  epoch="$(date -d "$ldate $ltime" +%s 2>/dev/null)" || return 1
  printf "%s %s" "$epoch" "$lsize"
}

local_stat() { # $1=path -> "epoch size" або нічого
  [[ -e "$1" ]] || return 1
  printf "%s %s" "$(stat -c '%Y' -- "$1")" "$(stat -c '%s' -- "$1")"
}

remote_size() { # $1=ім'я на Drive -> size або ""
  local st; st="$(remote_stat "$1" 2>/dev/null)" || { echo ""; return 1; }
  awk '{print $2}' <<<"$st"
}

rclone_copyto_retry() { # $1=src $2=dst(remote або local)
  local src="$1" dst="$2" i=1
  while (( i <= MAX_ATTEMPTS )); do
    if (( DRY_RUN )); then
      echo "${C_DIM}[dry-run] rclone copyto $src $dst${C_RESET}"
      return 0
    fi
    # ВАЖЛИВО: опції rclone мусять іти ДО '--', бо все після '--'
    # cobra-парсер rclone вважає позиційними аргументами (звідси була
    # помилка "Command copyto needs 2 arguments maximum").
    if rclone copyto "${RCLONE_OPTS[@]}" -- "$src" "$dst"; then
      return 0
    fi
    echo "${C_YELLOW}⚠ rclone copyto спроба $i/$MAX_ATTEMPTS не вдалася, пауза...${C_RESET}" >&2
    sleep 2
    ((i++))
  done
  return 1
}

do_upload() { # $1=local $2=drive_name
  local local="$1" name="$2" rp; rp="$(remote_path "$name")"
  local src_for_upload="$local" tmp=""
  if is_db "$local" && (( HAVE_SQLITE )); then
    tmp="$(mktemp --suffix=.db)"; TMP_FILES+=("$tmp")
    echo "${C_DIM}sqlite backup: $local -> tmp${C_RESET}"
    if ! sqlite_backup_to "$local" "$tmp"; then
      echo "${C_RED}✗ sqlite .backup не вдався: $local${C_RESET}"; return 1
    fi
    if ! sqlite_integrity_ok "$tmp"; then
      echo "${C_RED}✗ integrity_check бекапа НЕ ok, upload скасовано: $local${C_RESET}"; return 1
    fi
    src_for_upload="$tmp"
  elif is_db "$local"; then
    sqlite_checkpoint "$local" || true
  fi
  # mtime: tmp з .backup має час створення (≈ момент upload), а не оригінальний.
  # Без цього Drive отримає "новіший" mtime і наступний запуск захоче качати назад.
  # rclone copyto переносить mtime джерела на Drive, тому вирівнюємо його заздалегідь.
  if [[ "$src_for_upload" != "$local" ]]; then
    touch -r "$local" -- "$src_for_upload" 2>/dev/null || true
  fi
  (( DRY_RUN )) && { rclone_copyto_retry "$src_for_upload" "$rp"; return 0; }
  rclone_copyto_retry "$src_for_upload" "$rp" || { echo "${C_RED}✗ upload не вдався: $local -> $rp${C_RESET}"; return 1; }
  # верифікація: розмір на Drive == розміру залитого файлу
  local want got
  want="$(stat -c '%s' -- "$src_for_upload")"
  got="$(remote_size "$name")"
  if [[ -n "$got" && "$got" != "$want" ]]; then
    echo "${C_RED}✗ розмір після upload не збігся (local $want vs remote $got): $name${C_RESET}"; return 1
  fi
  return 0
}

do_download() { # $1=drive_name $2=local
  local name="$1" local="$2" rp; rp="$(remote_path "$name")"
  local tmp; tmp="$(mktemp --suffix=.download)"; TMP_FILES+=("$tmp")
  rclone_copyto_retry "$rp" "$tmp" || { echo "${C_RED}✗ download не вдався: $rp${C_RESET}"; return 1; }
  [[ -s "$tmp" ]] || { echo "${C_RED}✗ з Drive прилетів порожній файл: $name${C_RESET}"; return 1; }
  if is_db "$local" && (( HAVE_SQLITE )); then
    if ! sqlite_integrity_ok "$tmp"; then
      echo "${C_RED}✗ integrity_check завантаженого НЕ ok, локальний файл НЕ чіпаю: $local${C_RESET}"; return 1
    fi
    if (( DRY_RUN )); then echo "${C_DIM}[dry-run] sqlite backup tmp -> $local${C_RESET}"; return 0; fi
    mkdir -p -- "$(dirname -- "$local")"
    # Безпечна установка через backup API (консистентний файл, без копіювання WAL)
    local esc_local="${local//\'/\'\'}"
    if ! sqlite3 -- "$tmp" ".backup main '$esc_local'" 2>/dev/null; then
      echo "${C_RED}✗ sqlite .backup в локальний файл не вдався: $local${C_RESET}"; return 1
    fi
    sqlite_integrity_ok "$local" || { echo "${C_RED}✗ integrity_check фінального НЕ ok: $local${C_RESET}"; return 1; }
  else
    if (( DRY_RUN )); then echo "${C_DIM}[dry-run] cp tmp -> $local${C_RESET}"; return 0; fi
    mkdir -p -- "$(dirname -- "$local")"
    cp -- "$tmp" "$local" || { echo "${C_RED}✗ не вдалося записати локальний файл: $local${C_RESET}"; return 1; }
    cmp -s -- "$tmp" "$local" || { echo "${C_RED}✗ cmp після download не збігся: $local${C_RESET}"; return 1; }
  fi
  # mtime: встановлений файл має час "зараз" (≈ момент download), а не час з Drive.
  # Без цього наступний запуск побачить "новіший" локальний і захоче заливати назад.
  local rst rtime
  if rst="$(remote_stat "$name" 2>/dev/null)" && [[ -n "$rst" ]]; then
    rtime="$(awk '{print $1}' <<<"$rst")"
    touch -d "@$rtime" -- "$local" 2>/dev/null || \
      echo "${C_YELLOW}⚠ не вдалося виставити mtime локального (наступний запуск може перепитати)${C_RESET}"
  else
    echo "${C_YELLOW}⚠ не вдалося прочитати mtime з Drive (наступний запуск може перепитати)${C_RESET}"
  fi
  return 0
}

ask_yes() { # $1=питання -> 0 якщо так
  (( AUTO_YES )) && return 0
  local ans
  read -r -p "$1 [y/N]: " ans
  case "$ans" in y|Y|yes|YES|так|Так|ТАК|tak) return 0;; *) return 1;; esac
}

####### main cycle
for pair in "${pairs[@]}"; do
  local_file="${pair%%|*}"; drive_name="${pair#*|}"
  rp="$(remote_path "$drive_name")"
  echo "${C_BOLD}---${C_RESET} ${C_CYAN}$local_file${C_RESET} ${C_DIM}<->$C_RESET ${C_CYAN}$rp${C_RESET}"

  lstat="$(local_stat "$local_file" 2>/dev/null || echo "")"
  rstat="$(remote_stat "$drive_name" 2>/dev/null || echo "")"

  if [[ -z "$lstat" && -z "$rstat" ]]; then
    printf "${C_RED}✗ обох файлів нема, пропускаю${C_RESET}\n"; failed=$((failed+1)); continue
  fi
  if [[ -z "$lstat" ]]; then
    printf "${C_YELLOW}» локального нема, є тільки на Drive - завантажити?${C_RESET}\n"
    if ask_yes "Завантажити $drive_name -> $local_file?"; then
      if do_download "$drive_name" "$local_file"; then printf "${C_GREEN}✓ Завантажено: %s${C_RESET}\n" "$local_file"; copied=$((copied+1));
      else failed=$((failed+1)); fi
    else printf "${C_DIM}пропущено${C_RESET}\n"; skipped=$((skipped+1)); fi
    continue
  fi
  if [[ -z "$rstat" ]]; then
    printf "${C_YELLOW}» на Drive нема, є тільки локальний - залити?${C_RESET}\n"
    if ask_yes "Залити $local_file -> $drive_name?"; then
      if do_upload "$local_file" "$drive_name"; then printf "${C_GREEN}✓ Залито: %s${C_RESET}\n" "$drive_name"; copied=$((copied+1));
      else failed=$((failed+1)); fi
    else printf "${C_DIM}пропущено${C_RESET}\n"; skipped=$((skipped+1)); fi
    continue
  fi

  ltime="$(awk '{print $1}' <<<"$lstat")"; lsize="$(awk '{print $2}' <<<"$lstat")"
  rtime="$(awk '{print $1}' <<<"$rstat")"; rsize="$(awk '{print $2}' <<<"$rstat")"
  printf "LOC:%s (%s bytes)  RMT:%s (%s bytes)\n" \
    "$(date -d "@$ltime" '+%F %T' 2>/dev/null || echo "$ltime")" "$lsize" \
    "$(date -d "@$rtime" '+%F %T' 2>/dev/null || echo "$rtime")" "$rsize"

  # однакові: час в межах толерантності І розмір збігся
  diff=$(( ltime > rtime ? ltime - rtime : rtime - ltime ))
  if (( diff <= MTIME_TOLERANCE_SEC )) && [[ "$lsize" == "$rsize" ]]; then
    printf "${C_DIM}файли однакові, пропускаю${C_RESET}\n"; equal=$((equal+1)); continue
  fi

  if (( ltime > rtime )); then
    printf "${C_BOLD}${C_YELLOW}»${C_RESET} ${C_YELLOW}%s${C_RESET} новіший за ${C_YELLOW}%s${C_RESET}\n" "$local_file" "$rp"
    if ask_yes "Перезаписати на Drive новішим локальним?"; then
      if do_upload "$local_file" "$drive_name"; then printf "${C_GREEN}✓ Залито: %s${C_RESET}\n" "$drive_name"; copied=$((copied+1));
      else failed=$((failed+1)); fi
    else printf "${C_DIM}пропущено${C_RESET}\n"; skipped=$((skipped+1)); fi
  else
    printf "${C_BOLD}${C_YELLOW}»${C_RESET} ${C_YELLOW}%s${C_RESET} новіший за ${C_YELLOW}%s${C_RESET}\n" "$rp" "$local_file"
    if [[ "$lsize" != "$rsize" && "$diff" -le "$MTIME_TOLERANCE_SEC" ]]; then
      printf "${C_YELLOW}⚠ час майже однаковий, але розміри різні - звірте вручну перед перезаписом${C_RESET}\n"
    fi
    if ask_yes "Перезаписати локальний новішим з Drive?"; then
      if do_download "$drive_name" "$local_file"; then printf "${C_GREEN}✓ Завантажено: %s${C_RESET}\n" "$local_file"; copied=$((copied+1));
      else failed=$((failed+1)); fi
    else printf "${C_DIM}пропущено${C_RESET}\n"; skipped=$((skipped+1)); fi
  fi
done

printf "\n${C_BOLD}Підсумок:${C_RESET} ${C_GREEN}синхронізовано %d${C_RESET}, ${C_DIM}пропущено %d${C_RESET}, однакові %d" "$copied" "$skipped" "$equal"
if (( failed > 0 )); then printf ", ${C_RED}помилок %d${C_RESET}" "$failed"; fi
printf "\n"
(( failed > 0 )) && exit 1
exit 0
