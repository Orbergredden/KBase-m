package app.module.scheduler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.prefs.Preferences;

import javax.crypto.SecretKey;

import app.lib.ShowAppMsg;
import app.lib.crypto.AesUtil;
import app.lib.crypto.SystemDerivedKey;
import app.model.Params;
import app.module.scheduler.view.Task_MsgBase_ShowMessage_Controller;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import msgBase.db.DBMsgBasePg;
import msgBase.db.DBMsgBasePgEx;
import msgBase.model.Message;

/**
 * Клас управління Завданням "MsgBase. Показ повідомлення."
 */
public class TaskControl_MsgBase_ShowMessage extends TaskControl {
	// останій ід показаного повідомлення
	private long lastMsgId;
	
	// УРЛ з'єднання з БД msgBase
	private String dbMsgBaseURL;
	//
	private String login;
	//
	private String password;
	
	// перелік кодів систем для показу повідомлень
	private String systemSrcList;
	// перелік кодів систем для ігнору повідомлень
	private String systemSrcListIgnore;
	
	// вибір, звідки починать показ повідомлень : показувати з останього ід, з вказаного ід, з вказаної дати/часу, з моменту запуску Завдання
	private int typeStartTask;
	// початок, вказаний ід
	private long startMsgId;
	// початок, вказана дата та час, формат "YYYY-MM-DD hh:mi:ss"
	private String startDateTime;
	// початок, дата та час старту Завдання
	private LocalDateTime startTaskDatetIme;
	
	// Унікальний простір імен (для шифрування)
    private String appNs = "TaskControl_MsgBase_ShowMessage";
    
    // чи виконується зараз завдання
    private boolean isTaskRunning;
	
    /**
     * Конструктор.
     */
	TaskControl_MsgBase_ShowMessage () {
		isTaskRunning = false;
	}
    
	/**
     * Конструктор.
     */
	TaskControl_MsgBase_ShowMessage (long taskId, Params params, int initFlag) {
		super(taskId, params, initFlag);
		
		if (initFlag == TaskControl.INIT_FROM_DB) {
			try {
				lastMsgId = Long.parseLong(db.taskControlSpecificGetStr(id, "lastMsgId"));
				dbMsgBaseURL = db.taskControlSpecificGetStr(id, "dbMsgBaseURL");
				login = db.taskControlSpecificGetStr(id, "login");
				password = db.taskControlSpecificGetStr(id, "password");
				systemSrcList = db.taskControlSpecificGetStr(id, "systemSrcList");
				systemSrcListIgnore = db.taskControlSpecificGetStr(id, "systemSrcListIgnore");
				typeStartTask = Integer.parseInt(db.taskControlSpecificGetStr(id, "typeStartTask"));
				startMsgId = Long.parseLong(db.taskControlSpecificGetStr(id, "startMsgId"));
				startDateTime = db.taskControlSpecificGetStr(id, "startDateTime");
			} catch (NumberFormatException e) {
				e.printStackTrace();
	    		ShowAppMsg.showAlert("ERROR", "Task msgBase_ShowMessage error", 
	    				"Помилка конвнртації строки в число при ініціалізації з БД", e.getMessage());
			}
		}
		
		fxmlFileName = "module/scheduler/view/TaskDetail_MsgBase_ShowMessage.fxml";
		isTaskRunning = false;
	}
	
	/**
	 * додаємо інформацію в БД
	 */
	public void add () {
		super.add();
		db.taskControlSpecificAdd(id, "lastMsgId", 0, String.valueOf(lastMsgId));
		db.taskControlSpecificAdd(id, "dbMsgBaseURL", 0, dbMsgBaseURL);
		db.taskControlSpecificAdd(id, "login", 0, login);
		db.taskControlSpecificAdd(id, "password", 0, password);
		db.taskControlSpecificAdd(id, "systemSrcList", 0, systemSrcList);
		db.taskControlSpecificAdd(id, "systemSrcListIgnore", 0, systemSrcListIgnore);
		db.taskControlSpecificAdd(id, "typeStartTask", 0, String.valueOf(typeStartTask));
		db.taskControlSpecificAdd(id, "startMsgId", 0, String.valueOf(startMsgId));
		db.taskControlSpecificAdd(id, "startDateTime", 0, startDateTime);
	}
	
	/**
	 * оновлюємо інформацію в БД
	 */
	public void update () {
		super.update();
		db.taskControlSpecificUpdate(id, "lastMsgId", 0, String.valueOf(lastMsgId));
		db.taskControlSpecificUpdate(id, "dbMsgBaseURL", 0, dbMsgBaseURL);
		db.taskControlSpecificUpdate(id, "login", 0, login);
		db.taskControlSpecificUpdate(id, "password", 0, password);
		db.taskControlSpecificUpdate(id, "systemSrcList", 0, systemSrcList);
		db.taskControlSpecificUpdate(id, "systemSrcListIgnore", 0, systemSrcListIgnore);
		db.taskControlSpecificUpdate(id, "typeStartTask", 0, String.valueOf(typeStartTask));
		db.taskControlSpecificUpdate(id, "startMsgId", 0, String.valueOf(startMsgId));
		db.taskControlSpecificUpdate(id, "startDateTime", 0, startDateTime);
	}
	
