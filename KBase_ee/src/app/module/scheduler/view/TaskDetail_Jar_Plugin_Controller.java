package app.module.scheduler.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import app.exceptions.KBase_DbConnEx;
import app.lib.ShowAppMsg;
import app.module.scheduler.TaskControl;
import app.module.scheduler.TaskControl_Jar_Plugin;
import app.module.scheduler.TaskControl_MsgBase_ShowMessage;
import app.module.scheduler.db.DBScheduler;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Контролер форми з деталізацією інформації для "JAR-plugin"
 */
public class TaskDetail_Jar_Plugin_Controller extends TaskDetail_Simple_Controller {
	@FXML
	private TextField details_TextField_name;
	
	private TaskControl_Jar_Plugin tc;
	
	/**
	 * constructor
	 */
	public TaskDetail_Jar_Plugin_Controller () {
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
		tc = (TaskControl_Jar_Plugin)taskControl;
		
		details_TextField_name.setText(tc.getName());
	}
	
	/**
     * Очищуємо деякі контроли для режиму додавання Завдання
     */
	@Override
    public void clearControlsValueForAddMode() {
		
	}
	
	/**
     * Перевіряємо в обов'язкових контролах наявність інформації
     */
	@Override
    boolean checkControlsValue() {
		String pluginPath = null;
    	boolean retVal = true;
    	
    	if ((details_TextField_name.getText() == null) || details_TextField_name.getText().equals("")) {
    		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не вказана назва плагіну", "Вкажіть");
    		return false;
        }
    	
   		pluginPath = params.getConfig().getItemValue("Scheduler", "scheduler.plugin.path");
   		if (pluginPath == null) {
   			params.getConfig().add(
        				"Scheduler", 
        				"scheduler.plugin.path",
        				"Шлях до плагінів Шедулера",
        				"plugins/",
        				LocalDate.now(),
        				true,
        				true
        				);
   			params.getConfig().saveToFile();
   		}

   		
   		
   		
    	//TODO - перевіряти чи існує плагін з вказаною назвою
    	
    	return retVal;
    }
	
	/**
     * ініціюємо похідні специфіки з контролів у обьект-параметер tc
     */
	@Override
    void initSrecificVariables (TaskControl tc) {
		TaskControl_Jar_Plugin ta = (TaskControl_Jar_Plugin) tc;
		
		ta.setName(details_TextField_name.getText());
    }
	
	/**
     * оновлюємо похідні специфіки з контролів у обьект-параметер tc 
     * (для оновлення інформації існуючого завдання)
     */
	@Override
    void updateSrecificVariables (TaskControl tc) {
		TaskControl_Jar_Plugin ta = (TaskControl_Jar_Plugin) tc;
		
		ta.setName(details_TextField_name.getText());
    }
}
