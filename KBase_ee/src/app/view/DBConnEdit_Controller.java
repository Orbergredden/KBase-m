package app.view;

import java.security.GeneralSecurityException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.crypto.AEADBadTagException;

import app.exceptions.KBase_DbConnEx;
import app.lib.ConvertType;
import app.lib.KeyStorePrg;
import app.lib.ShowAppMsg;
import app.model.DBConn_Parameters;
import app.model.Params;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Контроллер окна добавления/редактирования параметров соединения
 * 
 * @author Igor Makarevich
 */
public class DBConnEdit_Controller {
	private Params params;
	/**
	 *  Ссылка на вызываемый обьект
	 */
    private DBConnList_Controller dbConnListC;
    /**
     * Тип операции : 1 - добавить ; 2 - редактировать
     */
    private int actionType;
    /**
     * Текущая запись (редактируемая) в списке соединений
     */
    private DBConn_Parameters selectedConnPar;
    
    @FXML
	private TextField textField_ConnName;
    @FXML
	private ComboBox<String> comboBox_Type;
    @FXML
	private Label label_Host;
    @FXML
	private TextField textField_Host;
    @FXML
	private TextField textField_Port;
    @FXML
	private TextField textField_Name;
    @FXML
	private TextField textField_Login;
    @FXML
	private PasswordField passwordField_Password;
    @FXML
	private CheckBox checkBox_AutoConn;
    
    @FXML
	private CheckBox checkBox_ColorUse;
    @FXML
	private ColorPicker colorPicker_Text_A;
    @FXML
	private ColorPicker colorPicker_Bgnd_A;
    @FXML
	private Label label_ColorTest_A;
    @FXML
	private ColorPicker colorPicker_Text_N;
    @FXML
	private ColorPicker colorPicker_Bgnd_N;
    @FXML
	private Label label_ColorTest_N;
    
    @FXML
	private Button button_Save;
	@FXML
	private Button button_Cancel;
	
	//
	private ObservableList<String> OList_DBType;
	private Stage dialogStage;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public DBConnEdit_Controller () {
    	
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
    	
    }
    
    /**
     * Вызывается родительским обьектом, которое даёт на себя ссылку.
     * Инициализирует контролы на слое.
     * 
     * @param 
     *        parrentObj
     *        actionType : 1 - добавить ; 2 - редактировать
     *        selectedConnPar текущее соединение в таблице
     */
    public void setParrentObj(Params params, DBConnList_Controller parrentObj, int actionType, DBConn_Parameters selectedConnPar,
    		Stage dialogStage) {
    	this.params = params;
        this.dbConnListC = parrentObj;
        this.actionType = actionType;
        this.dialogStage = dialogStage;
        
        // init controls
        this.selectedConnPar = selectedConnPar;
        initControlsValue(this.selectedConnPar);
    }
    
