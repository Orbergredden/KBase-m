#!/bin/bash
# Скрипт запускається з директорії, де буде створено KeyStore

# === Налаштування ===
STOREPASS="kbase_6"
KEYPASS="kbase_6"
KEYSTORE_PATH="$(pwd)/keystore.p12"
ALIAS="kbase-secrets-key"

# === Генерація AES ключа у PKCS12 KeyStore ===
"$JAVA_HOME/bin/keytool" -genseckey \
  -alias "$ALIAS" \
  -keyalg AES -keysize 256 \
  -storetype PKCS12 \
  -keystore "$KEYSTORE_PATH" \
  -storepass "$STOREPASS" \
  -keypass "$KEYPASS"

if [ $? -ne 0 ]; then
  echo "[ERROR] keytool failed"
  exit 1
fi

# === Зробити змінні доступними ОДРАЗУ у поточній консолі ===
export KBASE_KEYSTORE_PATH="$KEYSTORE_PATH"
export KBASE_KEYSTORE_TYPE="PKCS12"
export KBASE_KEY_ALIAS="$ALIAS"
export KBASE_KEYSTORE_PASSWORD="$STOREPASS"
export KBASE_KEY_PASSWORD="$KEYPASS"

# === Записати у файл для майбутніх сесій ===
ENV_FILE="$HOME/.kbase_env.sh"
cat > "$ENV_FILE" <<EOF
export KBASE_KEYSTORE_PATH="$KEYSTORE_PATH"
export KBASE_KEYSTORE_TYPE="PKCS12"
export KBASE_KEY_ALIAS="$ALIAS"
export KBASE_KEYSTORE_PASSWORD="$STOREPASS"
export KBASE_KEY_PASSWORD="$KEYPASS"
EOF

echo "Змінні записані у $ENV_FILE"
echo "Щоб активувати у новій сесії: source $ENV_FILE"
