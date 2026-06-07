package app.module.scheduler.view;

import app.Main;
import app.exceptions.ServiceException;
import app.lib.ShowAppMsg;
import app.lib.StringUtil;
import app.model.AppItem_Interface;
import app.model.Params;
import app.model.StateItem;
import app.model.StateList;
import app.module.scheduler.TaskControl;
import app.module.scheduler.TaskControlFactory;
import app.module.scheduler.TaskItem;
import app.module.scheduler.db.DBScheduler;
import app.util.FormattedDate;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeSortMode;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

/**
 * Контролер списку завдань для запуску
 * v.2.01.00.002 2024-12-02 - 2025-08-30
 */
public class TaskList_Controller implements AppItem_Interface {
	private Params params;
	
	@FXML
	private Button button_Exit;
    @FXML
    private TitledPane titledPane_Title;
    
    @FXML
    private SplitPane splitPane_main;
    
    @FXML
	public TreeTableView<TaskItem> treeTableView_Task;
	@FXML
	private TreeTableColumn<TaskItem, Long> treeTableColumn_id;
	@FXML
	private TreeTableColumn<TaskItem, Long> treeTableColumn_typeId;
	@FXML
	private TreeTableColumn<TaskItem, String> treeTableColumn_name;
	@FXML
	private TreeTableColumn<TaskItem, String> treeTableColumn_descr;
	@FXML
	private TreeTableColumn<TaskItem, FormattedDate> treeTableColumn_date_created;
	@FXML
	private TreeTableColumn<TaskItem, FormattedDate> treeTableColumn_date_modified;
	@FXML
	private TreeTableColumn<TaskItem, String> treeTableColumn_start;
	@FXML
	private TreeTableColumn<TaskItem, String> treeTableColumn_interval;
	
	@FXML
	private TabPane tabPaneDetail;
	@FXML
	private Tab tabDetailDirectory;
	@FXML
	private Tab tabDetailTask;
	
	@FXML
    private ToggleButton detailsDir_Button_AddMode;
	@FXML
    private Button detailsDir_Button_Save;
	@FXML
    private Button detailsDir_Button_Delete;
	@FXML
	private Label detailsDir_Label_id;
	@FXML
	private Label detailsDir_Label_parentId;
	@FXML
	private TextField detailsDir_TextField_Name;
	@FXML
	private TextField detailsDir_TextField_Descr;
	@FXML
	private Label detailsDir_Label_DateCreated;
	@FXML
	private Label detailsDir_Label_DateModified;
	
	@FXML
    private ToggleButton detailsTask_Button_AddMode;
	@FXML
    private Button detailsTask_Button_Save;
	@FXML
    private Button detailsTask_Button_Delete;
	@FXML
    private Button detailsTask_Button_Run;
	@FXML
    private Button detailsTask_Button_RunNow;
	@FXML
    private Button detailsTask_Button_Stop;
	@FXML
    private Button detailsTask_Button_Disable;
	@FXML
	private Label detailsTask_Label_id;
	@FXML
	private Label detailsTask_Label_dirId;
	@FXML
	private TextField detailsTask_TextField_Name;
	@FXML
	private TextField detailsTask_TextField_Descr;
	@FXML
	private CheckBox detailsTask_CheckBox_isShowMsgStart;
	@FXML
	private CheckBox detailsTask_CheckBox_isShowMsgFinish;
	@FXML
	private Label detailsTask_Label_DateCreated;
	@FXML
	private Label detailsTask_Label_DateModified;
	@FXML
	private Label detailsTask_Label_Type;
	@FXML
	private ComboBox<String> detailsTask_ComboBox_Type;
	@FXML
	private Label detailsTask_Label_State;
	@FXML
	private ComboBox<String> detailsTask_ComboBox_State;
	@FXML
	private TextField detailsTask_TextField_StartHour;
	@FXML
	private TextField detailsTask_TextField_StartMinute;
	@FXML
	private TextField detailsTask_TextField_Interval;
	
	@FXML
	private Tab tabDetailsTask_spec;
	
	// for detailsTask_ComboBox_...
	private ObservableList<String> oList_DetailsTaskType;
	private ObservableList<String> oList_DetailsTaskState;
	
    //
	private Preferences prefs;
	//
	private DBScheduler db;
	/**
	 * тип поточного завдання по якому показується деталізація (без директорій)
	 */
	private long currentTypeId;
	/**
	 * поточний контролер Специфічної деталізації
	 */
	private TaskDetail_Simple_Controller controllerSrecific;
	
