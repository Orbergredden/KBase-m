package app.view;

import app.exceptions.DataConnectionException;
import app.exceptions.DataQueryException;
import app.lib.ShowAppMsg;
import app.model.DBConCur_Parameters;
import app.model.Params;

import java.util.Optional;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Контролер для діалогового вікна клонування БД ("Clone database...")
 * @author Igor Makarevich
 */
public class DBClone_Controller {
	private static final int ACCESS_TYPE_DB_CLONE = 2;

	@FXML
	private ImageView imageView_Icon;
	@FXML
	private ComboBox<DBConCur_Parameters> comboBox_SourceDB;
	@FXML
	private ComboBox<DBConCur_Parameters> comboBox_TargetDB;
	@FXML
	private CheckBox checkBox_Icons;
	@FXML
	private CheckBox checkBox_Templates;
	@FXML
	private CheckBox checkBox_Sections;
	@FXML
	private CheckBox checkBox_Documents;
	@FXML
	private Label label_Warning;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Label label_Status;
	@FXML
	private Button button_Cancel;
	@FXML
	private Button button_Clone;

	private Params params;
	private Stage dialogStage;

	public DBClone_Controller() {
	}

	@FXML
	private void initialize() {
		button_Clone.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_16.png")));
		button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
		imageView_Icon.setImage(new Image("file:resources/images/icon_copy_16.png"));
	}

	public void setParams(Params params) {
		this.params = params;
		if (params != null) {
			this.dialogStage = params.getStageCur();
		}

		initDBComboBoxes();
	}

	/**
	 * Налаштування випадаючих списків відкритих підключень БД
	 */
	private void initDBComboBoxes() {
		if (params == null || params.getConnDB() == null || params.getConnDB().conList == null) {
			return;
		}

		StringConverter<DBConCur_Parameters> converter = new StringConverter<DBConCur_Parameters>() {
			@Override
			public String toString(DBConCur_Parameters object) {
				if (object == null || object.param == null) {
					return "";
				}
				return object.param.getName() + " (" + object.param.getHost() + ":" + object.param.getName() + ")";
			}

			@Override
			public DBConCur_Parameters fromString(String string) {
				return null;
			}
		};

		comboBox_SourceDB.setConverter(converter);
		comboBox_TargetDB.setConverter(converter);

		comboBox_SourceDB.setCellFactory(cell -> new ListCell<DBConCur_Parameters>() {
			@Override
			protected void updateItem(DBConCur_Parameters item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.param == null) {
					setText(null);
				} else {
					setText(item.param.getName() + " (" + item.param.getHost() + ":" + item.param.getName() + ")");
				}
			}
		});

