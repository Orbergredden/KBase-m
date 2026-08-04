package app.view.business;

import app.Main;
import app.exceptions.DataConnectionException;
import app.exceptions.DataQueryException;
import app.lib.AppDataObj;
import app.lib.ConvertType;
import app.lib.DateConv;
import app.lib.FileUtil;
import app.lib.ShowAppMsg;
import app.model.AppItem_Interface;
import app.model.DBConCur_Parameters;
import app.model.DBConn_Parameters;
import app.model.Params;
import app.model.StateItem;
import app.model.StateList;
import app.model.business.DictionaryItem;
import app.model.business.InfoHeaderItem;
import app.model.business.Info_FileItem;
import app.model.business.Info_ImageItem;
import app.model.business.Info_TextItem;
import app.model.business.SectionClipboardInfo;
import app.model.business.SectionFavoriteItem;
import app.model.business.SectionItem;
import app.util.FormattedDate;
import app.view.structure.TabNavigationHistory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 * Контроллер основного фрейма. Показывает дерево разделов (и др.) и инфо по разделам.
 * v.1.01.00.013 2025-03-18
 */
public class SectionList_Controller implements Container_Interface, AppItem_Interface {
	private Params params;
    //
    private long rootSectionId;
    
    /**
     * Ковертор даты/времени
     */
    private DateConv dateConv;
    
    @FXML
	private Button button_Exit;
    @FXML
    private TitledPane titledPane_Title;
    
    @FXML
    private ToggleButton toggleButton_fixSplitPaneMain;
    @FXML
	private Button button_Find;
	@FXML
	private Button button_Export;
    @FXML
	private Button button_Import;
    
    @FXML
    private SplitPane splitPane_main;
    @FXML
	private AnchorPane anchorPane_PanelLeft;
    @FXML
    TabPane tabPane_Sections;
    @FXML
    Tab tab_ContentTree;
    @FXML
    private AnchorPane anchorPane_ContentTree;
    @FXML
    private Tab tab_Favorite;
    @FXML
    private AnchorPane anchorPane_Favorite;
    
    @FXML
	public TreeTableView<SectionItem> treeTableView_sections;
	@FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_id;
	@FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_typeId;
	@FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_name;
	@FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_iconId;
	@FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_description;
    @FXML
    private TreeTableColumn<SectionItem, String> treeTableColumn_dateCreated;
    @FXML
    private TreeTableColumn<SectionItem, String> treeTableColumn_dateModified;
    @FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_userCreated;
    @FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_userModified;
    @FXML
    private TreeTableColumn<SectionItem, String> treeTableColumn_dateModifiedInfo;
    @FXML
    private TreeTableColumn<SectionItem, Long> treeTableColumn_iconRoot;
    @FXML
    private TreeTableColumn<SectionItem, Long> treeTableColumn_iconIdDef;
    @FXML
    private TreeTableColumn<SectionItem, String> treeTableColumn_styleMain;
    @FXML
    private TreeTableColumn<SectionItem, String> treeTableColumn_styleTree;
    @FXML
    private TreeTableColumn<SectionItem, String> treeTableColumn_styleRoot;
    @FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_themeId;
    
    @FXML
    private MenuItem menuitem_sectionOpen;
    @FXML
    private MenuItem menuitem_sectionOpenInMainTab;
    @FXML
    private MenuItem menuitem_sectionOpenInWindow;
    @FXML
	private MenuItem menuitem_sectionAdd;
    @FXML
	private MenuItem menuitem_sectionUpdate;
    @FXML
	private MenuItem menuitem_sectionDelete;
    @FXML
	private MenuItem menuitem_sectionCopy;
    @FXML
	private MenuItem menuitem_sectionCut;
    @FXML
	private MenuItem menuitem_sectionPaste;
    @FXML
	private MenuItem menuitem_treeRefresh;
    @FXML
	private MenuItem menuitem_treeOpenInMainTab;
    @FXML
	private MenuItem menuitem_treeOpenInWindow;
    @FXML
	private MenuItem menuitem_find;
	@FXML
	private MenuItem menuitem_sectionAddToFavorite;
	@FXML
	private MenuItem menuitem_export;
    @FXML
	private MenuItem menuitem_import;
    
    @FXML
    TabPane tabPane_info;
    @FXML
    private Tab tab_DocCur;

	//
	public SectionList_Controller.TreeView_Controller treeViewCtrl;
	// записується сюди перед динамічним розгортанням гілки і використовується після розгортання
	private long treeViewSelectedItemId = 0;
    
    /**
     * Контролери документа та словника
     */
    DocumentView_Controller controller_DocView;
    DictionaryView_Controller controller_DictView;
    AnchorPane pageCurDoc;
    AnchorPane pageCurDict;
    // тип поточної інформаціїї
    int curTypeId;

    //
	private Preferences prefs;
	
	private TabNavigationHistory tabNavigationHistory;
	
	// false - дерево розділів завантажується у додаток повністю, true - динамічно, тільки розгорнуті гілки
	private boolean isTreeDynamicLoad;
	
	// прапорець що сплітер був позіціонований перший раз, до цього не зберігати позицію сплітера
	private boolean isSplitterPositionedFirst;
	// позиція сплітера в пікселях відносно лівої сторони
	int spliterWidth;
	
	// прапорець динамічного завантаження гілки
	private volatile boolean isTreeLoading = false;
	// використовуємо при переході до вказаного Розділу, чекаємо динамічне завантаження гілки
	private int retryCount = 0;
	private static final int MAX_RETRIES = 5000;
	// при переході на вказаний Розділ, не робимо вибір Розділу при динамічному завантаженні гілки
	private volatile boolean isGotoSection = false;

	//
	private SectionFavoriteList_Controller controller_Favorite;
    
