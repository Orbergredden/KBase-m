#!/bin/bash

####### init
echo "files_sync v1.00.01.002  2026-05-08 - 2026-05-08"

src_files=(
  "db/scheduler.db"
)
dst_files=(
  "db/scheduler.db"
)

####### procedures
show_time() {
  local label="$1"
  local file="$2"

  if [[ -e "$file" ]]; then
    echo "$label: $file"
    echo "  $(stat -c '%y' -- "$file" | cut -d'.' -f1)"
  else
    echo "$label: $file"
    echo "  File not found."
  fi
}

ask_and_copy() {
  local newer="$1"
  local older="$2"

  echo
  echo "файл:"
  echo "  $newer"
  echo "новіший за:"
  echo "  $older"
  echo

  read -r -p "Перезаписати старіший файл свіжішим? [y/N]: " answer

  case "$answer" in
    y|Y|yes|YES|так|Так|ТАК|tak)
      cp -p -- "$newer" "$older"
      echo "Перезаписано: $older"
      ;;
    *)
      echo "Пропущено."
      ;;
  esac
}

####### main cycle
for i in "${!src_files[@]}"; do
  src="${src_files[i]}"
  dst="${dst_files[i]}"

  echo "===================================================="
  show_time "SRC" "$src"
  show_time "DST" "$dst"

  src_time=$(stat -c '%Y' "$src")
  dst_time=$(stat -c '%Y' "$dst")

  if (( src_time > dst_time )); then
    ask_and_copy "$src" "$dst"
  elif (( dst_time > src_time )); then
    ask_and_copy "$dst" "$src"
  else
    echo "файли мають однакову дату/час модифікації. Нічого не роблю."
  fi
done

# --- Зупинка після завершення ---
#read -p "Press Enter for exit..."
