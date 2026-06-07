
package app.view.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import app.lib.ShowAppMsg;
import app.model.Params;
import app.model.business.IconItem;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Контроллер окна выбора пиктограммы
 * 
 * @author Igor Makarevich
 */
public class IconSelect_Controller {
	
	private Params params;
    
    /**
     * Параметры текущего раздела
     */
    private long iconId;
    private long iconIdDef;
    private long iconIdLast;
    private long iconIdRoot;
    private boolean iconLastDisable = true;
    
    private int winType;   // тип сцены 0 - упрощенная ; 1 - с типом выбора
	
    //
    @FXML
	private RadioButton radioButton_IconDef;
    @FXML
	private ImageView imageView_IconDef;
	@FXML
	private Label label_IconDefName;
	@FXML
	private RadioButton radioButton_IconLast;
    @FXML
	private ImageView imageView_IconLast;
	@FXML
	private Label label_IconLastName;
	@FXML
	private RadioButton radioButton_IconInList;
    
    @FXML
	private TreeTableView<IconItem> treeTableView_icons;
	@FXML
	private TreeTableColumn<IconItem, String> treeTableColumn_name;
    @FXML
	private TreeTableColumn<IconItem, String> treeTableColumn_description;
    @FXML
	private TreeTableColumn<IconItem, String> treeTableColumn_id;
	
    @FXML
	private Button button_Select;
	@FXML
	private Button button_Cancel;
	
	// возвращаемые параметры
	public boolean isSelected = false;
	int typeSelection;           // 0 - дефолтный стиль ; 1 - текущий стиль ; 2 - стиль из списка
	public long iconIdRet = 0;

	//
	private Preferences prefs;

	// текущий выбраный элемент дерева
	private TreeItem<IconItem> curTreeItem;

	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public IconSelect_Controller () {   }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {    }
	
    /**
     * Вызывается родительским обьектом, которое даёт на себя ссылку.
     * Инициализирует контролы на слое.
     */
    public void setParams(
    		Params params, 
    		long iconId, long iconIdRoot, long iconIdDef) {
    	this.params      = params;
        this.iconId      = iconId;
        this.iconIdDef   = iconIdDef;
        this.iconIdRoot  = iconIdRoot;
        
        winType = (iconIdDef == 0) ? 0 : 1;
        
        // init controls
        initControlsValue();
    }
    
