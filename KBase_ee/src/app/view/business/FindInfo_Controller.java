package app.view.business;

import app.lib.AppDataObj;
import app.lib.ShowAppMsg;
import app.lib.StringUtil;
import app.model.AppItem_Interface;
import app.model.FindParams;
import app.model.FindResultItem;
import app.model.business.SectionItem;
import app.model.StateList;
import app.model.Params;
import app.util.FormattedDate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeSortMode;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Контролер фрейму Пошуку інформації
 */
public class FindInfo_Controller implements AppItem_Interface {
	private Params params;
	private SectionItem curSi;
	private Preferences prefs = Preferences.userNodeForPackage(FindInfo_Controller.class);

	@FXML
	private TabPane tabPane_Main;
	@FXML
	private Tab tab_Parameters;
	@FXML
	private Tab tab_Result;

	@FXML
	private RadioButton radioButton_StartInTree_All;
	@FXML
	private RadioButton radioButton_StartInTree_Root;
	@FXML
	private RadioButton radioButton_StartInTree_Current;
	@FXML
	private Label label_StartInTree_Root;
	@FXML
	private Label label_StartInTree_Current;
	@FXML
	private TextField textField_SectionId;
	@FXML
	private TextField textField_DateBegin;
	@FXML
	private TextField textField_DateEnd;
	@FXML
	private CheckBox checkBox_Section;
	@FXML
	private CheckBox checkBox_InfoHeader;
	@FXML
	private CheckBox checkBox_Dictionary;
	@FXML
	private CheckBox checkBox_Text;
	@FXML
	private CheckBox checkBox_Image;
	@FXML
	private CheckBox checkBox_File;
	@FXML
	private TextArea textArea_Text;
	@FXML
	private CheckBox checkBox_Text_IgnoreRegistr;
	@FXML
	private TextField textField_User;
	@FXML
	private CheckBox checkBox_SortTypeDesc;
	@FXML
	private TextField textField_NumberOfElements;
	@FXML
	private Button button_Find;

	@FXML
	private SplitPane splitPane_Result;
	@FXML
	private Button button_ResultChangeSplitOrientation;
	@FXML
	private Button button_ResultGoTo;
	@FXML
	private Button button_ResultOpen;
	@FXML
	private Button button_ResultCount;
	@FXML
	private Label label_ResultCount;
	@FXML
	private Button button_ResultPageLeft;
	@FXML
	private Label label_ResultPageCur;
	@FXML
	private Button button_ResultPageRight;

	@FXML
	public TreeTableView<FindResultItem> treeTableView_Result;
	@FXML
	private TreeTableColumn<FindResultItem, String> treeTableColumn_TextType;
	@FXML
	private TreeTableColumn<FindResultItem, Long> treeTableColumn_SectionId;
	@FXML
	private TreeTableColumn<FindResultItem, String> treeTableColumn_SectionName;
	@FXML
	private TreeTableColumn<FindResultItem, String> treeTableColumn_SectionPathName;
	@FXML
	private TreeTableColumn<FindResultItem, Long> treeTableColumn_InfoId;
	@FXML
	private TreeTableColumn<FindResultItem, String> treeTableColumn_InfoName;
	@FXML
	private TreeTableColumn<FindResultItem, String> treeTableColumn_Text;
	@FXML
	private TreeTableColumn<FindResultItem, FormattedDate> treeTableColumn_DateCreated;
	@FXML
	private TreeTableColumn<FindResultItem, FormattedDate> treeTableColumn_DateModified;
	@FXML
	private TreeTableColumn<FindResultItem, String> treeTableColumn_UserCreated;
	@FXML
	private TreeTableColumn<FindResultItem, String> treeTableColumn_UserModified;

	@FXML
	private SplitPane splitPane_Details;
	@FXML
	private Button button_DetailsChangeSplitOrientation;
	@FXML
	private ToggleButton toggleButton_ResultText_WordWrap;
	@FXML
	private TextArea textArea_ResultText;

