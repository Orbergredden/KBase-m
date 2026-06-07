package mc.plugins.api;

/**
 * 
 */
public interface Plugin {
    /**
     * Ядро викликає цю дію.
     */
    void run(PluginContext ctx) throws Exception;
}