    /**
     * Инициализирует контролы значениями из главного класса
     */
    private void initControlsValue(DBConn_Parameters selectedConnPar) {
    	
    	// инициализируем ComboBox "Тип БД"
    	OList_DBType = FXCollections.observableArrayList(
    			DBConn_Parameters.TYPE_CONN_POSTGRES,
    			DBConn_Parameters.TYPE_CONN_SQLITE);
    	comboBox_Type.setItems(OList_DBType);
    	
    	// Додаємо слухач змін до ComboBox
    	comboBox_Type.setOnAction(event -> {
    		String selectedValue = comboBox_Type.getValue(); // Отримуємо обране значення
    		
    		if (selectedValue.equals(DBConn_Parameters.TYPE_CONN_POSTGRES)) {
    			label_Host.setText("Сервер");
    			textField_Port.setDisable(false);
    			textField_Name.setDisable(false);
    			textField_Login.setDisable(false);
    			passwordField_Password.setDisable(false);
    		} else {
    			label_Host.setText("Шлях до БД");
    			textField_Port.setDisable(true);
    			textField_Name.setDisable(true);
    			textField_Login.setDisable(true);
    			passwordField_Password.setDisable(true);
    			if (passwordField_Password.getText().isEmpty()) {
    				passwordField_Password.setText("+");
    			}
    		}
        });
    	
    	//
    	if (actionType == 1) {         // add 
    		comboBox_Type.setValue(OList_DBType.get(0));    // первый эдемент в списке
        	colorPicker_Text_A.setValue(Color.BLACK);
        	colorPicker_Bgnd_N.setValue(Color.BLACK);
    	} else if (actionType == 2) {         // update
    		if (selectedConnPar != null) {
    			textField_ConnName.setText(selectedConnPar.getConnName());
    			
    			comboBox_Type.setValue(selectedConnPar.getType());
    			comboBox_Type.setDisable(true);
    			
    			textField_Host.setText(selectedConnPar.getHost());
    			textField_Port.setText(selectedConnPar.getPort());
    			textField_Name.setText(selectedConnPar.getName());
    			textField_Login.setText(selectedConnPar.getLogin());
    			
    			char[] decrypted = null;
    	    	try {
    	   		    decrypted = KeyStorePrg.decrypt(params, selectedConnPar.getPassword());
    	   		    passwordField_Password.setText(new String(decrypted));
    	   		} catch (AEADBadTagException e) {
    	   		    // Неправильний ключ / IV / підробка / невірний AAD
    	   		    // Обробіть як "невалідні дані"
    	   			e.printStackTrace();
    	   			ShowAppMsg.showAlert("ERROR", "Читання паролю", "Помилка читання паролю (AEADBadTagException)", e.getMessage());
    	   		} catch (GeneralSecurityException e) {
    	   			e.printStackTrace();
    	   			ShowAppMsg.showAlert("ERROR", "Читання паролю", "Помилка читання паролю (GeneralSecurityException)", e.getMessage());
    	   		} catch (Exception e) {
    	   			e.printStackTrace();
    	   			ShowAppMsg.showAlert("ERROR", "Читання паролю", "Помилка читання паролю (Exception)", e.getMessage());
    	   		} finally {
    	   		    if (decrypted != null) Arrays.fill(decrypted, '\0'); // затерти, коли більше не потрібен
    	   		}
    			
    			checkBox_AutoConn.setSelected(selectedConnPar.getAutoConn());
    			
    			checkBox_ColorUse.setSelected(selectedConnPar.getColorEnable());
    			colorPicker_Text_A.setValue(new Color(selectedConnPar.getColorTRed_A(),  selectedConnPar.getColorTGreen_A(),
    					                              selectedConnPar.getColorTBlue_A(), selectedConnPar.getColorTOpacity_A())); 
    			colorPicker_Bgnd_A.setValue(new Color(selectedConnPar.getColorBRed_A(),  selectedConnPar.getColorBGreen_A(),
                                                      selectedConnPar.getColorBBlue_A(), selectedConnPar.getColorBOpacity_A()));
    			colorPicker_Text_N.setValue(new Color(selectedConnPar.getColorTRed_N(),  selectedConnPar.getColorTGreen_N(),
                                                      selectedConnPar.getColorTBlue_N(), selectedConnPar.getColorTOpacity_N())); 
    			colorPicker_Bgnd_N.setValue(new Color(selectedConnPar.getColorBRed_N(),  selectedConnPar.getColorBGreen_N(),
                                                      selectedConnPar.getColorBBlue_N(), selectedConnPar.getColorBOpacity_N()));
    		} else {
    			ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбрано соединение", "Закройте окно и выберите соединение для редактирования");
    		}
    	}
    	
    	label_ColorTest_A.setStyle("-fx-text-fill: #"+ ConvertType.colorToHex(colorPicker_Text_A.getValue()) +"; " +
                                   "-fx-background-color: #"+ ConvertType.colorToHex(colorPicker_Bgnd_A.getValue()) +";");
    	label_ColorTest_N.setStyle("-fx-text-fill: #"+ ConvertType.colorToHex(colorPicker_Text_N.getValue()) +"; " +
                                   "-fx-background-color: #"+ ConvertType.colorToHex(colorPicker_Bgnd_N.getValue()) +";");
    	
    	// buttons
    	button_Save.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
    }
    
    /**
     * меняет цвет текста метки-примера (Активные цвета)
     */
    @FXML
    public void colorPicker_Text_A_OnAction(Event t) {
    	label_ColorTest_A.setStyle("-fx-text-fill: #"+ ConvertType.colorToHex(colorPicker_Text_A.getValue()) +"; " +
    			                   "-fx-background-color: #"+ ConvertType.colorToHex(colorPicker_Bgnd_A.getValue()) +";");
    }
    
    /**
     * меняет цвет фона метки-примера (Активные цвета)
     */
    @FXML
    public void colorPicker_Bgnd_A_OnAction(Event t) {
    	label_ColorTest_A.setStyle("-fx-text-fill: #"+ ConvertType.colorToHex(colorPicker_Text_A.getValue()) +"; " +
                                   "-fx-background-color: #"+ ConvertType.colorToHex(colorPicker_Bgnd_A.getValue()) +";");
    }
    
    /**
     * меняет цвет текста метки-примера (НЕ активные цвета)
     */
    @FXML
    public void colorPicker_Text_N_OnAction(Event t) {
    	label_ColorTest_N.setStyle("-fx-text-fill: #"+ ConvertType.colorToHex(colorPicker_Text_N.getValue()) +"; " +
    			                   "-fx-background-color: #"+ ConvertType.colorToHex(colorPicker_Bgnd_N.getValue()) +";");
    }
    
