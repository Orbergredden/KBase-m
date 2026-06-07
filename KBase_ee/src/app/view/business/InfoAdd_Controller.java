
package app.view.business;

import app.Main;
import app.exceptions.DataConnectionException;
import app.exceptions.DataQueryException;
import app.lib.AppDataObj;
import app.lib.ShowAppMsg;
import app.lib.StringUtil;
import app.model.Params;
import app.model.business.InfoHeaderItem;
import app.model.business.InfoTypeItem;
import app.model.business.Info_FileItem;
import app.model.business.Info_ImageItem;
import app.model.business.Info_TextItem;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateStyleItem;
import app.view.business.template.TemplateStyleSelect_Controller;

import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Котроллер фрейма добавления нового заголовка инфо блока и вызова фрейма редактирования этого блока
 * @author IMakarevich
 */
public class InfoAdd_Controller {
	//
	private Params params;
	long sectionId;
	TreeItem<InfoHeaderItem> selectedInfoHeader_ti;
	InfoHeaderItem selectedInfoHeader;
	
	@FXML
    private Label label_section;
	@FXML
	private TextField textField_name;
	@FXML
	private TextField textField_descr;
	@FXML
	private ComboBox<String> comboBox_infoType;
	@FXML
	private Button button_templateStyle;
	@FXML
	private Label label_templateStyle;
	@FXML
	private TextField textField_position;
	@FXML
	private Button button_next;
	@FXML
	private Button button_nextMainTab;
	@FXML
	private Button button_nextWindow;
	@FXML
	private Button button_cancel;
	
	// for comboBox_infoType
	private ObservableList<String> OList_infoType;
	List<InfoTypeItem> listInfoType;
	
	//
	TemplateStyleItem styleDefault;
	TemplateStyleItem styleSelected;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public InfoAdd_Controller () {
    	
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
    
    }
	
