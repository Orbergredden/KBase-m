package app.module.scheduler.view;

import app.module.scheduler.TaskControl;
import javafx.fxml.FXML;

/**
 * Контролер форми з деталізацією інформації для "(prg) Збереження усіх редагувань"
 */
public class TaskDetail_Prg_SaveChanges_Controller extends TaskDetail_Simple_Controller {
	
	/**
	 * constructor
	 */
	public TaskDetail_Prg_SaveChanges_Controller () {
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
    	
    	return retVal;
    }
	
	/**
     * ініціюємо похідні специфіки з контролів у обьект-параметер tc
     */
	@Override
    void initSrecificVariables (TaskControl tc) {
		
    }
	
	/**
     * оновлюємо похідні специфіки з контролів у обьект-параметер tc 
     * (для оновлення інформації існуючого завдання)
     */
	@Override
    void updateSrecificVariables (TaskControl tc) {
		
    }
}
