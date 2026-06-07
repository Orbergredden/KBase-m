package app.view.business.template;

import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import app.lib.ShowAppMsg;
import app.model.DBConCur_Parameters;
import app.model.business.template.TemplateStyleItem;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateSimpleItem;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Контроллер окна выбора стиля шаблонов.
 */
public class TemplateStyleSelect_Controller {
	/**
	 *  Ссылка на вызываемый обьект
	 */
	private Stage dialogStage;
	//
	long themeId;
	long infoTypeId;
	private long rootStyleDirId;
	public TemplateStyleItem styleSelected;
	public int styleTypeSelection;           // 0 - дефолтный стиль ; 1 - текущий стиль ; 2 - стиль из списка
	//
	DBConCur_Parameters conn;
	
	@FXML
	private RadioButton radioButton_defaultStyle;
	@FXML
	private Label label_defaultStyle;
	@FXML
	private RadioButton radioButton_lastStyle;
	@FXML
	private Label label_lastStyle;
	@FXML
	private RadioButton radioButton_inListStyle;
	@FXML
	private TreeTableView<TemplateSimpleItem> treeTableView_styles;
	@FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_name;
    @FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_description;
    @FXML
	private TreeTableColumn<TemplateSimpleItem, String> treeTableColumn_id;
	
    @FXML
	private Button button_Select;
	@FXML
	private Button button_Cancel;
	
	//
	private TemplateStyleItem styleDefault;
	private TemplateStyleItem styleLast;
	private TemplateStyleItem styleInList;
	
	// возвращаемые параметры
	public boolean isSelected = false;

	//
	Preferences prefs;
	
    /**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TemplateStyleSelect_Controller () {         }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {         }
	
    /**
     * Вызывается родительским обьектом, который даёт на себя ссылку.
     * Инициализирует контролы на слое.
     * 
     * @param 
     *        parentObj - обьект из которого вызывали это окно
     *        dialogStage
     */
    public void setParentObj(
    		Object parentObj, 
    		Stage dialogStage, 
    		DBConCur_Parameters conn,
    		long themeId,
    		long infoTypeId,
    		TemplateStyleItem styleSelected,
    		long rootStyleDirId) {
        this.dialogStage  = dialogStage;
        this.conn         = conn;
        this.styleSelected= styleSelected;
        this.themeId      = themeId;
        this.infoTypeId   = infoTypeId;
        this.rootStyleDirId = rootStyleDirId;
        
        // init controls
        initControlsValue();
    }
	
