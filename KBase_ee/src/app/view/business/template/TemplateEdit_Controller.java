package app.view.business.template;

import java.io.File;
import java.util.Date;
import java.util.prefs.Preferences;

import app.exceptions.KBase_Ex;
import app.exceptions.KBase_ReadTextFileUTFEx;
import app.lib.DateConv;
import app.lib.FileUtil;
import app.lib.ShowAppMsg;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateSimpleItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Контроллер форма для додавання/редагування шаблона
 * 
 * @author Igor Makarevich
 */
public class TemplateEdit_Controller {
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
	
	@FXML
	private Label label_TemplateId;
	@FXML
	private Label label_TemplateParentId;
	@FXML
	private TextField textField_TemplateName;
	@FXML
	private TextField textField_TemplateDescr;
	@FXML
	private Label label_TemplateDateCreated;
	@FXML
	private Label label_TemplateDateModified;
	@FXML
	private Label label_TemplateUserCreated;
	@FXML
	private Label label_TemplateUserModified;
	@FXML
	private Label label_LinkCount;
	@FXML
	private TextArea textArea_LinkList;

	@FXML
	private Button button_TemplateFileOpen;
	@FXML
	private Button button_TemplateFileSaveToDisk;
	@FXML
	private Button button_TemplateFileSaveToDB;
	@FXML
	private TextArea textArea_TemplateContent;
	
	@FXML
	private Button button_Ok;
	@FXML
	private Button button_Cancel;
	
