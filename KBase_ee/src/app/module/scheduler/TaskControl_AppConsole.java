package app.module.scheduler;

import java.io.File;
import java.io.IOException;

import app.model.Params;

/**
 * Клас управління Завданням "Консольний додаток"
 */
public class TaskControl_AppConsole extends TaskControl {
	/**
	 * 
	 */
	private long appConsoleId;
	/**
	 * повний шлях з ім'ям додатку
	 */
	private String appPath;
	/**
	 * чи встановлювати домашню директорію для додатку
	 */
	private boolean isHomeDir;
	
	/**
     * Конструктор.
     */
	TaskControl_AppConsole () {

	}

	/**
     * Конструктор.
     */
	TaskControl_AppConsole (long taskId, Params params, int initFlag) {
		super(taskId, params, initFlag);
		
		if (initFlag == TaskControl.INIT_FROM_DB) {
			db.taskControlAppConsoleGetByControlId(this);
		}
		
		fxmlFileName = "module/scheduler/view/TaskDetail_AppConsole.fxml";
	}
	
	/**
	 * додаємо інформацію в БД
	 */
	public void add () {
		super.add();
		db.taskControlAppConsoleAdd(this);
	}
	
	/**
	 * оновлюємо інформацію в БД
	 */
	public void update () {
		super.update();
		db.taskControlAppConsoleUpdate(this);
	}
	
	/**
	 * вилучаємо інформацію з БД
	 */
	public void delete () {
		super.delete();
		db.taskControlAppConsoleDelete(appConsoleId);
	}
	
	/**
	 * запускаємо Завдання
	 */
	void start () {
		try {
	        // Перевірка, чи не перерваний потік
	        if (Thread.currentThread().isInterrupted()) {
	            System.out.println("Завдання перервано перед стартом процесу.");
	            return;
	        }

	        // Отримайте директорію, в якій знаходиться .bat файл
            File batFile = new File(appPath);
            File batDirectory = batFile.getParentFile();
            
            // Створіть ProcessBuilder і вкажіть робочу директорію
            //ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", appPath);
            ProcessBuilder processBuilder = new ProcessBuilder(appPath);
            if (isHomeDir) {
            	processBuilder.directory(batDirectory); // Встановіть робочу директорію
            }
            processBuilder.inheritIO(); // Перенаправляємо стандартний ввід-вивід
            
            // Запускаємо процес
            Process process = processBuilder.start();   
	        
	        // Чекаємо завершення процесу з обробкою переривання
	        int exitCode;
	        try {
	            exitCode = process.waitFor();
	        } catch (InterruptedException e) {
	            process.destroy(); // зупинити процес, якщо потік перерваний
	            System.out.println("Процес перерваний, зупиняємо виконання.");
	            Thread.currentThread().interrupt(); // Повторне встановлення стану переривання
	            return;
	        }

	        System.out.println("Запуск '"+ appPath +"' завершено з кодом виходу: " + exitCode);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
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
    public TaskControl_AppConsole copy (long taskId) {
        return (TaskControl_AppConsole) super.copy(taskId);
    }
	
	/**
	 * Створюємо пустий екземпляр 
	 */
	@Override
    protected TaskControl createEmptyInstance() {
        return new TaskControl_AppConsole();
    }
	
	/**
	 * Копіюємо специфічну частину
	 * @param src
	 * @param dest
	 */
	@Override
    protected void copySpecificFrom(TaskControl src, TaskControl dest) {
		TaskControl_AppConsole s = (TaskControl_AppConsole) src;
		TaskControl_AppConsole d = (TaskControl_AppConsole) dest;
		
		d.setAppConsoleId(db.taskControlAppConsoleNextId());
		d.setAppPath(new String(s.getAppPath()));
		d.setIsHomeDir(s.getIsHomeDir());
    }
	
	public long getAppConsoleId() {
		return appConsoleId;
	}
	public void setAppConsoleId(long appConsoleId) {
		this.appConsoleId = appConsoleId;
	}
	public String getAppPath() {
		return appPath;
	}
	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}
	public boolean getIsHomeDir() {
		return isHomeDir;
	}
	public void setIsHomeDir(boolean isHomeDir) {
		this.isHomeDir = isHomeDir;
	}
}
