package app.module.scheduler.view;

import java.time.format.DateTimeFormatter;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import msgBase.model.Message;

/**
 * Вікно з повідомленням.
 */
public class Task_MsgBase_ShowMessage_Controller {
	private Stage stage;
	private Message msg;
	
	@FXML
	private Label label_MessageType;
	@FXML
	private Label label_SystemName;
	@FXML
	private Label label_MessageDate;
	@FXML
	private Label label_Id;
	@FXML
	private TextField textField_Subject;
	@FXML
	private TextArea textArea_Message;
	@FXML
	private Button button_Close;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public Task_MsgBase_ShowMessage_Controller () {
    	
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
    	
    }
    
    /**
     * Вызывается родительским обьектом, который даёт ссылки.
     * Инициализирует контролы на слое.
     * 
     * @param 
     */
    public void setParrentObj(Stage stage, Message msg) {
    	this.stage = stage;
    	this.msg = msg;
        
        // init controls
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы
     */
    private void initControlsValue() {
    	label_MessageType.setText(msg.getMessageTypeName());
    	label_SystemName.setText(msg.getSystemName());
    	label_MessageDate.setText(msg.getMessageDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    	label_Id.setText(String.valueOf(msg.getId()));
    	textField_Subject.setText(msg.getSubject());
    	textArea_Message.setText(msg.getMessage());
    }
	
    /**
     * Вызывается при нажатии на кнопке "Отмена"
     */
    @FXML
    private void handleButtonClose() {
    	//-------- save size
    	Preferences prefs = Preferences.userNodeForPackage(Task_MsgBase_ShowMessage_Controller.class);
        prefs.putDouble("stage_Task_MsgBase_ShowMessage_Controller_Width", stage.getWidth());
        prefs.putDouble("stage_Task_MsgBase_ShowMessage_Controller_Height",stage.getHeight());
    	
        stage.close();
    }

	public Message getMsg() {
		return msg;
	}
	public void setMsg(Message msg) {
		this.msg = msg;
	}
}
