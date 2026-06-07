package app.lib;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Класс для запису повідомлень у загальний лог-файл
 */
public class LogFile {
	private static final String FILE_NAME = "kbase.log";
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public LogFile() {
	}
	
	public static void write (String msg) {
		String filename = FILE_NAME;
		Logger logger = Logger.getLogger("KBase");
		FileHandler fileHandler = null;

        try {
            fileHandler = new FileHandler(filename, true);
            logger.addHandler(fileHandler);

            //SimpleFormatter formatter = new SimpleFormatter();
            SimpleFormatter formatter = new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord record) {
                    String formattedDate = dateFormat.format(new Date(record.getMillis()));
                    return formattedDate + " " +
                    	   System.getProperty("user.name") +" "+
                    	   record.getMessage() + "\n";
                }
            };
            fileHandler.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info(msg);

        fileHandler.close();
	}
}