    /**
     * Инициализирует контролы значениями из главного класса
     */
    private void initControlsValue() {
    	prefs = Preferences.userNodeForPackage(TemplateStyleSelect_Controller.class);
    	
    	//-------- get default style 
    	styleDefault = conn.db.templateStyleGetDefault(themeId, infoTypeId);
    	
    	// check for exist template for default style
    	if (styleDefault != null) {
    		TemplateItem ti = conn.db.templateGet(themeId, styleDefault.getId());
    		if (ti == null) {
    			ShowAppMsg.showAlert("WARNING", "Предупреждение", 
    					"Для стиля по умолчанию "+styleDefault.getName()+" ("+styleDefault.getId()+") нет шаблона", 
    					"Стиль не может использоваться.");
    			styleDefault = null;
    		}
    	}
    
    	// output default style
    	if (styleDefault != null) {
    		label_defaultStyle.setText(styleDefault.getName()+" ("+styleDefault.getId()+")");
    	} else {
    		label_defaultStyle.setText("");
    		radioButton_lastStyle.setSelected(true);
    		radioButton_defaultStyle.setSelected(false);
    		radioButton_defaultStyle.setDisable(true);
    	}
    
    	//-------- get last style
    	styleLast = conn.db.templateStyleGetCurrent(themeId, infoTypeId, 1);
    
    	// check for exist template for last style
    	if ((styleLast != null) && ((styleLast.getType() == 0) || (styleLast.getType() == 10))) {
    		TemplateItem ti = conn.db.templateGet(themeId, styleLast.getId());
    		if (ti == null) {
    			ShowAppMsg.showAlert("WARNING", "Предупреждение", 
    					"Для последнего стиля "+styleLast.getName()+" ("+styleLast.getId()+") нет шаблона", 
    					"Стиль не может использоваться.");
    			styleLast = null;
    		}
    	}
    
    	// output last style
    	if (styleLast != null) {
    		label_lastStyle.setText(styleLast.getName()+" ("+styleLast.getId()+")");
    	} else {
    		label_lastStyle.setText("");
    		if (styleDefault != null)  radioButton_defaultStyle.setSelected(true);
    		else                       radioButton_inListStyle.setSelected(true);
    		radioButton_lastStyle.setSelected(false);
    		radioButton_lastStyle.setDisable(true);
    	}
    	
    	//-------- set radioButton_inListStyle if exist selectedStyle
    	if (styleSelected != null) {
    		styleInList = styleSelected;
    		radioButton_inListStyle.setSelected(true);
    	}
    	
    	//-------- init treeTableView_styles
    	// setCellValueFactory for columns
    	treeTableColumn_name.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getName())
    			);
    	treeTableColumn_description.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
    			);
    	treeTableColumn_id.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<TemplateSimpleItem, String> param) -> 
    			new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getId()))
    			);
    	
    	// set/get Pref Width for columns
    	treeTableColumn_id.setPrefWidth(prefs.getDouble("TemplateStyleSelect__treeTableColumn_id__PrefWidth", 75));

    	treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TemplateStyleSelect__treeTableColumn_id__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_name.setPrefWidth(prefs.getDouble("TemplateStyleSelect__treeTableColumn_name__PrefWidth", 300));

    	treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TemplateStyleSelect__treeTableColumn_name__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_description.setPrefWidth(prefs.getDouble("TemplateStyleSelect__treeTableColumn_description__PrefWidth", 130));

    	treeTableColumn_description.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TemplateStyleSelect__treeTableColumn_description__PrefWidth", t1.doubleValue());
            }
        });

    	// init 
    	final TreeItem<TemplateSimpleItem> root;
    	
    	if (rootStyleDirId == 0) { 
    		root = new TreeItem<>(new TemplateSimpleItem(
    				0, "Теми", "це корінь", themeId, TemplateSimpleItem.TYPE_ITEM_DIR_STYLE, 
    				(infoTypeId > 0) ? 1 : 11,
    				infoTypeId, new Date(), new Date(), "",""));
    	} else {
    		root = new TreeItem<>(conn.db.templateStyleGet(rootStyleDirId));
    	}
    	root.setExpanded(true);
    	treeTableView_styles.setShowRoot(false);
    	treeTableView_styles.setRoot(root);
    	addTreeItems(root);
    	
    	// Обработчик событий строки таблицы
    	treeTableView_styles.setRowFactory( tv -> {
    	    TreeTableRow<TemplateSimpleItem> row = new TreeTableRow<>();
    	    
    	    //-------- коннект при двойном клике
    	    row.setOnMouseClicked(event -> {
    	        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
					radioButton_inListStyle.setSelected(true);
    	        	handleButtonSelect();
    	        }
    	    });
    	    return row ;
    	});
    	
    	// cell factory to display graphic
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
            		
            		if ((row.getSubtypeItem() == 1) || (row.getSubtypeItem() == 11)) {
            			graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_section_style_16.png"));
            			
            		} else {
            			TemplateItem ti = conn.db.templateGet(themeId, row.getId());
            			if (ti == null) {
            				graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_style_empty_16.png"));
            			} else {
            				graphic = new ImageView(new Image("file:resources/images/icon_templates/icon_style_16.png"));
            			}
            		}
            		
            		if (conn.db.templateStyleIsDefault (row.getThemeId(), row.getId())) {
        				graphic_default = new ImageView(new Image("file:resources/images/icon_default_item_16.png"));
        				hBox = new HBox();
        				hBox.getChildren().addAll(graphic, graphic_default);
        				isDefault = true;
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

		//---- восстанавливаем сортировку таблицы по столбцу
		String sortColumnId = prefs.get("stageTemplateStyleSelect_sortColumnId","");

		if (! sortColumnId.equals("")) {
			for (TreeTableColumn column : treeTableView_styles.getColumns()) {
				if (column.getId().equals(sortColumnId)) {
					String sortType = prefs.get("stageTemplateStyleSelect_sortType","ASCENDING");

					treeTableView_styles.setSortMode(TreeSortMode.ALL_DESCENDANTS);
					column.setSortable(true); // This performs a sort
					treeTableView_styles.getSortOrder().add(column);
					if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
					else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
					treeTableView_styles.sort();
				}
			}
		}

    	//======== buttons
    	button_Select.setGraphic(new ImageView(new Image("file:resources/images/icon_select_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
    
    /**
     * Инициализация TreeTableView.
     * @param
     */
    private void addTreeItems(TreeItem<TemplateSimpleItem> ti) {
    	TemplateSimpleItem f = (TemplateSimpleItem) ti.getValue();
    
    	if (f != null) {
    		List<TemplateSimpleItem> styleList = conn.db.templateStyleListByParent (f);
    		
    		for (TemplateSimpleItem i : styleList) {
    			
    			//System.out.println(i.getName() +" "+ i.getName());
    			
    			i.setThemeId(f.getThemeId());
    			TreeItem<TemplateSimpleItem> subItem = new TreeItem<>(i);
    			ti.getChildren().add(subItem);
    			
    			// делаем текущий стиль активный в дереве
    			if ((styleInList != null) && (i.getId() == styleInList.getId())) { 
    				ti.setExpanded(true);
    				treeTableView_styles.getSelectionModel().select(subItem);
    				treeTableView_styles.scrollTo(treeTableView_styles.getRow(subItem));
    			}
    			
    			addTreeItems(subItem);
    		}
    	}
    }

	/**
	 * Сохраняем текущее состояние фрейма и контролов
	 */
	private void saveState() {

		//-------- size and position
		prefs.putDouble("stageTemplateStyleSelect_Width", dialogStage.getWidth());
		prefs.putDouble("stageTemplateStyleSelect_Height",dialogStage.getHeight());
		prefs.putDouble("stageTemplateStyleSelect_PosX",  dialogStage.getX());
		prefs.putDouble("stageTemplateStyleSelect_PosY",  dialogStage.getY());

		//-------- sort
		if (treeTableView_styles.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
			TreeTableColumn currentSortColumn = (TreeTableColumn) treeTableView_styles.getSortOrder().get(0);
			prefs.put("stageTemplateStyleSelect_sortColumnId",currentSortColumn.getId());
			prefs.put("stageTemplateStyleSelect_sortType",currentSortColumn.getSortType().toString());
		} else {
			prefs.remove("stageTemplateStyleSelect_sortColumnId");
			prefs.remove("stageTemplateStyleSelect_sortType");
		}
	}
    
    /**
     * Вызывается при нажатии на кнопке "Выбрать"
     */
    @FXML
    private void handleButtonSelect() {
    	if (radioButton_inListStyle.isSelected() && (treeTableView_styles.getSelectionModel().getSelectedItem() == null)) {
    		ShowAppMsg.showAlert("WARNING", "Выбор стиля", "Стиль не выбран.", "Выберите стиль.");
    		return;
    	}
    	
    	if (radioButton_inListStyle.isSelected() && (treeTableView_styles.getSelectionModel().getSelectedItem() != null)) {
    		TemplateSimpleItem tsi = treeTableView_styles.getSelectionModel().getSelectedItem().getValue();

    		if ((tsi.getSubtypeItem() == 0) || (tsi.getSubtypeItem() == 10)) {
    			TemplateItem ti = conn.db.templateGet(themeId, tsi.getId());
    			if (ti == null) {
    				ShowAppMsg.showAlert("WARNING", "Предупреждение", 
    						"Для стиля "+tsi.getName()+" ("+tsi.getId()+") нет шаблона", 
    						"Стиль не может использоваться.");
    				return;
    			}
    		}
    	}
    	
		saveState();      // save stage position and other

    	//-------- set variable for return
    	isSelected = true;
    	
    	if (radioButton_defaultStyle.isSelected()) {
    		styleSelected = styleDefault;
    		styleTypeSelection = 0;
    	}
    	
    	if (radioButton_lastStyle.isSelected()) {
    		styleSelected = styleLast;
    		styleTypeSelection = 1;
    	}
    	
    	if (radioButton_inListStyle.isSelected()) {
    		TemplateSimpleItem si = treeTableView_styles.getSelectionModel().getSelectedItem().getValue();
    		styleSelected = conn.db.templateStyleGet(si.getId());
    		styleTypeSelection = 2;
    		
    		// save current style in DB
    		conn.db.templateStyleEditCurrent(themeId, infoTypeId, si.getId(), 1);
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

		saveState();      // save stage position and other
    	
        // get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