	/**
	 * вилучаємо інформацію з БД
	 */
	public void delete () {
		super.delete();
		db.taskControlSpecificDelete(id, "lastMsgId");
		db.taskControlSpecificDelete(id, "dbMsgBaseURL");
		db.taskControlSpecificDelete(id, "login");
		db.taskControlSpecificDelete(id, "password");
		db.taskControlSpecificDelete(id, "systemSrcList");
		db.taskControlSpecificDelete(id, "systemSrcListIgnore");
		db.taskControlSpecificDelete(id, "typeStartTask");
		db.taskControlSpecificDelete(id, "startMsgId");
		db.taskControlSpecificDelete(id, "startDateTime");
	}
	
	/**
	 * запускаємо Завдання
	 */
	void start () {
        // Перевірка, чи не перерваний потік
        if (Thread.currentThread().isInterrupted()) {
            System.out.println("Завдання перервано перед стартом процесу.");
            return;
        }

        Platform.runLater(() -> readAndShowMessage());
	}
	
	/**
	 * 
	 */
	private void readAndShowMessage() {
		if (isTaskRunning) return;
		isTaskRunning = true;
		
		DBMsgBasePg dbPgView = null;
		
		//======== connect to DB
		SecretKey key = null;
		try {
			key = SystemDerivedKey.deriveAesKey(appNs, 256);
			dbPgView = new DBMsgBasePg (dbMsgBaseURL, AesUtil.decrypt(login, key), AesUtil.decrypt(password, key));
		} catch (DBMsgBasePgEx e) {
			ShowAppMsg.showAlert("ERROR", "Конект до БД Повідомлень", "Помилка (DBMsgBasePgEx)", e.getMessage());
			e.printStackTrace();
			
			isTaskRunning = false;
			return;
		} catch (Exception e) {
			ShowAppMsg.showAlert("ERROR", "Конект до БД Повідомлень", "Помилка (Exception)", e.getMessage());
			e.printStackTrace();
		}
		
		//======== знаходимо ід за умовами старту
		switch (typeStartTask) {
		case 2 :  // з вказаного ід
			lastMsgId = startMsgId;
			typeStartTask = 1;
			break;
		case 3 :  // з вказаної дати та часу
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime dateTime = LocalDateTime.parse(startDateTime, formatter);
			try {
				lastMsgId = dbPgView.messagesGetLastIdByDateTime(dateTime);
			} catch (DBMsgBasePgEx e) {
				ShowAppMsg.showAlert("ERROR", "Шукаємо id повідомлення з БД Повідомлень", "Помилка (DBMsgBasePgEx)", e.getMessage());
				e.printStackTrace();
				e.parentEx.printStackTrace();
			}
			typeStartTask = 1;
			break;
		case 4 :  // з часу старта завдання
			try {
				lastMsgId = dbPgView.messagesGetLastIdByDateTime(startTaskDatetIme);
			} catch (DBMsgBasePgEx e) {
				ShowAppMsg.showAlert("ERROR", "Шукаємо id повідомлення з БД Повідомлень", "Помилка (DBMsgBasePgEx)", e.getMessage());
				e.printStackTrace();
			}
			typeStartTask = 1;
			break;
		}
		
		//======== show message
		while (true) {
			//-------- read message
	        Message msg = null;
			try {
				msg = dbPgView.messagesReadNext(lastMsgId, systemSrcList, systemSrcListIgnore);
			} catch (DBMsgBasePgEx e) {
				ShowAppMsg.showAlert("ERROR", "Читаємо повідомлення з БД Повідомлень", "Помилка (DBMsgBasePgEx)", e.getMessage());
				e.printStackTrace();
			}
	        if (msg == null) {
	            break;
	        }
	        
	        //-------- show message
	        //ShowAppMsg.showAlert("INFORMATION", msg.getSubject(), msg.getMessage(), msg.getMessageUser());
	        
	        if (msg.getMessageTypeId() == 4) {  // popup
	        	ShowAppMsg.NotificationPopup (msg.getMessage(), "peachpuff", 5);
	        } else {
	        	showMessage(msg);
	        }
	        
	        //-------- update lastMsgId...
	        lastMsgId = msg.getId();
	        update();
	    }
		
		//======== disconnect
		try {
			dbPgView.close();
		} catch (DBMsgBasePgEx e) {
			e.printStackTrace();
		}
		
		//
		isTaskRunning = false;
	}
	
