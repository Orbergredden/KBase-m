#!/bin/bash

./files_sync.sh

#~/.kbase_env.sh
export KBASE_KEYSTORE_PATH="/home/igor/.config/KBase/KBase_ee_dev/keystore.p12"
export KBASE_KEYSTORE_TYPE="PKCS12"
export KBASE_KEY_ALIAS="kbase-secrets-key"
export KBASE_KEYSTORE_PASSWORD="kbase_6"
export KBASE_KEY_PASSWORD="kbase_6"
#env | sort

# --- Встановлення JAVA_HOME (шлях до JDK 24) ---
#export JAVA_HOME="$HOME/_win64/_prog/_java/jdk-24"
#export PATH="$JAVA_HOME/bin:$PATH"

# --- Шляхи до твоїх бібліотек та кореня програми ---
KBASE_ROOT="$HOME/_prog/KBase_ee_Raketta"
KBASE_LIB="$HOME/_prog/KBase_ee_libExt"
PATH_TO_FX="$KBASE_LIB/javafx-sdk-25.0.1/lib"

# --- Створення CLASSPATH ---
CLASSPATH="$KBASE_ROOT"
CLASSPATH="$CLASSPATH:$KBASE_LIB/_single/activation-1.1.1.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/_single/postgresql-42.7.5.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/_single/sqlite-jdbc-3.46.0.0.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/_single/slf4j-api-2.0.13.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/_single/slf4j-simple-2.0.13.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/_single/ojdbc11-23.4.0.24.05.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/_single/orai18n.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/_my/msgBase.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/_my/plugins_api.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jasypt-1.9.2/jasypt-1.9.2.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/codemodel.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/dtd-parser.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/FastInfoset.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/istack-commons-runtime.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/istack-commons-tools.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/javax.activation-api.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/jaxb-api.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/jaxb-jxc.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/jaxb-runtime.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/jaxb-xjc.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/relaxng-datatype.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/rngom.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/stax-ex.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/txw2.jar"
CLASSPATH="$CLASSPATH:$KBASE_LIB/jaxb-ri-2.3.1/xsom.jar"

# --- Додаткові бібліотеки JavaFX ---
CLASSPATH="$CLASSPATH:$PATH_TO_FX/javafx-swt.jar"
CLASSPATH="$CLASSPATH:$PATH_TO_FX/javafx.web.jar"
CLASSPATH="$CLASSPATH:$PATH_TO_FX/javafx.base.jar"
CLASSPATH="$CLASSPATH:$PATH_TO_FX/javafx.fxml.jar"
CLASSPATH="$CLASSPATH:$PATH_TO_FX/javafx.media.jar"
CLASSPATH="$CLASSPATH:$PATH_TO_FX/javafx.swing.jar"
CLASSPATH="$CLASSPATH:$PATH_TO_FX/javafx.controls.jar"
CLASSPATH="$CLASSPATH:$PATH_TO_FX/javafx.graphics.jar"

# --- Запуск JavaFX програми ---
java \
    --module-path "$PATH_TO_FX" \
    --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.base,javafx.graphics \
    --add-reads javafx.base=ALL-UNNAMED \
    --add-reads javafx.graphics=ALL-UNNAMED \
    --enable-native-access=javafx.graphics \
    --enable-native-access=javafx.web \
    --enable-native-access=ALL-UNNAMED \
    -Dfile.encoding=UTF-8 \
    -classpath "$CLASSPATH" \
    app.Main

./files_sync.sh

# --- Зупинка після завершення ---
read -p "Натисніть Enter для виходу..."