    /**
     * Вызывается родительским обьектом, которое даёт на себя ссылку.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params, long iconId, long iconIdRoot) {
    	setParams(params, iconId, iconIdRoot, 0);
    }
	
    /**
     * Инициализирует контролы значениями из главного класса
     */
    private void initControlsValue() {
    	prefs = Preferences.userNodeForPackage(IconSelect_Controller.class);
    	
    	//======== Show icons and icons names
    	if (winType > 0) {
    		// default icon
    		IconItem iiDef = params.getConCur().db.iconGetById(iconIdDef);
    		imageView_IconDef.setImage(iiDef.image);
    		label_IconDefName.setText(iiDef.getName() +" ("+ iiDef.getId() +")");
    	
    		// last icon
    		IconItem iiLast = params.getConCur().db.iconGetCurrent();
        	if (iiLast != null) {
        		iconIdLast = iiLast.getId();
        		imageView_IconLast.setImage(iiLast.image);
        		label_IconLastName.setText(iiLast.getName() +" ("+ iiLast.getId() +")");
        	} else {
        		iconIdLast = 0;
        		label_IconLastName.setText("");
        	}
        	
        	// проверяем, попадает ли последняя иконка в указанное поддерево
        	checkIconLastInSubTreeRecursive (iconIdRoot);
        	radioButton_IconLast.setDisable(iconLastDisable);
        	
        	// select radio button
        	if (iconId == 0) {
    			radioButton_IconDef.setSelected(true);
    		} else {
    			radioButton_IconInList.setSelected(true);
    		}
    	}

    	//======== TreeTableView ============================================================================
		//-------- columns
    	// setCellValueFactory
    	treeTableColumn_name.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<IconItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getName())
    			);
    	treeTableColumn_description.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<IconItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
    			);
    	treeTableColumn_id.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<IconItem, String> param) -> 
    			new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getId()))
    			);
    	
    	// set/get Pref Width
    	treeTableColumn_name.setPrefWidth(prefs.getDouble("IconSelect__treeTableColumn_id__PrefWidth", 75));

    	treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("IconSelect__treeTableColumn_id__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_name.setPrefWidth(prefs.getDouble("IconSelect__treeTableColumn_name__PrefWidth", 300));

    	treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("IconSelect__treeTableColumn_name__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_description.setPrefWidth(prefs.getDouble("IconSelect__treeTableColumn_description__PrefWidth", 130));

    	treeTableColumn_description.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("IconSelect__treeTableColumn_description__PrefWidth", t1.doubleValue());
            }
        });

    	//-------- init
    	final TreeItem<IconItem> root;
    	IconItem iconRoot = null;
    	
    	if (iconIdRoot > 0) {
    		iconRoot = params.getConCur().db.iconGetById(iconIdRoot);
    	}
    	
    	if ((iconIdRoot == 0) || (iconRoot == null)) {
    		root = new TreeItem<>(new IconItem(
    	    	         			0, 0, "Пиктограммы", "это корень", 
    	    	         			new Image("file:resources/images/icon_CatalogIcons_24.png"),
    	    	         			new Date(), new Date(), "",""));
    	} else {
   			root = new TreeItem<>(new IconItem(params.getConCur().db.iconGetById(iconIdRoot)));
    	}
    	root.setExpanded(true);
    	treeTableView_icons.setShowRoot(false);
    	treeTableView_icons.setRoot(root);
    	addTreeItemsRecursive(root);
    	
    	// cell factory to display graphic
    	treeTableColumn_name.setCellFactory(ttc -> new TreeTableCell<IconItem, String>() {
    		private IconItem row;
    		private ImageView graphic;

            @Override
            protected void updateItem(String item, boolean empty) {    // display graphic
            	try {
            		row = getTreeTableRow().getItem();
            		graphic = new ImageView(row.image);
            	} catch (NullPointerException e) {
                    //e.printStackTrace();
                    graphic = null;
                }
            	
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setGraphic(empty ? null : graphic);
            }
        });

        //---- восстанавливаем сортировку таблицы по столбцу  - портит выбор текущего элемента
