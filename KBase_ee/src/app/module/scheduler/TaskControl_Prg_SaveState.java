package app.module.scheduler;

import app.model.Params;
import javafx.application.Platform;

/**
 * Клас управління Завданням "Програма. Збереження стану."
 */
public class TaskControl_Prg_SaveState extends TaskControl {
	/*
	 * ім'я файлу для збереження стану
	 * Якщо вказано в імені {DATETIME}, то буде додана дата та час в форматі YYMMDDHHMI
	 */
	private String filename;
	
	/**
     * Конструктор.
     */
	TaskControl_Prg_SaveState () {
	}
	
	/**
     * Конструктор.
     */
	TaskControl_Prg_SaveState (long taskId, Params params, int initFlag) {
		super(taskId, params, initFlag);
		
		if (initFlag == TaskControl.INIT_FROM_DB) {
			filename = db.taskControlSpecificGetStr(id, "FILENAME");
		}
		
		fxmlFileName = "module/scheduler/view/TaskDetail_Prg_SaveState.fxml";
	}
	
	/**
	 * додаємо інформацію в БД
	 */
	public void add () {
		super.add();
		db.taskControlSpecificAdd(id, "FILENAME", 0, filename);
	}
	
	/**
	 * оновлюємо інформацію в БД
	 */
	public void update () {
		super.update();
		db.taskControlSpecificUpdate(id, "FILENAME", 0, filename);
	}
	
	/**
	 * вилучаємо інформацію з БД
	 */
	public void delete () {
		super.delete();
		db.taskControlSpecificDelete(id, "FILENAME");
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

        Platform.runLater(() -> params.getMain().saveControlsStateMain (filename));
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
    public TaskControl_Prg_SaveState copy (long taskId) {
        return (TaskControl_Prg_SaveState) super.copy(taskId);
    }
	
	/**
	 * Створюємо пустий екземпляр 
	 */
	@Override
    protected TaskControl createEmptyInstance() {
        return new TaskControl_Prg_SaveState();
    }
	
	/**
	 * Копіюємо специфічну частину
	 * @param src
	 * @param dest
	 */
	@Override
    protected void copySpecificFrom(TaskControl src, TaskControl dest) {
		TaskControl_Prg_SaveState s = (TaskControl_Prg_SaveState) src;
		TaskControl_Prg_SaveState d = (TaskControl_Prg_SaveState) dest;
		
		d.setFilename(new String(s.getFilename()));
    }
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