	//
	public TaskList_Controller.TreeView_Controller treeViewCtrl;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public TaskList_Controller () {
    	prefs = Preferences.userNodeForPackage(TaskList_Controller.class);
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
    	this.params = params;
        db = params.getScheduler().getDb();
    	params.getScheduler().setController(this);
    	
        // init controls
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями 
     */
    private void initControlsValue() {
    	button_Exit.setTooltip(new Tooltip("Закрити фрейм"));
    	
    	//======== splitPane_main
    	splitPane_main.setDividerPositions(prefs.getDouble("TaskList_splitPane_main_position", 0.7));
    	
    	splitPane_main.getDividers().get(0).positionProperty().addListener(
                o -> {
                	prefs.putDouble("TaskList_splitPane_main_position", splitPane_main.getDividerPositions()[0]);
                }
        );
    	
    	//========
    	initTableTasks();
    	
    	//======== show icons
    	detailsDir_Button_AddMode.setTooltip(new Tooltip("Режим створення нової директорії"));
    	detailsDir_Button_AddMode.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
    	detailsDir_Button_Save.setTooltip(new Tooltip("Зберегти зміни"));
    	detailsDir_Button_Save.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	detailsDir_Button_Delete.setTooltip(new Tooltip("Вилучення директорії"));
    	detailsDir_Button_Delete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	
    	detailsTask_Button_AddMode.setTooltip(new Tooltip("Режим створення нового завдання"));
    	detailsTask_Button_AddMode.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
    	detailsTask_Button_Save.setTooltip(new Tooltip("Зберегти зміни"));
    	detailsTask_Button_Save.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	detailsTask_Button_Delete.setTooltip(new Tooltip("Вилучення завдання"));
    	detailsTask_Button_Delete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	detailsTask_Button_Run.setTooltip(new Tooltip("Run task"));
    	detailsTask_Button_Run.setGraphic(new ImageView(new Image("file:resources/images/scheduler/icon_task_act_run_16.png")));
    	detailsTask_Button_RunNow.setTooltip(new Tooltip("Run now task"));
    	detailsTask_Button_RunNow.setGraphic(new ImageView(new Image("file:resources/images/scheduler/icon_task_act_run_now_16.png")));
    	detailsTask_Button_Stop.setTooltip(new Tooltip("Stop task"));
    	detailsTask_Button_Stop.setGraphic(new ImageView(new Image("file:resources/images/scheduler/icon_task_act_stop_16.png")));
    	detailsTask_Button_Disable.setTooltip(new Tooltip("Disable task"));
    	detailsTask_Button_Disable.setGraphic(new ImageView(new Image("file:resources/images/scheduler/icon_task_act_disable_16.png")));
    	
    	// init details controls
    	detailsDir_Label_id.setText("");
		detailsDir_Label_parentId.setText("");
		detailsDir_Label_DateCreated.setText("");
		detailsDir_Label_DateModified.setText("");
		
		detailsTask_Label_id.setText("");
		detailsTask_Label_dirId.setText("");
		detailsTask_Label_DateCreated.setText("");
		detailsTask_Label_DateModified.setText("");
		
		oList_DetailsTaskType = db.taskTypeListCombo();
		detailsTask_ComboBox_Type.setItems(oList_DetailsTaskType);
    	detailsTask_ComboBox_Type.setValue(oList_DetailsTaskType.get(0));    // первый эдемент в списке
    	
    	oList_DetailsTaskState = FXCollections.observableArrayList();
    	oList_DetailsTaskState.add("disable (0)");
    	oList_DetailsTaskState.add("enable (1)");
    	
    	detailsTask_ComboBox_State.setItems(oList_DetailsTaskState);
    	detailsTask_ComboBox_State.setValue(oList_DetailsTaskState.get(0));    // первый эдемент в списке
    	
    	// обробник вибору типу таска
    	detailsTask_ComboBox_Type.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
    	    if (newVal != null) {
    	    	long newTypeId = StringUtil.getIdFromComboName(newVal);
    	    	
    	    	if (currentTypeId != newTypeId) {   // міняємо сцену специфіки
    	    		TaskControl tc = (new TaskControlFactory()).newTaskControl(0, newTypeId, params, TaskControl.INIT_EMPTY);
    	    		String fxmlFileName = tc.getFxmlFileName();
    	    		AnchorPane pane = null;
    	    		
    	    		try {
    	    	    	// Загружаем fxml-файл и создаём новую сцену
    	    			FXMLLoader loader = new FXMLLoader();
    	    			loader.setLocation(Main.class.getResource(fxmlFileName));
    	    			pane = loader.load();
    	    			
    	    			// Даём контроллеру доступ к родителю и инициализируем
    	    			controllerSrecific = loader.getController();
    	    			
    	    			Params params = new Params(this.params);
    	    			params.setParentObj(this);
    	    			
    	    			controllerSrecific.setParams(params, tc);
    	        	} catch (IOException e) {
    	                e.printStackTrace();
    	                ShowAppMsg.showAlert("WARNING", "Показ Детальної специфіки", "Помилка при відкритті панелі", 
    	    		             e.getMessage());
    	            }
    	        	
    	        	//---- добавляем в под-таб сцену
    	        	tabDetailsTask_spec.setContent(pane);
    	        	currentTypeId = newTypeId;
    	    	}
    	    }
    	});
    }
    
    /**
     * Ініціалізуємо таблицю з завданнями
     */
    private void initTableTasks() {
    	//-------- columns 
    	// setCellValueFactory
    	treeTableColumn_id.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<TaskItem, Long> param) -> 
    			new ReadOnlyObjectWrapper<>(param.getValue().getValue().getId())
    			);
    	treeTableColumn_name.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<TaskItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getName())
    			);
    	treeTableColumn_descr.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<TaskItem, String> param) -> 
    			new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
    			);
    	treeTableColumn_date_created.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<TaskItem, FormattedDate> param) -> {
                    Date dateCreated = param.getValue().getValue().getDateCreated();
                    FormattedDate formattedDate = new FormattedDate(dateCreated);
                    return new ReadOnlyObjectWrapper<>(formattedDate);
                }
            );
    	treeTableColumn_date_modified.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<TaskItem, FormattedDate> param) -> {
                    Date dateModified = param.getValue().getValue().getDateModified();
                    FormattedDate formattedDate = new FormattedDate(dateModified);
                    return new ReadOnlyObjectWrapper<>(formattedDate);
                }
            );
    	treeTableColumn_start.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<TaskItem, String> param) -> 
    			new ReadOnlyStringWrapper(
    					(param.getValue().getValue().getTypeId() > 0) ?
    					String.format("%02d:%02d", 
    						param.getValue().getValue().getControl().getStartHour(), 
    						param.getValue().getValue().getControl().getStartMinute())
    					: ""
    			)
    		);
    	treeTableColumn_interval.setCellValueFactory(
    			(TreeTableColumn.CellDataFeatures<TaskItem, String> param) -> 
    			new ReadOnlyStringWrapper(
    					(param.getValue().getValue().getTypeId() > 0) ?
    					showGoodMinutes (param.getValue().getValue().getControl().getInterval()) : ""
    			)
    		);

    	// set/get Pref Width
    	treeTableColumn_id.setPrefWidth(prefs.getDouble("TaskView__treeTableColumn_id__PrefWidth", 50));
    	treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TaskView__treeTableColumn_id__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_name.setPrefWidth(prefs.getDouble("TaskView__treeTableColumn_name__PrefWidth", 200));
    	treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TaskView__treeTableColumn_name__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_descr.setPrefWidth(prefs.getDouble("TaskView__treeTableColumn_descr__PrefWidth", 200));
    	treeTableColumn_descr.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TaskView__treeTableColumn_descr__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_date_created.setPrefWidth(prefs.getDouble("TaskView__treeTableColumn_date_created__PrefWidth", 150));
    	treeTableColumn_date_created.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TaskView__treeTableColumn_date_created__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_date_modified.setPrefWidth(prefs.getDouble("TaskView__treeTableColumn_date_modified__PrefWidth", 150));
    	treeTableColumn_date_modified.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TaskView__treeTableColumn_date_modified__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_start.setPrefWidth(prefs.getDouble("TaskView__treeTableColumn_start__PrefWidth", 50));
    	treeTableColumn_start.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TaskView__treeTableColumn_start__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_interval.setPrefWidth(prefs.getDouble("TaskView__treeTableColumn_interval__PrefWidth", 50));
    	treeTableColumn_interval.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("TaskView__treeTableColumn_interval__PrefWidth", t1.doubleValue());
            }
        });
    	
    	//======== load data
    	TreeItem<TaskItem> root;
		root = new TreeItem<>(new TaskItem(0, -1, -1, "Все", "це корінь, він не редагується", false, false,
				new Date(), new Date(), db, params));

		initTreeItemsRecursive(root);

		treeTableView_Task.setShowRoot(true);
		treeTableView_Task.setRoot(root);
		root.setExpanded(false);
    	
    	//======= CellFactory - показ іконок
		treeTableColumn_name.setCellFactory(ttc -> new TreeTableCell<TaskItem, String>() {
			private TaskItem row;
			private ImageView graphic;

			@Override
			protected void updateItem(String item, boolean empty) {    // display graphic
				try {
					row = getTreeTableRow().getItem();
					switch ((int)row.getTypeId()) {
					case -1:
						graphic = new ImageView(new Image("file:resources/images/scheduler/icon_scheduler_16.png"));
						break;
					case TaskItem.TYPE_ITEM_DIR :
						graphic = new ImageView(new Image("file:resources/images/scheduler/icon_directory_16.png"));
						break;
					default :      // усі види завдань, показуємо статуси
						if (row.getControl().getState() == row.getControl().STATE_DISABLE) {
							graphic = new ImageView(new Image("file:resources/images/scheduler/icon_task_disabled_16.png"));
						} else if (row.getControl().getState() == row.getControl().STATE_ENABLE) {
							graphic = new ImageView(new Image("file:resources/images/scheduler/icon_task_enabled_16.png"));
						} else if (row.getControl().getState() == row.getControl().STATE_RUNNING) {
							graphic = new ImageView(new Image("file:resources/images/scheduler/icon_task_running_16.png"));
						}
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
    	
		// Слушаем изменения выбора, и при изменении отображаем информацию .
    	treeTableView_Task.getSelectionModel().selectedItemProperty().addListener(
    			(observable, oldValue, newValue) -> onChangeSelectedItem(newValue));
    	
    	// востановлюємо сортування в таблиці, та ставимо слухача
    	initSortColumn ();
    	
    	//
    	treeViewCtrl.init();
    }
    
    /**
	 * востановлюємо сортування в таблиці, та ставимо слухачів
	 */
	public void initSortColumn () {
		//-------- востановлюємо сортування
		String sortColumnId = prefs.get("TaskList_sortColumnId","");

		if (! sortColumnId.equals("")) {
			for (TreeTableColumn column : treeTableView_Task.getColumns()) {
				if (column.getId().equals(sortColumnId)) {
					String sortType = prefs.get("TaskList_sortType","ASCENDING");

					treeTableView_Task.setSortMode(TreeSortMode.ALL_DESCENDANTS);
					column.setSortable(true); // This performs a sort
					treeTableView_Task.getSortOrder().add(column);
					if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
					else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
					treeTableView_Task.sort();
				}
			}
		}
		
		//-------- ставимо слухачів на зміну колонки та типу сортування
		treeTableView_Task.getSortOrder().addListener((ListChangeListener<TreeTableColumn>) change -> {
		    if (!change.getList().isEmpty()) {
		        TreeTableColumn <TaskItem, ?> sortedColumn = change.getList().get(0);
		        prefs.put("TaskList_sortColumnId", sortedColumn.getId());
		        prefs.put("TaskList_sortType", sortedColumn.getSortType().toString()); // "ASCENDING" або "DESCENDING"
		        //System.out.println("sortedColumn.getSortType().toString() = "+ sortedColumn.getSortType().toString());
		    }
		});
		
		for (TreeTableColumn<TaskItem, ?> column : treeTableView_Task.getColumns()) {
		    column.sortTypeProperty().addListener((obs, oldVal, newVal) -> {
		        if (treeTableView_Task.getSortOrder().contains(column)) {
		            prefs.put("TaskList_sortColumnId", column.getId());
		            prefs.put("TaskList_sortType", newVal.toString());
		            //System.out.println("Збережено: " + column.getId() + " -> " + newVal);
		        }
		    });
		}
		

		//-------- слухач на розкриття вузлів дерева
		treeTableView_Task.setRowFactory(tv -> {
		    TreeTableRow<TaskItem> row = new TreeTableRow<>();

		    row.treeItemProperty().addListener((obs, oldItem, newItem) -> {
		        if (newItem != null) {
		            newItem.expandedProperty().addListener((expandedObs, wasExpanded, isNowExpanded) -> {
		                if (isNowExpanded) {
		                    treeTableView_Task.sort(); // повторне сортування після розкриття
		                }
		            });
		        }
		    });

		    return row;
		});
	}
    
    /**
	 * Завантажуємо дані в дерево-контрол рекурсивно
	 */
	private void initTreeItemsRecursive (TreeItem<TaskItem> ti) {
		TaskItem f = ti.getValue();
		List<TaskItem> tList;

		if (f != null) {
			tList = db.dirListByParent (f);

			for (TaskItem i : tList) {
				//directories
				TreeItem<TaskItem> subItem = new TreeItem<>(i);
				ti.getChildren().add(subItem);
				
				//
				initTreeItemsRecursive (subItem);
			}
			
			//tasks
			List<TaskItem> taskList = db.taskListByDir (f, params);
			for (TaskItem j : taskList) {
				j.getControl().setState(params.getScheduler().getTasks().get(j.getId()).getControl().getState());
				TreeItem<TaskItem> taskItem = new TreeItem<>(j);
				ti.getChildren().add(taskItem);
			}
		}
	}
	
	/**
     * Перетворює кількість хвилин у форматовану строку типу "X год. Y хв."
     * @param totalMinutes кількість хвилин
     * @return форматована строка
     */
	private String showGoodMinutes (int totalMinutes) {
		int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        StringBuilder formattedTime = new StringBuilder();
        
        if (hours > 0) {
            formattedTime.append(hours).append(" год. ");
        }
        formattedTime.append(minutes).append(" хв.");
        
        return formattedTime.toString();
	}
    
    /**
     * Викликається при виборі Завдання зі списку.
     */
    private void onChangeSelectedItem(TreeItem<TaskItem> ti) {
    	if (ti != null) {
    		TaskItem i = ti.getValue();
    		
    		if ((i.getTypeId() == TaskItem.TYPE_ITEM_DIR) && (! detailsDir_Button_AddMode.isSelected())) {
    			//tabDetailDirectory.setDisable(false);
    			//tabDetailTask.setDisable(true);
    			tabPaneDetail.getSelectionModel().select(tabDetailDirectory);
    			
    			detailsDir_Label_id.setText(String.valueOf(i.getId()));
    			detailsDir_Label_parentId.setText(String.valueOf(i.getParentId()));
    			detailsDir_TextField_Name.setText(i.getName());
    			detailsDir_TextField_Descr.setText(i.getDescr());
    			
    			FormattedDate fdDateCreated = new FormattedDate(i.getDateCreated()); 
    			FormattedDate fdDateModified = new FormattedDate(i.getDateModified());
    			detailsDir_Label_DateCreated.setText(fdDateCreated.toString());
    			detailsDir_Label_DateModified.setText(fdDateModified.toString());
    		}
    		if ((i.getTypeId() > TaskItem.TYPE_ITEM_DIR) && (! detailsTask_Button_AddMode.isSelected())) {
    			//tabDetailDirectory.setDisable(true);
    			//tabDetailTask.setDisable(false);
    			tabPaneDetail.getSelectionModel().select(tabDetailTask);
    			
    			// set control's buttons status
    			switch (i.getControl().getState()) {
    			case TaskControl.STATE_DISABLE :
    				detailsTask_Button_Run.setDisable(false);
    			    detailsTask_Button_RunNow.setDisable(false);
    			    detailsTask_Button_Stop.setDisable(true);
    			    detailsTask_Button_Disable.setDisable(true);
    				break;
    			case TaskControl.STATE_ENABLE :
    				detailsTask_Button_Run.setDisable(true);
    			    detailsTask_Button_RunNow.setDisable(true);
    			    detailsTask_Button_Stop.setDisable(true);
    			    detailsTask_Button_Disable.setDisable(false);
    				break;
    			case TaskControl.STATE_RUNNING :
    				detailsTask_Button_Run.setDisable(true);
    			    detailsTask_Button_RunNow.setDisable(true);
    			    detailsTask_Button_Stop.setDisable(false);
    			    detailsTask_Button_Disable.setDisable(true);
    				break;
    			}
    			
    			//
    			detailsTask_Label_id.setText(String.valueOf(i.getId()));
    			detailsTask_Label_dirId.setText(String.valueOf(i.getParentId()));
    			detailsTask_TextField_Name.setText(i.getName());
    			detailsTask_TextField_Descr.setText(i.getDescr());
    			detailsTask_CheckBox_isShowMsgStart.setSelected(i.isShowMsgStart());
    			detailsTask_CheckBox_isShowMsgFinish.setSelected(i.isShowMsgFinish());
    			
    			FormattedDate fdDateCreated = new FormattedDate(i.getDateCreated()); 
    			FormattedDate fdDateModified = new FormattedDate(i.getDateModified());
    			detailsTask_Label_DateCreated.setText(fdDateCreated.toString());
    			detailsTask_Label_DateModified.setText(fdDateModified.toString());

    			detailsTask_Label_Type.setText(db.taskTypeGetNameById(i.getControl().getTypeId()));
    			switch (i.getControl().getState()) {
    			case 0 :
    				detailsTask_Label_State.setText("disable");
    				break;
    			case 1 :
    				detailsTask_Label_State.setText("enable");
    				break;
    			case 2 :
    				detailsTask_Label_State.setText("running");
    				break;
    			}
    			
    			StringUtil.selectComboBoxItemById(detailsTask_ComboBox_Type, i.getControl().getTypeId());
    			detailsTask_ComboBox_Type.setVisible(false);
    			detailsTask_ComboBox_Type.setManaged(false);
    			StringUtil.selectComboBoxItemById(detailsTask_ComboBox_State, i.getControl().getState());
    			detailsTask_ComboBox_State.setVisible(false);
    			detailsTask_ComboBox_State.setManaged(false);
    			
    			detailsTask_TextField_StartHour.setText(Integer.toString(i.getControl().getStartHour()));
    			detailsTask_TextField_StartMinute.setText(Integer.toString(i.getControl().getStartMinute()));
    			detailsTask_TextField_Interval.setText(Integer.toString(i.getControl().getInterval()));
    			
    			showDetailsSpecific(i);
    		}
    	}
    }
    
    /**
     * 
     */
    private void showDetailsSpecific(TaskItem i) {
    	if (currentTypeId != i.getControl().getTypeId()) {
    		String fxmlFileName = i.getControl().getFxmlFileName();
    		AnchorPane pane = null;
    		
        	//---- завантажуємо контролер Специфіки в AnchorPane
        	try {
    	    	// Загружаем fxml-файл и создаём новую сцену
    			FXMLLoader loader = new FXMLLoader();
    			loader.setLocation(Main.class.getResource(fxmlFileName));
    			pane = loader.load();
    			
    			// Даём контроллеру доступ к родителю и инициализируем
    			controllerSrecific = loader.getController();
    			
    			Params params = new Params(this.params);
    			params.setParentObj(this);
    			
    			controllerSrecific.setParams(params, i.getControl());
        	} catch (IOException e) {
                e.printStackTrace();
                ShowAppMsg.showAlert("WARNING", "Показ Детальної специфіки", "Помилка при відкритті панелі", 
    		             e.getMessage());
            }
        	
        	//---- добавляем в под-таб сцену
        	tabDetailsTask_spec.setContent(pane);
        	currentTypeId = i.getControl().getTypeId();
    	} else {
    		Params params = new Params(this.params);
			params.setParentObj(this);
			
			controllerSrecific.setParams(params, i.getControl());
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
     * На панелі детальної інформації вибираємо режим Додати/Редагувати директорію
     */
    @FXML
    private void handleButtonDetailsDirAddMode() {
    	if (detailsDir_Button_AddMode.isSelected()) {   // Add mode
    		detailsDir_Label_id.setText("");
    		detailsDir_Label_parentId.setText("");
    		detailsDir_Label_DateCreated.setText("");
    		detailsDir_Label_DateModified.setText("");
    	} else {          // Update mode
    		onChangeSelectedItem (treeTableView_Task.getSelectionModel().getSelectedItem());
    	}
    }
    
    /**
     * На панелі детальної інформації зберігаємо інформацію по директорії
     */
    @FXML
    private void handleButtonDetailsDirSave() {
		TreeItem<TaskItem> tti = treeTableView_Task.getSelectionModel().getSelectedItem();
    	TaskItem i = null;
    	
    	//---------- check data in fields
    	if ((detailsDir_TextField_Name.getText().equals("") || (detailsDir_TextField_Name.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не заповнена Назва", "Вкажіть Назву");
    		return;
        }
    	
    	//-------- save
    	if (detailsDir_Button_AddMode.isSelected()) {   // Add
    		// брати не поточний ітем для батьківства, 
    		// а перевіряти його на тип директорії, і якщо потрібно піднятися на позицію угору в ієрархії
    		if (tti.getValue().getTypeId() > 0) {
    			tti = tti.getParent();
    		}
    		
    		// add to db
    		i = new TaskItem(
    				db.dirNextId(),
    				tti.getValue().getId(),      // parentId
					TaskItem.TYPE_ITEM_DIR,
					detailsDir_TextField_Name.getText(),
					detailsDir_TextField_Descr.getText(),
					false,
					false,
					null,
					null,
					db,
					params
					); 
    		db.dirAdd(i);
    		i = db.dirGetById(i.getId()); // get full info
    		i.setParams(params);
    		
    		// додаємо в контрол
    		TreeItem<TaskItem> item = new TreeItem<>(i);
    		tti.getChildren().add(item);
    		treeTableView_Task.sort();

        	// виводимо повідомлення в статус бар
    		params.setMsgToStatusBar("Директорія '" + i.getName() + "' додана.");
    	} else {              // update
    		if (tti != null) {
    			TaskItem ti = tti.getValue();
    			
    			i = new TaskItem(
    					ti.getId(),
    					ti.getParentId(),
    					TaskItem.TYPE_ITEM_DIR,
    					detailsDir_TextField_Name.getText(),
    					detailsDir_TextField_Descr.getText(),
    					false,
    					false,
    					ti.getDateCreated(),
    					null,
    					db,
    					params
    					);
    			db.dirUpdate(i);
    			i = db.dirGetById(i.getId()); // get full info
    			i.setParams(params);
            		
               	// update in TreeTableView
           		tti.setValue(null);
            	tti.setValue(i);
    			
                // виводимо повідомлення в статус бар
                params.setMsgToStatusBar("Директорія '" + i.getName() + "' змінена.");
    		}
    	}
    }
    
    /**
     * На панелі детальної інформації вилучаємо директорію
     */
    @FXML
    private void handleButtonDetailsDirDelete() {
    	TreeItem<TaskItem> selectedItem = treeTableView_Task.getSelectionModel().getSelectedItem();

    	// check
		if (selectedItem == null) {
			params.setMsgToStatusBar("Нічого не вибрано для вилучення.");
			return;
		}
		if (! selectedItem.getChildren().isEmpty()) {
			ShowAppMsg.showAlert("WARNING", "Вилучення директорії", "Директорію з нащадками неможливо вилучити", "");
			return;
		}
		
		TaskItem di = selectedItem.getValue();
		
		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Вилучення директорії", 
	            "Вилучення '"+ di.getName() +"'", "Вилучити ?"))
			return;
		
		// delete from DB
		di.delete();
		
		// delete from TreeTableView
		TreeItem<TaskItem> parentItem = selectedItem.getParent();
		if (parentItem != null) {     // 
			parentItem.getChildren().remove(selectedItem);
		}
				
		// виводимо повідомлення в статус бар
		params.setMsgToStatusBar("Директорія завдань '" + di.getName() + "' вилучена.");
    }
    
    /**
     * На панелі детальної інформації вибираємо режим Додати/Редагувати завдання
     */
    @FXML
    private void handleButtonDetailsTaskAddMode() {
    	if (detailsTask_Button_AddMode.isSelected()) {   // Add mode
    		detailsTask_Button_Run.setDisable(true);
    		detailsTask_Button_RunNow.setDisable(true);
    		detailsTask_Button_Stop.setDisable(true);
    		detailsTask_Button_Disable.setDisable(true);
    		
    		detailsTask_Label_id.setText("");
    		detailsTask_Label_dirId.setText("");
    		detailsTask_Label_DateCreated.setText("");
    		detailsTask_Label_DateModified.setText("");
    		
    		detailsTask_Label_Type.setVisible(false);
    		detailsTask_Label_Type.setManaged(false);
    		//detailsTask_Label_State.setVisible(false);
    		//detailsTask_Label_State.setManaged(false);
			detailsTask_ComboBox_Type.setVisible(true);
			detailsTask_ComboBox_Type.setManaged(true);
			//detailsTask_ComboBox_State.setVisible(true);
			//detailsTask_ComboBox_State.setManaged(true);
			//detailsTask_ComboBox_State.setDisable(true);
    		
			controllerSrecific.clearControlsValueForAddMode();
    	} else {          // Update mode
    		detailsTask_Button_Run.setDisable(false);
    		detailsTask_Button_RunNow.setDisable(false);
    		detailsTask_Button_Stop.setDisable(false);
    		detailsTask_Button_Disable.setDisable(false);
    		
    		detailsTask_Label_Type.setVisible(true);
    		detailsTask_Label_Type.setManaged(true);
    		detailsTask_Label_State.setVisible(true);
    		detailsTask_Label_State.setManaged(true);
			detailsTask_ComboBox_Type.setVisible(false);
			detailsTask_ComboBox_Type.setManaged(false);
			detailsTask_ComboBox_State.setVisible(false);
			detailsTask_ComboBox_State.setManaged(false);
    		
    		onChangeSelectedItem (treeTableView_Task.getSelectionModel().getSelectedItem());
    	}
    }
    
    /**
     * На панелі детальної інформації зберігаємо інформацію по завданню
     */
    @FXML
    private void handleButtonDetailsTaskSave() {
    	TreeItem<TaskItem> tti = treeTableView_Task.getSelectionModel().getSelectedItem();
    	TaskItem i = null;
    	
    	//---------- check data in fields
    	if ((detailsTask_TextField_Name.getText().equals("") || (detailsTask_TextField_Name.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не заповнена Назва", "Вкажіть Назву");
    		return;
        }
    	if ((detailsTask_TextField_StartHour.getText().equals("") || (detailsTask_TextField_StartHour.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не заповнений Час", "Вкажіть Час");
    		return;
        }
    	if ((detailsTask_TextField_StartMinute.getText().equals("") || (detailsTask_TextField_StartMinute.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не заповнені Хвилини", "Вкажіть Хвилини");
    		return;
        }
    	if ((detailsTask_TextField_Interval.getText().equals("") || (detailsTask_TextField_Interval.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Відсутні дані", "Не заповнений Інтервал", "Вкажіть Інтервал");
    		return;
        }
    	
    	if (! controllerSrecific.checkControlsValue())  return;
    	
    	//-------- save
    	if (detailsTask_Button_AddMode.isSelected()) {   // Add
    		// брати не поточний ітем для батьківства, 
    		// а перевіряти його на тип директорії, і якщо потрібно піднятися на позицію угору в ієрархії
    		if (tti.getValue().getTypeId() > 0) {
    			tti = tti.getParent();
    		}
    		
    		//
    		long newTaskId = db.taskNextId();
    		long newTypeId = StringUtil.getIdFromComboName(detailsTask_ComboBox_Type.getValue());
    		
    		// створюємо ітем-контроль, ініціалізуємо, додаємо в БД
    		TaskControl tc = (new TaskControlFactory()).newTaskControl(newTaskId, newTypeId, params, TaskControl.INIT_EMPTY); 
    		tc.setId(db.taskControlNextId());
    		tc.setTypeId(newTypeId);
			tc.setState(TaskControl.STATE_DISABLE);
    		try {
    		    tc.setStartHour(Integer.parseInt(detailsTask_TextField_StartHour.getText()));
    		} catch (NumberFormatException e) {
    			ShowAppMsg.showAlert("WARNING", "Некоректні дані", "Некоректно заповнений Час", "");
        		return;
    		}
    		try {
    		    tc.setStartMinute(Integer.parseInt(detailsTask_TextField_StartMinute.getText()));
    		} catch (NumberFormatException e) {
    			ShowAppMsg.showAlert("WARNING", "Некоректні дані", "Некоректно заповнені Хвилини", "");
        		return;
    		}
    		try {
    		    tc.setInterval(Integer.parseInt(detailsTask_TextField_Interval.getText()));
    		} catch (NumberFormatException e) {
    			ShowAppMsg.showAlert("WARNING", "Некоректні дані", "Некоректно заповнений Інтервал", "");
        		return;
    		}
    		
    		controllerSrecific.initSrecificVariables(tc); 
    		
    		tc.add();
    		
    		// створюємо основний ітем Завдання, ініціалізуємо, додаємо в БД
    		i = new TaskItem(
    				newTaskId, 
    				tti.getValue().getId(),      // parentId
    				newTypeId, 
					detailsTask_TextField_Name.getText(),
					detailsTask_TextField_Descr.getText(),
					detailsTask_CheckBox_isShowMsgStart.isSelected(),
					detailsTask_CheckBox_isShowMsgFinish.isSelected(),
					new java.util.Date(),
					new java.util.Date(),
					db,
					params
					); // Контроль під'єднався при створенні обьєкта 
    		db.taskAdd(i);
    		
    		// додаємо в Шедулер та контрол
    		params.getScheduler().getTasks().put(i.getId(), i);
    		
    		TreeItem<TaskItem> item = new TreeItem<>(i);
    		tti.getChildren().add(item);
    		treeTableView_Task.sort();
    		
    		// виводимо повідомлення в статус бар
    		params.setMsgToStatusBar("Завдання '" + i.getName() + "' додане.");
    	} else {              // update
    		if (tti != null) {
    			i = tti.getValue();
    	
    			// update item-control's data
    			try {
        		    i.getControl().setStartHour(Integer.parseInt(detailsTask_TextField_StartHour.getText()));
        		} catch (NumberFormatException e) {
        			ShowAppMsg.showAlert("WARNING", "Некоректні дані", "Некоректно заповнений Час", "");
            		return;
        		}
    			try {
    				i.getControl().setStartMinute(Integer.parseInt(detailsTask_TextField_StartMinute.getText()));
        		} catch (NumberFormatException e) {
        			ShowAppMsg.showAlert("WARNING", "Некоректні дані", "Некоректно заповнені Хвилини", "");
            		return;
        		}
        		try {
        			i.getControl().setInterval(Integer.parseInt(detailsTask_TextField_Interval.getText()));
        		} catch (NumberFormatException e) {
        			ShowAppMsg.showAlert("WARNING", "Некоректні дані", "Некоректно заповнений Інтервал", "");
            		return;
        		}
    			
        		i.getControl().setDateModified(new java.util.Date());
    			
        		controllerSrecific.updateSrecificVariables(i.getControl());
        		
        		i.getControl().update();
    			
    			// update item's data
    			i.setName(detailsTask_TextField_Name.getText());
    			i.setDescr(detailsTask_TextField_Descr.getText());
    			i.setShowMsgStart(detailsTask_CheckBox_isShowMsgStart.isSelected());
    			i.setShowMsgFinish(detailsTask_CheckBox_isShowMsgFinish.isSelected());
    			i.setDateModified(new java.util.Date());
    			
    			db.taskUpdate(i);
    			
    			// update in Sheduler and TreeTableView
    			params.getScheduler().getTasks().put(i.getId(), i);
    			
           		tti.setValue(null);
            	tti.setValue(i);
    	
    			// виводимо повідомлення в статус бар
                params.setMsgToStatusBar("Завдання '" + i.getName() + "' змінено.");
    		}
    	}
    }
    
    /**
     * На панелі детальної інформації вилучаємо завдання
     */
    @FXML
    private void handleButtonDetailsTaskDelete() {
    	TreeItem<TaskItem> selectedItem = treeTableView_Task.getSelectionModel().getSelectedItem();

    	// check
		if (selectedItem == null) {
			params.setMsgToStatusBar("Нічого не вибрано для вилучення.");
			return;
		}
		
		TaskItem i = selectedItem.getValue();
		
		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Вилучення завдання", 
	            "Вилучення. БЕЗ ПЕРЕВІРКИ СТАТУСУ !!! '"+ i.getName() +"'", "Вилучити ?"))
			return;
		
		// delete from DB
		i.delete();
		
		// remove from Scheduler
		params.getScheduler().getTasks().remove(i.getId());
		
		// delete from TreeTableView
		TreeItem<TaskItem> parentItem = selectedItem.getParent();
		if (parentItem != null) {     // 
			parentItem.getChildren().remove(selectedItem);
		}
		
		// виводимо повідомлення в статус бар
		params.setMsgToStatusBar("Завдання '" + i.getName() + "' вилучене.");
    }
    
    /**
     * Ставимо завдання на виконання по розкладу
     * міняємо статус з Disable
     */
    @FXML
    private void handleButtonDetailsTaskRun() {
    	startTaskScheduler (false);
    }
    
    /**
     * Ставимо завдання на виконання і одразу виконуємо перший раз
     */
    @FXML
    private void handleButtonDetailsTaskRunNow() {
    	startTaskScheduler (true);
    }
    
    /**
     * Зупиняємо завдання
     */
    @FXML
    private void handleButtonDetailsTaskStop() {
    	TreeItem<TaskItem> selectedItem = treeTableView_Task.getSelectionModel().getSelectedItem();

    	// check
		if (selectedItem == null) {
			params.setMsgToStatusBar("Не вибрано завдання.");
			return;
		}
		
		TaskItem i = selectedItem.getValue();  // елемент на формі
		TaskItem is = params.getScheduler().getTasks().get(i.getId());  // елемент в Шедулері
    	
    	// stop
		if (is.getControl().getScheduledFuture() != null && !is.getControl().getScheduledFuture().isCancelled()) {
			is.getControl().getScheduledFuture().cancel(true);
        }
		
		// change in item
    	i.getControl().setState(TaskControl.STATE_ENABLE);
    	is.getControl().setState(TaskControl.STATE_ENABLE);
    	
    	// change in DB
    	//db.taskControlSetState(i.getControl());
    	
    	// update in TreeTableView
    	selectedItem.setValue(null);
    	selectedItem.setValue(i);
    	
    	onChangeSelectedItem(selectedItem);
    }
    
    /**
     * Забороняємо виконання завдань
     */
    @FXML
    private void handleButtonDetailsTaskDisable() {
    	TreeItem<TaskItem> selectedItem = treeTableView_Task.getSelectionModel().getSelectedItem();

    	// check
		if (selectedItem == null) {
			params.setMsgToStatusBar("Не вибрано завдання.");
			return;
		}
		
		TaskItem i = selectedItem.getValue();  // елемент на формі
		TaskItem is = params.getScheduler().getTasks().get(i.getId());  // елемент в Шедулері
    	
		// dasable Task
		if (is.getControl().getScheduler() != null && !is.getControl().getScheduler().isShutdown()) {
			is.getControl().getScheduler().shutdownNow();
        }
		
    	// change in item
    	i.getControl().setState(TaskControl.STATE_DISABLE);
    	is.getControl().setState(TaskControl.STATE_DISABLE);
    	
    	// change in DB
    	db.taskControlSetState(i.getControl());
    	
    	// виконуємо дії після Дізейблу
    	is.getControl().afterDisable();
    	
    	// update in TreeTableView
    	selectedItem.setValue(null);
    	selectedItem.setValue(i);
    	
    	onChangeSelectedItem(selectedItem);
    }
    
    /**
     * 
     * @param runImmediately
     */
    private void startTaskScheduler (boolean runImmediately) {
    	TreeItem<TaskItem> selectedItem;
    	TaskItem i;
    	
   		selectedItem = treeTableView_Task.getSelectionModel().getSelectedItem();

   		// check
   		if (selectedItem == null) {
   			params.setMsgToStatusBar("Не вибрано завдання.");
   			return;
   		}
    	i = selectedItem.getValue();
    	
		try {
			params.getScheduler().startTaskScheduler(params.getScheduler().getTasks().get(i.getId()), runImmediately);
			
			// change in item
			i.getControl().setState(TaskControl.STATE_ENABLE);
			params.getScheduler().getTasks().get(i.getId()).getControl().setState(TaskControl.STATE_ENABLE);
			
			// change in DB
			db.taskControlSetState(i.getControl());
			
			// update in TreeTableView
			selectedItem.setValue(null);
			selectedItem.setValue(i);
			
			onChangeSelectedItem(selectedItem);
		} catch (ServiceException e) {
			e.writeLog(params);
			e.showAlert("Шедулер : помилка", "Шедулер : помилка при запуску Завдання '"+i.getName()+"'");
		}
    }
    
    /**
     * 
     */
    public void changeTaskStateInProcess (TaskItem i, int state) {
    	TreeItem<TaskItem> ti = findTaskItemByIdRecursive(treeTableView_Task.getRoot(), i.getId());
    	
    	// Оновлюємо графіку для цього елемента (перемальовуємо рядок)
        ti.setValue(null);  // Скидаємо значення (змушує TreeTableView перемалювати рядок)
        ti.setValue(i);  // Встановлюємо знову значення
    	
    	if (ti == treeTableView_Task.getSelectionModel().getSelectedItem()) {
    		onChangeSelectedItem(ti);
    	}
    }

    /**
     * Пошук в дереві необхідного TaskItem по його id
     * @param current
     * @param targetId
     * @return
     */
    public TreeItem<TaskItem> findTaskItemByIdRecursive(TreeItem<TaskItem> current, long targetId) {
        if (current == null) return null;

        TaskItem task = current.getValue();
        if (task != null && task.getId() == targetId) {
            return current;
        }

        for (TreeItem<TaskItem> child : current.getChildren()) {
            TreeItem<TaskItem> found = findTaskItemByIdRecursive(child, targetId);
            if (found != null) {
                return found;
            }
        }

        return null; // якщо не знайдено
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
		return AppItem_Interface.ELEMENT_TASK_LIST;
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
		return 0;
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
    	
    	//
    	params.getScheduler().setController(null);

    	// close tab
		params.getObjContainer().closeContainer(getOID());
    }
    
    /**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Сохраняем состояние контролов в иерархической структуре
	 */
	public void saveControlsState (StateList stateList) {
		//-------- treeTableView_Task
		String sortColumnId;
		String sortType;
		
		if (treeTableView_Task.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
			TreeTableColumn currentSortColumn = (TreeTableColumn) treeTableView_Task.getSortOrder().get(0);
			
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
					Long.toString(treeTableView_Task.getSelectionModel().getSelectedItem().getValue().getId()),      // id in DB,
					null);
		} catch (NullPointerException ex) {    }
		addTreeItemStateRecursive(stateList,treeTableView_Task.getRoot());
		stateList.add(
				"TreeItemsDoExpandAndSelected",
				"",
				null);
	}
	
	/**
	 * рекурсивное сохранение развернутых разделов из дерева разделов
	 */
	private void addTreeItemStateRecursive(StateList stateList, TreeItem<TaskItem> ti) {

		//-------- проверяем и записываем развернутость итема
		if (ti.isExpanded()) {
			stateList.add(
					"TreeItemExpanded",
					Long.toString(ti.getValue().getId()),      // id in DB
					null);
		}
		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<TaskItem> i : ti.getChildren()) {
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
					treeTableView_Task.getSortOrder().clear();
					
					if (! sortColumnId.equals("")) {
						for (TreeTableColumn column : treeTableView_Task.getColumns()) {
							if (column.getId().equals(sortColumnId)) {
								treeTableView_Task.setSortMode(TreeSortMode.ALL_DESCENDANTS);
								column.setSortable(true); // This performs a sort
								treeTableView_Task.getSortOrder().add(column);
								if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
								else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
								treeTableView_Task.sort();
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
					restoreTreeItemStateRecursive(listItemsForExpand,treeTableView_Task.getRoot());
					treeTableView_Task.sort();
					restoreTreeItemSelectedRecursive(selectedItemId,treeTableView_Task.getRoot());
					treeTableView_Task.sort();
					
					break;
			}
		}
	}
	
	/**
	 * рекурсивное восстановление состояния разделов дерева разделов
	 */
	private void restoreTreeItemStateRecursive(
			ObservableList<Long> listItemsForExpand,
			TreeItem<TaskItem> ti) {

		//-------- проверяем и разворачиваем текущий итем
		for (Long i : listItemsForExpand) {
			if (i == ti.getValue().getId()) {
				ti.setExpanded(true);
				break;
			}
		}
		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<TaskItem> i : ti.getChildren()) {
			restoreTreeItemStateRecursive(listItemsForExpand, i);
		}
	}
	
	/**
	 * рекурсивно ищем активный раздел в дереве разделов и выбираем его
	 */
	private void restoreTreeItemSelectedRecursive(
			Long selectedItemId,
			TreeItem<TaskItem> ti) {

		//---------- проверяем и выбираем текущий итем
		if (selectedItemId == ti.getValue().getId()) {
			treeTableView_Task.getSelectionModel().select(ti);
		} 

		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<TaskItem> i : ti.getChildren()) {
			restoreTreeItemSelectedRecursive(selectedItemId, i);
		}
	}
	
	/**
	 * Клас обробки дерева Завдань
	 */
	public class TreeView_Controller {
		
		/**
		 * 
		 */
		public TreeView_Controller () {
			
		}
		
		/**
		 * 
		 */
		void init () {
			initRowFactory();
		}
		
		/**
		 * RowFactory - for Drag&Drop and tooltip
		 */
		void initRowFactory () {
			treeTableView_Task.setRowFactory(new Callback<TreeTableView<TaskItem>,
					TreeTableRow<TaskItem>>() {
				@Override
				public TreeTableRow<TaskItem> call(final TreeTableView<TaskItem> param) {
					final TreeTableRow<TaskItem> row = new TreeTableRow<TaskItem>();

					row.setOnDragDetected(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							// drag was detected, start drag-and-drop gesture
							TreeItem<TaskItem> selected =
									(TreeItem<TaskItem>) treeTableView_Task.getSelectionModel().getSelectedItem();

							if (selected != null) {
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
							Dragboard dbd = event.getDragboard();

							if (dragAndDropAcceptable(dbd, row)) {
								int index = (Integer) dbd.getContent(params.getMain().SERIALIZED_MIME_TYPE);
								TreeItem<TaskItem> item = treeTableView_Task.getTreeItem(index);

								if (event.getAcceptedTransferMode() == TransferMode.MOVE) {
									if (ShowAppMsg.showQuestion("CONFIRMATION", "Переміщення Завдання (директорії з Завданнями)",
											"Переміщення '"+ item.getValue().getName() +"'", "Перемістити ?")) {
										// update in DB
										if (item.getValue().getTypeId() == TaskItem.TYPE_ITEM_DIR) {
											db.dirMove(item.getValue().getId(), dragAndDropGetTarget(row).getValue().getId());
										} else {
											db.taskMove(item.getValue().getId(), dragAndDropGetTarget(row).getValue().getId());
										}
										
										//---- move in tree-control
										item.getParent().getChildren().remove(item);
										dragAndDropGetTarget(row).getChildren().add(item);
										event.setDropCompleted(true);
										
										// вибираємо батьківський
										treeTableView_Task.getSelectionModel().select(
												getTreeItemByIdAndType(treeTableView_Task.getRoot(), dragAndDropGetTarget(row).getValue()));
										// експандимо (розкриваємо) батьківський
										dragAndDropGetTarget(row).setExpanded(true);
										// вибираємо переміщений
										treeTableView_Task.getSelectionModel().select(
												getTreeItemByIdAndType(treeTableView_Task.getRoot(), item.getValue()));

										// сортуємо
										treeTableView_Task.sort();
										
										// выводим сообщение в статус бар
										params.setMsgToStatusBar("Завдання (директорія) '" + item.getValue().getName() + "' переміщено.");
									}
								} else if (event.getAcceptedTransferMode() == TransferMode.COPY) {
									boolean copyWithSubSections = prefs.get("copyWithSubSections", "No").equals("Yes");
									int retVal = ShowAppMsg.showQuestionWithOption(
											"CONFIRMATION", "Копіювання Завдань",
											"Копіювати Завдання '"+ item.getValue().getName() +"' ?", null,
											"Копіювати гілку повністю", copyWithSubSections);
									TaskItem newTask = null;
									
									if (retVal == ShowAppMsg.QUESTION_OK) {          // сохраняем только текущий итем
										event.setDropCompleted(true);

										newTask = copyTask (item.getValue(), dragAndDropGetTarget(row), false);

										// save Option value
										prefs.put("copyWithSubSections", "No");

										// выводим сообщение в статус бар
										params.setMsgToStatusBar("Завдання (директорія) '" + item.getValue().getName() + "' скопійоване.");
									}
									if (retVal == ShowAppMsg.QUESTION_OK_WITH_OPTION) {          // сохраняем всю ветку
										event.setDropCompleted(true);

										// item                          - source TreeItem
										// getTarget_forDragAndDrop(row) - target parent TreeItem
										newTask = copyTask (item.getValue(), dragAndDropGetTarget(row), true);

										// save Option value
										prefs.put("copyWithSubSections", "Yes");

										// выводим сообщение в статус бар
										params.setMsgToStatusBar("Завдання (директорія) '" + item.getValue().getName() + "' скопійоване.");
									}
									treeTableView_Task.sort();
									//expandTreeItemsById(root, newSectionId);
									//selectTreeItemById(root, newSectionId);
								} else {
									ShowAppMsg.showAlert("WARNING", "Перетаскування", "Невідомий режим перетаскування", "Не обробляється.");
								}
								event.consume();
							}
						}});

					return row;
				}
			});
		}
		
		/**
		 * Возвращает Истину, если перетаскивание возможно, иначе Ложь.
		 */
		private boolean dragAndDropAcceptable(Dragboard db, TreeTableRow<TaskItem> row) {
			boolean result = false;
			if (db.hasContent(params.getMain().SERIALIZED_MIME_TYPE)) {
				int index = (Integer) db.getContent(params.getMain().SERIALIZED_MIME_TYPE);
				if (row.getIndex() != index) {
					TreeItem<TaskItem> target = dragAndDropGetTarget(row);
					TreeItem<TaskItem> item = treeTableView_Task.getTreeItem(index);
					result = !dragAndDropIsParent(item, target);
				}
			}
			return result;
		}
		
		/**
		 * Получаем строчку-приемник при перетаскивании
		 */
		private TreeItem<TaskItem> dragAndDropGetTarget(TreeTableRow<TaskItem> row) {
			TreeItem<TaskItem> target = treeTableView_Task.getRoot();
			if (!row.isEmpty()) {
				target = row.getTreeItem();
				
				// брати не поточний ітем для батьківства, 
	    		// а перевіряти його на тип директорії, і якщо потрібно піднятися на позицію угору в ієрархії
	    		if (target.getValue().getTypeId() > 0) {
	    			target = target.getParent();
	    		}
			}
			return target;
		}
		
		/**
		 * prevent loops in the tree
		 */
		private boolean dragAndDropIsParent(TreeItem<TaskItem> parent, TreeItem<TaskItem> child) {
			boolean result = false;
			while (!result && child != null) {
				result = child.getParent() == parent;
				child = child.getParent();
			}
			return result;
		}
		
		/**
		 * В дереві Завдань знаходимо Завдання по його id (або директорію)
		 */
		public TreeItem<TaskItem> getTreeItemByIdAndType(TreeItem<TaskItem> parentItem, TaskItem item) {
			TaskItem parent = parentItem.getValue();
			
			if ((parent.getId() == item.getId()) && (parent.getTypeId() == item.getTypeId())) {
				return parentItem;
			}
			
			//-------- вибираємо дочірні ітеми та запускаємо рекурсію
			for (TreeItem<TaskItem> i : parentItem.getChildren()) {
				TreeItem<TaskItem> ti = getTreeItemByIdAndType(i, item);
				
				if (ti != null)
					return ti;
			}
			
			return null;
		}
		
		/**
		 * Копіюємо рекурсивно гілку Завдань або  одне Завдання
		 * cpyCII - копіюєме Завдання
		 * trgTI - элемент дерева, в який копіювати
		 */
		private TaskItem copyTask (
				TaskItem cpy, TreeItem<TaskItem> trgTI, boolean isRecursive) {

			if (trgTI.getValue() == null)  return null;

			// створюємо новий об'єкт Завдання чи Директорії
			TaskItem cur = cpy.copy(trgTI.getValue().getId());
			
			// додаємо в Шедулер
    		params.getScheduler().getTasks().put(cur.getId(), cur);
			
			// додаємо до нового предка
			TreeItem<TaskItem> curTI = new TreeItem<>(cur);
			trgTI.getChildren().add(curTI);
			
			// рекурсія
			if (isRecursive && (cur.getTypeId() == TaskItem.TYPE_ITEM_DIR)) {
				List<TaskItem> dirsList = db.dirListByParent(cpy);
				for (TaskItem i : dirsList) {
					copyTask (i, curTI, isRecursive);
				}
				
				List<TaskItem> tasksList = db.taskListByDir(cpy, params);
				for (TaskItem i : tasksList) {
					copyTask (i, curTI, isRecursive);
				}
			}
			
			return cur;
		}
	}
}
