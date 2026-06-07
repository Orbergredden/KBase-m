package app.module.scheduler.view;

import app.lib.ShowAppMsg;
import app.module.scheduler.TaskControl;
import app.module.scheduler.TaskControl_Prg_SaveState;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Контролер форми з деталізацією інформації для "(prg) Збереження стану програми"
 */
public class TaskDetail_Prg_SaveState_Controller extends TaskDetail_Simple_Controller {
	@FXML
	private TextField details_TextField_filename;
	
	private TaskControl_Prg_SaveState tc;
	
	/**
	 * constructor
	 */
	public TaskDetail_Prg_SaveState_Controller () {
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
		tc = (TaskControl_Prg_SaveState)taskControl;
		details_TextField_filename.setText(tc.getFilename());
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
    	
    	if ((details_TextField_filename.getText().equals("") || (details_TextField_filename.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не вказане ім'я файлу", "Вкажіть ім'я файлу");
    		return false;
        }
    	
    	return retVal;
    }
	
	/**
     * ініціюємо похідні специфіки з контролів у обьект-параметер tc
     */
	@Override
    void initSrecificVariables (TaskControl tc) {
		TaskControl_Prg_SaveState ta = (TaskControl_Prg_SaveState) tc;
		
		ta.setFilename(details_TextField_filename.getText());
    }
	
	/**
     * оновлюємо похідні специфіки з контролів у обьект-параметер tc 
     * (для оновлення інформації існуючого завдання)
     */
	@Override
    void updateSrecificVariables (TaskControl tc) {
		TaskControl_Prg_SaveState ta = (TaskControl_Prg_SaveState) tc;
		
		ta.setFilename(details_TextField_filename.getText());
    }
}
