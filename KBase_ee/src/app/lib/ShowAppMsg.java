package app.lib;

import java.util.Optional;
import java.util.prefs.Preferences;

import javafx.animation.PauseTransition;
import javafx.geometry.Rectangle2D;
//import app.view.CatalogIconsList_Controller;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 * Данный класс предназначен для вывода сообщений уровня приложения.
 * 
 * @author Igor Makarevich
 * @version 1.00.01.002 31.05.2017 - 11.06.2018
 */
public class ShowAppMsg {
	// return values
	public static final int QUESTION_CANCEL = 0;
	public static final int QUESTION_OK = 1;
	public static final int QUESTION_OK_WITH_OPTION = 2;
	public static final int SELECT_BUTTON_1 = 1;
	public static final int SELECT_BUTTON_2 = 2;
	public static final int SELECT_BUTTON_3 = 3;

	/**
	 * Выводится модальное окошко-алерт
	 */
	public static void showAlert (String msgType, String title, String header, String msg) {
		Alert alert = new Alert(AlertType.valueOf(msgType));
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(msg);
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		String iconFileName;
		switch (msgType) {
			case "CONFIRMATION" : iconFileName = new String("icon_confirmation_16.png");  break;
			case "ERROR"        : iconFileName = new String("icon_error_16.png");         break;
			case "INFORMATION"  : iconFileName = new String("icon_information_16.png");   break;
			case "NONE"         : iconFileName = new String("icon_none_16.png");          break;
			case "WARNING"      : iconFileName = new String("icon_warning_16.png");       break;
			default             : iconFileName = new String("MainIco.png");               break;
		}
		stage.getIcons().add(new Image("file:resources/images/"+ iconFileName));
        
        alert.showAndWait();
	}
	
