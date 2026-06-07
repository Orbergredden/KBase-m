package app.model;

//import java.time.LocalDateTime;

/**
 * Интерфейс. <br>
 * Элемент приложения, информация и действия <br>
 * Методы сохранения и восстановления состояния контролов в интерфейсе приложения.
 */
public interface AppItem_Interface {
	// for Name 
	public static final String ELEMENT_SECTION_LIST    = "SectionList_Controller";
	public static final String ELEMENT_DOCUMENT_VIEW   = "DocumentView_Controller";
	public static final String ELEMENT_DICTIONARY_VIEW = "DictionaryView_Controller";
	public static final String ELEMENT_INFO_EDIT       = "InfoEdit_Controller";
	public static final String ELEMENT_ICON_LIST       = "IconsList_Controller";
	public static final String ELEMENT_TEMPLATE_LIST   = "TemplateList_Controller";
	public static final String ELEMENT_TASK_LIST       = "TaskList_Controller";
	public static final String ELEMENT_FIND_INFO	   = "FindInfo_Controller";

	/**
	 * Получаем уникальный идентификатор обьекта
	 */
	int getOID();
	
    /**
     * Название элемента приложения
     */
    String getName();
    /**
     * Повертає параметри обьекта інтерфейса
     */
    default Params getParams () {  return null;  }
    
    /**
     * id обекта элемента приложения
     */
    default long getAppItemId() {  return 0;  }
    /**
     * id соединения с базой данных
     */
    int getDbConnId();
    /**
     * контроллер элемента приложения
     */
    Object getController();
    /**
     * Корень дерева (или другой какой то рут)
     */
    default long getRootId() {
    	return 0;
    }
    
    /**
     * Проверяем наличие несохраненных данных
     */
    default boolean checkUnsavedData () {
        return false;
    }
    
    /**
     * Сохраняем измененную информацию в БД
     */
    default void save () {
        //nothing
    }
    
    /**
     * Сохраняем всю измененную информацию в БД
     */
    default void saveAll () {
        //nothing
    }
    
    /**
     * Зачиняємо єлемент інтерфейсу
     */
    default void close () {
        //nothing
    }

    /**
     * Сохраняем состояние в иерархической структуре
     */
    default void saveControlsState (StateList stateList) {
        //nothing
    }

    /**
     * Восстанавливаем состояние контролов
     */
    default void restoreControlsState (StateList stateList) {
        //nothing
    }
}
