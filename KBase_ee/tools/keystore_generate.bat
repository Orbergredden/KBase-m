@echo off
rem скрипт запускається з директорії де повинен створитися файл KeyStore
rem !!!!! якщо ви будете водити паролі при старті програми (для більшої безпеки),
rem       то KBASE_KEYSTORE_PASSWORD та KBASE_KEY_PASSWORD не встановлюємо
REM === Налаштування ===
set "STOREPASS=kbase"
set "KEYPASS=kbase"
set "KEYSTORE_PATH=%~dp0keystore.p12"
set "ALIAS=kbase-secrets-key"

REM === Генерація AES ключа у PKCS12 KeyStore ===
"%JAVA_HOME%\bin\keytool" -genseckey ^
  -alias "%ALIAS%" ^
  -keyalg AES -keysize 256 ^
  -storetype PKCS12 ^
  -keystore "%KEYSTORE_PATH%" ^
  -storepass "%STOREPASS%" ^
  -keypass "%KEYPASS%"
if errorlevel 1 (
  echo [ERROR] keytool failed
  pause
  exit /b 1
)

REM === Зробити змінні доступними ОДРАЗУ у поточній консолі ===
rem set "KBASE_KEYSTORE_PATH=%KEYSTORE_PATH%"
set "KBASE_KEYSTORE_TYPE=PKCS12"
set "KBASE_KEY_ALIAS=%ALIAS%"
set "KBASE_KEYSTORE_PASSWORD=%STOREPASS%"
set "KBASE_KEY_PASSWORD=%KEYPASS%"

REM === Записати змінні назавжди (для НОВИХ консолей/процесів) ===
rem setx KBASE_KEYSTORE_PATH "%KEYSTORE_PATH%"
setx KBASE_KEYSTORE_TYPE "PKCS12"
setx KBASE_KEY_ALIAS "%ALIAS%"
setx KBASE_KEYSTORE_PASSWORD "%STOREPASS%"
setx KBASE_KEY_PASSWORD "%KEYPASS%"

pause
