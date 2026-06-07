package app.module.scheduler.view;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.crypto.SecretKey;

import app.Main;
import app.lib.ShowAppMsg;
import app.lib.crypto.AesUtil;
import app.lib.crypto.SystemDerivedKey;
import app.module.scheduler.TaskControl;
import app.module.scheduler.TaskControl_MsgBase_ShowMessage;
import app.view.InputPassword_Controller;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Контролер форми з деталізацією інформації для "(msgBase) Показ повідомлень"
 */
public class TaskDetail_MsgBase_ShowMessage_Controller extends TaskDetail_Simple_Controller {
	@FXML
	private Label details_Label_lastMsgId;
	
	@FXML
	private TextField details_TextField_dbMsgBaseURL;
	@FXML
	private Label details_Label_login;
	@FXML
	private Label details_Label_password;
	@FXML
	private Button details_Button_EnterLoginPassword;
	
	@FXML
	private TextField details_TextField_systemSrcList;
	@FXML
	private TextField details_TextField_systemSrcListIgnore;
	
	@FXML
	private RadioButton details_RadioButton_typeStartTask_lastMsgId;
	@FXML
	private RadioButton details_RadioButton_typeStartTask_startMsgId;
	@FXML
	private RadioButton details_RadioButton_typeStartTask_startDateTime;
	@FXML
	private RadioButton details_RadioButton_typeStartTask_startTask;
	@FXML
	private TextField details_TextField_startMsgId;
	@FXML
	private TextField details_TextField_startDateTime;
	
	private TaskControl_MsgBase_ShowMessage tc;
	
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * constructor
	 */
	public TaskDetail_MsgBase_ShowMessage_Controller () {
		super();
	}
	
	/**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {     }
    
    /**
     * Ініціалізуємо контроли значеннями  
     */
	@Override
	void initControlsValue() {
		tc = (TaskControl_MsgBase_ShowMessage)taskControl;
		
		details_Label_lastMsgId.setText(Long.toString(tc.getLastMsgId()));
		
		details_TextField_dbMsgBaseURL.setText(tc.getDbMsgBaseURL());
		details_Label_login.setText(tc.getLogin());
		details_Label_password.setText(tc.getPassword());
		
		details_TextField_systemSrcList.setText(tc.getSystemSrcList());
		details_TextField_systemSrcListIgnore.setText(tc.getSystemSrcListIgnore());
		
		details_RadioButton_typeStartTask_lastMsgId.setSelected(false);
		details_RadioButton_typeStartTask_startMsgId.setSelected(false);
		details_RadioButton_typeStartTask_startDateTime.setSelected(false);
		switch (tc.getTypeStartTask()) {
			case 1 :
				details_RadioButton_typeStartTask_lastMsgId.setSelected(true);
				break;
			case 2 :
				details_RadioButton_typeStartTask_startMsgId.setSelected(true);
				break;
			case 3 :
				details_RadioButton_typeStartTask_startDateTime.setSelected(true);
				break;
			case 4 :
				details_RadioButton_typeStartTask_startTask.setSelected(true);
				break;
		}
		
		details_TextField_startMsgId.setText(Long.toString(tc.getStartMsgId()));
		details_TextField_startDateTime.setText(tc.getStartDateTime());
	}
	
	/**
     * Очищуємо деякі контроли для режиму додавання Завдання
     */
	@Override
    void clearControlsValueForAddMode() {
		
	}
	
	/**
     * Перевіряємо в обов'язкових контролах наявність інформації
     */
	@Override
    boolean checkControlsValue() {
    	boolean retVal = true;
    	
    	if ((details_TextField_dbMsgBaseURL.getText() == null) || details_TextField_dbMsgBaseURL.getText().equals("")) {
    		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не вказаний URL до БД повідомлень", "Вкажіть");
    		return false;
        }
    	
    	if (!details_RadioButton_typeStartTask_lastMsgId.isSelected() &&
    		!details_RadioButton_typeStartTask_startMsgId.isSelected() &&
    		!details_RadioButton_typeStartTask_startDateTime.isSelected() &&
    		!details_RadioButton_typeStartTask_startTask.isSelected()) {
    		ShowAppMsg.showAlert("WARNING", "Некоректні дані", "Не вибраний тип початку показу", "");
    	}
    	
    	if (details_RadioButton_typeStartTask_startMsgId.isSelected()) {
    		if ((details_TextField_startMsgId.getText() == null) || details_TextField_startMsgId.getText().equals("")) {
        		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не вказаний startMsgId", "Вкажіть");
        		return false;
            }
    		try {
    		    Long.parseLong(details_TextField_startMsgId.getText());
    		} catch (NumberFormatException e) {
    			ShowAppMsg.showAlert("WARNING", "Некоректні дані", "Некоректно заповнений startMsgId", "");
        		return false;
    		}
    	}
    	
    	if (details_RadioButton_typeStartTask_startDateTime.isSelected()) {
    		if ((details_TextField_startDateTime.getText() == null) || details_TextField_startDateTime.getText().equals("")) {
        		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не вказаний startMsgId", "Вкажіть");
        		return false;
            }
    		try {
    			LocalDateTime.parse(details_TextField_startDateTime.getText(), FORMATTER);
    		} catch (DateTimeParseException e) {
    			ShowAppMsg.showAlert("WARNING", "Некоректні дані", "Некоректно заповнений startDateTime", "");
    			return false;
    		}
    	}
    	
    	return retVal;
    }
	
