package app.module.scheduler.view;

import app.lib.ShowAppMsg;
import app.module.scheduler.TaskControl;
import app.module.scheduler.TaskControl_AppConsole;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Контролер форми з деталізацією інформації для Косольних додатків
 */
public class TaskDetail_AppConsole_Controller extends TaskDetail_Simple_Controller{
	@FXML
	private Label details_Label_id;
	@FXML
	private TextArea details_TextArea_appPath;
	@FXML
	private CheckBox details_CheckBox_isHomeDir;
	
	private TaskControl_AppConsole tc_ac;
	
	/**
	 * constructor
	 */
	public TaskDetail_AppConsole_Controller () {
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
		tc_ac = (TaskControl_AppConsole)taskControl;
		details_Label_id.setText(String.valueOf(tc_ac.getAppConsoleId()));
		details_TextArea_appPath.setWrapText(true);
		details_TextArea_appPath.setText(tc_ac.getAppPath());
		details_CheckBox_isHomeDir.setSelected(tc_ac.getIsHomeDir());
	}
	
	/**
     * Очищуємо деякі контроли для режиму додавання Завдання
     */
	@Override
    void clearControlsValueForAddMode() {
		details_Label_id.setText("");
	}
	
	/**
     * Перевіряємо в обов'язкових контролах наявність інформації
     */
	@Override
    boolean checkControlsValue() {
    	boolean retVal = true;
    	
    	if ((details_TextArea_appPath.getText().equals("") || (details_TextArea_appPath.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не заповнений шлях з ім'ям додатку", "Вкажіть шлях з ім'ям додатку");
    		return false;
        }
    	
    	return retVal;
    }
    
    /**
     * ініціюємо похідні специфіки з контролів у обьект-параметер tc
     */
	@Override
    void initSrecificVariables (TaskControl tc) {
    	TaskControl_AppConsole ta = (TaskControl_AppConsole) tc;
    	
    	ta.setAppConsoleId(db.taskControlAppConsoleNextId());
    	ta.setAppPath(details_TextArea_appPath.getText());
    	ta.setIsHomeDir(details_CheckBox_isHomeDir.isSelected());
    }
	
	/**
     * оновлюємо похідні специфіки з контролів у обьект-параметер tc 
     * (для оновлення інформації існуючого завдання)
     */
	@Override
    void updateSrecificVariables (TaskControl tc) {
		TaskControl_AppConsole ta = (TaskControl_AppConsole) tc;
    	
    	ta.setAppPath(details_TextArea_appPath.getText());
    	ta.setIsHomeDir(details_CheckBox_isHomeDir.isSelected());
    }
}
