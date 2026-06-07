package app.module.scheduler;

import java.util.Date;

import app.model.Params;
import app.module.scheduler.db.DBScheduler;

/**
 * Клас-елемент для завдань шедулера
 */
public class TaskItem {
	// for "typeId"
	public static final int TYPE_ITEM_DIR = 0;
	public static final int TYPE_ITEM_TASK_APP_CONSOLE = 1;
	public static final int TYPE_ITEM_TASK_PRG_SAVE_STATE = 2;
	public static final int TYPE_ITEM_TASK_PRG_SAVE_ALL_CHANGES = 3;
	public static final int TYPE_ITEM_TASK_MSGBASE_SHOW_MESSAGES = 4;
	public static final int TYPE_ITEM_TASK_JAR_PLUGIN = 5;
	
	/**
	 * id
	 */
	private long id;
	/**
	 * Батьківська директорія
	 */
	private long parentId;
	/**
	 * Тип елементу. 0 - директорія, >0 - тип завдання з довідника типів
	 */
	private long typeId;
	
	private String name;
	private String descr;
	
	private boolean isShowMsgStart;
	private boolean isShowMsgFinish;
	
	private Date dateCreated;
	private Date dateModified;
	
	private DBScheduler db;
	private Params params;
	private TaskControl control;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TaskItem() {
		this(0, 0, 0, null, null, 
			 false, false,
			 null, null,
			 null, null);
	}
	
	/**
	 * Конструктор з ініціалізацією контроля з БД
	 * @param
	 */
	public TaskItem(
			long id, long parentId, long typeId, String name, String descr,
			boolean isShowMsgStart, boolean isShowMsgFinish,
			Date dateCreated, Date dateModified,
			DBScheduler db, Params params) {
		this.id           = id;
		this.parentId     = parentId;
		this.typeId       = typeId;
		this.name         = name;
		this.descr        = descr;
		this.isShowMsgStart  = isShowMsgStart;
		this.isShowMsgFinish = isShowMsgFinish;
		this.dateCreated  = dateCreated;
		this.dateModified = dateModified;
		this.db           = db;
		this.params       = params;
		this.control      = (new TaskControlFactory()).newTaskControl(id, typeId, params, TaskControl.INIT_FROM_DB);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TaskItem(TaskItem item) {
		this.id           = item.getId();
		this.parentId     = item.getParentId();
		this.typeId       = item.getTypeId();
		this.name         = item.getName();
		this.descr        = item.getDescr();
		this.isShowMsgStart  = item.isShowMsgStart();
		this.isShowMsgFinish = item.isShowMsgFinish();
		this.dateCreated  = item.getDateCreated();
		this.dateModified = item.getDateModified();
		this.db           = item.getDb();
		this.params       = item.getParams();
		this.control      = item.getControl();
	}
	
	/**
	 * Робить копію Завдання, створює новий об'єкт та запису в БД
	 * @return
	 */
	public TaskItem copy (long parentId) {
		var newItem = new TaskItem(this);
		
		if (typeId == TYPE_ITEM_DIR) {
			newItem.setId(db.dirNextId());
		} else {
			newItem.setId(db.taskNextId());
		}
		newItem.setParentId(parentId);
		
		if (typeId > TYPE_ITEM_DIR) {
			TaskControl newControl = control.copy(newItem.getId()); 
			newItem.setControl(newControl);
		}
		
		if (typeId == TYPE_ITEM_DIR) {
			db.dirAdd(newItem);
		} else {
			db.taskAdd(newItem);
		}
		
		return newItem;
	}
	
	/**
	 * вилучаємо інформацію з БД
	 */
	public void delete () {
		if (typeId == TYPE_ITEM_DIR) {   // Directory
			db.dirDelete(id);
		}
		
		if (typeId > TYPE_ITEM_DIR) {    // Task
			db.taskDelete(id);
			control.delete();
		}
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getParentId() {
		return parentId;
	}
	public void setParentId(long dirId) {
		this.parentId = dirId;
	}
	public long getTypeId() {
		return typeId;
	}
	public void setTypeId(long typeId) {
		this.typeId = typeId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescr() {
		return descr;
	}
	public void setDescr(String descr) {
		this.descr = descr;
	}
	public boolean isShowMsgStart() {
		return isShowMsgStart;
	}
	public void setShowMsgStart(boolean isShowMsgStart) {
		this.isShowMsgStart = isShowMsgStart;
	}
	public boolean isShowMsgFinish() {
		return isShowMsgFinish;
	}
	public void setShowMsgFinish(boolean isShowMsgFinish) {
		this.isShowMsgFinish = isShowMsgFinish;
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
	public Params getParams() {
		return params;
	}
	public void setParams(Params params) {
		this.params = params;
	}
	public TaskControl getControl() {
		return control;
	}
	public void setControl(TaskControl control) {
		this.control = control;
	}
}
