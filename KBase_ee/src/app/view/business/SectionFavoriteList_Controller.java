package app.view.business;

import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import app.exceptions.DataConnectionException;
import app.exceptions.DataQueryException;
import app.lib.ShowAppMsg;
import app.model.AppItem_Interface;
import app.model.Params;
import app.model.StateItem;
import app.model.StateList;
import app.model.business.DictionaryItem;
import app.model.business.SectionFavoriteItem;
import app.model.business.SectionItem;
import app.util.FormattedDate;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeSortMode;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Контролер фрейма показу дерева Фаворитів Розділів.
 */
public class SectionFavoriteList_Controller implements AppItem_Interface {
	private Params params;
	private SectionList_Controller controller_Section;
	private Preferences prefs = Preferences.userNodeForPackage(SectionFavoriteList_Controller.class);
	
	@FXML
	public TreeTableView<SectionFavoriteItem> treeTableView_favorite;
	@FXML
	private TreeTableColumn<SectionFavoriteItem, String> treeTableColumnFavorite_name;
	@FXML
	private TreeTableColumn<SectionFavoriteItem, Long> treeTableColumnFavorite_id;
	@FXML
	private TreeTableColumn<SectionFavoriteItem, Long> treeTableColumnFavorite_sectionId;
	@FXML
	private TreeTableColumn<SectionFavoriteItem, FormattedDate> treeTableColumnFavorite_dateCreated;

	@FXML
	private MenuItem menuitemFavorite_gotoSection;
	@FXML
	private MenuItem menuitemFavorite_removeFromFavorite;

	// for sort options in the Tree
	private String sortColumnId;
	private String sortType;

	/**
	 * Конструктор.
	 * Конструктор вызывается раньше метода initialize().
	 */
	public SectionFavoriteList_Controller () {

	}

	/**
	 * Инициализация класса-контроллера. Этот метод вызывается автоматически
	 * после того, как fxml-файл будет загружен.
	 */
	@FXML
	private void initialize() {
	}

	/**
	 * Вызывается родительским объектом, который передает параметры.
	 * Инициализирует контролы на слое.
	 */
	public void setParams(Params params) {
		this.params = params;
		controller_Section = (SectionList_Controller) params.getParentObj();

		// init controls
		initControlsValue();
	}

