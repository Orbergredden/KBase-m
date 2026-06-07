
package app.view.business.template;

import app.lib.ConvertType;
import app.lib.DateConv;
import app.lib.FileCache;
import app.lib.FileUtil;
import app.lib.ShowAppMsg;
import app.Main;
import app.model.AppItem_Interface;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.StateItem;
import app.model.StateList;
import app.model.business.InfoTypeItem;
import app.model.business.template.TemplateClipboardInfo;
import app.model.business.template.TemplateFileItem;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateSimpleItem;
import app.model.business.template.TemplateStyleItem;
import app.model.business.template.TemplateThemeItem;
import app.view.business.Container_Interface;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Контроллер фрейма каталога шаблонов. Показываем дерево-список тем и шаблонов, 
 * с возможность добавления, редактирования и удаления.
 * @author Igor Makarevich
 * @version 2.00.01.004   11.04.2021 - 27.01.2025
 */
public class TemplateList_Controller implements AppItem_Interface {
	
	private Params params;
	private DBConCur_Parameters conn;
	private Container_Interface objContainer;
    
    /**
     * Конвертор даты/времени
     */
    DateConv dateConv;
    
    @FXML
	private Button button_Exit;
    @FXML
    private TitledPane titledPane_Title;

    @FXML
	private Button button_templateAdd;
    @FXML
	private Button button_templateUpdate;
    @FXML
	private Button button_templateDelete;
    @FXML
	private Button button_templateCopy;
    @FXML
	private Button button_templateCut;
    @FXML
	private Button button_templatePaste;
    @FXML
	private Button button_treeRefresh;
    
    @FXML
	public TreeTableView<TemplateSimpleItem> treeTableView_templates;
	@FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_id;
    @FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_name;
    @FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_descr;
    @FXML
    private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_dateCreated;
    @FXML
    private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_dateModified;
    @FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_userCreated;
    @FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_userModified;

    @FXML
	private MenuItem menuitem_templateAdd;
    @FXML
	private MenuItem menuitem_templateUpdate;
    @FXML
	private MenuItem menuitem_templateDelete;
    @FXML
	private MenuItem menuitem_templateCopy;
    @FXML
	private MenuItem menuitem_templateCut;
    @FXML
	private MenuItem menuitem_templatePaste;
    @FXML
	private MenuItem menuitem_treeRefresh;
    
    //
	TemplateList_Controller.TreeView_Controller treeViewCtrl;

    //
	private Preferences prefs;
    
    /**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TemplateList_Controller () {
    	dateConv = new DateConv();
		prefs = Preferences.userNodeForPackage(TemplateList_Controller.class);

		treeViewCtrl = this.new TreeView_Controller();
    }
	
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
    	
    }
    
    /**
     * Вызывается главным приложением, которое даёт на себя ссылку.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params) {
    	this.params       = params;
    	this.conn         = params.getConCur();
    	this.objContainer = params.getObjContainer();
    	
        // init controls
        initControlsValue();
    }
	
    /**
     * Инициализирует контролы значениями 
     */
    private void initControlsValue() {
    	button_Exit.setTooltip(new Tooltip("Закрыть фрейм"));
    	
    	// title
    	titledPane_Title.setText(titledPane_Title.getText() + " - " + conn.param.getConnName());
    	
    	if (conn.param.getColorEnable()) {
    		titledPane_Title.setStyle(
    				"-fx-body-color: #" + 
    						ConvertType.colorToHex(new Color(conn.param.getColorBRed_A(),  conn.param.getColorBGreen_A(),
    								conn.param.getColorBBlue_A(), conn.param.getColorBOpacity_A())) + 
    						";" +
    						"-fx-text-fill: #" +
    						ConvertType.colorToHex(new Color(conn.param.getColorTRed_A(),  conn.param.getColorTGreen_A(),
              		                             conn.param.getColorTBlue_A(), conn.param.getColorTOpacity_A())) +
    				";");
    	}
    	
    	// ToolBar
    	button_templateAdd.setTooltip(new Tooltip("Добавить новый элемент"));
    	button_templateAdd.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
    	button_templateUpdate.setTooltip(new Tooltip("Изменить элемент"));
    	button_templateUpdate.setGraphic(new ImageView(new Image("file:resources/images/icon_update_16.png")));
    	button_templateDelete.setTooltip(new Tooltip("Удалить элемент"));
    	button_templateDelete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	button_templateCopy.setTooltip(new Tooltip("Копировать элемент (внутренний буфер)"));
    	button_templateCopy.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_16.png")));
    	button_templateCut.setTooltip(new Tooltip("Вырезать элемент (внутренний буфер)"));
    	button_templateCut.setGraphic(new ImageView(new Image("file:resources/images/icon_cut_16.png")));
    	button_templatePaste.setTooltip(new Tooltip("Вставить элемент (внутренний буфер)"));
    	button_templatePaste.setGraphic(new ImageView(new Image("file:resources/images/icon_paste_16.png")));
    	button_treeRefresh.setTooltip(new Tooltip("Оновити гілку дерева"));
    	button_treeRefresh.setGraphic(new ImageView(new Image("file:resources/images/icon_refresh_16.png")));
    		
    	// TreeTableView
		treeViewCtrl.init();

    	// ContextMenu
    	menuitem_templateAdd.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
    	menuitem_templateUpdate.setGraphic(new ImageView(new Image("file:resources/images/icon_update_16.png")));
    	menuitem_templateDelete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	menuitem_templateCopy.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_16.png")));
    	menuitem_templateCut.setGraphic(new ImageView(new Image("file:resources/images/icon_cut_16.png")));
    	menuitem_templatePaste.setGraphic(new ImageView(new Image("file:resources/images/icon_paste_16.png")));
    	menuitem_treeRefresh.setGraphic(new ImageView(new Image("file:resources/images/icon_refresh_16.png")));
    }
    
    /**
     * отображает информацию выбранного шаблона, темы или файла
     */
    private void showTemplateDetails(TreeItem<TemplateSimpleItem> ti) {
    	params.setMsgToStatusBar(treeViewCtrl.getTemplatePath (ti, 1));
    }
    
    /**
     * Добавляем новый элемент
     */
    @FXML
    private void handleButtonAddItem() {
    	int typeNew;
    	
    	//======== check
    	if (treeTableView_templates.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел", 
    				"Выберите раздел, в который добавиться новый элемент.");
    		return;
    	}
    	
