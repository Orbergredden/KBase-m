package app.view.business;

import app.lib.DateConv;
import app.lib.ShowAppMsg;
import app.lib.StringUtil;
import app.model.AppItem_Interface;
import app.model.Params;
import app.model.StateItem;
import app.model.StateList;
import app.model.business.DictionaryItem;
import app.model.business.InfoHeaderItem;
import app.model.business.InfoTypeItem;
import app.model.business.SectionItem;
import app.util.FormattedDate;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeSortMode;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Контролер фрейма показу словника.
 * @author Igor Makarevich
 */
public class DictionaryView_Controller implements AppItem_Interface {
	private Params params;
    private long sectionId;
    private DateConv dateConv;
    // можно ли закрывать данную сцену
    private boolean canClose;
    private Preferences prefs = Preferences.userNodeForPackage(DictionaryView_Controller.class);
    
    @FXML
	private AnchorPane anchorPane_Main;
    @FXML
    private SplitPane splitPane_info;
    
    @FXML
    private Button button_ChangeSpliterOrientation;
    @FXML
    private Button button_Refresh;
    @FXML
    private Button button_Close;
    
    @FXML
    private SplitPane splitPane_details;
    
    @FXML
	public TreeTableView<DictionaryItem> treeTableView_Dict;
	@FXML
	private TreeTableColumn<DictionaryItem, Long> treeTableColumn_id;
	@FXML
	private TreeTableColumn<DictionaryItem, Long> treeTableColumn_typeId;
	@FXML
	private TreeTableColumn<DictionaryItem, String> treeTableColumn_name;
	@FXML
	private TreeTableColumn<DictionaryItem, String> treeTableColumn_value;
	@FXML
	private TreeTableColumn<DictionaryItem, String> treeTableColumn_descr;
	@FXML
	private TreeTableColumn<DictionaryItem, Integer> treeTableColumn_rating;
	@FXML
	private TreeTableColumn<DictionaryItem, String> treeTableColumn_reverse;
	@FXML
	private TreeTableColumn<DictionaryItem, String> treeTableColumn_status;
	@FXML
	private TreeTableColumn<DictionaryItem, FormattedDate> treeTableColumn_date_viewed;
	@FXML
	private TreeTableColumn<DictionaryItem, FormattedDate> treeTableColumn_date_created;
	@FXML
	private TreeTableColumn<DictionaryItem, FormattedDate> treeTableColumn_date_modified;
	@FXML
	private TreeTableColumn<DictionaryItem, String> treeTableColumn_user_created;
	@FXML
	private TreeTableColumn<DictionaryItem, String> treeTableColumn_user_modified;
	
	@FXML
    private ToggleButton details_Button_AddMode;
	@FXML
    private Button details_Button_Save;
	@FXML
    private Button details_Button_Delete;
	@FXML
	private Label details_Label_id;
	@FXML
	private ComboBox<String> details_ComboBox_Type;
	@FXML
	private TextArea details_TextArea_Name;
	@FXML
	private TextArea details_TextArea_Value;
	@FXML
	private TextArea details_TextArea_Descr;
	@FXML
	private TextField details_TextField_Rating;
	@FXML
	private TextField details_TextField_Reverse;
	@FXML
	private TextField details_TextField_Status;
	@FXML
	private Label details_Label_DateViewed;
	@FXML
	private Label details_Label_DateCreated;
	@FXML
	private Label details_Label_DateModified;
	@FXML
	private Label details_Label_UserCreated;
	@FXML
	private Label details_Label_UserModified;
	
	/**
     * Корін в дереві-контролі з елементами словника
     */
    TreeItem<DictionaryItem> rootDictList;
    
    // for sort options in the dictionary list
 	private String sortColumnId;
 	private String sortType;
    
    // for details_ComboBox_Type
 	private ObservableList<String> OList_DetailsType;
	
