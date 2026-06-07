#!/bin/bash

####### init
echo "backup_db_files_list.sh v1.00.01.001  2026-05-15 - 2026-05-15"

input_files=(
    "db/export_kbase.db"
    "db/scheduler.db"
)
output_archives=(
    "db/_backup/export_kbase_[{DATE_TIME}].db.tar.gz"
    "db/_backup/scheduler_[{DATE_TIME}].db.tar.gz"
)

# Поточна дата/час у форматі YYMMDDHHMM
CURRENT_DT=$(date +"%y%m%d%H%M")

####### main cycle

for i in "${!input_files[@]}"; do
    in="${input_files[$i]}"
    out="${output_archives[$i]}"
    # Заміна [{DATE_TIME}] на дату
    out_processed="${out//\[\{DATE_TIME\}\]/$CURRENT_DT}"

    if [ ! -f "$in" ]; then
        echo "Пропущено (не існує): $in"
        continue
    fi

    echo "Архівую: $in -> $out_processed"
    tar -czf "$out_processed" -C "$(dirname "$in")" "$(basename "$in")"

    if [ $? -eq 0 ]; then
        echo "OK"
    else
        echo "Помилка архівації: $in"
    fi
done
