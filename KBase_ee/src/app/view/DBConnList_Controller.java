package app.view;

import app.Main;
import app.exceptions.KBase_DublicateConnIdEx;
import app.lib.ConvertType;
import app.lib.ShowAppMsg;
import app.model.DBConCur_Parameters;
import app.model.DBConnFolder;
import app.model.DBConnTree_Parameters;
import app.model.DBConn_Parameters;
import app.model.Params;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
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
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Контролер вікна дерева підключень до БД.
 * Папки з необмеженою вкладеністю, підключення всередині.
 * Видимий корінь "Підключення" — папки першого рівня створюються в ньому.
 *
 * @author Ігор Макаревич
 */
public class DBConnList_Controller {
	private Params params;
	/**
	 * Посилання на головний застосунок
	 */
	private Main mainApp;
	/**
	 * Дерево підключень
	 */
	public DBConnTree_Parameters dbTree;
	/**
	 * dialogStage даного вікна
	 */
	private Stage dialogStage;

	@FXML
	private TreeTableView<Object> treeTableView_Connections;
	@FXML
	private TreeTableColumn<Object, String> treeTableColumn_ConnName;
	@FXML
	private TreeTableColumn<Object, String> treeTableColumn_Descr;
	@FXML
	private TreeTableColumn<Object, String> treeTableColumn_Type;
	@FXML
	private TreeTableColumn<Object, String> treeTableColumn_Host;
	@FXML
	private TreeTableColumn<Object, String> treeTableColumn_Port;
	@FXML
	private TreeTableColumn<Object, String> treeTableColumn_Name;
	@FXML
	private TreeTableColumn<Object, String> treeTableColumn_Login;
	@FXML
	private TreeTableColumn<Object, Boolean> treeTableColumn_AutoConn;
	@FXML
	private TreeTableColumn<Object, LocalDate> treeTableColumn_lastConn;
	@FXML
	private TreeTableColumn<Object, Integer> treeTableColumn_counter;

	@FXML
	private CheckBox checkBox_ShowColors;

	@FXML
	private MenuItem menuitem_AddFolder;
	@FXML
	private MenuItem menuitem_Add;
	@FXML
	private MenuItem menuitem_Edit;
	@FXML
	private MenuItem menuitem_Delete;
	@FXML
	private MenuItem menuitem_Cut;
	@FXML
	private MenuItem menuitem_Copy;
	@FXML
	private MenuItem menuitem_Paste;
	@FXML
	private MenuItem menuitem_Duplicate;
	@FXML
	private MenuItem menuitem_Connect;
	@FXML
	private MenuItem menuitem_Disconnect;

	@FXML
	private Button button_AddFolder;
	@FXML
	private Button button_Add;
	@FXML
	private Button button_Edit;
	@FXML
	private Button button_Delete;
	@FXML
	private Button button_Connect;
	@FXML
	private Button button_Disconnect;
	@FXML
	private Button button_Exit;

	private Preferences prefs;

	//
	private TreeTableColumn currentSortColumn;

	/**
	 * Папка-призначення для нового підключення (встановлюється перед діалогом додавання)
	 */
	private int pendingParentFolderId = DBConnTree_Parameters.ROOT_FOLDER_ID;

	/**
	 * Показувати елементи в кольорах з'єднань (стан чекбокса, зберігається).
	 */
	private boolean showColors = true;

	/**
	 * Буфер обміну для вирізання/копіювання (лише один з Id встановлено).
	 */
	private Integer clipboardFolderId = null;
	private Integer clipboardConnId = null;
	private boolean clipboardIsCut = false;

	/**
	 * Ctrl на старті перетягування (на Linux режим дропа визначаємо по ньому,
	 * як у дереві Розділів — там transferMode дропа недостовірний).
	 */
	private boolean dragStartedWithCtrl = false;

	private Image folderIcon;
	private Image postgresIcon;
	private Image sqliteIcon;
	private Image oracleIcon;
	private Image activeIcon;

	/**
	 * Конструктор.
	 */
	public DBConnList_Controller() {
	}

	/**
	 * Ініціалізація класу-контролера. Викликається автоматично після завантаження fxml.
	 */
	@FXML
	private void initialize() {
	}

	/**
	 * Викликається батьківським об'єктом, передає параметри.
	 */
	public void setParams(Params params) {
		this.params = params;
		this.dbTree = new DBConnTree_Parameters(params);
		this.mainApp = params.getMain();
		this.dialogStage = params.getStageCur();

		initControlsValue();
	}

	/**
	 * Ініціалізує контроли значеннями.
	 */
	private void initControlsValue() {
		prefs = Preferences.userNodeForPackage(DBConnList_Controller.class);

		loadIcons();
		initColumns();

		// чекбокс показу кольорів (стан зберігається)
		showColors = prefs.getBoolean("DBConnList_showColors", true);
		checkBox_ShowColors.setSelected(showColors);
		checkBox_ShowColors.selectedProperty().addListener((obs, oldVal, newVal) -> {
			showColors = newVal;
			prefs.putBoolean("DBConnList_showColors", newVal);
			treeTableView_Connections.refresh();
		});

		treeTableView_Connections.setShowRoot(true);
		buildTree();
		initDragAndDrop();

		// перефарбування при навігації: вибраний елемент — активні кольори, решта — неактивні
		treeTableView_Connections.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> {
					treeTableView_Connections.refresh();
					updateClipboardMenuState();
				});

