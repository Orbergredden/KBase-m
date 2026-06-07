package app.lib;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 */
public class PropertiesFile {
	public Properties properties;

    public PropertiesFile(String fileName) {
    	load(fileName);
    }
    
    private void load (String fileName) {
    	properties = new Properties();
        try (FileInputStream input = new FileInputStream(fileName)) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