    /**
     * Вызывается родительским обьектом, который даёт параметры.
     * Инициализирует контролы на слое.
     */
    public void setParams (
    		Params params, 
    		long sectionId,
    		TreeItem<InfoHeaderItem> selectedInfoHeader_ti) {
    	this.params = params;
        this.sectionId = sectionId;
        
        // init controls
        this.selectedInfoHeader_ti = selectedInfoHeader_ti;
        if (selectedInfoHeader_ti != null)  this.selectedInfoHeader = selectedInfoHeader_ti.getValue();
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями 
     */
    private void initControlsValue() {
	
    	label_section.setText(params.getConCur().db.sectionGetById(sectionId).getName()+" ("+sectionId+")");
    	
    	//-------- init comboBox_infoType
    	OList_infoType = FXCollections.observableArrayList();
    	listInfoType = params.getConCur().db.infoTypeList();
    	for (InfoTypeItem i : listInfoType) {
    		OList_infoType.add(i.getName() +" ("+ i.getId() +")");
		}
    	comboBox_infoType.setItems(OList_infoType);
    	comboBox_infoType.setValue(OList_infoType.get(0));    // первый эдемент в списке
    	
    	comboBox_infoType.getSelectionModel().selectedItemProperty().addListener( (options, oldValue, newValue) -> {
            getAndShowDefaultStyle (newValue);
    	});
    	
    	//-------- init style controls
    	button_templateStyle.setTooltip(new Tooltip("Выбор стиля"));
    	button_templateStyle.setGraphic(new ImageView(new Image("file:resources/images/icon_templates/icon_template_link_16.png")));
    	
    	// get default style and output
    	getAndShowDefaultStyle (comboBox_infoType.getSelectionModel().getSelectedItem());
    	
    	//-------- get position
    	if (selectedInfoHeader_ti != null) {     // добавление перед текущим
    		long positionPrev = params.getConCur().db.infoGetMaxPosition (sectionId, selectedInfoHeader.getPosition());
    		if ((selectedInfoHeader.getPosition() - positionPrev) == 1) {
    			// если нет промежутка для нового блока, берем позицию текущего
    			textField_position.setText(Long.toString(selectedInfoHeader.getPosition()));
    		} else {    // есть промежуток, вычисляем середину
    			long positionNew = positionPrev + ((selectedInfoHeader.getPosition() - positionPrev) / 2);
    			textField_position.setText(Long.toString(positionNew));
    		}
    	} else {                                 // добавление в конец списка
    		textField_position.setText(Long.toString(params.getConCur().db.infoGetMaxPosition(sectionId,0)+10));
    	}
    	
    	//-------- init buttons
		button_next.setTooltip(new Tooltip("Редактирование инфо блока...\n во внутреннем табе"));
    	button_next.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_1_16.png")));
		button_nextMainTab.setTooltip(new Tooltip("Редактирование инфо блока...\n в главном табе"));
		button_nextMainTab.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_2_16.png")));
		button_nextWindow.setTooltip(new Tooltip("Редактирование инфо блока...\n в отдельном окне"));
		button_nextWindow.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_3_16.png")));
    	button_cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
    
    /**
     * определяем и показываем стиль по умолчанию для выбранного типа инфо блока
     */
    private void getAndShowDefaultStyle (String comboBoxItemName) {
    	long themeDefId = AppDataObj.sectionGetDefaultTheme(params, sectionId);
    	long infoTypeSelId = StringUtil.getIdFromComboName(comboBoxItemName);
    	styleDefault = params.getConCur().db.templateStyleGetDefault(themeDefId, infoTypeSelId);
    	
    	// check for exist template for default style
    	if (styleDefault != null) {
    		TemplateItem ti = params.getConCur().db.templateGet(themeDefId, styleDefault.getId());
    		if (ti == null) {
    			ShowAppMsg.showAlert("WARNING", "Предупреждение", 
    					"Для стиля по умолчанию "+styleDefault.getName()+" ("+styleDefault.getId()+") нет шаблона", 
    					"Стиль не может использоваться.");
    			styleDefault = null;
    		}
    	}
    	
    	// output
    	if (styleDefault != null)  
    		label_templateStyle.setText("[по умолчанию] "+styleDefault.getName()+" ("+styleDefault.getId()+")");
    	else 
    		label_templateStyle.setText("");
    }

	/**
	 * Создает новый пустой инфо блок и возвращает его
	 */
	private InfoHeaderItem createInfoBlock () {
		long infoTypeId = StringUtil.getIdFromComboName(comboBox_infoType.getSelectionModel().getSelectedItem());

		//-------- save stage position
		Preferences prefs = Preferences.userNodeForPackage(SectionEdit_Controller.class);
		prefs.putDouble("stageInfoAdd_Width", params.getStageCur().getWidth());
		prefs.putDouble("stageInfoAdd_Height",params.getStageCur().getHeight());
		prefs.putDouble("stageInfoAdd_PosX",  params.getStageCur().getX());
		prefs.putDouble("stageInfoAdd_PosY",  params.getStageCur().getY());

		//-------- Проверяем заполненные поля
		if ((textField_name.getText().equals("") || (textField_name.getText() == null))) {
			ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Название блока", "Укажите Название блока");
			return null;
		}
		if (infoTypeId <= 0) {
			ShowAppMsg.showAlert("WARNING", "Нет данных", "Не выбран тип блока", "Выберите тип блока");
			return null;
		}
		if ((styleDefault == null) && (styleSelected == null)) {
			ShowAppMsg.showAlert("WARNING", "Нет данных", "Не выбран стиль блока", "Выберите стиль блока");
			return null;
		}
		try {
			if (Long.parseLong(textField_position.getText()) <= 0) {
				ShowAppMsg.showAlert("WARNING", "Нет данных", "Неверное значение позиции блока.", "Укажите правильное значение.");
				return null;
			}
		} catch (NumberFormatException e) {
			ShowAppMsg.showAlert("WARNING", "Неверные данные", "Неверное значение позиции блока.", "Укажите правильное значение.");
			return null;
		}

		InfoHeaderItem ihi = null;
		
		try {
			//-------- Проверяем и упорядочиваем (при необходимости) позициии
			long infoId = 0;
			long posInfo = params.getConCur().db.infoPositionCheckAndRenumber(sectionId, Long.parseLong(textField_position.getText()));
	
			//-------- Создаем заголовок инфо блока и добавляем его в БД
			// получаем новый infoId в соответствии с типом блока
			switch ((int)infoTypeId) {
				case 1 :                // Простой текст
					infoId = params.getConCur().db.info_TextNextId();
					break;
				case 2 :                // Изображение
					infoId = params.getConCur().db.info_ImageNextId();
					break;
				case 3 :                // Файл
					infoId = params.getConCur().db.info_FileNextId();
					break;
			}
			//
			ihi = new InfoHeaderItem (
					params.getConCur().db.infoNextId(),
					sectionId,
					infoTypeId,
					(styleSelected != null) ? styleSelected.getId() : 0,
					infoId,
					posInfo,
					textField_name.getText(),
					textField_descr.getText()
			);
			params.getConCur().db.infoAdd(ihi);
			ihi = params.getConCur().db.infoGet(ihi.getId());
	
			//-------- Создаем пустой инфо блок соответствующего типа
			switch ((int)infoTypeId) {
				case 1 :                // Простой текст
					Info_TextItem iti = new Info_TextItem (
							infoId,
							ihi.getName(),
							"",
							1                   // isShowTitle
					);
					params.getConCur().db.info_TextAdd(iti);
					break;
				case 2 :                // Изображение
					Info_ImageItem ii = new Info_ImageItem (
							infoId,
							ihi.getName(),
							new Image("file:resources/images/icon_empty_128.png"),
							0,
							0,
							"",
							"",
							1,                   // isShowTitle
							0,
							0
					);
					params.getConCur().db.info_ImageAdd(ii, "resources/images/icon_empty_128.png");
					break;
				case 3 :                // Файл
					Info_FileItem ifi = new Info_FileItem (
							infoId, 
							ihi.getName(), 
							null, 
							"",
				            0,
				            "", 
				            "", 
				            1,
				            0,
				            0
							);
					params.getConCur().db.info_FileAdd(ifi, "resources/files/empty.txt");
					break;
			}
	
			//-------- refresh headers list
			((DocumentView_Controller)params.getParentObj()).handleButtonRefresh();
		} catch (DataConnectionException | DataQueryException e) {
			e.writeLog(params);
			ShowAppMsg.showAlert(
                    "ERROR", "Помилка при створенні пустого інфо блоку. ",
                    Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
		}

		return ihi;
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
			controller.setParentObj(this, dialogStage, params.getConCur(), 
					AppDataObj.sectionGetDefaultTheme(params, sectionId),
					StringUtil.getIdFromComboName(comboBox_infoType.getSelectionModel().getSelectedItem()), 
					styleSelected, 0);
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        if (controller.isSelected) {
	        	switch (controller.styleTypeSelection) {
	        	case 0 :         // default style
	        		styleDefault  = controller.styleSelected;
	        		styleSelected = null;
	        		label_templateStyle.setText("[по умолчанию] "+styleDefault.getName()+" ("+styleDefault.getId()+")");
	        		break;
	        	case 1 :         // current style
	        	case 2 :          // style from list
	        		styleSelected = controller.styleSelected;
	        		label_templateStyle.setText(styleSelected.getName()+" ("+styleSelected.getId()+")");
	        		break;
	        	}
	        }
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Вызывается при нажатии на кнопке "Далее"
     */
    @FXML
    private void handleButtonNext() {
		InfoHeaderItem ihi = createInfoBlock();

    	if (ihi == null)  return;

    	//-------- открываем таб для редактирования
    	Params params = new Params(this.params);
    	
    	AppDataObj.openEditInfo (params, ihi);
    	
    	//-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

	/**
	 * Вызывается при нажатии на кнопке "Далее" (в главном табе)
	 */
	@FXML
	private void handleButtonNextMainTab() {
		InfoHeaderItem ihi = createInfoBlock();

		if (ihi == null)  return;

		//-------- открываем таб для редактирования
		Params params = new Params(this.params);
		params.setObjContainer(params.getRootController());
		params.setTabPane_Cur(params.getTabPane_Main());
		
		AppDataObj.openEditInfo (params, ihi);
		
		//-------- close window
		// get a handle to the stage
		Stage stage = (Stage) button_cancel.getScene().getWindow();
		// do what you have to do
		stage.close();
	}

	/**
	 * Вызывается при нажатии на кнопке "Далее" (в отдельном окне)
	 */
	@FXML
	private void handleButtonNextWindow() {
		InfoHeaderItem ihi = createInfoBlock();

		if (ihi == null)  return;

		//-------- close dialog window
		// get a handle to the stage
		Stage stage = (Stage) button_cancel.getScene().getWindow();
		// do what you have to do
		stage.close();

		//-------- открываем окно для редактирования
		(new AppDataObj()).openEditInfoInWin(params, ihi);
	}

    /**
     * Вызывается при нажатии на кнопке "Отмена"
     */
    @FXML
    private void handleButtonCancel() {
    	//-------- save stage position
    	Preferences prefs = Preferences.userNodeForPackage(SectionEdit_Controller.class);
    	prefs.putDouble("stageInfoAdd_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageInfoAdd_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageInfoAdd_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageInfoAdd_PosY",  params.getStageCur().getY());
    	
        // get a handle to the stage
        Stage stage = (Stage) button_cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
