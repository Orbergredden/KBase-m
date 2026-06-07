package app.view.business.template;

import java.util.Date;
import java.util.prefs.Preferences;

import app.lib.DateConv;
import app.lib.FileCache;
import app.lib.ShowAppMsg;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.template.TemplateFileItem;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateSimpleItem;
import app.model.business.template.TemplateStyleItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Контроллер форма для добавления/изменения директории файлов, стилей или шаблонов
 * 
 * @author Igor Makarevich
 */
public class TemplateDirEdit_Controller {
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
    public TemplateDirEdit_Controller () {
    	prefs = Preferences.userNodeForPackage(TemplateDirEdit_Controller.class);
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
    	TemplateFileItem tip;
    	TemplateStyleItem sip;
    	TemplateItem ti;
    	
    	//-------- save stage position
    	prefs.putDouble("stageTemplateDirEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateDirEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateDirEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateDirEdit_PosY",  params.getStageCur().getY());
    	
    	//---------- check data in fields
    	if ((textField_Name.getText().equals("") || (textField_Name.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Название директории", "Укажите Название директории");
    		return;
    		//throw new KBase_Ex (1, "Ошибка при сохранении", "Не заполнено Название темы", this);
    	}
    	
    	//-------- Сохраняем
    	switch (actionType) {
    	case ACTION_TYPE_ADD :
    		long newId;
    		
    		switch (editedItem.getTypeItem()) {
    		case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
    		case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
    			newId = conn.db.templateFileNextId();
    			
    			// проверка на уникальность имени нової директорії в бальківській директорії
        		//System.out.println("textField_FileName.getText() = "+ textField_FileName.getText());
        		if (conn.db.templateFileIsExistNameInDir(
        				newId, 
        				editedItem.getId(),              // parent_id
        				editedItem.getThemeId(),
        				(int)editedItem.getSubtypeItem(),     // type
        				textField_Name.getText())) {
        			ShowAppMsg.showAlert("WARNING", "Додавання директорії файлів для шаблонів.",
        					"Директорія або файл з такою назвою вже існує у вказаній батьківській директорії.",
        					"Додавання перерване.");
        			return;
    			}
    			
    			tip = new TemplateFileItem(
    					newId, editedItem.getId(), editedItem.getThemeId(), (int)editedItem.getSubtypeItem(), 0, 
    					textField_Name.getText(), textField_Descr.getText(), null, null
    					);
    			conn.db.templateFileAdd (tip);             // обьект-директорию добавляем в БД
    			tip = conn.db.templateFileGetById(newId);  // get full info
    			
    			//--- добавляем в контрол-дерево
    			resultItem = new TreeItem<>(tip);
    			//((TemplateList_Controller)params.getParentObj()).treeViewCtrl.root.getChildren().add(item);
    			editedItem_ti.getChildren().add(resultItem);
    			
    			// при необходимости кешируем директорію на диске
    			FileCache fileCache = new FileCache (params, tip.getThemeId());
    			fileCache.createTemplateFile(tip);
        		
    			// выводим сообщение в статус бар
    			params.setMsgToStatusBar("Директория файлов для шаблонов '" + tip.getName() + "' добавлена.");
    			
    			break;
    		case TemplateSimpleItem.TYPE_ITEM_DIR_STYLE :
    			newId = conn.db.templateStyleNextId();
    			
    			//-------- add to DB
    			sip = new TemplateStyleItem(
    					newId,
    					editedItem.getId(),
    					(int)editedItem.getSubtypeItem(),  // type
    					editedItem.getFlag(),         // infoTypeId
    					textField_Name.getText(),
    					textField_Descr.getText(),
    					"");                          // tag
    			conn.db.templateStyleAdd(sip);  // стиль добавляем в БД
    			sip = conn.db.templateStyleGet(newId);                   // get full info by Id
    			
    			// добавляем в контрол-дерево. Добавляем во все темы.
    			((TemplateList_Controller)params.getParentObj()).treeViewCtrl.addStyleItemRecursive(
    					((TemplateList_Controller)params.getParentObj()).treeViewCtrl.root,
    					sip);
    			// раскрываем текущий элемент
    			editedItem_ti.setExpanded(true);
    			
    			// выводим сообщение в статус бар
    			params.setMsgToStatusBar("Стиль '" + sip.getName() + "' добавлен.");
    			
    			break;
    		case TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE :
    			newId = conn.db.templateNextId();
    			
    			ti = new TemplateItem(
    					newId,
    					editedItem.getId(),
    					1, //(int)editedItem.getSubtypeItem(),  // type - директория, без разницы зарезервированная или нет
    					textField_Name.getText(),
    					textField_Descr.getText(),
    					"");
    			conn.db.templateAdd(ti);             // обьект-директорию добавляем в БД
    			ti = conn.db.templateGet(newId);     // get full info
    			
    			//--- добавляем в контрол-дерево
    			resultItem = new TreeItem<>(ti);
    			editedItem_ti.getChildren().add(resultItem);
        		
    			// раскрываем текущий элемент
    			editedItem_ti.setExpanded(true);
    			
    			// выводим сообщение в статус бар
    			params.setMsgToStatusBar("Директория шаблонов '" + ti.getName() + "' добавлена.");
    			
    			break;
    		}
    		break;
    	case ACTION_TYPE_EDIT :
    		switch (editedItem.getTypeItem()) {
    		case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
    		case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
    			// проверка на уникальность имени нової директорії в батьківській директорії
        		if (conn.db.templateFileIsExistNameInDir(
        				editedItem.getId(), 
        				editedItem_ti.getParent().getValue().getId(),      // parent_id
        				editedItem.getThemeId(),
        				(int)editedItem.getSubtypeItem(),     // type
        				textField_Name.getText())) {
        			ShowAppMsg.showAlert("WARNING", "Редачування директорії файлів для шаблонів.",
        					"Директорія або файл з такою назвою вже існує у вказаній батьківській директорії.",
        					"Редачування перерване.");
        			return;
    			}
    			
    			// create directory of files object and update it into db
    			tip = new TemplateFileItem(
    					editedItem.getId(), 
    					editedItem_ti.getParent().getValue().getId(), 
    					editedItem.getThemeId(), 
    					(int)editedItem.getSubtypeItem(), 
    					0, 
    					textField_Name.getText(), 
    					textField_Descr.getText(), 
    					null, 
    					null
    					);
    			conn.db.templateFileUpdate(tip);
    			tip = conn.db.templateFileGetById(editedItem.getId()); // get full info
    			
    			// update in TreeTableView
    			editedItem_ti.setValue(null);
    			editedItem_ti.setValue(tip);

    			// определяем текущий активный итем
    			resultItem = editedItem_ti;

    			// при необходимости кешируем директорію на диске
    			FileCache fileCache = new FileCache (params, tip.getThemeId());
    			fileCache.createTemplateFile(tip);
    			
    			// выводим сообщение в статус бар
    			params.setMsgToStatusBar("Директория файлов для шаблонов '" + tip.getName() + "' изменена.");
    			
    			break;
    		case TemplateSimpleItem.TYPE_ITEM_DIR_STYLE :
    			sip = conn.db.templateStyleGet(editedItem.getId());
    			
    			//-------- create style object and update it into db
    			sip = new TemplateStyleItem(
    					sip.getId(),
    					sip.getParentId(),
    					sip.getType(),
    					sip.getInfoTypeId(),
    					textField_Name.getText(),
    					textField_Descr.getText(),
    					sip.getTag(),
    					sip.getDateCreated(),
    					null,
    					sip.getUserCreated(),
    					null
    					);
    			conn.db.templateStyleUpdate(sip);
    			sip = conn.db.templateStyleGet(sip.getId());                   // get full info by Id
    			
    			// update in TreeTableView. Изменяем во всех темах
    			((TemplateList_Controller)params.getParentObj()).treeViewCtrl.updateStyleItemRecursive(
    					((TemplateList_Controller)params.getParentObj()).treeViewCtrl.root,
    					sip);

    			// определяем текущий активный итем
    			resultItem = editedItem_ti;

    			// выводим сообщение в статус бар
    			params.setMsgToStatusBar("Стиль '" + sip.getName() + "' змінений.");
    			
    			break;
    		case TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE :
    			// create template object and update it into db
    			ti = conn.db.templateGet(editedItem.getId());
    			ti.setName(textField_Name.getText());
    			ti.setDescr(textField_Descr.getText());
    			conn.db.templateUpdate(ti);
    			ti = conn.db.templateGet(ti.getId());     // get full info
    			
    			// update in TreeTableView
    			editedItem_ti.setValue(null);
    			editedItem_ti.setValue(ti);

    			// определяем текущий активный итем
    			resultItem = editedItem_ti;

    			// выводим сообщение в статус бар
    			params.setMsgToStatusBar("Директория шаблонов '" + ti.getName() + "' изменена.");
    			
    			break;
    		}
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
    	prefs.putDouble("stageTemplateDirEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateDirEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateDirEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateDirEdit_PosY",  params.getStageCur().getY());
    	
    	//-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
