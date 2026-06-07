
package app.view.business.template;

import app.lib.ShowAppMsg;
import app.model.Params;
import app.model.business.template.TemplateThemeItem;

import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Контроллер окна выбора темы шаблонов
 * 
 * @author Igor Makarevich
 */
public class TemplateThemeSelect_Controller {
	
    private Params params;
    
    /**
     * Параметры текущей темы
     */
    private long themeIdCur;

    //
    @FXML
	private TreeTableView<TemplateThemeItem> treeTableView_themes;
	@FXML
	private TreeTableColumn<TemplateThemeItem, String> treeTableColumn_name;
    @FXML
	private TreeTableColumn<TemplateThemeItem, String> treeTableColumn_description;
    @FXML
	private TreeTableColumn<TemplateThemeItem, String> treeTableColumn_id;
	
    @FXML
	private Button button_Select;
	@FXML
	private Button button_Cancel;
	
	// возвращаемые параметры
	public boolean isSelected = false;
	public long themeIdRet = 0;

	//
	private Preferences prefs;

	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TemplateThemeSelect_Controller () {         }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {         }
	
    /**
     * Вызывается родительским обьектом, который даёт на себя ссылку.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params, long themeIdCur) {
        this.params      = params;
        this.themeIdCur  = themeIdCur;
        
        // init controls
        initControlsValue();
    }
	
    /**
     * Инициализирует контролы значениями из главного класса
     */
    private void initControlsValue() {
    	prefs = Preferences.userNodeForPackage(TemplateThemeSelect_Controller.class);

    	//======== TreeTableView ============================================================================
    	//-------- columns 
    	// setCellValueFactory
    	treeTableColumn_name.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<TemplateThemeItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getName())
    			);
    	treeTableColumn_description.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<TemplateThemeItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
    			);
    	treeTableColumn_id.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<TemplateThemeItem, String> param) -> 
    			new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getId()))
    			);
    	
    	// set/get Pref Width
    	treeTableColumn_id.setPrefWidth(prefs.getDouble("TemplateThemeSelect__treeTableColumn_id__PrefWidth", 75));

    	treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TemplateThemeSelect__treeTableColumn_id__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_name.setPrefWidth(prefs.getDouble("TemplateThemeSelect__treeTableColumn_name__PrefWidth", 300));

    	treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TemplateThemeSelect__treeTableColumn_name__PrefWidth", t1.doubleValue());
            }
        });
    	
    	treeTableColumn_description.setPrefWidth(prefs.getDouble("TemplateThemeSelect__treeTableColumn_description__PrefWidth", 130));

    	treeTableColumn_description.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TemplateThemeSelect__treeTableColumn_description__PrefWidth", t1.doubleValue());
            }
        });
    	
    	//-------- init 
    	final TreeItem<TemplateThemeItem> root;
   		root = new TreeItem<>(new TemplateThemeItem(0, "Темы", "это корень", new Date(), new Date(), "",""));
    	root.setExpanded(true);
    	treeTableView_themes.setShowRoot(false);
    	treeTableView_themes.setRoot(root);
    	addTreeItems(root);
    	
    	//======== Обработчик событий строки таблицы
    	treeTableView_themes.setRowFactory( tv -> {
    	    TreeTableRow<TemplateThemeItem> row = new TreeTableRow<>();
    	    
    	    //-------- коннект при двойном клике
    	    row.setOnMouseClicked(event -> {
    	        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
    	        	handleButtonSelect();
    	        }
    	    });
    	    return row ;
    	});

		//---- восстанавливаем сортировку таблицы по столбцу
		String sortColumnId = prefs.get("stageTemplateThemeSelect_sortColumnId","");

		if (! sortColumnId.equals("")) {
			for (TreeTableColumn column : treeTableView_themes.getColumns()) {
				if (column.getId().equals(sortColumnId)) {
					String sortType = prefs.get("stageTemplateThemeSelect_sortType","ASCENDING");

					treeTableView_themes.setSortMode(TreeSortMode.ALL_DESCENDANTS);
					column.setSortable(true); // This performs a sort
					treeTableView_themes.getSortOrder().add(column);
					if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
					else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
					treeTableView_themes.sort();
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
    private void addTreeItems(TreeItem<TemplateThemeItem> ti) {
    	TemplateThemeItem f = (TemplateThemeItem) ti.getValue();
    
    	if (f != null) {
    		List<TemplateThemeItem> themeList = params.getConCur().db.templateThemesList();
    		
    		for (TemplateThemeItem i : themeList) {
    			TreeItem<TemplateThemeItem> subItem = new TreeItem<>(i);
    			ti.getChildren().add(subItem);
    			
    			// делаем текущую тему активной в дереве
    			if (i.getId() == themeIdCur) { 
    				treeTableView_themes.getSelectionModel().select(subItem);
    			}
    		}
    	}
    }

	/**
	 * Сохраняем текущее состояние фрейма и контролов
	 */
	private void saveState() {

		//-------- size and position
		prefs.putDouble("stageTemplateThemeSelect_Width", params.getStageCur().getWidth());
		prefs.putDouble("stageTemplateThemeSelect_Height",params.getStageCur().getHeight());
		prefs.putDouble("stageTemplateThemeSelect_PosX",  params.getStageCur().getX());
		prefs.putDouble("stageTemplateThemeSelect_PosY",  params.getStageCur().getY());

		//-------- sort
		if (treeTableView_themes.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
			TreeTableColumn currentSortColumn = (TreeTableColumn) treeTableView_themes.getSortOrder().get(0);
			prefs.put("stageTemplateThemeSelect_sortColumnId",currentSortColumn.getId());
			prefs.put("stageTemplateThemeSelect_sortType",currentSortColumn.getSortType().toString());
		} else {
			prefs.remove("stageTemplateThemeSelect_sortColumnId");
			prefs.remove("stageTemplateThemeSelect_sortType");
		}
	}
    
    /**
     * Вызывается при нажатии на кнопке "Выбрать"
     */
    @FXML
    private void handleButtonSelect() {
    	if (treeTableView_themes.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Выбор темы", "Тема не выбрана.", "Выберите тему.");
    		return;
    	}
    	
    	TemplateThemeItem si = treeTableView_themes.getSelectionModel().getSelectedItem().getValue();

		saveState();      // save stage position and other

    	isSelected = true;
    	themeIdRet = si.getId();
    	
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
