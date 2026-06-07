package app.module.scheduler.tasks.plugin.core;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.logging.*;

import mc.plugins.api.DbEntry;
import mc.plugins.api.PluginContext;

/**
 * Контекст для плагіна (логер + конекти)
 */
public final class PluginContextImpl implements PluginContext, AutoCloseable {
    private final Map<String, DbEntry> conns;
    private final Properties props;
    private final Logger logger;
    private final Handler fileHandler;

    public PluginContextImpl(String pluginId, Path pluginDir,
                             Map<String, DbEntry> connections,
                             Properties properties) {
        this.conns = connections;
        this.props = properties;

        this.logger = Logger.getLogger("plugin." + pluginId);
        // окремий файл у папці плагіна
        try {
            Path logPath = pluginDir.resolve(pluginId + ".log");
            this.fileHandler = new FileHandler(logPath.toString(), true);
            this.fileHandler.setFormatter(new SimpleFormatter());
            this.logger.setUseParentHandlers(false);
            this.logger.addHandler(this.fileHandler);
            this.logger.setLevel(Level.INFO);
        } catch (Exception e) {
            throw new RuntimeException("Cannot init logger for " + pluginId, e);
        }
    }

    @Override public Map<String, DbEntry> connections() { return conns; }
    @Override public Properties properties() { return props; }
    @Override public Logger logger() { return logger; }

    @Override public void close() {
        // закриваємо file handler
        if (fileHandler != null) {
            fileHandler.flush();
            fileHandler.close();
            logger.removeHandler(fileHandler);
        }
    }
}
