package app.view;

import app.Main;
import app.lib.DateConv;
import app.model.ConfigMainItem;
import app.model.ConfigMainList;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.prefs.Preferences;

/**
 * Контроллер окна списка настроек приложения.
 *
 * @author Игорь Макаревич
 */
public class ConfigMainList_Controller {
    /**
     * Список настроек приложения
     */
    public ConfigMainList list;
    /**
     * dialogStage данного окна
     */
    private Stage dialogStage;
    /**
     * Ковертор даты/времени
     */
    public DateConv dateConv;

    @FXML
    public TreeTableView<ConfigMainItem> treeTableView_config;
    @FXML
    private TreeTableColumn<ConfigMainItem, String> treeTableColumn_name;
    @FXML
    private TreeTableColumn<ConfigMainItem, String> treeTableColumn_value;
    @FXML
    private TreeTableColumn<ConfigMainItem, String> treeTableColumn_descr;
    @FXML
    private TreeTableColumn<ConfigMainItem, LocalDate> treeTableColumn_date;
    @FXML
    private TreeTableColumn<ConfigMainItem, String> treeTableColumn_id;

    @FXML
    private Button button_Save;
    @FXML
    private Button button_Cancel;

    //
    private Preferences prefs;

    //
    private TreeTableColumn currentSortColumn;

    /**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public ConfigMainList_Controller () {
        dateConv = new DateConv();
    }

    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {    
    	
    }

    /*
     * 
     */
    public void setMainApp(ConfigMainList list, Stage dialogStage) {
        this.list = list;
        this.dialogStage = dialogStage;

        // init controls
        initControlsValue();
    }

