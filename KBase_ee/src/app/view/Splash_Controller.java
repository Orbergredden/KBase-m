package app.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Контролер стартового віконця (splash), яке показується при запуску програми
 * і зникає коли відкриється основне вікно.
 * @author Igor Makarevich
 */
public class Splash_Controller {
	@FXML
	private ImageView imageView_AppIcon;
	@FXML
	private Label label_AppName;
	@FXML
	private Label label_Version;
	@FXML
	private Label label_Date;
	@FXML
	private Label label_CodeName;
	@FXML
	private Label label_Status;

	/**
	 * Конструктор.
	 * Конструктор викликається раніше метода initialize().
	 */
	public Splash_Controller() {

	}

	/**
	 * Ініціалізація класа-контролера. Цей метод викликається автоматично
	 * після того, як fxml-файл буде завантажено.
	 */
	@FXML
	private void initialize() {
		imageView_AppIcon.setImage(new Image("file:resources/images/MainIco.png"));
	}

	/**
	 * Задає інформацію про програму для відображення у віконці:
	 * назву, версію, діапазон дат версії та кодову назву.
	 */
	public void setAppInfo(String appName, String version, String dateRange, String codeName) {
		label_AppName.setText(appName);
		label_Version.setText("Версія: " + version);
		label_Date.setText(dateRange);
		label_CodeName.setText("Кодова назва: " + codeName);
	}

	/**
	 * Показує поточний етап завантаження у віконці.
	 */
	public void setStatus(String text) {
		label_Status.setText(text);
	}
}