    	//======== select type for new item
    	typeNew = selectTypeItemForAdd ();
    	switch (typeNew) {
    	case TemplateSimpleItem.TYPE_ITEM_THEME :
    		editTheme (0);
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
    	case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
    	case TemplateSimpleItem.TYPE_ITEM_DIR_STYLE :
    	case TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE :
    		editDir (0);
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_FILE :
    	case TemplateSimpleItem.TYPE_ITEM_FILE_OPTIONAL :
    		editFile (0);
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_STYLE :
    		editStyle(0);
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_TEMPLATE :
    		editTemplate(0);
    		break;
    	}
    }
    
    /**
     * Изменяем текущий элемент в справочнике
     */
    @FXML
    private void handleButtonUpdateItem() {
    	int typeEdit;
    	
    	//======== check
    	if (treeTableView_templates.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран элемент", 
    				"Выберите элемент для редактирования.");
    		return;
    	}
    	
    	if ((treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getTypeItem() == TemplateSimpleItem.TYPE_ITEM_ROOT) || 
        	(treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_THEME) || 
        	((treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_FILE) && 
        	 (treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getId() == 0)) ||
        	((treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL) && 
             (treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getId() == 0)) ||
        	((treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_STYLE) && 
             (treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getId() == 0)) ||
        	((treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE) && 
             (treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getId() == 0))) {
        		ShowAppMsg.showAlert("WARNING", "Не редактируется", 
        				"\"" + treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getName() + "\"", 
        				"Данный элемент списка не редактируется.");
        		return;
        }
    	
    	//======== get type of current item for edit
    	typeEdit = treeTableView_templates.getSelectionModel().getSelectedItem().getValue().getTypeItem();
    	
    	switch (typeEdit) {
    	case TemplateSimpleItem.TYPE_ITEM_THEME :
    		editTheme (1);
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
    	case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
    	case TemplateSimpleItem.TYPE_ITEM_DIR_STYLE :
    	case TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE :
    		editDir (1);
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_FILE :
    	case TemplateSimpleItem.TYPE_ITEM_FILE_OPTIONAL :
    		editFile (1);
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_STYLE :
    		editStyle(1);
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_TEMPLATE :
    		editTemplate(1);
    		break;
    	}
    }
    
    /**
     * Удаляем текущий элемент
     */
    @FXML
    private void handleButtonDeleteItem() {
    	TreeItem<TemplateSimpleItem> selectedItem = treeTableView_templates.getSelectionModel().getSelectedItem();
    	
    	if (selectedItem == null) {
    		params.setMsgToStatusBar("Ничего не выбрано для удаления.");
    		return;
    	}
    	
    	TemplateSimpleItem tft = selectedItem.getValue();
    	
    	if ((tft.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_ROOT) || 
            (tft.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_THEME) || 
           ((tft.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_FILE) && 
            (tft.getId() == 0)) ||
           ((tft.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL) && 
            (tft.getId() == 0)) ||
           ((tft.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_STYLE) && 
            (tft.getId() == 0)) ||
           ((tft.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE) && 
            (tft.getId() == 0))) {
            ShowAppMsg.showAlert("WARNING", "Не удаляется", 
            				"\"" + tft.getName() + "\"", 
            				"Данный элемент списка нельзя удалять.");
            return;
        }
    	
    	TreeItem<TemplateSimpleItem> parentItem = selectedItem.getParent();
    	
    	//======== get type of current item for delete
    	switch (tft.getTypeItem()) {
    	case TemplateSimpleItem.TYPE_ITEM_THEME :
    		long countThemes = conn.db.templateThemeCount();
    		String msgTheme1;
    		String msgTheme2;
    		
    		if (countThemes > 1) {
    			msgTheme1 = "\nСтили удаляться не будут.";
    			msgTheme2 = "Удалить тему вместе со всеми ее файлами ?";
    		}
    		else {
    			msgTheme1 = "";
    			msgTheme2 = "Удалить тему вместе со всеми ее файлами ?";
    		}
    		
    		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление темы", 
					  "Удаление темы '"+ tft.getName() +"'." + msgTheme1, 
					  msgTheme2))
    			return;
    		
    		conn.db.templateFilesDelete(tft.getId());
    		conn.db.templateThemeDelete(tft.getId());
    		
    		if (countThemes == 1) {
    			if (ShowAppMsg.showQuestion("CONFIRMATION", "Удаление стилей", 
  					  "Удаление всех стилей", "Удалить все стили шаблонов ?")) {
    				conn.db.templateStylesDelete();
    			}
    		}
    		
    		if (parentItem != null) {     // текущая иконка не корневая
                parentItem.getChildren().remove(selectedItem);
        	}
    		
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
    	case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
    	case TemplateSimpleItem.TYPE_ITEM_FILE :
    	case TemplateSimpleItem.TYPE_ITEM_FILE_OPTIONAL :
    		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Вилучення файла", 
					  "Вилучення файла/директорії '"+ tft.getName() +"' з усією ієрархією", 
					  "Вилучити файл/директорію ?"))
  			return;
    		
    		conn.db.templateFileDelete(tft.getId());
    		parentItem.getChildren().remove(selectedItem);
    		
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_DIR_STYLE :
    	case TemplateSimpleItem.TYPE_ITEM_STYLE :
    		if (conn.db.templateStyleIsUsed(tft.getId())) {
    			ShowAppMsg.showAlert("WARNING", "Вилучення стилю", 
    					"Вилучення стилю '"+ tft.getName() +"'",
    					"Вилучення стилю неможливе, він або його підстиль використовується в інфо блоках.");
    			return;
    		}
    		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Вилучення стиля", 
					  "Вилучення стиля/директорії '"+ tft.getName() +"' з усією ієрархією та зв'язками з шаблонами", 
					  "Вилучити стиль ?"))
    			return;
    		
    		conn.db.templateStyleDelete(tft.getId());
    		treeViewCtrl.deleteStyleItemRecursive (treeViewCtrl.root, tft.getId());
    		
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE :
    	case TemplateSimpleItem.TYPE_ITEM_TEMPLATE :
    		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление шаблона", 
					  "Вилучення шаблона/директорії '"+ tft.getName() +"' з усією ієрархією та зв'язками зі стилями", 
					  "Удалить шаблон ?"))
    			return;
    	
        	conn.db.templateDelete(tft.getId());
    		parentItem.getChildren().remove(selectedItem);

    		break;
    	default : 
    		ShowAppMsg.showQuestion("CONFIRMATION", "Удаление шаблона/файла/темы/стилю", 
    				"Тип элемента '"+ tft.getName() +"' не определен.", "Не удаляем.");
    	}
    	
    	// выводим сообщение в статус бар
    	params.setMsgToStatusBar("Элемент '" + tft.getName() + "' удален.");
    }
    
    /**
     * Копирует текущий элемент в локальный буфер обмена
     */
    @FXML
    private void handleButtonItemCopy() {
    	saveToClipboard (TemplateClipboardInfo.TYPE_OPER_COPY);
    }
    
    /**
     * Вырезает текущий элемент с занесением в локальный буфер обмена
     */
    @FXML
    private void handleButtonItemCut() {
    	saveToClipboard (TemplateClipboardInfo.TYPE_OPER_CUT);
    }
    
	/**
     * Вставляет элемент указанный в буфере обмена
     */
    @FXML
    private void handleButtonItemPaste() {
    	TemplateClipboardInfo clip;
    	TreeItem<TemplateSimpleItem> selectedItem = treeTableView_templates.getSelectionModel().getSelectedItem();
    	TemplateSimpleItem item;
    	
    	String path =
    			FileUtil.getFullUserFileName(params, params.getConfig().getItemValue("directories", "PathDirCache")) +
    			"clipboard/";
    	
    	//-------- check 
    	if (selectedItem == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран элемент", 
    				"Выберите элемент.");
    		return;
    	}
    	
    	item = selectedItem.getValue();
    	
    	//--------- read info object
    	clip = TemplateClipboardInfo.unserialize(path, TemplateClipboardInfo.FILE_NAME_INFO);
    	
    	// check CUT for different connections
    	if ((clip.getTypeOper() == TemplateClipboardInfo.TYPE_OPER_CUT) && 
    		(clip.getDbCon_Id() != conn.Id)) {
    		ShowAppMsg.showAlert("WARNING", "Перенос об'екту", 
    				"Неможливо переносити об'єкт між різними БД, тільки копіювати.", "");
    		return;
    	}
    	
    	//-------- 
    	switch (clip.getTypeObj()) {
    	case TemplateClipboardInfo.TYPE_OBJ_FILE :
    		pasteFile(selectedItem, clip, path);
    		break;
    	case TemplateClipboardInfo.TYPE_OBJ_STYLE :
    		pasteStyle(selectedItem, clip, path);
    		break;
    	case TemplateClipboardInfo.TYPE_OBJ_TEMPLATE :
    		pasteTemplate(selectedItem, clip, path);
    		break;
    	}
    }
    
    /**
     * Оновлює поточну гілку дерева
     */
    @FXML
    private void handleButtonRefreshTree() {
    	StateList stateListTree = new StateList();
    	TreeItem<TemplateSimpleItem> mainItem = treeTableView_templates.getSelectionModel().getSelectedItem();
    	    	
    	//==== check
    	if (mainItem == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не вибраний елемент дерева", 
    				"Виберіть елемент який необхідно оновити.");
    		return;
    	}
    	
    	TemplateSimpleItem item = mainItem.getValue();
    	
    	if ((item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_THEME) ||
    		(item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_THEME)) {
    		ShowAppMsg.showAlert("WARNING", "Оновлення гілки дерева", "Тема не оновлюється", 
    				"Спустіться нижче по дереву.");
    		return;
    	}
    	
    	//==== save state
    	try {
    		stateListTree.add(
					"TreeItemSelected",
					Long.toString(mainItem.getValue().getId()),
					null);
		} catch (NullPointerException ex) {    }
    	
   		addTreeItemStateRecursive(stateListTree, mainItem);
    	
    	stateListTree.add(
				"TreeItemsDoExpandAndSelected",
				"",
				null);
    	
    	//==== удаляем ветку (без текущего итема, только дочерние)
    	mainItem.getChildren().clear();
    	
    	//==== обновляем текущий итем, если нужно
    	if (item.getId() != 0) {
    		switch (item.getTypeItem()) {
    		case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
    		case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
    		case TemplateSimpleItem.TYPE_ITEM_FILE :
    		case TemplateSimpleItem.TYPE_ITEM_FILE_OPTIONAL :
    			mainItem.setValue(params.getConCur().db.templateFileGetById(item.getId()));
    			break;
    		case TemplateSimpleItem.TYPE_ITEM_DIR_STYLE :
    		case TemplateSimpleItem.TYPE_ITEM_STYLE :
    			mainItem.setValue(params.getConCur().db.templateStyleGet(item.getId()));
    			break;
    		case TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE :
    		case TemplateSimpleItem.TYPE_ITEM_TEMPLATE :
    			mainItem.setValue(params.getConCur().db.templateGet(item.getId()));
    			break;
    		}
    	}
    	
    	//==== создаем ветку заново
    	switch (item.getTypeItem()) {
		case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
		case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
		case TemplateSimpleItem.TYPE_ITEM_FILE :
		case TemplateSimpleItem.TYPE_ITEM_FILE_OPTIONAL :
			treeViewCtrl.initTreeItemsFilesRecursive(mainItem);
			break;
		case TemplateSimpleItem.TYPE_ITEM_DIR_STYLE :
		case TemplateSimpleItem.TYPE_ITEM_STYLE :
			treeViewCtrl.initTreeItemsStylesRecursive(mainItem);
			break;
		case TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE :
		case TemplateSimpleItem.TYPE_ITEM_TEMPLATE :
			treeViewCtrl.initTreeItemsTemplatesRecursive(mainItem);
			break;
		}
    	
    	//==== восстанавливаем состояние ветки
    	ObservableList<Long> listItemsForExpand = FXCollections.observableArrayList();
		Long selectedItemId = 0L;
    	
    	for (StateItem si : stateListTree.list) {
			switch (si.getName()) {
				case "TreeItemSelected" :
					selectedItemId = new Long(si.getParams());
					break;
				case "TreeItemExpanded":
					listItemsForExpand.add(new Long(si.getParams()));
					break;
				case "TreeItemsDoExpandAndSelected" :
					restoreTreeItemStateRecursive(listItemsForExpand,mainItem);
					treeTableView_templates.sort();
					restoreTreeItemSelectedRecursive(selectedItemId,mainItem);
					treeTableView_templates.sort();
					break;
			}
    	}
    }
    
    /**
     * Вызывается при нажатии на кнопке "Закрыть" (X)
     */
    @FXML
    private void handleButtonExit() {
    	close();
    }
    
    private int selectTypeItemForAdd () {
    	int typeNew = -1;
    	
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/template/TemplateTypeSelect.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Выбор типа добавляемого элемента");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_templates/icon_CatalogTemplates_16.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(TemplateTypeSelect.class);
			dialogStage.setWidth(prefs.getDouble("stageTemplateTypeSelect_Width", 500));
			dialogStage.setHeight(prefs.getDouble("stageTemplateTypeSelect_Height", 600));
			dialogStage.setX(prefs.getDouble("stageTemplateTypeSelect_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageTemplateTypeSelect_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			TemplateTypeSelect controller = loader.getController();
			
			Params params = new Params (this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
			controller.setParams(params, treeTableView_templates.getSelectionModel().getSelectedItem().getValue());
	        
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        if (controller.isSelected) {
	        	typeNew = controller.returnTypeItem;
	        }
    	} catch (IOException e) {
            e.printStackTrace();
        }
    
    	return typeNew;
    }
    
    /**
     * Добавление редактирование темы
     * actionType : 0 - добавить, 1 - редактировать
     * @param actionType
     */
    private void editTheme (int actionType) {
    	TreeItem<TemplateSimpleItem> ti = null;
    
    	//-------- ищем корневой элемент с темами (куда вставлять)
    	switch (actionType) {
    	case 0 :     // add
    		// ищем корень тем
    		for (TreeItem<TemplateSimpleItem> i : treeViewCtrl.root.getChildren()) {
    			if (i.getValue().getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_THEME) {
    				ti = i;
    			}
    		}
    		break;
    	case 1 :     // edit
    		ti = treeTableView_templates.getSelectionModel().getSelectedItem();
    		break;
    	}
    	
    	//-------- open stage
    	try {
    		// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
    		FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(Main.class.getResource("view/business/template/TemplateThemeEdit.fxml"));
    		AnchorPane page = loader.load();
    		
    		// Создаём диалоговое окно Stage.
    		Stage dialogStage = new Stage();
			dialogStage.setTitle(((actionType == 0) ? "Добавление" : "Редактирование") +" темы");
			dialogStage.initModality(Modality.NONE);
			//dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_templates/icon_theme_16.png"));
  
			Preferences prefs = Preferences.userNodeForPackage(TemplateList_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble("stageThemeEdit_Width", 700));
			dialogStage.setHeight(prefs.getDouble("stageThemeEdit_Height", 600));
			dialogStage.setX(prefs.getDouble("stageThemeEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageThemeEdit_PosY", 0));
    		
			// Даём контроллеру доступ к главному прилодению.
			TemplateThemeEdit_Controller controller = loader.getController();
    					
    		Params params = new Params(this.params);
    		params.setParentObj(this);
    		params.setStageCur(dialogStage);
	        
	        controller.setParams(params, actionType, ti);
    			        
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
			//dialogStage.show();
    			        
	        //
	        ///////////treeTableView_sections.refresh();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Добавление редактирование директории файлов, стилей или шаблонов
     * actionType : 0 - добавить, 1 - редактировать
     * @param actionType
     */
    private void editDir (int actionType) {
    	TreeItem<TemplateSimpleItem> ti = null;
    	
    	//-------- 			ищем корневой элемент с темами (куда вставлять)
    	switch (actionType) {
    	case 0 :     // add
    		TemplateSimpleItem tiv = null;
    		
    		// ищем первую вышестоящую директорию
    		ti = treeTableView_templates.getSelectionModel().getSelectedItem();
    		tiv = ti.getValue();
    		
    		while ((tiv.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_DIR_FILE) &&
    			   (tiv.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL) &&
    			   (tiv.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_DIR_STYLE) &&	
    			   (tiv.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE)) {
    			ti = ti.getParent();
        		tiv = ti.getValue();
    		}
    		break;
    	case 1 :     // edit
    		ti = treeTableView_templates.getSelectionModel().getSelectedItem();
    		break;
    	}
    	
    	//-------- open stage
    	try {
    		// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
    		FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(Main.class.getResource("view/business/template/TemplateDirEdit.fxml"));
    		AnchorPane page = loader.load();
    	
    		String title = ((actionType == 0) ? "Добавление" : "Редактирование") +" директории ";
    		String iconFileName = null;
    		switch (ti.getValue().getTypeItem()) {
    		case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
    			title = title + "файлов";
    			iconFileName = "icon_section_file_16.png";
    			break;
    		case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
    			title = title + "необязательных файлов";
    			iconFileName = "icon_section_file_16.png";
    			break;
    		case TemplateSimpleItem.TYPE_ITEM_DIR_STYLE :
    			title = title + "стилей";
    			iconFileName = "icon_section_style_16.png";
    			break;
    		case TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE :
    			title = title + "шаблонов";
    			iconFileName = "icon_section_template_16.png";
    			break;
    		}
    		
    		// Создаём диалоговое окно Stage.
    		Stage dialogStage = new Stage();
			dialogStage.setTitle(title);
			dialogStage.initModality(Modality.NONE);
			//dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_templates/"+iconFileName));

			Preferences prefs = Preferences.userNodeForPackage(TemplateList_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble ("stageTemplateDirEdit_Width", 700));
			dialogStage.setHeight(prefs.getDouble("stageTemplateDirEdit_Height", 600));
			dialogStage.setX(prefs.getDouble     ("stageTemplateDirEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble     ("stageTemplateDirEdit_PosY", 0));

			// Даём контроллеру доступ к главному прилодению.
			TemplateDirEdit_Controller controller = loader.getController();

    		Params params = new Params(this.params);
    		params.setParentObj(this);
    		params.setStageCur(dialogStage);
	        
	        controller.setParams(params, actionType, ti);
    		
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
			//dialogStage.show();
    			        
	        //
	        ///////////treeTableView_sections.refresh();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Добавление/редактирование файла шаблонів
     * actionType : 0 - добавить, 1 - редактировать
     * @param actionType
     */
    private void editFile (int actionType) {
    	TreeItem<TemplateSimpleItem> ti = null;
    	
    	//-------- 			ищем корневой элемент с темами (куда вставлять)
    	switch (actionType) {
    	case 0 :     // add
    		TemplateSimpleItem tiv = null;
    		
    		// ищем первую вышестоящую директорию
    		ti = treeTableView_templates.getSelectionModel().getSelectedItem();
    		tiv = ti.getValue();
    		
    		while ((tiv.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_DIR_FILE) &&
    			   (tiv.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL)) {
    			ti = ti.getParent();
        		tiv = ti.getValue();
    		}
    		break;
    	case 1 :     // edit
    		ti = treeTableView_templates.getSelectionModel().getSelectedItem();
    		break;
    	}
    	
    	//-------- open stage
    	try {
    		// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
    		FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(Main.class.getResource("view/business/template/TemplateFileEdit.fxml"));
    		AnchorPane page = loader.load();
    	
    		String title = ((actionType == 0) ? "Додавання" : "Редагування") +" файла для шаблонів";
    		String iconFileName = "icon_file_text_16.png";
    		
    		// Создаём диалоговое окно Stage.
    		Stage dialogStage = new Stage();
			dialogStage.setTitle(title);
			dialogStage.initModality(Modality.NONE);
			//dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_templates/"+iconFileName));

			Preferences prefs = Preferences.userNodeForPackage(TemplateList_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble ("stageTemplateFileEdit_Width", 700));
			dialogStage.setHeight(prefs.getDouble("stageTemplateFileEdit_Height", 600));
			dialogStage.setX(prefs.getDouble     ("stageTemplateFileEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble     ("stageTemplateFileEdit_PosY", 0));

			// Даём контроллеру доступ к главному прилодению.
			TemplateFileEdit_Controller controller = loader.getController();

    		Params params = new Params(this.params);
    		params.setParentObj(this);
    		params.setStageCur(dialogStage);
	        
	        controller.setParams(params, actionType, ti);
    		
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
			//dialogStage.show();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Добавление/редактирование стиля шаблонів
     * actionType : 0 - добавить, 1 - редактировать
     * @param actionType
     */
    private void editStyle (int actionType) {
    	TreeItem<TemplateSimpleItem> ti = null;
    	
    	//-------- шукаємо директорію для вставки, при редагуванні берем поточний елемент
    	switch (actionType) {
    	case 0 :     // add
    		TemplateSimpleItem tiv = null;
    		
    		// ищем первую вышестоящую директорию
    		ti = treeTableView_templates.getSelectionModel().getSelectedItem();
    		tiv = ti.getValue();
    		
    		while (tiv.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_DIR_STYLE) {
    			ti = ti.getParent();
        		tiv = ti.getValue();
    		}
    		break;
    	case 1 :     // edit
    		ti = treeTableView_templates.getSelectionModel().getSelectedItem();
    		break;
    	}
    	
    	//-------- open stage
    	try {
    		// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
    		FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(Main.class.getResource("view/business/template/TemplateStyleEdit.fxml"));
    		AnchorPane page = loader.load();
    	
    		String title = ((actionType == 0) ? "Додавання" : "Редагування") +" стилю шаблонів";
    		String iconFileName = "icon_style_16.png";
    		
    		// Создаём диалоговое окно Stage.
    		Stage dialogStage = new Stage();
			dialogStage.setTitle(title);
			dialogStage.initModality(Modality.NONE);
			//dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_templates/"+iconFileName));

			Preferences prefs = Preferences.userNodeForPackage(TemplateList_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble ("stageTemplateStyleEdit_Width", 700));
			dialogStage.setHeight(prefs.getDouble("stageTemplateStyleEdit_Height", 600));
			dialogStage.setX(prefs.getDouble     ("stageTemplateStyleEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble     ("stageTemplateStyleEdit_PosY", 0));

			// Даём контроллеру доступ к главному прилодению.
			TemplateStyleEdit_Controller controller = loader.getController();

    		Params params = new Params(this.params);
    		params.setParentObj(this);
    		params.setStageCur(dialogStage);
	        
	        controller.setParams(params, actionType, ti);
    		
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
			//dialogStage.show();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Добавление/редактирование шаблона
     * actionType : 0 - добавить, 1 - редактировать
     * @param actionType
     */
    private void editTemplate (int actionType) {
    	TreeItem<TemplateSimpleItem> ti = null;
    	
    	//-------- шукаємо директорію для вставки, при редагуванні берем поточний елемент
    	switch (actionType) {
    	case 0 :     // add
    		TemplateSimpleItem tiv = null;
    		
    		// ищем первую вышестоящую директорию
    		ti = treeTableView_templates.getSelectionModel().getSelectedItem();
    		tiv = ti.getValue();
    		
    		while (tiv.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE) {
    			ti = ti.getParent();
        		tiv = ti.getValue();
    		}
    		break;
    	case 1 :     // edit
    		ti = treeTableView_templates.getSelectionModel().getSelectedItem();
    		break;
    	}
    	
    	//-------- open stage
    	try {
    		// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
    		FXMLLoader loader = new FXMLLoader();
    		
    		loader.setLocation(Main.class.getResource("view/business/template/TemplateEdit.fxml"));
    		AnchorPane page = loader.load();
    		
    		String title = ((actionType == 0) ? "Додавання" : "Редагування") +" шаблона";
    		String iconFileName = "icon_template_16.png";
    		
    		// Создаём диалоговое окно Stage.
    		Stage dialogStage = new Stage();
			dialogStage.setTitle(title);
			dialogStage.initModality(Modality.NONE);
			//dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_templates/"+iconFileName));
    	
			Preferences prefs = Preferences.userNodeForPackage(TemplateList_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble ("stageTemplateEdit_Width", 700));
			dialogStage.setHeight(prefs.getDouble("stageTemplateEdit_Height", 600));
			dialogStage.setX(prefs.getDouble     ("stageTemplateEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble     ("stageTemplateEdit_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			TemplateEdit_Controller controller = loader.getController();

			Params params = new Params(this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
				        
			controller.setParams(params, actionType, ti);
    		
    		// Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
			//dialogStage.show();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Зберігає поточний обьект у буфері обміну
     * @param typeOper
     */
    private void saveToClipboard(int typeOper) {
    	int typeObj = 0;
    	TemplateClipboardInfo clip;
    	TreeItem<TemplateSimpleItem> selectedItem = treeTableView_templates.getSelectionModel().getSelectedItem();
    	TemplateSimpleItem item;
    	
    	String path =
    			FileUtil.getFullUserFileName(params, params.getConfig().getItemValue("directories", "PathDirCache")) +
    			"clipboard/";
    	File fDocDir = new File(path);
    	
    	//-------- check 
    	if (selectedItem == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран элемент", 
    				"Выберите элемент.");
    		return;
    	}
    	
    	item = selectedItem.getValue();
    	
    	if ((item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_ROOT) || 
            (item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_THEME) || 
            (item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_THEME) ||
            ((item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_FILE) && (item.getId() == 0)) ||
            ((item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL) && (item.getId() == 0)) ||
            ((item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_STYLE) && (item.getId() == 0)) ||
            ((item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE) && (item.getId() == 0))) {
            		ShowAppMsg.showAlert("WARNING", "Не копіюється", 
            				"\"" + item.getName() + "\"", 
            				"Вибраний тип елементів не копіюється в локальний буфер обміну.");
            		return;
        }
    	
    	//-------- init and save
    	// create dir
    	if (! fDocDir.exists()) {
			if (! fDocDir.mkdirs()) {
				ShowAppMsg.showAlert("WARNING", "Створення директорії", 
        				"\"" + path + "\"", 
        				"Неможливо створити директорію.");
        		return;
			}
    	}
    	
    	// set typeObj
    	switch (item.getTypeItem()) {
    	case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
    	case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
    	case TemplateSimpleItem.TYPE_ITEM_FILE :
    	case TemplateSimpleItem.TYPE_ITEM_FILE_OPTIONAL :
    		typeObj = TemplateClipboardInfo.TYPE_OBJ_FILE;
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_DIR_STYLE :
    	case TemplateSimpleItem.TYPE_ITEM_STYLE :
    		typeObj = TemplateClipboardInfo.TYPE_OBJ_STYLE;
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE :
    	case TemplateSimpleItem.TYPE_ITEM_TEMPLATE :
    		typeObj = TemplateClipboardInfo.TYPE_OBJ_TEMPLATE;
    		break;
    	}
    	
    	// init info object and write
    	clip = new TemplateClipboardInfo (
				typeOper, 
				typeObj, 
				params.getConCur().Id,
				params.getConCur().param.getConnName());
    	clip.serialize(path, TemplateClipboardInfo.FILE_NAME_INFO);
    	
    	// write data object
    	switch (item.getTypeItem()) {
    	case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
    	case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
    	case TemplateSimpleItem.TYPE_ITEM_FILE :
    	case TemplateSimpleItem.TYPE_ITEM_FILE_OPTIONAL :
    		TemplateFileItem dataItemFile = conn.db.templateFileGetById(item.getId());
    		dataItemFile.serialize(path, TemplateClipboardInfo.FILE_NAME);
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_DIR_STYLE :
    	case TemplateSimpleItem.TYPE_ITEM_STYLE :
    		TemplateStyleItem dataItemStyle = conn.db.templateStyleGet(item.getId());
    		dataItemStyle.serialize(path, TemplateClipboardInfo.FILE_NAME);
    		break;
    	case TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE :
    	case TemplateSimpleItem.TYPE_ITEM_TEMPLATE :
    		TemplateItem dataItem = conn.db.templateGet(item.getId());
    		dataItem.serialize(path, TemplateClipboardInfo.FILE_NAME);
    		break;
    	}
	}
    
    /**
     * Вставляємо з буфера файл (директорію)
     */
    private void pasteFile (TreeItem<TemplateSimpleItem> selectedItem, TemplateClipboardInfo clip, String path) {
    	TemplateSimpleItem item = selectedItem.getValue();
    	
    	// check type 
		if ((item.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_DIR_FILE) && 
			(item.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL) &&
			(item.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_FILE) &&
			(item.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_FILE_OPTIONAL)
		   ) {
			ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", "Несумісність типів.", "");
			return;
		}
		if ((item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_FILE) ||
    		(item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_FILE_OPTIONAL)
    	   ) {
			selectedItem = selectedItem.getParent();
    		item = selectedItem.getValue(); 
    	}
		
		// read data object
		TemplateFileItem fi = TemplateFileItem.unserialize(path, TemplateClipboardInfo.FILE_NAME);
		
		List<TemplateSimpleItem> fList = conn.db.templateFileListByParent (item);
		for (TemplateSimpleItem i : fList) {
			if (fi.getFileName().equalsIgnoreCase(i.getName())) {
				ShowAppMsg.showAlert("WARNING", "Вставка файла з буфера обміну", 
        				"\"" + fi.getFileName() + "\"", 
        				"Файл чи директорія з таким іменем вже присутній.");
				return;
			}
		}
		
		// copy or cut object
		switch (clip.getTypeOper()) {
		case TemplateClipboardInfo.TYPE_OPER_COPY :
			fi.setId(conn.db.templateFileNextId());
			fi.setParentId(item.getId());
			fi.setThemeId(item.getThemeId());
			if ((fi.getType() < 10) && (item.getSubtypeItem() == 11)) {
				fi.setType(fi.getType() + 10);
			}
			if ((fi.getType() >= 10) && (item.getSubtypeItem() == 1)) {
				fi.setType(fi.getType() - 10);
			}
			
			//
			if ((fi.getType() == 1) || (fi.getType() == 11)) {    // directory
				conn.db.templateFileAdd(fi);
			} else {
				conn.db.templateFileAdd(fi, path + fi.getFileName());
			}
			
			TreeItem<TemplateSimpleItem> subItemFC = new TreeItem<>(fi);
			selectedItem.getChildren().add(subItemFC);
			selectedItem.setExpanded(true);
			
			//--- при необходимости кешируем файл на диске (для обов'язкових файлів)
			if (fi.getType() < 10) {
				FileCache fileCacheC = new FileCache (params, fi.getThemeId());
				fileCacheC.createTemplateFile(fi);
			}
			
			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Файл (чи директорія) '" + fi.getFileName() + "' скопійований з буфера обміну.");
			
			break;
		case TemplateClipboardInfo.TYPE_OPER_CUT :
			boolean isThemeChange = (fi.getThemeId() != item.getThemeId()) ? true : false;
			int typeChanging = 0;	// необхідний коли гілка дерева переноситься між обов'язковим та не обов'язковим роздалами
			
			fi.setParentId(item.getId());
			fi.setThemeId(item.getThemeId());
			
			if ((fi.getType() < 10) && (item.getSubtypeItem() == 11)) {
				typeChanging = 10;
			}
			if ((fi.getType() >= 10) && (item.getSubtypeItem() == 1)) {
				typeChanging = -10;
			}
			fi.setType(fi.getType() + typeChanging);
			
			// 
			if ((fi.getType() == 1) || (fi.getType() == 11)) {    // directory
				conn.db.templateFileUpdate(fi);
			} else {
				conn.db.templateFileUpdate(fi, "");
			}
		
			TreeItem<TemplateSimpleItem> subItemF = new TreeItem<>(fi);
			selectedItem.getChildren().add(subItemF);
			selectedItem.setExpanded(true);
			
			//--- при необходимости кешируем файл на диске (для обов'язкових файлів)
			if (fi.getType() < 10) {
				FileCache fileCache = new FileCache (params, fi.getThemeId());
				fileCache.createTemplateFile(fi);
			}
			
			// будуємо гілку піддерева-контрола, міняємо у піддереві Тему та Тип файлів (обов'язкові чи необов'язкові)
			initTreeItemsFilesRecursiveForMove (subItemF, isThemeChange, typeChanging);
			
			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Файл (чи директорія) '" + fi.getFileName() + "' перенесений.");
    	
			break;
		}
    }
    
    /**
     * Вставляємо з буфера стиль
     */
    private void pasteStyle (TreeItem<TemplateSimpleItem> selectedItem, TemplateClipboardInfo clip, String path) {
    	TemplateSimpleItem item = selectedItem.getValue();
    	
    	// check type
		if ((item.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_DIR_STYLE) && 
    		(item.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_STYLE)
   		   ) {
   			ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", "Несумісність типів.", "");
   			return;
   		}
		if (item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_STYLE) {
        	selectedItem = selectedItem.getParent();
    		item = selectedItem.getValue(); 
        }
		
		// read data object
		TemplateStyleItem si = TemplateStyleItem.unserialize(path, TemplateClipboardInfo.FILE_NAME);
    	
		// copy or cut object
		switch (clip.getTypeOper()) {
		case TemplateClipboardInfo.TYPE_OPER_COPY :
			si.setId(conn.db.templateStyleNextId());
			si.setParentId(item.getId());
			if ((si.getType() < 10) && (item.getSubtypeItem() == 11)) {
				si.setType(si.getType() + 10);
			}
			if ((si.getType() >= 10) && (item.getSubtypeItem() == 1)) {
				si.setType(si.getType() - 10);
			}
			si.setInfoTypeId(item.getFlag());
			if (conn.db.templateStyleTagIsPresent (si.getTag())) {
				si.setTag("");
			}
			
			//
			conn.db.templateStyleAdd(si);
			
			// добавляем в контрол-дерево. Добавляем во все темы.
			treeViewCtrl.addStyleItemRecursive(treeViewCtrl.root, si);
			// раскрываем текущий элемент
			selectedItem.setExpanded(true);

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Стиль (чи директорія) '" + si.getName() + "' скопійований з буфера обміну.");
			
			break;
		case TemplateClipboardInfo.TYPE_OPER_CUT :
			si.setParentId(item.getId());
			if ((si.getType() < 10) && (item.getSubtypeItem() == 11)) {
				si.setType(si.getType() + 10);
			}
			if ((si.getType() >= 10) && (item.getSubtypeItem() == 1)) {
				si.setType(si.getType() - 10);
			}
			si.setInfoTypeId(item.getFlag());

			//
			conn.db.templateStyleUpdate(si);
			
			// добавляем в контрол-дерево. Добавляем во все темы.
			treeViewCtrl.addStyleItemRecursive(treeViewCtrl.root, si);
			// раскрываем текущий элемент
			selectedItem.setExpanded(true);

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Стиль (чи директорія) '" + si.getName() + "' перенесені.");
			
			break;
		}
    }
    
    /**
     * Вставляємо з буфера шаблон
     */
    private void pasteTemplate (TreeItem<TemplateSimpleItem> selectedItem, TemplateClipboardInfo clip, String path) {
    	TemplateSimpleItem item = selectedItem.getValue();
    	
    	// check type
		if ((item.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE) && 
        	(item.getTypeItem() != TemplateSimpleItem.TYPE_ITEM_TEMPLATE)
       	   ) {
       		ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", "Несумісність типів.", "");
       		return;
       	}
		if (item.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_TEMPLATE) {
			selectedItem = selectedItem.getParent();
    		item = selectedItem.getValue(); 
        }
		
		// read data object
		TemplateItem ti = TemplateItem.unserialize(path, TemplateClipboardInfo.FILE_NAME);
		
		// copy or cut object
		switch (clip.getTypeOper()) {
		case TemplateClipboardInfo.TYPE_OPER_COPY :
			ti.setId(conn.db.templateNextId());
			ti.setParentId(item.getId());
			if ((ti.getType() < 10) && (item.getSubtypeItem() == 11)) {
				ti.setType(ti.getType() + 10);
			}
			if ((ti.getType() >= 10) && (item.getSubtypeItem() == 1)) {
				ti.setType(ti.getType() - 10);
			}

			//
			conn.db.templateAdd(ti);
			
			// добавляем в контрол-дерево.
			TreeItem<TemplateSimpleItem> subItemS = new TreeItem<>(ti);
			selectedItem.getChildren().add(subItemS);
			selectedItem.setExpanded(true);

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Шаблон '" + ti.getName() + "' скопійований з буфера обміну.");
	
			break;
		case TemplateClipboardInfo.TYPE_OPER_CUT :
			ti.setParentId(item.getId());
			if ((ti.getType() < 10) && (item.getSubtypeItem() == 11)) {
				ti.setType(ti.getType() + 10);
			}
			if ((ti.getType() >= 10) && (item.getSubtypeItem() == 1)) {
				ti.setType(ti.getType() - 10);
			}

			//
			conn.db.templateUpdate(ti);
			
			// добавляем в контрол-дерево.
			TreeItem<TemplateSimpleItem> subItemSM = new TreeItem<>(ti);
			selectedItem.getChildren().add(subItemSM);
			selectedItem.setExpanded(true);

			// выводим сообщение в статус бар
			params.setMsgToStatusBar("Шаблон '" + ti.getName() + "' перенесений.");
					
			break;
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
		return AppItem_Interface.ELEMENT_TEMPLATE_LIST;
	}
	
	/**
	 * Повертає параметри обьекта інтерфейса
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public Params getParams() {
		return params;
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
	 * Реалізуємо метод інтерфейсу AppItem_Interface.
     * Зачиняємо таб з цим елементом інтерфейсу
     */
    public void close () {
    	if (! ShowAppMsg.showQuestion (
				"CONFIRMATION", 
				"Питання", 
				"", 
				"Закрити вкладку ?")) {
    		return;
    	}
    	
    	//-------- sort
    	if (treeTableView_templates.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
			TreeTableColumn currentSortColumn = treeTableView_templates.getSortOrder().get(0);
			prefs.put("stageTemplatesList_sortColumnId",currentSortColumn.getId());
			prefs.put("stageTemplatesList_sortType",currentSortColumn.getSortType().toString());
		} else {
			prefs.remove("stageTemplatesList_sortColumnId");
			prefs.remove("stageTemplatesList_sortType");
		}

    	//
		objContainer.closeContainer(getOID());
    }
	
	/**
	 * будуємо гілку піддерева-контрола, міняємо у піддереві Тему та Тип файлів (обов'язкові чи необов'язкові)
	 */
	private void initTreeItemsFilesRecursiveForMove (
			TreeItem<TemplateSimpleItem> rootItem, boolean isThemeChange, int typeChanging) {
		TemplateSimpleItem f = rootItem.getValue();
		List<TemplateSimpleItem> tList;
		TemplateFileItem fi;

		if (f != null) {
			tList = conn.db.templateFileListByParent (f);

			for (TemplateSimpleItem i : tList) {
				fi = conn.db.templateFileGetById(i.getId());
				
				if ((typeChanging != 0) || isThemeChange) {
					if (typeChanging != 0) {
						fi.setType(fi.getType() + typeChanging);
					}
					if (isThemeChange) {
						fi.setThemeId(f.getThemeId());
					}
				
					if ((fi.getType() == 1) || (fi.getType() == 11)) {    // directory
						conn.db.templateFileUpdate(fi);
					} else {
						conn.db.templateFileUpdate(fi, "");
					}
					
					if (fi.getType() == 0) {
						//--- при необходимости кешируем файл на диске
		    			FileCache fileCache = new FileCache (params, fi.getThemeId());
		    			fileCache.createTemplateFile(fi);
					}
				}
				
				TreeItem<TemplateSimpleItem> subItem = new TreeItem<>(fi);
				rootItem.getChildren().add(subItem);
				
				initTreeItemsFilesRecursiveForMove (subItem, isThemeChange, typeChanging);
			}
		}
	}
	
	/**
	 * рекурсивное сохранение развернутых разделов из дерева разделов
	 */
	private void addTreeItemStateRecursive(StateList stateList, TreeItem<TemplateSimpleItem> ti) {
	
		//-------- проверяем и записываем развернутость итема
		if (ti.isExpanded()) {
			stateList.add(
					"TreeItemExpanded",
					Long.toString(ti.getValue().getId()),
					null);
		}
		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<TemplateSimpleItem> i : ti.getChildren()) {
			addTreeItemStateRecursive(stateList, i);
		}
	}

	/**
	 * рекурсивное восстановление состояния елементів дерева
	 */
	private void restoreTreeItemStateRecursive(
			ObservableList<Long> listItemsForExpand,
			TreeItem<TemplateSimpleItem> ti) {

		//-------- проверяем и разворачиваем текущий итем
		for (Long i : listItemsForExpand) {
			if (i == ti.getValue().getId()) {
				ti.setExpanded(true);
				break;
			}
		}
		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<TemplateSimpleItem> i : ti.getChildren()) {
			restoreTreeItemStateRecursive(listItemsForExpand, i);
		}
	}
	
	/**
	 * рекурсивно ищем активный елемент в дереві та вибираємо його
	 */
	private void restoreTreeItemSelectedRecursive(
			Long selectedItemId,
			TreeItem<TemplateSimpleItem> ti) {

		//---------- проверяем и выбираем текущий итем
		if (selectedItemId == ti.getValue().getId())
			treeTableView_templates.getSelectionModel().select(ti);

		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<TemplateSimpleItem> i : ti.getChildren()) {
			restoreTreeItemSelectedRecursive(selectedItemId, i);
		}
	}

	/**
	 * Класс обработки дерева шаблонов
	 */
	class TreeView_Controller {
		//
		public TreeItem<TemplateSimpleItem> root;

		/**
		 * constructor
		 */
		TreeView_Controller () {
			root = null;
		}

		/**
		 * инициализируем дерево, основной метод инициализации
		 */
		void init () {
			initCellValueFactory ();
			initColumnWidth ();

			root = new TreeItem<>(new TemplateSimpleItem(
					0, "Темы и Шаблоны", "это корень, он не редактируется", 0, 
					TemplateSimpleItem.TYPE_ITEM_ROOT, 0, 0));
			root.setExpanded(true);
			treeTableView_templates.setShowRoot(false);
			treeTableView_templates.setRoot(root);

			initTreeItems(root);
			initCellFactory();
///			initRowFactory();

			// Слушаем изменения выбора, и при изменении отображаем информацию.
			treeTableView_templates.getSelectionModel().selectedItemProperty().addListener(
					(observable, oldValue, newValue) -> showTemplateDetails(newValue));

			initSortColumn();
		}

		/**
		 * initCellValueFactory ()
		 */
		private void initCellValueFactory () {
			treeTableColumn_id.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getId()))
			);
			treeTableColumn_name.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getName())
			);
			treeTableColumn_descr.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
			);
			treeTableColumn_dateCreated.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateCreated()))
			);
			treeTableColumn_dateModified.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateModified()))
			);
			treeTableColumn_userCreated.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getUserCreated())
			);
			treeTableColumn_userModified.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getUserModified())
			);
		}

		/**
		 * initColumnWidth ()
		 */
		private void initColumnWidth () {
			// set/get Pref Width
			treeTableColumn_id.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_id__PrefWidth", 50));

			treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_id__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_name.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_name__PrefWidth", 250));

			treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_name__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_descr.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_descr__PrefWidth", 250));

			treeTableColumn_descr.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_descr__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_dateCreated.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_dateCreated__PrefWidth", 150));

			treeTableColumn_dateCreated.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_dateCreated__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_dateModified.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_dateModified__PrefWidth", 150));

			treeTableColumn_dateModified.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_dateModified__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_userCreated.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_userCreated__PrefWidth", 150));

			treeTableColumn_userCreated.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_userCreated__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_userModified.setPrefWidth(prefs.getDouble("CatalogTemplates__treeTableColumn_userModified__PrefWidth", 150));

			treeTableColumn_userModified.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("CatalogTemplates__treeTableColumn_userModified__PrefWidth", t1.doubleValue());
				}
			});
		}

		/**
		 * Инициализация TreeTableView.
		 */
		private void initTreeItems (TreeItem<TemplateSimpleItem> ti) {
			TemplateSimpleItem f = ti.getValue();

			if (f == null)  return;
			
			TreeItem<TemplateSimpleItem> sectionThemeItem = new TreeItem<>(new TemplateSimpleItem(
					0, "Темы", "", 0, TemplateSimpleItem.TYPE_ITEM_DIR_THEME, 0, 0));
			ti.getChildren().add(sectionThemeItem);
			
			List<TemplateThemeItem> themesList = conn.db.templateThemesList();
			
			for (TemplateThemeItem i : themesList) {       // cycle for themes
				TreeItem<TemplateSimpleItem> themeItem = new TreeItem<>(i);
				themeItem.getValue().setThemeId(i.getId());
				themeItem.getValue().setTypeItem(TemplateSimpleItem.TYPE_ITEM_THEME);
				sectionThemeItem.getChildren().add(themeItem);
				
				//======== files
				TreeItem<TemplateSimpleItem> fileDirReqItem = new TreeItem<>(new TemplateSimpleItem(
						0, "Файлы обязательные", "", i.getId(), TemplateSimpleItem.TYPE_ITEM_DIR_FILE, 1, 0));
				themeItem.getChildren().add(fileDirReqItem);
				
				initTreeItemsFilesRecursive (fileDirReqItem);
				
				TreeItem<TemplateSimpleItem> fileDirOptItem = new TreeItem<>(new TemplateSimpleItem(
						0, "Файлы не обязательные", "", i.getId(), TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL, 11, 0));
				themeItem.getChildren().add(fileDirOptItem);
				
				initTreeItemsFilesRecursive (fileDirOptItem);
				
				//======== styles
				TreeItem<TemplateSimpleItem> styleDirResItem = new TreeItem<>(new TemplateSimpleItem(
						0, "Стили зарезервированные", "", i.getId(), TemplateSimpleItem.TYPE_ITEM_DIR_STYLE, 11, 0));
				themeItem.getChildren().add(styleDirResItem);
				
				initTreeItemsStylesRecursive (styleDirResItem);
				
				List<InfoTypeItem> listInfoTypes = conn.db.infoTypeList ();
				for (InfoTypeItem iInfo : listInfoTypes) {
					TreeItem<TemplateSimpleItem> styleInfoTypeItem = new TreeItem<>(new TemplateSimpleItem(
							0, "Стиль \""+iInfo.getName()+"\"", iInfo.getDescr(), i.getId(), 
							TemplateSimpleItem.TYPE_ITEM_DIR_STYLE, 1, 
							iInfo.getId()));
					themeItem.getChildren().add(styleInfoTypeItem);
					
					initTreeItemsStylesRecursive (styleInfoTypeItem);
				}
			}
			
			//======== Templates
			TreeItem<TemplateSimpleItem> sectionTemplateItem = new TreeItem<>(new TemplateSimpleItem(
					0, "Шаблоны", "", 0, TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE, 0, 0));
			ti.getChildren().add(sectionTemplateItem);
			
			initTreeItemsTemplatesRecursive (sectionTemplateItem);
		}
		
		/**
		 * Инициализирует подветки TreeTableView, содержащие файлы
		 */
		private void initTreeItemsFilesRecursive (TreeItem<TemplateSimpleItem> ti) {
			TemplateSimpleItem f = ti.getValue();
			List<TemplateSimpleItem> tList;

			if (f != null) {
				tList = conn.db.templateFileListByParent (f);

				for (TemplateSimpleItem i : tList) {
					TreeItem<TemplateSimpleItem> subItem = new TreeItem<>(i);
					ti.getChildren().add(subItem);
					initTreeItemsFilesRecursive (subItem);
				}
			}
		}
		
		/**
		 * Инициализирует подветки TreeTableView, содержащие стили
		 */
		private void initTreeItemsStylesRecursive (TreeItem<TemplateSimpleItem> ti) {
			TemplateSimpleItem f = ti.getValue();
			List<TemplateSimpleItem> tList;

			if (f != null) {
				tList = conn.db.templateStyleListByParent (f);

				for (TemplateSimpleItem i : tList) {
					TreeItem<TemplateSimpleItem> subItem = new TreeItem<>(i);
					ti.getChildren().add(subItem);
					initTreeItemsStylesRecursive (subItem);
				}
			}
		}
		
		/**
		 * Инициализирует подветки TreeTableView, содержащие шаблоны
		 */
		private void initTreeItemsTemplatesRecursive (TreeItem<TemplateSimpleItem> ti) {
			TemplateSimpleItem f = ti.getValue();
			List<TemplateSimpleItem> tList;

			if (f != null) {
				tList = conn.db.templateListByParent (f.getId());

				for (TemplateSimpleItem i : tList) {
					TreeItem<TemplateSimpleItem> subItem = new TreeItem<>(i);
					ti.getChildren().add(subItem);
					initTreeItemsTemplatesRecursive (subItem);
				}
			}
		}
		
		/**
		 * CellFactory - показ иконок
		 */
		public void initCellFactory () {
			treeTableColumn_name.setCellFactory(ttc -> new TreeTableCell<TemplateSimpleItem, String>() {
				private TemplateSimpleItem row;
				private ImageView graphic;
				private ImageView graphic_default;
				private HBox hBox;
				private boolean isDefault;

				@Override
				protected void updateItem(String item, boolean empty) {    // display graphic
					isDefault = false;

					try {
						row = getTreeTableRow().getItem();
						switch (row.getTypeItem()) {
							case TemplateSimpleItem.TYPE_ITEM_ROOT :                      // 0 - корень
								graphic = null;
								break;
							case TemplateSimpleItem.TYPE_ITEM_DIR_THEME :
							case TemplateSimpleItem.TYPE_ITEM_THEME : 
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_theme_16.png"));
								break;
							case TemplateSimpleItem.TYPE_ITEM_DIR_FILE : 
							case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_section_file_16.png"));
								break;
							case TemplateSimpleItem.TYPE_ITEM_FILE : 
							case TemplateSimpleItem.TYPE_ITEM_FILE_OPTIONAL :
								//switch ((int)row.getSubtypeItem()) {
								switch ((int)row.getFlag()) {
								case TemplateSimpleItem.SUBTYPE_FILE_TEXT :                  // 1 - текстовый
									graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_file_text_16.png"));
									break;
								case TemplateSimpleItem.SUBTYPE_FILE_IMAGE :                  // 2 - картинка
									graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_file_picture_16.png"));
									break;
								case TemplateSimpleItem.SUBTYPE_FILE_BINARY :                  // 3 - бинарный
									graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_file_binary_16.png"));
									break;
								}
								break;
							case TemplateSimpleItem.TYPE_ITEM_DIR_STYLE :
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_section_style_16.png"));
								break;
							case TemplateSimpleItem.TYPE_ITEM_STYLE :
								if (row.getFlag2() == 0) {
									graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_style_empty_16.png"));
								} else {
									graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_style_16.png"));
								}
								if (conn.db.templateStyleIsDefault (row.getThemeId(), row.getId())) {
									graphic_default = new ImageView(new Image("file:resources/images/icon_default_item_16.png"));
									hBox = new HBox();
									hBox.getChildren().addAll(graphic, graphic_default);
									isDefault = true;
								}
								break;
							case TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE :
								graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_section_template_16.png"));
								break;
							case TemplateSimpleItem.TYPE_ITEM_TEMPLATE :
								if (row.getFlag() == 0) {
									graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_template_16.png"));
								} else {
									graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_template_link_16.png"));
								}
								break;
						}
					} catch (NullPointerException e) {
						//e.printStackTrace();
						graphic = null;
					}

					super.updateItem(item, empty);
					setText(empty ? null : item);
					if (isDefault) setGraphic(empty ? null : hBox);
					else           setGraphic(empty ? null : graphic);
				}
			});
		}
		
		/**
		 * восстанавливаем сортировку таблицы по столбцу
		 */
		public void initSortColumn () {
			String sortColumnId = prefs.get("stageTemplatesList_sortColumnId","");

			if (! sortColumnId.equals("")) {
				for (TreeTableColumn column : treeTableView_templates.getColumns()) {
					if (column.getId().equals(sortColumnId)) {
						String sortType = prefs.get("stageTemplatesList_sortType","ASCENDING");

						treeTableView_templates.setSortMode(TreeSortMode.ALL_DESCENDANTS);
						column.setSortable(true); // This performs a sort
						treeTableView_templates.getSortOrder().add(column);
						if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
						else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
						treeTableView_templates.sort();
					}
				}
			}
		}

		/*
		 * Возвращает строку иерархического пути шаблона
		 *
		 * ti - текущий шаблон
		 * level - 0 - показывает без текущего шаблона ; 1 - с текущим шаблоном
		 */
		public String getTemplatePath (TreeItem<TemplateSimpleItem> ti, int level) {
			String msg = null;
			boolean isFirst = true;

			if (ti != null) {
				TemplateSimpleItem f = ti.getValue();

				// show template path in StatusBar
				if (level == 1) msg = f.getName();
				//if (f.getId() > 0) {
				if (f.getTypeItem() != 0) {
					TreeItem<TemplateSimpleItem> curTI = ti.getParent();
					TemplateSimpleItem curTemplate = curTI.getValue();

					while (curTemplate.getTypeItem() != 0) {
						if ((level == 1) || (! isFirst)) {
							msg = curTemplate.getName() + " / " + msg;
						} else {
							msg = curTemplate.getName();
							isFirst = false;
						}

						if (curTemplate.getTypeItem() == 0)   break;
						else {
							curTI = curTI.getParent();
							curTemplate = curTI.getValue();
						}
					}
				}
			}
			return msg;
		}
		
		/**
	     * Добавляет новый стиль в дерево-контрол во все темы
	     */
	    void addStyleItemRecursive (TreeItem<TemplateSimpleItem> parentTI, TemplateStyleItem new_sip) {
	    	// получаем список дочерних элементов
	    	List<TreeItem<TemplateSimpleItem>> oList = parentTI.getChildren(); 

	    	// делаем цикл по дочерним элементам
	    	for (TreeItem<TemplateSimpleItem> i : oList) {
	    		//TemplateSimpleItem tft = new TemplateSimpleItem(i.getValue());
	            TemplateSimpleItem tft = i.getValue();
	    		boolean isFound = false;
	    		
	    		// проверяем элемент на нужный стиль : по typeItem, flag (infoTypeId)
	    		if (((tft.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_STYLE) || 
	    			 (tft.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_STYLE)) && 
		    		(tft.getId() == new_sip.getParentId()) && 
		    		(tft.getFlag() == new_sip.getInfoTypeId()))
		    		isFound = true;
	    		
	    		// создаем элемент для вставки. Вставляем новый элемент как его дочерний с нужным ИД темы
	    		if (isFound) {
	    			TreeItem<TemplateSimpleItem> item = new TreeItem<>(new TemplateSimpleItem(
	        				new_sip.getId(),        // style id
	        				new_sip.getName(),
	        				new_sip.getDescr(),
	        				tft.getThemeId(),
	        				new_sip.getTypeItem(),
	        				new_sip.getSubtypeItem(),
	        				new_sip.getInfoTypeId(),
	        				new_sip.getDateCreated(),
	        				new_sip.getDateModified(),
	        				new_sip.getUserCreated(),
	        				new_sip.getUserModified()
	                		));
	    			i.getChildren().add(item);
	    		}
	    		
	    		// вызываем эту ф-цию для проверки элементов следующей глубины вложения для текущего элемента
	    		addStyleItemRecursive (i, new_sip);
	    	}
	    }
	    
	    /**
	     * Изменяет стиль в дереве-контроле во всех темах
	     */
	    void updateStyleItemRecursive (TreeItem<TemplateSimpleItem> parentTI, TemplateStyleItem new_sip) {
	    	// получаем список дочерних элементов
	    	List<TreeItem<TemplateSimpleItem>> oList = parentTI.getChildren(); 

	    	// делаем цикл по дочерним элементам
	    	for (TreeItem<TemplateSimpleItem> i : oList) {
	    		//TemplateSimpleItem tft = new TemplateSimpleItem(i.getValue());
	    		TemplateSimpleItem tft = i.getValue();
	    		
	    		// проверяем элемент на нужный стиль
	    		if (((tft.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_STYLE) ||
	    			 (tft.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_STYLE)) &&
	    		    (tft.getId() == new_sip.getId())) {
	    			
	    			// создаем элемент для замены. Вставляем новый элемент с нужным ИД темы
	    			TemplateSimpleItem ttt = new TemplateSimpleItem(
	        				new_sip.getId(),        // style id
	        				new_sip.getName(),
	        				new_sip.getDescr(),
	        				tft.getThemeId(),
	        				new_sip.getTypeItem(),
	        				new_sip.getSubtypeItem(),
	        				new_sip.getInfoTypeId(),
	        				new_sip.getDateCreated(),
	        				new_sip.getDateModified(),
	        				new_sip.getUserCreated(),
	        				new_sip.getUserModified()
	                		);
	    			i.setValue(null);
	        		i.setValue(ttt);
	    		}

	    		// вызываем эту ф-цию для проверки элементов следующей глубины вложения для текущего элемента
	    		updateStyleItemRecursive (i, new_sip);
	    	}
	    }
	    
	    /**
		 * Удаляет стиль в дереве-контроле во всех темах
		 */
		private void deleteStyleItemRecursive (TreeItem<TemplateSimpleItem> parentTI, long styleId) {
			// получаем список дочерних элементов
			List<TreeItem<TemplateSimpleItem>> oList = parentTI.getChildren();
			Iterator<TreeItem<TemplateSimpleItem>> iter = oList.iterator();

			while (iter.hasNext()) {
				TreeItem<TemplateSimpleItem> ti = iter.next();
				TemplateSimpleItem tft = new TemplateSimpleItem(ti.getValue());

				// проверяем элемент на нужный стиль
				if (((tft.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_DIR_STYLE) || 
					 (tft.getTypeItem() == TemplateSimpleItem.TYPE_ITEM_STYLE)) && 
					(tft.getId() == styleId)) {
					//parentTI.getChildren().remove(i);
					iter.remove();
				} else {
					// вызываем эту ф-цию для проверки элементов следующей глубины вложения для текущего элемента
					deleteStyleItemRecursive (ti, styleId);
				}
			}
		}
	}
}
