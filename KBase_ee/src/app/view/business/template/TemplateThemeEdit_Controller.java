package app.view.business.template;

import app.lib.DateConv;
import app.lib.ShowAppMsg;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.template.TemplateSimpleItem;
import app.model.business.template.TemplateThemeItem;

import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Контроллер форма для добавления/изменения темы
 * 
 * @author Igor Makarevich
 */
public class TemplateThemeEdit_Controller {
	// for actionType
	public static final int ACTION_TYPE_ADD       = 0;
	public static final int ACTION_TYPE_EDIT      = 1;
	
	private Params params;
	private DBConCur_Parameters conn;
    /**
     * Тип операции : 0 - добавить ; 1 - редактировать
     */
    private int actionType;
    /**
     * Текущая запись в дереве шаблонов, передаваемая в класс
     */
    private TreeItem<TemplateSimpleItem> editedItem_ti;
	private TemplateSimpleItem editedItem;
	/**
	 * Результирующий итем в дереве после создания/изменения
	 */
	private TreeItem<TemplateSimpleItem> resultItem;
	
	@FXML
	private Label label_Id;
	@FXML
	private TextField textField_Name;
	@FXML
	private TextField textField_Descr;
	@FXML
	private Label label_DateCreated;
	@FXML
	private Label label_DateModified;
	@FXML
	private Label label_UserCreated;
	@FXML
	private Label label_UserModified;
	@FXML
	private Button button_Ok;
	@FXML
	private Button button_Cancel;
	
	private Preferences prefs;
	private DateConv dateConv;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TemplateThemeEdit_Controller () {
    	prefs = Preferences.userNodeForPackage(TemplateThemeEdit_Controller.class);
    	dateConv = new DateConv();
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {         }
    
    /**
     * Вызывается родительским обьектом, которое даёт на себя ссылку.
     * Инициализирует контролы на слое.
     * 
     * @param 
     *        actionType : 0 - добавить ; 1 - редактировать
     */
    public void setParams(Params params, int actionType, 
    		TreeItem<TemplateSimpleItem> editedItem_ti) {
    	this.params     = params;
    	this.conn       = params.getConCur();
    	this.actionType = actionType;
        this.editedItem_ti = editedItem_ti;
        if (editedItem_ti != null) this.editedItem = editedItem_ti.getValue();
        else                       this.editedItem = null;
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями из главного класса
     */
    private void initControlsValue() {
    	
    	if (actionType == 0) {                 // add
    		label_Id.setText("");
    		label_DateCreated.setText("");
    		label_DateModified.setText("");
    		label_UserCreated.setText("");
    		label_UserModified.setText("");
    	} else if (actionType == 1) {         // update
    		label_Id.setText(Long.toString(editedItem.getId()));
			textField_Name.setText(editedItem.getName());
			textField_Descr.setText(editedItem.getDescr());
			label_DateCreated.setText(dateConv.dateTimeToStr(editedItem.getDateCreated()));
			label_DateModified.setText(dateConv.dateTimeToStr(editedItem.getDateModified()));
			label_UserCreated.setText(editedItem.getUserCreated());
			label_UserModified.setText(editedItem.getUserModified());
    	}
    	
    	//======== buttons
    	button_Ok.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
	
    /**
     * Вызывается при нажатии на кнопке "Ok"
     */
    @FXML
    private void handleButtonOk() {
    	TemplateThemeItem tip;
    	
    	//-------- save stage position
    	prefs.putDouble("stageThemeEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageThemeEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageThemeEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageThemeEdit_PosY",  params.getStageCur().getY());
    	
    	//---------- check data in fields
    	if ((textField_Name.getText().equals("") || (textField_Name.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Название темы", "Укажите Название темы");
    		return;
    		//throw new KBase_Ex (1, "Ошибка при сохранении", "Не заполнено Название темы", this);
    	}
    	
    	//-------- Сохраняем
    	switch (actionType) {
    	case ACTION_TYPE_ADD :
			long newId = conn.db.templateThemeNextId();
			
			tip = new TemplateThemeItem (
					newId,
					textField_Name.getText(),
					textField_Descr.getText()
			);
			conn.db.templateThemeAdd(tip);             // обьект-тему добавляем в БД
			tip = conn.db.templateThemeGetById(newId); // get full info
			//tip.setThemeId(tip.getId());
			//tip.setTypeItem(TemplateSimpleItem.TYPE_ITEM_THEME);
			
			//--- добавляем в контрол-дерево
			resultItem = new TreeItem<>(tip);
			//((TemplateList_Controller)params.getParentObj()).treeViewCtrl.root.getChildren().add(item);
			editedItem_ti.getChildren().add(resultItem);
    		
			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Тема шаблонов '" + tip.getName() + "' добавлена.");
    		
    		break;
    	case ACTION_TYPE_EDIT :
			// create theme object and update it into db
			tip = new TemplateThemeItem (
					editedItem.getId(),
					textField_Name.getText(),
					textField_Descr.getText()
			);
			conn.db.templateThemeUpdate(tip);
			tip = conn.db.templateThemeGetById(editedItem.getId()); // get full info

			// update in TreeTableView
			editedItem_ti.setValue(null);
			editedItem_ti.setValue(tip);

			// определяем текущий активный итем
			resultItem = editedItem_ti;

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Тема '" + tip.getName() + "' изменена.");
    	
    		break;
    	}
    	
    	//-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
    
    /**
     * Вызывается при нажатии на кнопке "Отмена"
     */
    @FXML
    private void handleButtonCancel() {
    	//-------- save stage position
    	prefs.putDouble("stageThemeEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageThemeEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageThemeEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageThemeEdit_PosY",  params.getStageCur().getY());
    	
    	//-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