	private FindParams findParams;

	private List<FindResultItem> listResult;

	/**
	 * Конструктор.
	 * Конструктор вызывается раньше метода initialize().
	 */
	public FindInfo_Controller () {
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
	public void setParams(Params params, SectionItem curSi) {
		this.params = params;
		this.curSi = curSi;

		// init controls
		initControlValues();
	}

	/**
	 * Инициализирует контролы значениями
	 */
	private void initControlValues() {
		// parameters
		SectionList_Controller c = (SectionList_Controller) params.getParentObj();
		SectionItem root = c.treeTableView_sections.getRoot().getValue();
		label_StartInTree_Root.setText(root.getName() + " [" + root.getId() + "]");
		label_StartInTree_Current.setText(curSi.getName() + " [" + curSi.getId() + "]");
		button_Find.setGraphic(new ImageView(new Image("file:resources/images/icon_find_16.png")));

		//========== splitPane_Result
		if (params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка установки в конфигурации
			String orientation = prefs.get("FindInfo_splitPane_Result_orientation", "VERTICAL");

			if (orientation.equals("HORIZONTAL")) {
				splitPane_Result.setOrientation(Orientation.HORIZONTAL);
			} else {
				splitPane_Result.setOrientation(Orientation.VERTICAL);
			}
		}

		splitPane_Result.setDividerPositions(prefs.getDouble("FindInfo_splitPane_Result_position", 0.6));

		splitPane_Result.getDividers().get(0).positionProperty().addListener(
			(o, oldv, newv) -> {
				prefs.putDouble("FindInfo_splitPane_Result_position", splitPane_Result.getDividerPositions()[0]);
			}
		);

		//========== Result, Toolbar
		button_ResultChangeSplitOrientation.setTooltip(new Tooltip("Змінити орієнтацію панелей"));
		button_ResultChangeSplitOrientation.setGraphic(new ImageView(new Image("file:resources/images/icon_orientation_16.png")));
		button_ResultGoTo.setTooltip(new Tooltip("Перейти до Розділу"));
		button_ResultGoTo.setGraphic(new ImageView(new Image("file:resources/images/icon_ContentTree_16.png")));
		button_ResultOpen.setTooltip(new Tooltip("Відкрити"));
		button_ResultOpen.setGraphic(new ImageView(new Image("file:resources/images/icon_open_16.png")));

		//========== Details, Toolbar
		button_DetailsChangeSplitOrientation.setTooltip(new Tooltip("Змінити орієнтацію панелей"));
		button_DetailsChangeSplitOrientation.setGraphic(new ImageView(new Image("file:resources/images/icon_orientation_16.png")));
		toggleButton_ResultText_WordWrap.setTooltip(new Tooltip("Word Wrap"));
		toggleButton_ResultText_WordWrap.setGraphic(new ImageView(new Image("file:resources/images/icon_word_wrap_16.png")));

		//========== splitPane_Details
		if (params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка установки в конфигурации
			String orientation = prefs.get("FindInfo_splitPane_Details_orientation", "VERTICAL");

			if (orientation.equals("HORIZONTAL")) {
				splitPane_Details.setOrientation(Orientation.HORIZONTAL);
			} else {
				splitPane_Details.setOrientation(Orientation.VERTICAL);
			}
		}

		splitPane_Details.setDividerPositions(prefs.getDouble("FindInfo_splitPane_Details_position", 0.6));

		splitPane_Details.getDividers().get(0).positionProperty().addListener(
			(o, oldv, newv) -> {
				prefs.putDouble("FindInfo_splitPane_Details_position", splitPane_Details.getDividerPositions()[0]);
			}
		);

		//--------
		initTableResult();
	}

	/**
	 * Ініціалізуємо таблицю з результатом
	 */
	private void initTableResult() {

		// setCellValueFactory
		treeTableColumn_TextType.setCellValueFactory(
			(TreeTableColumn.CellDataFeatures<FindResultItem, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getValue().getTextType())
		);
		treeTableColumn_SectionId.setCellValueFactory(
			(TreeTableColumn.CellDataFeatures<FindResultItem, Long> param) ->
				new ReadOnlyObjectWrapper(param.getValue().getValue().getSectionId())
		);
		treeTableColumn_SectionName.setCellValueFactory(
			(TreeTableColumn.CellDataFeatures<FindResultItem, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getValue().getSectionName())
		);
		treeTableColumn_SectionPathName.setCellValueFactory(
			(TreeTableColumn.CellDataFeatures<FindResultItem, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getValue().getSectionPathName())
		);
		treeTableColumn_InfoId.setCellValueFactory(
			(TreeTableColumn.CellDataFeatures<FindResultItem, Long> param) ->
				new ReadOnlyObjectWrapper(param.getValue().getValue().getInfoId())
		);
		treeTableColumn_InfoName.setCellValueFactory(
			(TreeTableColumn.CellDataFeatures<FindResultItem, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getValue().getInfoName())
		);
		treeTableColumn_Text.setCellValueFactory(
				(TreeTableColumn.CellDataFeatures<FindResultItem, String> param) ->
				new ReadOnlyStringWrapper(StringUtil.findLineWithText(
					param.getValue().getValue().getText(),
					textArea_Text.getText(),
					!checkBox_Text_IgnoreRegistr.isSelected()
				))
			);
		treeTableColumn_DateCreated.setCellValueFactory(param -> {
			LocalDateTime ldt = param.getValue().getValue().getDateCreated();
			Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
			return new ReadOnlyObjectWrapper(new FormattedDate(date));
		});
		treeTableColumn_DateModified.setCellValueFactory(param -> {
			LocalDateTime ldt = param.getValue().getValue().getDateModified();
			Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
			return new ReadOnlyObjectWrapper(new FormattedDate(date));
		});
		treeTableColumn_UserCreated.setCellValueFactory(
			(TreeTableColumn.CellDataFeatures<FindResultItem, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getValue().getUserCreated())
		);
		treeTableColumn_UserModified.setCellValueFactory(
			(TreeTableColumn.CellDataFeatures<FindResultItem, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getValue().getUserModified())
		);

		// setPrefWidth
		treeTableColumn_TextType.setPrefWidth(prefs.getDouble("FindInfo_treeTableColumn_TextType_PrefWidth", 100));
		treeTableColumn_TextType.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> obs, Number t, Number t1) {
				prefs.putDouble("FindInfo_treeTableColumn_TextType_PrefWidth", t1.doubleValue());
			}
		});

		treeTableColumn_SectionId.setPrefWidth(prefs.getDouble("FindInfo_treeTableColumn_SectionId_PrefWidth", 50));
		treeTableColumn_SectionId.widthProperty().addListener((obs, oldw, neww) -> {
			prefs.putDouble("FindInfo_treeTableColumn_SectionId_PrefWidth", neww.doubleValue());
		});

		treeTableColumn_SectionName.setPrefWidth(prefs.getDouble("FindInfo_treeTableColumn_SectionName_PrefWidth", 200));
		treeTableColumn_SectionName.widthProperty().addListener((obs, oldw, neww) -> {
			prefs.putDouble("FindInfo_treeTableColumn_SectionName_PrefWidth", neww.doubleValue());
		});

		treeTableColumn_SectionPathName.setPrefWidth(prefs.getDouble("FindInfo_treeTableColumn_SectionPathName_PrefWidth", 300));
		treeTableColumn_SectionPathName.widthProperty().addListener((obs, oldw, neww) -> {
			prefs.putDouble("FindInfo_treeTableColumn_SectionPathName_PrefWidth", neww.doubleValue());
		});

		treeTableColumn_InfoId.setPrefWidth(prefs.getDouble("FindInfo_treeTableColumn_InfoId_PrefWidth", 50));
		treeTableColumn_InfoId.widthProperty().addListener((obs, oldw, neww) -> {
			prefs.putDouble("FindInfo_treeTableColumn_InfoId_PrefWidth", neww.doubleValue());
		});

		treeTableColumn_InfoName.setPrefWidth(prefs.getDouble("FindInfo_treeTableColumn_InfoName_PrefWidth", 100));
		treeTableColumn_InfoName.widthProperty().addListener((obs, oldw, neww) -> {
			prefs.putDouble("FindInfo_treeTableColumn_InfoName_PrefWidth", neww.doubleValue());
		});

		treeTableColumn_Text.setPrefWidth(prefs.getDouble("FindInfo_treeTableColumn_Text_PrefWidth", 300));
		treeTableColumn_Text.widthProperty().addListener((obs, oldw, neww) -> {
			prefs.putDouble("FindInfo_treeTableColumn_Text_PrefWidth", neww.doubleValue());
		});

		treeTableColumn_DateCreated.setPrefWidth(prefs.getDouble("FindInfo_treeTableColumn_DateCreated_PrefWidth", 100));
		treeTableColumn_DateCreated.widthProperty().addListener((obs, oldw, neww) -> {
			prefs.putDouble("FindInfo_treeTableColumn_DateCreated_PrefWidth", neww.doubleValue());
		});

		treeTableColumn_DateModified.setPrefWidth(prefs.getDouble("FindInfo_treeTableColumn_DateModified_PrefWidth", 100));
		treeTableColumn_DateModified.widthProperty().addListener((obs, oldw, neww) -> {
			prefs.putDouble("FindInfo_treeTableColumn_DateModified_PrefWidth", neww.doubleValue());
		});

		treeTableColumn_UserCreated.setPrefWidth(prefs.getDouble("FindInfo_treeTableColumn_UserCreated_PrefWidth", 100));
		treeTableColumn_UserCreated.widthProperty().addListener((obs, oldw, neww) -> {
			prefs.putDouble("FindInfo_treeTableColumn_UserCreated_PrefWidth", neww.doubleValue());
		});

		treeTableColumn_UserModified.setPrefWidth(prefs.getDouble("FindInfo_treeTableColumn_UserModified_PrefWidth", 100));
		treeTableColumn_UserModified.widthProperty().addListener((obs, oldw, neww) -> {
			prefs.putDouble("FindInfo_treeTableColumn_UserModified_PrefWidth", neww.doubleValue());
		});

		//------------- Init
		TreeItem<FindResultItem> rootResultList = new TreeItem<>(new FindResultItem("", 0, "", "",
				0, "", "", null, null, null, null));
		rootResultList.setExpanded(true);
		treeTableView_Result.setShowRoot(false);
		treeTableView_Result.setRoot(rootResultList);

		// Слухаємо зміни вибору, з прямим відображенням інформації
		treeTableView_Result.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> onChangeSelectedItem(newValue));

		//---------- write sort
		treeTableView_Result.getSortOrder().addListener((ListChangeListener<TreeTableColumn<FindResultItem, ?>>) change -> {
			if (!change.getList().isEmpty()) {
				TreeTableColumn sortedColumn = change.getList().get(0);
				prefs.put("FindInfo_sortTypeColumnId", sortedColumn.getId());
				prefs.put("FindInfo_sortType", sortedColumn.getSortType().toString()); // "ASCENDING" або "DESCENDING"
			}
		});

		for (TreeTableColumn<FindResultItem, ?> column : treeTableView_Result.getColumns()) {
			column.sortTypeProperty().addListener((obs, oldval, newval) -> {
				if (treeTableView_Result.getSortOrder().contains(column)) {
					prefs.put("FindInfo_sortTypeColumnId", column.getId());
					prefs.put("FindInfo_sortType", newval.toString());
				}
			});
		}

	}

	/**
	 * Завантажуємо дані в контрол-таблицю результату
	 */
	public void load () {

		treeTableView_Result.getRoot().getChildren().clear();
		TreeItem<FindResultItem> rootResultList = new TreeItem<>(new FindResultItem("", 0, "", "",
				0, "", "", null, null, null, null));
		rootResultList.setExpanded(true);
		treeTableView_Result.setShowRoot(false);
		treeTableView_Result.setRoot(rootResultList);

		//=========== list init -- load data
		for (FindResultItem i : listResult) {
			TreeItem<FindResultItem> subItem = new TreeItem<>(i);
			rootResultList.getChildren().add(subItem);
		}

		//=========== select first item
		if (!treeTableView_Result.getRoot().getChildren().isEmpty()) {
			//treeTableView_Result.getSelectionModel().select(0);
			TreeItem<FindResultItem> startItem = treeTableView_Result.getRoot().getChildren().get(0);
			treeTableView_Result.getSelectionModel().select(startItem);
		}

		//---------- set sort
		String sortColumnId = prefs.get("FindInfo_sortTypeColumnId", "");

		if (!sortColumnId.equals("")) {
			for (TreeTableColumn column : treeTableView_Result.getColumns()) {
				if (column.getId().equals(sortColumnId)) {
					String sortType = prefs.get("FindInfo_sortType", "ASCENDING");

					treeTableView_Result.setSortMode(TreeSortMode.ALL_DESCENDANTS);
					column.setSortable(true); // this performs a sort
					treeTableView_Result.getSortOrder().add(column);
					if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
					else column.setSortType(TreeTableColumn.SortType.ASCENDING);
					
					treeTableView_Result.sort();
				}
			}
		}

	}

	/**
	 * Викликається при виборі елемента таблиці результату.
	 */
	private void onChangeSelectedItem(TreeItem<FindResultItem> ti) {
		if (ti != null) {
			FindResultItem i = ti.getValue();

			textArea_ResultText.setText(i.getText());
		}
	}

	/**
	 *
	 */
	@FXML
	private void handleButtonFind() {
		// init findParams
		findParams = new FindParams();
		findParams.setObjSection(checkBox_Section.isSelected());
		findParams.setObjInfoHeader(checkBox_InfoHeader.isSelected());
		findParams.setObjDictionary(checkBox_Dictionary.isSelected());
		findParams.setObjText(checkBox_Text.isSelected());
		findParams.setObjImage(checkBox_Image.isSelected());
		findParams.setObjFile(checkBox_File.isSelected());
		findParams.setText(textArea_Text.getText());
		findParams.setTextIgnoreRegistr(checkBox_Text_IgnoreRegistr.isSelected());

		// get result
		listResult = params.getConCur().db.findInfo(findParams);

		load();

		tabPane_Main.getSelectionModel().select(1);

		for (FindResultItem item : listResult) {
			System.out.println("texttype: " + item.getTextType());
			System.out.println("sectionId: " + item.getSectionId());
			System.out.println("sectionName: " + item.getSectionName());
			System.out.println("sectionPathName: " + item.getSectionPathName());
			System.out.println("infoId: " + item.getInfoId());
			System.out.println("infoName: " + item.getInfoName());
			System.out.println("text: " + item.getText());
			System.out.println("dateCreated: " + item.getDateCreated());
			System.out.println("dateModified: " + item.getDateModified());
			System.out.println("userCreated: " + item.getUserCreated());
			System.out.println("userModified: " + item.getUserModified());
			System.out.println("----------------------------------------");
		}
	}

	/**
	 *
	 */
	@FXML
	private void handleButtonResultChangeSplitOrientation() {
		if (splitPane_Result.getOrientation() == Orientation.HORIZONTAL) {
			splitPane_Result.setOrientation(Orientation.VERTICAL);
			if (params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка установки в конфигурации
				prefs.put("FindInfo_splitPane_Result_orientation", "VERTICAL");
			}
		} else {
			splitPane_Result.setOrientation(Orientation.HORIZONTAL);
			if (params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка установки в конфигурации
				prefs.put("FindInfo_splitPane_Result_orientation", "HORIZONTAL");
			}
		}
	}

	/**
	 *
	 */
	@FXML
	private void handleButtonResultGoTo() {
		if (treeTableView_Result.getSelectionModel().getSelectedItem() == null) {
			ShowAppMsg.showAlert("Warning", "Нет выбора", "Ни один выбран раздел в списке",
					"Выберите раздел, который необходимо открыть.");
			return;
		}

		SectionList_Controller slc = (SectionList_Controller) params.getParentObj();
		long sectionId = treeTableView_Result.getSelectionModel().getSelectedItem().getValue().getSectionId();

		slc.treeViewCtrl.gotoSection(sectionId);
	}

	/**
	 *
	 */
	@FXML
	private void handleButtonResultOpen() {
		if (treeTableView_Result.getSelectionModel().getSelectedItem() == null) {
			ShowAppMsg.showAlert("Warning", "Нет выбора", "Ни один выбран раздел в списке",
					"Выберите раздел, который необходимо открыть.");
			return;
		}

		FindResultItem ri = treeTableView_Result.getSelectionModel().getSelectedItem().getValue();
		TreeItem<SectionItem> tsi = new TreeItem<SectionItem>(params.getConCur().db.sectionGetById(ri.getSectionId()));

		//----------- открываем таб для просмотра документа
		Params params = new Params(this.params);
		params.setObjContainer(this.params.getObjContainer());
		params.setParentObj(this.params.getParentObj());
		params.setTabPane_Cur(this.params.getTabPane_Cur());

		AppDataObj.openDocumentView (params, tsi);
	}

	/**
	 *
	 */
	@FXML
	private void handleButtonResultCount() {

	}
	//TODO

	/**
	 *
	 */
	@FXML
	private void handleButtonResultPageLeft() {

	}
	//TODO

	/**
	 *
	 */
	@FXML
	private void handleButtonResultPageRight() {

	}
	//TODO

	/**
	 *
	 */
	@FXML
	private void handleButtonDetailsChangeSplitOrientation() {
		if (splitPane_Details.getOrientation() == Orientation.HORIZONTAL) {
			splitPane_Details.setOrientation(Orientation.VERTICAL);
			if (params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка установки в конфигурации
				prefs.put("FindInfo_splitPane_Details_orientation", "VERTICAL");
			}
		} else {
			splitPane_Details.setOrientation(Orientation.HORIZONTAL);
			if (params.getConfig().getItemValue("AppState", "SaveAppStateOnExit").equals("1")) { // проверка установки в конфигурации
				prefs.put("FindInfo_splitPane_Details_orientation", "HORIZONTAL");
			}
		}
	}

	/**
	 *
	 */
	@FXML
	private void handleToggleButtonResultText_WordWrap() {
		textArea_ResultText.setWrapText(toggleButton_ResultText_WordWrap.isSelected());
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
		return AppItem_Interface.ELEMENT_FIND_INFO;
	}

	/**
	 * Повертає параметри об'єкта інтерфейса
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public Params getParams() {
		return params;
	}

	/**
	 * Id обьекта элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public long getAppItemId() {
		return curSi.getId();
	}

	/**
	 * Id соединения с базой данных
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
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Зачиняємо таб чи вікно з цим елементом інтерфейсу
	 */
	public void close () {
		if (! ShowAppMsg.showQuestion (
				"CONFIRMATION",
				"Питання",
				"",
				"Закрити вкладку? " ) ) {
			return;
		}
		params.getObjContainer().closeContainer(getOID());
	}

	/**
	 * Реализуем метод интерфейса AppItem_Interface.             <br>
	 * Сохраняем состояние контролов в иерархической структуре
	 */
	public void saveControlState (StateList stateList) {

	}

	/**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Восстанавливаем состояние контролов из иерархической структуры
	 */
	public void restoreControlState (StateList stateList) {

	}

}