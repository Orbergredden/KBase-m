package app.view.business;

import java.io.IOException;
import java.util.Date;
import java.util.prefs.Preferences;

import app.Main;
import app.exceptions.DataConnectionException;
import app.exceptions.DataQueryException;
import app.lib.AppDataObj;
import app.lib.ShowAppMsg;
import app.model.AppItem_Interface;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.StateItem;
import app.model.StateList;
import app.model.business.InfoHeaderItem;
import app.model.business.InfoTypeItem;
import app.model.business.SectionItem;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateStyleItem;
import app.view.business.template.TemplateStyleSelect_Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Котроллер фрейма редактирования инфо блока
 * @author IMakarevich
 */
public class InfoEdit_Controller implements AppItem_Interface {

	private Params params;
	public long infoHeaderId;
	
	private DBConCur_Parameters conn;
	
	@FXML
	private AnchorPane anchorPane_Main;
	
	@FXML
	private Button button_Save;
	@FXML
	private Button button_Close;

	@FXML
    private TabPane tabPane_InfoEdit;
    @FXML
    private Tab tab_Information;

	@FXML
	private Label label_SectionName;
	@FXML
	private Label label_SectionPath;
	@FXML
	private Label label_InfoHeaderId;
	@FXML
	private Label label_InfoBlockId;
	@FXML
	private TextField textField_Name;
	@FXML
	private TextField textField_Descr;
	@FXML
	private Label label_InfoType;
	@FXML
	private Button button_TemplateStyle;
	@FXML
	private Label label_TemplateStyle;
	@FXML
	private TextField textField_Position;
	
	//
	private TemplateStyleItem styleDefault;
	private TemplateStyleItem styleSelected;
	
	// for info block
	private InfoEdit_Simple_Controller controller_Info;

	// признак редактирования информации после последнего сохранения
	private boolean isChanged = false;

	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public InfoEdit_Controller () {      }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {       }
	
    /**
     * Вызывается родительским обьектом, которое даёт на себя ссылку.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params, long infoHeaderId) {
    	this.params       = params;
    	this.infoHeaderId = infoHeaderId;
    	
    	conn = this.params.getConCur();
    	
        // create info block scene
        createInfoBlockScene();
        
        // init controls
        initControlsValue();
    }
    
    /**
     * Создаем сцену с полями инфо блока в под-табе "Информация"
     */
    private void createInfoBlockScene () {
    	InfoHeaderItem ihi = null;
    	String fxmlFileName;
    	AnchorPane paneForEditInfo = null;
    	
    	try {
			ihi = conn.db.infoGet(infoHeaderId);
		} catch (DataConnectionException | DataQueryException e) {
			e.writeLog(params);
			ShowAppMsg.showAlert(
                    "ERROR", "Помилка при створенні сцени з інфоблоком. ",
                    Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
		}
    	
    	//---- определяем fxml для инфо блока
    	switch ((int)ihi.getInfoTypeId()) {
    	case 1 :
    		fxmlFileName = "view/business/InfoEdit_Text.fxml";
    		break;
    	case 2 :     // Изображение
    		fxmlFileName = "view/business/InfoEdit_Image.fxml";
    		break;
    	case 3 :     // attached file
    		fxmlFileName = "view/business/InfoEdit_File.fxml";
    		break;
    	default :
    		fxmlFileName = null;
    	}
    	
    	//---- загружаем контроллер Инфо блока в AnchorPane
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource(fxmlFileName));
			paneForEditInfo = loader.load();
			
			// Даём контроллеру доступ к родителю и инициализируем
			controller_Info = loader.getController();
			
			Params params = new Params(this.params);
			params.setParentObj(this);
			
			controller_Info.setParams(params, ihi.getInfoTypeId(), ihi.getInfoId());
    	} catch (IOException e) {
            e.printStackTrace();
            ShowAppMsg.showAlert("WARNING", "Редактирование инфо блока", "Ошибка при открытии под-таба редактирования инфо блока", 
		             e.getMessage());
        }
    	
