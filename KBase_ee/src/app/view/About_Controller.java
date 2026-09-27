package app.view;

import java.awt.Desktop;
import java.net.URI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Контролер віконця "Про програму".
 * @author Igor Makarevich
 */
public class About_Controller {
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
	private Label label_Author;
	@FXML
	private Hyperlink hyperlink_Site;
	@FXML
	private Button button_Close;

	// адреса сайту для відкриття у браузері
	private String siteUrl;

	/**
	 * Конструктор.
	 * Конструктор викликається раніше метода initialize().
	 */
	public About_Controller() {

	}

	/**
	 * Ініціалізація класа-контролера. Цей метод викликається автоматично
	 * після того, як fxml-файл буде завантажено.
	 */
	@FXML
	private void initialize() {
		imageView_AppIcon.setImage(new Image("file:resources/images/MainIco.png"));
		button_Close.setGraphic(new ImageView(new Image("file:resources/images/icon_ok_16.png")));
	}

	/**
	 * Задає інформацію про програму для відображення у віконці:
	 * назву, версію, діапазон дат версії, кодову назву, автора та сайт.
	 */
	public void setAppInfo(String appName, String version, String dateRange, String codeName, String author, String site) {
		label_AppName.setText(appName);
		label_Version.setText("Версія: " + version);
		label_Date.setText(dateRange);
		label_CodeName.setText("Кодова назва: " + codeName);
		label_Author.setText("Автор: " + author);
		hyperlink_Site.setText(site);

		siteUrl = site;
	}

	/**
	 * Відкриває сайт програми у браузері за замовчуванням.
	 */
	@FXML
	private void handleSiteLink() {
		try {
			if ((siteUrl != null) && (! siteUrl.isEmpty()) && Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI(siteUrl));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Викликається при натисканні на кнопці "Закрити"
	 */
	@FXML
	private void handleButtonClose() {
		//-------- close window
		// get a handle to the stage
		Stage stage = (Stage) button_Close.getScene().getWindow();
		// do what you have to do
		stage.close();
	}
}
