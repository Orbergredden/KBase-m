package app.view.business;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import app.Main;
import app.lib.FileUtil;
import app.lib.ShowAppMsg;
import app.model.Params;
import app.model.StateItem;
import app.model.StateList;
import app.model.business.IconItem;
import app.model.business.Info_FileItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Контроллер инфо блока "Файл"
 * @author Igor Makarevich
 */
public class InfoEdit_File_Controller extends InfoEdit_Simple_Controller {
	
	private Info_FileItem ifi;
	private IconItem ii;

	@FXML
	private CheckBox checkBox_isShowTitle;
	@FXML
	private TextField textField_title;
	@FXML
	private CheckBox checkBox_isShowDescr;
	@FXML
	private TextField textField_descr;
	@FXML
	private CheckBox checkBox_isShowText;
	@FXML
	private TextArea textArea_text;
	
	@FXML
	private Button button_LoadFromFile;
	@FXML
	private Button button_SaveToFile;
	@FXML
	private TextField textField_FileName;
	@FXML
	private Label label_FileSize;
	
	@FXML
	private Label label_IconWidth;
	@FXML
	private Label label_IconHeight;
	@FXML
	private Button button_LoadIconFromImageList;
	@FXML
	private Button button_CopyIconToClipboard;
	@FXML
	private ImageView imageView_PreviewIcon;
	
	@FXML
	private SplitPane splitPane_Main;
	
	// полный путь выбранного нового файла (button LoadFromFile)
	private String filePath;
	
	private Preferences prefs;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public InfoEdit_File_Controller () {
    	super();
    	
    	prefs = Preferences.userNodeForPackage(InfoEdit_File_Controller.class);
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
    	
    }
	
