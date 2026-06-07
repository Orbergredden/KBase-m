#!/bin/bash

# Шлях до папки з JavaFX SDK (змініть на свій фактичний шлях)
PATH_TO_FX="/home/igor/_prog/KBase_ee_libExt/javafx-sdk-25.0.1/lib"





# Очищення попередньої збірки
rm -rf out
mkdir out

# Пошук усіх .java файлів у проекті
find src -name "*.java" > sources.txt

# Компіляція
javac -d out \
      --module-path $PATH_TO_FX \
      --add-modules javafx.controls,javafx.fxml \
      @sources.txt

rm sources.txt
echo "Компіляція завершена. Файли знаходяться в папці out/"