    /**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public SectionList_Controller() {
    	dateConv = new DateConv();
		treeViewCtrl = this.new TreeView_Controller();
		prefs = Preferences.userNodeForPackage(SectionList_Controller.class);
		tabNavigationHistory = new TabNavigationHistory();
		isSplitterPositionedFirst = false;
    }
	
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {        
    }
    
    /**
     * Вызывается главным приложением, которое даёт параметры.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params, long rootSectionId) {
    	this.params = params;
        this.rootSectionId = rootSectionId;
        
        try {
			isTreeDynamicLoad = 
				(params.getConCur().db.settingsGetValue("SECTION_TREE_DYNAMIC_LOAD").compareTo("1") == 0) ? true : false;
		} catch (DataConnectionException | DataQueryException e) {
			e.writeLog(params);
			ShowAppMsg.showAlert(
				"ERROR", "Помилка при ініціалізації дерева Розділів Документів, "+
				"помилка при читанні типу завантаження дерева з таблиці налаштувань, встановлюю true",
				Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
			isTreeDynamicLoad = true;
		}
        
        // init controls
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями 
     */
    private void initControlsValue() {
    	button_Exit.setTooltip(new Tooltip("Зачинити фрейм"));
    	
    	//======== title
    	titledPane_Title.setText(titledPane_Title.getText() + " - " + params.getConCur().param.getConnName());
    	if (params.getConCur().param.getColorEnable()) {
    		titledPane_Title.setStyle("-fx-body-color: #" + 
	                              ConvertType.colorToHex(new Color(
	                            		  params.getConCur().param.getColorBRed_A(),  params.getConCur().param.getColorBGreen_A(),
	                            		  params.getConCur().param.getColorBBlue_A(), params.getConCur().param.getColorBOpacity_A())) + 
	                              ";" +
	                              "-fx-text-fill: #" +
    			                  ConvertType.colorToHex(new Color(
    			                		  params.getConCur().param.getColorTRed_A(),  params.getConCur().param.getColorTGreen_A(),
    			                		  params.getConCur().param.getColorTBlue_A(), params.getConCur().param.getColorTOpacity_A())) +
                                  ";");
    	}
    	
    	//======== Buttons bar
    	toggleButton_fixSplitPaneMain.setTooltip(new Tooltip("Фіксуємо головний спліттер"));
    	toggleButton_fixSplitPaneMain.setGraphic(new ImageView(new Image("file:resources/images/icon_fix_splitter_16.png")));
    	toggleButton_fixSplitPaneMain.setSelected(false);
		button_Find.setTooltip(new Tooltip("Пошук..."));
		button_Find.setGraphic(new ImageView(new Image("file:resources/images/icon_find_16.png")));
    	button_Export.setTooltip(new Tooltip("Експорт Розділа в технічну БД"));
    	button_Export.setGraphic(new ImageView(new Image("file:resources/images/icon_export_16.png")));
    	button_Import.setTooltip(new Tooltip("Імпорт Розділа з технічної БД"));
    	button_Import.setGraphic(new ImageView(new Image("file:resources/images/icon_import_16.png")));
    	
    	//======== splitPane_main
    	// беремо значення позиції сплітера з конфіга
    	spliterWidth = getSpliterWidthFromConfig();

    	// Встановлення початкового розміру після побудови
    	Platform.runLater(() -> {
    		double position = getSpliterPos(spliterWidth); // Відносна позиція сплітера
    	    splitPane_main.setDividerPositions(position); // Встановлення позиції
    	    isSplitterPositionedFirst = true;
    	    //System.out.println("Встановлено позицію сплітера: " + position + " (spliterWidth: " + spliterWidth + ")");
    	});
    	
    	// Слухач для збереження позиції
    	splitPane_main.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
    	    if (!toggleButton_fixSplitPaneMain.isSelected()) {
    	    	if (isSplitterPositionedFirst) {
    	    		spliterWidth = getSpliterWidth(newValue.doubleValue());
    	    	}
    	    } else {
    	    	//Platform.runLater(() -> {
    	    	    double position = getSpliterPos(spliterWidth); // Відносна позиція сплітера
    	    	    splitPane_main.setDividerPositions(position); // Встановлення позиції
    	    	    isSplitterPositionedFirst = true;
    	    	    //System.out.println("Встановлено fix позицію : " + position + " (savedWidth: " + savedWidth + ", windowWidth: " + windowWidth + ")");
    	    	//});
    	    }
    	});

    	//======== Content Tabs (left side)
    	// tab_ContentTree
    	// выводим картинку и надпись
    	HBox hbox_ContentTree = new HBox();
    	Label label_TitleContentTree = new Label(" Зміст");
    	hbox_ContentTree.getChildren().add(new ImageView(new Image("file:resources/images/icon_ContentTree_16.png")));
    	hbox_ContentTree.getChildren().add(label_TitleContentTree);
    	tab_ContentTree.setText("");
    	tab_ContentTree.setGraphic(hbox_ContentTree);
    	
    	//======== hot keys
    	anchorPane_ContentTree.setOnKeyPressed(event -> {
    		if (event.getCode() == KeyCode.R && event.isControlDown()) {
    	        handleButtonRefreshTree();
    	    }
    		// розблокуємо сплітер
    		if (event.getCode() == KeyCode.U && event.isControlDown()) {
    			toggleButton_fixSplitPaneMain.setSelected(false);
    			
    			spliterWidth = getSpliterWidthFromConfig();
    			double pos = getSpliterPos(spliterWidth);
    			splitPane_main.setDividerPositions(pos);
    		}
    	});
    	
    	//======== TreeTableView Sections
		treeViewCtrl.init();

    	// ContextMenu
		menuitem_sectionOpen.setGraphic(new ImageView(new Image("file:resources/images/icon_open_16.png")));
		menuitem_sectionOpenInMainTab.setGraphic(new ImageView(new Image("file:resources/images/icon_open_16.png")));
		menuitem_sectionOpenInWindow.setGraphic(new ImageView(new Image("file:resources/images/icon_open_16.png")));
    	menuitem_sectionCopy.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_16.png")));
    	menuitem_sectionCut.setGraphic(new ImageView(new Image("file:resources/images/icon_cut_16.png")));
    	menuitem_sectionPaste.setGraphic(new ImageView(new Image("file:resources/images/icon_paste_16.png")));
    	menuitem_sectionAdd.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
    	menuitem_sectionUpdate.setGraphic(new ImageView(new Image("file:resources/images/icon_update_16.png")));
    	menuitem_sectionDelete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	menuitem_treeRefresh.setGraphic(new ImageView(new Image("file:resources/images/icon_refresh_16.png")));
    	menuitem_treeOpenInMainTab.setGraphic(new ImageView(new Image("file:resources/images/icon_open_tree_16.png")));
    	menuitem_treeOpenInWindow.setGraphic(new ImageView(new Image("file:resources/images/icon_open_tree_16.png")));
		menuitem_find.setGraphic(new ImageView(new Image("file:resources/images/icon_find_16.png")));
		menuitem_sectionAddToFavorite.setGraphic(new ImageView(new Image("file:resources/images/icon_favorite_16.png")));
    	menuitem_export.setGraphic(new ImageView(new Image("file:resources/images/icon_export_16.png")));
    	menuitem_import.setGraphic(new ImageView(new Image("file:resources/images/icon_import_16.png")));

    	//
    	initTabPaneInfo();

    	//======== init tab_DocCurs
    	TreeItem<SectionItem> startSectionItem = null;
    	
    	// вибираємо перший ліпший розділ
    	if (treeTableView_sections.getRoot().getValue().getId() > 0) {
    		startSectionItem = treeTableView_sections.getRoot();
    	} else {
    		if (! treeTableView_sections.getRoot().getChildren().isEmpty()) {
    			if (treeTableView_sections.getRoot().getChildren().get(0).getValue().getId() > 0) {
    				startSectionItem = treeTableView_sections.getRoot().getChildren().get(0);
    			}
    		}
    	}
    	
    	treeTableView_sections.getSelectionModel().select(startSectionItem);
    	
    	// add tab icon
    	HBox hbox_DocCur = new HBox();
    	Label label_TitleDocCur = new Label(" Поточний");
    	hbox_DocCur.getChildren().add(new ImageView(new Image("file:resources/images/icon_document_16.png")));
    	hbox_DocCur.getChildren().add(label_TitleDocCur);
    	tab_DocCur.setText("");
    	tab_DocCur.setGraphic(hbox_DocCur);
    	
    	//======== Favorite Tab (left side)
    	initTabFavorite();
    }
    
    /**
	 * Створюємо на ініціалізуємо таб з Фаворитами у лівому ТабПейні Розділів
	 */
	private void initTabFavorite () {

		//======== create
		anchorPane_Favorite = new AnchorPane();
		// (опціонально) налаштування розмірів або стилів
		//anchorPane_Favorite.setPrefSize(100, 300);
		tab_Favorite = new Tab("Favorite");
		//tab_Favorite.setContent(anchorPane_Favorite);
		tabPane_Sections.getTabs().add(tab_Favorite);

		//======== показуємо картинку та напис
		HBox hbox_Favorite = new HBox();
		Label label_TitleFavorite = new Label("Favorite");
		hbox_Favorite.getChildren().add(new ImageView(new Image("file:resources/images/icon_favorite_16.png")));
		hbox_Favorite.getChildren().add(label_TitleFavorite);
		tab_Favorite.setText("");
		tab_Favorite.setGraphic(hbox_Favorite);

		//======== load form
		try {
			// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/SectionFavoriteList.fxml"));
			anchorPane_Favorite = loader.load();
			tab_Favorite.setContent(anchorPane_Favorite);

			// Даём контроллеру доступ к главному приложению (передаем параметры).
			controller_Favorite = loader.getController();

			Params params = new Params(this.params);
			params.setObjContainer(this);
			params.setParentObj(this);

			controller_Favorite.setParams(params);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    private void initTabPaneInfo () {
    	//изменение активного таба
    	tabPane_info.getSelectionModel().selectedItemProperty().addListener(
    	    new ChangeListener<Tab>() {
    	        @Override
    	        public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
    	        	//-------- adding a new tab to the history of active tabs (stack)
    	        	if (newTab != null) {
						AppItem_Interface newTabCtrl = (AppItem_Interface)newTab.getUserData();
    	        	
						if (! tabNavigationHistory.getIsDeleted()) {
							tabNavigationHistory.add(newTabCtrl.getOID());
						}
    	        	}
    	        }
    	    }
    	);
    }
    
    private void initControllerDocView () {
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/DocumentView_Layout.fxml"));
			pageCurDoc = loader.load();
		
			// Даём контроллеру доступ к главному прилодению (передаем параметры).
			controller_DocView = loader.getController();
			
			Params params = new Params(this.params);
			params.setObjContainer(this);
			params.setParentObj(this);
			
			controller_DocView.setParams(params, false);
    	} catch (IOException e) {
            e.printStackTrace();
            curTypeId = 0;
        }
    }
    
    private void initControllerDictView () {
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/DictionaryView.fxml"));
			pageCurDict = loader.load();
		
			// Даём контроллеру доступ к главному прилодению (передаем параметры).
			controller_DictView = loader.getController();
			
			Params params = new Params(this.params);
			params.setObjContainer(this);
			params.setParentObj(this);
			
			controller_DictView.setParams(params, false);
    	} catch (IOException e) {
            e.printStackTrace();
            curTypeId = 0;
        }
    }
    
    /**
     * Вызывается при выборе раздела в дереве разделов
     */
    private void showSectionInfo(TreeItem<SectionItem> ti) {
    	if (ti != null) {
    		switch (ti.getValue().getTypeId()) {
    		case 1:      // Document
    			if (curTypeId != 1) {
        			if (controller_DocView == null) {
            			initControllerDocView();
            		}
        			tab_DocCur.setContent(pageCurDoc);
        			tab_DocCur.setUserData(controller_DocView);         // запихиваем ссылку на контролер для внешних вызовов
        			curTypeId = 1;
        		}
    			
    			params.setMsgToStatusBar(treeViewCtrl.getSectionPath (ti, 1));
            	
            	//load document
            	controller_DocView.load(ti.getValue().getId(), false);
    			
    			break;
    		case 2:      // Dictionary
    			if (curTypeId != 2) {
        			if (controller_DictView == null) {
            			initControllerDictView();
            		}
        			tab_DocCur.setContent(pageCurDict);
        			tab_DocCur.setUserData(controller_DictView);         // запихиваем ссылку на контролер для внешних вызовов
        			curTypeId = 2;
        		}
    			
    			params.setMsgToStatusBar(treeViewCtrl.getSectionPath (ti, 1));
    			
    			//load document
            	controller_DictView.load(ti.getValue().getId());
    			
    			break;
    		default:
    			params.setMsgToStatusBar("");
        		
        		if (curTypeId != 1) {
        			if (controller_DocView == null) {
            			initControllerDocView();
            		}
        			tab_DocCur.setContent(pageCurDoc);
        			tab_DocCur.setUserData(controller_DocView);         // запихиваем ссылку на контролер для внешних вызовов
        			curTypeId = 1;
        		}
        		
        		//load empty document
            	controller_DocView.loadEmptyPage();
    		}
    	} else {
    		params.setMsgToStatusBar("");
    		
    		if (curTypeId != 1) {
    			if (controller_DocView == null) {
        			initControllerDocView();
        		}
    			tab_DocCur.setContent(pageCurDoc);
    			tab_DocCur.setUserData(controller_DocView);         // запихиваем ссылку на контролер для внешних вызовов
    			curTypeId = 1;
    		}
    		
    		//load empty document
        	controller_DocView.loadEmptyPage();
    	}
    }
    
    /**
     * Вызывается при нажатии на кнопке "Закрыть" (X)
     */
    @FXML
    private void handleButtonExit() {
    	close();
    }
    
    /**
     * 
     */
    @FXML
    private void handleButtonFixSplitPaneMain() {
   		params.setMsgToStatusBar("Розділи документів, фіксаціяі сплітера з шириною ліворуч " + spliterWidth);
    }
    
    /**
	 * 
	 */
	@FXML
	private void handleButtonFind() {
		
		try {
			TreeItem<SectionItem> tsi = treeTableView_sections.getSelectionModel().getSelectedItem();
			
			//------- відкриваємо таб для пошуку
			Params params = new Params(this.params);
			params.setObjContainer(this);
			params.setParentObj(this);
			params.setTabPane_Cur(tabPane_info);
			
			AppDataObj.openFindInfo (params, tsi);
		} catch (NullPointerException e) {
			ShowAppMsg.showAlert("WARNING", "Немає вибору", "Не вибраний розділ в списку",
					"Виберіть розділ.");
			return;
		}
	}
	
	/**
     * 
     */
    @FXML
    private void handleButtonExport() {
   		
    	
    	
    	
    }
    //TODO
    
    /**
     * 
     */
    @FXML
    private void handleButtonImport() {
   		ShowAppMsg.showAlert("WARNING", "", "", "Не реалізовано...");
    	
    	
    	
    	
    }
    //TODO

    /**
	 * Открывает раздел (документ) в новой вкладке в табе разделов
	 */
	@FXML
	private void handleButtonOpenSection() {
		
		if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел в списке", 
    				"Выберите раздел, который необходимо открыть.");
    		return;
    	}

		TreeItem<SectionItem> tsi = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
    	//-------- открываем таб для просмотра документа
		Params params = new Params(this.params);
		params.setObjContainer(this);
		params.setParentObj(this);
		params.setTabPane_Cur(tabPane_info);
		
		AppDataObj.openDocumentView (params, tsi);
	}
	
	/**
	 * Открывает раздел (документ) в новой вкладке в основном табе
	 */
	@FXML
	private void handleButtonOpenSectionInMainTab() {
		
		if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел в списке", 
    				"Выберите раздел, который необходимо открыть.");
    		return;
    	}

		TreeItem<SectionItem> tsi = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
    	//-------- открываем таб для просмотра документа
		Params params = new Params(this.params);
		params.setObjContainer(params.getRootController());
		params.setParentObj(this);
		params.setTabPane_Cur(params.getTabPane_Main());
		
		AppDataObj.openDocumentView (params, tsi);
	}
	
	/**
	 * Открывает раздел (документ) в новой вкладке в отдельном окне
	 */
	@FXML
	private void handleButtonOpenSectionInWindow() {
		
		if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел в списке", 
    				"Выберите раздел, который необходимо открыть.");
    		return;
    	}

		TreeItem<SectionItem> tsi = treeTableView_sections.getSelectionModel().getSelectedItem();

		//-------- открываем окно для просмотра документа
		Params params = new Params(this.params);
		params.setObjContainer(params.getWinList());
		params.setParentObj(this);
		params.setTabPane_Cur(null);
		
		(new AppDataObj()).openDocumentViewInWin(params, tsi);
	}
    
    /**
	 * Добавляем новый раздел
	 */
	@FXML
	private void handleButtonAddSection() {
		if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
			ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел", 
					"Выберите раздел, в который добавиться новый подраздел.");
			return;
		}
		
		try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/SectionEdit_Layout.fxml"));
			AnchorPane page = loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Добавление нового раздела");
			dialogStage.initModality(Modality.NONE);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_Sections_24.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(SectionList_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble("stageSectionsEdit_Width", 500));
			dialogStage.setHeight(prefs.getDouble("stageSectionsEdit_Height", 700));
			dialogStage.setX(prefs.getDouble("stageSectionsEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageSectionsEdit_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			SectionEdit_Controller controller = loader.getController();
			
			Params params = new Params(this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
	        controller.setParams(params, 1, treeTableView_sections.getSelectionModel().getSelectedItem());
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        //
	        ///////////treeTableView_sections.refresh();
		} catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Змінюємо поточний розділ
	 */
	@FXML
	private void handleButtonUpdateSection() {
		if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
			ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел", "Выберите раздел для редактирования");
			return;
		}
		
		try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/SectionEdit_Layout.fxml"));
			AnchorPane page = loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Редактирование раздела");
			dialogStage.initModality(Modality.NONE);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_Sections_24.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(SectionList_Controller.class);
			dialogStage.setWidth(prefs.getDouble("stageSectionsEdit_Width", 500));
			dialogStage.setHeight(prefs.getDouble("stageSectionsEdit_Height", 700));
			dialogStage.setX(prefs.getDouble("stageSectionsEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageSectionsEdit_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			SectionEdit_Controller controller = loader.getController();
			
			Params params = new Params(this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
	        controller.setParams(params, 2, treeTableView_sections.getSelectionModel().getSelectedItem());
	        
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
		} catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Удаляем текущий раздел
	 */
	@FXML
	private void handleButtonDeleteSection() {
		TreeItem<SectionItem> selectedItem = treeTableView_sections.getSelectionModel().getSelectedItem();
		
		if (selectedItem == null) {
			params.setMsgToStatusBar("Нічого не обрано для видалення.");
			return;
		}
		
		SectionItem sip = selectedItem.getValue();
		
		if (sip.getId() == 0) {
			ShowAppMsg.showAlert("INFORMATION", "Удаление раздела", 
					"Удаление раздела '"+ sip.getName() +"'", "Удалять корневой раздел нельзя !");
			return;
		}
		
		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление раздела", 
	            "Удаление раздела '"+ sip.getName() +"'", "Удалить раздел ?"))
			return;
		
		int childrenCount = params.getConCur().db.sectionGetNumberOfChildren(sip.getId());
		
		if ((childrenCount > 0) && (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление раздела", 
	            "Раздел '"+ sip.getName() +"' содержит подраздел(ы).", "Удалить раздел вместе с подразделами ?")))  
			return;
		
		// delete from DB 
		params.getConCur().db.sectionDelete(sip.getId());
		
		// delete from TreeTableView
		TreeItem<SectionItem> parentItem = selectedItem.getParent();
		if (parentItem != null) {     // текущая иконка не корневая
	        parentItem.getChildren().remove(selectedItem);
		}
		
		// выводим сообщение в статус бар
		params.setMsgToStatusBar("Розділ '" + sip.getName() + "' вилучений.");
	}

	/**
     * Копирует текущий раздел в локальный буфер обмена
     */
    @FXML
    private void handleButtonSectionCopy() {
    	TreeItem<SectionItem> selectedItem = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
    	saveToClipboard (SectionClipboardInfo.TYPE_OPER_COPY);
    	
        params.setMsgToStatusBar("Раздел '"+ selectedItem.getValue().getName() +"' скопирован в локальный буфер обмена.");
    }
    
    /**
     * Вырезает текущий раздел с занесением в локальный буфер обмена
     */
    @FXML
    private void handleButtonSectionCut() {
    	TreeItem<SectionItem> selectedItem = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
    	saveToClipboard (SectionClipboardInfo.TYPE_OPER_CUT);
    	
        params.setMsgToStatusBar("Раздел '"+ selectedItem.getValue().getName() +"' вырезан в локальный буфер обмена.");
    }
    
    /**
     * Вставляет раздел указанный в буфере обмена
     */
    @FXML
    private void handleButtonSectionPaste() {
    	SectionClipboardInfo clip;
    	TreeItem<SectionItem> selectedItem = treeTableView_sections.getSelectionModel().getSelectedItem();
    	SectionItem item;
    	
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
    	clip = SectionClipboardInfo.unserialize(path, SectionClipboardInfo.FILE_NAME_INFO);
    	
    	// check CUT for different connections
    	if ((clip.getTypeOper() == SectionClipboardInfo.TYPE_OPER_CUT) && 
    		(clip.getDbCon_Id() != params.getConCur().Id)) {
    		ShowAppMsg.showAlert("WARNING", "Перенос об'екту", 
    				"Неможливо переносити об'єкт між різними БД, тільки копіювати.", "");
    		return;
    	}
    	
    	// read section object
    	SectionItem sectionItem = 
    			SectionItem.unserialize(path, SectionClipboardInfo.FILE_NAME, SectionClipboardInfo.FILE_NAME_ICON);
    	
    	// copy or cut object
    	switch (clip.getTypeOper()) {
    	case SectionClipboardInfo.TYPE_OPER_COPY :
    		if (clip.getDbCon_Id() == params.getConCur().Id) {  // вставляємо в туж саму БД (взяв старий код)
    			boolean copyWithSubSection = prefs.get("copyWithSubSection", "No").equals("Yes");
            	int retVal = ShowAppMsg.showQuestionWithOption(
            			"CONFIRMATION", "Копирование раздела", 
                          "Копировать раздел '"+ sectionItem.getName() +"' ?", null,
                          "Копировать ветку целиком", copyWithSubSection);
            	long newSectionId = 0;
            	
            	if (retVal == ShowAppMsg.QUESTION_OK) {          // сохраняем только один раздел
            		newSectionId = treeViewCtrl.copySection (sectionItem, selectedItem, false);
            		
            		// save Option value
            		prefs.put("copyWithSubSection", "No");
            		
            		// выводим сообщение в статус бар
            		params.setMsgToStatusBar("Раздел '" + sectionItem.getName() + "' скопирован.");
            	}
            	if (retVal == ShowAppMsg.QUESTION_OK_WITH_OPTION) {          // сохраняем всю ветку
            		newSectionId = treeViewCtrl.copySection (sectionItem, selectedItem, true);
            		
            		// save Option value
            		prefs.put("copyWithSubSection", "Yes");
            		
            		// выводим сообщение в статус бар
            		params.setMsgToStatusBar("Раздел (ветка) '" + sectionItem.getName() + "' скопирована.");
            	}
            	
            	treeTableView_sections.sort();
            	treeViewCtrl.expandTreeItemById(treeViewCtrl.root, newSectionId);
            	treeViewCtrl.selectTreeItemById(treeViewCtrl.root, newSectionId);
    		} else {      // бази даних різні
    			//---- десеріалізація та підготовка даних, додаємо в БД
    			sectionItem.setId(params.getConCur().db.sectionNextId());
    			sectionItem.setParentId(item.getId());
    			sectionItem.setIconId(0);
    			sectionItem.setIconIdRoot(0);
    			sectionItem.setIconIdDef(0);
    			sectionItem.setThemeId(item.getThemeId());
    			
    			params.getConCur().db.sectionAdd(sectionItem);
    			
    			//
    			InfoHeaderItem ihi;
    			
    			for (int i=1; i<=clip.getNumberOfInfoBlocks(); i++) {
    				ihi = InfoHeaderItem.unserialize(path, 
    						SectionClipboardInfo.FILE_PREFIX_INFO_HEADER+i+SectionClipboardInfo.FILE_POSTFIX);
    				ihi.setId(params.getConCur().db.infoNextId());
    				ihi.setSectionId(sectionItem.getId());
    				ihi.setTemplateStyleId(0);
    				//infoId ...
    				
    				switch ((int)ihi.getInfoTypeId()) {
    	    		case 1 :    // Простий текст
    	    			Info_TextItem it = Info_TextItem.unserialize(path, 
    	    					SectionClipboardInfo.FILE_PREFIX_INFO_BLOCK+i+SectionClipboardInfo.FILE_POSTFIX);
    	    			it.setId(params.getConCur().db.info_TextNextId());
    	    			ihi.setInfoId(it.getId());
    	    			params.getConCur().db.info_TextAdd(it);
    	    			break;
    	    		case 2 :    // Зображення
    	    			Info_ImageItem ii = Info_ImageItem.unserialize(path, 
    	    					SectionClipboardInfo.FILE_PREFIX_INFO_BLOCK+i+SectionClipboardInfo.FILE_POSTFIX,
    	        				SectionClipboardInfo.FILE_PREFIX_INFO_FILE +i+SectionClipboardInfo.FILE_POSTFIX);
    	    			ii.setId(params.getConCur().db.info_ImageNextId());
    	    			ihi.setInfoId(ii.getId());
    	    			params.getConCur().db.info_ImageAdd(ii, 
    	    					path+SectionClipboardInfo.FILE_PREFIX_INFO_FILE +i+SectionClipboardInfo.FILE_POSTFIX);
    	    			break;
    	    		case 3 :    // Файл
    	    			Info_FileItem ifl = Info_FileItem.unserialize(path, 
    	    					SectionClipboardInfo.FILE_PREFIX_INFO_BLOCK+i+SectionClipboardInfo.FILE_POSTFIX);
    	    			ifl.setId(params.getConCur().db.info_TextNextId());
    	    			ifl.setIconId(0);;
    	    			ihi.setInfoId(ifl.getId());
    	    			params.getConCur().db.info_FileAdd(ifl, null);
    	    			break;
    	    		default :
    	    			ShowAppMsg.showAlert("WARNING", "Copy/Cut to local clipboard", 
    	    					"Невідомий тип інфоблока", 
    	   		                "Тип інфоблока "+ihi.getInfoTypeId()+" не визначений");
    	    		}
    				
    				params.getConCur().db.infoAdd(ihi);
    			}
    			
    			//---- додаємо в дерево-контрол
    			TreeItem<SectionItem> subItemS = new TreeItem<>(sectionItem);
    			selectedItem.getChildren().add(subItemS);
    			selectedItem.setExpanded(true);
    		}
    		break;
    	case SectionClipboardInfo.TYPE_OPER_CUT :
    		TreeItem<SectionItem> tiSectionFromSer = new TreeItem<SectionItem> (sectionItem); 
    		
    		selectedItem.getChildren().add(tiSectionFromSer);
    		treeTableView_sections.sort();
    		treeTableView_sections.getSelectionModel().select(tiSectionFromSer);
			//treeViewCtrl.selectTreeItemById(treeTableView_sections.getRoot(), clipBoard_tiSection.getValue().getId());
    		
    		// update in DB
    		params.getConCur().db.sectionMove (sectionItem.getId(), item.getId());
    		
    		// выводим сообщение в статус бар
    		params.setMsgToStatusBar("Раздел '" + sectionItem.getName() + "' перемещен.");
    		
    		break;
    	}
    }
    
    /**
     * Обновляет ветку разделов
     */
    @FXML
    private void handleButtonRefreshTree() {
    	StateList stateListTree = new StateList();
    	TreeItem<SectionItem> mainItem = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
    	if (mainItem == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел в списке", 
    				"Выберите раздел, который необходимо обновить.");
    		return;
    	}
    	
    	//==== save state
    	try {
    		stateListTree.add(
					"TreeItemSelected",
					Long.toString(mainItem.getValue().getId()),      // section id in DB,
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
    	if (mainItem.getValue().getId() != 0) {
    		mainItem.setValue(params.getConCur().db.sectionGetById(mainItem.getValue().getId()));
    	}
    	
    	//==== создаем ветку заново
    	if (! isTreeDynamicLoad) {
    		treeViewCtrl.initTreeItemsRecursive(mainItem);
    	}
    	

    	//==== восстанавливаем состояние ветки
    	ObservableList<Long> listItemsForExpand = FXCollections.observableArrayList();
		Long selectedItemId = new Long(0);
    	
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
					treeTableView_sections.sort();
					restoreTreeItemSelectedRecursive(selectedItemId,mainItem);
					treeTableView_sections.sort();
					break;
			}
    	}
    	
    	if (isTreeDynamicLoad) {
    		TreeItem<SectionItem> mi = treeTableView_sections.getSelectionModel().getSelectedItem();
    		if ((! mi.isExpanded()) && (params.getConCur().db.sectionGetNumberOfChildren(mi.getValue().getId()) > 0)) {
    			mi.getChildren().add(treeViewCtrl.createItem_Loading());
    		}
    	}
    	
    	//==== рефрешнуть документ
    	showSectionInfo(mainItem);
    }
    
    /**
     * Открывает ветку разделов в новой вкладке в основном табе
     */
    @FXML
    private void handleButtonOpenTreeInMainTab() {
    	
    	if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел в списке", 
    				"Выберите раздел, который необходимо открыть.");
    		return;
    	}

		TreeItem<SectionItem> tsi = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
    	//-------- открываем таб для ветки разделов
		Params params = new Params(this.params);
		params.setObjContainer(params.getRootController());
		params.setTabPane_Cur(params.getTabPane_Main());
		
		AppDataObj.openSectionTree (params, tsi.getValue().getId());
    }
    
    /**
     * Открывает ветку разделов в новом окне
     */
    @FXML
    private void handleButtonOpenTreeInWindow() {
    	
    	if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел в списке", 
    				"Выберите раздел, который необходимо открыть.");
    		return;
    	}

		TreeItem<SectionItem> tsi = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
		//-------- открываем окно
		Params params = new Params(this.params);
		params.setObjContainer(params.getWinList());
		params.setTabPane_Cur(null);
		
		(new AppDataObj()).openSectionTreeInWin(params, tsi.getValue().getId());
    }

    /**
	 * Додаємо Розділ в Фаворити
	 */
	@FXML
	private void handleButtonAddToFavorite() {
		TreeItem<SectionItem> tsi = treeTableView_sections.getSelectionModel().getSelectedItem();

		if (tsi == null) {
			return;
		}

		try {
			if (params.getConCur().db.sectionFavoriteIsPresent(tsi.getValue().getId())) {
				return;
			}
			params.getConCur().db.sectionFavoriteAdd(tsi.getValue().getId());
			params.getConCur().db.sectionFavoriteRebuildParentId();

			controller_Favorite.refreshFavoriteTree();
		} catch (DataConnectionException | DataQueryException e) {
			e.writeLog(params);
			ShowAppMsg.showAlert(
					"ERROR", "Помилка при додаванні Розділа в Favorite",
					Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
		}
	}
    
    /**
	 * Реализуем метод интерфейса Container_Interface.
     * Показывает состояние инфо блока во внешнем контейнере - были несохраненные изменения или нет.
	 */
	public void showStateChanged(int oid, boolean isChanged) {
        Tab ourTab;
        HBox hbox;
        String changingIconId = "changingIconId";

        //---- ищем таб по ИД
        for (int i=0; i<tabPane_info.getTabs().size(); i++) {
        	if ((tabPane_info.getTabs().get(i).getUserData() != null) && 
    			(((AppItem_Interface)tabPane_info.getTabs().get(i).getUserData()).getOID() == oid)) {
                ourTab = tabPane_info.getTabs().get(i);
                hbox = (HBox) ourTab.getGraphic();

                if (isChanged) {               // was change
                    if ((hbox.getChildren().get(1).getId() == null) ||
                        (! hbox.getChildren().get(1).getId().equals(changingIconId))) {
                        ImageView imageView_changed;

                        imageView_changed = new ImageView(new Image("file:resources/images/icon_edited_16.png"));
                        imageView_changed.setId(changingIconId);

						hbox.getChildren().add(1, imageView_changed);
                    }
                } else {                       // no change
                    if ((hbox.getChildren().get(1).getId() != null) &&
                        (hbox.getChildren().get(1).getId().equals(changingIconId))) {
                        hbox.getChildren().remove(1);
                    }
                }
                
                // меняем состояние у родителя
                if (isChanged) {
                	params.getObjContainer().showStateChanged(getOID(), isChanged);
                } else {
                	if (! checkUnsavedData()) {
                		params.getObjContainer().showStateChanged(getOID(), isChanged);
                	}
                }
            }
        }
	}

    /**
     * Реализуем метод интерфейса Container_Interface.
     * Закрываем фрейм с редактированием инфо блока чи ще якоюсь інформаціє у майбутньому
     */
	public void closeContainer (int oid) {
        for (int i=0; i<tabPane_info.getTabs().size(); i++) {
			if ((tabPane_info.getTabs().get(i).getUserData() != null) && 
				(((AppItem_Interface)tabPane_info.getTabs().get(i).getUserData()).getOID() == oid)) {
				
				tabNavigationHistory.delete(oid);
				
				tabPane_info.getTabs().remove(i);
			}
		}
		
		tabNavigationHistory.setIsDeleted(false);
		int lastOId = tabNavigationHistory.getLast();
		
		if (lastOId != 0) {
			for (int i=0; i<tabPane_info.getTabs().size(); i++) {
				if ((tabPane_info.getTabs().get(i).getUserData() != null) && 
					(((AppItem_Interface)tabPane_info.getTabs().get(i).getUserData()).getOID() == lastOId)) {
					
					// activate tab
					tabPane_info.getSelectionModel().select(i);
				}
			}
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
		return AppItem_Interface.ELEMENT_SECTION_LIST;
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
		return params.getConCur().Id;
	}

	/**
	 * контроллер элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public Object getController() {
		return this;
	}
	
	/**
     * Корень дерева
     * Реализуем метод интерфейса AppItem_Interface.
     */
    public long getRootId() {
    	return rootSectionId;
    }
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.            <br>
	 * Проверяем наличие несохраненных данных
	 */
	public boolean checkUnsavedData () {
		AppItem_Interface appItem;
		
		for (int i=1; i<tabPane_info.getTabs().size(); i++) {
			appItem = (AppItem_Interface)tabPane_info.getTabs().get(i).getUserData();
			if (appItem.checkUnsavedData()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.
     * Сохранение  измененной информации в БД
     */
    public void save () {
    	int curentIndex = tabPane_info.getSelectionModel().getSelectedIndex();
    	
    	if (curentIndex > 0) {
    		AppItem_Interface appItem = (AppItem_Interface)tabPane_info.getTabs().get(curentIndex).getUserData();
    		appItem.save();
    	}
    }
    
    /**
	 * Реализуем метод интерфейса AppItem_Interface.
     * Сохраняем всю измененную информацию в БД
     */
    public void saveAll () {
    	AppItem_Interface appItem;

		for (int i=1; i<tabPane_info.getTabs().size(); i++) {
			appItem = (AppItem_Interface)tabPane_info.getTabs().get(i).getUserData();
			appItem.saveAll();
		}
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
    	
    	//-------- проверка на несохраненные данные
    	if (checkUnsavedData()) {
    		if (ShowAppMsg.showQuestion (
					"CONFIRMATION", 
					"Питання", 
					"Во вкладке есть несохраненные данные.", 
					"Сохнять их перед закрытием вкладки ?")) {
				saveAll();
			}
    	}

		//-------- sort
		if (treeTableView_sections.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
			TreeTableColumn currentSortColumn = (TreeTableColumn) treeTableView_sections.getSortOrder().get(0);
			prefs.put("SectionsList_sortColumnId",currentSortColumn.getId());
			prefs.put("SectionsList_sortType",currentSortColumn.getSortType().toString());
		} else {
			prefs.remove("SectionsList_sortColumnId");
			prefs.remove("SectionsList_sortType");
		}

    	//
		params.getObjContainer().closeContainer(getOID());
    }

    /**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Сохраняем состояние контролов в иерархической структуре
	 */
	public void saveControlsState (StateList stateList) {
		StateItem stateItem;
		
		//-------- treeTableView_sections
		String sortColumnId;
		String sortType;
		
		if (treeTableView_sections.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
			TreeTableColumn currentSortColumn = (TreeTableColumn) treeTableView_sections.getSortOrder().get(0);
			
			sortColumnId = currentSortColumn.getId();
			sortType = currentSortColumn.getSortType().toString();
		} else {
			sortColumnId = "";
			sortType = "";
		}
		stateList.add("TreeTable_sortColumnId",	sortColumnId, null);
		stateList.add("TreeTable_sortType",	sortType, null);
		stateList.add("TreeTable_doSort", "", null);
		
		try {
			stateList.add(
					"TreeItemSelected",
					Long.toString(treeTableView_sections.getSelectionModel().getSelectedItem().getValue().getId()),      // section id in DB,
					null);
		} catch (NullPointerException ex) {    }
		addTreeItemStateRecursive(stateList,treeTableView_sections.getRoot());
		stateList.add(
				"TreeItemsDoExpandAndSelected",
				"",
				null);
		
		//------- Favorite
		stateItem = stateList.add(
				"favoriteSubItems",
				"",
				new StateList());
		controller_Favorite.saveControlsState(stateItem.subItems);
		
		//-------- save main split state
		stateList.add(
				"splitPane_main_Position",
				String.valueOf(spliterWidth),
				null);
		stateList.add(
				"splitPane_main_fixMode",
				(toggleButton_fixSplitPaneMain.isSelected()) ? "1" : "0",
				null);

		//--------- save tabs state
		AppItem_Interface appItem;
		DBConCur_Parameters conCur;           // обьект текущего соединения
		DBConn_Parameters conPar;             // параметры текущего соединения
		
		// "Текущий" таб
		appItem = (AppItem_Interface)tabPane_info.getTabs().get(0).getUserData();
		stateItem = stateList.add(
				"tabSubItems",
				"",
				new StateList());
		appItem.saveControlsState(stateItem.subItems);

		for (int i=1; i<tabPane_info.getTabs().size(); i++) {
			appItem = (AppItem_Interface)tabPane_info.getTabs().get(i).getUserData();
			conCur = params.getConnDB().conList.get(params.getConnDB().getIndexById(appItem.getDbConnId()));
			conPar = conCur.param;
			
			Tab curTab = tabPane_info.getTabs().get(i);
			HBox hbox = (HBox) curTab.getGraphic();
			Node nodeTitle;
			if (hbox.getChildren().size() == 3) {
				nodeTitle = hbox.getChildren().get(2);
			} else {
				nodeTitle = hbox.getChildren().get(1);
			}
			String tabTitle = ((Label)nodeTitle).getText();

			stateList.add(
					"tabName",
					appItem.getName(),
					null);
			stateList.add(
					"tabAppItemId",
					Long.toString(appItem.getAppItemId()),
					null);
			stateList.add(
					"tabDbConnId",
					Integer.toString(conPar.getConnId()),
					null);
			stateList.add(
					"tabCreateAction",
					"",
					null);
			stateList.add(
					"tabRenameTitleAction",
					tabTitle,
					null);
			stateItem = stateList.add(
					"tabSubItems",
					"",
					new StateList());
			appItem.saveControlsState(stateItem.subItems);
		}

		// active tab
		stateList.add(
				"tabActiveIndex",
				Integer.toString(tabPane_info.getSelectionModel().getSelectedIndex()),
				null);
	}

	/**
	 * рекурсивное сохранение развернутых разделов из дерева разделов
	 */
	private void addTreeItemStateRecursive(StateList stateList, TreeItem<SectionItem> ti) {

		//-------- проверяем и записываем развернутость итема
		if (ti.isExpanded()) {
			stateList.add(
					"TreeItemExpanded",
					Long.toString(ti.getValue().getId()),      // section id in DB
					null);
		}
		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<SectionItem> i : ti.getChildren()) {
			addTreeItemStateRecursive(stateList, i);
		}
	}

	/**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Восстанавливаем состояние контролов из иерархической структуры
	 */
	public void restoreControlsState (StateList stateList) {
		// for TreeColumn sort
		String sortColumnId = "";
		String sortType = "";
		// for TreeItems
		ObservableList<Long> listItemsForExpand = FXCollections.observableArrayList();
		Long selectedItemId = 0L;
		// for tabs
		String tabName = null;
		long tabAppItemId = 0;
		int tabDbConnId = 0;

		for (StateItem si : stateList.list) {
			switch (si.getName()) {
				//======== TreeTable sort column
				case "TreeTable_sortColumnId" :
					sortColumnId = si.getParams();
					break;
				case "TreeTable_sortType" :
					sortType = si.getParams();
					break;
				case "TreeTable_doSort" :
					treeTableView_sections.getSortOrder().clear();
					
					if (! sortColumnId.equals("")) {
						for (TreeTableColumn column : treeTableView_sections.getColumns()) {
							if (column.getId().equals(sortColumnId)) {
								treeTableView_sections.setSortMode(TreeSortMode.ALL_DESCENDANTS);
								column.setSortable(true); // This performs a sort
								treeTableView_sections.getSortOrder().add(column);
								if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
								else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
								treeTableView_sections.sort();
							}
						}
					}
					break;
			
				//======== TreeItems state
				case "TreeItemSelected" :
					selectedItemId = new Long(si.getParams());
					break;
				case "TreeItemExpanded":
					listItemsForExpand.add(new Long(si.getParams()));
					break;
				case "TreeItemsDoExpandAndSelected" :
					restoreTreeItemStateRecursive(listItemsForExpand,treeTableView_sections.getRoot());
					treeTableView_sections.sort();
					restoreTreeItemSelectedRecursive(selectedItemId,treeTableView_sections.getRoot());
					treeTableView_sections.sort();
					
					if (isTreeDynamicLoad) {
			    		TreeItem<SectionItem> mi = treeTableView_sections.getSelectionModel().getSelectedItem();
			    		if ((! mi.isExpanded()) && (params.getConCur().db.sectionGetNumberOfChildren(mi.getValue().getId()) > 0)) {
			    			mi.getChildren().add(treeViewCtrl.createItem_Loading());
			    		}
			    	}
					
					break;
				//======= Favorite
				case "favoriteSubItems" :
					controller_Favorite.restoreControlsState(si.subItems);
					break;
					
				//======== restore main split state
				case "splitPane_main_Position" :
					Platform.runLater(() -> {
						spliterWidth = Integer.parseInt(si.getParams());
			    		double position = getSpliterPos(spliterWidth); // Відносна позиція сплітера
			    	    splitPane_main.setDividerPositions(position); // Встановлення позиції
			    	    isSplitterPositionedFirst = true;
			    	    //System.out.println("Рестор сплітера: " + position + " (spliterWidth: " + spliterWidth + ")");
			    	});
					break;
				case "splitPane_main_fixMode" :
					toggleButton_fixSplitPaneMain.setSelected(
							(si.getParams().equals("1")) ? true : false
							);
					break;
					
				//======== tabs state
				case "tabName" :
					tabName = si.getParams();
					break;
				case "tabAppItemId" :
					tabAppItemId = Long.parseLong(si.getParams());
					break;
				case "tabDbConnId" :
					tabDbConnId = Integer.parseInt(si.getParams());
					break;
				case "tabCreateAction" :
					switch (tabName) {
						case AppItem_Interface.ELEMENT_DOCUMENT_VIEW :
						case AppItem_Interface.ELEMENT_DICTIONARY_VIEW :
							Params par = new Params(this.params);
							par.setObjContainer(this);
							par.setParentObj(this);
							par.setTabPane_Cur(tabPane_info);
							par.setStageCur(par.getMainStage());
							
							AppDataObj.openDocumentView (
									par,
									treeViewCtrl.getTreeItemById(treeViewCtrl.root, tabAppItemId));
							break;
						case AppItem_Interface.ELEMENT_INFO_EDIT :
							Params parIE = new Params(this.params);
							parIE.setObjContainer(this);
							parIE.setParentObj(this);
							parIE.setTabPane_Cur(tabPane_info);
							parIE.setStageCur(parIE.getMainStage());
							
							try {
								AppDataObj.openEditInfo (parIE, parIE.getConCur().db.infoGet(tabAppItemId));
							} catch (DataConnectionException | DataQueryException e) {
								e.writeLog(params);
								ShowAppMsg.showAlert(
					                    "ERROR", "Помилка при востановлені стану програми, дерево документів, "+
					                    "відкриття таба з інфо блоком.",
					                    Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
							}
							
							break;
					}
					break;
				case "tabRenameTitleAction" :
					// берем последний созданный таб и изменяем в нем заголовок 
					if (tabPane_info.getTabs().size() > 1) {
						String tabTitle = si.getParams();
						int tabIndex = tabPane_info.getTabs().size()-1;
						Tab curTab = tabPane_info.getTabs().get(tabIndex);
						HBox hbox = (HBox) curTab.getGraphic();
						Node nodeTitle = hbox.getChildren().get(1);
						
						((Label)nodeTitle).setText(tabTitle);
					}
					break;
				case "tabSubItems" :
					// берем последний созданный таб и вызываем в нем метод восстановления состояния
					//if (tabPane_info.getTabs().size() > 1) {
						int tabIndex = tabPane_info.getTabs().size()-1;
						AppItem_Interface ai =
								(AppItem_Interface)tabPane_info.getTabs().get(tabIndex).getUserData();
						ai.restoreControlsState(si.subItems);
					//}
					break;	
				case "tabActiveIndex" :
					tabPane_info.getSelectionModel().select(Integer.parseInt(si.getParams()));
					break;
			}
		}
	}

	/**
	 * рекурсивное восстановление состояния разделов дерева разделов
	 */
	private void restoreTreeItemStateRecursive(
			ObservableList<Long> listItemsForExpand,
			TreeItem<SectionItem> ti) {

		//-------- проверяем и разворачиваем текущий итем
		for (Long i : listItemsForExpand) {
			if (i == ti.getValue().getId()) {
				ti.setExpanded(true);
				if (isTreeDynamicLoad) {
					treeViewCtrl.createChildItems (ti);
				}
				break;
			}
		}
		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<SectionItem> i : ti.getChildren()) {
			restoreTreeItemStateRecursive(listItemsForExpand, i);
		}
	}

	/**
	 * рекурсивно ищем активный раздел в дереве разделов и выбираем его
	 */
	private void restoreTreeItemSelectedRecursive(
			Long selectedItemId,
			TreeItem<SectionItem> ti) {

		//---------- проверяем и выбираем текущий итем
		if (selectedItemId == ti.getValue().getId()) {
			treeTableView_sections.getSelectionModel().select(ti);
			
			int row = treeTableView_sections.getRow(ti);
			if (row >= 0) {
				treeTableView_sections.scrollTo(row);
			}
		}

		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<SectionItem> i : ti.getChildren()) {
			restoreTreeItemSelectedRecursive(selectedItemId, i);
		}
	}
	
	/**
     * Зберігає поточний документ з інфоблоками у буфері обміну
     * @param typeOper
     */
    private void saveToClipboard(int typeOper) {
    	TreeItem<SectionItem> selectedItem = treeTableView_sections.getSelectionModel().getSelectedItem();
    	SectionItem item;
    	SectionClipboardInfo clip;
    	
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
    	
    	if (item.getId() == 0) {
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
    	
    	// init info object and write
    	clip = new SectionClipboardInfo (
				typeOper, 
				params.getConCur().Id,
				params.getConCur().param.getConnName(),
				params.getConCur().db.sectionGetNumberOfInfoBlocks(item.getId())
				);
    	clip.serialize(path, SectionClipboardInfo.FILE_NAME_INFO);
    	
    	// write section
    	item.serialize(path, SectionClipboardInfo.FILE_NAME, SectionClipboardInfo.FILE_NAME_ICON);
    	
    	// write infoblocks
    	List<InfoHeaderItem> listInfoHeaders = params.getConCur().db.infoListBySectionId(item.getId());
    	int cnt = 0;
    	
    	for (InfoHeaderItem i : listInfoHeaders) {
			cnt++;
    		i.serialize(path, 
    				SectionClipboardInfo.FILE_PREFIX_INFO_HEADER+cnt+SectionClipboardInfo.FILE_POSTFIX);
    		
    		switch ((int)i.getInfoTypeId()) {
    		case 1 :    // Простий текст
    			Info_TextItem it = params.getConCur().db.info_TextGet(i.getInfoId());
    			it.serialize(path, 
        				SectionClipboardInfo.FILE_PREFIX_INFO_BLOCK+cnt+SectionClipboardInfo.FILE_POSTFIX);
    			break;
    		case 2 :    // Зображення
    			Info_ImageItem ii = params.getConCur().db.info_ImageGet(i.getInfoId());
    			ii.serialize(path, 
        				SectionClipboardInfo.FILE_PREFIX_INFO_BLOCK+cnt+SectionClipboardInfo.FILE_POSTFIX,
        				SectionClipboardInfo.FILE_PREFIX_INFO_FILE +cnt+SectionClipboardInfo.FILE_POSTFIX);
    			break;
    		case 3 :    // Файл
    			Info_FileItem ifl = params.getConCur().db.info_FileGet(i.getInfoId());
    			ifl.serialize(path, 
        				SectionClipboardInfo.FILE_PREFIX_INFO_BLOCK+cnt+SectionClipboardInfo.FILE_POSTFIX);
    			break;
    		default :
    			ShowAppMsg.showAlert("WARNING", "Copy/Cut to local clipboard", 
    					"Невідомий тип інфоблока", 
   		                "Тип інфоблока "+i.getInfoTypeId()+" не визначений");
    		}
		}
    }
    
    /**
     * берем значення позиції сплітера з конфіга (ширина лівої панелі в пікселях)
     */
    private int getSpliterWidthFromConfig () {
    	int spliterWidthDefault = 150;
    	String strSpliterWidth = params.getConfig().getItemValue("AppState", "sections.spliter.pos");
    	int spliterWidth;
    	
    	if (strSpliterWidth != null) {
    		try {
    			spliterWidth = Integer.parseInt(strSpliterWidth);
    		} catch (NumberFormatException e) {
    			spliterWidth = spliterWidthDefault;
    			ShowAppMsg.showAlert("WARNING", "Позиціонування сплітера", 
    					"Некоректне значення в конфігі, ставлю " + spliterWidthDefault, 
    					strSpliterWidth);
    		}
    	} else {
    		spliterWidth = spliterWidthDefault;
    		params.getConfig().add(
    				"AppState", 
    				"sections.spliter.pos",
    				"Вікно розділів документів, позиція сплітера в пікселях відносно лівої сторони",
    				"150",
    				LocalDate.now(),
    				true,
    				true
    				);
    	}
    	
    	return spliterWidth;
    }
    
    /**
     * повертає позицію сплітера відносно ширини лівої панелі
     */
    private double getSpliterPos (int width) {
    	double windowWidth = params.getStageCur().getWidth(); // Загальна ширина вікна
	    double position = width / windowWidth; // Відносна позиція сплітера
    	
    	return position;
    }
    
    /**
     * повертає ширину лівої панелі відносно позиції сплітера
     */
    private int getSpliterWidth (double pos) {
    	double windowWidth = params.getStageCur().getWidth(); // Загальна ширина вікна
	    double newWidth = pos * windowWidth; // Абсолютна ширина лівої панелі
    	
    	return (int) Math.round(newWidth);
    }

	/**
	 * Класс обработки дерева разделов
	 */
	public class TreeView_Controller {
		public TreeItem<SectionItem> root;
		public boolean dragStartedWithCtrl = false;

		/**
		 * инициализируем дерево, основной метод инициализации
		 */
		void init () {
			initCellValueFactory ();
			initColumnWidth ();

			initRoot ();
			if (! isTreeDynamicLoad) initTreeItemsRecursive(root);
			else addExpandHandler(root);

			initCellFactory();
			initRowFactory();

			// Слушаем изменения выбора, и при изменении отображаем информацию .
			treeTableView_sections.getSelectionModel().selectedItemProperty().addListener(
					(observable, oldValue, newValue) -> showSectionInfo(newValue));

			initSortColumn();
		}
		
		/**
		 * Создаем корневой раздел в дереве-контроле
		 */
		private void initRoot () {
			
			if (rootSectionId == 0) {
				try {
					root = new TreeItem<>(new SectionItem(
							0, 0, 0, "Усі", "це корінь, він не редагується",
							0, new Image("file:resources/images/icon_Sections_24.png"),
							params.getConCur().db.settingsGetValue("SECTION_TEMPLATE_MAIN_DEFAULT"), 1, 0,
							0,
							Long.parseLong(params.getConCur().db.settingsGetValue("SECTION_ICON_DEFAULT")),
							Long.parseLong(params.getConCur().db.settingsGetValue("SECTION_THEME_DEFAULT")),
							0,
							null, null, "", "", null));
				} catch (DataConnectionException | DataQueryException e) {
					e.writeLog(params);
					ShowAppMsg.showAlert(
							"ERROR", "Помилка при створенні кореня дерева Розділів, "+
							"помилка при читанні інформації з таблиці налаштувань",
							Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
				}
			} else {
				root = new TreeItem<>(params.getConCur().db.sectionGetById(rootSectionId));
			}
			if (isTreeDynamicLoad) {
				if (params.getConCur().db.sectionGetNumberOfChildren(root.getValue().getId()) > 0) {
					root.getChildren().add(createItem_Loading());
				}
			}
			
			treeTableView_sections.setShowRoot(true);
			treeTableView_sections.setRoot(root);
			root.setExpanded(false);
		}

		/**
		 * initCellValueFactory ()
		 */
		private void initCellValueFactory () {
			treeTableColumn_id.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getId()))
			);
			treeTableColumn_typeId.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(
									Integer.toString(param.getValue().getValue().getTypeId())
			));
			treeTableColumn_name.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getName())
			);
			treeTableColumn_iconId.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(
									(param.getValue().getValue().getIconId() == 0) ?
											"" : Long.toString(param.getValue().getValue().getIconId())
							));
			treeTableColumn_themeId.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(
									(param.getValue().getValue().getThemeId() == 0) ?
											"" : Long.toString(param.getValue().getValue().getThemeId())
							));
			treeTableColumn_description.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
			);
			treeTableColumn_dateCreated.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateCreated()))
			);
			treeTableColumn_dateModified.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateModified()))
			);
			treeTableColumn_userCreated.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getUserCreated())
			);
			treeTableColumn_userModified.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getUserModified())
			);
			treeTableColumn_dateModifiedInfo.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateModifiedInfo()))
			);
			
			treeTableColumn_iconIdDef.setCellValueFactory(
				    (TreeTableColumn.CellDataFeatures<SectionItem, Long> param) ->
				        new SimpleLongProperty(param.getValue().getValue().getIconIdDef()).asObject()
			);
			treeTableColumn_iconRoot.setCellValueFactory(
				    (TreeTableColumn.CellDataFeatures<SectionItem, Long> param) ->
				        new SimpleLongProperty(param.getValue().getValue().getIconIdRoot()).asObject()
			);

			treeTableColumn_styleMain.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getTemplateMain())
			);
			treeTableColumn_styleTree.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(
									Integer.toString(param.getValue().getValue().getTemplateMainTree())
			));
			treeTableColumn_styleRoot.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(
									(param.getValue().getValue().getTemplateMainRoot() == 0) ?
											"" : Long.toString(param.getValue().getValue().getTemplateMainRoot())
			));
		}

		/**
		 * initColumnWidth ()
		 */
		private void initColumnWidth () {
			// set/get Pref Width
			treeTableColumn_id.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_id__PrefWidth", 50));

			treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_id__PrefWidth", t1.doubleValue());
					//System.out.print(treeTableColumn_name.getText() + "  ");
					//System.out.println(t1);
				}
			});
			
			treeTableColumn_typeId.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_typeId__PrefWidth", 50));

			treeTableColumn_typeId.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_typeId__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_name.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_name__PrefWidth", 250));

			treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_name__PrefWidth", t1.doubleValue());
					//System.out.print(treeTableColumn_name.getText() + "  ");
					//System.out.println(t1);
				}
			});

			treeTableColumn_iconId.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_iconId__PrefWidth", 50));

			treeTableColumn_iconId.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_iconId__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_themeId.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_themeId__PrefWidth", 50));

			treeTableColumn_themeId.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_themeId__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_description.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_description__PrefWidth", 250));

			treeTableColumn_description.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_description__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_dateCreated.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_dateCreated__PrefWidth", 150));

			treeTableColumn_dateCreated.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_dateCreated__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_dateModified.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_dateModified__PrefWidth", 150));

			treeTableColumn_dateModified.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_dateModified__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_userCreated.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_userCreated__PrefWidth", 150));

			treeTableColumn_userCreated.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_userCreated__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_userModified.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_userModified__PrefWidth", 150));

			treeTableColumn_userModified.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_userModified__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_dateModifiedInfo.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_dateModifiedInfo__PrefWidth", 150));

			treeTableColumn_dateModifiedInfo.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_dateModifiedInfo__PrefWidth", t1.doubleValue());
				}
			});
			
			treeTableColumn_iconRoot.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_iconRoot__PrefWidth", 50));

			treeTableColumn_iconRoot.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_iconRoot__PrefWidth", t1.doubleValue());
				}
			});
			
			treeTableColumn_iconIdDef.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_iconIdDef__PrefWidth", 50));

			treeTableColumn_iconIdDef.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_iconIdDef__PrefWidth", t1.doubleValue());
				}
			});
			
			treeTableColumn_styleMain.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_styleMain__PrefWidth", 150));

			treeTableColumn_styleMain.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_styleMain__PrefWidth", t1.doubleValue());
				}
			});
			
			treeTableColumn_styleTree.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_styleTree__PrefWidth", 150));

			treeTableColumn_styleTree.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_styleTree__PrefWidth", t1.doubleValue());
				}
			});
			
			treeTableColumn_styleRoot.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_styleRoot__PrefWidth", 150));

			treeTableColumn_styleRoot.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_styleRoot__PrefWidth", t1.doubleValue());
				}
			});
		}

		/**
		 * Инициализация TreeTableView. Рекурсия по дереву.
		 */
		private void initTreeItemsRecursive(TreeItem<SectionItem> ti) {
			SectionItem f = ti.getValue();

			if (f != null) {
				List<SectionItem> sectionsList = params.getConCur().db.sectionListByParentId (f.getId());

				for (SectionItem i : sectionsList) {
					TreeItem<SectionItem> subItem = new TreeItem<>(i);
					ti.getChildren().add(subItem);

					initTreeItemsRecursive(subItem);
				}
			}
		}

		/**
		 * CellFactory - показ иконок
		 */
		public void initCellFactory () {
			treeTableColumn_name.setCellFactory(ttc -> new TreeTableCell<SectionItem, String>() {
				private SectionItem row;
				private ImageView graphic;

				@Override
				protected void updateItem(String item, boolean empty) {    // display graphic
					try {
						row = getTreeTableRow().getItem();
						if ((row.getIconId() > 0) || (row.getId() <= 0)) {
							graphic = new ImageView(row.icon);
						} else {                                    // show default icon
							long iconIdDef = params.getConCur().db.sectionGetIconIdDefault(row.getParentId(), true);
							graphic = new ImageView(params.getConCur().db.iconGetImageById(iconIdDef));
						}
					} catch (NullPointerException e) {
						//e.printStackTrace();
						graphic = null;
					}

					super.updateItem(item, empty);
					setText(empty ? null : item);
					setGraphic(empty ? null : graphic);
				}
			});
			
			treeTableColumn_iconIdDef.setCellFactory(
				    new Callback<TreeTableColumn<SectionItem, Long>, TreeTableCell<SectionItem, Long>>() {
				        @Override
				        public TreeTableCell<SectionItem, Long> call(TreeTableColumn<SectionItem, Long> param) {
				            return new TreeTableCell<SectionItem, Long>() {
				            	private ImageView graphic;

				            	{
				                    // Встановлюємо відображення по центру
				                    setAlignment(Pos.CENTER);
				                    setContentDisplay(ContentDisplay.CENTER);
				                }
				            	
				                @Override
				                protected void updateItem(Long item, boolean empty) {
				                	super.updateItem(item, empty);
				                    if (item != null && !empty && item != 0) {
				                    	graphic = new ImageView(params.getConCur().db.iconGetImageById(item));
				                    	graphic.setFitWidth(16);
				                    	graphic.setFitHeight(16);
				                    	setGraphic(graphic);
				                    } else {
				                        setGraphic(null);
				                    }
				                }
				            };
				        }
				    }
			);
			
			treeTableColumn_iconRoot.setCellFactory(
				    new Callback<TreeTableColumn<SectionItem, Long>, TreeTableCell<SectionItem, Long>>() {
				        @Override
				        public TreeTableCell<SectionItem, Long> call(TreeTableColumn<SectionItem, Long> param) {
				            return new TreeTableCell<SectionItem, Long>() {
				            	private ImageView graphic;

				            	{
				                    // Встановлюємо відображення по центру
				                    setAlignment(Pos.CENTER);
				                    setContentDisplay(ContentDisplay.CENTER);
				                }
				            	
				                @Override
				                protected void updateItem(Long item, boolean empty) {
				                	super.updateItem(item, empty);
				                    if (item != null && !empty && item != 0) {
				                    	graphic = new ImageView(params.getConCur().db.iconGetImageById(item));
				                    	graphic.setFitWidth(16);
				                    	graphic.setFitHeight(16);
				                        setGraphic(graphic);
				                    } else {
				                        setGraphic(null);
				                    }
				                }
				            };
				        }
				    }
			);
			
			treeTableColumn_styleTree.setCellFactory(
				    new Callback<TreeTableColumn<SectionItem, String>, TreeTableCell<SectionItem, String>>() {
				        @Override
				        public TreeTableCell<SectionItem, String> call(TreeTableColumn<SectionItem, String> param) {
				            return new TreeTableCell<SectionItem, String>() {
				            	private CheckBox checkBox = new CheckBox();

				            	{
				                    // Встановлюємо відображення по центру
				                    setAlignment(Pos.CENTER);
				                    setContentDisplay(ContentDisplay.CENTER);
				                }
				            	
				                @Override
				                protected void updateItem(String item, boolean empty) {
				                	super.updateItem(item, empty);
				                    if (item != null && !empty) {
				                        checkBox.setSelected(item.equals("1"));
				                        checkBox.setDisable(true);
				                        setGraphic(checkBox);
				                    } else {
				                        setGraphic(null);
				                    }
				                }
				            };
				        }
				    }
			);
		}

		/**
		 * RowFactory - for Drag&Drop and tooltip
		 */
		void initRowFactory () {
			treeTableView_sections.setRowFactory(new Callback<TreeTableView<SectionItem>,
					TreeTableRow<SectionItem>>() {
				@Override
				public TreeTableRow<SectionItem> call(final TreeTableView<SectionItem> param) {
					final TreeTableRow<SectionItem> row = new TreeTableRow<SectionItem>();

					WebView webView = new WebView();
					Tooltip tooltip = new Tooltip();

		            row.hoverProperty().addListener((observable, oldValue, newValue) -> {
		                if (row.getItem() != null) {
		                    //tooltip.setText(row.getItem().getName());
		                	
		                	String htmlContent = 
		                			"<html>" +
		                			"<body>" + 
		                			"<p style='font-size: 11pt; padding:0px; margin:0px;'>"+ 
		                				row.getItem().getName() +" ("+ row.getItem().getId() +")</p>"+
		                			"<p style='font-size: 11pt; padding:0px; margin:0px;'>"+ 
		                				row.getItem().getDescr() +"</p>"+
		                			"<p style='font-size: 11pt; padding:0px; margin:0px;'>"+ 
										dateConv.dateTimeToStr(row.getItem().getDateCreated()) +"  - створений</p>"+
									"<p style='font-size: 11pt; padding:0px; margin:0px;'>"+ 
										dateConv.dateTimeToStr(row.getItem().getDateModified()) +"  - модифікований</p>"+
									"<p style='font-size: 11pt; padding:0px; margin:0px;'>"+ 
										dateConv.dateTimeToStr(row.getItem().getDateModifiedInfo()) +"  - інформація</p>"+
		                			"</body></html>";
		                    webView.getEngine().loadContent(htmlContent);
		                    webView.setPrefHeight(100);
		                    tooltip.setGraphic(webView);
		                    Tooltip.install(row, tooltip);
		                }
		            });
					
					row.setOnDragDetected(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							// drag was detected, start drag-and-drop gesture
							TreeItem<SectionItem> selected =
									(TreeItem<SectionItem>) treeTableView_sections.getSelectionModel().getSelectedItem();

							if (selected != null) {
								dragStartedWithCtrl = event.isControlDown();
								Dragboard db = row.startDragAndDrop(TransferMode.ANY);

								// create a miniature of the row you're dragging
								db.setDragView(row.snapshot(null, null));

								// Keep whats being dragged on the clipboard
								ClipboardContent content = new ClipboardContent();
								content.put(params.getMain().SERIALIZED_MIME_TYPE, row.getIndex());
								db.setContent(content);

								event.consume();
							}
						}
					});

					row.setOnDragOver(new EventHandler<DragEvent>() {
						@Override
						public void handle(DragEvent event) {
							// data is dragged over the target
							Dragboard db = event.getDragboard();

							if (dragAndDropAcceptable(db, row)) {
								event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
								event.consume();
							}
						}});

					row.setOnDragDropped(new EventHandler<DragEvent>() {
						@Override
						public void handle(DragEvent event) {
							Dragboard db = event.getDragboard();

							if (dragAndDropAcceptable(db, row)) {
								int index = (Integer) db.getContent(params.getMain().SERIALIZED_MIME_TYPE);
								TreeItem<SectionItem> item = treeTableView_sections.getTreeItem(index);

								TransferMode actionTransferMode = event.getAcceptedTransferMode();
								if (System.getProperty("os.name").toLowerCase().contains("linux")) {
									actionTransferMode = dragStartedWithCtrl ? TransferMode.COPY : TransferMode.MOVE;
								}

								if (actionTransferMode == TransferMode.MOVE) {
									if (ShowAppMsg.showQuestion("CONFIRMATION", "Переміщення розділу",
											"Переміщення розділу '" + item.getValue().getName() + "'", "Перемістити розділ ?")) {
										// update in DB
										params.getConCur().db.sectionMove (item.getValue().getId(), dragAndDropGetTarget(row).getValue().getId());
										
										//---- move in tree-control
										item.getParent().getChildren().remove(item);
										dragAndDropGetTarget(row).getChildren().add(item);
										event.setDropCompleted(true);
										
										// вибираємо батьківський
										treeTableView_sections.getSelectionModel().select(
												getTreeItemById(root, dragAndDropGetTarget(row).getValue().getId()));
										// експандимо (розкриваємо) батьківський
										dragAndDropGetTarget(row).setExpanded(true);
										// рефрешимо батьківський як активний
										///handleButtonRefreshTree();
										// вибираємо переміщений
										treeTableView_sections.getSelectionModel().select(
												getTreeItemById(root, item.getValue().getId()));
										// сортуємо
										treeTableView_sections.sort();
										
										// выводим сообщение в статус бар
										params.setMsgToStatusBar("Раздел '" + item.getValue().getName() + "' перемещен.");
									}
								} else if (actionTransferMode == TransferMode.COPY) {
									Preferences prefs = Preferences.userNodeForPackage(SectionList_Controller.class);
									boolean copyWithSubSections = prefs.get("copyWithSubSections", "No").equals("Yes");
									int retVal = ShowAppMsg.showQuestionWithOption(
											"CONFIRMATION", "Копіювання розділу",
											"Копіювати розділ '"+ item.getValue().getName() +"' ?", null,
											"Копіювати гілку повністю", copyWithSubSections);
									long newSectionId = 0;

									if (retVal == ShowAppMsg.QUESTION_OK) {          // сохраняем только текущий итем
										event.setDropCompleted(true);

										newSectionId = copySection (item.getValue(), dragAndDropGetTarget(row), false);

										// save Option value
										prefs.put("copyWithSubSections", "No");

										// выводим сообщение в статус бар
										params.setMsgToStatusBar("Раздел '" + item.getValue().getName() + "' скопирован.");
									}
									if (retVal == ShowAppMsg.QUESTION_OK_WITH_OPTION) {          // сохраняем всю ветку
										event.setDropCompleted(true);

										// item                          - source TreeItem
										// getTarget_forDragAndDrop(row) - target parent TreeItem
										newSectionId = copySection (item.getValue(), dragAndDropGetTarget(row), true);

										// save Option value
										prefs.put("copyWithSubSections", "Yes");

										// выводим сообщение в статус бар
										params.setMsgToStatusBar("Раздел (ветка) '" + item.getValue().getName() + "' скопирован.");
									}
									treeTableView_sections.sort();
									expandTreeItemById(root, newSectionId);
									selectTreeItemById(root, newSectionId);
								} else {
									ShowAppMsg.showAlert("WARNING", "Перетаскивание", "Не известный режим перетаскивания", "Не обрабатывается.");
								}
								event.consume();
							}
						}});

					return row;
				}
			});
		}

		/**
		 * восстанавливаем сортировку таблицы по столбцу
		 */
		public void initSortColumn () {
			String sortColumnId = prefs.get("SectionsList_sortColumnId","");

			if (! sortColumnId.equals("")) {
				for (TreeTableColumn column : treeTableView_sections.getColumns()) {
					if (column.getId().equals(sortColumnId)) {
						String sortType = prefs.get("SectionsList_sortType","ASCENDING");

						treeTableView_sections.setSortMode(TreeSortMode.ALL_DESCENDANTS);
						column.setSortable(true); // This performs a sort
						treeTableView_sections.getSortOrder().add(column);
						if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
						else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
						treeTableView_sections.sort();
					}
				}
			}
		}

		/**
		 * Возвращает Истину, если перетаскивание возможно, иначе Ложь.
		 */
		private boolean dragAndDropAcceptable(Dragboard db, TreeTableRow<SectionItem> row) {
			boolean result = false;
			if (db.hasContent(params.getMain().SERIALIZED_MIME_TYPE)) {
				int index = (Integer) db.getContent(params.getMain().SERIALIZED_MIME_TYPE);
				if (row.getIndex() != index) {
					TreeItem<SectionItem> target = dragAndDropGetTarget(row);
					TreeItem<SectionItem> item = treeTableView_sections.getTreeItem(index);
					result = !dragAndDropIsParent(item, target);
				}
			}
			return result;
		}

		/**
		 * Получаем строчку-приемник при перетаскивании
		 */
		private TreeItem<SectionItem> dragAndDropGetTarget(TreeTableRow<SectionItem> row) {
			TreeItem<SectionItem> target = treeTableView_sections.getRoot();
			if (!row.isEmpty()) {
				target = row.getTreeItem();
			}
			return target;
		}

		/**
		 * prevent loops in the tree
		 */
		private boolean dragAndDropIsParent(TreeItem<SectionItem> parent, TreeItem<SectionItem> child) {
			boolean result = false;
			while (!result && child != null) {
				result = child.getParent() == parent;
				child = child.getParent();
			}
			return result;
		}

		/*
		 * Возвращает строку иерархического пути раздела
		 *
		 * ti - текущий раздел
		 * level - 0 - показывает без текущего раздела ; 1 - с текущим разделом
		 */
		public String getSectionPath (TreeItem<SectionItem> ti, int level) {
			String msg = null;
			boolean isFirst = true;

			if (ti != null) {
				SectionItem f = (SectionItem) ti.getValue();

				if (level == 1) msg = new String(f.getName());
				if (f.getId() != rootSectionId) {
					TreeItem<SectionItem> curTI = ti.getParent();
					SectionItem curSection = curTI.getValue();

					while (curSection.getId() != rootSectionId) {
						if ((level == 1) || (! isFirst)) msg = curSection.getName() + " / " + msg;
						else {
							msg = curSection.getName();
							isFirst = false;
						}

						if (curSection.getId() == 0)   break;
						else {
							curTI = curTI.getParent();
							curSection = curTI.getValue();
						}
					}
					
					msg = curSection.getName() + " / " + msg;
				}
			}
			return msg;
		}

		/**
		 * Раскрываем в дереве разделов раздел по его Id
		 */
		private int expandTreeItemById(TreeItem<SectionItem> ti, long selId) {
			SectionItem si = ti.getValue();

			if (si.getId() == selId) {
				//treeTableView_sections.getSelectionModel().select(ti);
				return 1;
			}

			//-------- выбираем дочерние итемы и запускаем рекурсию
			for (TreeItem<SectionItem> i : ti.getChildren()) {
				if (expandTreeItemById(i, selId) == 1) {
					ti.setExpanded(true);
					return 1;
				}
			}

			return 0;
		}
		
		/**
		 * Розкриваємо в дереві розділів ланцюжок розділів по списку їх id.
		 * При необхідності вибираємо останній розділ в ланцюжку.
		 * @param sectionPath
		 */
		private void expandTreeItemsByIds (List<Long> sectionPath, boolean doSelect) {
			TreeItem<SectionItem> ti = root;
			boolean isRoot = true;

			// чекаємо якщо зараз йде динамічне завантаження гілки
			if (isTreeLoading) {
				if (retryCount++ < MAX_RETRIES) {
					Platform.runLater(() -> expandTreeItemsByIds(sectionPath, doSelect));
				} else {
					System.out.println("Navigation timeout");
					retryCount = 0;
				}
				return;
			}
			retryCount = 0;

			//
			isGotoSection = true;

			// розкриваємо гілку
			for (Long id : sectionPath) {
				if (isRoot) {
					ti.setExpanded(true);
					isRoot = false;
				} else {
					for (TreeItem<SectionItem> child : ti.getChildren()) {
						if (child.getValue().getId() == id) {
							ti = child;
							ti.setExpanded(true);
							//System.out.println("- " + ti.getValue().getId());
							break; // важливо!
						}
					}
				}
			}

			// вибираємо вказаний елемент
			TreeItem<SectionItem> finalTi = ti; //  копія
			PauseTransition pause = new PauseTransition(Duration.millis(100));
			pause.setOnFinished(e -> {
				//System.out.println("finalTi(id) = "+ finalTi.getValue().getId());
				if (doSelect && finalTi != null) {
					Platform.runLater(() -> {
						treeTableView_sections.getSelectionModel().select(finalTi);

						Platform.runLater(() -> {
							int row = treeTableView_sections.getRow(finalTi);
							if (row >= 0) {
								treeTableView_sections.scrollTo(row);
							}

							isGotoSection = false;
						});
					});
					tabPane_info.getSelectionModel().select(tab_DocCur);
				}
			});

			pause.play();
		}
		
		/**
		 * Выбираем в дереве разделов раздел по его Id
		 */
		public TreeItem<SectionItem> getTreeItemById(TreeItem<SectionItem> ti, long selId) {
			SectionItem si = ti.getValue();

			if (si.getId() == selId) {
				return ti;
			}

			//-------- выбираем дочерние итемы и запускаем рекурсию
			for (TreeItem<SectionItem> i : ti.getChildren()) {
				TreeItem<SectionItem> tsi = getTreeItemById(i, selId);
				
				if (tsi != null)
					return tsi;
			}

			return null;
		}

		/**
		 * Выбираем в дереве разделов текущий раздел по его Id
		 */
		int selectTreeItemById(TreeItem<SectionItem> ti, long selId) {
			SectionItem si = ti.getValue();

			if (si.getId() == selId) {
				treeTableView_sections.getSelectionModel().select(ti);
				return 1;
			}

			//-------- выбираем дочерние итемы и запускаем рекурсию
			for (TreeItem<SectionItem> i : ti.getChildren()) {
				if (selectTreeItemById(i, selId) == 1)
					return 1;
			}

			return 0;
		}

		/**
		 * Копируем рекурсивно ветку разделов или один раздел
		 * cpyCII - копируемый раздел
		 * trgTI - элемент дерева-раздела, в который копировать
		 */
		private long copySection (
				SectionItem cpyCII, TreeItem<SectionItem> trgTI, boolean isRecursive) {

			if (trgTI.getValue() == null)  return 0;

			// создаем новый обьект раздела для копирования
			SectionItem curCII = new SectionItem(cpyCII);
			curCII.setId(params.getConCur().db.sectionNextId());
			curCII.setParentId(trgTI.getValue().getId());
			//curCII.setDateCreated(new Date());
			//curCII.setDateModified(new Date());
			//curCII.setDateModifiedInfo(new Date());

			// раздел и его инфо блоки добавляем в БД
			params.getConCur().db.sectionAdd(curCII);
			params.getConCur().db.sectionCopyInfoBlocks(cpyCII.getId(), curCII.getId());
			//params.getConCur().db.sectionUpdateDateModifiedInfo(curCII.getId());

			// добавляем к новому родителю
			TreeItem<SectionItem> curTI = new TreeItem<>(curCII);
			trgTI.getChildren().add(curTI);

			// в цикле вызываем рекурсивный метод
			if (isRecursive) {
				List<SectionItem> sectionsList = params.getConCur().db.sectionListByParentId(cpyCII.getId());

				for (SectionItem i : sectionsList) {
					copySection (i, curTI, isRecursive);
				}
			}
			
			return curCII.getId();
		}
		
		/**
		 * Додавання обробника розгортання до вказаного вузла 
		 * @param item
		 */
	    private void addExpandHandler(TreeItem<SectionItem> item) {
	        item.addEventHandler(TreeItem.branchExpandedEvent(), event -> {
	        	Object source = event.getSource();
	            if (source instanceof TreeItem) {
	                TreeItem<SectionItem> expandedItem = (TreeItem<SectionItem>) source;
	                createChildItems (expandedItem);
	            }
	        });
	    }
	    
	    /**
	     * Створює дочірні розділи в дереві-контролі при динамічному завантаженню
	     */
	    void createChildItems(TreeItem<SectionItem> ti) {
	        if (!ti.getValue().isChildLoaded()) {
	        	// зберігаємо активний елемент дерева
	        	TreeItem<SectionItem> selectedItem = treeTableView_sections.getSelectionModel().getSelectedItem();
            	treeViewSelectedItemId = selectedItem != null ? selectedItem.getValue().getId() : -1;
	        	
	        	// Завантажуємо гілку
	            List<SectionItem> sectionsList = params.getConCur().db.sectionListByParentId(ti.getValue().getId());

	            ti.getChildren().clear();  // delete item "loading"

	            for (SectionItem i : sectionsList) {
	                TreeItem<SectionItem> subItem = new TreeItem<>(i);
	                ti.getChildren().add(subItem);

	                if (params.getConCur().db.sectionGetNumberOfChildren(i.getId()) > 0) {
	                    subItem.getChildren().add(createItem_Loading());
	                }
	            }

	            ti.getValue().setChildLoaded(true);

	            // Безпечне сортування після оновлення дерева
	            Platform.runLater(() -> {
	                try {
	                    // Перевірити, чи є хоча б один стовпець для сортування
	                    if (!treeTableView_sections.getSortOrder().isEmpty()) {
	                        treeTableView_sections.sort();
	                    }
	                } catch (Exception e) {
	                    System.out.println("Помилка сортування гілки розділів: " + e.getMessage());
	                }
	            });
	            
	            // Відновлюємо активний елемент
	            Platform.runLater(() -> {
	                try {
	                    TreeItem<SectionItem> curSelected = treeTableView_sections.getSelectionModel().getSelectedItem();
	                    long curSelectedId = curSelected != null ? curSelected.getValue().getId() : -1;
	                    
	                    if ((treeViewSelectedItemId != -1) && 
	        	           	(treeViewSelectedItemId != curSelectedId)) {		
	                    		
	                        TreeItem<SectionItem> restoredItem = getTreeItemById(treeTableView_sections.getRoot(), treeViewSelectedItemId);
	                        if (restoredItem != null) {
                                treeTableView_sections.getSelectionModel().select(restoredItem);
	                        }
	                    }
	                } catch (Exception e) {
	                	System.out.println("Помилка відновлення активного елемента");
	                }
	            });
	        }
	    }

	    /**
	     * створюємо технічний елемент "loading..."
	     */
		TreeItem<SectionItem> createItem_Loading () {
			TreeItem<SectionItem> ti = null;

			try {
				ti = new TreeItem<>(new SectionItem(
						-1, -1, 1, "loading...", "зачекайте, триває загрузка",
						0, new Image("file:resources/images/icon_load_process_16.png"),
						params.getConCur().db.settingsGetValue("SECTION_TEMPLATE_MAIN_DEFAULT"), 1, 0,
						0,
						Long.parseLong(params.getConCur().db.settingsGetValue("SECTION_ICON_DEFAULT")),
						Long.parseLong(params.getConCur().db.settingsGetValue("SECTION_THEME_DEFAULT")),
						0,
						null, null, "", "", null));
			} catch (DataConnectionException | DataQueryException e) {
				e.writeLog(params);
				ShowAppMsg.showAlert(
						"ERROR", "Помилка при створенні технічного елемента \"loading...\" в дереві Розділів, "+
						"помилка при читанні інформації з таблиці налаштувань",
						Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
			}

			return ti;
		}

		/**
		 * Переходимо на вказаний розділ в дереві
		 * @param sectionId
		 */
		public void gotoSection (long sectionId) {
			boolean isPresent = false;

			//---- перевіряємо чи присутній розділ в дереві
			try {
				isPresent = params.getConCur().db.sectionIsPresentInTree(rootSectionId, sectionId);
			} catch (DataConnectionException | DataQueryException e) {
				e.writeLog(params);
				ShowAppMsg.showAlert(
						"ERROR", "Помилка при переході на вказаний розділ, "+
						"при пошуку Розділа в дереві.",
						Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
			}
			if (! isPresent) {
				ShowAppMsg.showAlert("INFORMATION", "Повідомлення",
						"Вказаний розділ не знайдений в дереві.",
						"Перехід неможливий.");
				return;
			}

			//---- знаходимо шлях до ітема, розгортаємо та вибираємо




			try {
				// 1. Будуємо список ID для шляху від цільового розділу до кореня
				List<Long> pathIds = new java.util.ArrayList<>();
				long currentId = sectionId;

				while (true) {
					pathIds.add(0, currentId);
					if (currentId == rootSectionId) break;

					SectionItem si = params.getConCur().db.sectionGetById(currentId);
					if (si == null) break;
					currentId = si.getParentId();
				}

				// 2. Покроково проходимо по дереву від root
				TreeItem<SectionItem> currentTI = root;
				for (Long id : pathIds) {
					// Якщо поточний TreeItem не має потрібного ID, шукаємо його серед дочірніх елементів
					if (currentTI.getValue().getId() != id) {
						TreeItem<SectionItem> foundChild = null;
						for (TreeItem<SectionItem> child : currentTI.getChildren()) {
							if (child.getValue().getId() == id) {
								foundChild = child;
								break;
							}
						}

						if (foundChild != null) {
							currentTI = foundChild;
						} else {
							return; // Елемент не знайдено в ієрархії
						}
					}

					// Розгортаємо вузол та підвантажуємо дані для динамічного дерева
					if (id != sectionId) {
						currentTI.setExpanded(true);
						if (isTreeDynamicLoad) {
							createChildItems(currentTI);
						}
					}
				}

				// 3. Виділяємо знайдений розділ та фокусуємося на ньому
				final TreeItem<SectionItem> targetTI = currentTI;
				Platform.runLater(() -> {
					treeTableView_sections.getSelectionModel().select(targetTI);
					int index = treeTableView_sections.getSelectionModel().getSelectedIndex();
					if (index >= 0) {
						treeTableView_sections.scrollTo(index);
					}
				});

			} catch (Exception e) {
				params.setMsgToStatusBar("Помилка навігації до розділу: " + e.getMessage());
			}
		}
	}
}