	/**
	 * Инициализирует контролы значениями
	 */
	private void initControlsValue() {
		//======== context menu
		menuitemFavorite_gotoSection.setGraphic(new ImageView(new Image("file:resources/images/icon_ContentTree_16.png")));
		menuitemFavorite_removeFromFavorite.setGraphic(new ImageView(new Image("file:resources/images/icon_favorite_no_16.png")));

		//======== TreeTableView
		//-------- columns
		// setCellValueFactory
		treeTableColumnFavorite_name.setCellValueFactory(
				(TreeTableColumn.CellDataFeatures<SectionFavoriteItem, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getValue().getSection().getName())
		);
		treeTableColumnFavorite_id.setCellValueFactory(
				(TreeTableColumn.CellDataFeatures<SectionFavoriteItem, Long> param) ->
				new ReadOnlyObjectWrapper<>(param.getValue().getValue().getId())
		);
		treeTableColumnFavorite_sectionId.setCellValueFactory(
				(TreeTableColumn.CellDataFeatures<SectionFavoriteItem, Long> param) ->
				new ReadOnlyObjectWrapper<>(param.getValue().getValue().getSectionId())
		);
		treeTableColumnFavorite_dateCreated.setCellValueFactory(
				(TreeTableColumn.CellDataFeatures<SectionFavoriteItem, FormattedDate> param) -> {
					Date dateCreated = param.getValue().getValue().getDateCreated();
					FormattedDate formattedDate = new FormattedDate(dateCreated);
					return new ReadOnlyObjectWrapper<>(formattedDate);
				}
		);

		// set/get Pref Width
		treeTableColumnFavorite_name.setPrefWidth(prefs.getDouble("SectionFavoriteList__treeTableColumnFavorite_name__PrefWidth", 50));
		treeTableColumnFavorite_name.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				prefs.putDouble("SectionFavoriteList__treeTableColumnFavorite_name__PrefWidth", t1.doubleValue());
			}
		});
		treeTableColumnFavorite_id.setPrefWidth(prefs.getDouble("SectionFavoriteList__treeTableColumnFavorite_id__PrefWidth", 50));
		treeTableColumnFavorite_id.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				prefs.putDouble("SectionFavoriteList__treeTableColumnFavorite_id__PrefWidth", t1.doubleValue());
			}
		});
		treeTableColumnFavorite_sectionId.setPrefWidth(prefs.getDouble("SectionFavoriteList__treeTableColumnFavorite_sectionId__PrefWidth", 50));
		treeTableColumnFavorite_sectionId.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				prefs.putDouble("SectionFavoriteList__treeTableColumnFavorite_sectionId__PrefWidth", t1.doubleValue());
			}
		});
		treeTableColumnFavorite_dateCreated.setPrefWidth(prefs.getDouble("SectionFavoriteList__treeTableColumnFavorite_dateCreated__PrefWidth", 50));
		treeTableColumnFavorite_dateCreated.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				prefs.putDouble("SectionFavoriteList__treeTableColumnFavorite_dateCreated__PrefWidth", t1.doubleValue());
			}
		});

		//-------- init
		TreeItem<SectionFavoriteItem> root = new TreeItem<>(new SectionFavoriteItem(
				0, 0, 0, controller_Section.treeViewCtrl.root.getValue(), new Date()));
		treeTableView_favorite.setShowRoot(true);
		treeTableView_favorite.setRoot(root);
		root.setExpanded(false);

		initTreeItemsRecursive(root);
	
		//-------- CellFactory - показ іконок
		treeTableColumnFavorite_name.setCellFactory(ttc -> new TreeTableCell<SectionFavoriteItem, String>() {
			private SectionFavoriteItem row;
			private ImageView graphic;

			@Override
			protected void updateItem(String item, boolean empty) {	// display graphic
				try {
					row = getTreeTableRow().getItem();
					if ((row.getSection().getIconId() > 0) || (row.getSection().getId() <= 0)) {
						graphic = new ImageView(row.getSection().icon);
					} else {	// show default icon
						long iconIdDef = params.getConCur().db.sectionGetIconIdDefault(row.getParentId(), true);
						graphic = new ImageView(params.getConCur().db.iconGetImageById(iconIdDef));
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

		//-------- write sort
		treeTableView_favorite.getSortOrder().addListener((ListChangeListener<TreeTableColumn>) change -> {
			if (!change.getList().isEmpty()) {
				TreeTableColumn sortedColumn = change.getList().get(0);
				prefs.put("SectionFavoriteList_sortColumnId", sortedColumn.getId());
				prefs.put("SectionFavoriteList_sortType", sortedColumn.getSortType().toString()); // "ASCENDING" або "DESCENDING"
			}
		});

		for (TreeTableColumn<SectionFavoriteItem, ?> column : treeTableView_favorite.getColumns()) {
			column.sortTypeProperty().addListener((obs, oldVal, newVal) -> {
				if (treeTableView_favorite.getSortOrder().contains(column)) {
					prefs.put("SectionFavoriteList_sortColumnId", column.getId());
					prefs.put("SectionFavoriteList_sortType", newVal.toString());
				}
			});
		}
		
		//------- double click
		treeTableView_favorite.setRowFactory(tv -> {
			TreeTableRow<SectionFavoriteItem> row = new TreeTableRow<>();

			row.setOnMouseClicked(event -> {
				if (!row.isEmpty() && event.getClickCount() == 2) {
					//TreeItem<SectionFavoriteItem> item = row.getTreeItem();
					handleButtonFavorite_gotoSection();
				}
			});

			return row;
		});
	}

	/**
	 * Ініціалізація TreeTableView. Рекурсія по дереву.
	 */
	private void initTreeItemsRecursive(TreeItem<SectionFavoriteItem> ti) {
		SectionFavoriteItem f = ti.getValue();

		if (f != null) {
			List<SectionFavoriteItem> sectionsList = params.getConCur().db.sectionFavoriteListByParentId (f.getId());

			for (SectionFavoriteItem i : sectionsList) {
				TreeItem<SectionFavoriteItem> subItem = new TreeItem<>(i);
				ti.getChildren().add(subItem);

				initTreeItemsRecursive(subItem);
			}
		}
	}

	/**
	 * Переходимо до вибраного Розділу
	 */
	@FXML
	private void handleButtonFavorite_gotoSection() {
		TreeItem<SectionFavoriteItem> curSelected = treeTableView_favorite.getSelectionModel().getSelectedItem();

		if (curSelected != null) {
			controller_Section.treeViewCtrl.gotoSection(curSelected.getValue().getSectionId());
			controller_Section.tabPane_Sections.getSelectionModel().select(controller_Section.tab_ContentTree);
		}
	}

	/**
	 * Вилучаємо Розділ з Фаворитів
	 */
	@FXML
	private void handleButtonFavorite_removeFromFavorite() {
		TreeItem<SectionFavoriteItem> curSelected = treeTableView_favorite.getSelectionModel().getSelectedItem();

		if (curSelected == null) {
			params.setMsgToStatusBar("Нічого не обрано для видалення.");
			return;
		}

		SectionFavoriteItem sfi = curSelected.getValue();
		boolean isDeleteWithSubSections = prefs.get("isDeleteWithSubSections", "No").equals("Yes");
		int choice = ShowAppMsg.showQuestionWithOption(
				"CONFIRMATION", "Вилучення розділу",
				"Вилучити розділ '" + sfi.getSection().getName() + "' ?", null,
				"Вилучити гілку повністю", isDeleteWithSubSections);

		if ((choice != ShowAppMsg.QUESTION_OK) && (choice != ShowAppMsg.QUESTION_OK_WITH_OPTION)) {
			return;
		}

		boolean withSubTree = (choice == ShowAppMsg.QUESTION_OK) ? false : true;

		try {
			params.getConCur().db.sectionFavoriteDelete(sfi.getId(), withSubTree);
			params.getConCur().db.sectionFavoriteRebuildParentId();
			refreshFavoriteTree();
		} catch (DataConnectionException | DataQueryException e) {
			e.writeLog(params);
			ShowAppMsg.showAlert(
					"ERROR", "Помилка при вилученні Розділа з Favorite",
					Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
		}
	}

	/**
	 * Перезавантажуємо дерево Фавортів
	 */
	@FXML
	void refreshFavoriteTree() {
		StateList stateListTree = new StateList();
		TreeItem<SectionFavoriteItem> mainItem = treeTableView_favorite.getRoot();

		//==== save state
		try {
			stateListTree.add(
					"TreeItemSelected",
					Long.toString(treeTableView_favorite.getSelectionModel().getSelectedItem().getValue().getId()),	// section id in DB,
					null);
		} catch (NullPointerException ex) {  }

		addTreeItemStateRecursive(stateListTree, mainItem);

		stateListTree.add(
				"TreeItemsDoExpandAndSelected",
				"",
				null);

		//==== удаляем ветку (без текущего итема, только дочерние)
		mainItem.getChildren().clear();

		//==== создаем ветку заново
		initTreeItemsRecursive(mainItem);

		//==== восстанавливаем состояние ветки
		ObservableList<Long> listItemsForExpand = FXCollections.observableArrayList();
		Long selectedItemId = new Long(0);

		for (StateItem si : stateListTree.list) {
			switch (si.getName()) {
				case "TreeItemSelected" :
					selectedItemId = new Long(si.getParams());
					break;
				case "TreeItemExpanded":
					listItemsForExpand.add(new Long(si.getParams()));
					break;
				case "TreeItemsDoExpandAndSelected" :
					restoreTreeItemStateRecursive(listItemsForExpand,mainItem);
					treeTableView_favorite.sort();
					restoreTreeItemSelectedRecursive(selectedItemId,mainItem);
					treeTableView_favorite.sort();
					break;
			}
		}
	}
	
	/**
	 * уникальный ИД объекта
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
		return AppItem_Interface.ELEMENT_SECTION_FAVORITE;
	}

	/**
	 * Повертає параметри объекта интерфейса
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
		ShowAppMsg.showAlert (
			"INFORMATION",
			"Повідомлення",
			"",
			"Цю вкладку неможна закрити.");
		return;
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.                <br>
	 * Сохраняем состояние контролов в иерархической структуре
	 */
	public void saveControlsState (StateList stateList) {
		
		//-------- treeTableView_favorite
		String sortColumnId;
		String sortType;
		
		if (treeTableView_favorite.getSortOrder().size() > 0) {        // при сортировке по нескольким столбцам поменять if на for
			TreeTableColumn currentSortColumn = (TreeTableColumn) treeTableView_favorite.getSortOrder().get(0);
			
			sortColumnId = currentSortColumn.getId();
			sortType = currentSortColumn.getSortType().toString();
		} else {
			sortColumnId = "";
			sortType = "";
		}
		stateList.add("TreeTable_sortColumnId", sortColumnId, null);
		stateList.add("TreeTable_sortType", sortType, null);
		stateList.add("TreeTable_doSort", "", null);
		
		try {
			stateList.add(
					"TreeItemSelected",
					Long.toString(treeTableView_favorite.getSelectionModel().getSelectedItem().getValue().getId()),        // id in DB,
					null);
		} catch (NullPointerException ex) {    }
		addTreeItemStateRecursive(stateList,treeTableView_favorite.getRoot());
		stateList.add(
				"TreeItemsDoExpandAndSelected",
				"",
				null);

	}
	
	/**
	 * рекурсивное сохранение развернутых разделов из дерева разделов
	 */
	private void addTreeItemStateRecursive(StateList stateList, TreeItem<SectionFavoriteItem> ti) {

		//------- проверяем и записываем развернутость итема
		if (ti.isExpanded()) {
			stateList.add(
					"TreeItemExpanded",
					Long.toString(ti.getValue().getId()),		// id in DB
					null);
		}
		//------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<SectionFavoriteItem> i : ti.getChildren()) {
			addTreeItemStateRecursive(stateList, i);
		}
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Востанавливаем состояние контролов из иерархической структуры
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
					treeTableView_favorite.getSortOrder().clear();

					if (! sortColumnId.equals("")) {
						for (TreeTableColumn column : treeTableView_favorite.getColumns()) {
							if (column.getId().equals(sortColumnId)) {
								treeTableView_favorite.setSortMode(TreeSortMode.ALL_DESCENDANTS);
								column.setSortable(true); // This performs a sort
								treeTableView_favorite.getSortOrder().add(column);
								if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
								else column.setSortType(TreeTableColumn.SortType.ASCENDING);
								treeTableView_favorite.sort();
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
					restoreTreeItemStateRecursive(listItemsForExpand,treeTableView_favorite.getRoot());
					treeTableView_favorite.sort();
					restoreTreeItemSelectedRecursive(selectedItemId,treeTableView_favorite.getRoot());
					treeTableView_favorite.sort();
					break;
			}
		}
	}

	/**
	 * рекурсивное восстановление состояния разделов дерева разделов
	 */
	private void restoreTreeItemStateRecursive(
			ObservableList<Long> listItemsForExpand,
			TreeItem<SectionFavoriteItem> ti) {
		
		//-------- проверяем и разворачиваем текущий итем
		for (Long i : listItemsForExpand) {
			if (i == ti.getValue().getId()) {
				ti.setExpanded(true);
				break;
			}
		}
		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<SectionFavoriteItem> i : ti.getChildren()) {
			restoreTreeItemStateRecursive(listItemsForExpand, i);
		}
	}

	/**
	 * рекурсивно ищем активный раздел в дереве разделов и выбираем его
	 */
	private void restoreTreeItemSelectedRecursive(
			Long selectedItemId,
			TreeItem<SectionFavoriteItem> ti) {
		
		//-------- проверяем и выбираем текущий итем
		if (selectedItemId == ti.getValue().getId()) {
			treeTableView_favorite.getSelectionModel().select(ti);
			
			int row = treeTableView_favorite.getRow(ti);
			if (row >= 0) {
				treeTableView_favorite.scrollTo(row);
			}
		}
		
		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<SectionFavoriteItem> i : ti.getChildren()) {
			restoreTreeItemSelectedRecursive(selectedItemId, i);
		}
	}
}
