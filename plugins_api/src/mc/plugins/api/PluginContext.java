package mc.plugins.api;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 
 */
public interface PluginContext {
    /** Список JDBC-конектів */
    Map<String, DbEntry> connections();

    /** Налаштування з plugin.properties. */
    Properties properties();

    /** Логер плагіна */
    Logger logger();
}