    /**
     * Инициализирует контролы значениями из главного класса
     */
    private void initControlsValue() {
        prefs = Preferences.userNodeForPackage(ConfigMainList_Controller.class);

        //======== TreeTableView ============================================================================
        treeTableView_config.setEditable(true);

        Callback<TreeTableColumn<ConfigMainItem, String>, TreeTableCell<ConfigMainItem, String>> cellFactory
                = (TreeTableColumn<ConfigMainItem, String> param) -> new EditingCell();

        //-------- columns
        // setCellValueFactory
        treeTableColumn_name.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<ConfigMainItem, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getName())
        );
        treeTableColumn_value.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<ConfigMainItem, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getValue())
        );
        treeTableColumn_descr.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<ConfigMainItem, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
        );
        treeTableColumn_date.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<ConfigMainItem, LocalDate> param) ->
                        new ReadOnlyObjectWrapper<LocalDate>(param.getValue().getValue().getLastModified())
        );
        treeTableColumn_id.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<ConfigMainItem, String> param) ->
                        new ReadOnlyStringWrapper(
                                (param.getValue().getValue().getId() == 0) ?
                                        "" : Long.toString(param.getValue().getValue().getId())
                        ));

        // set/get Pref Width
        treeTableColumn_name.setPrefWidth(prefs.getDouble("ConfigMainList__treeTableColumn_name__PrefWidth", 100));

        treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                prefs.putDouble("ConfigMainList__treeTableColumn_name__PrefWidth", t1.doubleValue());
            }
        });

        treeTableColumn_value.setPrefWidth(prefs.getDouble("ConfigMainList__treeTableColumn_value__PrefWidth", 100));

        treeTableColumn_value.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                prefs.putDouble("ConfigMainList__treeTableColumn_value__PrefWidth", t1.doubleValue());
            }
        });

        treeTableColumn_descr.setPrefWidth(prefs.getDouble("ConfigMainList__treeTableColumn_descr__PrefWidth", 100));

        treeTableColumn_descr.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                prefs.putDouble("ConfigMainList__treeTableColumn_descr__PrefWidth", t1.doubleValue());
            }
        });

        treeTableColumn_date.setPrefWidth(prefs.getDouble("ConfigMainList__treeTableColumn_date__PrefWidth", 100));

        treeTableColumn_date.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                prefs.putDouble("ConfigMainList__treeTableColumn_date__PrefWidth", t1.doubleValue());
            }
        });

        treeTableColumn_id.setPrefWidth(prefs.getDouble("ConfigMainList__treeTableColumn_id__PrefWidth", 100));

        treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                prefs.putDouble("ConfigMainList__treeTableColumn_id__PrefWidth", t1.doubleValue());
            }
        });

        //-------- for editing cells
        treeTableColumn_value.setCellFactory(cellFactory);
        treeTableColumn_value.setOnEditCommit(
                (TreeTableColumn.CellEditEvent<ConfigMainItem, String> t) -> {
                    ((ConfigMainItem) t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow())
                            .getValue()).setValue(t.getNewValue()
                            );

                });

        treeTableColumn_descr.setCellFactory(cellFactory);
        treeTableColumn_descr.setOnEditCommit(
                (TreeTableColumn.CellEditEvent<ConfigMainItem, String> t) -> {
                    ((ConfigMainItem) t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow())
                            .getValue()).setDescr(t.getNewValue()
                    );

                });

        //-------- init
        final TreeItem<ConfigMainItem> root =
                new TreeItem<>(new ConfigMainItem(
                    -1, "root", "root", "это корень, его не показываем", "null",
                    LocalDate.now(), true, false));
        root.setExpanded(true);
        treeTableView_config.setShowRoot(false);
        treeTableView_config.setRoot(root);
        addTreeItems(root);

        // cell factory to display graphic
        treeTableColumn_name.setCellFactory(ttc -> new TreeTableCell<ConfigMainItem, String>() {
            private ConfigMainItem row;
            private ImageView graphic;

            @Override
            protected void updateItem(String item, boolean empty) {    // display graphic
                try {
                    row = getTreeTableRow().getItem();
                    if (row.getId() == 0) {                     // section
                        graphic = new ImageView(new Image("file:resources/images/icon_setting_section_16.png"));
                    } else {                                    // item
                        if (row.getIsEditable()) {
                            graphic = new ImageView(new Image("file:resources/images/icon_setting_item_16.png"));
                        } else {
                            graphic = new ImageView(new Image("file:resources/images/icon_setting_item_disable_16.png"));
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

        //---- восстанавливаем сортировку таблицы по столбцу
        String sortColumnId = prefs.get("ConfigMainList_sortColumnId","");

        if (! sortColumnId.equals("")) {
            for (TreeTableColumn column : treeTableView_config.getColumns()) {
                if (column.getId().equals(sortColumnId)) {
                    String sortType = prefs.get("ConfigMainList_sortType","ASCENDING");

                    treeTableView_config.setSortMode(TreeSortMode.ALL_DESCENDANTS);
                    column.setSortable(true); // This performs a sort
                    treeTableView_config.getSortOrder().add(column);
                    if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
                    else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
                    treeTableView_config.sort();
                }
            }
        }

        //======== buttons =========================================
        button_Save.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
        button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }

    /**
     * Инициализация TreeTableView.
     */
    private void addTreeItems (TreeItem<ConfigMainItem> ti_root) {

        //-------- вставляем разделы
        for (ConfigMainItem i : list.list) {
            TreeItem<ConfigMainItem> ti_section = null;
            TreeItem<ConfigMainItem> ti_item;

            // проверяем, если этот элемент нельзя показывать, то пропускаем итерацию
            if (! i.getIsShow()) continue;

            // проверяем наличие такого раздела в дереве-контроле ; вставляем если нету
            for (TreeItem<ConfigMainItem> ti : ti_root.getChildren()) {
                if (ti.getValue().getName().equals(i.getSectionName())) {
                    ti_section = ti;
                    break;
                }
            }
            if (ti_section == null) {
                ti_section = new TreeItem<>(new ConfigMainItem(
                        0, "section", i.getSectionName(), "", "",
                        LocalDate.now(), true, false));
                ti_root.getChildren().add(ti_section);
            }

            // вставляем текущий итем в раздел
            ti_item = new TreeItem<>(new ConfigMainItem(i));
            ti_section.getChildren().add(ti_item);
        }
    }

    /**
     * Сохраняем текущее состояние фрейма и контролов
     */
    private void saveState() {

        //-------- size and position
        prefs.putDouble("ConfigMainList_Width", dialogStage.getWidth());
        prefs.putDouble("ConfigMainList_Height",dialogStage.getHeight());
        prefs.putDouble("ConfigMainList_PosX",  dialogStage.getX());
        prefs.putDouble("ConfigMainList_PosY",  dialogStage.getY());

        //-------- sort
        if (treeTableView_config.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
            currentSortColumn = (TreeTableColumn) treeTableView_config.getSortOrder().get(0);
            prefs.put("ConfigMainList_sortColumnId",currentSortColumn.getId());
            prefs.put("ConfigMainList_sortType",currentSortColumn.getSortType().toString());
        } else {
            prefs.remove("ConfigMainList_sortColumnId");
            prefs.remove("ConfigMainList_sortType");
        }
    }

    /**
     * Вызывается при нажатии на кнопке "Сохранить"
     */
    @FXML
    private void handleButtonSave() {
        //-------- save stage position
        saveState();

        //-------- save changes in the config
        ConfigMainItem configItem;
        boolean        isDateChange;   // если меняется значение и/или описание, то меняем дату

        for (TreeItem<ConfigMainItem> si : treeTableView_config.getRoot().getChildren()) {
            for (TreeItem<ConfigMainItem> ti : si.getChildren()) {
                configItem = list.getItem(si.getValue().getName(),ti.getValue().getName());
                isDateChange = false;

                if (! configItem.getValue().equals(ti.getValue().getValue())) {
                    configItem.setValue(ti.getValue().getValue());
                    isDateChange = true;
                }

                if ((configItem.getDescr() == null) && (ti.getValue().getDescr() == null)) {
                } else {
                    if (((configItem.getDescr() == null) && (ti.getValue().getDescr() != null)) ||
                            ((configItem.getDescr() != null) && (ti.getValue().getDescr() == null)) ||
                            (!configItem.getDescr().equals(ti.getValue().getDescr()))) {
                        configItem.setDescr(ti.getValue().getDescr());
                        isDateChange = true;
                    }
                }

                if (isDateChange) {
                    configItem.setLastModified(LocalDate.now());
                }
            }
        }

        list.saveToFile();

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
        saveState();

        // get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    /**
     *
     */
    class EditingCell extends TreeTableCell<ConfigMainItem, String> {

        private TextField textField;

        private EditingCell() {
        }

        @Override
        public void startEdit() {
            if ((!isEmpty()) && (getTreeTableRow().getItem().getIsEditable())) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(item);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
//                        setGraphic(null);
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnAction((e) -> commitEdit(textField.getText()));
            textField.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (!newValue) {
                    //System.out.println("Commiting " + textField.getText());
                    commitEdit(textField.getText());
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }
}
