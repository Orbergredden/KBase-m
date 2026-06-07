@echo off

set JAVA_HOME="C:\Users\imakarevich\_win64\_prog\_java\jdk-24"
set PATH=%JAVA_HOME%\bin;%PATH%

REM Створити JAR з усіх .class файлів
echo Creating JAR file...
jar cf plugin.jar -C bin .

REM Копіювати JAR у інший проєкт
set TARGET_DIR=..\KBase_ee\plugins\plugin_example

if not exist %TARGET_DIR% (
    mkdir %TARGET_DIR%
)
copy /Y plugin.jar        %TARGET_DIR%
copy /Y plugin.properties %TARGET_DIR%

echo Done.
pause