    	//---- добавляем в под-таб сцену
    	tab_Information.setContent(paneForEditInfo);
    }

    /**
     * Инициализирует контролы значениями 
     */
    private void initControlsValue() {
    	try {
	    	InfoHeaderItem ihi = conn.db.infoGet(infoHeaderId);
	    	SectionItem si = conn.db.sectionGetById(ihi.getSectionId());
	    	InfoTypeItem iti = conn.db.infoTypeGet(ihi.getInfoTypeId());
	    
	    	//======== hot keys
	    	anchorPane_Main.setOnKeyPressed(event -> {
	    		if (event.getCode() == KeyCode.S && event.isControlDown()) {
	    	        handleButtonSave();
	    	    }
	    	    if (event.getCode() == KeyCode.Q && event.isControlDown()) {
	    	        handleButtonClose();
	    	    }
	    	});
	    	
	    	//======== ToolBar
	    	button_Save.setTooltip(new Tooltip("Зберегти інформацію (Ctrl+S)"));
	    	button_Save.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
	    	button_Close.setTooltip(new Tooltip("Зачинити (Ctrl+Q)"));
	    	button_Close.setGraphic(new ImageView(new Image("file:resources/images/icon_close_16.png")));
	    	
	    	//======== TabPane
	        tabPane_InfoEdit.getSelectionModel().select(tab_Information);
	
	    	//======== Заголовок
	    	label_SectionName.setText("("+ si.getId() +") "+ si.getName());
	    	label_SectionPath.setText(conn.db.sectionGetPathName(si.getId(), " / "));
	    	label_InfoHeaderId.setText(Long.toString(ihi.getId()));
	    	label_InfoBlockId.setText(Long.toString(ihi.getInfoId()));
	    	textField_Name.setText(ihi.getName());
	    	textField_Descr.setText(ihi.getDescr());
	    	label_InfoType.setText("("+ ihi.getInfoTypeId() +") "+ iti.getName());
	    	
	    	//-------- init style controls
	    	button_TemplateStyle.setTooltip(new Tooltip("Вибір стилю"));
	    	button_TemplateStyle.setGraphic(new ImageView(new Image("file:resources/images/icon_templates/icon_template_link_16.png")));
	    	
	    	// get default style 
			long themeDefId = AppDataObj.sectionGetDefaultTheme(params, ihi.getSectionId());
	    	styleDefault = conn.db.templateStyleGetDefault(themeDefId, ihi.getInfoTypeId());
	    	
	    	if (ihi.getTemplateStyleId() == 0) {              // default style
	    		styleSelected = null;
	    		
	        	// check for exist template for default style
	        	if (styleDefault != null) {
	        		TemplateItem ti = conn.db.templateGet(themeDefId, styleDefault.getId());
	        		if (ti == null) {
	        			ShowAppMsg.showAlert("WARNING", "Предупреждение", 
	        					"Для стиля по умолчанию "+styleDefault.getName()+" ("+styleDefault.getId()+") нет шаблона", 
	        					"Стиль не может использоваться.");
	        			styleDefault = null;
	        		}
	        	} else {
	        		ShowAppMsg.showAlert("WARNING", "Предупреждение", 
	    					"Стиль по умолчанию не определен для данной темы и типа инфо блока.", 
	    					"Стиль не может использоваться.");
	        	}
	        	
	        	// output
	        	if (styleDefault != null)  
	        		label_TemplateStyle.setText("[по умолчанию] ("+styleDefault.getId()+") "+styleDefault.getName());
	        	else 
	        		label_TemplateStyle.setText("");
	    	} else {                                            // selected style
	    		styleSelected = conn.db.templateStyleGet(ihi.getTemplateStyleId());
	    		TemplateItem ti = conn.db.templateGet(themeDefId, styleSelected.getId());
	    		if (ti == null) {
	    			ShowAppMsg.showAlert("WARNING", "Предупреждение", 
	    					"Для стиля "+styleSelected.getName()+" ("+styleSelected.getId()+") нет шаблона", 
	    					"Стиль не может использоваться.");
	    			styleSelected = null;
	    		}
	
	    		// output
	        	if (styleSelected != null)  
	        		label_TemplateStyle.setText("("+styleSelected.getId()+") "+ styleSelected.getName());
	        	else 
	        		label_TemplateStyle.setText("");
	    	}
	    	
	    	//-------- init position
	    	textField_Position.setText(Long.toString(ihi.getPosition()));
	
	    	//======== define listeners
	        textField_Name.textProperty().addListener((observable, oldValue, newValue) -> {
	            //parrentObj.mainInfo_C.mainApp.statusBar_ShowMsg("textfield changed from " + oldValue + " to " + newValue);
	            setIsChanged(true);
	        });
	        textField_Descr.textProperty().addListener((observable, oldValue, newValue) -> {
	            setIsChanged(true);
	        });
	        textField_Position.textProperty().addListener((observable, oldValue, newValue) -> {
	            setIsChanged(true);
	        });
    	} catch (DataConnectionException | DataQueryException e) {
			e.writeLog(params);
			ShowAppMsg.showAlert(
                    "ERROR", "Помилка при редагуванні інфо блоку, initControlsValue.",
                    Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
		}
    }
	
    /**
     * Сохранение информации в БД
     */
    @FXML
    public void handleButtonSave() {
    	save();
    }
    
    /**
     * Закрываем таб
     */
    @FXML
    public void handleButtonClose() {
		close();
    }
    
    /**
     * Вызывается при нажатии на кнопке выбора стиля для инфо блока
     */
    @FXML
    private void handleButtonTemplateStyle() {
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/template/TemplateStyleSelect.fxml"));
			AnchorPane page = loader.load();
			
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Выбор стиля шаблонов документа");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getStageCur());
			Scene scene = new Scene(page);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_templates/icon_style_16.png"));

			Preferences prefs = Preferences.userNodeForPackage(TemplateStyleSelect_Controller.class);
			dialogStage.setWidth(prefs.getDouble("stageTemplateStyleSelect_Width", 500));
			dialogStage.setHeight(prefs.getDouble("stageTemplateStyleSelect_Height", 600));
			dialogStage.setX(prefs.getDouble("stageTemplateStyleSelect_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageTemplateStyleSelect_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			TemplateStyleSelect_Controller controller = loader.getController();
			try {
				controller.setParentObj(this, dialogStage, conn,
						AppDataObj.sectionGetDefaultTheme(params, conn.db.infoGet(infoHeaderId).getSectionId()),
						conn.db.infoGet(infoHeaderId).getInfoTypeId(),
						styleSelected, 0);
			} catch (DataConnectionException | DataQueryException e) {
				e.writeLog(params);
				ShowAppMsg.showAlert(
	                    "ERROR", "Помилка при редагуванні інфо блока, "+
	                    "відкриття вікна вибору стилю.",
	                    Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
			}
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        if (controller.isSelected) {
	        	switch (controller.styleTypeSelection) {
	        	case 0 :         // default style
	        		styleDefault  = controller.styleSelected;
	        		styleSelected = null;
	        		label_TemplateStyle.setText("[по умолчанию] ("+styleDefault.getId()+") "+styleDefault.getName());
	        		break;
	        	case 1 :         // current style
	        	case 2 :          // style from list
	        		styleSelected = controller.styleSelected;
	        		label_TemplateStyle.setText("("+styleSelected.getId()+") "+ styleSelected.getName());
	        		break;
	        	}

	        	setIsChanged(true);
	        }
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Сеттер isChanged
     */
    void setIsChanged(boolean isChanged) {

        if (this.isChanged != isChanged) {
        	this.isChanged = isChanged;
        	params.getObjContainer().showStateChanged(getOID(), isChanged);
        }
    }
    
    /**
	 * уникальный ИД обьекта
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public int getOID() {
		return hashCode();
	}

	/**
	 * Название элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public String getName() {
		return AppItem_Interface.ELEMENT_INFO_EDIT;
	}
	
	/**
	 * Повертає параметри обьекта інтерфейса
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public Params getParams() {
		return params;
	}

	/**
	 * id обекта элемента приложения - id заголовка инфо блока
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public long getAppItemId() {
		return infoHeaderId;
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
	 * Проверяем наличие несохраненных данных
	 */
	public boolean checkUnsavedData () {
		return isChanged;
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.            <br>
     * Сохранение  измененной информации в БД
     */
    public void save () {
    	
    	//-------- Проверяем заполненные поля
    	if ((textField_Name.getText().equals("") || (textField_Name.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Название блока", "Укажите Название блока");
    		return;
        }
    	if ((styleDefault == null) && (styleSelected == null)) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не выбран стиль блока", "Выберите стиль блока");
    		return;
        }
    	try {
    		if (Long.parseLong(textField_Position.getText()) <= 0) {
    			ShowAppMsg.showAlert("WARNING", "Нет данных", "Неверное значение позиции блока.", "Укажите правильное значение.");
    			return;
    		}
    	} catch (NumberFormatException e) {
    		ShowAppMsg.showAlert("WARNING", "Неверные данные", "Неверное значение позиции блока.", "Укажите правильное значение.");
			return;
    	}
    	
    	controller_Info.check();

        //--------
    	try {
	        InfoHeaderItem ihi = conn.db.infoGet(infoHeaderId);
	
	        if (isChanged) {
	            long posInfo = conn.db.infoPositionCheckAndRenumber( // Проверяем и упорядочиваем (при необходимости) позициии
	                    ihi.getSectionId(),
	                    Long.parseLong(textField_Position.getText()));
	
	            //-------- Обновляем заголовок инфо блока в БД
	            InfoHeaderItem ihi_new = new InfoHeaderItem(
	                    ihi.getId(),
	                    ihi.getSectionId(),
	                    ihi.getInfoTypeId(),
	                    (styleSelected != null) ? styleSelected.getId() : 0,
	                    ihi.getInfoId(),
	                    posInfo,
	                    textField_Name.getText(),
	                    textField_Descr.getText()
	            );
	            conn.db.infoUpdate(ihi_new);
	            ihi = conn.db.infoGet(ihi_new.getId());
	
	            //-------- save info block
	            controller_Info.save();
	
	            //-------- прибираємо в контейнері ознаку (червону бульку) незбережених змін інформації
	            setIsChanged(false);
	
	            //--------
	            params.setMsgToStatusBar("Инфо блок \"" + ihi.getName() + "\" сохранен. " +
	                    (new Date()));
	        } else {
	        	params.setMsgToStatusBar("Инфо блок \"" + ihi.getName() + "\". Нет изменений. " +
	                    (new Date()));
	        }
    	} catch (DataConnectionException | DataQueryException e) {
			e.writeLog(params);
			e.showAlert("Помилка при редагуванні інфо блока", "Збереження інформації, save().");
		}
    }
    
    /**
	 * Реализуем метод интерфейса AppItem_Interface.            <br>
     * Сохраняем всю измененную информацию в БД
     */
    public void saveAll () {
    	save();
    }
    
    /**
	 * Реалізуємо метод інтерфейсу AppItem_Interface.
     * Зачиняємо цей фрейм
     */
    public void close() {
		int btnResult;
		boolean isClose = true;
		
		if (! ShowAppMsg.showQuestion (
				"CONFIRMATION", 
				"Питання", 
				"", 
				"Закрити вкладку ?")) {
    		return;
    	}

		if (isChanged) {
            btnResult = ShowAppMsg.showQuestionWith3Buttons(
                    "CONFIRMATION",
                    "Закрытие окна редактирования",
                    "Изменненые данные не сохранены.",
                    "Выберите действие",
                    "Сохранить и выйти",
                    "Выйти без сохранения",
                    "Продолжить редактирование"
                    );
            switch (btnResult) {
                case ShowAppMsg.SELECT_BUTTON_1 :
                    handleButtonSave();
                    isClose = true;
                    break;
                case ShowAppMsg.SELECT_BUTTON_2 :
                	// прибираємо в контейнері ознаку (червону бульку) незбережених змін інформації
                    setIsChanged(false);
                    isClose = true;
                    break;
                case ShowAppMsg.SELECT_BUTTON_3 :
                    return;
                default :
                    isClose = false;
            }
        }

		if (isClose) {
			params.getObjContainer().closeContainer(getOID());
		}
    }
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.            <br>
	 * Сохраняем состояние контролов в иерархической структуре
	 */
	public void saveControlsState (StateList stateList) {
		StateItem stateItem;
		
		stateItem = stateList.add(
				"tabSubItems",
				"",
				new StateList());
		controller_Info.saveControlsState(stateItem.subItems);
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Восстанавливаем состояние контролов из иерархической структуры
	 */
	public void restoreControlsState (StateList stateList) {
		
		for (StateItem si : stateList.list) {
			switch (si.getName()) {
			case "tabSubItems" :
				controller_Info.restoreControlsState(si.subItems);
				break;
			}
		}
	}
}