package app.module.scheduler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.exceptions.ServiceException;
import app.lib.ShowAppMsg;
import app.lib.crypto.AesUtil;
import app.model.Params;
import app.module.scheduler.db.DbList;
import app.module.scheduler.tasks.plugin.core.PluginContextImpl;
import app.module.scheduler.tasks.plugin.core.PluginLoader;
import javafx.application.Platform;
import mc.plugins.api.Plugin;

/**
 * Клас управління Завданням "JAR-plugin"
 */
public class TaskControl_Jar_Plugin extends TaskControl {
	// назва плагіну
	private String name;
	
	// список конектів до баз даних
	private DbList dbList;
	
	// метаданні «завантаженого» плагіна
	PluginLoader.Loaded pluginLoaded;
	
	// чи виконується зараз завдання
    private boolean isTaskRunning;

    /**
     * Конструктор.
     */
    TaskControl_Jar_Plugin () {
		isTaskRunning = false;
	}
    
    /**
     * Конструктор.
     */
    TaskControl_Jar_Plugin (long taskId, Params params, int initFlag) {
		super(taskId, params, initFlag);
		
		if (initFlag == TaskControl.INIT_FROM_DB) {
			name = db.taskControlSpecificGetStr(id, "name");
		}
		
		fxmlFileName = "module/scheduler/view/TaskDetail_Jar_Plugin.fxml";
		isTaskRunning = false;
	}
    
    /**
	 * додаємо інформацію в БД
	 */
	public void add () {
		super.add();
		db.taskControlSpecificAdd(id, "name", 0, name);
	}
	
	/**
	 * оновлюємо інформацію в БД
	 */
	public void update () {
		super.update();
		db.taskControlSpecificUpdate(id, "name", 0, name);
	}
	
	/**
	 * вилучаємо інформацію з БД
	 */
	public void delete () {
		super.delete();
		db.taskControlSpecificDelete(id, "name");
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
		
		List<AutoCloseable> toCloseOnShutdown = new ArrayList<>();
		
		Map<String, mc.plugins.api.DbEntry> dbListForPlugin = new HashMap<>();
		dbList.getList().forEach((key, entry) -> {
			mc.plugins.api.DbEntry copy = null;
			try {
				copy = new mc.plugins.api.DbEntry(
				    entry.name(),
				    entry.dbType(),
				    entry.url(),
				    AesUtil.decrypt(entry.user(), dbList.getKeySys()),
				    AesUtil.decrypt(entry.password(), dbList.getKeySys()),
				    entry.connectByPlugin(),
				    entry.conn()
				);
			} catch (Exception e) {
				e.printStackTrace();
			}
			dbListForPlugin.put(key, copy);
		});
		
		var ctx = new PluginContextImpl(pluginLoaded.id, pluginLoaded.dir, dbListForPlugin, pluginLoaded.props);
        toCloseOnShutdown.add(ctx);
		
        Plugin plugin = pluginLoaded.plugin;
        //ctx.logger().info("Plugin started");
        try {
			plugin.run(ctx);
		} catch (Exception e) {
			ctx.logger().info("error run"+e.getMessage());
			e.printStackTrace();
		}
        //ctx.logger().info("Plugin finished");
		
        for (AutoCloseable c : toCloseOnShutdown) try { c.close(); } catch (Exception ignored) {}
		
		//
		isTaskRunning = false;
	}
	
	/**
	 * Виконується перед стартом (не виконанням) Завдання
	 */
	void beforeStart () throws ServiceException {
		//------- читаємо властивості з файлу
		Path filename = Path.of(
		    params.getConfig().getItemValue("Scheduler", "scheduler.plugin.path") + name,
		    "plugin.properties"
		);

		try {
			params.getScheduler().getDbList().add(filename);
		} catch (IOException e) {
			ShowAppMsg.showAlert("ERROR", "Помилка завантаження властивостей плагіну", "Плагін "+name, 
		             e.getMessage());
			e.printStackTrace();
		}
		
		//-------- локальний список конектів, конектимось до БД, якщо треба
		if (dbList != null) {
			dbList.finish();
		}
		try {
			dbList = new DbList(params.getScheduler().getDbList(), filename);
		} catch (IOException e) {
			ShowAppMsg.showAlert("ERROR", "Помилка", "Помилка створення списку конектів", "Завдання-плагін в Шедулері");
			e.printStackTrace();
		}
		
		//-------- завантажуємо плагін
		Path dirPath = Path.of(params.getConfig().getItemValue("Scheduler", "scheduler.plugin.path") + name);
		try {
			pluginLoaded = new PluginLoader().loadFromDirectory(dirPath);
		} catch (Exception e) {
			ShowAppMsg.showAlert("ERROR", "Помилка завантаження плагіна Шедулера для запуску", "Плагін "+name, 
		             e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Виконуєть одразу після встановлення статусу Завдання "Disable"
	 */
	public void afterDisable () {
		dbList.finish();
		try {
			pluginLoaded.cl.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Створюємо копію контролу
	 * @return
	 */
	@Override
    public TaskControl_Jar_Plugin copy (long taskId) {
        return (TaskControl_Jar_Plugin) super.copy(taskId);
    }
	
	/**
	 * Створюємо пустий екземпляр 
	 */
	@Override
    protected TaskControl createEmptyInstance() {
        return new TaskControl_Jar_Plugin();
    }
	
	/**
	 * Копіюємо специфічну частину
	 * @param src
	 * @param dest
	 */
	@Override
    protected void copySpecificFrom(TaskControl src, TaskControl dest) {
		TaskControl_Jar_Plugin s = (TaskControl_Jar_Plugin) src;
		TaskControl_Jar_Plugin d = (TaskControl_Jar_Plugin) dest;
		
		d.setName(s.getName());
    }
    
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isTaskRunning() {
		return isTaskRunning;
	}

	public void setTaskRunning(boolean isTaskRunning) {
		this.isTaskRunning = isTaskRunning;
	}
}
