@echo off

set JAVA_HOME="C:\Users\imakarevich\_win64\_prog\_java\jdk-24"
set PATH=%JAVA_HOME%\bin;%PATH%

REM Створити JAR з усіх .class файлів
echo Creating JAR file...
jar cf plugins_api.jar -C bin .

REM Копіювати JAR у інший проєкт
set TARGET_DIR=..\..\KBase_ee_libExt\_my

if not exist %TARGET_DIR% (
    mkdir %TARGET_DIR%
)
copy /Y plugins_api.jar %TARGET_DIR%

echo Done.
pause
