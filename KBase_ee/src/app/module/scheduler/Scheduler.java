package app.module.scheduler;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import app.exceptions.KBase_DbConnEx;
import app.exceptions.ServiceException;
import app.lib.ShowAppMsg;
import app.model.Params;
import app.module.scheduler.db.DBScheduler;
import app.module.scheduler.db.DbList;
import app.module.scheduler.view.TaskList_Controller;
import javafx.application.Platform;

/**
 * Клас який керує Завданнями
 */
public class Scheduler {
	//
	private Params params;
	//
	private DBScheduler db;
	//
	private Map<Long, TaskItem> tasks;
	//
	private TaskList_Controller controller;
	// параметри конектів до БД які використовують Завдання 
	private DbList dbList;
	
	/**
	 * 
	 */
	public Scheduler (Params params) {
		params.setScheduler(this);
		this.params = params;
		
		//---- create db connection
    	try {
    		String dbPath = params.getConfig().getItemValue("db", "scheduler.path");
    		if (dbPath == null) {
    			params.getConfig().add(
        				"db", 
        				"scheduler.path",
        				"Файл зі шляхом БД планувальника",
        				"db/scheduler.db",
        				LocalDate.now(),
        				true,
        				true
        				);
    		}
    		db = new DBScheduler(dbPath);
		} catch (KBase_DbConnEx e) {
			ShowAppMsg.showAlert("WARNING", "З'єднання з локальною БД Шедулера", 
					"З'єднання не встановлене. Шедулер не ініціалізовано.", e.msg);
			//e.printStackTrace();
			return;
		}
    	
    	//---- init DbList
    	dbList = new DbList(params);
    	
    	//---- init tasks
    	tasks = db.taskListAll(params);
    	
    	tasks.forEach((id, task) -> {
    		if (task.getControl().getState() == TaskControl.STATE_ENABLE) {
				try {
					startTaskScheduler (task, false);
				} catch (ServiceException e) {
					e.writeLog(params);
					e.showAlert("Шедулер : помилка", "Шедулер : помилка при запуску Завдання '"+task.getName()+"'");
				}
			}
		});
	}
	
	/**
	 * @throws ServiceException
	 * 
	 */
	public void startTaskScheduler (TaskItem i, boolean runImmediately) throws ServiceException {
    	i.getControl().beforeStart();  // виконуємо на початку старту Завдання
    	
    	if (i.getControl().getScheduler() == null || i.getControl().getScheduler().isShutdown()) {
    		i.getControl().setScheduler(Executors.newSingleThreadScheduledExecutor());
        }
    	
    	if (i.getControl().getStartHour() == -1)  runImmediately = true;
    	
    	if (runImmediately) {
    		if (i.getControl().getInterval() == -1) {
    			// Запускаємо завдання негайно і виконуємо лише один раз
    	        i.getControl().setScheduledFuture(
    	        	    i.getControl().getScheduler().schedule(
    	        	        () -> {
    	        	            // Виконуємо завдання в окремому потоці
    	        	            ExecutorService executor = Executors.newSingleThreadExecutor();
    	        	            executor.submit(() -> runTask(i));
    	        	            executor.shutdown();
    	        	        }, 0, TimeUnit.SECONDS
    	        	    )
    	        );
    		} else {
    			// Запускаємо завдання негайно, а потім воно буде виконуватись через інтервал
    			i.getControl().setScheduledFuture(
    					i.getControl().getScheduler().scheduleAtFixedRate(
    	    				() -> runTask(i), 0, i.getControl().getInterval()*60, TimeUnit.SECONDS));
    		}
        } else {
            // Обчислюємо початкову затримку до наступного запуску (як вказано)
        	long initialDelayInMillis = computeInitialDelay(i.getControl().getStartHour(), i.getControl().getStartMinute());
        	long initialDelayInSeconds = TimeUnit.MILLISECONDS.toSeconds(initialDelayInMillis) + 1;
        	if (i.getControl().getInterval() == -1) {
        		i.getControl().setScheduledFuture(
    	        	    i.getControl().getScheduler().schedule(
    	        	        () -> {
    	        	            // Виконуємо завдання в окремому потоці
    	        	            ExecutorService executor = Executors.newSingleThreadExecutor();
    	        	            executor.submit(() -> runTask(i));
    	        	            executor.shutdown();
    	        	        }, initialDelayInSeconds, TimeUnit.SECONDS
    	        	    )
    	        );
        	} else {
        		i.getControl().setScheduledFuture(
        				i.getControl().getScheduler().scheduleAtFixedRate(
    						() -> runTask(i), initialDelayInSeconds, i.getControl().getInterval()*60, TimeUnit.SECONDS));
        	}
        }
    }
    
