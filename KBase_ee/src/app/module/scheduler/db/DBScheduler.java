package app.module.scheduler.db;

import app.exceptions.KBase_DbConnEx;
import app.lib.DateConv;
import app.lib.ShowAppMsg;
import app.model.ConfigMainList;
import app.model.Params;
import app.module.scheduler.TaskControl;
import app.module.scheduler.TaskControl_AppConsole;
import app.module.scheduler.TaskItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Доступ до локальної БД Шедулера
 */
public class DBScheduler {
	private String dbURL = "jdbc:sqlite:db/scheduler.db";
	
	private DateConv dateConv;
	
	// Дескриптор соединения с сервером БД
	private Connection conn;
	
	// Конструктор
	public DBScheduler () throws KBase_DbConnEx {
		dateConv = new DateConv("yyyy-MM-dd","yyyy-MM-dd HH:mm:ss");
		connect();
	}
	
	// Конструктор
	public DBScheduler (String dbPath) throws KBase_DbConnEx {
		dbURL = "jdbc:sqlite:" + dbPath;
		dateConv = new DateConv("yyyy-MM-dd","yyyy-MM-dd HH:mm:ss");
		connect();
	}
	
	// Метод для підключення до бази даних
    public void connect() throws KBase_DbConnEx {
    	try {
    	    Class.forName("org.sqlite.JDBC");
    	} catch (ClassNotFoundException e) {
    	    e.printStackTrace();
    	    throw new KBase_DbConnEx ("SQLite JDBC Driver is not found. Include it in your library path ", this);
    	}
    	
        try {
            this.conn = DriverManager.getConnection(dbURL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new KBase_DbConnEx ("Ошибка подключения.", this);
        }
    	
    	if (conn == null) {
    		System.out.println("Failed to make connection to database");
    		throw new KBase_DbConnEx ("Ошибка подключения. (conn == null)", this);
    	}
        
    	//-------- check version
    	ConfigMainList configSys = new ConfigMainList("ConfigSysMain.xml");
    	String versionProgram = configSys.getItemValue("DB", "Required version.Scheduler");
    	String versionDB = settingsGetValue("VERSION_DB_NUMBER");
    	
    	versionProgram = versionProgram.substring(0, versionProgram.lastIndexOf("."));
    	versionDB = versionDB.substring(0, versionDB.lastIndexOf("."));
    	if (versionProgram.compareTo(versionDB) != 0) {
    		throw new KBase_DbConnEx (
    				"Версия Базы Данных "+ settingsGetValue("VERSION_DB_NUMBER") +" не поддерживается этой версией программы.", 
    				this);
    	}
    }
	
    // Метод для відключення від бази даних
    public void disconnect() throws KBase_DbConnEx {
        try {
            if (this.conn != null && !this.conn.isClosed()) {
                this.conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new KBase_DbConnEx ("Помилка закриття з'єднання з локальною БД Шедулера ("+ dbURL +")", this);
        }
    }
    
	/**
	 * Видає наступний Id для директорій та завдань
	 */
	public long getNextId () {
		long retVal = -1;
		
		try {
			String stm;
			PreparedStatement pst;
			ResultSet rs;
			long dirId;
			long taskId;
			
			// directories
			stm = "select max(id)+1 from directories";
			pst = conn.prepareStatement(stm);
			rs = pst.executeQuery();
			
			rs.next();
			dirId = rs.getLong(1);
			
			rs.close();
			pst.close();
			
			// tasks
			stm = "select max(id)+1 from tasks";
			pst = conn.prepareStatement(stm);
			rs = pst.executeQuery();
			
			rs.next();
			taskId = rs.getLong(1);
			
			rs.close();
			pst.close();

			// result
			if (dirId > taskId) retVal = dirId;
			else                retVal = taskId;
		} catch (SQLException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", "getNextId ()");
		}
		
		return retVal;
	}

    /**
	 * Додавання нової директорії
	 */
	public void dirAdd (TaskItem i) {
		String stm;
		PreparedStatement pst = null;
		
		try {
            stm = """
            		INSERT INTO directories (id, parent_id, name, descr, date_created, date_modified)  
            		VALUES (?, ?, ?, ?, ?, ?)
            		""";
            pst = conn.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setLong  (2, i.getParentId());
            pst.setString(3, i.getName());
            pst.setString(4, i.getDescr());
            pst.setString(5, dateConv.dateTimeToStr(new java.util.Date()));
            pst.setString(6, dateConv.dateTimeToStr(new java.util.Date()));
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "Помилка при додаванні нової директорії, dirAdd(id = "+ i.getId() +").");
		}
	}
	
	/**
	 * Вилучення директорії.
	 * @param
	 */
	public void dirDelete (long id) {
		PreparedStatement pst = null;
		
		try {
			String stm = "DELETE FROM directories WHERE id = ?";
            pst = conn.prepareStatement(stm);
            pst.setLong  (1, id);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер, dirDelete()", 
		             ex.getMessage());
        }
	}
    
