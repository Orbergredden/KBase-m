#!/bin/bash

# Шлях до проекту (директорія, яку архівуємо)
PROJECT_DIR="/home/igor/_prog/KBase-m_dev"

# Папка для збереження бекапів проекту
BACKUP_DIR="/home/igor/_backup/_prog/KBase-m"

# Створення папки для бекапів, якщо вона не існує
mkdir -p "$BACKUP_DIR"

# Форматування дати та часу (наприклад: "2026-07-05_11:12:50")
BACKUP_DATE=$(date "+%Y-%m-%d_%H:%M:%S")
ZIP_FILE="$BACKUP_DIR/KBase-m_dev_${BACKUP_DATE}.zip"

echo "=== Початок створення архіву проекту ==="
echo "Джерело: $PROJECT_DIR"
echo "Архів: $ZIP_FILE"

# Переходимо в директорію проекту, щоб архів мав відносні шляхи
cd "$PROJECT_DIR" || exit 1

# Запуск архівування в zip.
zip -q -r "$ZIP_FILE" . 

# Перевірка статусу виконання команди
if [ $? -eq 0 ]; then
    echo "Архів проекту успішно створено!"
else
    echo "Помилка при створенні архіву проекту!" >&2
    exit 1
fi