	//
	private Preferences prefs;
	private DateConv dateConv;
	private String fileName;       // for save to disk

	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TemplateEdit_Controller () {
    	prefs = Preferences.userNodeForPackage(TemplateEdit_Controller.class);
    	dateConv = new DateConv();
    	fileName = "";
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
    	
    	//========
    	if (actionType == ACTION_TYPE_ADD) {
    		label_TemplateId.setText("");
    		label_TemplateParentId.setText(editedItem.getName() +" ("+ Long.toString(editedItem.getId()) +")");
    		label_TemplateDateCreated.setText("");
    		label_TemplateDateModified.setText("");
    		label_TemplateUserCreated.setText("");
    		label_TemplateUserModified.setText("");
    		label_LinkCount.setText("0");
    	} else if (actionType == ACTION_TYPE_EDIT) {
    		TemplateItem ti = conn.db.templateGet(editedItem.getId());
    		
    		label_TemplateId.setText(Long.toString(editedItem.getId()));
    		label_TemplateParentId.setText(
    				editedItem_ti.getParent().getValue().getName() +
    				" ("+ Long.toString(editedItem_ti.getParent().getValue().getId()) +")");
    		textField_TemplateName.setText(editedItem.getName());
        	textField_TemplateDescr.setText(editedItem.getDescr());
        	label_TemplateDateCreated.setText(dateConv.dateTimeToStr(editedItem.getDateCreated()));
        	label_TemplateDateModified.setText(dateConv.dateTimeToStr(editedItem.getDateModified()));
        	label_TemplateUserCreated.setText(editedItem.getUserCreated());
        	label_TemplateUserModified.setText(editedItem.getUserModified());
    		
        	label_LinkCount.setText(Long.toString(editedItem.getFlag()));
        	textArea_LinkList.setText(String.join(System.lineSeparator(), conn.db.TemplateListLinks(editedItem.getId())));
        	//textArea_LinkList.setDisable(true);
        	
        	textArea_TemplateContent.appendText(ti.getBody());
    	}
    	
    	//======== buttons
    	button_TemplateFileOpen.setGraphic(new ImageView(new Image("file:resources/images/icon_file_open_16.png")));
		button_TemplateFileOpen.setTooltip(new Tooltip("Открыть шаблон..."));
		button_TemplateFileSaveToDisk.setGraphic(new ImageView(new Image("file:resources/images/icon_SaveToFile_16.png")));
		button_TemplateFileSaveToDisk.setTooltip(new Tooltip("Сохранить шаблон на диске..."));
		button_TemplateFileSaveToDB.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
		button_TemplateFileSaveToDB.setTooltip(new Tooltip("Сохранить шаблон в базе данных..."));button_TemplateFileOpen.setGraphic(new ImageView(new Image("file:resources/images/icon_file_open_16.png")));
		button_TemplateFileOpen.setTooltip(new Tooltip("Открыть шаблон..."));
		button_TemplateFileSaveToDisk.setGraphic(new ImageView(new Image("file:resources/images/icon_SaveToFile_16.png")));
		button_TemplateFileSaveToDisk.setTooltip(new Tooltip("Сохранить шаблон на диске..."));
		button_TemplateFileSaveToDB.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
		button_TemplateFileSaveToDB.setTooltip(new Tooltip("Сохранить шаблон в базе данных..."));
    	
    	button_Ok.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
	
    /**
     * Функція збереження шаблона в БД
     */
    private void save() throws KBase_Ex {
    	TemplateItem ti;
    	
    	//---------- check data in fields
		if ((textField_TemplateName.getText().equals("") || (textField_TemplateName.getText() == null))) {
    		//ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено Название шаблона", "Укажите Название шаблона");
    		//return;
    		throw new KBase_Ex (1, "Ошибка при сохранении", "Не заполнено Название шаблона", this);
    	}
    	if ((textArea_TemplateContent.getText().equals("") || (textArea_TemplateContent.getText() == null))) {
    		//ShowAppMsg.showAlert("WARNING", "Нет данных", "Нет текста шаблона", "Укажите текст шаблона");
    		//return;
    		throw new KBase_Ex (1, "Ошибка при сохранении", "Нет текста шаблона", this);
    	}
    	
    	//-------- Сохраняем
    	switch (actionType) {
    	case ACTION_TYPE_ADD :
    		long newId = conn.db.templateNextId();
    		
    		//-------- add to DB
    		ti = new TemplateItem(
    				newId,
    				editedItem.getId(),
    				(editedItem.getSubtypeItem() == 1) ? 0 : 10,
    				textField_TemplateName.getText(),
    				textField_TemplateDescr.getText(),
    				textArea_TemplateContent.getText()
    				);
    		conn.db.templateAdd(ti);  // шаблон добавляем в БД
    		ti = conn.db.templateGet(newId);                   // get full info by Id
    		
    		//--- добавляем в контрол-дерево
			// Prepare a new TreeItem with a new template object
			TreeItem<TemplateSimpleItem> item = new TreeItem<>(ti);
			// Add the new item as children to the parent item
			// Make sure the new item is visible
			editedItem_ti.getChildren().add(item);
			editedItem_ti.setExpanded(true);
			
			// для наступних зберігань кнопкою на панелі ісходнику
			actionType = ACTION_TYPE_EDIT;
			editedItem_ti = item;
			editedItem = editedItem_ti.getValue();
			
			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Новий шаблон '" + ti.getName() + "' доданий.");
    		
    		break;
    	case ACTION_TYPE_EDIT :
    		ti = conn.db.templateGet(editedItem.getId());
    		
    		//-------- дополнительные проверки
			if (	textField_TemplateName.getText().equals(ti.getName()) &&
					textField_TemplateDescr.getText().equals(ti.getDescr()) &&
					textArea_TemplateContent.getText().equals(ti.getBody())
					) {
				//ShowAppMsg.showAlert("WARNING", "Изменение шаблона", "Нет никаких изменений", "Укажите новые значения.");
				//return;
				throw new KBase_Ex (1, "Ошибка при сохранении", "Нет никаких изменений", this);
			}
    	
			//-------- create template object and update it into db
			ti = new TemplateItem (
					ti.getId(),
					ti.getParentId(),
					ti.getType(),
					textField_TemplateName.getText(),
					textField_TemplateDescr.getText(),
					textArea_TemplateContent.getText(),
					ti.getDateCreated(),
					null,
					ti.getUserCreated(),
					null
			);
			conn.db.templateUpdate (ti);
			ti = conn.db.templateGet(ti.getId());                   // get full info by Id
			ti.setFlag(conn.db.TemplateListLinks(ti.getId()).size());  // кількість лінків у шаблона
			
			editedItem_ti.setValue(null);
			editedItem_ti.setValue(ti);
			
			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Шаблон '" + ti.getName() + "' змінено.  " + (new Date()));
    		
    		break;
    	}
    }
    
	/**
     * Вызывается для открытия тестового файла с тектом для шаблона
     */
    @FXML
    private void handleButtonTemplateFileOpen() {
    	FileChooser fileChooser = new FileChooser();
    	String curDir;
    	
    	// Задаём фильтр расширений
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML файлы (*.html)", "*.html"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы (*.txt)", "*.txt"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы (*.*)", "*.*"));
    	
        // set directory
        curDir = prefs.get("stageTemplateEdit_SelectTemplateDir", "");
        if (! curDir.equals("")) {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory()) {
                fileChooser.setInitialDirectory(dir);
            }
        }
        
        // Показываем диалог загрузки файла
        File file = fileChooser.showOpenDialog(params.getStageCur());
    	
        if (file != null) {
        	int fileType = FileUtil.getFileTypeByExt (file.toString());// return 0 - тип не определен ; 1 - текстовый ; 2 - картинка
        	
        	if (fileType == 0) {                   // 0 - тип не определен
        		//textArea_TemplateContent.appendText("Тип загружаемого файла не определен !" +"\n");
        		ShowAppMsg.showAlert("WARNING", "Тип файла", "Загрузка файла шаблона", "Тип загружаемого файла не определен !");
        		return;
        	}

        	if (fileType == 1) {                   // 1 - текстовый
        		try {
        			textArea_TemplateContent.clear();
        			
    				//textArea_TemplateContent.appendText(FileUtil.readTextFileToString(label_TemplateNameNew.getText()));
        			textArea_TemplateContent.appendText(FileUtil.readTextFileToString(file.toString()));
    			} catch (KBase_ReadTextFileUTFEx e) {
    				//e.printStackTrace();
    				ShowAppMsg.showAlert("WARNING", "Чтение файла", e.msg, "Чтение файла шаблона прервано.");
        			return;
    			}
        	}
        	
        	if (fileType == 2) {                   // 2 - image
        		//textArea_TemplateContent.appendText("Картинки использовать нельзя!" +"\n");
        		ShowAppMsg.showAlert("WARNING", "Тип файла", "Загрузка файла шаблона", "Картинки здесь использовать нельзя!");
        		return;
        	}
        	
        	// save dir name
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("stageTemplateEdit_SelectTemplateDir", curDir);
        }
    }
    
