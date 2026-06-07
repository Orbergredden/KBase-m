package app.module.scheduler.view;

import app.model.Params;
import app.module.scheduler.TaskControl;
import app.module.scheduler.db.DBScheduler;

/**
 * Батьківський (абстрактний) контролер для всього різномаїття видів Завдань
 * @author Igor Makarevich
 */
public abstract class TaskDetail_Simple_Controller {
	protected Params params;
	protected TaskList_Controller parrentObj;
	protected DBScheduler db;
	protected TaskControl taskControl;
	
	/**
     * Конструктор.
     */
	TaskDetail_Simple_Controller () {      }
    
    /**
     * Вызывается родительским обьектом, которое даёт на себя ссылку.
     * Инициализирует переменные и контролы.
     */
    public void setParams(Params params, TaskControl taskControl) {
    	this.params     = params;
        this.parrentObj = (TaskList_Controller)params.getParentObj();
        this.db         = params.getScheduler().getDb();
        this.taskControl= taskControl;
        
        // init controls
        initControlsValue();
    }
	
    /**
     * Инициализирует контролы значениями  
     */
    abstract void initControlsValue();
	
    /**
     * Очищуємо деякі контроли для режиму додавання Завдання
     */
    abstract void clearControlsValueForAddMode();
    
    /**
     * Перевіряємо в обов'язкових контролах наявність інформації
     */
    abstract boolean checkControlsValue();
    
    /**
     * ініціюємо похідні специфіки з контролів у обьект-параметер tc 
     * (для нового завдання)
     */
    abstract void initSrecificVariables (TaskControl tc);
    
    /**
     * оновлюємо похідні специфіки з контролів у обьект-параметер tc 
     * (для оновлення інформації існуючого завдання)
     */
    abstract void updateSrecificVariables (TaskControl tc);
    
    
}
