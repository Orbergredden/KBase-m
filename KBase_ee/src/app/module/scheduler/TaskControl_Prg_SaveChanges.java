package app.module.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.lib.crypto.AesUtil;
import app.model.Params;
import app.module.scheduler.tasks.plugin.core.PluginContextImpl;
import javafx.application.Platform;
import mc.plugins.api.Plugin;

/**
 * Клас управління Завданням "Програма. Збереження всіх редагувань."
 */
public class TaskControl_Prg_SaveChanges extends TaskControl {
	// чи виконується зараз завдання
    private boolean isTaskRunning;
	
	/**
     * Конструктор.
     */
	TaskControl_Prg_SaveChanges () {
		isTaskRunning = false;
	}
	
	/**
     * Конструктор.
     */
	TaskControl_Prg_SaveChanges (long taskId, Params params, int initFlag) {
		super(taskId, params, initFlag);
		
		fxmlFileName = "module/scheduler/view/TaskDetail_Prg_SaveChanges.fxml";
		isTaskRunning = false;
	}
	
	/**
	 * додаємо інформацію в БД
	 */
	public void add () {
		super.add();
	}
	
	/**
	 * оновлюємо інформацію в БД
	 */
	public void update () {
		super.update();
	}
	
	/**
	 * вилучаємо інформацію з БД
	 */
	public void delete () {
		super.delete();
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

        Platform.runLater(() -> run());
	}
	
	/**
	 * 
	 */
	private void run() {
		if (isTaskRunning) return;
		isTaskRunning = true;

		params.getRootController().handleSaveAll();
		
		//
		isTaskRunning = false;
	}
	
	/**
	 * Виконується перед стартом (не виконанням) Завдання
	 */
	void beforeStart () {
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
    public TaskControl_Prg_SaveChanges copy (long taskId) {
        return (TaskControl_Prg_SaveChanges) super.copy(taskId);
    }
	
	/**
	 * Створюємо пустий екземпляр 
	 */
	@Override
    protected TaskControl createEmptyInstance() {
        return new TaskControl_Prg_SaveChanges();
    }
	
	/**
	 * Копіюємо специфічну частину
	 * @param src
	 * @param dest
	 */
	@Override
    protected void copySpecificFrom(TaskControl src, TaskControl dest) {
    }
}
