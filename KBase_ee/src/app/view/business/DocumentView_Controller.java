
package app.view.business;

import app.Main;
import app.exceptions.DataConnectionException;
import app.exceptions.DataQueryException;
import app.exceptions.KBase_DbConnEx;
import app.exceptions.KBase_Ex;
import app.exceptions.KBase_HtmlCompileEx;
import app.lib.AppDataObj;
import app.lib.DateConv;
import app.lib.FileCache;
import app.lib.FromJS_InfoFile;
import app.lib.FromJS_Preferences;
import app.lib.HtmlCompile;
import app.lib.ShowAppMsg;
import app.model.AppItem_Interface;
import app.model.Params;
import app.model.StateItem;
import app.model.StateList;
import app.model.business.DocumentItem;
import app.model.business.InfoHeaderItem;
import app.model.business.SectionItem;
import app.model.business.template.TemplateFileItem;
import app.model.business.template.TemplateThemeItem;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import netscape.javascript.JSObject;

/**
 * Контроллер фрейма показа документа. Показывает список инфо блоков и готовый документ.
 * @author Igor Makarevich
 * v.1.00.01.005 2025-01-20
 */
public class DocumentView_Controller implements AppItem_Interface {
	
	private Params params;
	
    /**
     * Раздел отображаемого документа
     */
    private long sectionId;
    /**
     * Ковертор даты/времени
     */
    private DateConv dateConv;
    
    @FXML
	private AnchorPane anchorPane_Main;
    @FXML
    private AnchorPane anchorPane_DocView;
    @FXML
    private WebView webView_current;
    
    @FXML
    private Button button_refresh;
    @FXML
    private Button button_AddInfoBefore;
    @FXML
    private Button button_AddInfoLast;
    @FXML
    private Button button_EditInfo;
	@FXML
	private Button button_EditInfo2;
	@FXML
	private Button button_EditInfo3;
    @FXML
    private Button button_DeleteInfo;
    @FXML
    private Button button_ChangeInfoListOrientation;
    @FXML
    private ToggleButton toggleButton_fixSplitPane;
    @FXML
    private Button button_Close;
    
    @FXML
    private SplitPane splitPane_info;
    
    @FXML
	public TreeTableView<InfoHeaderItem> treeTableView_InfoHeader;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_id;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_name;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_descr;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_position;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_type;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_style;
	@FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_infoId;
    @FXML
    private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_dateCreated;
    @FXML
    private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_dateModified;
    @FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_userCreated;
    @FXML
	private TreeTableColumn<InfoHeaderItem, String> treeTableColumn_userModified;
    
    @FXML
	private MenuItem menuitem_AddInfoBefore;
    @FXML
	private MenuItem menuitem_AddInfoLast;
    @FXML
	private MenuItem menuitem_EditInfo;
    @FXML
	private MenuItem menuitem_DeleteInfo;
    
    // можно ли закрывать данную сцену
    private boolean canClose;

    /**
     * Корень в дереве-контроле списка инфо блоков
     */
    TreeItem<InfoHeaderItem> rootInfoList;
    /**
     * 
     */
    WebEngine webEngine;
    /**
     * for debug
     */
    private boolean isDebug = false;
    String strDebugForShow;
    
    //
    private Preferences prefs = Preferences.userNodeForPackage(DocumentView_Controller.class);
    
    // флаг/id для листенера - нужно ли переходить в документе на текущий инфо блок
    private long isReloadedId = -1;
    
    // прапорець що сплітер був позіціонований перший раз, до цього не зберігати позицію сплітера
 	private boolean isSplitterPositionedFirst;
 	// позиція сплітера в пікселях відносно лівої/нижньої сторони
 	int spliterWidth;
    
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public DocumentView_Controller () {
    	dateConv = new DateConv();
    	isSplitterPositionedFirst = false;
    }
	
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
    	//======== init webView_current
    	anchorPane_DocView.getStyleClass().add("WevView_Doc_Pain");
    	webEngine = webView_current.getEngine();
    	