	/**
	 * 
	 * @param msg
	 */
	private void showMessage (Message msg) {
		try {
            FXMLLoader loader = new FXMLLoader(
                //getClass().getResource("module/scheduler/view/Task_MsgBase_ShowMessage.fxml")
            	getClass().getResource("view/Task_MsgBase_ShowMessage.fxml")
            );
            Parent root = loader.load();
            Task_MsgBase_ShowMessage_Controller controller = loader.getController();

            Stage stage = new Stage(StageStyle.UTILITY); // або DECORATED/UNDECORATED — за потреби
            stage.setTitle("Повідомлення від msgBase");
            stage.setScene(new Scene(root));

            // ВАЖЛИВО: незалежність від головного вікна
            // НЕ викликайте stage.initOwner(...)

            // Немодальність
            stage.initModality(Modality.NONE);

            // Завжди поверх інших вікон (OS‑level topmost)
            stage.setAlwaysOnTop(true);

            // Опційно:
            stage.setResizable(true);
            // stage.centerOnScreen(); // якщо потрібно центрувати
            
            Preferences prefs = Preferences.userNodeForPackage(Task_MsgBase_ShowMessage_Controller.class);
			stage.setWidth(prefs.getDouble("stage_Task_MsgBase_ShowMessage_Controller_Width", 380));
			stage.setHeight(prefs.getDouble("stage_Task_MsgBase_ShowMessage_Controller_Height", 400));
			
			// позіціонуємо
	        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
	        stage.setX(screenBounds.getMaxX() - stage.getWidth() - 10);
	        stage.setY(screenBounds.getMaxY() - stage.getHeight() - 10);
            
            controller.setParrentObj(stage, msg); // ваш метод у контролері

            //stage.show();
            stage.showAndWait();
            stage.toFront();  // на випадок, якщо було позаду
        } catch (IOException e) {
            e.printStackTrace();
            ShowAppMsg.showAlert("ERROR", "Показуємо повідомлення з БД Повідомлень", "Неможливо відкрити вікно", e.getMessage());
        }
	}
	
	/**
	 * Виконується перед стартом (не виконанням) Завдання
	 */
	void beforeStart () {
		startTaskDatetIme = LocalDateTime.now();
	}
	
	/**
	 * Виконуєть одразу після встановлення статусу Завдання "Disable"
	 */
	public void afterDisable () {
	}
	
	/**
	 * Створюємо копію контролу
	 * @return
	 */
	@Override
    public TaskControl_MsgBase_ShowMessage copy (long taskId) {
        return (TaskControl_MsgBase_ShowMessage) super.copy(taskId);
    }
	
	/**
	 * Створюємо пустий екземпляр 
	 */
	@Override
    protected TaskControl createEmptyInstance() {
        return new TaskControl_MsgBase_ShowMessage();
    }
	
	/**
	 * Копіюємо специфічну частину
	 * @param src
	 * @param dest
	 */
	@Override
    protected void copySpecificFrom(TaskControl src, TaskControl dest) {
		TaskControl_MsgBase_ShowMessage s = (TaskControl_MsgBase_ShowMessage) src;
		TaskControl_MsgBase_ShowMessage d = (TaskControl_MsgBase_ShowMessage) dest;
		
		d.setLastMsgId(s.getLastMsgId());
		d.setDbMsgBaseURL(new String(s.getDbMsgBaseURL()));
		d.setLogin(new String(s.getLogin()));
		d.setPassword(new String(s.getPassword()));
		d.setSystemSrcList(new String(s.getSystemSrcList()));
		d.setSystemSrcListIgnore(s.getSystemSrcListIgnore());
		d.setTypeStartTask(s.getTypeStartTask());
		d.setStartMsgId(s.getStartMsgId());
		d.setStartDateTime(new String(s.getStartDateTime()));
    }

	public long getLastMsgId() {
		return lastMsgId;
	}

	public void setLastMsgId(long lastMsgId) {
		this.lastMsgId = lastMsgId;
	}

	public String getDbMsgBaseURL() {
		return dbMsgBaseURL;
	}
	public void setDbMsgBaseURL(String dbMsgBaseURL) {
		this.dbMsgBaseURL = dbMsgBaseURL;
	}
	
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSystemSrcList() {
		return systemSrcList;
	}

	public void setSystemSrcList(String systemSrcList) {
		this.systemSrcList = systemSrcList;
	}

	public String getSystemSrcListIgnore() {
		return systemSrcListIgnore;
	}

	public void setSystemSrcListIgnore(String systemSrcListIgnore) {
		this.systemSrcListIgnore = systemSrcListIgnore;
	}

	public int getTypeStartTask() {
		return typeStartTask;
	}

	public void setTypeStartTask(int typeStartTask) {
		this.typeStartTask = typeStartTask;
	}

	public long getStartMsgId() {
		return startMsgId;
	}

	public void setStartMsgId(long startMsgId) {
		this.startMsgId = startMsgId;
	}

	public String getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}

	public String getAppNs() {
		return appNs;
	}
}
