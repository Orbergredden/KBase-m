
package app.view.business;

import app.exceptions.KBase_DbConnEx;
import app.lib.ConvertType;
import app.lib.DateConv;
import app.lib.ShowAppMsg;
import app.model.AppItem_Interface;
import app.model.business.IconItem;
import app.model.DBConCur_Parameters;
import app.model.Params;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TreeTableView.TreeTableViewSelectionModel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;

/**
 * Контроллер фрейма каталога пиктограмм. Показываем дерево-список пиктограмм, 
 * с возможность добавления, редактирования и удаления.
 * @author Igor Makarevich
 * v.1.00.00.001 2024-11-14
 */
public class IconsList_Controller implements AppItem_Interface {
    
	private Params params;
    private DBConCur_Parameters conn;
    
    /**
     * Ковертор даты/времени
     */
    public DateConv dateConv;
    
    @FXML
	private Button button_Exit;
    @FXML
    private TitledPane titledPane_Title;
    
    @FXML
	private Button button_iconSaveToDisk;
    @FXML
	private Button button_iconCopy;
    @FXML
	private Button button_iconCut;
    @FXML
	private Button button_iconPaste;
    @FXML
	private Button button_iconDelete;
    
    @FXML
    private SplitPane splitPane_main;
    
    @FXML
	private TreeTableView<IconItem> treeTableView_icons;
    @FXML
	private TreeTableColumn<IconItem, Long> treeTableColumn_id;
    @FXML
	private TreeTableColumn<IconItem, String> treeTableColumn_name;
    @FXML
	private TreeTableColumn<IconItem, String> treeTableColumn_description;
    
    @FXML
	private MenuItem menuitem_iconSaveToDisk;
    @FXML
	private MenuItem menuitem_iconCopy;
    @FXML
	private MenuItem menuitem_iconCut;
    @FXML
	private MenuItem menuitem_iconPaste;
    @FXML
	private MenuItem menuitem_iconDelete;
    
    // добавление
    @FXML
	private Label label_a_id;
    @FXML
	private Label label_a_parentId;
    @FXML
	private Label label_a_parentName;
    @FXML
	private Label label_a_dateCreated;
    @FXML
	private Label label_a_dateModified;
    @FXML
	private TextField textField_a_name;
    @FXML
	private TextField textField_a_description;
    @FXML
    private ImageView imageView_a_image;
    @FXML
	private TextField textField_a_fileName;
    @FXML
    private Label label_a_imageSize;
    @FXML
	private Button button_a_selectImage;
    @FXML
	private Button button_add;

    // изменение
    @FXML
	private Label label_u_id;
    @FXML
	private Label label_u_parentId;
    @FXML
	private Label label_u_parentName;
    @FXML
	private Label label_u_dateCreated;
    @FXML
	private Label label_u_dateModified;
    @FXML
	private TextField textField_u_name;
    @FXML
	private TextField textField_u_description;
    @FXML
    private ImageView imageView_u_image;
    @FXML
	private TextField textField_u_fileName;
    @FXML
    private Label label_u_imageSize;
    @FXML
	private Button button_u_selectImage;
    @FXML
	private Button button_update;
    @FXML
	private Button button_delete;
    
    /**
     * Указывает, загружалась ли картинка из файла для последующего Изменения в БД для активной пиктограммы.
     */
    private boolean isChangeImgForUpdate;
    
    /**
     * Локальный буфер обмена. Элемент дерева-списка с обьектом иконки
     */
    private TreeItem<IconItem> clipBoard_tiIcon;
    /**
     * Локальный буфер обмена. Тип операции : 0 - копировать ; 1 - вырезать
     */
    private int clipBoard_typeOperation;
    private static final int CLIPBOARD_TYPE_OPERATION__COPY = 0;
    private static final int CLIPBOARD_TYPE_OPERATION__CUT  = 1;

    //
	private Preferences prefs;
    
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public IconsList_Controller () {
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
     * Вызывается главным приложением, которое даёт на себя ссылку.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params) {
    	this.params = params;
    	this.conn   = params.getConCur();
        
        // init controls
        initControlsValue();
    }
	
