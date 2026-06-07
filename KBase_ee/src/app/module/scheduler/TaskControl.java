package app.module.scheduler;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import app.exceptions.ServiceException;
import app.model.Params;
import app.module.scheduler.db.DBScheduler;

/**
 * Батьківський (абстрактний) клас управління Завданням
 */
public abstract class TaskControl {
	// for constructor
	public static final int INIT_EMPTY     = 0;
	public static final int INIT_FROM_DB   = 1;
	
	// for variable state
	public static final int STATE_DISABLE     = 0;
	public static final int STATE_ENABLE      = 1;
	public static final int STATE_RUNNING     = 2;
	
	protected long id;
	private long taskId;
	/**
	 * Тип завдання з довідника типів
	 */
	private long typeId;
	/**
	 * Стан завдання. 0 - disable, 1 - enable, 2 - running
	 */
	private int state;
	
	/**
	 * Година коли старує завдання (0-23). Якщо -1, то стартуємо негайно
	 */
	private int startHour;
	/**
	 * Хвилини коли старує завдання (0-59)
	 */
	private int startMinute;
	/**
	 * Інтервал з яким повторюється завдання в хвилинах. Якщо -1, то виконується один раз.
	 */
	private int interval;
		
	private Date dateCreated;
	private Date dateModified;
	
	protected DBScheduler db;
	protected Params params;
	
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> scheduledFuture;
	
	//
	protected String fxmlFileName;
    
	/**
     * Конструктор.
     */
	TaskControl () {
	}
	
	/**
     * Конструктор.
     */
	TaskControl (long taskId, Params params, int initFlag) {
		this.taskId = taskId;
		this.params = params;
		this.db     = params.getScheduler().getDb();
		
		if (initFlag == TaskControl.INIT_FROM_DB) {
			db.taskControlGetByTaskId(this);
		}
	}
	
	/**
	 * додаємо інформацію в БД
	 */
	public void add () {
		if (dateCreated == null)   dateCreated = new java.util.Date();
		if (dateModified == null)   dateModified = new java.util.Date();
		
		db.taskControlAdd(this);
	}
	
	/**
	 * оновлюємо інформацію в БД
	 */
	public void update () {
		if (dateModified == null)   dateModified = new java.util.Date();
		
		db.taskControlUpdate(this);
	}
	
	/**
	 * вилучаємо інформацію з БД
	 */
	public void delete () {
		db.taskControlDelete(id);
	}
	
	/**
	 * запускаємо Завдання
	 */
	abstract void start ();
	
	/**
	 * Виконується перед стартом (не виконанням) Завдання
	 * @throws ServiceException
	 */
	abstract void beforeStart () throws ServiceException;
	
	/**
	 * Виконуєть одразу після встановлення статусу Завдання "Disable"
	 */
	abstract public void afterDisable ();
	
	/**
	 * Створюємо копію контролу
	 * @return
	 */
	public TaskControl copy (long taskId) {
		TaskControl dest = createEmptyInstance();
        copyCommonFieldsTo(dest, taskId);
        copySpecificFrom(this, dest);
        dest.add();
        return dest;
    }
	protected void copyCommonFieldsTo(TaskControl dest, long taskId) {
		dest.setId(db.taskControlNextId());
		dest.setTaskId(taskId);
		dest.setTypeId(typeId);
        dest.setState(STATE_DISABLE);
        dest.setStartHour(startHour);
		dest.setStartMinute(startMinute);
		dest.setInterval(interval);
		dest.setDateCreated(new Date(dateCreated.getTime()));
		dest.setDateModified(new Date(dateModified.getTime()));
    	dest.setDb(db);
    	dest.params = params;
    	dest.fxmlFileName = new String(fxmlFileName);
    }
    protected abstract TaskControl createEmptyInstance();
    protected abstract void copySpecificFrom(TaskControl src, TaskControl dest);
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	public long getTypeId() {
		return typeId;
	}
	public void setTypeId(long typeId) {
		this.typeId = typeId;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getStartHour() {
		return startHour;
	}
	public void setStartHour(int start_hour) {
		this.startHour = start_hour;
	}
	public int getStartMinute() {
		return startMinute;
	}
	public void setStartMinute(int start_minute) {
		this.startMinute = start_minute;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Date getDateModified() {
		return dateModified;
	}
	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}
	public DBScheduler getDb() {
		return db;
	}
	public void setDb(DBScheduler db) {
		this.db = db;
	}
	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}
	public void setScheduler(ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}
	public ScheduledFuture<?> getScheduledFuture() {
		return scheduledFuture;
	}
	public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
		this.scheduledFuture = scheduledFuture;
	}

	public String getFxmlFileName() {
		return fxmlFileName;
	}
	
}