	/**
     * ініціюємо похідні специфіки з контролів у обьект-параметер tc
     */
	@Override
    void initSrecificVariables (TaskControl tc) {
		TaskControl_MsgBase_ShowMessage ta = (TaskControl_MsgBase_ShowMessage) tc;
		
		ta.setLastMsgId(Long.parseLong(details_Label_lastMsgId.getText()));
		ta.setDbMsgBaseURL(details_TextField_dbMsgBaseURL.getText());
		ta.setLogin(details_Label_login.getText());
		ta.setPassword(details_Label_password.getText());
		
		ta.setSystemSrcList(details_TextField_systemSrcList.getText());
		ta.setSystemSrcListIgnore(details_TextField_systemSrcListIgnore.getText());

		if (details_RadioButton_typeStartTask_lastMsgId.isSelected())      ta.setTypeStartTask(1);
		if (details_RadioButton_typeStartTask_startMsgId.isSelected())     ta.setTypeStartTask(2);
		if (details_RadioButton_typeStartTask_startDateTime.isSelected())  ta.setTypeStartTask(3);
		if (details_RadioButton_typeStartTask_startTask.isSelected())      ta.setTypeStartTask(4);
		
		ta.setStartMsgId(Long.parseLong(details_TextField_startMsgId.getText()));
		ta.setStartDateTime(details_TextField_startDateTime.getText());
    }
	
	/**
     * оновлюємо похідні специфіки з контролів у обьект-параметер tc 
     * (для оновлення інформації існуючого завдання)
     */
	@Override
    void updateSrecificVariables (TaskControl tc) {
		TaskControl_MsgBase_ShowMessage ta = (TaskControl_MsgBase_ShowMessage) tc;
		
		ta.setLastMsgId(Long.parseLong(details_Label_lastMsgId.getText()));
		ta.setDbMsgBaseURL(details_TextField_dbMsgBaseURL.getText());
		ta.setLogin(details_Label_login.getText());
		ta.setPassword(details_Label_password.getText());
		
		ta.setSystemSrcList(details_TextField_systemSrcList.getText());
		ta.setSystemSrcListIgnore(details_TextField_systemSrcListIgnore.getText());

		if (details_RadioButton_typeStartTask_lastMsgId.isSelected())      ta.setTypeStartTask(1);
		if (details_RadioButton_typeStartTask_startMsgId.isSelected())     ta.setTypeStartTask(2);
		if (details_RadioButton_typeStartTask_startDateTime.isSelected())  ta.setTypeStartTask(3);
		if (details_RadioButton_typeStartTask_startTask.isSelected())      ta.setTypeStartTask(4);
		
		ta.setStartMsgId(Long.parseLong(details_TextField_startMsgId.getText()));
		ta.setStartDateTime(details_TextField_startDateTime.getText());
    }
	
	/**
     * У віконечку водимо логін та пароль та потім шифруємо
     */
    @FXML
    private void handleButtonEnterLoginPassword() {
    	String login = null;
    	String password = null;
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
	        controllerIP.setParrentObj(login, password, isPassword, "Ведіть логін та пароль");
	        
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        // get result
	        isPassword = controllerIP.isPassword;
	        login = controllerIP.login;
	        password = controllerIP.password;
    	} catch (IOException e) {
            e.printStackTrace();
        }
    	
		//-------- check password is present
		if (isPassword.equals("Ok")) {
			TaskControl_MsgBase_ShowMessage ta = (TaskControl_MsgBase_ShowMessage) tc;
			// Детермінований ключ з системного ID (нічого не зберігаємо)
	        SecretKey key = null;
			
	        try {
				key = SystemDerivedKey.deriveAesKey(ta.getAppNs(), 256);
				details_Label_login.setText(AesUtil.encrypt(login, key));
				details_Label_password.setText(AesUtil.encrypt(password, key));
			} catch (Exception e) {
				ShowAppMsg.showAlert("ERROR", "Некоректні дані", "Помилка перенесення логіна та пароля", "");
				e.printStackTrace();
			}
		}
    }
}
