set JAVA_HOME="C:\Users\imakarevich\_win64\_prog\_java\jdk-24"
set PATH=%JAVA_HOME%\bin;%PATH%

set KBASE_ROOT=C:\Users\imakarevich\_prog\KBase_ee_dev\KBase_ee
set KBASE_LIB=C:\Users\imakarevich\_prog\KBase_ee_libExt
set PATH_TO_FX=%KBASE_LIB%\javafx-sdk-24\lib

@rem set TNS_ADMIN=C:\Oracle\client\product\19.0.0\client_1\network\admin

java --module-path %PATH_TO_FX% ^
     --add-modules=javafx.controls,javafx.fxml,javafx.web ^
	 --add-modules javafx.base,javafx.graphics ^
	 --add-reads javafx.base=ALL-UNNAMED ^
	 --add-reads javafx.graphics=ALL-UNNAMED ^
	 -Dfile.encoding=UTF-8 ^
	 -classpath %KBASE_ROOT%;^
%KBASE_LIB%\_single\activation-1.1.1.jar;^
%KBASE_LIB%\_single\postgresql-42.7.5.jar;^
%KBASE_LIB%\_single\sqlite-jdbc-3.46.0.0.jar;^
%KBASE_LIB%\_single\slf4j-api-2.0.13.jar;^
%KBASE_LIB%\_single\slf4j-simple-2.0.13.jar;^
%KBASE_LIB%\_single\ojdbc11-23.4.0.24.05.jar;^
%KBASE_LIB%\_single\orai18n.jar;^
%KBASE_LIB%\_my\msgBase.jar;^
%KBASE_LIB%\_my\plugins_api.jar;^
%KBASE_LIB%\jasypt-1.9.2\jasypt-1.9.2.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\codemodel.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\dtd-parser.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\FastInfoset.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\istack-commons-runtime.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\istack-commons-tools.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\javax.activation-api.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\jaxb-api.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\jaxb-jxc.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\jaxb-runtime.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\jaxb-xjc.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\relaxng-datatype.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\rngom.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\stax-ex.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\txw2.jar;^
%KBASE_LIB%\jaxb-ri-2.3.1\xsom.jar;^
%PATH_TO_FX%\javafx-swt.jar;^
%PATH_TO_FX%\javafx.web.jar;^
%PATH_TO_FX%\javafx.base.jar;^
%PATH_TO_FX%\javafx.fxml.jar;^
%PATH_TO_FX%\javafx.media.jar;^
%PATH_TO_FX%\javafx.swing.jar;^
%PATH_TO_FX%\javafx.controls.jar;^
%PATH_TO_FX%\javafx.graphics.jar ^
	 app.Main

@pause