    /**
	 * Вибираємо директорію по її id
	 */
	public TaskItem dirGetById (long id) {
		TaskItem retVal = null;
		
		try {
			String stm = """
					select id, parent_id, name, descr,
		                   date_created, date_modified  
					  from directories 
					 where id = ? 
					""";
			PreparedStatement pst = conn.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();

			java.util.Date dateTmpCre;
			String sDateCreated = rs.getString("date_created");
			if (sDateCreated != null) dateTmpCre = dateConv.strToDateTime(sDateCreated);
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			String sDateModified = rs.getString("date_modified");
			if (sDateModified != null) dateTmpMod = dateConv.strToDateTime(sDateModified);
			else                       dateTmpMod = null;
			
			retVal = new TaskItem (
					rs.getLong("id"), 
					rs.getLong("parent_id"),
					TaskItem.TYPE_ITEM_DIR,
         			rs.getString("name"),
         			rs.getString("descr"),
         			false,
         			false,
					dateTmpCre, 
         			dateTmpMod,
         			this,
         			null
					);
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
    				"dirGetById ("+id+")");
    	}
		
		return retVal;
	}
    
    /**
	 * Повертає список директорий по id батьківської директории. 
	 * @param parentId
	 * @return
	 */
	public List<TaskItem> dirListByParent (TaskItem parentItem) {
		List<TaskItem> retVal = new ArrayList<TaskItem>();
		PreparedStatement pst = null;
		//DateConv dateConv = new DateConv("yyyy-MM-dd","yyyy-MM-dd HH:mm:ss");
		
		try {
			String stm = "select id, parent_id, name, descr, " +
		                 "       date_created, date_modified " + 
					     "  from directories " +
					     " where parent_id = ? "; 
			pst = conn.prepareStatement(stm);
			pst.setLong (1, parentItem.getId());
				
			ResultSet rs = pst.executeQuery();
		
			while (rs.next()) {
				java.util.Date dateTmpCre;
				String sDateCreated = rs.getString("date_created");
				if (sDateCreated != null) dateTmpCre = dateConv.strToDateTime(sDateCreated);
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				String sDateModified = rs.getString("date_modified");
				if (sDateModified != null) dateTmpMod = dateConv.strToDateTime(sDateModified);
				else                       dateTmpMod = null;
				
				TaskItem ti = new TaskItem (
						rs.getLong("id"), 
						rs.getLong("parent_id"),
						TaskItem.TYPE_ITEM_DIR,
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			false,
	         			false,
						dateTmpCre, 
	         			dateTmpMod,
	         			this,
	         			null
						);
				retVal.add(ti);
			}
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "dirListByParent("+parentItem.getId()+")");
    	}
		
		return retVal;
	}
	
	/**
	 * апдейтимо директорію
	 */
	public void dirMove (long itemId, long parentId) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = """
					update directories
					   set parent_id = ?, date_modified = ?
					 where id = ?
					""";
			pst = conn.prepareStatement(stm);
			pst.setLong  (1, parentId);
			pst.setString(2, dateConv.dateTimeToStr(new java.util.Date()));
			pst.setLong  (3, itemId);
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "dirMove(id = "+ itemId +")");
		}
	}
	
	/**
	 * Видає наступний Id для додавання нової директорії
	 */
	public long dirNextId () {
		return getNextId();
	}	
	
	/**
	 * апдейтимо директорію
	 */
	public void dirUpdate (TaskItem di) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = """
					update directories
					   set name = ?, descr = ?, date_modified = ?
					 where id = ?
					""";
			pst = conn.prepareStatement(stm);
			pst.setString(1, di.getName());
			pst.setString(2, di.getDescr());
			pst.setString(3, dateConv.dateTimeToStr(new java.util.Date()));
			pst.setLong  (4, di.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "dirUpdate(id = "+ di.getId() +")");
		}
	}
    
    /**
	 * Установки. Получаем значение по алиасу
	 */
	public String settingsGetValue (String alias) {
		String retVal = null;
		
		try {
			String stm = "SELECT value " +
				         "  FROM settings " +
				         " WHERE alias = ?";
			PreparedStatement pst = conn.prepareStatement(stm);
			pst.setString (1, alias);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getString("value");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("settingsGetValue : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Завдання. Додаємо нове
	 */
	public void taskAdd (TaskItem ti) {
		String stm;
		PreparedStatement pst = null;
		
		try {
			stm = """
            		INSERT INTO tasks (id, dir_id, name, descr, msg_start, msg_finish, date_created, date_modified)  
            		VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            		""";
            pst = conn.prepareStatement(stm);
            pst.setLong  (1, ti.getId());
            pst.setLong  (2, ti.getParentId());
            pst.setString(3, ti.getName());
            pst.setString(4, ti.getDescr());
            pst.setInt   (5, ti.isShowMsgStart() ? 1 : 0);
            pst.setInt   (6, ti.isShowMsgFinish() ? 1 : 0);
            pst.setString(7, dateConv.dateTimeToStr(ti.getDateCreated()));
            pst.setString(8, dateConv.dateTimeToStr(ti.getDateModified()));
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "Помилка при додаванні нового завдання, taskAdd(id = "+ ti.getId() +").");
		}
	}
	
	/**
	 * Вилучення Завдання.
	 * @param
	 */
	public void taskDelete (long id) {
		PreparedStatement pst = null;
		
		try {
			String stm = "DELETE FROM tasks WHERE id = ?";
            pst = conn.prepareStatement(stm);
            pst.setLong  (1, id);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер, taskDelete()", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Повертає список усіх Завдань 
	 * @return
	 */
	public Map<Long, TaskItem> taskListAll (Params params) {
		Map<Long, TaskItem> retVal = new HashMap<>();
		PreparedStatement pst = null;
		
		try {
			String stm = """
					select t.id, t.dir_id, c.type_id, t.name, t.descr, t.msg_start, t.msg_finish, 
					       t.date_created, t.date_modified  
					  from tasks t 
					  join tasks_control c   on c.task_id = t.id
					"""; 
			pst = conn.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				String sDateCreated = rs.getString("date_created");
				if (sDateCreated != null) dateTmpCre = dateConv.strToDateTime(sDateCreated);
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				String sDateModified = rs.getString("date_modified");
				if (sDateModified != null) dateTmpMod = dateConv.strToDateTime(sDateModified);
				else                       dateTmpMod = null;
				
				TaskItem ti = new TaskItem (
						rs.getLong("id"), 
						rs.getLong("dir_id"),
						rs.getLong("type_id"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			rs.getInt("msg_start") > 0 ? true : false,
	         			rs.getInt("msg_finish") > 0 ? true : false,
						dateTmpCre, 
	         			dateTmpMod,
	         			this,
	         			params
						);
				retVal.put(ti.getId(), ti);
			}
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "taskListAll()");
    	}
		
		return retVal;
	}
	
	/**
	 * Повертає список Завдань по id директорії. 
	 * @param parentId
	 * @return
	 */
	public List<TaskItem> taskListByDir (TaskItem dirItem, Params params) {
		List<TaskItem> retVal = new ArrayList<TaskItem>();
		PreparedStatement pst = null;
		
		try {
			String stm = "select t.id, c.type_id, t.name, t.descr, t.msg_start, t.msg_finish, t.date_created, t.date_modified "+ 
					     "  from tasks t "+
					     "  join tasks_control c   on c.task_id = t.id "+ 
					     " where t.dir_id = ? ";  
			pst = conn.prepareStatement(stm);
			pst.setLong (1, dirItem.getId());
				
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				String sDateCreated = rs.getString("date_created");
				if (sDateCreated != null) dateTmpCre = dateConv.strToDateTime(sDateCreated);
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				String sDateModified = rs.getString("date_modified");
				if (sDateModified != null) dateTmpMod = dateConv.strToDateTime(sDateModified);
				else                       dateTmpMod = null;
				
				TaskItem ti = new TaskItem (
						rs.getLong("id"), 
						dirItem.getId(),
						rs.getLong("type_id"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			rs.getInt("msg_start") > 0 ? true : false,
	    	         	rs.getInt("msg_finish") > 0 ? true : false,
						dateTmpCre, 
	         			dateTmpMod,
	         			this,
	         			params
						);
				retVal.add(ti);
			}
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "taskListByDir("+dirItem.getId()+")");
    	}
		
		return retVal;
	}
	
	/**
	 * апдейтимо ітем Завдання
	 */
	public void taskMove (long itemId, long parentId) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = """
					update tasks
					   set dir_id = ?, date_modified = ?
					 where id = ?
					""";
			pst = conn.prepareStatement(stm);
			int i = 1;
			pst.setLong  (i++, parentId);
			pst.setString(i++, dateConv.dateTimeToStr(new java.util.Date()));
			pst.setLong  (i++, itemId);
            
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "taskMove(id = "+ itemId +")");
		}
	}
	
	/**
	 * Видає наступний Id для додавання нового завдання
	 */
	public long taskNextId () {
		return getNextId();
	}
	
	/**
	 * апдейтимо ітем Завдання
	 */
	public void taskUpdate (TaskItem i) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = """
					update tasks
					   set name = ?, descr = ?, msg_start = ?, msg_finish = ?, date_modified = ?
					 where id = ?
					""";
			pst = conn.prepareStatement(stm);
            pst.setString(1, i.getName());
            pst.setString(2, i.getDescr());
            pst.setInt   (3, i.isShowMsgStart() ? 1 : 0);
            pst.setInt   (4, i.isShowMsgFinish() ? 1 : 0);
            pst.setString(5, dateConv.dateTimeToStr(i.getDateModified()));
            pst.setLong  (6, i.getId());
            
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "taskUpdate(id = "+ i.getId() +")");
		}
	}
	
	/**
	 * обьект управління завданням
	 * додаємо новий, тільки загальну частину
	 */
	public void taskControlAdd (TaskControl tc) {
		String stm;
		PreparedStatement pst = null;
		
		try {
			stm = """
            		INSERT INTO tasks_control (id, task_id, type_id, state, 
            		                           start_hour, start_minute, interval, 
			                                   date_created, date_modified)  
            		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            		""";
            pst = conn.prepareStatement(stm);
            pst.setLong  (1, tc.getId());
            pst.setLong  (2, tc.getTaskId());
            pst.setLong  (3, tc.getTypeId());
            pst.setInt   (4, tc.getState());
            pst.setInt   (5, tc.getStartHour());
            pst.setInt   (6, tc.getStartMinute());
            pst.setInt   (7, tc.getInterval());
            pst.setString(8, dateConv.dateTimeToStr(tc.getDateCreated()));
            pst.setString(9, dateConv.dateTimeToStr(tc.getDateModified()));
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "Помилка при додаванні нового контролю завданням, taskControlAdd(id = "+ tc.getId() +").");
		}
	}
	
	/**
	 * Вилучення загальної частини контролю Завдання.
	 * @param
	 */
	public void taskControlDelete (long id) {
		PreparedStatement pst = null;
		
		try {
			String stm = "DELETE FROM tasks_control WHERE id = ?";
            pst = conn.prepareStatement(stm);
            pst.setLong  (1, id);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер, taskControlDelete()", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Батьківський обьект управління завданням.
	 * Ініціалізація по taskId.
	 */
	public void taskControlGetByTaskId (TaskControl tc) {
		try {
			String stm = "select id, type_id, state, start_hour, start_minute, interval, date_created, date_modified "+
					     "  from tasks_control "+
					     " where task_id = ? "; 
			PreparedStatement pst = conn.prepareStatement(stm);
			pst.setLong (1, tc.getTaskId());
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			String sDateCreated = rs.getString("date_created");
			if (sDateCreated != null) dateTmpCre = dateConv.strToDateTime(sDateCreated);
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			String sDateModified = rs.getString("date_modified");
			if (sDateModified != null) dateTmpMod = dateConv.strToDateTime(sDateModified);
			else                       dateTmpMod = null;
			
			tc.setId(rs.getLong("id"));
			tc.setTypeId(rs.getLong("type_id"));
			tc.setState(rs.getInt("state"));
			tc.setStartHour(rs.getInt("start_hour"));
			tc.setStartMinute(rs.getInt("start_minute"));
			tc.setInterval(rs.getInt("interval"));
			tc.setDateCreated(dateTmpCre);
			tc.setDateModified(dateTmpMod);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "Scheduler db error", "Помилка при роботі з локальною базою даних Планувальника", 
		             "taskControlGetById(taskId = "+ tc.getId() +")");
    	}
	}
	
	/**
	 * Видає наступний Id для додавання нового ітема-контролю для завдання
	 */
	public long taskControlNextId () {
		long retVal = -1;
		
		try {
			String stm = "select max(id)+1 from tasks_control";
			PreparedStatement pst = conn.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", "taskControlNextId ()");
    	}
		
		return retVal;
	}
	
	/**
	 * Змінюємо статус завдання
	 */
	public void taskControlSetState (TaskControl i) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = """
					update tasks_control
					   set state = ?
					 where id = ?
					""";
			pst = conn.prepareStatement(stm);
            pst.setInt   (1, i.getState());
            pst.setLong  (2, i.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "taskControlSetState(id = "+ i.getId() +")");
		}
	}
	
	/**
	 * апдейтимо загальну частину контролю ітема
	 */
	public void taskControlUpdate (TaskControl i) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = """
					update tasks_control
					   set start_hour = ?, start_minute = ?, interval = ?, date_modified = ?
					 where id = ?
					""";
			pst = conn.prepareStatement(stm);
            pst.setInt   (1, i.getStartHour());
            pst.setInt   (2, i.getStartMinute());
            pst.setInt   (3, i.getInterval());
            pst.setString(4, dateConv.dateTimeToStr(i.getDateModified()));
            pst.setLong  (5, i.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "taskControlUpdate(id = "+ i.getId() +")");
		}
	}
	
	/**
	 * обьект управління завданням, консольний додаток.
	 * додаємо новий
	 */
	public void taskControlAppConsoleAdd (TaskControl_AppConsole tc) {
		String stm;
		PreparedStatement pst = null;
		
		try {
			stm = """
            		INSERT INTO tasks_control_app_console (id, control_id, app_path, is_home_dir, date_created, date_modified)  
            		VALUES (?, ?, ?, ?, ?, ?)
            		""";
            pst = conn.prepareStatement(stm);
            pst.setLong  (1, tc.getAppConsoleId());
            pst.setLong  (2, tc.getId());
            pst.setString(3, tc.getAppPath());
            pst.setInt   (4, (tc.getIsHomeDir()) ? 1 : 0);
            pst.setString(5, dateConv.dateTimeToStr(new java.util.Date()));
            pst.setString(6, dateConv.dateTimeToStr(new java.util.Date()));
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "Помилка при додаванні нової специфіки Консольного додатка, taskControlAppConsoleAdd(id = "+ tc.getAppConsoleId() +").");
		}
	}
	
	/**
	 * Вилучення загальної частини контролю Завдання.
	 * @param
	 */
	public void taskControlAppConsoleDelete (long id) {
		PreparedStatement pst = null;
		
		try {
			String stm = "DELETE FROM tasks_control_app_console WHERE id = ?";
            pst = conn.prepareStatement(stm);
            pst.setLong  (1, id);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер, taskControlAppConsoleDelete()", 
		             ex.getMessage());
        }
	}
	
	/**
	 * обьект управління завданням, консольний додаток.
	 * Ініціалізація по controlId.
	 */
	public void taskControlAppConsoleGetByControlId (TaskControl_AppConsole tc) {
		try {
			String stm = "select id, app_path, is_home_dir "+
					     "  from tasks_control_app_console "+ 
					     " where control_id = ? "; 
			PreparedStatement pst = conn.prepareStatement(stm);
			pst.setLong (1, tc.getId());
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			tc.setAppConsoleId(rs.getLong("id"));
			tc.setAppPath(rs.getString("app_path"));
			tc.setIsHomeDir((rs.getInt("is_home_dir") == 0) ? false : true);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "Scheduler db error", "Помилка при роботі з локальною базою даних Планувальника", 
		             "taskControlAppConsoleGetByControlId(controlId = "+ tc.getId() +")");
    	}
	}
	
	/**
	 * Видає наступний Id для додавання нового ітема-контролю-специфіки для завдання
	 */
	public long taskControlAppConsoleNextId () {
		long retVal = -1;
		
		try {
			String stm = "select max(id)+1 from tasks_control_app_console";
			PreparedStatement pst = conn.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", "taskControlAppConsoleNextId ()");
    	}
		
		return retVal;
	}
	
	/**
	 * апдейтимо специфічну частину контролю ітема
	 */
	public void taskControlAppConsoleUpdate (TaskControl_AppConsole i) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = """
					update tasks_control_app_console
					   set app_path = ?, is_home_dir = ?, date_modified = ?
					 where id = ?
					""";
			pst = conn.prepareStatement(stm);
			pst.setString(1, i.getAppPath());
			pst.setInt   (2, (i.getIsHomeDir()) ? 1 : 0);
            pst.setString(3, dateConv.dateTimeToStr(new java.util.Date()));
			pst.setLong  (4, i.getAppConsoleId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "taskControlAppConsoleUpdate(id = "+ i.getAppConsoleId() +")");
		}
	}
	
	/**
	 * додаємо параметр специфіки
	 */
	public void taskControlSpecificAdd (long controlId, String fieldType, int valueNum, String valueStr) {
		String stm;
		PreparedStatement pst = null;
		
		try {
			stm = """
					insert into tasks_control_specific (id,control_id,field_type,value_num,value_str,date_created,date_modified)
                    values (?, ?, ?, ?, ?, ?, ?)
					""";
            pst = conn.prepareStatement(stm);
            pst.setLong  (1, taskControlSpecificNextId());
            pst.setLong  (2, controlId);
            pst.setString(3, fieldType);
            pst.setInt   (4, valueNum);
            pst.setString(5, valueStr);
            pst.setString(6, dateConv.dateTimeToStr(new java.util.Date()));
            pst.setString(7, dateConv.dateTimeToStr(new java.util.Date()));
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "Помилка при додаванні нової специфіки Програма, збереження стану, taskControlSpecificAdd(id = "+ 
					            		 controlId +").");
		}
	}
	
	/**
	 * додаємо параметр специфіки, для вказаного користувача
	 */
	public void taskControlSpecificAdd (long controlId, String fieldType, int valueNum, String valueStr, String username) {
		String stm;
		PreparedStatement pst = null;
		
		try {
			stm = """
					insert into tasks_control_specific (id,control_id,field_type,value_num,value_str,
					            username,
					            date_created,date_modified)
                    values (?, ?, ?, ?, ?, ?, ?, ?)
					""";
            pst = conn.prepareStatement(stm);
            pst.setLong  (1, taskControlSpecificNextId());
            pst.setLong  (2, controlId);
            pst.setString(3, fieldType);
            pst.setInt   (4, valueNum);
            pst.setString(5, valueStr);
            pst.setString(6, username);
            pst.setString(7, dateConv.dateTimeToStr(new java.util.Date()));
            pst.setString(8, dateConv.dateTimeToStr(new java.util.Date()));
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "Помилка при додаванні нової специфіки Програма, збереження стану, taskControlSpecificAdd(id = "+ 
					            		 controlId +").");
		}
	}
	
	/**
	 * Вилучення параметру специфіки.
	 * @param
	 */
	public void taskControlSpecificDelete (long controlId, String fieldType) {
		PreparedStatement pst = null;
		
		try {
			String stm = """
					delete from tasks_control_specific 
                     where control_id = ?
                       and field_type = ?
					""";
            pst = conn.prepareStatement(stm);
            pst.setLong  (1, controlId);
            pst.setString(2, fieldType);
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер, taskControlSpecificDelete()", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Вилучення параметру специфіки, для вказаного користувача
	 * @param
	 */
	public void taskControlSpecificDelete (long controlId, String fieldType, String username) {
		PreparedStatement pst = null;
		
		try {
			String stm = """
					delete from tasks_control_specific 
                     where control_id = ?
                       and field_type = ?
                       and username = ?
					""";
            pst = conn.prepareStatement(stm);
            pst.setLong  (1, controlId);
            pst.setString(2, fieldType);
            pst.setString(3, username);
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер, taskControlSpecificDelete()", 
		             ex.getMessage());
        }
	}
	
	/**
	 * повертає текстовий параметр специфіки
	 */
	public String taskControlSpecificGetStr (long controlId, String fieldType) {
		String retVal = "";
		
		try {
			String stm = """
					select value_str
                      from tasks_control_specific 
                     where control_id = ?
                       and field_type = ?
					""";
			PreparedStatement pst = conn.prepareStatement(stm);
			pst.setLong  (1, controlId);
			pst.setString(2, fieldType);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getString("value_str");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "Scheduler db error", "Помилка при роботі з локальною базою даних Планувальника", 
		             "taskControlSpecificGetStr(id = "+ controlId +")");
    	}
		
		return retVal;
	}
	
	/**
	 * повертає текстовий параметр специфіки, для вказаного користувача
	 */
	public String taskControlSpecificGetStr (long controlId, String fieldType, String username) {
		String retVal = "";
		
		try {
			String stm = """
					select value_str
                      from tasks_control_specific 
                     where control_id = ?
                       and field_type = ?
                       and username = ?
					""";
			PreparedStatement pst = conn.prepareStatement(stm);
			pst.setLong  (1, controlId);
			pst.setString(2, fieldType);
			pst.setString(3, username);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getString("value_str");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "Scheduler db error", "Помилка при роботі з локальною базою даних Планувальника", 
		             "taskControlSpecificGetStr(id = "+ controlId +")");
    	}
		
		return retVal;
	}
	
	/**
	 * Видає наступний Id для додавання нового параметра специфіки
	 */
	private long taskControlSpecificNextId () {
		long retVal = -1;
		
		try {
			String stm = "select max(id)+1 from tasks_control_specific";
			PreparedStatement pst = conn.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", "taskControlSpecificNextId ()");
    	}
		
		return retVal;
	}
	
	/**
	 * апдейтимо параметр специфіки
	 */
	public void taskControlSpecificUpdate (long controlId, String fieldType, int valueNum, String valueStr) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = """
					update tasks_control_specific
                       set value_num = ?,
                           value_str = ?,
                           date_modified = ?
                     where control_id = ?
                       and field_type = ?
					""";
			pst = conn.prepareStatement(stm);
			pst.setInt   (1, valueNum);
			pst.setString(2, valueStr);
			pst.setString(3, dateConv.dateTimeToStr(new java.util.Date()));
			pst.setLong  (4, controlId);
			pst.setString(5, fieldType);
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "taskControlSpecificUpdate(id = "+ controlId +")");
		}
	}
	
	/**
	 * апдейтимо параметр специфіки, для вказаного користувача
	 */
	public void taskControlSpecificUpdate (long controlId, String fieldType, int valueNum, String valueStr, String username) {
		PreparedStatement pst = null;
		String stm;
		
		try {
			stm = """
					update tasks_control_specific
                       set value_num = ?,
                           value_str = ?,
                           date_modified = ?
                     where control_id = ?
                       and field_type = ?
                       and username = ?
					""";
			pst = conn.prepareStatement(stm);
			pst.setInt   (1, valueNum);
			pst.setString(2, valueStr);
			pst.setString(3, dateConv.dateTimeToStr(new java.util.Date()));
			pst.setLong  (4, controlId);
			pst.setString(5, fieldType);
			pst.setString(6, username);
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "taskControlSpecificUpdate(id = "+ controlId +")");
		}
	}

	/**
	 * Повертає назву типу завдання по ід
	 */
	public String taskTypeGetNameById (long id) {
		String retVal = null;
		
		try {
			String stm = "select name from tasks_type where id = ? "; 
			PreparedStatement pst = conn.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
		
			rs.next();
			retVal = rs.getString("name");
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "taskTypeGetNameById("+ id +")");
    	}
		
		return retVal;
	}
	
	/**
	 * Повертає список типів завдань у вигляді "Назва (ід)"
	 */
	public ObservableList<String> taskTypeListCombo () {
		ObservableList<String> retVal = FXCollections.observableArrayList();
		PreparedStatement pst = null;
		
		try {
			String stm = "select id, name from tasks_type "; 
			pst = conn.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
		
			while (rs.next()) {
				retVal.add(rs.getString("name")+" ("+ String.valueOf(rs.getLong("id")) +")");
			}
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з БД Шедулер", 
					             "taskTypeListCombo()");
    	}
		
		return retVal;
	}
}