    /**
     * Инициализирует контролы значениями  
     */
    public void initControlsValue() {       
    	ifi = conn.db.info_FileGet(infoId);
    	ii = conn.db.iconGetById(ifi.getIconId());
	
		checkBox_isShowTitle.setSelected(ifi.getIsShowTitle() > 0);
    	textField_title.setText(ifi.getTitle());
    	checkBox_isShowDescr.setSelected(ifi.getIsShowDescr() > 0);
    	textField_descr.setText(ifi.getDescr());
    	checkBox_isShowText.setSelected(ifi.getIsShowText() > 0);
    	textArea_text.setText(ifi.getText());
    	textArea_text.setWrapText(true);

    	button_LoadFromFile.setTooltip(new Tooltip("Load from file..."));
    	button_LoadFromFile.setGraphic(new ImageView(new Image("file:resources/images/icon_file_open_16.png")));
    	button_SaveToFile.setTooltip(new Tooltip("Save to file..."));
    	button_SaveToFile.setGraphic(new ImageView(new Image("file:resources/images/icon_SaveToFile_16.png")));
    	textField_FileName.setText(ifi.getName());
    	label_FileSize.setText(Integer.toString(ifi.getFileBody().length));
    	
    	if (ii != null) {
    		label_IconWidth.setText(Integer.toString((int)ii.image.getWidth()));
    		label_IconHeight.setText(Integer.toString((int)ii.image.getHeight()));
    		imageView_PreviewIcon.setImage(ii.image);
    	}
    	button_LoadIconFromImageList.setTooltip(new Tooltip("Load icon from Image List..."));
    	button_LoadIconFromImageList.setGraphic(new ImageView(new Image("file:resources/images/icon_CatalogIcons_16.png")));
    	button_CopyIconToClipboard.setTooltip(new Tooltip("Copy icon to system clipboard"));
    	button_CopyIconToClipboard.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_16.png")));
    	
		//======== define listeners
        checkBox_isShowTitle.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
                parrentObj.setIsChanged(true);
            }
        });
        textField_title.textProperty().addListener((observable, oldValue, newValue) -> {
            parrentObj.setIsChanged(true);
		});
        
        checkBox_isShowDescr.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
                parrentObj.setIsChanged(true);
            }
        });
        textField_descr.textProperty().addListener((observable, oldValue, newValue) -> {
            parrentObj.setIsChanged(true);
		});

        checkBox_isShowText.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
                parrentObj.setIsChanged(true);
            }
        });
        textArea_text.textProperty().addListener((observable, oldValue, newValue) -> {
            parrentObj.setIsChanged(true);
        });
        
        textField_FileName.textProperty().addListener((observable, oldValue, newValue) -> {
            parrentObj.setIsChanged(true);
		});
        
    	imageView_PreviewIcon.imageProperty().addListener((observable, oldValue, newValue) -> {
        	parrentObj.setIsChanged(true);
        	label_IconWidth.setText(Integer.toString((int)imageView_PreviewIcon.getImage().getWidth()));
        	label_IconHeight.setText(Integer.toString((int)imageView_PreviewIcon.getImage().getHeight()));
        });
    }
	
    /**
	 * Проверка введенных значений
	 */
	public void check()  {     }
	
	/**
     * Select and load from file
     */
    @FXML
    public void handleButtonLoadFromFile() {
    	FileChooser fileChooser = new FileChooser();
    	//Preferences prefs = Preferences.userNodeForPackage(InfoEdit_File_Controller.class);
    	String curDir;

        // Задаём фильтр расширений
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);
        
        // set directory
        curDir = prefs.get("InfoEditFile_CurDirNameForLoad", "");
        if (! curDir.equals("")) {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory()) {
                fileChooser.setInitialDirectory(dir);
            }
        }
        
        // Показываем диалог загрузки файла
        //File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
        //File file = fileChooser.showOpenDialog(null);
        File file = fileChooser.showOpenDialog(params.getStageCur());

        if (file != null) {
        	// обновляем контролы
        	//filePath = file.toURI().toString();
        	filePath = file.getAbsolutePath();
        	textField_FileName.setText(file.getName());
        	label_FileSize.setText(Long.toString(file.length()));
        	
        	// save dir name
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("InfoEditFile_CurDirNameForLoad", curDir);
        }
    }
    
    /**
     * Select and save to file
     */
    @FXML
    public void handleButtonSaveToFile() {
    	FileChooser fileChooser = new FileChooser();
    	String curDir;
    	
    	//---- check file availability
    	if ((filePath == null) && (ifi.getName().length() == 0)) {
    		ShowAppMsg.showAlert("WARNING", "Message", "No file to save", "");
    		return;
    	}
    	
    	//---- open dialog SaveFile
    	fileChooser.setTitle("Save file to disk");
    	fileChooser.setInitialFileName(textField_FileName.getText());
    	
    	// Задаём фильтр расширений
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);
        
        // set directory
        curDir = prefs.get("InfoEditFile_CurDirNameForLoad", "");
        if (! curDir.equals("")) {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory()) {
                fileChooser.setInitialDirectory(dir);
            }
        }
        
        // Показываем диалог загрузки файла
        //File file = fileChooser.showOpenDialog(params.getStageCur());
        File file = fileChooser.showSaveDialog(params.getStageCur());
    	
    	//-------- save file
        if (file != null) {
        	if (filePath == null) {         // get source file from DB
        		FileUtil.writeBinaryFile(file.getAbsolutePath(), ifi.getFileBody());
        	} else {                        // get source file from disk
        		//ShowAppMsg.showAlert("ERROR", "TEST", "Test copy file", "from '"+filePath+"' to '"+file.getAbsolutePath()+"'");
        		
        		try {
					FileUtil.copyFile(new File(filePath), file);
				} catch (IOException e) {
					ShowAppMsg.showAlert("ERROR", "Error copy file", "Error copy file", 
							"from '"+filePath+"' to '"+file.getAbsolutePath()+"'");
					//e.printStackTrace();
				}
        	}
    	
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("InfoEditFile_CurDirNameForLoad", curDir);
        }
    }
	
    /**
     * Load icon from Image List
     */
    @FXML
    public void handleButtonLoadIconFromImageList() {
    	
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/IconSelect_Layout.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Выбор пиктограммы");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getStageCur());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_CatalogIcons_16.png"));
			
			dialogStage.setWidth(prefs.getDouble("stageIconSelect_Width", 500));
			dialogStage.setHeight(prefs.getDouble("stageIconSelect_Height", 600));
			dialogStage.setX(prefs.getDouble("stageIconSelect_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageIconSelect_PosY", 0));
			
			// Даём новому контроллеру доступ к парамтрам приложения
			IconSelect_Controller controller = loader.getController();
			
			Params params = new Params (this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
			controller.setParams(
					params, 
					(ii != null) ? ii.getId() : 0, 
					0);

	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        if (controller.isSelected) {
	        	ii = conn.db.iconGetById(controller.iconIdRet);
	        	imageView_PreviewIcon.setImage(conn.db.iconGetImageById(ii.getId()));
	        	label_IconWidth.setText(Double.toString(ii.image.getWidth()));
	        	label_IconHeight.setText(Double.toString(ii.image.getHeight()));
	        }
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    /**
     * Копируем картинку в системный буфер обмена
     */
    @FXML
    public void handleButtonCopyIconToClipboard() {
    	Clipboard clipboard = Clipboard.getSystemClipboard();
    	ClipboardContent content = new ClipboardContent();
    	// for paste as image, e.g. in GIMP
    	content.putImage(imageView_PreviewIcon.getImage()); // the image you want, as javafx.scene.image.Image
    	// for paste as file, e.g. in Windows Explorer
    	//content.putFiles(java.util.Collections.singletonList(new File("C:\\Users\\Admin\\Desktop\\my\\mysql.gif")));
    	clipboard.setContent(content);
    }
    
    /**
	 * уникальный ИД обьекта
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
		return "InfoEdit_File_Controller";
	}
	
	/**
	 * контроллер элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public Object getController() {
		return this;
	}
    
	/**
     * Сохранение информации
     */
	public void save () {
		
		// check
		if ((textField_FileName == null) || (textField_FileName.getText().length() == 0)) {
    		ShowAppMsg.showAlert("INFORMATION", "Message", "File Name is empty", "");
    		return;
    	}
		
		// create object
		Info_FileItem ifi = new Info_FileItem (
				this.ifi.getId(),
				textField_title.getText(),
				(filePath == null) ? this.ifi.getFileBody() : FileUtil.readBinaryFile(filePath),
				textField_FileName.getText(),
				ii.getId(),
				textField_descr.getText(),
				textArea_text.getText(),
				(checkBox_isShowTitle.isSelected()) ? 1 : 0,
				(checkBox_isShowDescr.isSelected()) ? 1 : 0,
				(checkBox_isShowText.isSelected()) ? 1 : 0
				);
		
		// save file
		String fileName =
				FileUtil.getFullUserFileName(params, params.getConfig().getItemValue("directories", "PathDirCache")) +
				"tmp.bin";
		FileUtil.writeBinaryFile(fileName, ifi.getFileBody());
		conn.db.info_FileUpdate(ifi, fileName);
	}
    
	/**
	 * Реализуем метод интерфейса AppItem_Interface.            <br>
	 * Сохраняем состояние контролов в иерархической структуре
	 */
	public void saveControlsState (StateList stateList) {
		
		stateList.add(
				"splitPane_Main_Position",
				String.valueOf(splitPane_Main.getDividerPositions()[0]),
				null);
		stateList.add(
				"caretPosition",
				Integer.toString(textArea_text.getCaretPosition()),
				null);
	}
    
	/**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Восстанавливаем состояние контролов из иерархической структуры
	 */
	public void restoreControlsState (StateList stateList) {
		
		for (StateItem si : stateList.list) {
			switch (si.getName()) {
				case "splitPane_Main_Position" :
					splitPane_Main.setDividerPositions(Double.parseDouble(si.getParams()));  
					break;
				case "caretPosition" :
					textArea_text.positionCaret(Integer.parseInt(si.getParams()));
					break;
			}
		}
	}
}
