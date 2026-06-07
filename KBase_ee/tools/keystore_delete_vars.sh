#!/bin/bash
# Видалення змінних з поточної сесії
unset KBASE_KEYSTORE_PATH
unset KBASE_KEYSTORE_TYPE
unset KBASE_KEY_ALIAS
unset KBASE_KEYSTORE_PASSWORD
unset KBASE_KEY_PASSWORD

# Видалення файлу збережених змінних
rm -f "$HOME/.kbase_env.sh"
echo "Змінні видалено"
