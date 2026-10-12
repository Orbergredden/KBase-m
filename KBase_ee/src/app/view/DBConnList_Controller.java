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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Контролер вікна дерева підключень до БД.
 * Папки з необмеженою вкладеністю, підключення всередині.
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
	private TreeView<Object> treeView_Connections;

	@FXML
	private MenuItem menuitem_AddFolder;
	@FXML
	private MenuItem menuitem_Add;
	@FXML
	private MenuItem menuitem_Edit;
	@FXML
	private MenuItem menuitem_Delete;
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

	/**
	 * Папка-призначення для нового підключення (встановлюється перед діалогом додавання)
	 */
	private int pendingParentFolderId = DBConnTree_Parameters.ROOT_FOLDER_ID;

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

		treeView_Connections.setShowRoot(false);
		treeView_Connections.setCellFactory(tv -> new TreeCell<Object>() {
			@Override
			protected void updateItem(Object item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
					setStyle("");
					setTooltip(null);
					return;
				}
				if (item instanceof DBConnFolder) {
					DBConnFolder f = (DBConnFolder) item;
					int cnt = f.getFolders().size() + f.getConnections().size();
					setText(f.getFolderName() + (cnt > 0 ? " (" + cnt + ")" : ""));
					setGraphic(new ImageView(folderIcon));
					setStyle("");
					String tip = f.getDescr() != null && !f.getDescr().isEmpty() ? f.getDescr() : "Папка";
					setTooltip(new Tooltip(tip));
				} else if (item instanceof DBConn_Parameters) {
					DBConn_Parameters c = (DBConn_Parameters) item;
					boolean active = isConnectionActive(c.getConnId());
					setText(c.getConnName() + (active ? "  ●" : ""));
					setGraphic(new ImageView(getTypeIcon(c.getType())));
					if (c.getColorEnable()) {
						try {
							Color tc;
							Color bc;
							if (active) {
								tc = new Color(c.getColorTRed_A(), c.getColorTGreen_A(), c.getColorTBlue_A(), c.getColorTOpacity_A());
								bc = new Color(c.getColorBRed_A(), c.getColorBGreen_A(), c.getColorBBlue_A(), c.getColorBOpacity_A());
							} else {
								tc = new Color(c.getColorTRed_N(), c.getColorTGreen_N(), c.getColorTBlue_N(), c.getColorTOpacity_N());
								bc = new Color(c.getColorBRed_N(), c.getColorBGreen_N(), c.getColorBBlue_N(), c.getColorBOpacity_N());
							}
							String st = "-fx-text-fill: #" + ConvertType.colorToHex(tc) + "; "
									+ "-fx-background-color: #" + ConvertType.colorToHex(bc) + ";"
									+ (active ? " -fx-font-weight: bold;" : "");
							setStyle(st);
						} catch (Exception e) {
							setStyle(active ? "-fx-font-weight: bold;" : "");
						}
					} else {
						setStyle(active ? "-fx-font-weight: bold;" : "");
					}
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
					if (active) {
						sb.append("\nАктивне");
					}
					setTooltip(new Tooltip(sb.toString()));
				} else {
					setText(item.toString());
					setGraphic(null);
					setStyle("");
					setTooltip(null);
				}
			}
		});

		buildTree();

		treeView_Connections.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				TreeItem<Object> sel = treeView_Connections.getSelectionModel().getSelectedItem();
				if (sel == null || sel.getValue() == null) {
					return;
				}
				if (sel.getValue() instanceof DBConn_Parameters) {
					handleButtonConnect();
				} else if (sel.getValue() instanceof DBConnFolder) {
					sel.setExpanded(!sel.isExpanded());
				}
			}
		});

		// ContextMenu
		menuitem_AddFolder.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
		menuitem_Add.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
		menuitem_Edit.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_16.png")));
		menuitem_Delete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
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
	 * Чи підключення зараз активне (відкрите).
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
	 * Побудова дерева зі збереженням розкриття та вибору.
	 */
	private void buildTree() {
		Set<Integer> expanded = new HashSet<>();
		Object selectedValue = null;
		TreeItem<Object> curSel = treeView_Connections.getSelectionModel().getSelectedItem();
		if (curSel != null && curSel.getValue() != null) {
			selectedValue = curSel.getValue();
		}
		collectExpanded(treeView_Connections.getRoot(), expanded);

		TreeItem<Object> root = new TreeItem<>((Object) "ROOT");
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

		treeView_Connections.setRoot(root);
		restoreExpanded(root, expanded, selectedValue);
		// за замовчуванням розкриваємо перший рівень
		if (expanded.isEmpty()) {
			for (TreeItem<Object> ch : root.getChildren()) {
				ch.setExpanded(true);
			}
		}
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
					treeView_Connections.getSelectionModel().select(item);
				}
			} else if (selectedValue instanceof DBConn_Parameters && item.getValue() instanceof DBConn_Parameters) {
				if (((DBConn_Parameters) selectedValue).getConnId() == ((DBConn_Parameters) item.getValue()).getConnId()) {
					// знаходимо актуальний об'єкт з моделі
					DBConn_Parameters fresh = dbTree.findConnectionById(((DBConn_Parameters) selectedValue).getConnId());
					if (fresh != null) {
						item.setValue(fresh);
					}
					treeView_Connections.getSelectionModel().select(item);
				}
			}
		}
		for (TreeItem<Object> ch : item.getChildren()) {
			restoreExpanded(ch, expanded, selectedValue);
		}
	}

	private void refreshTree() {
		buildTree();
		treeView_Connections.refresh();
	}

	private DBConn_Parameters getSelectedConnection() {
		TreeItem<Object> sel = treeView_Connections.getSelectionModel().getSelectedItem();
		if (sel != null && sel.getValue() instanceof DBConn_Parameters) {
			return (DBConn_Parameters) sel.getValue();
		}
		return null;
	}

	private DBConnFolder getSelectedFolder() {
		TreeItem<Object> sel = treeView_Connections.getSelectionModel().getSelectedItem();
		if (sel != null && sel.getValue() instanceof DBConnFolder) {
			return (DBConnFolder) sel.getValue();
		}
		return null;
	}

	/**
	 * Папка-призначення для створення (папка вибору або батько з'єднання або корінь).
	 */
	private int getTargetFolderIdForAdd() {
		TreeItem<Object> sel = treeView_Connections.getSelectionModel().getSelectedItem();
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
	 * Нова папка.
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
			ShowAppMsg.showAlert("WARNING", "Немає вибору", "Не обрано з'єднання або папку", "Оберіть папку або з'єднання для редагування");
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
			// забороняємо видалення папки з активними підключеннями
			List<DBConn_Parameters> inside = new ArrayList<>();
			collectFolderConnections(sf, inside);
			for (DBConn_Parameters c : inside) {
				if (isConnectionActive(c.getConnId())) {
					ShowAppMsg.showAlert("WARNING", "Видалення", "Папка містить активне з'єднання '" + c.getConnName() + "'",
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
			ShowAppMsg.showAlert("WARNING", "Видалення", "З'єднання '" + sel.getConnName() + "' зараз активне",
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
		// коректно закриваємо та прибираємо зі списку активних
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
