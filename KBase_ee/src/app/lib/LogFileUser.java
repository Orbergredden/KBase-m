package app.lib;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import app.model.Params;

/**
 * Класс для запису повідомлень у користувацький лог-файл
 */
public class LogFileUser {
	private static final String FILE_NAME = "kbase_user.log";
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public LogFileUser() {
	}
	
	public static void write (Params params, String msg) {
		String filename = FileUtil.getFullUserFileName(params, FILE_NAME);
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
                    return formattedDate + " " + record.getMessage() + "\n";
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
