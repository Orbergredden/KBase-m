package app.view.business;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;

import app.lib.FileUtil;
import app.model.StateItem;
import app.model.StateList;
import app.model.business.Info_ImageItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
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
import javafx.stage.FileChooser;

/**
 * Контроллер инфо блока "Изображение"
 * @author Igor Makarevich
 */
public class InfoEdit_Image_Controller extends InfoEdit_Simple_Controller {
	
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
	private TextField textField_width;
	@FXML
	private TextField textField_height;
	@FXML
	private Label label_width;
	@FXML
	private Label label_height;
	@FXML
	private Button button_LoadFromFile;
	@FXML
	private Button button_CopyToClipboard;
	@FXML
	private Button button_PasteFromClipboard;
	@FXML
	private ImageView imageView_Preview;
	
	@FXML
	private SplitPane splitPane_Main;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public InfoEdit_Image_Controller () {
    	super();
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {       }
	
    /**
     * Инициализирует контролы значениями  
     */
    public void initControlsValue() {       
    	Info_ImageItem iti = conn.db.info_ImageGet(infoId);
	
		checkBox_isShowTitle.setSelected(iti.getIsShowTitle() > 0);
    	textField_title.setText(iti.getTitle());
    	checkBox_isShowDescr.setSelected(iti.getIsShowDescr() > 0);
    	textField_descr.setText(iti.getDescr());
    	checkBox_isShowText.setSelected(iti.getIsShowText() > 0);
    	textArea_text.setText(iti.getText());
    	textArea_text.setWrapText(true);

    	textField_width.setText(Integer.toString(iti.getWidth()));
    	textField_height.setText(Integer.toString(iti.getHeight()));
    	label_width.setText(Integer.toString((int)iti.image.getWidth()));
    	label_height.setText(Integer.toString((int)iti.image.getHeight()));
    	imageView_Preview.setImage(iti.image);
    	
    	button_LoadFromFile.setTooltip(new Tooltip("Загрузить картинку из файла..."));
    	button_LoadFromFile.setGraphic(new ImageView(new Image("file:resources/images/icon_file_open_16.png")));
    	button_CopyToClipboard.setTooltip(new Tooltip("Копируем картинку в системный буфер обмена"));
    	button_CopyToClipboard.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_16.png")));
    	button_PasteFromClipboard.setTooltip(new Tooltip("Вставляем картинку из системного буфера обмена"));
    	button_PasteFromClipboard.setGraphic(new ImageView(new Image("file:resources/images/icon_paste_16.png")));
    	
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
        
        textField_width.textProperty().addListener((observable, oldValue, newValue) -> {
            parrentObj.setIsChanged(true);
        });
        textField_height.textProperty().addListener((observable, oldValue, newValue) -> {
            parrentObj.setIsChanged(true);
        });
        
        imageView_Preview.imageProperty().addListener((observable, oldValue, newValue) -> {
        	parrentObj.setIsChanged(true);
        	label_width.setText(Integer.toString((int)imageView_Preview.getImage().getWidth()));
        	label_height.setText(Integer.toString((int)imageView_Preview.getImage().getHeight()));
        });
    }

	/**
	 * Проверка введенных значений
	 */
	public void check()  {     }
	
	/**
	 * getImageFromClipboard()
	 */
	private java.awt.Image getImageFromClipboard() {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {
                return (java.awt.Image) transferable.getTransferData(DataFlavor.imageFlavor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
	
	/**
	 * awtImageToFX()
	 */
	private static javafx.scene.image.Image awtImageToFX(java.awt.Image image) throws Exception {
        if (!(image instanceof RenderedImage)) {
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();

            image = bufferedImage;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) image, "png", out);
        out.flush();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new javafx.scene.image.Image(in);
    }
	
	/**
     * Выбор и загрузка изображения из файла
     */
    @FXML
    public void handleButtonLoadFromFile() {
    	FileChooser fileChooser = new FileChooser();
    	Preferences prefs = Preferences.userNodeForPackage(InfoEdit_Image_Controller.class);
    	String curDir;

        // Задаём фильтр расширений
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        
        // set directory
        //prefs.remove("icons_CurDirNameForAdd");
        curDir = prefs.get("InfoEditImage_CurDirNameForLoad", "");
        //System.out.println("curDir = " + curDir);
        if (! curDir.equals("")) {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory()) {
                fileChooser.setInitialDirectory(dir);
            }
        }
        
        // Показываем диалог загрузки файла
        //File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
        	// обновляем контролы
        	//Image img = new Image(file.toURI().toString(), 20, 20, false, false);
        	Image img = new Image(file.toURI().toString());           // not resize
        	imageView_Preview.setImage(img);
        	label_width.setText(Double.toString(img.getWidth()));
        	label_height.setText(Double.toString(img.getHeight()));
        	
        	// save dir name
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("InfoEditImage_CurDirNameForLoad", curDir);
        }
    }
    
    /**
     * Копируем картинку в системный буфер обмена
     */
    @FXML
    public void handleButtonCopyToClipboard() {
    	Clipboard clipboard = Clipboard.getSystemClipboard();
    	ClipboardContent content = new ClipboardContent();
    	// for paste as image, e.g. in GIMP
    	content.putImage(imageView_Preview.getImage()); // the image you want, as javafx.scene.image.Image
    	// for paste as file, e.g. in Windows Explorer
    	//content.putFiles(java.util.Collections.singletonList(new File("C:\\Users\\Admin\\Desktop\\my\\mysql.gif")));
    	clipboard.setContent(content);
    }
    
    /**
     * Вставляем картинку из системного буфера обмена
     */
    @FXML
    public void handleButtonPasteFromClipboard() {
    	
    	try {
            java.awt.Image image = getImageFromClipboard();
            if (image != null) {
                javafx.scene.image.Image fimage = awtImageToFX(image);
                imageView_Preview.setImage(fimage);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
		return "InfoEdit_Image_Controller";
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
	public void save ()    {
		Info_ImageItem iii = new Info_ImageItem (
				infoId,
				textField_title.getText(),
				imageView_Preview.getImage(),
				Integer.valueOf(textField_width.getText()),
				Integer.valueOf(textField_height.getText()),
				textField_descr.getText(),
				textArea_text.getText(),
				(checkBox_isShowTitle.isSelected()) ? 1 : 0,
				(checkBox_isShowDescr.isSelected()) ? 1 : 0,
				(checkBox_isShowText.isSelected()) ? 1 : 0
				);
		
		String fileName =
				FileUtil.getFullUserFileName(params, params.getConfig().getItemValue("directories", "PathDirCache")) +
				"tmp.png";
		FileUtil.writeImageFile(fileName, iii.image);
		conn.db.info_ImageUpdate(iii, fileName);
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