    /**
     * безпосереднє виконання завдання в паралельному потоку
     */
    private void runTask(TaskItem ti) {
    	// получаємо кількість секунд показу повідомлення
    	String sWaitSec = params.getConfig().getItemValue("Scheduler","scheduler.msg.waitSec");
		int iWaitSec;
		try {
			iWaitSec = Integer.parseInt(sWaitSec);
		} catch (NumberFormatException e) {
			iWaitSec = 5;
		}
		AtomicInteger aWaitSec = new AtomicInteger(iWaitSec);
    	
    	//
    	if (ti.isShowMsgStart()) {
    		Platform.runLater(() -> ShowAppMsg.NotificationPopup("'"+ ti.getName() +"' started", "lightgreen", aWaitSec.get()));
    	}
    	
    	ti.getControl().setState(TaskControl.STATE_RUNNING);
    	if (controller != null) {
    		Platform.runLater(() -> controller.changeTaskStateInProcess(ti, TaskControl.STATE_RUNNING));
    	}
    	
    	ti.getControl().start();
    	
    	if (ti.getControl().getInterval() == -1) {  // завдання виконувалось один раз
    		ti.getControl().setState(TaskControl.STATE_DISABLE);
    		db.taskControlSetState(ti.getControl());
    		if (controller != null) {
    			Platform.runLater(() -> controller.changeTaskStateInProcess(ti, TaskControl.STATE_DISABLE));
    		}
    	} else {
    		ti.getControl().setState(TaskControl.STATE_ENABLE);
    		if (controller != null) {
    			Platform.runLater(() -> controller.changeTaskStateInProcess(ti, TaskControl.STATE_ENABLE));
    		}
    	}
    	
    	if (ti.isShowMsgFinish()) {
    		Platform.runLater(() -> ShowAppMsg.NotificationPopup("'"+ ti.getName() +"' finished", "lightblue", aWaitSec.get()));
    	}
    }
    
    /**
     * 
     */
    private long computeInitialDelay(int targetHour, int targetMinute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0);

        // Якщо зараз уже пізніше, ніж час для запуску, переходимо на наступний день
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }

        // Обчислюємо тривалість у мілісекундах між теперішнім моментом і наступним запуском
        Duration duration = Duration.between(now, nextRun);
        return duration.toMillis(); // Повертаємо кількість мілісекунд
    }
	
	/**
	 * Дії перед завершенням програми
	 */
	public void finish () {
		// db disconect
    	try {
    		db.disconnect();
		} catch (KBase_DbConnEx e) {
			ShowAppMsg.showAlert("WARNING", "Закриття з'єднання з локальною БД Шедулера", "Не можу закрити з'єднання", e.msg);
			//e.printStackTrace();
		}
	}
	
	/**
	 * Перевіряємо чи виконуються зараз якісь Завдання
	 */
	public String isTaskRunning () {
		StringBuilder strBuilder = new StringBuilder();
		
		tasks.forEach((id, task) -> {
			// Перевірка на тип і наявність
	        if (task != null && task.getTypeId() > 0) {
	        	if (task.getControl().getState() == TaskControl.STATE_RUNNING) {
	        		if (strBuilder.length() > 0) {
	        			strBuilder.append("\n");
	        		}
	        		strBuilder.append(task.getName());
	        	}
	        }
		});
		
		return strBuilder.toString();
	}

	public DBScheduler getDb() {
		return db;
	}

	public void setDb(DBScheduler db) {
		this.db = db;
	}
	
	public Map<Long, TaskItem> getTasks() {
		return tasks;
	}

	public void setTasks(Map<Long, TaskItem> tasks) {
		this.tasks = tasks;
	}

	public TaskList_Controller getController() {
		return controller;
	}

	public void setController(TaskList_Controller controller) {
		this.controller = controller;
	}
	
	public DbList getDbList() {
		return dbList;
	}

	public void setDbList(DbList dbList) {
		this.dbList = dbList;
	}

	/**
	 * 
	 */
	@Override
    public String toString() {
        return "Sheduler";
    }
}