    	webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {
        		//-------- Executing Java From JavaScript
        		final JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaInfoFile", new FromJS_InfoFile(params));
                
                final JSObject doc = (JSObject) window.getMember("document");
                FromJS_Preferences jsPref = new FromJS_Preferences();
                doc.setMember("javaPreferences", jsPref);

            	//-------- Show debug information
            	if (isDebug) {
            		webEngine.executeScript("document.body.innerHTML += '"+ strDebugForShow +"' ");
            	}
            	
            	//-------- go to current pos
            	if (isReloadedId != -1) {
            		webEngine.executeScript("scrollToElement(\""+ isReloadedId +"\")");
            		isReloadedId = -1;
            	}
            }
        });
    }
    
    /**
     * Вызывается родительским обьектом, который передает параметры.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params, boolean canClose) {
    	this.params = params;
    	this.canClose = canClose;
    	
        // init controls
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями 
     */
    private void initControlsValue() {
    	
    	//======== splitPane_info
    	// Встановлюємо орієнтацію
    	String orientation = prefs.get("DocumentView_splitPane_info_orientation", "HORIZONTAL");
		if (orientation.equals("HORIZONTAL")) {
			splitPane_info.setOrientation(Orientation.HORIZONTAL);
		} else {
			splitPane_info.setOrientation(Orientation.VERTICAL);
		}
    	
		// беремо значення позиції сплітера з конфіга
    	spliterWidth = getSpliterWidthFromConfig();
		
    	// Встановлення початкового розміру після побудови.
    	// Використовуємо applySpliterPos з retry-логікою (на Linux getWidth() може бути 0).
    	applySpliterPos(spliterWidth);

    	// Слухач для збереження позиції
    	splitPane_info.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
    	    if (!toggleButton_fixSplitPane.isSelected()) {
    	    	if (isSplitterPositionedFirst) {
    	    		spliterWidth = getSpliterWidth(newValue.doubleValue());
    	    	}
    	    } else {
   	    	    double position = getSpliterPos(spliterWidth); // Відносна позиція сплітера
   	    	    splitPane_info.setDividerPositions(position); // Встановлення позиції
   	    	    isSplitterPositionedFirst = true;
    	    }
    	});
    	
    	//======== hot keys
    	anchorPane_Main.setOnKeyPressed(event -> {
    		if (event.getCode() == KeyCode.R && event.isControlDown()) {
    	        handleButtonRefresh();
    	    }
    		// розблокуємо сплітер
    		if (event.getCode() == KeyCode.U && event.isControlDown()) {
    			toggleButton_fixSplitPane.setSelected(false);
    			
    			spliterWidth = getSpliterWidthFromConfig();
    			double pos = getSpliterPos(spliterWidth);
    			splitPane_info.setDividerPositions(pos);
    		}
    	});
    	
    	//======== ToolBar
    	button_refresh.setTooltip(new Tooltip("Оновити документ (Ctrl+R)"));
    	button_refresh.setGraphic(new ImageView(new Image("file:resources/images/icon_refresh_16.png")));
    	button_AddInfoBefore.setTooltip(new Tooltip("Добавить новый блок перед текущим..."));
    	button_AddInfoBefore.setGraphic(new ImageView(new Image("file:resources/images/icon_insert_up_16.png")));
    	button_AddInfoLast.setTooltip(new Tooltip("Добавить новый блок в конец списка..."));
    	button_AddInfoLast.setGraphic(new ImageView(new Image("file:resources/images/icon_insert_down_16.png")));
    	button_EditInfo.setTooltip(new Tooltip("Редактирование инфо блока...\n во внутреннем табе"));
    	button_EditInfo.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_1_16.png")));
		button_EditInfo2.setTooltip(new Tooltip("Редактирование инфо блока...\n в главном табе"));
		button_EditInfo2.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_2_16.png")));
		button_EditInfo3.setTooltip(new Tooltip("Редактирование инфо блока...\n в отдельном окне"));
		button_EditInfo3.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_3_16.png")));
    	button_DeleteInfo.setTooltip(new Tooltip("Удаление инфо блока"));
    	button_DeleteInfo.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	
    	button_ChangeInfoListOrientation.setTooltip(new Tooltip("Змінити орієнтацію списка інфоблоків"));
    	button_ChangeInfoListOrientation.setGraphic(new ImageView(new Image("file:resources/images/icon_orientation_16.png")));    	
    	toggleButton_fixSplitPane.setTooltip(new Tooltip("Фіксуємо спліттер"));
    	toggleButton_fixSplitPane.setGraphic(new ImageView(new Image("file:resources/images/icon_fix_splitter_16.png")));
    	toggleButton_fixSplitPane.setSelected(prefs.getBoolean("DocumentView_fixSplitPaneMain", false));
    	
    	button_Close.setTooltip(new Tooltip("Зачинити"));
    	button_Close.setGraphic(new ImageView(new Image("file:resources/images/icon_close_16.png")));
    	button_Close.setDisable(! canClose);
    	
    	//======== TreeTableView InfoHeader ============================================================================
    	//-------- columns 
    	// setCellValueFactory
    	treeTableColumn_id.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getId()))
    			);
    	treeTableColumn_name.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getName())
    			);
    	treeTableColumn_descr.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
    			);
    	treeTableColumn_position.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getPosition()))
    			);
    	treeTableColumn_type.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(
    					Long.toString(param.getValue().getValue().getInfoTypeId()) +" - "+
    					params.getConCur().db.infoTypeGet(param.getValue().getValue().getInfoTypeId()).getName())
    			);
    	treeTableColumn_style.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(
    					(param.getValue().getValue().getTemplateStyleId() != 0) ?
    					Long.toString(param.getValue().getValue().getTemplateStyleId()) +" - "+
    					params.getConCur().db.templateStyleGet(param.getValue().getValue().getTemplateStyleId()).getName() :
    					""
    			));
    	treeTableColumn_infoId.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getInfoId()))
    			);
    	treeTableColumn_dateCreated.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateCreated()))
    			);
    	treeTableColumn_dateModified.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateModified()))
    			);
    	treeTableColumn_userCreated.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getUserCreated())
    			);
    	treeTableColumn_userModified.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<InfoHeaderItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getUserModified())
    			);
    	
    	// set/get Pref Width
    	treeTableColumn_id.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_id__PrefWidth", 50));
    	treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_id__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_name.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_name__PrefWidth", 250));
    	treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_name__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_descr.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_descr__PrefWidth", 250));
    	treeTableColumn_descr.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_descr__PrefWidth", t1.doubleValue());
            }
        });

    	treeTableColumn_position.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_position__PrefWidth", 75));
    	treeTableColumn_position.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_position__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_type.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_type__PrefWidth", 75));
    	treeTableColumn_type.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_type__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_style.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_style__PrefWidth", 75));
    	treeTableColumn_style.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_style__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_infoId.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_infoId__PrefWidth", 75));
    	treeTableColumn_infoId.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_infoId__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_dateCreated.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_dateCreated__PrefWidth", 150));
    	treeTableColumn_dateCreated.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_dateCreated__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_dateModified.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_dateModified__PrefWidth", 150));
    	treeTableColumn_dateModified.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_dateModified__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_userCreated.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_userCreated__PrefWidth", 150));
    	treeTableColumn_userCreated.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_userCreated__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_userModified.setPrefWidth(prefs.getDouble("DocumentView__treeTableColumn_userModified__PrefWidth", 150));
    	treeTableColumn_userModified.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DocumentView__treeTableColumn_userModified__PrefWidth", t1.doubleValue());
            }
        });
    	
    	//-------- init 
    	rootInfoList = new TreeItem<>(new InfoHeaderItem(0,0,0,0,0,0, "root item","invisible"));
    	rootInfoList.setExpanded(true);
    	treeTableView_InfoHeader.setShowRoot(false);
    	treeTableView_InfoHeader.setRoot(rootInfoList);
    	
    	// Слушаем изменения выбора, и при изменении отображаем информацию .
    	treeTableView_InfoHeader.getSelectionModel().selectedItemProperty().addListener(
    			(observable, oldValue, newValue) -> onChangeSelectedInfo(newValue));
    	
    	// show Tooltip
    	treeTableView_InfoHeader.setRowFactory(new Callback<TreeTableView<InfoHeaderItem>,
				TreeTableRow<InfoHeaderItem>>() {
			@Override
			public TreeTableRow<InfoHeaderItem> call(final TreeTableView<InfoHeaderItem> param) {
				final TreeTableRow<InfoHeaderItem> row = new TreeTableRow<InfoHeaderItem>();

				WebView webView = new WebView();
				Tooltip tooltip = new Tooltip();

	            row.hoverProperty().addListener((observable, oldValue, newValue) -> {
	                if (row.getItem() != null) {
	                    //tooltip.setText(row.getItem().getName());
	                	
	                	String styleName = 
	                			(row.getItem().getTemplateStyleId() != 0) ?
            					Long.toString(row.getItem().getTemplateStyleId()) +" - "+
            					params.getConCur().db.templateStyleGet(row.getItem().getTemplateStyleId()).getName() :
            					"<i>default</i>"; 
	                	
	                	String htmlContent = 
	                			"<html>" +
	                			"<body>" + 
	                			"<p style='font-size: 11pt; padding:0px; margin:0px;'>"+ 
	                				row.getItem().getName() +"</p>"+
	                			"<p style='font-size: 11pt; padding:0px; margin:0px;'>"+ 
	                				row.getItem().getDescr() +"</p>"+
	                			"<p style='font-size: 11pt; padding:0px; margin:0px;'>"+ 
	                				Long.toString(row.getItem().getInfoTypeId()) +" - "+
	                				params.getConCur().db.infoTypeGet(row.getItem().getInfoTypeId()).getName() +"</p>"+
	                			"<p style='font-size: 11pt; padding:0px; margin:0px;'>"+ 
	                				styleName +"</p>"+
	                			"<p style='font-size: 11pt; padding:0px; margin:0px;'>"+ 
									dateConv.dateTimeToStr(row.getItem().getDateCreated()) +"  - створений</p>"+
								"<p style='font-size: 11pt; padding:0px; margin:0px;'>"+ 
									dateConv.dateTimeToStr(row.getItem().getDateModified()) +"  - модифікований</p>"+
	                			"</body></html>";
	                    webView.getEngine().loadContent(htmlContent);
	                    webView.setPrefWidth(500);
	                    webView.setPrefHeight(120);
	                    tooltip.setGraphic(webView);
	                    Tooltip.install(row, tooltip);
	                }
	            });

				return row;
			}
		});
    	
    	// ContextMenu
    	menuitem_AddInfoBefore.setGraphic(new ImageView(new Image("file:resources/images/icon_insert_up_16.png")));
    	menuitem_AddInfoLast.setGraphic(new ImageView(new Image("file:resources/images/icon_insert_down_16.png")));
    	menuitem_EditInfo.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_16.png")));
    	menuitem_DeleteInfo.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));

    	loadEmptyPage();
    }
    
    /**
     * Загружаем пустой документ
     */
    public void loadEmptyPage () {
    	webView_current.getEngine().loadContent(
    			"<html>" +
                "<head>" +
                "</head>" +
                "<body>" +
                "Ничего не выбрано.<br><br>" +
                "</body>" +
                "</html>");
    }
    
    /**
     * Загружаем документ
     */
    public void load (long sectionId, boolean isReload) {
    	long countInfoHeaders = params.getConCur().db.infoCount(sectionId);
    	long selectedItemId = 0;

    	this.sectionId = sectionId;
    	
    	//======== если это релоад , то запоминаем текущий инфо блок , что бы потом на него вернуться
    	if (isReload && (countInfoHeaders > 0)) {
    		if (treeTableView_InfoHeader.getSelectionModel().getSelectedItem() != null) {
    			selectedItemId = treeTableView_InfoHeader.getSelectionModel().getSelectedItem().getValue().getId();
    		}
    	}
    	
    	//======== info block list init
    	rootInfoList.getChildren().clear();
    	rootInfoList = new TreeItem<>(new InfoHeaderItem(0,0,0,0,0,0, "root item","invisible"));
    	rootInfoList.setExpanded(true);
    	treeTableView_InfoHeader.setShowRoot(false);
    	treeTableView_InfoHeader.setRoot(rootInfoList);
    	
    	//======== 
    	showDocument (sectionId, 0, isReload);
		
		//======== info block list init -- load data
    	List<InfoHeaderItem> infoList = params.getConCur().db.infoListBySectionId (sectionId);
    	
    	for (InfoHeaderItem i : infoList) {
			TreeItem<InfoHeaderItem> subItem = new TreeItem<>(i);
			rootInfoList.getChildren().add(subItem);
		}
    	
    	//======== restore active list item
    	if (isReload) {
    		for (TreeItem<InfoHeaderItem> t : rootInfoList.getChildren()) {
    			if (t.getValue().getId() == selectedItemId) {
    				treeTableView_InfoHeader.getSelectionModel().select(t);
    				isReloadedId = t.getValue().getId();
    			}
    		}
    	}
    }
    
    /**
     * Вызывается при выборе заголовка инфоблока из списка.
     * Перемещается к нужной части документа.
     * 
     * @param ti — информация по одном разделе
     */
    private void onChangeSelectedInfo(TreeItem<InfoHeaderItem> ti) {
    	if (ti != null) {
    		webEngine.executeScript("scrollToElement(\""+ ti.getValue().getId() +"\")");
    	}
    }
    
    /**
     * Показывает документ в WebView.
     * Перед показом возможна компиляция и кеширование.
     */
    private void showDocument (long sectionId, long infoHeaderId, boolean isReload) {
    	// Тип кеширования : 1 - документы кешируются на локальном диске; 2 - кешируются в БД; 3 - кешируются на диске только обязательные файлы
    	SectionItem si = params.getConCur().db.sectionGetById(sectionId);
		int cacheType = 1;
		try {
			cacheType =
				(si.getCacheType() == 0) ?
				Integer.parseInt(params.getConCur().db.settingsGetValue("MAIN__CACHE_DOC__ENABLE")) :
				si.getCacheType();
		} catch (DataConnectionException | DataQueryException e) {
			e.writeLog(params);
			ShowAppMsg.showAlert(
					"ERROR", "Помилка при показі документу, "+
					"помилка при читанні типу кешування з таблиці налаштувань, встановлюю 1",
					Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
		}
    	TemplateThemeItem tti = params.getConCur().db.templateThemeGetById(params.getConCur().db.sectionGetThemeId(sectionId, true));
    	boolean isDocumentCached = params.getConCur().db.documentFindBySectionId(sectionId);
    	DocumentItem di = (isDocumentCached) ? params.getConCur().db.documentGetBySectionId(sectionId) : null;
    	
    	FileCache fileCache = new FileCache(params, tti.getId());
    	
    	//======== Компилируем документ если это необходимо
    	if (isReload || (di == null) || 
    	    ((si.getDateModifiedInfo() != null) && (di.getDateModified().compareTo(si.getDateModifiedInfo()) < 0))
    	   ) {
    		HtmlCompile hc = new HtmlCompile (params, sectionId);
    		
    		try {
    			hc.compile();
    		} catch (KBase_HtmlCompileEx ex) {
    			ShowAppMsg.showAlert("WARNING", "Compile document", ex.msg, "");
    		}
    		
    		di = params.getConCur().db.documentGetBySectionId(sectionId);
    	}
    	
    	//======== Проверка на диске наличия директорий, обязательных файлов и документа (если нужно). Создаем их если нужно.
    	if (cacheType != 2) {        // есть дисковое кеширование обязательных файлов
    		//-------- создаем недостающие директории и файлы
    		try {
				fileCache.createDirAndFiles();
			} catch (KBase_Ex e) {
				ShowAppMsg.showAlert("ERROR", "Дисковое кеширование файлов", e.msg, "Документ не показывается.");
				e.printStackTrace();
				return;
			}        
    		
    		//-------- Проверка на диске наличия документа
        	if (cacheType == 1) {        // есть дисковое кеширование документа
        		fileCache.createFileDoc(di);
        		
        		try {
        			fileCache.createFilesOfInfoBlocksForDoc(sectionId);
    			} catch (KBase_Ex e) {
    				ShowAppMsg.showAlert("ERROR", "Дисковое кеширование Изображений", e.msg, "Документ не показывается.");
    				e.printStackTrace();
    				return;
    			}
        	}
    	}

    	//========== show document
    	if (isDebug) {
    		strDebugForShow = 
    			"<hr>" +
    			"<h1>"+si.getName()+"</h1>" +
    			"cacheType = "+ cacheType +"<br>"+
    			"sectionId = "+ si.getId() +"<br>"+
    			"themeId   = "+ tti.getId() +"<br>"+
    			"Document cached = "+ isDocumentCached +"<br>"+
    			"Document directory = " + fileCache.getPath()  +"<br>"+
    			"Document name = " + fileCache.getDocFileName() +"<br>"
    			;
    	}
    	
    	if (cacheType == 1) {                             // files cache
    		if (infoHeaderId == 0) {
    			File fDoc = new File(fileCache.getPath() + fileCache.getDocFileName());
    			webEngine.load(fDoc.toURI().toString());
    			if (isReload) {
    				webEngine.reload();
    			}
    		} else {
    			File fDoc = new File(fileCache.getPath() + fileCache.getDocFileName());
    			webEngine.load (fDoc.toURI().toString() +"#"+ infoHeaderId);
    			if (isReload) {
    				webEngine.reload();
    			}
    		}
    	} else {                                          // DB cache only
    		//webEngine.loadContent("");
    		//webEngine.load("about:blank");
    		webEngine.loadContent(di.getText());
    		if (infoHeaderId != 0) {                            // not work - реализовано в onChangeSelectedInfo
    			///webEngine.load ("#"+ infoHeaderId);
    			///webEngine.executeScript("scrollToElement(\""+ infoHeaderId +"\")");
    			///webEngine.executeScript("document.querySelector(\"[href='#"+ infoHeaderId +"']\").click()");
    		}
    	}
    }
    
    /**
     * Показывает обязательный файл в WebView.
     */
    private void showTemplateFile (long themeId, String fileName) {
    	if (isDebug)   strDebugForShow = "";
    	
    	TemplateFileItem trf = params.getConCur().db.templateFileGet(themeId, fileName);
    	webEngine.loadContent(trf.getBody());
    }
    
    /**
     * Обновляем документ.
     * При это происходит принудительная компиляция.
     */
    @FXML
    void handleButtonRefresh() {
    	if (sectionId > 0) {
    		load (sectionId, true);
    	}
    }
    
    /**
     * Добавление нового блока перед текущим
     */
    @FXML
    private void handleButtonAddInfoBefore() {
    	if (treeTableView_InfoHeader.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран инфо блок в списке", 
    				"Выберите инфо блок, перед которым необходимо добавить новый.");
    		return;
    	}
    	
    	try {
    		// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
    		FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(Main.class.getResource("view/business/InfoAdd.fxml"));
    		AnchorPane page = (AnchorPane) loader.load();
    		
    		// Создаём диалоговое окно Stage.
    		Stage dialogStage = new Stage();
    		dialogStage.setTitle("Добавление нового блока перед текущим");
    		dialogStage.initModality(Modality.WINDOW_MODAL);
    		dialogStage.initOwner(params.getMainStage());
    		Scene scene = new Scene(page);
    		scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
    		dialogStage.setScene(scene);
    		dialogStage.getIcons().add(new Image("file:resources/images/icon_insert_up_16.png"));
    		
    		Preferences prefs = Preferences.userNodeForPackage(DocumentView_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble("stageInfoAdd_Width", 450));
			dialogStage.setHeight(prefs.getDouble("stageInfoAdd_Height", 300));
			dialogStage.setX(prefs.getDouble("stageInfoAdd_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageInfoAdd_PosY", 0));
    		
			// Даём контроллеру доступ к главному прилодению.
			InfoAdd_Controller controller = loader.getController();
			
			Params params = new Params(this.params);
			params.setTabPane_Cur(((SectionList_Controller)params.getParentObj()).tabPane_info);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
			controller.setParams(
					params,
					sectionId,
					treeTableView_InfoHeader.getSelectionModel().getSelectedItem());
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Добавление нового блока в конец списка
     */
    @FXML
    private void handleButtonAddInfoLast() {
    	if (sectionId == 0)   return;
    	
    	try {
    		// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
    		FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(Main.class.getResource("view/business/InfoAdd.fxml"));
    		AnchorPane page = (AnchorPane) loader.load();
    		
    		// Создаём диалоговое окно Stage.
    		Stage dialogStage = new Stage();
    		dialogStage.setTitle("Добавление нового блока в конец списка");
    		dialogStage.initModality(Modality.WINDOW_MODAL);
    		dialogStage.initOwner(params.getMainStage());
    		Scene scene = new Scene(page);
    		scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
    		dialogStage.setScene(scene);
    		dialogStage.getIcons().add(new Image("file:resources/images/icon_insert_down_16.png"));
    		
    		Preferences prefs = Preferences.userNodeForPackage(DocumentView_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble("stageInfoAdd_Width", 450));
			dialogStage.setHeight(prefs.getDouble("stageInfoAdd_Height", 300));
			dialogStage.setX(prefs.getDouble("stageInfoAdd_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageInfoAdd_PosY", 0));
    		
			// Даём контроллеру доступ к главному прилодению.
			InfoAdd_Controller controller = loader.getController();
			
			Params params = new Params(this.params);
			params.setTabPane_Cur(((SectionList_Controller)params.getParentObj()).tabPane_info);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
			controller.setParams(
					params,
					sectionId,
					null);
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Редактирование блока во внутреннем табе
     */
    @FXML
    private void handleButtonEditInfo() {
    	
    	if (treeTableView_InfoHeader.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран инфо блок в списке", 
    				"Выберите инфо блок, который необходимо редактировать.");
    		return;
    	}

    	InfoHeaderItem ihi = treeTableView_InfoHeader.getSelectionModel().getSelectedItem().getValue();
    	
    	//-------- открываем таб для редактирования
    	Params params = new Params(this.params);
		params.setTabPane_Cur(((SectionList_Controller)params.getParentObj()).tabPane_info);
		params.setObjContainer((SectionList_Controller)params.getParentObj());
		params.setParentObj(this);
		params.setStageCur(null);
		
		AppDataObj.openEditInfo (params, ihi);
    }

	/**
	 * Редактирование блока в главном табе
	 */
	@FXML
	private void handleButtonEditInfo2() {

		if (treeTableView_InfoHeader.getSelectionModel().getSelectedItem() == null) {
			ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран инфо блок в списке",
					"Выберите инфо блок, который необходимо редактировать.");
			return;
		}

		InfoHeaderItem ihi = treeTableView_InfoHeader.getSelectionModel().getSelectedItem().getValue();

		//-------- открываем таб для редактирования
		Params params = new Params(this.params);
		params.setTabPane_Cur(params.getTabPane_Main());
		params.setObjContainer(params.getRootController());
		params.setParentObj(this);
		params.setStageCur(null);
		
		AppDataObj.openEditInfo (params, ihi);
	}

	/**
	 * Редактирование блока в отдельном окне
	 */
	@FXML
	private void handleButtonEditInfo3() {

		if (treeTableView_InfoHeader.getSelectionModel().getSelectedItem() == null) {
			ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран инфо блок в списке",
					"Выберите инфо блок, который необходимо редактировать.");
			return;
		}

		InfoHeaderItem ihi = treeTableView_InfoHeader.getSelectionModel().getSelectedItem().getValue();

		//-------- открываем окно для редактирования
		(new AppDataObj()).openEditInfoInWin(params, ihi);
	}
    
    /**
     * Удаление текущего инфо блока
     */
    @FXML
    private void handleButtonDeleteInfo() {
    	TreeItem<InfoHeaderItem> ti = treeTableView_InfoHeader.getSelectionModel().getSelectedItem();

    	if (ti == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран инфо блок в списке", 
    				"Выберите инфо блок, который необходимо удалить.");
    		return;
    	}
    	
    	if (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление инфо блока", 
                "Удаление блока '"+ ti.getValue().getName() +"' ("+ ti.getValue().getId() +")", "Удалить блок ?"))
    		return;
    	
    	try {
			params.getConCur().db.infoDelete(ti.getValue().getId());
			
			// delete from TreeTableView
	    	TreeItem<InfoHeaderItem> parentItem = ti.getParent();
	    	if (parentItem != null) {     // текущая иконка не корневая
	            parentItem.getChildren().remove(ti);
	    	}
	    	
	    	//
	    	load (sectionId, true);
	    	
	    	// выводим сообщение в статус бар
	        params.setMsgToStatusBar("Инфо блок '" + ti.getValue().getName() + "' удален.");
		} catch (KBase_DbConnEx e) {
			ShowAppMsg.showAlert("WARNING", "Помилка", "Помилка при вилученні інфо блоку", e.msg); 
			e.printStackTrace();
		}
    }
    
    /**
     * Изменить ориентацию списка инфоблоков
     */
    @FXML
    void handleButtonChangeInfoListOrientation() {
    	if (splitPane_info.getOrientation() == Orientation.HORIZONTAL) {
    		splitPane_info.setOrientation(Orientation.VERTICAL);
   			prefs.put("DocumentView_splitPane_info_orientation", "VERTICAL");
    	} else {
    		splitPane_info.setOrientation(Orientation.HORIZONTAL);
   			prefs.put("DocumentView_splitPane_info_orientation", "HORIZONTAL");
    	}
    }
    
    /**
     * 
     */
    @FXML
    private void handleButtonFixSplitPane() {
    	params.setMsgToStatusBar("Документ, фіксаціяі сплітера зі значенням " + spliterWidth);
    }
    
    /**
     * Закрываем таб/окно просмотра документа
     */
    @FXML
    private void handleButtonClose() {
    	close();
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
		return AppItem_Interface.ELEMENT_DOCUMENT_VIEW;
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
		return sectionId;
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
    	params.getObjContainer().closeContainer(getOID());
    }
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.            <br>
	 * Сохраняем состояние контролов в иерархической структуре
	 */
	public void saveControlsState (StateList stateList) {
		//-------- save info split state
		String splitOrientation = 
				(splitPane_info.getOrientation() == Orientation.HORIZONTAL) ? "HORIZONTAL" : "VERTICAL";
		
		stateList.add(
				"splitPane_info_Orientation",
				splitOrientation,
				null);
		stateList.add(
				"splitPane_info_Position",
				String.valueOf(spliterWidth),
				null);
		stateList.add(
				"splitPane_info_fixMode",
				(toggleButton_fixSplitPane.isSelected()) ? "1" : "0",
				null);
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Восстанавливаем состояние контролов из иерархической структуры
	 */
	public void restoreControlsState (StateList stateList) {
		
		for (StateItem si : stateList.list) {
			switch (si.getName()) {
			//======== restore info split state
			case "splitPane_info_Orientation" :
				if (si.getParams().equals("HORIZONTAL")) {
					splitPane_info.setOrientation(Orientation.HORIZONTAL);
				} else {
					splitPane_info.setOrientation(Orientation.VERTICAL);
				}
				break;
			case "splitPane_info_Position" :
				// Використовуємо applySpliterPos з retry-логікою:
				// на Linux getWidth() може бути 0 в момент runLater (контрол ще не відрендерений),
				// тому повторюємо спроби до SPLITTER_RETRY_MAX разів
				applySpliterPos(Integer.parseInt(si.getParams()));
				break;
			case "splitPane_info_fixMode" :
				toggleButton_fixSplitPane.setSelected(
						(si.getParams().equals("1")) ? true : false
						);
				break;
			}
		}
	}
	
	/**
     * берем значення позиції сплітера з конфіга (ширина/висота правої панелі в пікселях)
     */
    private int getSpliterWidthFromConfig () {
    	int spliterWidthDefault = 150;
    	String strSpliterWidth = params.getConfig().getItemValue("AppState", "document.spliter.pos");
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
    				"document.spliter.pos",
    				"Вікно документа, позиція сплітера в пікселях відносно правої/нижньої сторони",
    				"150",
    				LocalDate.now(),
    				true,
    				true
    				);
    	}
    	
    	return spliterWidth;
    }
    
    /**
     * повертає позицію сплітера відносно ширини/висоти правої/нижньої панелі
     */
    private double getSpliterPos (int width) {
    	double windowWidth;
    	if (splitPane_info.getOrientation() == Orientation.HORIZONTAL) {
    		windowWidth = splitPane_info.getWidth();
	    } else {
	    	windowWidth = splitPane_info.getHeight();
	    }
    	double position = 1 - width / windowWidth; // Відносна позиція сплітера
    	
    	return position;
    }
    
    /** Максимальна кількість повторних спроб встановлення позиції сплітера */
    private static final int SPLITTER_RETRY_MAX = 5;
    
    /**
     * Встановлює позицію сплітера відновлення зі збереженого стану.
     * На Linux контрол може ще не мати реального розміру в перших тактах runLater
     * (getWidth()==0), що призводить до ділення на нуль і зсуву сплітера вліво.
     * Якщо розмір ще 0 — повторюємо спробу через наступний Platform.runLater
     * (не більше SPLITTER_RETRY_MAX разів).
     */
    private void applySpliterPos (int targetWidth) {
    	applySpliterPos(targetWidth, 0);
    }
    
    private void applySpliterPos (int targetWidth, int attempt) {
    	Platform.runLater(() -> {
    		double paneSize = (splitPane_info.getOrientation() == Orientation.HORIZONTAL)
    				? splitPane_info.getWidth()
    				: splitPane_info.getHeight();
    		
    		if (paneSize <= 0) {
    			// Контрол ще не відрендерений — повторюємо спробу
    			if (attempt < SPLITTER_RETRY_MAX) {
    				applySpliterPos(targetWidth, attempt + 1);
    			}
    			return;
    		}
    		
    		spliterWidth = targetWidth;
    		double position = 1.0 - targetWidth / paneSize; // Відносна позиція сплітера
    		splitPane_info.setDividerPositions(position); // Встановлення позиції
    		isSplitterPositionedFirst = true;
    		//System.out.println("Рестор сплітера: " + position + " (spliterWidth: " + targetWidth + ", attempt: " + attempt + ")");
    	});
    }
    
    /**
     * повертає ширину/висоту правої/нижньої панелі відносно позиції сплітера
     */
    private int getSpliterWidth (double pos) {
    	double windowWidth;
    	if (splitPane_info.getOrientation() == Orientation.HORIZONTAL) {
    		windowWidth = splitPane_info.getWidth();
	    } else {
	    	windowWidth = splitPane_info.getHeight();
	    }
	    double newWidth = (1 - pos) * windowWidth; // Абсолютна ширина лівої панелі
    	
    	return (int) Math.round(newWidth);
    }
}