	/**
	 * Выводится модальное окошко с вопросом и кнопками Да и Нет
	 */
	public static boolean showQuestion (String msgType, String title, String header, String msg) {
		ButtonType buttonType_Ok     = new ButtonType("Да", ButtonBar.ButtonData.OK_DONE);
		ButtonType ButtonType_Cancel = new ButtonType("Нет", ButtonBar.ButtonData.CANCEL_CLOSE);
		Alert alert = new Alert(AlertType.valueOf(msgType), msg, buttonType_Ok, ButtonType_Cancel);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		String iconFileName;
		
		alert.setTitle(title);
		alert.setHeaderText(header);
		switch (msgType) {
			case "CONFIRMATION" : iconFileName = new String("icon_confirmation_16.png");  break;
			case "ERROR"        : iconFileName = new String("icon_error_16.png");         break;
			case "INFORMATION"  : iconFileName = new String("icon_information_16.png");   break;
			case "NONE"         : iconFileName = new String("icon_none_16.png");          break;
			case "WARNING"      : iconFileName = new String("icon_warning_16.png");       break;
			default             : iconFileName = new String("MainIco.png");               break;
		}
		stage.getIcons().add(new Image("file:resources/images/"+ iconFileName));
		
		Optional<ButtonType> result = alert.showAndWait();

		if (result.isPresent() && result.get() == buttonType_Ok) {
		    return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Выводится модальное окошко с вопросом и кнопками Да и Нет  и чек-боксом (состоянием)
	 */
	public static int showQuestionWithOption (
			String msgType, String title, String header, String msg, String msgOpt, boolean valOpt
			) {
		ButtonType buttonType_Ok     = new ButtonType("Да", ButtonBar.ButtonData.OK_DONE);
		ButtonType ButtonType_Cancel = new ButtonType("Нет", ButtonBar.ButtonData.CANCEL_CLOSE);
		
		Preferences prefs = Preferences.userNodeForPackage(ShowAppMsg.class);
		prefs.put("showQuestionWithOption.OptionValue", valOpt ? "Yes" : "No");
		
		Alert alert = createAlertWithOption(
				AlertType.valueOf(msgType), title, header, msg, msgOpt, valOpt,
                param -> {
                    prefs.put("showQuestionWithOption.OptionValue", param ? "Yes" : "No");
                    return null;
                }, buttonType_Ok, ButtonType_Cancel);
		
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		String iconFileName;
		switch (msgType) {
			case "CONFIRMATION" : iconFileName = "icon_confirmation_16.png";  break;
			case "ERROR"        : iconFileName = "icon_error_16.png";         break;
			case "INFORMATION"  : iconFileName = "icon_information_16.png";   break;
			case "NONE"         : iconFileName = "icon_none_16.png";          break;
			case "WARNING"      : iconFileName = "icon_warning_16.png";       break;
			default             : iconFileName = "MainIco.png";               break;
		}
		stage.getIcons().add(new Image("file:resources/images/"+ iconFileName));
		
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == buttonType_Ok) {
			if (prefs.get("showQuestionWithOption.OptionValue", "No").equals("Yes"))  return QUESTION_OK_WITH_OPTION;
			else  return QUESTION_OK;
		} else {
			return QUESTION_CANCEL;
		}
	}
	
	/**
	 * Создает диалог с вопросом (кнопками) и чек-боксом
	 */
	public static Alert createAlertWithOption (
			AlertType type, String title, String headerText, String message, 
			String optOutMessage, boolean optValue, Callback<Boolean, Void> optOutAction, 
            ButtonType... buttonTypes) {
		Alert alert = new Alert(type);
		
		// Need to force the alert to layout in order to grab the graphic,
		// as we are replacing the dialog pane with a custom pane
		alert.getDialogPane().applyCss();
		
		Node graphic = alert.getDialogPane().getGraphic();
		
		// Create a new dialog pane that has a checkbox instead of the hide/show details button
		// Use the supplied callback for the action of the checkbox
		alert.setDialogPane(new DialogPane() {
			@Override
		    protected Node createDetailsButton() {
				CheckBox optOut = new CheckBox();
				optOut.setText(optOutMessage);
				optOut.setSelected(optValue);
				optOut.setOnAction(e -> optOutAction.call(optOut.isSelected()));
				return optOut;
		   }
		});
		 
		alert.getDialogPane().getButtonTypes().addAll(buttonTypes);
		alert.getDialogPane().setContentText(message);
		// Fool the dialog into thinking there is some expandable content
		// a Group won't take up any space if it has no children
		alert.getDialogPane().setExpandableContent(new Group());
		alert.getDialogPane().setExpanded(true);
		// Reset the dialog graphic using the default style
		alert.getDialogPane().setGraphic(graphic);
		alert.setTitle(title);
		alert.setHeaderText(headerText); 
		
		return alert;
	}
	
	/**
	 * Диалог с двумя пользовательскими кнопками. Возвращает номер кнопки.
	 */
	public static int showQuestionWith2Buttons (String msgType, String title, String header, String msg, 
			String buttonName_1, String buttonName_2) {
		ButtonType bt1 = new ButtonType(buttonName_1, ButtonBar.ButtonData.OK_DONE);
		ButtonType bt2 = new ButtonType(buttonName_2, ButtonBar.ButtonData.OK_DONE);
		Alert alert = new Alert(AlertType.valueOf(msgType), msg, bt1, bt2);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		String iconFileName;
		
		alert.setTitle(title);
		alert.setHeaderText(header);
		switch (msgType) {
			case "CONFIRMATION" : iconFileName = new String("icon_confirmation_16.png");  break;
			case "ERROR"        : iconFileName = new String("icon_error_16.png");         break;
			case "INFORMATION"  : iconFileName = new String("icon_information_16.png");   break;
			case "NONE"         : iconFileName = new String("icon_none_16.png");          break;
			case "WARNING"      : iconFileName = new String("icon_warning_16.png");       break;
			default             : iconFileName = new String("MainIco.png");               break;
		}
		stage.getIcons().add(new Image("file:resources/images/"+ iconFileName));
		
		Optional<ButtonType> result = alert.showAndWait();

		if (result.isPresent() && result.get() == bt2) {
			return SELECT_BUTTON_2;
		} else {
			return SELECT_BUTTON_1;
		}
	}

    /**
     * Диалог с тремя пользовательскими кнопками. Возвращает номер кнопки.
     */
    public static int showQuestionWith3Buttons (String msgType, String title, String header, String msg,
                                                String buttonName_1, String buttonName_2, String buttonName_3) {
        ButtonType bt1 = new ButtonType(buttonName_1, ButtonBar.ButtonData.OK_DONE);
        ButtonType bt2 = new ButtonType(buttonName_2, ButtonBar.ButtonData.OK_DONE);
        ButtonType bt3 = new ButtonType(buttonName_3, ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(AlertType.valueOf(msgType), msg, bt1, bt2, bt3);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        String iconFileName;

        alert.setTitle(title);
        alert.setHeaderText(header);
        switch (msgType) {
            case "CONFIRMATION" : iconFileName = new String("icon_confirmation_16.png");  break;
            case "ERROR"        : iconFileName = new String("icon_error_16.png");         break;
            case "INFORMATION"  : iconFileName = new String("icon_information_16.png");   break;
            case "NONE"         : iconFileName = new String("icon_none_16.png");          break;
            case "WARNING"      : iconFileName = new String("icon_warning_16.png");       break;
            default             : iconFileName = new String("MainIco.png");               break;
        }
        stage.getIcons().add(new Image("file:resources/images/"+ iconFileName));

        Optional<ButtonType> result = alert.showAndWait();

        if        (result.isPresent() && result.get() == bt1) {
            return SELECT_BUTTON_1;
        } else if (result.isPresent() && result.get() == bt2) {
            return SELECT_BUTTON_2;
        } else if (result.isPresent() && result.get() == bt3) {
            return SELECT_BUTTON_3;
        } else {
            return 0;
        }
    }
    
    /**
     * Створює та показує вікно з одним повідомленням
     * Без кнопок та заголовку вікна
     */
    public static Stage showMessage (String message) { 
    	Stage stage = new Stage();
    
        Label label = new Label(message);

        // Створення кореневого вузла
        StackPane root = new StackPane();
        root.getChildren().add(label);

        // Створення сцени
        Scene scene = new Scene(root, 300, 200);

        // Налаштування вікна без системного заголовку
        stage.initStyle(StageStyle.UNDECORATED);

        // Відключення кнопок мінімізації, максимізації та закриття
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> e.consume());

        // Налаштування сцени і показ вікна
        stage.setScene(scene);
        stage.show();
    	
    	return stage;
    }
    
    /**
     * Показується маленьке віконечко з повідомленням, 
     * яке з'являляється поверх усіх вікон в нижньому правому куті екрану, а потім зникає
     * @param message
     * @param backgroundColor
     * lightgray, gold, khaki, peachpuff, lightblue, lavender, mistyrose, lightgreen, cornsilk, powderblue
     */
    public static void NotificationPopup (String message, String backgroundColor, int waitSec) {
    	Stage popup = new Stage();
        popup.initStyle(StageStyle.UNDECORATED);
        popup.setAlwaysOnTop(true);

        Label label = new Label(message);
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        StackPane root = new StackPane(label);
        root.setStyle("-fx-background-color: "+ backgroundColor +"; -fx-padding: 20; -fx-border-color: black; -fx-border-width: 2px;");
        
        Scene scene = new Scene(root);
        popup.setScene(scene);

        // Збільшені розміри вікна
        double width = 600;
        double height = 100;
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        popup.setWidth(width);
        popup.setHeight(height);
        popup.setX(screenBounds.getMaxX() - width - 10);
        popup.setY(screenBounds.getMaxY() - height - 10);

        popup.show();

        // Автоматичне закриття через 2 секунди
        PauseTransition delay = new PauseTransition(Duration.seconds(waitSec));
        delay.setOnFinished(event -> popup.close());
        delay.play();
    }
}