/*        String sortColumnId = prefs.get("stageIconSelect_sortColumnId","");

        if (! sortColumnId.equals("")) {
            for (TreeTableColumn column : treeTableView_icons.getColumns()) {
                if (column.getId().equals(sortColumnId)) {
                    String sortType = prefs.get("stageIconSelect_sortType","ASCENDING");

                    treeTableView_icons.setSortMode(TreeSortMode.ALL_DESCENDANTS);
                    column.setSortable(true); // This performs a sort
                    treeTableView_icons.getSortOrder().add(column);
                    if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
                    else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
                    treeTableView_icons.sort();
                }
            }
        }
*/
        //---- устанавливаем текущую иконку
		if (curTreeItem != null) {
			// выбираем цепочку итемов от детеныша до корня
			List<TreeItem<IconItem>> tmpList = new ArrayList<>();
			TreeItem<IconItem> tmpItem = curTreeItem.getParent();

			// раскрывает ветки
			while (tmpItem != null) {
				tmpList.add(tmpItem);
				tmpItem = tmpItem.getParent();
			}
			for (int j = tmpList.size()-1; j>=0; j--) {
				tmpList.get(j).setExpanded(true);
			}

			// делаем активной
			treeTableView_icons.getSelectionModel().select(curTreeItem);
		}

        //======== Обработчик событий строки таблицы
    	treeTableView_icons.setRowFactory( tv -> {
    	    TreeTableRow<IconItem> row = new TreeTableRow<>();
    	    
    	    //-------- коннект при двойном клике
    	    row.setOnMouseClicked(event -> {
    	        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
					if (winType != 0) {
						radioButton_IconInList.setSelected(true);
					}
    	        	handleButtonSelect();
    	        }
    	    });
    	    return row ;
    	});
    	
    	//======== buttons
    	button_Select.setGraphic(new ImageView(new Image("file:resources/images/icon_select_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
    
    /**
     * Ищется рекурсивно иконка в поддереве иконок
     */
    private void checkIconLastInSubTreeRecursive (long iconIdRoot_cur) {
    	
    	if ((iconIdRoot_cur == iconIdLast) || (! iconLastDisable)) {
    		iconLastDisable = false;
    		return;
    	}
    	
    	List<IconItem> iconsList = params.getConCur().db.iconListByParentId(iconIdRoot_cur);
    	for (IconItem i : iconsList) {
    		checkIconLastInSubTreeRecursive (i.getId());
    	}
    }
    
    /**
     * Инициализация TreeTableView. Рекурсия по дереву. 
     * @param
     */
    private void addTreeItemsRecursive(TreeItem<IconItem> ti) {
    	IconItem f = (IconItem) ti.getValue();
    
    	if (f != null) {
    		List<IconItem> iconsList = params.getConCur().db.iconListByParentId(f.getId());
    		
    		for (IconItem i : iconsList) {
    			TreeItem<IconItem> subItem = new TreeItem<>(i);
    			ti.getChildren().add(subItem);
    			
    			// делаем текущую иконку активной в дереве
    			if (i.getId() == iconId) {
					curTreeItem = subItem;
    			}
    			
    			addTreeItemsRecursive(subItem);
    		}
    	}
    }

	/**
	 * Сохраняем текущее состояние фрейма и контролов
	 */
	private void saveState() {

		//-------- size and position
		prefs.putDouble("stageIconSelect_Width", params.getStageCur().getWidth());
		prefs.putDouble("stageIconSelect_Height",params.getStageCur().getHeight());
		prefs.putDouble("stageIconSelect_PosX",  params.getStageCur().getX());
		prefs.putDouble("stageIconSelect_PosY",  params.getStageCur().getY());

		//-------- sort
		if (treeTableView_icons.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
			TreeTableColumn currentSortColumn = (TreeTableColumn) treeTableView_icons.getSortOrder().get(0);
			prefs.put("stageIconSelect_sortColumnId",currentSortColumn.getId());
			prefs.put("stageIconSelect_sortType",currentSortColumn.getSortType().toString());
		} else {
			prefs.remove("stageIconSelect_sortColumnId");
			prefs.remove("stageIconSelect_sortType");
		}
	}
    
    /**
     * Вызывается при нажатии на кнопке "Выбрать"
     */
    @FXML
    private void handleButtonSelect() {
    	//if (((winType == 0) || (! radioButton_IconDef.isSelected())) &&
		if (((winType == 0) || radioButton_IconInList.isSelected()) &&
    		(treeTableView_icons.getSelectionModel().getSelectedItem() == null)) {
    		ShowAppMsg.showAlert("WARNING", "Выбор пиктограммы", "Пиктограмма не выбрана.", "Выберите пиктограмму.");
    		return;
    	}

		saveState();  // save stage position and other

    	isSelected = true;
    	if (winType != 0) {
    		if (radioButton_IconDef.isSelected()) {
    			iconIdRet = 0;
        		typeSelection = 0;
    		}
    		
    		if (radioButton_IconLast.isSelected()) {
    			iconIdRet = iconIdLast;
    			typeSelection = 1;
        	}
    		
    		if (radioButton_IconInList.isSelected()) {
        		IconItem ii = treeTableView_icons.getSelectionModel().getSelectedItem().getValue();
        		iconIdRet = ii.getId();
        		typeSelection = 2;
        		
        		// save current style in DB
        		params.getConCur().db.iconEditCurrent(ii.getId());
        	}
    	} else {
    		IconItem si = treeTableView_icons.getSelectionModel().getSelectedItem().getValue();
    		iconIdRet = si.getId();
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