		comboBox_TargetDB.setCellFactory(cell -> new ListCell<DBConCur_Parameters>() {
			@Override
			protected void updateItem(DBConCur_Parameters item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.param == null) {
					setText(null);
				} else {
					setText(item.param.getName() + " (" + item.param.getHost() + ":" + item.param.getName() + ")");
				}
			}
		});

		comboBox_SourceDB.setItems(params.getConnDB().conList);
		comboBox_TargetDB.setItems(params.getConnDB().conList);

		// Значення за замовчуванням
		if (params.getConnDB().conList.size() > 0) {
			comboBox_SourceDB.getSelectionModel().select(0);
		}
		if (params.getConnDB().conList.size() > 1) {
			comboBox_TargetDB.getSelectionModel().select(1);
		} else if (params.getConnDB().conList.size() == 1) {
			comboBox_TargetDB.getSelectionModel().select(0);
		}

		// Валідація однакових підключень
		comboBox_SourceDB.valueProperty().addListener((obs, oldVal, newVal) -> validateConnections());
		comboBox_TargetDB.valueProperty().addListener((obs, oldVal, newVal) -> validateConnections());

		validateConnections();
	}

	private void validateConnections() {
		DBConCur_Parameters src = comboBox_SourceDB.getValue();
		DBConCur_Parameters target = comboBox_TargetDB.getValue();

		if (src != null && target != null && src.Id == target.Id) {
			label_Warning.setText("⚠ БД-джерело та БД-приймач повинні бути різними підключеннями!");
			button_Clone.setDisable(true);
		} else {
			label_Warning.setText("⚠ Увага! База-приймач буде попередньо очищена від вибраних типів даних.");
			button_Clone.setDisable(false);
		}
	}

	@FXML
	private void handleCancel() {
		closeStage();
	}

	@FXML
	private void handleClone() {
		DBConCur_Parameters srcCon = comboBox_SourceDB.getValue();
		DBConCur_Parameters targetCon = comboBox_TargetDB.getValue();

		if (srcCon == null || targetCon == null) {
			ShowAppMsg.showAlert("WARNING", "Не вибрано БД", "Помилка вибору підключення", "Виберіть БД-джерело та БД-приймач.");
			return;
		}

		if (srcCon.Id == targetCon.Id) {
			ShowAppMsg.showAlert("WARNING", "Однакові БД", "Некоректний вибір", "Джерело та приймач не можуть бути однією і тією ж БД.");
			return;
		}

		boolean doIcons = checkBox_Icons.isSelected();
		boolean doTemplates = checkBox_Templates.isSelected();
		boolean doSections = checkBox_Sections.isSelected();

		if (!doIcons && !doTemplates && !doSections) {
			ShowAppMsg.showAlert("WARNING", "Нічого не вибрано", "Оберіть дані для переносу", "Потрібно відмітити хоча б один тип даних.");
			return;
		}

		// Перевірка прав доступу до бази-приймача
		try {
			boolean hasAccess1 = srcCon.db.accessGet(ACCESS_TYPE_DB_CLONE);
			boolean hasAccess2 = targetCon.db.accessGet(ACCESS_TYPE_DB_CLONE);
			if (!hasAccess1) {
				ShowAppMsg.showAlert("WARNING", "Доступ заборонено",
						"У вас немає прав для клонування цієї БД",
						"База: " + targetCon.param.getName() + "\nКористувач: " + targetCon.db.getCurrentUser());
				return;
			}
			if (!hasAccess2) {
				ShowAppMsg.showAlert("WARNING", "Доступ заборонено",
						"У вас немає прав для очищення/запису в базу-приймач",
						"База: " + targetCon.param.getName() + "\nКористувач: " + targetCon.db.getCurrentUser());
				return;
			}
		} catch (DataConnectionException | DataQueryException e) {
			e.writeLog(params);
			ShowAppMsg.showAlert("ERROR", "Помилка перевірки прав", Integer.toString(e.getErrCode()) + " " + e.getErrSign(), e.getMsg());
			return;
		}

		// Підтвердження
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Підтвердження клонування");
		alert.setHeaderText("Ви дійсно бажаєте виконати клонування даних?");
		alert.setContentText("Джерело: " + srcCon.param.getName() + "\n" +
				"Приймач: " + targetCon.param.getName() + "\n\n" +
				"УВАГА! База-приймач буде попередньо очищена від вибраних блоків даних. Операція є незворотньою.");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			runCloneProcess(srcCon, targetCon, doIcons, doTemplates, doSections);
		}
	}

	private void runCloneProcess(DBConCur_Parameters srcCon, DBConCur_Parameters targetCon,
	                            boolean doIcons, boolean doTemplates, boolean doSections) {
		button_Clone.setDisable(true);
		button_Cancel.setDisable(true);
		comboBox_SourceDB.setDisable(true);
		comboBox_TargetDB.setDisable(true);
		checkBox_Icons.setDisable(true);
		checkBox_Templates.setDisable(true);
		checkBox_Sections.setDisable(true);

		progressBar.setVisible(true);
		progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		label_Status.setVisible(true);
		label_Status.setText("Виконується клонування даних...");

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				targetCon.db.dbCloneFrom(srcCon.db, doIcons, doTemplates, doSections);
				return null;
			}
		};

		task.setOnSucceeded(event -> {
			progressBar.setVisible(false);
			label_Status.setText("Клонування успішно завершено!");
			ShowAppMsg.showAlert("INFORMATION", "Повідомлення",
					"Клонування бази даних завершено",
					"Дані з БД '" + srcCon.param.getName() + "' успішно перенесені в БД '" + targetCon.param.getName() + "'.");
			closeStage();
		});

		task.setOnFailed(event -> {
			progressBar.setVisible(false);
			label_Status.setVisible(false);
			button_Clone.setDisable(false);
			button_Cancel.setDisable(false);
			comboBox_SourceDB.setDisable(false);
			comboBox_TargetDB.setDisable(false);
			checkBox_Icons.setDisable(false);
			checkBox_Templates.setDisable(false);
			checkBox_Sections.setDisable(false);

			Throwable e = task.getException();
			if (e instanceof DataQueryException) {
				((DataQueryException) e).writeLog(params);
			} else if (e instanceof DataConnectionException) {
				((DataConnectionException) e).writeLog(params);
			} else if (e != null) {
				e.printStackTrace();
			}

			ShowAppMsg.showAlert("ERROR", "Помилка клонування",
					"Виникла помилка при клонуванні бази даних",
					e != null ? e.getMessage() : "Невідома помилка");
		});

		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}

	private void closeStage() {
		if (dialogStage != null) {
			dialogStage.close();
		}
	}
}