    /**
     * меняет цвет фона метки-примера (Не активные цвета)
     */
    @FXML
    public void colorPicker_Bgnd_N_OnAction(Event t) {
    	label_ColorTest_N.setStyle("-fx-text-fill: #"+ ConvertType.colorToHex(colorPicker_Text_N.getValue()) +"; " +
                                   "-fx-background-color: #"+ ConvertType.colorToHex(colorPicker_Bgnd_N.getValue()) +";");
    }
    
    /**
     * Вызывается при нажатии на кнопке "Сохранить"
     */
    @FXML
    private void handleButtonSave() {
    	int connId;
    	
    	//-------- save stage position
    	Preferences prefs = Preferences.userNodeForPackage(DBConnEdit_Controller.class);
    	prefs.putDouble("stageDBConnEdit_PosX",  dialogStage.getX());
    	prefs.putDouble("stageDBConnEdit_PosY",  dialogStage.getY());

        //---------- check data in fields
    	if ((textField_ConnName.getText().equals("") || (textField_ConnName.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено поле 'Название соединения'", "Укажите значение поля");
    		return;
        }
    	if ((textField_Host.getText().equals("") || (textField_Host.getText() == null))) {
    		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено поле 'Сервер'", "Укажите значение поля");
    		return;
        }
    	if (comboBox_Type.equals(DBConn_Parameters.TYPE_CONN_POSTGRES)) {
    		if ((textField_Port.getText().equals("") || (textField_Port.getText() == null))) {
        		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено поле 'Порт'", "Укажите значение поля");
        		return;
            }
        	if ((textField_Name.getText().equals("") || (textField_Name.getText() == null))) {
        		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено поле 'Имя БД'", "Укажите значение поля");
        		return;
            }
        	if ((textField_Login.getText().equals("") || (textField_Login.getText() == null))) {
        		ShowAppMsg.showAlert("WARNING", "Нет данных", "Не заполнено поле 'Логин'", "Укажите значение поля");
        		return;
            }
    	}
    	
        //-------- Сохраняем параметры соединения
    	if (selectedConnPar == null) {
    		connId = 0;
    	} else {
    		connId = selectedConnPar.getConnId();
    	}
    	
    	char[] password = passwordField_Password.getText().toCharArray();
		KeyStorePrg.EncBlob blob = null;
		try {
			blob = KeyStorePrg.encrypt(params, password, "aes-v1", "db-1");
			dbConnListC.edit(actionType, 
			         new DBConn_Parameters(connId, textField_ConnName.getText(), comboBox_Type.getValue(), 
			        		               textField_Host.getText(), textField_Port.getText(), textField_Name.getText(), 
			        		               textField_Login.getText(), blob,
			        		               checkBox_AutoConn.isSelected() ,null, 0, checkBox_ColorUse.isSelected(),
			                               colorPicker_Text_A.getValue().getRed(), colorPicker_Text_A.getValue().getGreen(),
			                               colorPicker_Text_A.getValue().getBlue(),colorPicker_Text_A.getValue().getOpacity(),
			                               colorPicker_Bgnd_A.getValue().getRed(), colorPicker_Bgnd_A.getValue().getGreen(),
			                               colorPicker_Bgnd_A.getValue().getBlue(),colorPicker_Bgnd_A.getValue().getOpacity(),
			                               colorPicker_Text_N.getValue().getRed(), colorPicker_Text_N.getValue().getGreen(),
			                               colorPicker_Text_N.getValue().getBlue(),colorPicker_Text_N.getValue().getOpacity(),
			                               colorPicker_Bgnd_N.getValue().getRed(), colorPicker_Bgnd_N.getValue().getGreen(),
			                               colorPicker_Bgnd_N.getValue().getBlue(),colorPicker_Bgnd_N.getValue().getOpacity())
			         );
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("ERROR", "Запис паролю", "Помилка запису паролю (GeneralSecurityException)", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("ERROR", "Запис паролю", "Помилка запису паролю (Exception)", e.getMessage());
		}
		// Важливо: затерти пароль після використання в коді, де ви його сформували
		Arrays.fill(password, '\0');
        
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
    	Preferences prefs = Preferences.userNodeForPackage(DBConnEdit_Controller.class);
    	prefs.putDouble("stageDBConnEdit_PosX",  dialogStage.getX());
    	prefs.putDouble("stageDBConnEdit_PosY",  dialogStage.getY());
    	
        // get a handle to the stage
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