    /**
     * Инициализирует контролы значениями 
     */
    private void initControlsValue() {
    	prefs = Preferences.userNodeForPackage(IconsList_Controller.class);
    	
    	button_Exit.setTooltip(new Tooltip("Закрыть фрейм"));
    	
    	//======== title
    	titledPane_Title.setText(titledPane_Title.getText() + " - " + params.getConCur().param.getConnName());
    	
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
    	
    	//======== ToolBar
    	button_iconSaveToDisk.setTooltip(new Tooltip("Сохранить пиктограмму на диск..."));
    	button_iconSaveToDisk.setGraphic(new ImageView(new Image("file:resources/images/icon_SaveToFile_24.png")));
    	
    	button_iconCopy.setTooltip(new Tooltip("Копировать пиктограмму (внутренний буфер)"));
    	button_iconCopy.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_24.png")));
    	
    	button_iconCut.setTooltip(new Tooltip("Вырезать пиктограмму (внутренний буфер)"));
    	button_iconCut.setGraphic(new ImageView(new Image("file:resources/images/icon_cut_24.png")));
    	
    	button_iconPaste.setTooltip(new Tooltip("Вставить пиктограмму (внутренний буфер)"));
    	button_iconPaste.setGraphic(new ImageView(new Image("file:resources/images/icon_paste_24.png")));
    	
    	button_iconDelete.setTooltip(new Tooltip("Удалить пиктограмму"));
    	button_iconDelete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_24.png")));
    	
    	//======== splitPane_main
    	splitPane_main.setDividerPositions(prefs.getDouble("CatalogIcons_splitPane_main_position", 0.7));
    	
    	splitPane_main.getDividers().get(0).positionProperty().addListener(
                o -> {
                	prefs.putDouble("CatalogIcons_splitPane_main_position", splitPane_main.getDividerPositions()[0]);
                	//System.out.println(splitPane_main.getDividerPositions()[0]);
                }
        );
    	
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
    			(TreeTableColumn.CellDataFeatures<IconItem, Long> param) -> 
    			new ReadOnlyObjectWrapper<>(param.getValue().getValue().getId())
    			);
    	