		treeTableView_Connections.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				TreeItem<Object> sel = treeTableView_Connections.getSelectionModel().getSelectedItem();
				if (sel == null || sel.getValue() == null) {
					return;
				}
				if (sel.getValue() instanceof DBConn_Parameters) {
					handleButtonConnect();
				} else {
					sel.setExpanded(!sel.isExpanded());
				}
			}
		});

		// ContextMenu
		menuitem_AddFolder.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
		menuitem_Add.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
		menuitem_Edit.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_16.png")));
		menuitem_Delete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
		menuitem_Cut.setGraphic(new ImageView(new Image("file:resources/images/icon_cut_16.png")));
		menuitem_Copy.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_16.png")));
		menuitem_Paste.setGraphic(new ImageView(new Image("file:resources/images/icon_paste_16.png")));
		menuitem_Duplicate.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_16.png")));
		menuitem_Connect.setGraphic(new ImageView(new Image("file:resources/images/icon_Connect_16.png")));
		menuitem_Disconnect.setGraphic(new ImageView(new Image("file:resources/images/icon_disconnect_16.png")));

		// buttons
		button_AddFolder.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
		button_Add.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
		button_Edit.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_16.png")));
		button_Delete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
		button_Connect.setGraphic(new ImageView(new Image("file:resources/images/icon_Connect_16.png")));
		button_Disconnect.setGraphic(new ImageView(new Image("file:resources/images/icon_disconnect_16.png")));
		button_Exit.setGraphic(new ImageView(new Image("file:resources/images/icon_close_16.png")));

		// гарячі клавіші буфера обміну (працюють і при закритому контекстному меню)
		if (dialogStage != null && dialogStage.getScene() != null) {
			dialogStage.getScene().getAccelerators().put(
					new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN), this::handleMenuCut);
			dialogStage.getScene().getAccelerators().put(
					new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN), this::handleMenuCopy);
			dialogStage.getScene().getAccelerators().put(
					new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN), this::handleMenuPaste);
			dialogStage.getScene().getAccelerators().put(
					new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), this::handleMenuDuplicate);
		}

		updateClipboardMenuState();
	}

	/**
	 * Ініціалізація колонок: значення, ширина (зберігається), сортування (зберігається), вигляд клітинок.
	 */
	private void initColumns() {
		//======== значення колонок
		treeTableColumn_ConnName.setCellValueFactory(param -> {
			Object v = param.getValue().getValue();
			if (v instanceof DBConnFolder) {
				return new ReadOnlyStringWrapper(((DBConnFolder) v).getFolderName());
			}
			if (v instanceof DBConn_Parameters) {
				return new ReadOnlyStringWrapper(((DBConn_Parameters) v).getConnName());
			}
			return new ReadOnlyStringWrapper(v == null ? "" : v.toString());
		});
		treeTableColumn_Descr.setCellValueFactory(param -> {
			Object v = param.getValue().getValue();
			if (v instanceof DBConnFolder) {
				return new ReadOnlyStringWrapper(((DBConnFolder) v).getDescr());
			}
			if (v instanceof DBConn_Parameters) {
				return new ReadOnlyStringWrapper(((DBConn_Parameters) v).getDescr());
			}
			return new ReadOnlyStringWrapper("");
		});
		treeTableColumn_Type.setCellValueFactory(param -> {
			Object v = param.getValue().getValue();
			if (v instanceof DBConn_Parameters) {
				return new ReadOnlyStringWrapper(typeTitle(((DBConn_Parameters) v).getType()));
			}
			return new ReadOnlyStringWrapper("");
		});
		treeTableColumn_Host.setCellValueFactory(param -> {
			Object v = param.getValue().getValue();
			if (v instanceof DBConn_Parameters) {
				return new ReadOnlyStringWrapper(((DBConn_Parameters) v).getHost());
			}
			return new ReadOnlyStringWrapper("");
		});
		treeTableColumn_Port.setCellValueFactory(param -> {
			Object v = param.getValue().getValue();
			if (v instanceof DBConn_Parameters) {
				return new ReadOnlyStringWrapper(((DBConn_Parameters) v).getPort());
			}
			return new ReadOnlyStringWrapper("");
		});
		treeTableColumn_Name.setCellValueFactory(param -> {
			Object v = param.getValue().getValue();
			if (v instanceof DBConn_Parameters) {
				return new ReadOnlyStringWrapper(((DBConn_Parameters) v).getName());
			}
			return new ReadOnlyStringWrapper("");
		});
		treeTableColumn_Login.setCellValueFactory(param -> {
			Object v = param.getValue().getValue();
			if (v instanceof DBConn_Parameters) {
				return new ReadOnlyStringWrapper(((DBConn_Parameters) v).getLogin());
			}
			return new ReadOnlyStringWrapper("");
		});
		treeTableColumn_AutoConn.setCellValueFactory(param -> {
			Object v = param.getValue().getValue();
			if (v instanceof DBConn_Parameters) {
				return new ReadOnlyObjectWrapper<Boolean>(((DBConn_Parameters) v).getAutoConn());
			}
			return new ReadOnlyObjectWrapper<Boolean>(null);
		});
		treeTableColumn_lastConn.setCellValueFactory(param -> {
			Object v = param.getValue().getValue();
			if (v instanceof DBConn_Parameters) {
				return new ReadOnlyObjectWrapper<LocalDate>(((DBConn_Parameters) v).getLastConn());
			}
			return new ReadOnlyObjectWrapper<LocalDate>(null);
		});
		treeTableColumn_counter.setCellValueFactory(param -> {
			Object v = param.getValue().getValue();
			if (v instanceof DBConn_Parameters) {
				return new ReadOnlyObjectWrapper<Integer>(((DBConn_Parameters) v).getCounter());
			}
			return new ReadOnlyObjectWrapper<Integer>(null);
		});

		// сортування порожніх (папки) — nulls першими
		treeTableColumn_AutoConn.setComparator(Comparator.nullsFirst(Comparator.naturalOrder()));
		treeTableColumn_lastConn.setComparator(Comparator.nullsFirst(Comparator.naturalOrder()));
		treeTableColumn_counter.setComparator(Comparator.nullsFirst(Comparator.naturalOrder()));

		//======== ширина колонок (зберігається, як в інших деревах проєкту)
		initColumnWidth(treeTableColumn_ConnName, "DBConnList__treeTableColumn_ConnName__PrefWidth");
		initColumnWidth(treeTableColumn_Descr, "DBConnList__treeTableColumn_Descr__PrefWidth");
		initColumnWidth(treeTableColumn_Type, "DBConnList__treeTableColumn_Type__PrefWidth");
		initColumnWidth(treeTableColumn_Host, "DBConnList__treeTableColumn_Host__PrefWidth");
		initColumnWidth(treeTableColumn_Port, "DBConnList__treeTableColumn_Port__PrefWidth");
		initColumnWidth(treeTableColumn_Name, "DBConnList__treeTableColumn_Name__PrefWidth");
		initColumnWidth(treeTableColumn_Login, "DBConnList__treeTableColumn_Login__PrefWidth");
		initColumnWidth(treeTableColumn_AutoConn, "DBConnList__treeTableColumn_AutoConn__PrefWidth");
		initColumnWidth(treeTableColumn_lastConn, "DBConnList__treeTableColumn_lastConn__PrefWidth");
		initColumnWidth(treeTableColumn_counter, "DBConnList__treeTableColumn_counter__PrefWidth");

		//======== вигляд клітинок
		treeTableColumn_ConnName.setCellFactory(ttc -> new TreeTableCell<Object, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				TreeTableRow<Object> row = getTreeTableRow();
				Object v = (row == null) ? null : row.getItem();
				if (empty || v == null) {
					setText(null);
					setGraphic(null);
					setStyle("");
					setTooltip(null);
					return;
				}
				if (v instanceof DBConnFolder) {
					DBConnFolder f = (DBConnFolder) v;
					int cnt = f.getFolders().size() + f.getConnections().size();
					setText((item == null ? "" : item) + (cnt > 0 ? " (" + cnt + ")" : ""));
					setGraphic(new ImageView(folderIcon));
					setStyle("");
					String tip = (f.getDescr() != null && !f.getDescr().isEmpty()) ? f.getDescr() : "Папка";
					setTooltip(new Tooltip(tip));
				} else if (v instanceof DBConn_Parameters) {
					DBConn_Parameters c = (DBConn_Parameters) v;
					boolean open = isConnectionActive(c.getConnId());
					setText((item == null ? "" : item) + (open ? "  ●" : ""));
					setGraphic(new ImageView(getTypeIcon(c.getType())));
					setTooltip(new Tooltip(buildConnTooltip(c, open)));
					styleConnectionCell(this, c);
				} else {
					setText(item);
					setGraphic(new ImageView(folderIcon));
					setStyle("");
					setTooltip(null);
				}
			}
		});

		treeTableColumn_Descr.setCellFactory(ttc -> createTextCell());
		treeTableColumn_Type.setCellFactory(ttc -> createTextCell());
		treeTableColumn_Host.setCellFactory(ttc -> createTextCell());
		treeTableColumn_Port.setCellFactory(ttc -> createTextCell());
		treeTableColumn_Name.setCellFactory(ttc -> createTextCell());
		treeTableColumn_Login.setCellFactory(ttc -> createTextCell());

		treeTableColumn_AutoConn.setCellFactory(ttc -> new TreeTableCell<Object, Boolean>() {
			private final CheckBox checkBox = new CheckBox();
			{
				checkBox.setDisable(true);
				checkBox.setFocusTraversable(false);
				setAlignment(Pos.CENTER);
			}
			@Override
			protected void updateItem(Boolean item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setText(null);
					setGraphic(null);
					setStyle("");
					return;
				}
				TreeTableRow<Object> row = getTreeTableRow();
				Object v = (row == null) ? null : row.getItem();
				if (v instanceof DBConn_Parameters) {
					checkBox.setSelected(item != null && item);
					setText(null);
					setGraphic(checkBox);
					styleConnectionCell(this, (DBConn_Parameters) v);
				} else {
					setText(null);
					setGraphic(null);
					setStyle("");
				}
			}
		});

		treeTableColumn_lastConn.setCellFactory(ttc -> new TreeTableCell<Object, LocalDate>() {
			@Override
			protected void updateItem(LocalDate item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setText(null);
					setGraphic(null);
					setStyle("");
					return;
				}
				setText(item == null ? "" : item.toString());
				setGraphic(null);
				TreeTableRow<Object> row = getTreeTableRow();
				Object v = (row == null) ? null : row.getItem();
				if (v instanceof DBConn_Parameters) {
					styleConnectionCell(this, (DBConn_Parameters) v);
				} else {
					setStyle("");
				}
			}
		});

		treeTableColumn_counter.setCellFactory(ttc -> new TreeTableCell<Object, Integer>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setText(null);
					setGraphic(null);
					setStyle("");
					return;
				}
				setText(item == null ? "" : item.toString());
				setGraphic(null);
				TreeTableRow<Object> row = getTreeTableRow();
				Object v = (row == null) ? null : row.getItem();
				if (v instanceof DBConn_Parameters) {
					styleConnectionCell(this, (DBConn_Parameters) v);
				} else {
					setStyle("");
				}
			}
		});

		//======== збереження сортування (як в інших деревах проєкту)
		treeTableView_Connections.getSortOrder().addListener((ListChangeListener<TreeTableColumn<Object, ?>>) change -> {
			if (!change.getList().isEmpty()) {
				TreeTableColumn<Object, ?> sortedColumn = change.getList().get(0);
				prefs.put("stageDBConnList_sortColumnId", sortedColumn.getId());
				prefs.put("stageDBConnList_sortType", sortedColumn.getSortType().toString());
			}
		});

		for (TreeTableColumn<Object, ?> column : treeTableView_Connections.getColumns()) {
			column.sortTypeProperty().addListener((obs, oldVal, newVal) -> {
				if (treeTableView_Connections.getSortOrder().contains(column)) {
					prefs.put("stageDBConnList_sortColumnId", column.getId());
					prefs.put("stageDBConnList_sortType", newVal.toString());
				}
			});
		}
	}

	/**
	 * Ширина колонки: відновлення зі сховища + збереження при зміні.
	 */
	private void initColumnWidth(TreeTableColumn<Object, ?> column, String prefKey) {
		column.setPrefWidth(prefs.getDouble(prefKey, column.getPrefWidth()));
		column.widthProperty().addListener((obs, oldVal, newVal) -> prefs.putDouble(prefKey, newVal.doubleValue()));
	}

	/**
	 * Текстова клітинка з кольорами з'єднання.
	 */
	private TreeTableCell<Object, String> createTextCell() {
		return new TreeTableCell<Object, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setText(null);
					setGraphic(null);
					setStyle("");
					return;
				}
				setText(item);
				setGraphic(null);
				TreeTableRow<Object> row = getTreeTableRow();
				Object v = (row == null) ? null : row.getItem();
				if (v instanceof DBConn_Parameters) {
					styleConnectionCell(this, (DBConn_Parameters) v);
				} else {
					setStyle("");
				}
			}
		};
	}

	/**
	 * Кольори клітинки з'єднання: вибраний елемент — активні кольори, решта — неактивні.
	 * Відкрите з'єднання позначається лише маркером ● (не кольором).
	 */
	private void styleConnectionCell(TreeTableCell<?, ?> cell, DBConn_Parameters c) {
		boolean selected = cell.getTreeTableRow() != null && cell.getTreeTableRow().isSelected();
		boolean open = isConnectionActive(c.getConnId());
		if (!showColors || !c.getColorEnable()) {
			cell.setStyle(open ? "-fx-font-weight: bold;" : "");
			return;
		}
		try {
			Color tc;
			Color bc;
			if (selected) {
				tc = new Color(c.getColorTRed_A(), c.getColorTGreen_A(), c.getColorTBlue_A(), c.getColorTOpacity_A());
				bc = new Color(c.getColorBRed_A(), c.getColorBGreen_A(), c.getColorBBlue_A(), c.getColorBOpacity_A());
			} else {
				tc = new Color(c.getColorTRed_N(), c.getColorTGreen_N(), c.getColorTBlue_N(), c.getColorTOpacity_N());
				bc = new Color(c.getColorBRed_N(), c.getColorBGreen_N(), c.getColorBBlue_N(), c.getColorBOpacity_N());
			}
			cell.setStyle("-fx-text-fill: #" + ConvertType.colorToHex(tc) + "; "
					+ "-fx-background-color: #" + ConvertType.colorToHex(bc) + ";"
					+ (open ? " -fx-font-weight: bold;" : ""));
		} catch (Exception e) {
			cell.setStyle(open ? "-fx-font-weight: bold;" : "");
		}
	}

	/**
	 * Підказка для з'єднання.
	 */
	private String buildConnTooltip(DBConn_Parameters c, boolean open) {
		StringBuilder sb = new StringBuilder();
		if (c.getDescr() != null && !c.getDescr().isEmpty()) {
			sb.append(c.getDescr()).append("\n");
		}
		sb.append(typeTitle(c.getType()));
		if (c.getHost() != null && !c.getHost().isEmpty()) {
			sb.append("  ").append(c.getHost());
		}
		if (c.getPort() != null && !c.getPort().isEmpty()) {
			sb.append(":").append(c.getPort());
		}
		if (c.getName() != null && !c.getName().isEmpty()) {
			sb.append(" / ").append(c.getName());
		}
		if (open) {
			sb.append("\nВідкрите ●");
		}
		return sb.toString();
	}

	private void loadIcons() {
		try {
			folderIcon = new Image("file:resources/images/db/icon_folder_16.png");
			postgresIcon = new Image("file:resources/images/db/icon_postgres_16.png");
			sqliteIcon = new Image("file:resources/images/db/icon_sqlite_16.png");
			oracleIcon = new Image("file:resources/images/db/icon_oracle_16.png");
			activeIcon = new Image("file:resources/images/db/icon_active_16.png");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// запасний варіант якщо файли відсутні
		if (folderIcon == null || folderIcon.isError()) {
			folderIcon = new Image("file:resources/images/icon_file_open_16.png");
		}
		if (postgresIcon == null || postgresIcon.isError()) {
			postgresIcon = new Image("file:resources/images/icon_DBTools_16.png");
		}
		if (sqliteIcon == null || sqliteIcon.isError()) {
			sqliteIcon = new Image("file:resources/images/icon_document_16.png");
		}
		if (oracleIcon == null || oracleIcon.isError()) {
			oracleIcon = new Image("file:resources/images/icon_DBTools_16.png");
		}
	}

	private Image getTypeIcon(String type) {
		if (DBConn_Parameters.TYPE_CONN_POSTGRES.equals(type)) {
			return postgresIcon;
		}
		if (DBConn_Parameters.TYPE_CONN_SQLITE.equals(type)) {
			return sqliteIcon;
		}
		if (DBConn_Parameters.TYPE_CONN_ORACLE.equals(type)) {
			return oracleIcon;
		}
		return postgresIcon;
	}

	private String typeTitle(String type) {
		if (DBConn_Parameters.TYPE_CONN_POSTGRES.equals(type)) {
			return "PostgreSQL";
		}
		if (DBConn_Parameters.TYPE_CONN_SQLITE.equals(type)) {
			return "SQLite";
		}
		if (DBConn_Parameters.TYPE_CONN_ORACLE.equals(type)) {
			return "Oracle";
		}
		return type == null ? "" : type;
	}

	/**
	 * Чи підключення зараз відкрите.
	 */
	private boolean isConnectionActive(int connId) {
		if (params == null || params.getConnDB() == null) {
			return false;
		}
		for (DBConCur_Parameters con : params.getConnDB().conList) {
			if (con.param.getConnId() == connId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Відновлення збереженого сортування (як в інших деревах проєкту).
	 */
	private void restoreSort() {
		String sortColumnId = prefs.get("stageDBConnList_sortColumnId", "");
		if (sortColumnId == null || sortColumnId.isEmpty()) {
			return;
		}
		for (TreeTableColumn<Object, ?> column : treeTableView_Connections.getColumns()) {
			if (sortColumnId.equals(column.getId())) {
				String sortType = prefs.get("stageDBConnList_sortType", "ASCENDING");
				treeTableView_Connections.setSortMode(TreeSortMode.ALL_DESCENDANTS);
				column.setSortable(true);
				if (!treeTableView_Connections.getSortOrder().contains(column)) {
					treeTableView_Connections.getSortOrder().add(column);
				}
				if ("DESCENDING".equals(sortType)) {
					column.setSortType(TreeTableColumn.SortType.DESCENDING);
				} else {
					column.setSortType(TreeTableColumn.SortType.ASCENDING);
				}
				treeTableView_Connections.sort();
			}
		}
	}

	/**
	 * Drag&Drop: переміщення (перетягування) та копіювання (Ctrl+перетягування)
	 * папок і з'єднань, як в інших деревах проєкту.
	 */
	private void initDragAndDrop() {
		treeTableView_Connections.setRowFactory(tv -> {
			TreeTableRow<Object> row = new TreeTableRow<>();
			row.setOnDragDetected(event -> {
				TreeItem<Object> selected = treeTableView_Connections.getSelectionModel().getSelectedItem();
				if (selected != null
						&& (selected.getValue() instanceof DBConnFolder
								|| selected.getValue() instanceof DBConn_Parameters)) {
					dragStartedWithCtrl = event.isControlDown();
					Dragboard db = row.startDragAndDrop(TransferMode.COPY_OR_MOVE);
					try {
						db.setDragView(row.snapshot(null, null));
					} catch (Exception e) {
						// без мініатюри — не критично
					}
					ClipboardContent content = new ClipboardContent();
					content.put(params.getMain().SERIALIZED_MIME_TYPE, row.getIndex());
					db.setContent(content);
					event.consume();
				}
			});
			row.setOnDragOver(event -> {
				if (acceptableForDragAndDrop(event.getDragboard(), row)) {
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
					event.consume();
				}
			});
			row.setOnDragDropped(event -> {
				Dragboard db = event.getDragboard();
				if (acceptableForDragAndDrop(db, row)) {
					int index = (Integer) db.getContent(params.getMain().SERIALIZED_MIME_TYPE);
					TreeItem<Object> dragItem = null;
					try {
						dragItem = treeTableView_Connections.getTreeItem(index);
					} catch (Exception e) {
						dragItem = null;
					}
					if (dragItem != null) {
						TransferMode actionTransferMode = event.getAcceptedTransferMode();
						if (System.getProperty("os.name").toLowerCase().contains("linux")) {
							actionTransferMode = dragStartedWithCtrl ? TransferMode.COPY : TransferMode.MOVE;
						}
						if (actionTransferMode == TransferMode.MOVE) {
							dropMove(dragItem, row);
						} else if (actionTransferMode == TransferMode.COPY) {
							dropCopy(dragItem, row);
						} else {
							ShowAppMsg.showAlert("WARNING", "Перетягування", "Невідомий режим перетягування", "Не обробляється.");
						}
						event.setDropCompleted(true);
					}
					event.consume();
				}
			});
			return row;
		});
	}

	/**
	 * Чи можна кинути перетягнуте на даний рядок (захист від циклів).
	 */
	private boolean acceptableForDragAndDrop(Dragboard db, TreeTableRow<Object> row) {
		if (db == null || !db.hasContent(params.getMain().SERIALIZED_MIME_TYPE)) {
			return false;
		}
		int index;
		try {
			index = (Integer) db.getContent(params.getMain().SERIALIZED_MIME_TYPE);
		} catch (Exception e) {
			return false;
		}
		if (row.getIndex() == index) {
			return false;
		}
		TreeItem<Object> dragItem;
		try {
			dragItem = treeTableView_Connections.getTreeItem(index);
		} catch (Exception e) {
			return false;
		}
		if (dragItem == null || dragItem.getValue() == null) {
			return false;
		}
		if (!(dragItem.getValue() instanceof DBConnFolder)
				&& !(dragItem.getValue() instanceof DBConn_Parameters)) {
			return false;
		}
		// папку не можна кидати в саму себе або у її вкладену папку
		if (dragItem.getValue() instanceof DBConnFolder) {
			int dragId = ((DBConnFolder) dragItem.getValue()).getFolderId();
			Integer targetId = targetFolderIdForRow(row);
			if (targetId != null && targetId != DBConnTree_Parameters.ROOT_FOLDER_ID) {
				if (targetId == dragId) {
					return false;
				}
				DBConnFolder target = dbTree.findFolderById(targetId);
				while (target != null) {
					if (target.getFolderId() == dragId) {
						return false;
					}
					target = dbTree.findParentFolderOfFolder(target.getFolderId());
				}
			}
		}
		return true;
	}

	/**
	 * Папка-призначення для рядка-приймача (порожній рядок — корінь).
	 */
	private Integer targetFolderIdForRow(TreeTableRow<Object> row) {
		if (row == null || row.isEmpty() || row.getTreeItem() == null || row.getTreeItem().getValue() == null) {
			return DBConnTree_Parameters.ROOT_FOLDER_ID;
		}
		Object v = row.getTreeItem().getValue();
		if (v instanceof DBConnFolder) {
			return ((DBConnFolder) v).getFolderId();
		}
		if (v instanceof DBConn_Parameters) {
			DBConnFolder parent = dbTree.findParentFolderOfConnection(((DBConn_Parameters) v).getConnId());
			return parent == null ? DBConnTree_Parameters.ROOT_FOLDER_ID : parent.getFolderId();
		}
		return DBConnTree_Parameters.ROOT_FOLDER_ID;
	}

	/**
	 * Перетягнуто без Ctrl — переміщення (з підтвердженням, як в інших деревах).
	 */
	private void dropMove(TreeItem<Object> dragItem, TreeTableRow<Object> row) {
		Object v = dragItem.getValue();
		int targetId = targetFolderIdForRow(row);
		if (v instanceof DBConn_Parameters) {
			DBConn_Parameters c = (DBConn_Parameters) v;
			if (ShowAppMsg.showQuestion("CONFIRMATION", "Переміщення з'єднання",
					"Перемістити з'єднання '" + c.getConnName() + "' в " + targetFolderTitle(targetId) + "?", "Перемістити?")) {
				if (dbTree.moveConnection(c.getConnId(), targetId)) {
					expandTreeItemByFolderId(targetId);
					dbTree.saveToFile();
					refreshTree();
					selectTreeItemById(null, c.getConnId());
				} else {
					ShowAppMsg.showAlert("WARNING", "Переміщення", "Неможливо перемістити з'єднання", "");
				}
			}
		} else if (v instanceof DBConnFolder) {
			DBConnFolder f = (DBConnFolder) v;
			if (ShowAppMsg.showQuestion("CONFIRMATION", "Переміщення папки",
					"Перемістити папку '" + f.getFolderName() + "' в " + targetFolderTitle(targetId) + "?", "Перемістити?")) {
				if (dbTree.moveFolder(f.getFolderId(), targetId)) {
					expandTreeItemByFolderId(targetId);
					dbTree.saveToFile();
					refreshTree();
					selectTreeItemById(f.getFolderId(), null);
				} else {
					ShowAppMsg.showAlert("WARNING", "Переміщення", "Неможливо перемістити папку в її вкладену папку", "");
				}
			}
		}
	}

	/**
	 * Перетягнуто з Ctrl — копіювання.
	 */
	private void dropCopy(TreeItem<Object> dragItem, TreeTableRow<Object> row) {
		Object v = dragItem.getValue();
		int targetId = targetFolderIdForRow(row);
		if (v instanceof DBConn_Parameters) {
			DBConn_Parameters np = dbTree.copyConnection(((DBConn_Parameters) v).getConnId(), targetId);
			if (np == null) {
				ShowAppMsg.showAlert("WARNING", "Копіювання", "Неможливо скопіювати з'єднання", "");
				return;
			}
			expandTreeItemByFolderId(targetId);
			dbTree.saveToFile();
			refreshTree();
			selectTreeItemById(null, np.getConnId());
		} else if (v instanceof DBConnFolder) {
			DBConnFolder np = dbTree.copyFolder(((DBConnFolder) v).getFolderId(), targetId);
			if (np == null) {
				ShowAppMsg.showAlert("WARNING", "Копіювання", "Неможливо скопіювати папку в її вкладену папку", "");
				return;
			}
			expandTreeItemByFolderId(targetId);
			dbTree.saveToFile();
			refreshTree();
			selectTreeItemById(np.getFolderId(), null);
		}
	}

	/**
	 * Назва папки-призначення для повідомлень.
	 */
	private String targetFolderTitle(int folderId) {
		if (folderId == DBConnTree_Parameters.ROOT_FOLDER_ID) {
			return "корінь «Підключення»";
		}
		DBConnFolder f = dbTree.findFolderById(folderId);
		return f == null ? "корінь" : "папку «" + f.getFolderName() + "»";
	}

	/**
	 * Розкрити папку в поточному дереві (щоб вставлене було видно).
	 */
	private void expandTreeItemByFolderId(int folderId) {
		if (folderId == DBConnTree_Parameters.ROOT_FOLDER_ID) {
			return;
		}
		TreeItem<Object> item = findTreeItemByFolderId(treeTableView_Connections.getRoot(), folderId);
		if (item != null) {
			item.setExpanded(true);
		}
	}

	/**
	 * Вибрати елемент за Id папки або з'єднання (один з Id — null).
	 */
	private boolean selectTreeItemById(Integer folderId, Integer connId) {
		return selectTreeItemById(treeTableView_Connections.getRoot(), folderId, connId);
	}

	private boolean selectTreeItemById(TreeItem<Object> item, Integer folderId, Integer connId) {
		if (item == null) {
			return false;
		}
		Object v = item.getValue();
		if (folderId != null && v instanceof DBConnFolder
				&& ((DBConnFolder) v).getFolderId() == folderId) {
			treeTableView_Connections.getSelectionModel().select(item);
			return true;
		}
		if (connId != null && v instanceof DBConn_Parameters
				&& ((DBConn_Parameters) v).getConnId() == connId) {
			treeTableView_Connections.getSelectionModel().select(item);
			return true;
		}
		for (TreeItem<Object> ch : item.getChildren()) {
			if (selectTreeItemById(ch, folderId, connId)) {
				return true;
			}
		}
		return false;
	}

	private TreeItem<Object> findTreeItemByFolderId(TreeItem<Object> item, int folderId) {
		if (item == null) {
			return null;
		}
		if (item.getValue() instanceof DBConnFolder
				&& ((DBConnFolder) item.getValue()).getFolderId() == folderId) {
			return item;
		}
		for (TreeItem<Object> ch : item.getChildren()) {
			TreeItem<Object> r = findTreeItemByFolderId(ch, folderId);
			if (r != null) {
				return r;
			}
		}
		return null;
	}

	/**
	 * Побудова дерева зі збереженням розкриття та вибору.
	 */
	private void buildTree() {
		Set<Integer> expanded = new HashSet<>();
		Object selectedValue = null;
		TreeItem<Object> curSel = treeTableView_Connections.getSelectionModel().getSelectedItem();
		if (curSel != null && curSel.getValue() != null) {
			selectedValue = curSel.getValue();
		}
		collectExpanded(treeTableView_Connections.getRoot(), expanded);

		TreeItem<Object> root = new TreeItem<>((Object) "Підключення");
		root.setExpanded(true);

		List<DBConnFolder> sortedFolders = new ArrayList<>(dbTree.folders);
		sortedFolders.sort(Comparator.comparing(f -> f.getFolderName() == null ? "" : f.getFolderName().toLowerCase()));
		for (DBConnFolder f : sortedFolders) {
			root.getChildren().add(createFolderItem(f));
		}
		List<DBConn_Parameters> sortedConns = new ArrayList<>(dbTree.rootConnections);
		sortedConns.sort(Comparator.comparing(c -> c.getConnName() == null ? "" : c.getConnName().toLowerCase()));
		for (DBConn_Parameters c : sortedConns) {
			root.getChildren().add(new TreeItem<>((Object) c));
		}

		treeTableView_Connections.setRoot(root);
		restoreExpanded(root, expanded, selectedValue);
		// за замовчуванням розкриваємо перший рівень
		if (expanded.isEmpty()) {
			for (TreeItem<Object> ch : root.getChildren()) {
				ch.setExpanded(true);
			}
		}
		restoreSort();
	}

	private TreeItem<Object> createFolderItem(DBConnFolder folder) {
		TreeItem<Object> item = new TreeItem<>((Object) folder);
		item.setExpanded(true);
		List<DBConnFolder> sf = new ArrayList<>(folder.getFolders());
		sf.sort(Comparator.comparing(f -> f.getFolderName() == null ? "" : f.getFolderName().toLowerCase()));
		for (DBConnFolder sub : sf) {
			item.getChildren().add(createFolderItem(sub));
		}
		List<DBConn_Parameters> sc = new ArrayList<>(folder.getConnections());
		sc.sort(Comparator.comparing(c -> c.getConnName() == null ? "" : c.getConnName().toLowerCase()));
		for (DBConn_Parameters c : sc) {
			item.getChildren().add(new TreeItem<>((Object) c));
		}
		return item;
	}

	private void collectExpanded(TreeItem<Object> item, Set<Integer> out) {
		if (item == null) {
			return;
		}
		if (item.getValue() instanceof DBConnFolder) {
			if (item.isExpanded()) {
				out.add(((DBConnFolder) item.getValue()).getFolderId());
			}
		}
		for (TreeItem<Object> ch : item.getChildren()) {
			collectExpanded(ch, out);
		}
	}

	private void restoreExpanded(TreeItem<Object> item, Set<Integer> expanded, Object selectedValue) {
		if (item.getValue() instanceof DBConnFolder) {
			int id = ((DBConnFolder) item.getValue()).getFolderId();
			if (expanded.contains(id)) {
				item.setExpanded(true);
			}
		}
		if (selectedValue != null) {
			if (selectedValue instanceof DBConnFolder && item.getValue() instanceof DBConnFolder) {
				if (((DBConnFolder) selectedValue).getFolderId() == ((DBConnFolder) item.getValue()).getFolderId()) {
					treeTableView_Connections.getSelectionModel().select(item);
				}
			} else if (selectedValue instanceof DBConn_Parameters && item.getValue() instanceof DBConn_Parameters) {
				if (((DBConn_Parameters) selectedValue).getConnId() == ((DBConn_Parameters) item.getValue()).getConnId()) {
					// знаходимо актуальний об'єкт з моделі
					DBConn_Parameters fresh = dbTree.findConnectionById(((DBConn_Parameters) selectedValue).getConnId());
					if (fresh != null) {
						item.setValue(fresh);
					}
					treeTableView_Connections.getSelectionModel().select(item);
				}
			} else if (selectedValue instanceof String && item.getValue() instanceof String) {
				if (selectedValue.equals(item.getValue())) {
					treeTableView_Connections.getSelectionModel().select(item);
				}
			}
		}
		for (TreeItem<Object> ch : item.getChildren()) {
			restoreExpanded(ch, expanded, selectedValue);
		}
	}

	private void refreshTree() {
		buildTree();
	}

	private DBConn_Parameters getSelectedConnection() {
		TreeItem<Object> sel = treeTableView_Connections.getSelectionModel().getSelectedItem();
		if (sel != null && sel.getValue() instanceof DBConn_Parameters) {
			return (DBConn_Parameters) sel.getValue();
		}
		return null;
	}

	private DBConnFolder getSelectedFolder() {
		TreeItem<Object> sel = treeTableView_Connections.getSelectionModel().getSelectedItem();
		if (sel != null && sel.getValue() instanceof DBConnFolder) {
			return (DBConnFolder) sel.getValue();
		}
		return null;
	}

	/**
	 * Папка-призначення для створення (папка вибору або батько з'єднання або корінь).
	 */
	private int getTargetFolderIdForAdd() {
		TreeItem<Object> sel = treeTableView_Connections.getSelectionModel().getSelectedItem();
		if (sel == null || sel.getValue() == null) {
			return DBConnTree_Parameters.ROOT_FOLDER_ID;
		}
		if (sel.getValue() instanceof DBConnFolder) {
			return ((DBConnFolder) sel.getValue()).getFolderId();
		}
		if (sel.getValue() instanceof DBConn_Parameters) {
			int cid = ((DBConn_Parameters) sel.getValue()).getConnId();
			DBConnFolder parent = dbTree.findParentFolderOfConnection(cid);
			return parent == null ? DBConnTree_Parameters.ROOT_FOLDER_ID : parent.getFolderId();
		}
		return DBConnTree_Parameters.ROOT_FOLDER_ID;
	}

	/**
	 * Зберігаємо поточний стан вікна.
	 */
	private void saveState() {
		if (prefs == null || dialogStage == null) {
			return;
		}
		prefs.putDouble("stageDBConnList_Width", dialogStage.getWidth());
		prefs.putDouble("stageDBConnList_Height", dialogStage.getHeight());
		prefs.putDouble("stageDBConnList_PosX", dialogStage.getX());
		prefs.putDouble("stageDBConnList_PosY", dialogStage.getY());

		//-------- sort
		if (treeTableView_Connections.getSortOrder().size() > 0) {
			currentSortColumn = (TreeTableColumn) treeTableView_Connections.getSortOrder().get(0);
			prefs.put("stageDBConnList_sortColumnId", currentSortColumn.getId());
			prefs.put("stageDBConnList_sortType", currentSortColumn.getSortType().toString());
		} else {
			prefs.remove("stageDBConnList_sortColumnId");
			prefs.remove("stageDBConnList_sortType");
		}
	}

	/**
	 * Діалог створення/редагування папки. Повертає {назва, опис} або null.
	 */
	private String[] showFolderDialog(String title, String initName, String initDescr) {
		Dialog<String[]> dialog = new Dialog<>();
		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(15, 15, 15, 15));

		TextField tfName = new TextField(initName == null ? "" : initName);
		tfName.setPromptText("Назва папки");
		TextField tfDescr = new TextField(initDescr == null ? "" : initDescr);
		tfDescr.setPromptText("Опис (не обов'язково)");

		grid.add(new Label("Назва:"), 0, 0);
		grid.add(tfName, 1, 0);
		grid.add(new Label("Опис:"), 0, 1);
		grid.add(tfDescr, 1, 1);

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(btn -> {
			if (btn == ButtonType.OK) {
				return new String[]{tfName.getText(), tfDescr.getText()};
			}
			return null;
		});

		Optional<String[]> res = dialog.showAndWait();
		return res.orElse(null);
	}

	/**
	 * Нова папка. Без вибору або на корені — папка першого рівня.
	 */
	@FXML
	private void handleButtonAddFolder() {
		int parentId = getTargetFolderIdForAdd();
		String[] res = showFolderDialog("Нова папка", "", "");
		if (res == null) {
			return;
		}
		String name = res[0] == null ? "" : res[0].trim();
		String descr = res[1] == null ? "" : res[1];
		if (name.isEmpty()) {
			ShowAppMsg.showAlert("WARNING", "Немає даних", "Не заповнено назву папки", "Вкажіть назву папки");
			return;
		}
		try {
			dbTree.addFolder(parentId, name, descr);
			dbTree.saveToFile();
			refreshTree();
		} catch (KBase_DublicateConnIdEx e) {
			ShowAppMsg.showAlert("WARNING", "Увага", "Дублювання Id", e.msg);
		}
	}

	/**
	 * Викликається при натисканні на кнопці "Додати".
	 */
	@FXML
	private void handleButtonAdd() {
		pendingParentFolderId = getTargetFolderIdForAdd();
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/DBConnEdit_Layout.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			Stage st = new Stage();
			st.setTitle("Додавання нового підключення");
			st.initModality(Modality.WINDOW_MODAL);
			st.initOwner(this.dialogStage);
			Scene scene = new Scene(page);
			st.setScene(scene);
			st.getIcons().setAll(params.getMainStage().getIcons());

			Preferences p = Preferences.userNodeForPackage(DBConnEdit_Controller.class);
			st.setX(p.getDouble("stageDBConnEdit_PosX", 0));
			st.setY(p.getDouble("stageDBConnEdit_PosY", 0));

			DBConnEdit_Controller controller = loader.getController();
			controller.setParrentObj(params, this, 1, null, st);

			st.showAndWait();

			refreshTree();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Викликається при натисканні на кнопці "Редагувати".
	 */
	@FXML
	private void handleButtonEdit() {
		DBConnFolder sf = getSelectedFolder();
		if (sf != null) {
			String[] res = showFolderDialog("Редагування папки", sf.getFolderName(), sf.getDescr());
			if (res == null) {
				return;
			}
			String name = res[0] == null ? "" : res[0].trim();
			if (name.isEmpty()) {
				ShowAppMsg.showAlert("WARNING", "Немає даних", "Не заповнено назву папки", "Вкажіть назву папки");
				return;
			}
			sf.setFolderName(name);
			sf.setDescr(res[1] == null ? "" : res[1]);
			dbTree.saveToFile();
			refreshTree();
			return;
		}

		DBConn_Parameters sel = getSelectedConnection();
		if (sel == null) {
			ShowAppMsg.showAlert("WARNING", "Немає вибору", "Не обрано з'єднання або папку", "Оберіть корінь, папку або з'єднання");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/DBConnEdit_Layout.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			Stage st = new Stage();
			st.setTitle("Редагування підключення");
			st.initModality(Modality.WINDOW_MODAL);
			st.initOwner(this.dialogStage);
			Scene scene = new Scene(page);
			st.setScene(scene);
			st.getIcons().setAll(params.getMainStage().getIcons());

			Preferences p = Preferences.userNodeForPackage(DBConnEdit_Controller.class);
			st.setX(p.getDouble("stageDBConnEdit_PosX", 0));
			st.setY(p.getDouble("stageDBConnEdit_PosY", 0));

			DBConnEdit_Controller controller = loader.getController();
			controller.setParrentObj(params, this, 2, sel, st);

			st.showAndWait();

			refreshTree();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Викликається при натисканні на кнопці "Видалити".
	 */
	@FXML
	private void handleButtonDelete() {
		DBConnFolder sf = getSelectedFolder();
		if (sf != null) {
			// забороняємо видалення папки з відкритими підключеннями
			List<DBConn_Parameters> inside = new ArrayList<>();
			collectFolderConnections(sf, inside);
			for (DBConn_Parameters c : inside) {
				if (isConnectionActive(c.getConnId())) {
					ShowAppMsg.showAlert("WARNING", "Видалення", "Папка містить відкрите з'єднання '" + c.getConnName() + "'",
							"Спочатку від'єднайтесь");
					return;
				}
			}
			if (ShowAppMsg.showQuestion("CONFIRMATION", "Видалення папки",
					"Видалення папки '" + sf.getFolderName() + "' з усім вмістом", "Видалити папку?")) {
				dbTree.deleteFolder(sf.getFolderId());
				dbTree.saveToFile();
				refreshTree();
			}
			return;
		}

		DBConn_Parameters sel = getSelectedConnection();
		if (sel == null) {
			ShowAppMsg.showAlert("WARNING", "Немає вибору", "Не обрано з'єднання", "Оберіть з'єднання для видалення");
			return;
		}
		if (isConnectionActive(sel.getConnId())) {
			ShowAppMsg.showAlert("WARNING", "Видалення", "З'єднання '" + sel.getConnName() + "' зараз відкрите",
					"Спочатку від'єднайтесь");
			return;
		}

		String connName = sel.getConnName();
		if (ShowAppMsg.showQuestion("CONFIRMATION", "Видалення з'єднання",
				"Видалення з'єднання '" + connName + "'", "Видалити з'єднання?")) {
			dbTree.deleteConnection(sel.getConnId());
			dbTree.saveToFile();
			refreshTree();
		}
	}

	/**
	 * Вирізати вибране (папку або з'єднання) в буфер обміну для переміщення.
	 */
	@FXML
	private void handleMenuCut() {
		DBConnFolder sf = getSelectedFolder();
		if (sf != null) {
			clipboardFolderId = sf.getFolderId();
			clipboardConnId = null;
			clipboardIsCut = true;
			updateClipboardMenuState();
			params.setMsgToStatusBar("Вирізано папку '" + sf.getFolderName() + "'. Оберіть місце та натисніть Вставити.");
			return;
		}
		DBConn_Parameters sel = getSelectedConnection();
		if (sel == null) {
			ShowAppMsg.showAlert("WARNING", "Немає вибору", "Не обрано папку або з'єднання", "Оберіть елемент для вирізання");
			return;
		}
		clipboardFolderId = null;
		clipboardConnId = sel.getConnId();
		clipboardIsCut = true;
		updateClipboardMenuState();
		params.setMsgToStatusBar("Вирізано з'єднання '" + sel.getConnName() + "'. Оберіть місце та натисніть Вставити.");
	}

	/**
	 * Копіювати вибране (папку або з'єднання) в буфер обміну.
	 */
	@FXML
	private void handleMenuCopy() {
		DBConnFolder sf = getSelectedFolder();
		if (sf != null) {
			clipboardFolderId = sf.getFolderId();
			clipboardConnId = null;
			clipboardIsCut = false;
			updateClipboardMenuState();
			params.setMsgToStatusBar("Скопійовано папку '" + sf.getFolderName() + "'. Оберіть місце та натисніть Вставити.");
			return;
		}
		DBConn_Parameters sel = getSelectedConnection();
		if (sel == null) {
			ShowAppMsg.showAlert("WARNING", "Немає вибору", "Не обрано папку або з'єднання", "Оберіть елемент для копіювання");
			return;
		}
		clipboardFolderId = null;
		clipboardConnId = sel.getConnId();
		clipboardIsCut = false;
		updateClipboardMenuState();
		params.setMsgToStatusBar("Скопійовано з'єднання '" + sel.getConnName() + "'. Оберіть місце та натисніть Вставити.");
	}

	/**
	 * Вставити з буфера обміну у вибрану папку (або корінь).
	 */
	@FXML
	private void handleMenuPaste() {
		if (clipboardFolderId == null && clipboardConnId == null) {
			ShowAppMsg.showAlert("WARNING", "Буфер обміну", "Буфер порожній", "Спочатку виріжте або скопіюйте елемент");
			return;
		}
		int targetId = getTargetFolderIdForAdd();
		if (clipboardIsCut) {
			if (clipboardFolderId != null) {
				if (targetId == clipboardFolderId) {
					ShowAppMsg.showAlert("INFORMATION", "Вставлення", "Папка вже знаходиться тут", "");
					return;
				}
				if (dbTree.moveFolder(clipboardFolderId, targetId)) {
					int movedId = clipboardFolderId;
					clipboardFolderId = null;
					expandTreeItemByFolderId(targetId);
					dbTree.saveToFile();
					refreshTree();
					selectTreeItemById(movedId, null);
					updateClipboardMenuState();
				} else {
					ShowAppMsg.showAlert("WARNING", "Переміщення",
							"Неможливо перемістити папку (можливо, її видалено або ціль — її вкладена папка)", "");
				}
			} else {
				if (dbTree.moveConnection(clipboardConnId, targetId)) {
					int movedId = clipboardConnId;
					clipboardConnId = null;
					expandTreeItemByFolderId(targetId);
					dbTree.saveToFile();
					refreshTree();
					selectTreeItemById(null, movedId);
					updateClipboardMenuState();
				} else {
					ShowAppMsg.showAlert("WARNING", "Переміщення",
							"Неможливо перемістити з'єднання (можливо, його видалено)", "");
				}
			}
		} else {
			if (clipboardFolderId != null) {
				DBConnFolder np = dbTree.copyFolder(clipboardFolderId, targetId);
				if (np == null) {
					ShowAppMsg.showAlert("WARNING", "Копіювання",
							"Неможливо скопіювати папку (можливо, її видалено або ціль — її вкладена папка)", "");
					return;
				}
				expandTreeItemByFolderId(targetId);
				dbTree.saveToFile();
				refreshTree();
				selectTreeItemById(np.getFolderId(), null);
			} else {
				DBConn_Parameters np = dbTree.copyConnection(clipboardConnId, targetId);
				if (np == null) {
					ShowAppMsg.showAlert("WARNING", "Копіювання",
							"Неможливо скопіювати з'єднання (можливо, його видалено)", "");
					return;
				}
				expandTreeItemByFolderId(targetId);
				dbTree.saveToFile();
				refreshTree();
				selectTreeItemById(null, np.getConnId());
			}
		}
	}

	/**
	 * Дублювати вибране в тій же папці.
	 */
	@FXML
	private void handleMenuDuplicate() {
		DBConnFolder sf = getSelectedFolder();
		if (sf != null) {
			DBConnFolder parent = dbTree.findParentFolderOfFolder(sf.getFolderId());
			int parentId = parent == null ? DBConnTree_Parameters.ROOT_FOLDER_ID : parent.getFolderId();
			DBConnFolder np = dbTree.copyFolder(sf.getFolderId(), parentId);
			if (np == null) {
				ShowAppMsg.showAlert("WARNING", "Дублювання", "Неможливо продублювати папку", "");
				return;
			}
			dbTree.saveToFile();
			refreshTree();
			selectTreeItemById(np.getFolderId(), null);
			return;
		}
		DBConn_Parameters sel = getSelectedConnection();
		if (sel == null) {
			ShowAppMsg.showAlert("WARNING", "Немає вибору", "Не обрано папку або з'єднання", "Оберіть елемент для дублювання");
			return;
		}
		DBConnFolder parent = dbTree.findParentFolderOfConnection(sel.getConnId());
		int parentId = parent == null ? DBConnTree_Parameters.ROOT_FOLDER_ID : parent.getFolderId();
		DBConn_Parameters np = dbTree.copyConnection(sel.getConnId(), parentId);
		if (np == null) {
			ShowAppMsg.showAlert("WARNING", "Дублювання", "Неможливо продублювати з'єднання", "");
			return;
		}
		dbTree.saveToFile();
		refreshTree();
		selectTreeItemById(null, np.getConnId());
	}

	/**
	 * Стан пунктів буфера обміну: Вставити — лише коли буфер не порожній.
	 */
	private void updateClipboardMenuState() {
		boolean hasSel = getSelectedFolder() != null || getSelectedConnection() != null;
		menuitem_Cut.setDisable(!hasSel);
		menuitem_Copy.setDisable(!hasSel);
		menuitem_Duplicate.setDisable(!hasSel);
		menuitem_Paste.setDisable(clipboardFolderId == null && clipboardConnId == null);
	}

	private void collectFolderConnections(DBConnFolder folder, List<DBConn_Parameters> out) {
		out.addAll(folder.getConnections());
		for (DBConnFolder sub : folder.getFolders()) {
			collectFolderConnections(sub, out);
		}
	}

	/**
	 * Викликається при натисканні на кнопці "Під'єднати".
	 */
	@FXML
	private void handleButtonConnect() {
		DBConn_Parameters selectedConn = getSelectedConnection();

		saveState();

		if (selectedConn == null) {
			ShowAppMsg.showAlert("WARNING", "Немає вибору", "Не обрано з'єднання", "Оберіть з'єднання.");
			return;
		}

		mainApp.dbConnect(new DBConn_Parameters(selectedConn));

		// міняємо дату останнього підключення та збільшуємо лічильник
		selectedConn.setLastConn(LocalDate.now());
		selectedConn.setCounter(selectedConn.getCounter() + 1);

		dbTree.saveToFile();
		refreshTree();
	}

	/**
	 * Викликається кнопкою або контекстним меню "Від'єднати".
	 */
	@FXML
	private void handleButtonDisconnect() {
		DBConn_Parameters selectedConn = getSelectedConnection();

		saveState();

		if (selectedConn == null) {
			ShowAppMsg.showAlert("WARNING", "Немає вибору", "Не обрано з'єднання", "Оберіть з'єднання.");
			return;
		}

		boolean isConnectOpen = false;
		DBConCur_Parameters toClose = null;
		for (DBConCur_Parameters con : params.getConnDB().conList) {
			if (con.param.getConnId() == selectedConn.getConnId()) {
				isConnectOpen = true;
				toClose = con;
				break;
			}
		}
		if (!isConnectOpen || toClose == null) {
			ShowAppMsg.showAlert("WARNING", "З'єднання", "З'єднання не відкрите", "");
			return;
		}
		// коректно закриваємо та прибираємо зі списку відкритих
		params.getConnDB().delete(toClose.Id);
		params.getRootController().createConnectionIndicators();
		refreshTree();
	}

	/**
	 * Викликається при натисканні на кнопці "Закрити".
	 */
	@FXML
	private void handleButtonExit() {
		saveState();
		Stage stage = (Stage) button_Exit.getScene().getWindow();
		stage.close();
	}

	/**
	 * Редагування запису: нове — додаємо у папку pendingParentFolderId, існуюче — змінюємо.
	 */
	public void edit(int actionType, DBConn_Parameters newConnPar) {
		DBConn_Parameters curConnPar;

		if (actionType == 1) {
			try {
				dbTree.add(pendingParentFolderId,
						newConnPar.getConnName(), newConnPar.getDescr(),
						newConnPar.getType(), newConnPar.getHost(), newConnPar.getPort(),
						newConnPar.getName(), newConnPar.getLogin(), newConnPar.getPassword(),
						newConnPar.getAutoConn(), null, 0, newConnPar.getColorEnable(),
						newConnPar.getColorTRed_A(), newConnPar.getColorTGreen_A(), newConnPar.getColorTBlue_A(), newConnPar.getColorTOpacity_A(),
						newConnPar.getColorBRed_A(), newConnPar.getColorBGreen_A(), newConnPar.getColorBBlue_A(), newConnPar.getColorBOpacity_A(),
						newConnPar.getColorTRed_N(), newConnPar.getColorTGreen_N(), newConnPar.getColorTBlue_N(), newConnPar.getColorTOpacity_N(),
						newConnPar.getColorBRed_N(), newConnPar.getColorBGreen_N(), newConnPar.getColorBBlue_N(), newConnPar.getColorBOpacity_N());
			} catch (KBase_DublicateConnIdEx e) {
				ShowAppMsg.showAlert("WARNING", "Увага", "Дублювання Id з'єднання", e.msg);
			}
		} else if (actionType == 2) {
			curConnPar = getSelectedConnection();

			if (curConnPar == null) {
				ShowAppMsg.showAlert("WARNING", "Увага", "Не обрано запис у дереві.", "Зміна запису неможлива.");
				return;
			}
			if (curConnPar.getConnId() != newConnPar.getConnId()) {
				ShowAppMsg.showAlert("WARNING", "Увага", "Змінився поточний запис у дереві.", "Зміна запису неможлива.");
				return;
			}

			curConnPar.setConnName(newConnPar.getConnName());
			curConnPar.setDescr(newConnPar.getDescr() == null ? "" : newConnPar.getDescr());
			curConnPar.setType(newConnPar.getType());
			curConnPar.setHost(newConnPar.getHost());
			curConnPar.setPort(newConnPar.getPort());
			curConnPar.setName(newConnPar.getName());
			curConnPar.setLogin(newConnPar.getLogin());
			curConnPar.setPassword(newConnPar.getPassword());
			curConnPar.setAutoConn(newConnPar.getAutoConn());

			curConnPar.setColorEnable(newConnPar.getColorEnable());

			curConnPar.setColorTRed_A(newConnPar.getColorTRed_A());
			curConnPar.setColorTGreen_A(newConnPar.getColorTGreen_A());
			curConnPar.setColorTBlue_A(newConnPar.getColorTBlue_A());
			curConnPar.setColorTOpacity_A(newConnPar.getColorTOpacity_A());

			curConnPar.setColorBRed_A(newConnPar.getColorBRed_A());
			curConnPar.setColorBGreen_A(newConnPar.getColorBGreen_A());
			curConnPar.setColorBBlue_A(newConnPar.getColorBBlue_A());
			curConnPar.setColorBOpacity_A(newConnPar.getColorBOpacity_A());

			curConnPar.setColorTRed_N(newConnPar.getColorTRed_N());
			curConnPar.setColorTGreen_N(newConnPar.getColorTGreen_N());
			curConnPar.setColorTBlue_N(newConnPar.getColorTBlue_N());
			curConnPar.setColorTOpacity_N(newConnPar.getColorTOpacity_N());

			curConnPar.setColorBRed_N(newConnPar.getColorBRed_N());
			curConnPar.setColorBGreen_N(newConnPar.getColorBGreen_N());
			curConnPar.setColorBBlue_N(newConnPar.getColorBBlue_N());
			curConnPar.setColorBOpacity_N(newConnPar.getColorBOpacity_N());
		}

		dbTree.saveToFile();
		refreshTree();
	}

	/**
	 * Для сумісності: повертає плоский список усіх підключень.
	 */
	public List<DBConn_Parameters> getFlatConnections() {
		return dbTree.getAllConnections();
	}
}
