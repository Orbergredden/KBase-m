package app.lib;

import java.util.prefs.Preferences;

/**
 * Клас для роботи з класом Preferences з JavaScript.
 * Збереження та читання усіляких параметрів веб-сторінки 
 */
public class FromJS_Preferences {
	private Preferences prefs;
	
	/**
	 * Constructor
	 */
	public FromJS_Preferences () {
		prefs = Preferences.userNodeForPackage(FromJS_Preferences.class);
	}
	
	/**
	 * TEST
	 */
	public String getTest () {
	    return "test diff";
	}
	//TODO
	
	/**
	 * Читаємо строковий параметер
	 */
	public String get (String tag, String defaultValue) {
		String value = prefs.get(tag, defaultValue);
	    return String.valueOf(value);
	}
	
	/**
	 * Зберігаємо строковий параметер
	 */
	public void put (String tag, String value) {
		prefs.put(tag, value);
	}
}
