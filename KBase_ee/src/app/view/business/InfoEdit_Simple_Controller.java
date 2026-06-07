package app.view.business;

import app.model.AppItem_Interface;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.StateList;

/**
 * Родительский (абстрактный) контроллер для всего разнообразия видов инфо блоков
 * @author Igor Makarevich
 */
public abstract class InfoEdit_Simple_Controller implements AppItem_Interface {
	protected Params params;
	protected InfoEdit_Controller parrentObj;
	protected DBConCur_Parameters conn;
	protected long infoId;
	protected long infoTypeId;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    InfoEdit_Simple_Controller () {      }
    
    /**
     * Вызывается родительским обьектом, которое даёт на себя ссылку.
     * Инициализирует переменные и контролы.
     */
    public void setParams(Params params, long infoTypeId, long infoId) {
    	this.params     = params;
        this.parrentObj = (InfoEdit_Controller)params.getParentObj();
        this.conn       = params.getConCur();
        this.infoTypeId = infoTypeId;
        this.infoId = infoId;
        
        // init controls
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями  
     */
    abstract void initControlsValue();
    
    /**
     * Проверка введенных значений
     */
    abstract void check();
	
    /**
	 * Название элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public String getName() {
		return "InfoEdit_Simple_Controller";
	}

	/**
	 * id обекта элемента приложения - id заголовка инфо блока
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public long getAppItemId() {
		return 0;
	}

	/**
	 * id соединения с базой данных
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public int getDbConnId() {
		return conn.Id;
	}

	/**
	 * контроллер элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public Object getController() {
		return this;
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.            <br>
     * Сохранение  измененной информации в БД
     */
    public void save () {
    }
    
    /**
	 * Реализуем метод интерфейса AppItem_Interface.            <br>
     * Сохраняем всю измененную информацию в БД
     */
    public void saveAll () {
    	save();
    }
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.            <br>
	 * Сохраняем состояние контролов в иерархической структуре
	 */
	public void saveControlsState (StateList stateList) {
		//System.out.println(">>> InfoEdit_Simple_Controller :: saveControlsState");
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Восстанавливаем состояние контролов из иерархической структуры
	 */
	public void restoreControlsState (StateList stateList) {
		//System.out.println(">>> InfoEdit_Simple_Controller :: restoreControlsState");
	}
}
