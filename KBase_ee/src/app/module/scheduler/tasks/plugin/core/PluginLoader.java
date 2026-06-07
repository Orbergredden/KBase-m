package app.module.scheduler.tasks.plugin.core;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.Properties;

import mc.plugins.api.Plugin;

/**
 * Завантажувач плагінів
 */
public final class PluginLoader {

    public static class Loaded {
        public final String id;
        public final Properties props;
        public final Path dir;
        public final URLClassLoader cl;
        public final Plugin plugin;

        public Loaded(String id, Properties props, Path dir, URLClassLoader cl, Plugin plugin) {
            this.id = id; this.props = props; this.dir = dir; this.cl = cl; this.plugin = plugin;
        }
    }

    public Loaded loadFromDirectory(Path pluginDir) throws Exception {
        // читаємо plugin.properties
        Properties props = new Properties();
        try (var in = Files.newInputStream(pluginDir.resolve("plugin.properties"))) {
            props.load(in);
        }
        String id = required(props, "plugin.id");
        String mainClass = required(props, "plugin.main-class");

        // JAR плагіна
        Path jar = pluginDir.resolve("plugin.jar");
        if (!Files.exists(jar)) throw new IOException("JAR not found: " + jar);

        // окремий клас‑лоадер на плагін
        URLClassLoader cl = new URLClassLoader(new URL[]{ jar.toUri().toURL() },
                PluginLoader.class.getClassLoader());

        Class<?> clazz = Class.forName(mainClass, true, cl);
        Plugin plugin = (Plugin) clazz.getDeclaredConstructor().newInstance();
        return new Loaded(id, props, pluginDir, cl, plugin);
    }

    private static String required(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) throw new IllegalArgumentException("Missing " + key);
        return v.trim();
    }
}