    /**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public DictionaryView_Controller () {
    	dateConv = new DateConv();
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
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
    	if (! params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка утановки в конфигурации
    		String orientation = prefs.get("DictionaryView_splitPane_info_orientation", "HORIZONTAL");
    		
    		if (orientation.equals("HORIZONTAL")) {
    			splitPane_info.setOrientation(Orientation.HORIZONTAL);
    		} else {
    			splitPane_info.setOrientation(Orientation.VERTICAL);
    		}
		}
    	
    	splitPane_info.setDividerPositions(prefs.getDouble("DictionaryView_splitPane_info_position", 0.3));
    	
    	splitPane_info.getDividers().get(0).positionProperty().addListener(
                o -> {
                	prefs.putDouble("DictionaryView_splitPane_info_position", splitPane_info.getDividerPositions()[0]);
                }
        );
    	
    	//======== hot keys
    	anchorPane_Main.setOnKeyPressed(event -> {
    		if (event.getCode() == KeyCode.R && event.isControlDown()) {
    	        handleButtonRefresh();
    	    }
    	});
    	
    	//======== ToolBar
    	button_ChangeSpliterOrientation.setTooltip(new Tooltip("Змінити орієнтацію панелей"));
    	button_ChangeSpliterOrientation.setGraphic(new ImageView(new Image("file:resources/images/icon_orientation_16.png")));
    	button_Refresh.setTooltip(new Tooltip("Оновити словник (Ctrl+R)"));
    	button_Refresh.setGraphic(new ImageView(new Image("file:resources/images/icon_refresh_16.png")));
    	button_Close.setTooltip(new Tooltip("Зачинити"));
    	button_Close.setGraphic(new ImageView(new Image("file:resources/images/icon_close_16.png")));
    	button_Close.setDisable(! canClose);
    	
    	//======== splitPane_details
    	splitPane_details.setDividerPositions(prefs.getDouble("DictionaryView_splitPane_details_position", 0.3));
    	
    	splitPane_details.getDividers().get(0).positionProperty().addListener(
                o -> {
                	prefs.putDouble("DictionaryView_splitPane_details_position", splitPane_details.getDividerPositions()[0]);
                }
        );
    	
    	//========
    	initTableDict();
    	
    	//-------- tool bar for details information
    	details_Button_AddMode.setTooltip(new Tooltip("Режим створення нового елемента"));
    	details_Button_AddMode.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
    	details_Button_Save.setTooltip(new Tooltip("Зберегти зміни"));
    	details_Button_Save.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	details_Button_Delete.setTooltip(new Tooltip("Вилучення елемента словника"));
    	details_Button_Delete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	
    	//-------- init comboBox_infoType
    	OList_DetailsType = FXCollections.observableArrayList();
    	OList_DetailsType.add("Word (1)");
    	OList_DetailsType.add("Phrase (2)");
    	OList_DetailsType.add("Text (3)");

    	details_ComboBox_Type.setItems(OList_DetailsType);
    	details_ComboBox_Type.setValue(OList_DetailsType.get(0));    // первый эдемент в списке
    	
    	//-------- clear controls
    	details_Label_id.setText("");
    	details_Label_DateViewed.setText("");
    	details_Label_DateCreated.setText("");
    	details_Label_DateModified.setText("");
    	details_Label_UserCreated.setText("");
    	details_Label_UserModified.setText("");
    }
    
    /**
     * Ініціалізуємо таблицю з елементами словника
     */
    private void initTableDict() {
    	//-------- columns 
    	// setCellValueFactory
    	treeTableColumn_id.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<DictionaryItem, Long> param) -> 
    			new ReadOnlyObjectWrapper<>(param.getValue().getValue().getId())
    			);
    	treeTableColumn_typeId.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<DictionaryItem, Long> param) -> 
    			new SimpleLongProperty((long)param.getValue().getValue().getTypeId()).asObject()
    	);
    	treeTableColumn_name.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<DictionaryItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getName())
    			);
    	treeTableColumn_value.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<DictionaryItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getValue())
    			);
    	treeTableColumn_descr.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<DictionaryItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
    			);
    	treeTableColumn_rating.setCellValueFactory(
    		    (TreeTableColumn.CellDataFeatures<DictionaryItem, Integer> param) ->
    		    new ReadOnlyObjectWrapper<>(param.getValue().getValue().getRating())
    		);
    	treeTableColumn_reverse.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<DictionaryItem, String> param) -> 
    			new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getReverse()))
    			);
    	treeTableColumn_status.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<DictionaryItem, String> param) -> 
    			new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getStatus()))
    			);
    	
    	treeTableColumn_date_viewed.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<DictionaryItem, FormattedDate> param) -> {
                    Date dateViewed = param.getValue().getValue().getDateViewed();
                    FormattedDate formattedDate = new FormattedDate(dateViewed);
                    return new ReadOnlyObjectWrapper<>(formattedDate);
                }
            );
    	treeTableColumn_date_created.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<DictionaryItem, FormattedDate> param) -> {
                    Date dateCreated = param.getValue().getValue().getDateCreated();
                    FormattedDate formattedDate = new FormattedDate(dateCreated);
                    return new ReadOnlyObjectWrapper<>(formattedDate);
                }
            );
    	treeTableColumn_date_modified.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<DictionaryItem, FormattedDate> param) -> {
                    Date dateModified = param.getValue().getValue().getDateModified();
                    FormattedDate formattedDate = new FormattedDate(dateModified);
                    return new ReadOnlyObjectWrapper<>(formattedDate);
                }
            );
    	treeTableColumn_user_created.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<DictionaryItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getUserCreated())
    			);
    	treeTableColumn_user_modified.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<DictionaryItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getUserModified())
    			);
    	
    	// CellFactory
    	treeTableColumn_typeId.setCellFactory(
			    new Callback<TreeTableColumn<DictionaryItem, Long>, TreeTableCell<DictionaryItem, Long>>() {
			        @Override
			        public TreeTableCell<DictionaryItem, Long> call(TreeTableColumn<DictionaryItem, Long> param) {
			            return new TreeTableCell<DictionaryItem, Long>() {
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
			                    	String iconPath = null;
			                    	
			                    	if (item == 1) {
			                    		iconPath = "file:resources/images/dictionary/type_word_16.png";
			                    	} else if (item == 2) {
			                    		iconPath = "file:resources/images/dictionary/type_phrase_16.png";
			                    	} else if (item == 3) {
			                    		iconPath = "file:resources/images/dictionary/type_text_16.png";
			                    	} else {
			                    		iconPath = "file:resources/images/icon_error_16.png";
			                    	}
			                    	graphic = new ImageView(new Image(iconPath));
			                    	
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
    	
    	// set/get Pref Width
    	treeTableColumn_id.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_id__PrefWidth", 50));
    	treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_id__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_typeId.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_typeId__PrefWidth", 50));
    	treeTableColumn_typeId.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_typeId__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_name.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_name__PrefWidth", 50));
    	treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_name__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_value.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_value__PrefWidth", 50));
    	treeTableColumn_value.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_value__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_descr.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_descr__PrefWidth", 50));
    	treeTableColumn_descr.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_descr__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_rating.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_rating__PrefWidth", 50));
    	treeTableColumn_rating.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_rating__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_reverse.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_reverse__PrefWidth", 50));
    	treeTableColumn_reverse.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_reverse__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_status.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_status__PrefWidth", 50));
    	treeTableColumn_status.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_status__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_date_viewed.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_date_viewed__PrefWidth", 50));
    	treeTableColumn_date_viewed.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_date_viewed__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_date_created.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_date_created__PrefWidth", 50));
    	treeTableColumn_date_created.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_date_created__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_date_modified.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_date_modified__PrefWidth", 50));
    	treeTableColumn_date_modified.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_date_modified__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_user_created.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_user_created__PrefWidth", 50));
    	treeTableColumn_user_created.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_user_created__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_user_modified.setPrefWidth(prefs.getDouble("DictionaryView__treeTableColumn_user_modified__PrefWidth", 50));
    	treeTableColumn_user_modified.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("DictionaryView__treeTableColumn_user_modified__PrefWidth", t1.doubleValue());
            }
        });
    	
    	//-------- init 
    	rootDictList = new TreeItem<>(new DictionaryItem(0, sectionId, 0,  
    			"root item", "", "invisible", 0, 0, 0, 
    			null, null, null, null, null));
    	rootDictList.setExpanded(true);
    	treeTableView_Dict.setShowRoot(false);
    	treeTableView_Dict.setRoot(rootDictList);
    	
    	// Слушаем изменения выбора, и при изменении отображаем информацию .
    	treeTableView_Dict.getSelectionModel().selectedItemProperty().addListener(
    			(observable, oldValue, newValue) -> onChangeSelectedItem(newValue));
    	
    	//-------- write sort
		treeTableView_Dict.getSortOrder().addListener((ListChangeListener<TreeTableColumn>) change -> {
		    if (!change.getList().isEmpty()) {
		        TreeTableColumn sortedColumn = change.getList().get(0);
		        prefs.put("DictionaryView_sortColumnId", sortedColumn.getId());
		        prefs.put("DictionaryView_sortType", sortedColumn.getSortType().toString()); // "ASCENDING" або "DESCENDING"
		        //System.out.println("sortedColumn.getSortType().toString() = "+ sortedColumn.getSortType().toString());
		    }
		});
		
		for (TreeTableColumn<DictionaryItem, ?> column : treeTableView_Dict.getColumns()) {
		    column.sortTypeProperty().addListener((obs, oldVal, newVal) -> {
		        if (treeTableView_Dict.getSortOrder().contains(column)) {
		            prefs.put("DictionaryView_sortColumnId", column.getId());
		            prefs.put("DictionaryView_sortType", newVal.toString());
		            //System.out.println("Збережено: " + column.getId() + " -> " + newVal);
		        }
		    });
		}
    }
    
    /**
     * Завантажуємо словник
     */
    public void load (long sectionId) {
    	this.sectionId = sectionId;
    	
    	//-------- init 
    	rootDictList.getChildren().clear();
    	rootDictList = new TreeItem<>(new DictionaryItem(0, sectionId, 0,  
    			"root item", "", "invisible", 0, 0, 0, 
    			null, null, null, null, null));
    	rootDictList.setExpanded(true);
    	treeTableView_Dict.setShowRoot(false);
    	treeTableView_Dict.setRoot(rootDictList);
    	
    	//======== list init -- load data
    	List<DictionaryItem> dictList = params.getConCur().db.dictListBySectionId (sectionId);
    	
    	for (DictionaryItem i : dictList) {
			TreeItem<DictionaryItem> subItem = new TreeItem<>(i);
			rootDictList.getChildren().add(subItem);
		}
    	
    	//-------- select first item
    	if (! treeTableView_Dict.getRoot().getChildren().isEmpty()) {
    		TreeItem<DictionaryItem> startDictItem = null;
			startDictItem = treeTableView_Dict.getRoot().getChildren().get(0);
			treeTableView_Dict.getSelectionModel().select(startDictItem);
		}
    	
    	//-------- set sort
    	String sortColumnId = prefs.get("DictionaryView_sortColumnId","");

		if (! sortColumnId.equals("")) {
			for (TreeTableColumn column : treeTableView_Dict.getColumns()) {
				if (column.getId().equals(sortColumnId)) {
					String sortType = prefs.get("DictionaryView_sortType","ASCENDING");

					treeTableView_Dict.setSortMode(TreeSortMode.ALL_DESCENDANTS);
					column.setSortable(true); // This performs a sort
					treeTableView_Dict.getSortOrder().add(column);
					if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
					else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
					treeTableView_Dict.sort();
				}
			}
		}
    }
    
    /**
     * Викликається при виборі елемента словника зі списку.
     */
    private void onChangeSelectedItem(TreeItem<DictionaryItem> ti) {
    	if ((ti != null) && (! details_Button_AddMode.isSelected())) {
    		DictionaryItem i = ti.getValue();
    		
    		details_Label_id.setText(Long.toString(i.getId()));
    		details_ComboBox_Type.setValue(OList_DetailsType.get(i.getTypeId()-1));
    		details_ComboBox_Type.setDisable(true);
    		details_TextArea_Name.setText(i.getName());
    		details_TextArea_Value.setText(i.getValue());
    		details_TextArea_Descr.setText(i.getDescr());
    		details_TextField_Rating.setText(String.valueOf(i.getRating()));
    		details_TextField_Reverse.setText(String.valueOf(i.getReverse()));
    		details_TextField_Status.setText(String.valueOf(i.getStatus()));
    		details_Label_DateViewed.setText(dateConv.dateTimeToStr(i.getDateViewed()));
    		details_Label_DateCreated.setText(dateConv.dateTimeToStr(i.getDateCreated()));
    		details_Label_DateModified.setText(dateConv.dateTimeToStr(i.getDateModified()));
    		details_Label_UserCreated.setText(i.getUserCreated());
    		details_Label_UserModified.setText(i.getUserModified());
    	}
    }
    
    /**
     * Шукає в списку-контролі елемент словника по id
     */
    private boolean isPresentDictItemInList (long id) {
    	boolean retVal = false;

    	for (TreeItem<DictionaryItem> i : treeTableView_Dict.getRoot().getChildren()) {
    		if (i.getValue().getId() == id)
    			return true;
		}
    
    	return retVal;
    }
    
    /**
     * Вибирає поточний елемент по id  
     */
    private void setSelectedDictItemInList(long id) {
    	for (TreeItem<DictionaryItem> i : treeTableView_Dict.getRoot().getChildren()) {
    		if (i.getValue().getId() == id)
    			treeTableView_Dict.getSelectionModel().select(i);
		}
    }
    
    /**
     * Зберігаємо в сторонку параметри сортування в списку елементів 
     */
    private void getSortOptions () { 
    	if (treeTableView_Dict.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
    		TreeTableColumn currentSortColumn = (TreeTableColumn) treeTableView_Dict.getSortOrder().get(0);
    				
    		sortColumnId = currentSortColumn.getId();
    		sortType = currentSortColumn.getSortType().toString();
    	} else {
    		sortColumnId = "";
    		sortType = "";
    	}
    }
    
    /**
     * Востановлюємо параметри сортування
     */
    private void restoreSortOptions () {
    	treeTableView_Dict.getSortOrder().clear();
		
		if (! sortColumnId.equals("")) {
			for (TreeTableColumn column : treeTableView_Dict.getColumns()) {
				if (column.getId().equals(sortColumnId)) {
					treeTableView_Dict.setSortMode(TreeSortMode.ALL_DESCENDANTS);
					column.setSortable(true); // This performs a sort
					treeTableView_Dict.getSortOrder().add(column);
					if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
					else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
					treeTableView_Dict.sort();
				}
			}
		}
    }
    
    /**
     * Змінити оріентацію панелей (сплітера)
     */
    @FXML
    private void handleButtonChangeSpliterOrientation() {
    	
    	if (splitPane_info.getOrientation() == Orientation.HORIZONTAL) {
    		splitPane_info.setOrientation(Orientation.VERTICAL);
    		if (! params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка утановки в конфигурации
    			prefs.put("DictionaryView_splitPane_info_orientation", "VERTICAL");
    		}
    	} else {
    		splitPane_info.setOrientation(Orientation.HORIZONTAL);
    		if (! params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка утановки в конфигурации
    			prefs.put("DictionaryView_splitPane_info_orientation", "HORIZONTAL");
    		}
    	}
    }
    
    /**
     * Перечитуємо словник з БД.
     */
    @FXML
    void handleButtonRefresh() {
    	// get selected item's id
    	TreeItem<DictionaryItem> selectedDictItem = treeTableView_Dict.getSelectionModel().getSelectedItem();
    	long selectedDictItem_id = 0;
    	if (selectedDictItem != null) {
    		selectedDictItem_id = selectedDictItem.getValue().getId();
    	}
    	
    	// get sort options
    	getSortOptions ();
    	
    	// load
    	if (sectionId > 0) {
    		load (sectionId);
    	}
    	
    	// restore selected item
    	if ((selectedDictItem_id > 0) && isPresentDictItemInList(selectedDictItem_id)) {
    		setSelectedDictItemInList(selectedDictItem_id);
    	}
    	
    	// restore sort options
    	restoreSortOptions ();
    }
    
    /**
     * Закрываем таб/окно просмотра документа
     */
    @FXML
    private void handleButtonClose() {
    	close();
    }
    
    /**
     * На панелі детальної інформації вибираємо режим Додати/Редагувати елемент
     */
    @FXML
    private void handleButtonDetailsAddMode() {
    	if (details_Button_AddMode.isSelected()) {   // Add mode
    		details_Label_id.setText("");
    		details_ComboBox_Type.setDisable(false);
    		details_Label_DateViewed.setText("");
    		details_Label_DateCreated.setText("");
    		details_Label_DateModified.setText("");
    		details_Label_UserCreated.setText("");
    		details_Label_UserModified.setText("");
    	} else {          // Update mode
    		onChangeSelectedItem (treeTableView_Dict.getSelectionModel().getSelectedItem());
    	}
    }
    
    /**
     * На панелі детальної інформації зберігаємо інформацію по елементу
     */
    @FXML
    private void handleButtonDetailsSave() {
    	DictionaryItem di = null;
    	int rating, reverse, status;
    	
		//---------- запитуємо чи робити збереження
		if (! details_Button_AddMode.isSelected()) {
			if (! ShowAppMsg.showQuestion("CONFIRMATION", "Збереження", 
					"Збереження змін існуючого елемента", "Зберігаємо ?"))
				return;
		}

    	//---------- check data in fields
    	if ((details_TextArea_Name.getText().equals("") || (details_TextArea_Name.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не заповнена Назва", "Вкажіть Назву");
    		return;
        }
    	if ((details_TextArea_Value.getText().equals("") || (details_TextArea_Value.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не заповнене Значення", "Вкажіть Значення");
    		return;
        }
    	try {
    	    rating = Integer.parseInt(details_TextField_Rating.getText());
    	} catch (NumberFormatException e) {
    		ShowAppMsg.showAlert("WARNING", "Помилка при ведені даних", "Неправильне значення Rating", "Вкажіть Rating");
    		return;
    	}
    	try {
    	    reverse = Integer.parseInt(details_TextField_Reverse.getText());
    	} catch (NumberFormatException e) {
    		ShowAppMsg.showAlert("WARNING", "Помилка при ведені даних", "Неправильне значення Reverse", "Вкажіть Reverse");
    		return;
    	}
    	try {
    	    status = Integer.parseInt(details_TextField_Status.getText());
    	} catch (NumberFormatException e) {
    		ShowAppMsg.showAlert("WARNING", "Помилка при ведені даних", "Неправильне значення Status", "Вкажіть Status");
    		return;
    	}
    	/*
    	if (! details_Button_AddMode.isSelected()) {
    		TreeItem<DictionaryItem> tdi = treeTableView_Dict.getSelectionModel().getSelectedItem(); 
    		
    		if (tdi != null) {
    			DictionaryItem si = treeTableView_Dict.getSelectionModel().getSelectedItem().getValue();
    	
    			if (si.getName().equals(details_TextArea_Name.getText()) &&
    				si.getValue().equals(details_TextArea_Value.getText()) &&
    				si.getDescr().equals(details_TextArea_Descr.getText()) &&   // може бути null
    				(si.getRating() == rating) &&
    				(si.getReverse() == reverse) &&
    				(si.getStatus() == status)
    				) {
    				ShowAppMsg.showAlert("WARNING", "Інформація не змінювалася", "Немає чого зберігати", "");
    	    		return;
    			}
    		}
    	}
    	*/
    	
    	//-------- save
    	if (details_Button_AddMode.isSelected()) {   // Add
    		long newId = params.getConCur().db.dictNextId();
    		
    		di = new DictionaryItem(
    				newId, 
    				sectionId, 
    				(int)StringUtil.getIdFromComboName(details_ComboBox_Type.getSelectionModel().getSelectedItem()),
    				details_TextArea_Name.getText(),
    				details_TextArea_Value.getText(),
    				details_TextArea_Descr.getText(),
    				rating,
    				reverse,
    				status,
    				null, null, null, null, null
    				);
    		params.getConCur().db.dictAdd(di);
    		di = params.getConCur().db.dictGetById(newId); // get full info
    		
    		// додаємо в контрол
    		TreeItem<DictionaryItem> item = new TreeItem<>(new DictionaryItem(di));
    		rootDictList.getChildren().add(item);
    		setSelectedDictItemInList(newId);
    		treeTableView_Dict.sort();

        	// виводимо повідомлення в статус бар
    		params.setMsgToStatusBar("Елемент словника '" + di.getName() + "' додано.");
    	} else {               // update
    		TreeItem<DictionaryItem> tdi = treeTableView_Dict.getSelectionModel().getSelectedItem(); 
    		
    		if (tdi != null) {
    			DictionaryItem si = treeTableView_Dict.getSelectionModel().getSelectedItem().getValue();
    		
    			di = new DictionaryItem(
    				si.getId(), 
    				si.getSectionId(), 
    				si.getTypeId(),
    				details_TextArea_Name.getText(),
    				details_TextArea_Value.getText(),
    				details_TextArea_Descr.getText(),
    				rating,
    				reverse,
    				status,
    				si.getDateViewed(),
    				si.getDateCreated(),
    				null,
    				si.getUserCreated(),
    				null
    				);
    			params.getConCur().db.dictUpdate(di);
        		di = params.getConCur().db.dictGetById(si.getId()); // get full info
        		
            	// update in TreeTableView
        		tdi.setValue(null);
        		tdi.setValue(di);
            	
            	// виводимо повідомлення в статус бар
            	params.setMsgToStatusBar("Елемент словника '" + di.getName() + "' змінено.");
    		}
    	}
    }
    
    /**
     * На панелі детальної інформації вилучаємо елемент зі словника
     */
    @FXML
    private void handleButtonDetailsDelete() {
    	TreeItem<DictionaryItem> selectedItem = treeTableView_Dict.getSelectionModel().getSelectedItem();
		
		if (selectedItem == null) {
			params.setMsgToStatusBar("Нічого не вибрано для вилучення.");
			return;
		}
		
		DictionaryItem di = selectedItem.getValue();
		
		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Вилучення елементу словника", 
	            "Вилучення '"+ di.getName() +"'", "Вилучити ?"))
			return;
		
		// delete from DB 
		params.getConCur().db.dictDelete(di.getId());
				
		// delete from TreeTableView
		TreeItem<DictionaryItem> parentItem = selectedItem.getParent();
		if (parentItem != null) {     // текущая иконка не корневая
			parentItem.getChildren().remove(selectedItem);
		}
				
		// виводимо повідомлення в статус бар
		params.setMsgToStatusBar("Елемент словника '" + di.getName() + "' вилучений.");
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
		return AppItem_Interface.ELEMENT_DICTIONARY_VIEW;
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
     * Зачиняємо таб чи вікно з цим елементом інтерфейсу
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
		//-------- save splitPane_info state
		String splitOrientation = 
				(splitPane_info.getOrientation() == Orientation.HORIZONTAL) ? "HORIZONTAL" : "VERTICAL";
		
		stateList.add(
				"splitPane_info_Orientation",
				splitOrientation,
				null);
		stateList.add(
				"splitPane_info_Position",
				String.valueOf(splitPane_info.getDividerPositions()[0]),
				null);
		
		//-------- save splitPane_info state
		stateList.add(
				"splitPane_details_Position",
				String.valueOf(splitPane_details.getDividerPositions()[0]),
				null);
		
		//-------- treeTableView_Dict
		// sort
		getSortOptions ();
		if (! sortColumnId.equals("")) {
			stateList.add("TreeTable_sortColumnId",	sortColumnId, null);
			stateList.add("TreeTable_sortType",	sortType, null);
			stateList.add("TreeTable_doSort", "", null);
    	}
		
		// active item
		try {
			stateList.add(
					"TreeItemSelected",
					Long.toString(treeTableView_Dict.getSelectionModel().getSelectedItem().getValue().getId()),      // section id in DB,
					null);
		} catch (NullPointerException ex) {    }
		stateList.add("TreeItemsDoSelected", "", null);
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Восстанавливаем состояние контролов из иерархической структуры
	 */
	public void restoreControlsState (StateList stateList) {
		// for TreeItems
		Long selectedItemId = 0L;
		
		for (StateItem si : stateList.list) {
			switch (si.getName()) {
			//======== restore splitPane_info state
			case "splitPane_info_Orientation" :
				if (si.getParams().equals("HORIZONTAL")) {
					splitPane_info.setOrientation(Orientation.HORIZONTAL);
				} else {
					splitPane_info.setOrientation(Orientation.VERTICAL);
				}
				break;
			case "splitPane_info_Position" :
				splitPane_info.setDividerPositions(Double.parseDouble(si.getParams()));
				break;
			
			//======== restore splitPane_info state
			case "splitPane_details_Position" :
				splitPane_details.setDividerPositions(Double.parseDouble(si.getParams()));
				break;
				
			//======== treeTableView_Dict
			// sort
			case "TreeTable_sortColumnId" :
				sortColumnId = si.getParams();
				break;
			case "TreeTable_sortType" :
				sortType = si.getParams();
				break;
			case "TreeTable_doSort" :
				restoreSortOptions ();
				break;
			
			// active item
			case "TreeItemSelected" :
				selectedItemId = Long.valueOf(si.getParams());
				break;
			case "TreeItemsDoSelected" :
				restoreTreeItemSelectedRecursive(selectedItemId,treeTableView_Dict.getRoot());
				treeTableView_Dict.sort();
				break;
			}
		}
	}
	
	/**
	 * рекурсивно шукаємо активний елемент в дереві словника та вибираємо його
	 */
	private void restoreTreeItemSelectedRecursive(
			Long selectedItemId,
			TreeItem<DictionaryItem> ti) {

		//---------- проверяем и выбираем текущий итем
		if (selectedItemId == ti.getValue().getId()) {
			treeTableView_Dict.getSelectionModel().select(ti);
		}

		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<DictionaryItem> i : ti.getChildren()) {
			restoreTreeItemSelectedRecursive(selectedItemId, i);
		}
	}
}
