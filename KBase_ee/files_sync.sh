#!/bin/bash

####### init
if [[ -t 1 ]]; then
  C_RESET=$'\033[0m'; C_BOLD=$'\033[1m'; C_DIM=$'\033[2m'
  C_RED=$'\033[31m'; C_GREEN=$'\033[32m'; C_YELLOW=$'\033[33m'; C_CYAN=$'\033[36m'
else
  C_RESET=''; C_BOLD=''; C_DIM=''; C_RED=''; C_GREEN=''; C_YELLOW=''; C_CYAN=''
fi

echo "-------------------------------------------------"
echo "files_sync v2.01.00.006  2026-05-08 - 2026-09-17 "
echo "-------------------------------------------------"

src_files=(
  "db/scheduler.db"
)
dst_files=(
  "db/scheduler.db"
)

total=${#src_files[@]}
copied=0; skipped=0; equal=0; failed=0

####### procedures
show_time() {
  local label="$1" file="$2"
  if [[ -e "$file" ]]; then
    local mtime
    mtime=$(stat -c '%y' -- "$file" | cut -d'.' -f1)
    printf "%-4s%s ${C_CYAN}%-30s${C_RESET}\n" "$label:" "$mtime" "$file"
  else
    printf "%-4s${C_CYAN}%-30s${C_RESET} ${C_RED}not found${C_RESET}\n" "$label:" "$file"
  fi
}

ask_and_copy() {
  local newer="$1" older="$2"
  printf "${C_BOLD}${C_YELLOW}»${C_RESET} ${C_YELLOW}%s${C_RESET} новіший за ${C_YELLOW}%s${C_RESET}\n" "$newer" "$older"
  read -r -p "Перезаписати старіший файл? [y/N]: " answer
  case "$answer" in
    y|Y|yes|YES|так|Так|ТАК|tak)
      # БЕЗ 'cp -p'.
      # На rclone/FUSE 'cp -p' падає з 'preserving permissions: Input/output error',
      # бо chmod/chown на Google Drive не підтримуються.
      # Крок 1: копіюємо тільки контент (без збереження прав/власника).
      if ! cp -- "$newer" "$older"; then
        printf "${C_RED}✗ Помилка копіювання: %s -> %s${C_RESET}\n" "$newer" "$older"
        failed=$((failed+1)); return 1
      fi
      # Крок 2: best-effort синхронізація mtime (щоб наступний запуск не питав знову).
      # На FUSE може дати EIO - це некритично, тому ігноруємо з попередженням.
      if ! touch -r "$newer" -- "$older" 2>/dev/null; then
        printf "${C_YELLOW}⚠ контент скопійовано, але mtime не збережено (FUSE/rclone EIO)${C_RESET}\n"
      fi
      # Крок 3: верифікація контенту. Без неї скрипт раніше завжди друкував '✓'.
      if ! cmp -s -- "$newer" "$older"; then
        printf "${C_RED}✗ Файли відрізняються після копіювання: %s${C_RESET}\n" "$older"
        failed=$((failed+1)); return 1
      fi
      printf "${C_GREEN}✓ Перезаписано (перевірено cmp): %s${C_RESET}\n" "$older"
      copied=$((copied+1));;
    *)
      printf "${C_DIM}пропущено${C_RESET}\n"
      skipped=$((skipped+1));;
  esac
}

####### main cycle
for i in "${!src_files[@]}"; do
  src="${src_files[i]}"; dst="${dst_files[i]}"

  printf "${C_BOLD}[%d/%d]${C_RESET} ${C_DIM}sync${C_RESET}\r" "$((i+1))" "$total"
  show_time "SRC" "$src"
  show_time "DST" "$dst"
  printf '\033[2K\r'

  src_time=$(stat -c '%Y' -- "$src" 2>/dev/null || echo "")
  dst_time=$(stat -c '%Y' -- "$dst" 2>/dev/null || echo "")

  if [[ -z "$src_time" || -z "$dst_time" ]]; then
    printf "${C_RED}✗ один з файлів відсутній, пропускаю пару${C_RESET}\n"
    failed=$((failed+1)); continue
  fi

  if (( src_time > dst_time )); then
    ask_and_copy "$src" "$dst" || true
  elif (( dst_time > src_time )); then
    ask_and_copy "$dst" "$src" || true
  else
    printf "${C_DIM}файли однакові, пропускаю${C_RESET}\n"
    equal=$((equal+1))
  fi
done

printf "\n${C_BOLD}Підсумок:${C_RESET} ${C_GREEN}перезаписано %d${C_RESET}, ${C_DIM}пропущено %d${C_RESET}, однакові %d" "$copied" "$skipped" "$equal"
if (( failed > 0 )); then
  printf ", ${C_RED}помилок %d${C_RESET}" "$failed"
fi
printf "\n"
(( failed > 0 )) && exit 1
exit 0

# --- Зупинка після завершення ---
#read -p "Press Enter for exit..."