    /**
     * Вызывается для сохранения шаблона в файл на диске
     */
    @FXML
    private void handleButtonTemplateFileSaveToDisk() {
    	
    	//======== get file name
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Сохранение шаблона в файл");
    	  
        //Set extension filter
    	fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML файлы (*.html)", "*.html"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы (*.txt)", "*.txt"));
        fileChooser.setInitialFileName(fileName);
        
        // set current dir
        String curDir = prefs.get("stageTemplateEdit_CurDirNameForSave", "");
        if (! curDir.equals("")) {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory()) {
                fileChooser.setInitialDirectory(dir);
            }
        }
        
        //Show save file dialog
        File file = fileChooser.showSaveDialog(params.getStageCur());
    	
        if (file != null) {
        	FileUtil.writeTextFile(file.toString(), textArea_TemplateContent.getText());
            
            // save filename and dir name
        	fileName = file.getAbsolutePath();
        	fileName = fileName.substring(fileName.lastIndexOf(File.separator)+1, fileName.length());
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("stageTemplateEdit_CurDirNameForSave", curDir);
        }
    }
    
    /**
	 * Вызывается для сохранения шаблона в БД
	 */
	@FXML
	private void handleButtonTemplateFileSaveToDB() {
		try {
			save();
		} catch (KBase_Ex e) {
			//e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", e.errSign, e.msg, "");
			return;
		}
		((TemplateList_Controller)params.getParentObj()).treeTableView_templates.sort();
	}
	
	/**
     * Вызывается при нажатии на кнопке "Ok"
     */
    @FXML
    private void handleButtonOk() {
    	
    	try {
			save();
		} catch (KBase_Ex e) {
			//e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", e.errSign, e.msg, "");
			return;
		}
    	
    	//-------- save stage position
    	prefs.putDouble("stageTemplateEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateEdit_PosY",  params.getStageCur().getY());
    	
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
    	prefs.putDouble("stageTemplateEdit_Width", params.getStageCur().getWidth());
    	prefs.putDouble("stageTemplateEdit_Height",params.getStageCur().getHeight());
    	prefs.putDouble("stageTemplateEdit_PosX",  params.getStageCur().getX());
    	prefs.putDouble("stageTemplateEdit_PosY",  params.getStageCur().getY());
    	
    	//-------- close window
    	// get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
