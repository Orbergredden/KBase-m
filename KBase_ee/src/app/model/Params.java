package app.model;

import app.Main;
import app.module.scheduler.Scheduler;
import app.view.Root_Controller;
import app.view.business.Container_Interface;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Передается как параметр при инициализации бизнес-классов
 * 
 * @author IMakarevich
 */
public class Params {
	// Список основных настроек приложения, Пользовательских
	private ConfigMainList config;
	// Список основных настроек приложения, НЕ Пользовательских
	private ConfigMainList configSys;
	// Список основных настроек приложения, НЕ Пользовательских, змінних
	private ConfigMainList configSysVars;
	// Список открытых соединений к БД знаний
	private DBConCurList_Parameters connDB;
	// Список отдельно открытых окон в приложении
	private WinList winList;

	// основной обьект
	private Main main;
	// Основной контроллер
	private Root_Controller rootController;
	// главная сцена
	private Stage mainStage;
	// Главный набор вкладок
	private TabPane tabPane_Main;
	
	// Текущее открытое соединение с БД знаний
	private DBConCur_Parameters conCur;
	// Обьект-контейнер в котором содержится текущий обьект (таб или окно)
	private Container_Interface objContainer;
	// Текущий набор вкладок
	private TabPane tabPane_Cur;
	// Родительский обьект, из которого вызывался текущий
	private Object parentObj;
	
	// Текущая сцена (как правило сцена модальных окон)
	private Stage stageCur;
	
	// Клас який керує Завданнями додатку
	private Scheduler scheduler;
	
	/**
	 * constructor. Начальная инициализация 
	 */
	public Params () {
		configSys = new ConfigMainList("ConfigSysMain.xml");
		configSysVars = new ConfigMainList("ConfigSysVars.xml");
		config = new ConfigMainList(this);
		connDB = new DBConCurList_Parameters();
		winList = new WinList();
	}
	
	/**
	 * constructor. На основе существубщего
	 */
	public Params (Params params) {
		this.config = params.getConfig();
		this.configSys = params.getConfigSys();
		this.configSysVars = params.getConfigSysVars();
		this.connDB = params.getConnDB();
		this.winList = params.getWinList();

		this.main = params.getMain();
		this.rootController = params.getRootController();
		this.mainStage = params.getMainStage();
		this.tabPane_Main = params.getTabPane_Main();
		
		this.conCur = params.getConCur();
		this.objContainer = params.getObjContainer();
		this.tabPane_Cur = params.getTabPane_Cur();
		this.parentObj = params.getParentObj();
		
		this.stageCur = params.getStageCur();
		
		this.scheduler = params.scheduler;
	}
	
	public void setMsgToStatusBar (String msg) {
		rootController.statusBar_ShowMsg(msg);
	}
	
	//====================== getters and setters
	public ConfigMainList getConfig() {
		return config;
	}

	public void setConfig(ConfigMainList config) {
		this.config = config;
	}
	
	public ConfigMainList getConfigSys() {
		return configSys;
	}

	public void setConfigSys(ConfigMainList configSys) {
		this.configSys = configSys;
	}
	
	public ConfigMainList getConfigSysVars() {
		return configSysVars;
	}

	public void setConfigSysVars(ConfigMainList configSysVars) {
		this.configSysVars = configSysVars;
	}

	public DBConCurList_Parameters getConnDB() {
		return connDB;
	}

	public void setConnDB(DBConCurList_Parameters connDB) {
		this.connDB = connDB;
	}
	
	public WinList getWinList() {
		return winList;
	}

	public void setWinList(WinList winList) {
		this.winList = winList;
	}
	
	public Main getMain() {
		return main;
	}

	public void setMain(Main main) {
		this.main = main;
	}
	
	public Root_Controller getRootController() {
		return rootController;
	}

	public void setRootController(Root_Controller rootController) {
		this.rootController = rootController;
	}
	
	public Stage getMainStage() {
		return mainStage;
	}

	public void setMainStage(Stage mainStage) {
		this.mainStage = mainStage;
	}
	
	public TabPane getTabPane_Main() {
		return tabPane_Main;
	}

	public void setTabPane_Main(TabPane tabPane_Main) {
		this.tabPane_Main = tabPane_Main;
	}
	
	public DBConCur_Parameters getConCur() {
		return conCur;
	}

	public void setConCur(DBConCur_Parameters conCur) {
		this.conCur = conCur;
	}
	
	public Container_Interface getObjContainer() {
		return objContainer;
	}

	public void setObjContainer(Container_Interface objContainer) {
		this.objContainer = objContainer;
	}
	
	public TabPane getTabPane_Cur() {
		return tabPane_Cur;
	}

	public void setTabPane_Cur(TabPane tabPane_Cur) {
		this.tabPane_Cur = tabPane_Cur;
	}
	
	public Object getParentObj() {
		return parentObj;
	}

	public void setParentObj(Object parentObj) {
		this.parentObj = parentObj;
	}
	
	public Stage getStageCur() {
		return stageCur;
	}

	public void setStageCur(Stage stageCur) {
		this.stageCur = stageCur;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
}
