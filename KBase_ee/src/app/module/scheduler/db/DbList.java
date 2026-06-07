package app.module.scheduler.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import app.Main;
import app.exceptions.DataConnectionException;
import app.exceptions.ServiceException;
import app.lib.ShowAppMsg;
import app.lib.crypto.AesUtil;
import app.lib.crypto.SystemDerivedKey;
import app.model.Params;
import app.view.InputPassword_Controller;

/**
 * Реєстр JDBC-конектів, які читаються з properties-файлів
 */
public final class DbList {
	private Params params;
	
    /**  */
    private final Map<String, DbEntry> list = new ConcurrentHashMap<>();
    
    // Ключ для шифрування пароля
    private String key;
    private SecretKey keySys;	// Детермінований ключ з системного ID

    /**  
     * Конструктоh для Шедулера
     */
    public DbList(Params params) {
    	this.params = params;
        key = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try {
			keySys = SystemDerivedKey.deriveAesKey(key, 256);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**  
     * Конструктор для Контроля Завдання-плагіна
     */
    public DbList(DbList dbListMain, Path propsFile) throws IOException, ServiceException {
    	params = dbListMain.params;
        key = dbListMain.getKey();
        keySys = dbListMain.getKeySys();
        
        // завантажуємо файл
    	Properties p = new Properties();
        try (InputStream in = Files.newInputStream(propsFile)) {
            p.load(in);
        }
        
        // читаємо назви конектів і беремо з головного списку
        int count = Integer.parseInt(p.getProperty("db.count", "0"));
        for (int i=1; i<=count; i++) {
        	String prefix = "db." + i + ".";
        	String name = p.getProperty(prefix + "name");
        	
        	DbEntry entryMain = dbListMain.getList().get(name);
        	Connection conn = null;
			try {
				if (! entryMain.connectByPlugin()) {
					conn = getConnection(entryMain);
				}
				
				// додаємо в список
				DbEntry entry = new DbEntry(
						entryMain.name(), entryMain.dbType(), entryMain.url(),
						entryMain.user(), entryMain.password(), entryMain.connectByPlugin(), conn);
				list.put(name, entry);
			} catch (NullPointerException e) {
				throw new ServiceException (
						ServiceException.ERRCODE_OTHERS, "DbList entryMain==null",
						"Шедулер. Відсутній конект '"+name+"' до БД в списку конектів.\n"+
						"Скоріш за все не пройшов тест-коннект і не був доданий до списку.",
						e, 1, null, "NullPointerException");
			}
        }
    }

    /**
     * Додаємо в основний список конекти (власник Шедулер)
     * @param propsFile
     * @throws IOException
     */
    public synchronized void add (Path propsFile) throws IOException {
    	// завантажуємо файл
    	Properties p = new Properties();
        try (InputStream in = Files.newInputStream(propsFile)) {
            p.load(in);
        }
        
        // вичитуємо з файла конекти 
        int count = Integer.parseInt(p.getProperty("db.count", "0"));
        for (int i=1; i<=count; i++) {
        	String prefix = "db." + i + ".";
        	String name = p.getProperty(prefix + "name");
        	String type = p.getProperty(prefix + "type");
            String url  = p.getProperty(prefix + "url");
            String user = p.getProperty(prefix + "username");                 // може бути порожній
            String pass = p.getProperty(prefix + "password");                 // може бути порожній
            boolean connectByPlugin = (p.getProperty(prefix + "connectByPlugin").equals("0")) ? false : true;
            
            // Якщо вже є такий — пропускаємо
            if (list.containsKey(name)) {
                continue;
            }
            
            // якщо немає логіну чи паролю - виводимо вікно для воду
            if ((user.length() == 0) || (pass.length() == 0)) {
            	String isPassword = "";
            	
            	//-------- open window for login and password input
        		try {
        	    	// Загружаем fxml-файл и создаём новую сцену
        			// для всплывающего диалогового окна.
        			FXMLLoader loader = new FXMLLoader();
        			loader.setLocation(Main.class.getResource("view/InputPassword_Layout.fxml"));
        			AnchorPane page = loader.load();
        		
        			// Создаём диалоговое окно Stage.
        			Stage dialogStage = new Stage();
        			dialogStage.setTitle("Пароль");
        			dialogStage.initModality(Modality.WINDOW_MODAL);
        			dialogStage.initOwner(params.getMainStage());
        			Scene scene = new Scene(page);
        			dialogStage.setScene(scene);
        		
        			// Даём контроллеру доступ к главному прилодению.
        			InputPassword_Controller controllerIP = loader.getController();
        	        controllerIP.setParrentObj(user, pass, isPassword, "Ведіть логін та пароль для ДБ "+name);
        	        
        	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
        	        dialogStage.showAndWait();
        	        
        	        // get result
        	        if (controllerIP.isPassword.equals("Ok")) {
        	        	user = controllerIP.login;
            	        pass = controllerIP.password;
        	        }
            	} catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            // шифруємо логін та пароль
	        try {
				user = AesUtil.encrypt(user, keySys);
				pass = AesUtil.encrypt(pass, keySys);
			} catch (Exception e) {
				ShowAppMsg.showAlert("ERROR", "Некоректні дані", "Помилка перенесення логіна та пароля", "");
				e.printStackTrace();
			}
            
			// тестуємо та додаємо в список
			DbEntry cfg = new DbEntry(name, type, url, user, pass, connectByPlugin, null);
			Connection testCon = getConnection(cfg);
			if (testCon != null) {
				list.put(name, cfg);
				try {
					testCon.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				ShowAppMsg.NotificationPopup("test con Ok : "+cfg.name(), "lightblue", 3);
			} else {
				ShowAppMsg.NotificationPopup("test con FAIL : "+cfg.name(), "#ff3030", 3);
			}
        }
    }
    
    /**
     * Завершуємо роботу з об'єктом, в контролі Завдання-плагіну
     */
    public void finish() {
    	// Закрити всі конекти у списку
    	list.forEach((key, entry) -> {
    	    Connection c = entry.conn();
    	    if (c != null) {
    	        try {
    	            c.close();
    	        } catch (Exception e) {
    	            System.err.println("Failed to close connection for: " + key + " -> " + e.getMessage());
    	        }
    	    }
    	});
    }
    
    /**
     * Конектимось до БД
     * @param entry
     * @return
     */
    private Connection getConnection (DbEntry entry) {
    	Connection retVal = null;
    	
    	switch (entry.dbType()) {
    	case "postgres" :
    		try {
    			retVal = DriverManager.getConnection(
        				entry.url(), 
        				AesUtil.decrypt(entry.user(), keySys), 
        				AesUtil.decrypt(entry.password(), keySys));
    		} catch (Exception e) {
    			ShowAppMsg.showAlert("ERROR", "Некоректні дані", "Помилка конекту до БД "+entry.name(), "");
    			e.printStackTrace();
    		}
    		break;
    	case "oracle" :
    		try {
                // Створюємо з'єднання з Oracle БД за допомогою TNS
                String dbURL = "jdbc:oracle:thin:@" + entry.url();
                //conn = DriverManager.getConnection(dbURL, user, password);
                
                Properties properties = new Properties();
                properties.put("user", AesUtil.decrypt(entry.user(), keySys));
                properties.put("password", AesUtil.decrypt(entry.password(), keySys));
                //properties.put("useUnicode", "true");
                //properties.put("characterEncoding", "CL8MSWIN1251");
                

	            // Заповнимо v$session.program (та інші за бажання)
                properties.put("v$session.program", "SQL Developer");
                //properties.put("v$session.machine", "client-pc-01");    // MACHINE (необов’язково)
                //properties.put("v$session.osuser",   "igor");           // OSUSER  (необов’язково)

                retVal = DriverManager.getConnection(dbURL, properties);
                
                retVal.setClientInfo("OCSID.ACTION", "start");
            } catch (Exception e) {
            	ShowAppMsg.showAlert("ERROR", "Некоректні дані", "Помилка конекту до БД "+entry.name(), "");
    			e.printStackTrace();
            }
    	}
    	
    	return retVal;
    }

	public Params getParams() {
		return params;
	}

	public void setParams(Params params) {
		this.params = params;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public SecretKey getKeySys() {
		return keySys;
	}

	public void setKeySys(SecretKey keySys) {
		this.keySys = keySys;
	}

	public Map<String, DbEntry> getList() {
		return list;
	}
}
