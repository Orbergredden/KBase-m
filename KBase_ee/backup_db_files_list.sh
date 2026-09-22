#!/bin/bash

####### init
echo "-------------------------------------------------------------"
echo "backup_db_files_list.sh v2.00.00.004  2026-05-15 - 2026-08-13"
echo "-------------------------------------------------------------"

input_files=(
    "db/export_kbase.db"
    "db/scheduler.db"
)
output_archives=(
    "db/_backup/export_kbase_[{DATE_TIME}].db.tar.gz"
    "db/_backup/scheduler_[{DATE_TIME}].db.tar.gz"
)

RED='\033[31m'
GREEN='\033[32m'
YELLOW='\033[33m'
CYAN='\033[36m'
RESET='\033[0m'

# Поточна дата/час у форматі YYMMDDHHMM
CURRENT_DT=$(date +"%y%m%d%H%M")

####### main cycle

for i in "${!input_files[@]}"; do
    in="${input_files[$i]}"
    out="${output_archives[$i]}"
    # Заміна [{DATE_TIME}] на дату
    out_processed="${out//\[\{DATE_TIME\}\]/$CURRENT_DT}"

    if [ ! -f "$in" ]; then
		printf "${RED}Пропущено (не існує): $in${RESET}\n"
        continue
    fi

    printf "Архівую: ${CYAN}$in${RESET} -> $out_processed ... "
    tar -czf "$out_processed" -C "$(dirname "$in")" "$(basename "$in")"

    if [ $? -eq 0 ]; then
        printf "${GREEN}OK${RESET}\n"
    else
		printf "${RED}Помилка архівації: $in${RESET}\n"
    fi
done