    	// set/get Pref Width
    	treeTableColumn_id.setPrefWidth(prefs.getDouble("CatalogIcons__treeTableColumn_id__PrefWidth", 50));
    	treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("CatalogIcons__treeTableColumn_id__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_name.setPrefWidth(prefs.getDouble("CatalogIcons__treeTableColumn_name__PrefWidth", 300));
    	treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("CatalogIcons__treeTableColumn_name__PrefWidth", t1.doubleValue());
            }
        });
    	treeTableColumn_description.setPrefWidth(prefs.getDouble("CatalogIcons__treeTableColumn_description__PrefWidth", 400));
    	treeTableColumn_description.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            	prefs.putDouble("CatalogIcons__treeTableColumn_description__PrefWidth", t1.doubleValue());
            }
        });
    	
    	//-------- init 
    	final TreeItem<IconItem> root = 
    			new TreeItem<>(new IconItem(
    	    	         			0, 0, "Пиктограммы", "это корень, он не редактируется", 
    	    	         			//rootIconImage,
    	    	         			new Image("file:resources/images/icon_CatalogIcons_24.png"),
    	    	         			new Date(), new Date(), "",""));
    	root.setExpanded(true);
    	treeTableView_icons.setShowRoot(true);
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
    	
    	// row factory for Drag&Drop
    	treeTableView_icons.setRowFactory(new Callback<TreeTableView<IconItem>, 
    			                                       TreeTableRow<IconItem>>() {
    		@Override
    	    public TreeTableRow<IconItem> call(final TreeTableView<IconItem> param) {
    			final TreeTableRow<IconItem> row = new TreeTableRow<IconItem>();
    	
    			row.setOnDragDetected(new EventHandler<MouseEvent>() {
    	            @Override
    	            public void handle(MouseEvent event) {
    	                // drag was detected, start drag-and-drop gesture
    	                TreeItem<IconItem> selected = 
    	                		(TreeItem<IconItem>) treeTableView_icons.getSelectionModel().getSelectedItem();
    	                //CatalogIconsItem_Parameters curIcon = selected.getValue();

    	                if (selected != null) {
    	                    //Dragboard db = treeTableView_icons.startDragAndDrop(TransferMode.ANY);
    	                	//Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
    	                	Dragboard db = row.startDragAndDrop(TransferMode.ANY);

    	                    // create a miniature of the row you're dragging
    	                    db.setDragView(row.snapshot(null, null));

    	                    // Keep whats being dragged on the clipboard
    	                    ClipboardContent content = new ClipboardContent();
    	                    //content.putString(selected.getValue().getName());
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
    	                
    	                if (acceptable_forDragAndDrop(db, row)) {
    	                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
    	                    event.consume();
    	                }
    	        }});
    	
    			row.setOnDragDropped(new EventHandler<DragEvent>() {
    	            @Override
    	            public void handle(DragEvent event) {
    	                Dragboard db = event.getDragboard();

    	                if (acceptable_forDragAndDrop(db, row)) {
    	                    int index = (Integer) db.getContent(params.getMain().SERIALIZED_MIME_TYPE);
    	                    TreeItem<IconItem> item = treeTableView_icons.getTreeItem(index);
    	                    
    	                    if (event.getAcceptedTransferMode() == TransferMode.MOVE) {
    	                    	if (ShowAppMsg.showQuestion("CONFIRMATION", "Перемещение пиктограммы", 
    	                              "Перемещение пиктограммы '"+ item.getValue().getName() +"'", "Переместить пиктограмму ?")) {
    	                    		item.getParent().getChildren().remove(item);
    	                    		getTarget_forDragAndDrop(row).getChildren().add(item);
    	                    		event.setDropCompleted(true);
    	                    		treeTableView_icons.getSelectionModel().select(item);
    	                    
    	                    		// update in DB
    	                    		conn.db.iconMove (item.getValue().getId(), getTarget_forDragAndDrop(row).getValue().getId());
    	                    		
    	                    		// выводим сообщение в статус бар
    	                            params.setMsgToStatusBar("Пиктограмма '" + item.getValue().getName() + "' перемещена.");
    	                    	}
    	                    } else if (event.getAcceptedTransferMode() == TransferMode.COPY) {
    	                    	Preferences prefs = Preferences.userNodeForPackage(IconsList_Controller.class);
    	                    	boolean copyWithSubIcon = prefs.get("copyWithSubIcon", "No").equals("Yes") ? true : false;
    	                    	int retVal = ShowAppMsg.showQuestionWithOption(
    	                    			"CONFIRMATION", "Копирование пиктограммы", 
    	      	                        "Копировать пиктограмму '"+ item.getValue().getName() +"' ?", null,
    	      	                        "Копировать ветку целиком", copyWithSubIcon);

    	                    	if (retVal == ShowAppMsg.QUESTION_OK) {          // сохраняем только одну иконку
    	                    		event.setDropCompleted(true);
    	                    		
    	                    		copyIcon (item.getValue(), getTarget_forDragAndDrop(row), false);
    	                    		
    	                    		// save Option value
    	                    		prefs.put("copyWithSubIcon", "No");
    	                    		
    	                    		// выводим сообщение в статус бар
    	                    		params.setMsgToStatusBar("Пиктограмма '" + item.getValue().getName() + "' скопирована.");
    	                    	}
    	                    	if (retVal == ShowAppMsg.QUESTION_OK_WITH_OPTION) {          // сохраняем всю ветку
    	                    		event.setDropCompleted(true);
    	                    		
    	                    		// item                          - source TreeItem
    	                    		// getTarget_forDragAndDrop(row) - target parent TreeItem
    	                    		copyIcon (item.getValue(), getTarget_forDragAndDrop(row), true);
    	                    		
    	                    		// save Option value
    	                    		prefs.put("copyWithSubIcon", "Yes");
    	                    		
    	                    		// выводим сообщение в статус бар
    	                    		params.setMsgToStatusBar("Пиктограмма (ветка) '" + item.getValue().getName() + "' скопирована.");
    	                    	}
    	                    } else {
    	                    	ShowAppMsg.showAlert("WARNING", "Перетаскивание", "Не известный режим перетаскивания", "Не обрабатывается.");
    	                    }
    	                    event.consume();
    	                }
    	        }});
    	
    			return row;
    		}
    	});
    	
    	// Слушаем изменения выбора, и при изменении отображаем информацию об разделе/пиктограмме (для добавления/изменения, ...).
    	treeTableView_icons.getSelectionModel().selectedItemProperty().addListener(
    			(observable, oldValue, newValue) -> showIconDetails(newValue));

		//---- восстанавливаем сортировку таблицы по столбцу
		String sortColumnId = prefs.get("stageIconsList_sortColumnId","");

		if (! sortColumnId.equals("")) {
			for (TreeTableColumn column : treeTableView_icons.getColumns()) {
				if (column.getId().equals(sortColumnId)) {
					String sortType = prefs.get("stageIconsList_sortType","ASCENDING");

					treeTableView_icons.setSortMode(TreeSortMode.ALL_DESCENDANTS);
					column.setSortable(true); // This performs a sort
					treeTableView_icons.getSortOrder().add(column);
					if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
					else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
					treeTableView_icons.sort();
				}
			}
		}

    	// ContextMenu
    	menuitem_iconSaveToDisk.setGraphic(new ImageView(new Image("file:resources/images/icon_SaveToFile_16.png")));
    	menuitem_iconCopy.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_16.png")));
    	menuitem_iconCut.setGraphic(new ImageView(new Image("file:resources/images/icon_cut_16.png")));
    	menuitem_iconPaste.setGraphic(new ImageView(new Image("file:resources/images/icon_paste_16.png")));
    	menuitem_iconDelete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	
    	//======== Add/Update panels
    	button_a_selectImage.setGraphic(new ImageView(new Image("file:resources/images/icon_load_16.png")));
    	button_u_selectImage.setGraphic(new ImageView(new Image("file:resources/images/icon_load_16.png")));
    	
    	button_add.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
    	button_update.setGraphic(new ImageView(new Image("file:resources/images/icon_update_16.png")));
    	button_delete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    }
    
    /**
     * Инициализация TreeTableView. Рекурсия по дереву. 
     * @param
     */
    private void addTreeItemsRecursive(TreeItem<IconItem> ti) {
    	IconItem f = (IconItem) ti.getValue();
    
    	if (f != null) {
    		List<IconItem> iconsList = conn.db.iconListByParentId(f.getId());
    		
    		for (IconItem i : iconsList) {
    			TreeItem<IconItem> subItem = new TreeItem<>(i);
    			ti.getChildren().add(subItem);
    			
    			addTreeItemsRecursive(subItem);
    		}
    	}
    }
    
    /**
     * Заполняет все текстовые поля для добавления/изменения пиктограммы.
     * Если указанная пиктограмма = null, то все текстовые поля очищаются.
     * 
     * @param ti — информация по одной пиктограмме
     */
    private void showIconDetails(TreeItem<IconItem> ti) {
    	String fileNameStd = new String("resources/images/icon_root.png");
    	Image imgStd = new Image("file:" + fileNameStd);
    	
    	
        if (ti != null) {
        	IconItem f = (IconItem) ti.getValue();
        	
        	// controls for add
        	label_a_id.setText("");
        	label_a_parentId.setText(Long.toString(f.getId()));
        	label_a_parentName.setText(f.getName());
        	label_a_dateCreated.setText("");
        	label_a_dateModified.setText("");
        	textField_a_name.setText("");
        	textField_a_description.setText("");
            imageView_a_image.setImage(imgStd);
        	textField_a_fileName.setText("");
        	label_a_imageSize.setText("");
            button_add.setDisable(false);
            
            // controls for update
            if (f.getId() > 0) {
            	label_u_id.setText(Long.toString(f.getId()));
                label_u_parentId.setText(Long.toString(ti.getParent().getValue().getId()));
                label_u_parentName.setText(ti.getParent().getValue().getName());
                label_u_dateCreated.setText(dateConv.dateTimeToStr(f.getDateCreated()) +"   "+ f.getUserCreated());
                label_u_dateModified.setText(dateConv.dateTimeToStr(f.getDateModified()) +"   "+ f.getUserModified());
            	textField_u_name.setText(f.getName());
            	textField_u_description.setText(f.getDescr());
            	textField_u_fileName.setText("");
                imageView_u_image.setImage(f.image);
                label_u_imageSize.setText((int)f.image.getWidth() +" x "+ (int)f.image.getHeight());
                button_update.setDisable(false);
                button_delete.setDisable(false);
            } else {
            	button_update.setDisable(true);
            	button_delete.setDisable(true);
            }
            // unset flag for update image
        	isChangeImgForUpdate = false;
            
            // show icon path in StatusBar
            String msg = new String(f.getName());
            if (f.getId() > 0) {
            	TreeItem<IconItem> curTI = ti.getParent();
                IconItem curIcon = curTI.getValue();
                
                while (curIcon.getId() >= 0) {
                	msg = curIcon.getName() + " / " + msg;
                	
                    if (curIcon.getId() == 0)   break;
                    else {
                    	curTI = curTI.getParent();
                        curIcon = curTI.getValue();
                    }
                }
            }
            params.setMsgToStatusBar(msg);
        } else {
        	// controls for add
        	label_a_id.setText("");
        	label_a_parentId.setText("");
        	label_a_parentName.setText("");
        	label_a_dateCreated.setText("");
        	label_a_dateModified.setText("");
        	textField_a_name.setText("");
        	textField_a_description.setText("");
            imageView_a_image.setImage(imgStd);
        	textField_a_fileName.setText("");
        	label_a_imageSize.setText("");
            button_add.setDisable(true);
            
            // controls for update
        	label_u_id.setText("");
        	label_u_parentId.setText("");
        	label_u_parentName.setText("");
        	label_u_dateCreated.setText("");
        	label_u_dateModified.setText("");
        	textField_u_name.setText("");
        	textField_u_description.setText("");
            imageView_u_image.setImage(imgStd);
        	textField_u_fileName.setText("");
        	label_u_imageSize.setText("");
            button_update.setDisable(true);
            button_delete.setDisable(true);
            
            // unset flag for update image
        	isChangeImgForUpdate = false;
        }
    }
    
    /**
     * Возвращает Истину, если перетаскивание возможно, иначе Ложь.
     * @param db
     * @param row
     * @return
     */
    private boolean acceptable_forDragAndDrop(Dragboard db, TreeTableRow<IconItem> row) {
        boolean result = false;
        if (db.hasContent(params.getMain().SERIALIZED_MIME_TYPE)) {
            int index = (Integer) db.getContent(params.getMain().SERIALIZED_MIME_TYPE);
            if (row.getIndex() != index) {
                TreeItem<IconItem> target = getTarget_forDragAndDrop(row);
                TreeItem<IconItem> item = treeTableView_icons.getTreeItem(index);
                result = !isParent_forDragAndDrop(item, target);
            }
        }
        return result;
    }
   
    /**
     * Получаем строчку-приемник при перетаскивании
     * @param row
     * @return
     */
    private TreeItem<IconItem> getTarget_forDragAndDrop(TreeTableRow<IconItem> row) {
        TreeItem<IconItem> target = treeTableView_icons.getRoot();
        if (!row.isEmpty()) {
            target = row.getTreeItem();
        }
        return target;
    }
    
    /**
     * prevent loops in the tree
     * @param parent
     * @param child
     * @return
     */
    private boolean isParent_forDragAndDrop(TreeItem<IconItem> parent, TreeItem<IconItem> child) {
        boolean result = false;
        while (!result && child != null) {
            result = child.getParent() == parent;
            child = child.getParent();
        }
        return result;
    }
    
    /**
     * Копируем рекурсивно ветку иконок или одну иконку
     * @param cpyCII - обьект который копируем
     * @param trgTI - элемент дерева куда копируем 
     */
    private void copyIcon (
    		IconItem cpyCII, TreeItem<IconItem> trgTI, boolean isRecursive) {
    	
    	if (trgTI.getValue() == null)  return;
    	
    	// создаем новый обьект иконки для копирования
    	IconItem curCII = new IconItem(cpyCII);
    	curCII.setId(conn.db.iconNextId());
    	curCII.setParentId(trgTI.getValue().getId());
    	curCII.setDateCreated(new Date());
    	curCII.setDateModified(new Date());
    	
    	// обьект-иконку добавляем в БД
    	conn.db.iconAdd(curCII);
    	
    	// добавляем к новому родителю
    	TreeItem<IconItem> curTI = new TreeItem<>(curCII);
    	trgTI.getChildren().add(curTI);
    	
    	// в цикле вызываем рекурсивный метод
    	if (isRecursive) {
    		List<IconItem> iconsList = conn.db.iconListByParentId(cpyCII.getId());
    	
    		for (IconItem i : iconsList) {
    			copyIcon (i, curTI, isRecursive);
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
    
    /**
     * Вызывается при нажатии на кнопке "Сохранить пиктограмму на диск..."
     */
    @FXML
    private void handleButtonIconSaveToDisk() {

    	//======== get current icon
    	TreeItem<IconItem> selectedItem = treeTableView_icons.getSelectionModel().getSelectedItem();
    	if (selectedItem == null) {
    		params.setMsgToStatusBar("Не выбрана пиктограмма для сохранения.");
    		return;
    	}
    	
    	IconItem cii = (IconItem) selectedItem.getValue();
    	
    	//======== get file name
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Сохранение пиктограммы в файл");
    	  
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG файл (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName(cii.getName());
        
        // set current dir
        String curDir = prefs.get("icons_CurDirNameForSave", "");
        if (! curDir.equals("")) 
        	fileChooser.setInitialDirectory(new File(curDir));
        
        //Show save file dialog
        File file = fileChooser.showSaveDialog(params.getMainStage());
        
        if(file != null){
        	//======== save file
        	BufferedImage bImage = SwingFXUtils.fromFXImage(cii.image, null);
            try {
              ImageIO.write(bImage, "png", file);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
            
            // save dir name
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("icons_CurDirNameForSave", curDir);
        }
    }
    
    /**
     * Копирует текущую иконку с локальный буфер обмена
     */
    @FXML
    private void handleButtonIconCopy() {
    	TreeItem<IconItem> selectedItem = treeTableView_icons.getSelectionModel().getSelectedItem();
    	
    	// проверяем наличие выбранного обьекта-иконки
    	if (selectedItem == null) {
    		ShowAppMsg.showAlert("INFORMATION", "Копирование пиктограммы", 
    				"Не выбрана пиктограмма для копирования.", "Ничего не скопировано.");
    		params.setMsgToStatusBar("Ничего не выбрано для копирования.");
    		return;
    	}
    	
    	// заносим данные в локальный буфер обмена
    	clipBoard_tiIcon = selectedItem;
        clipBoard_typeOperation = CLIPBOARD_TYPE_OPERATION__COPY;
        params.setMsgToStatusBar("Пиктограмма '"+ selectedItem.getValue().getName() +"' скопирована в локальный буфер обмена.");
    }
    
    /**
     * Вырезает текущую иконку с занесением в локальный буфер обмена
     */
    @FXML
    private void handleButtonIconCut() {
    	TreeItem<IconItem> selectedItem = treeTableView_icons.getSelectionModel().getSelectedItem();
    	
    	// проверяем наличие выбранного обьекта-иконки
    	if (selectedItem == null) {
    		ShowAppMsg.showAlert("INFORMATION", "Вырезание пиктограммы", 
    				"Не выбрана пиктограмма для вырезания.", "Ничего не вырезано.");
    		params.setMsgToStatusBar("Ничего не выбрано для вырезания.");
    		return;
    	}
    	
    	// заносим данные в локальный буфер обмена
    	clipBoard_tiIcon = selectedItem;
        clipBoard_typeOperation = CLIPBOARD_TYPE_OPERATION__CUT;
        params.setMsgToStatusBar("Пиктограмма '"+ selectedItem.getValue().getName() +"' вырезана в локальный буфер обмена.");
    }
    
    /**
     * Вставляет иконку указанную в буфере обмена
     */
    @FXML
    private void handleButtonIconPaste() {
    	TreeItem<IconItem> trgItem = treeTableView_icons.getSelectionModel().getSelectedItem();
    	
    	// проверяем наличие обьекта-иконки в буфере
    	if (clipBoard_tiIcon == null) {
    		ShowAppMsg.showAlert("INFORMATION", "Вставка пиктограммы", 
    				"Локальный буфер обмена пустой.", "Ничего не вставлено.");
    		params.setMsgToStatusBar("Локальный буфер обмена пустой.");
    		return;
    	}
    	if ((clipBoard_typeOperation != CLIPBOARD_TYPE_OPERATION__COPY) && 
    		(clipBoard_typeOperation != CLIPBOARD_TYPE_OPERATION__CUT)) {
    		ShowAppMsg.showAlert("INFORMATION", "Вставка пиктограммы", 
    				"Неизвестная команда в буфере обмена.", "Ничего не вставлено.");
    		params.setMsgToStatusBar("Неизвестная команда в буфере обмена.");
    		return;
    	}
    	
    	// проверяем наличие выбранного обьекта-иконки как приемника
    	if (trgItem == null) {
    		ShowAppMsg.showAlert("INFORMATION", "Вставка пиктограммы", 
    				"Не выбрана пиктограмма-приемник.", "Ничего не вставлено.");
    		params.setMsgToStatusBar("Не выбрана пиктограмма-приемник.");
    		return;
    	}
    	
    	// Copy
    	if (clipBoard_typeOperation == CLIPBOARD_TYPE_OPERATION__COPY) {
    		Preferences prefs = Preferences.userNodeForPackage(IconsList_Controller.class);
        	boolean copyWithSubIcon = prefs.get("copyWithSubIcon", "No").equals("Yes") ? true : false;
        	int retVal = ShowAppMsg.showQuestionWithOption(
        			"CONFIRMATION", "Копирование пиктограммы", 
                      "Копировать пиктограмму '"+ clipBoard_tiIcon.getValue().getName() +"' ?", null,
                      "Копировать ветку целиком", copyWithSubIcon);
        	if (retVal == ShowAppMsg.QUESTION_OK) {          // сохраняем только одну иконку
        		copyIcon (clipBoard_tiIcon.getValue(), trgItem, false);
        		
        		// save Option value
        		prefs.put("copyWithSubIcon", "No");
        		
        		// выводим сообщение в статус бар
        		params.setMsgToStatusBar("Пиктограмма '" + clipBoard_tiIcon.getValue().getName() + "' скопирована.");
        	}
        	if (retVal == ShowAppMsg.QUESTION_OK_WITH_OPTION) {          // сохраняем всю ветку
        		copyIcon (clipBoard_tiIcon.getValue(), trgItem, true);
        		
        		// save Option value
        		prefs.put("copyWithSubIcon", "Yes");
        		
        		// выводим сообщение в статус бар
        		params.setMsgToStatusBar("Пиктограмма (ветка) '" + clipBoard_tiIcon.getValue().getName() + "' скопирована.");
        	}
    	}
    	
    	// Move
    	if (clipBoard_typeOperation == CLIPBOARD_TYPE_OPERATION__CUT) {
    		clipBoard_tiIcon.getParent().getChildren().remove(clipBoard_tiIcon);
    		trgItem.getChildren().add(clipBoard_tiIcon);
    		treeTableView_icons.getSelectionModel().select(clipBoard_tiIcon);
    
    		// update in DB
    		conn.db.iconMove (clipBoard_tiIcon.getValue().getId(), trgItem.getValue().getId());
    		
    		// выводим сообщение в статус бар
    		params.setMsgToStatusBar("Пиктограмма '" + clipBoard_tiIcon.getValue().getName() + "' перемещена.");
    		
            clipBoard_tiIcon = null;
    	}
    }
    
    /**
     * Выбираем файл с картинкой для добавления и показываем ее.
     * При необходимости меняем размер на 20x20
     */
    @FXML
    private void handleButtonSelectImageForAdd() {
    	FileChooser fileChooser = new FileChooser();
    	Preferences prefs = Preferences.userNodeForPackage(IconsList_Controller.class);
    	String curDir;

        // Задаём фильтр расширений
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        
        // set directory
        curDir = prefs.get("icons_CurDirNameForAdd", "");
        if (! curDir.equals("")) 
        	fileChooser.setInitialDirectory(new File(curDir));
        
        // Показываем диалог загрузки файла
        File file = fileChooser.showOpenDialog(params.getMainStage());

        if (file != null) {
        	// обновляем контролы
        	textField_a_fileName.setText(file.toString());
        	//Image img = new Image(file.toURI().toString(), 20, 20, false, false);
        	Image img = new Image(file.toURI().toString());           // not resize
        	imageView_a_image.setImage(img);
        	label_a_imageSize.setText(img.getWidth() +" x "+ img.getHeight());
        	
        	// save dir name
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("icons_CurDirNameForAdd", curDir);
        }
    }
    
    /**
     * Выбираем файл с картинкой для изменения и показываем ее
     */
    @FXML
    private void handleButtonSelectImageForUpdate() {
    	FileChooser fileChooser = new FileChooser();
    	Preferences prefs = Preferences.userNodeForPackage(IconsList_Controller.class);
    	String curDir;

        // Задаём фильтр расширений
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        
        // set directory
        curDir = prefs.get("icons_CurDirNameForUpdate", "");
        if (! curDir.equals("")) 
        	fileChooser.setInitialDirectory(new File(curDir));

        // Показываем диалог загрузки файла
        File file = fileChooser.showOpenDialog(params.getMainStage());

        if (file != null) {
        	// обновляем контролы
        	textField_u_fileName.setText(file.toString());
        	//Image img = new Image(file.toURI().toString(), 20, 20, false, false);
        	Image img = new Image(file.toURI().toString());            // not resize
        	imageView_u_image.setImage(img);
        	label_u_imageSize.setText(img.getWidth() +" x "+ img.getHeight());
        	
        	// set flag for update image
        	isChangeImgForUpdate = true;
        	
        	// save dir name to Preferences
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("icons_CurDirNameForUpdate", curDir);
        }
    }
    
    /**
     * Добавляем новую пиктограмму в справочник
     */
    @FXML
    private void handleButtonAddIcon() {
    	long newId;
    	IconItem i = null;
    	
    	// проверяем выбранного родителя для добавляемой/редактируемой пиктограммы
    	if ((label_a_parentId.getText().equals("") || (label_a_parentId.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не выбран родительский раздел", "Выберите родительский раздел в дереве пиктограмм.");
    		return;
        }
    	
    	// check data in fields
    	if ((textField_a_name.getText().equals("") || (textField_a_name.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено поле 'Название пиктограммы'", "Укажите значение поля");
    		return;
        }
    	if ((textField_a_fileName.getText().equals("") || (textField_a_fileName.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не выбран файл с пиктограммой", "Выберите пиктограмму.");
    		return;
        }
    	
    	// create icon object and add her into db
    	newId = conn.db.iconNextId();
    	
    	i = new IconItem(
    			newId, Long.valueOf(label_a_parentId.getText()), textField_a_name.getText(),
    			textField_a_description.getText(), 
    			imageView_a_image.getImage(), new Date(), new Date(), "","");
    	
    	conn.db.iconAdd(i);           // обьект-иконку добавляем в БД
    	i = conn.db.iconGetById(i.getId());  // щоб підтягнути користувачів
    	
    	// добавляем в контрол-дерево
    	// Prepare a new TreeItem with a new Icon object
        TreeItem<IconItem> item = new TreeItem<>(new IconItem(i));
    	// Get the selection model
        TreeTableViewSelectionModel<IconItem> sm = treeTableView_icons.getSelectionModel();
        // Get the selected row index
    	int rowIndex = sm.getSelectedIndex();
        // Get the selected TreeItem
    	TreeItem<IconItem> selectedItem = sm.getModelItem(rowIndex);
        // Add the new item as children to the selected item
        selectedItem.getChildren().add(item);
        // Make sure the new item is visible
        selectedItem.setExpanded(true);
    	
    	// выводим сообщение в статус бар
        params.setMsgToStatusBar("Пиктограмма '" + i.getName() + "' добавлена.");
    }
    
    /**
     * Изменяем текущую пиктограмму в справочнике
     */
    @FXML
    private void handleButtonUpdateIcon() {
    	IconItem cii = null;           // текущая иконка
    	IconItem i = null;             // новая иконка
    	
    	// проверяем выбранную пиктограмму
    	if ((label_u_id.getText().equals("") || (label_u_id.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не выбрана пиктограмма.", "Выберите пиктограмму.");
    		return;
        }
    	
    	// check data in fields
    	if ((textField_u_name.getText().equals("") || (textField_u_name.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено поле 'Название пиктограммы'", "Укажите значение поля");
    		return;
        }
    	
    	// проверяем, менялось ли имя файла картинки вручную
    	TreeItem<IconItem> selectedItem = treeTableView_icons.getSelectionModel().getSelectedItem();
    	if (selectedItem == null) {
    		params.setMsgToStatusBar("Ничего не выбрано для изменения.");
    		return;
    	}
    	cii = (IconItem) selectedItem.getValue();
    	//if (! textField_u_fileName.getText().equals(cii.getFileName()))
    	if ((textField_u_fileName.getText().equals("") || (textField_u_fileName.getText() == null))) {
    		isChangeImgForUpdate = false;
    	} else {
    		// set flag for update image
        	isChangeImgForUpdate = true;
    	}
    	
    	// create icon object and add her into db
    	i = new IconItem(
    			cii.getId(), cii.getParentId(), textField_u_name.getText(),
    			textField_u_description.getText(), 
    			imageView_u_image.getImage(), cii.getDateCreated(), new Date(), cii.getUserCreated(), "");
    	
    	conn.db.iconUpdate(i, isChangeImgForUpdate);           // обьект-иконку изменяем в БД
    	i = conn.db.iconGetById(i.getId());     // підтягнути користувача
    	
    	// update in TreeTableView
    	selectedItem.setValue(null);
    	selectedItem.setValue(i);
    	
    	// выводим сообщение в статус бар
    	params.setMsgToStatusBar("Пиктограмма '" + cii.getName() + "' изменена.");
    }
    
    /**
     * Удаляем текущую пиктограмму из справочника
     */
    @FXML
    private void handleButtonDeleteIcon() {
    	TreeItem<IconItem> selectedItem = treeTableView_icons.getSelectionModel().getSelectedItem();
    	
    	if (selectedItem == null) {
    		params.setMsgToStatusBar("Ничего не выбрано для удаления.");
    		return;
    	}
    	
    	IconItem cii = (IconItem) selectedItem.getValue();
    	
    	if (cii.getId() == 0) {
    		ShowAppMsg.showAlert("INFORMATION", "Удаление пиктограммы", 
    				"Удаление пиктограммы '"+ cii.getName() +"'", "Удалять корневую пиктограмму нельзя !");
    		return;
    	}
    	
    	if (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление пиктограммы", 
                "Удаление пиктограммы '"+ cii.getName() +"'", "Удалить пиктограмму ?"))
    		return;
    	
    	int childrenCount = conn.db.iconGetNumberOfChildren(cii.getId());
    	
    	if ((childrenCount > 0) && (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление пиктограммы", 
                "Пиктограмма '"+ cii.getName() +"' содержит подраздел(ы).", "Удалить пиктограмму вместе с подразделами ?")))  
    		return;
    	
    	//---- deleting
    	try {
    		// delete from DB 
			conn.db.iconDelete(cii.getId());
			
			// delete from TreeTableView
	    	TreeItem<IconItem> parentItem = selectedItem.getParent();
	    	if (parentItem != null) {     // текущая иконка не корневая
	            parentItem.getChildren().remove(selectedItem);
	    	}
	    	
	    	// выводим сообщение в статус бар
	    	params.setMsgToStatusBar("Пиктограмма '" + cii.getName() + "' удалена.");
		} catch (KBase_DbConnEx e) {
			//e.printStackTrace();
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
		return AppItem_Interface.ELEMENT_ICON_LIST;
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
    	if (treeTableView_icons.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
			TreeTableColumn currentSortColumn = (TreeTableColumn) treeTableView_icons.getSortOrder().get(0);
			prefs.put("stageIconsList_sortColumnId",currentSortColumn.getId());
			prefs.put("stageIconsList_sortType",currentSortColumn.getSortType().toString());
		} else {
			prefs.remove("stageIconsList_sortColumnId");
			prefs.remove("stageIconsList_sortType");
		}

    	//
		params.getObjContainer().closeContainer(getOID());
    }
}
