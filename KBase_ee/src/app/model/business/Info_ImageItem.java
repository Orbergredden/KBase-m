
package app.model.business;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import app.exceptions.KBase_ReadTextFileUTFEx;
import app.lib.FileUtil;
import app.lib.ShowAppMsg;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

/*
 * Класс содержит тело информационного блока документа типа "Изображение"
 * 
 * @author Игорь Макаревич
 */
public class Info_ImageItem extends SimpleItem implements Serializable {
	/**
	 * Заголовок инфо блока
	 */
	private transient StringProperty title;
	/**
	 * Изображение
	 */
	public transient Image image;
	/**
	 * Ширина изображения, если не указана, то оригинальная
	 */
	private transient IntegerProperty width;
	/**
	 * Высота изображения, если не указана, то оригинальная
	 */
	private transient IntegerProperty height;
	/**
	 * Текст инфоблока
	 */
	private transient StringProperty text;
	/**
	 * 1 - показывать заголовок ; 0 или NULL - не показывать
	 */
	private transient IntegerProperty isShowTitle;
	/**
	 * 1 - показывать описание ; 0 или NULL - не показывать
	 */
	private transient IntegerProperty isShowDescr;
	/**
	 * 1 - показывать текст ; 0 или NULL - не показывать
	 */
	private transient IntegerProperty isShowText;
	
	// for Serialization
	private String title_ser;
	private int width_ser;
	private int height_ser;
	private String text_ser;
	private int isShowTitle_ser;
	private int isShowDescr_ser;
	private int isShowText_ser;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public Info_ImageItem() {
		this(0, null, null, 0, 0, null, null, 0, 0, 0);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public Info_ImageItem(long id, String title, 
			              Image image, int width, int height, String descr, String text, 
			              int isShowTitle, int isShowDescr, int isShowText) {
		super(id, null, descr);
		this.title       = new SimpleStringProperty(title);
		this.image       = image;
		this.width       = new SimpleIntegerProperty(width);
		this.height      = new SimpleIntegerProperty(height);
		this.text        = new SimpleStringProperty(text);
		this.isShowTitle = new SimpleIntegerProperty(isShowTitle);
		this.isShowDescr = new SimpleIntegerProperty(isShowDescr);
		this.isShowText  = new SimpleIntegerProperty(isShowText);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public Info_ImageItem(
			long id, String title, 
            Image image, int width, int height, String descr, String text, 
            int isShowTitle, int isShowDescr, int isShowText,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, null, descr, dateCreated, dateModified, userCreated, userModified);
		this.title       = new SimpleStringProperty(title);
		this.image       = image;
		this.width       = new SimpleIntegerProperty(width);
		this.height      = new SimpleIntegerProperty(height);
		this.text        = new SimpleStringProperty(text);
		this.isShowTitle = new SimpleIntegerProperty(isShowTitle);
		this.isShowDescr = new SimpleIntegerProperty(isShowDescr);
		this.isShowText  = new SimpleIntegerProperty(isShowText);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public Info_ImageItem(Info_ImageItem item) {
		super((SimpleItem)item);
		this.title       = new SimpleStringProperty(item.getTitle());
		this.image       = item.image;
		this.width       = new SimpleIntegerProperty(item.getWidth());
		this.height      = new SimpleIntegerProperty(item.getHeight());
		this.text        = new SimpleStringProperty(item.getText());
		this.isShowTitle = new SimpleIntegerProperty(item.getIsShowTitle());
		this.isShowDescr = new SimpleIntegerProperty(item.getIsShowDescr());
		this.isShowText  = new SimpleIntegerProperty(item.getIsShowText());
	}
	
	// title  -- g,s,p
	public String getTitle() {
	    return title.get();
	}
	public void setTitle(String title) {
	    this.title.set(title);
	}
	public StringProperty titleProperty() {
	    return title;
	}
	
	// width  -- g,s,p
	public int getWidth() {
	    return width.get();
	}
	public void setWidth(int width) {
	    this.width.set(width);
	}
	public IntegerProperty widthProperty() {
	    return width;
	}
	
	// height  -- g,s,p
	public int getHeight() {
	    return height.get();
	}
	public void setHeight(int height) {
	    this.height.set(height);
	}
	public IntegerProperty heightProperty() {
	    return height;
	}
	
	// text  -- g,s,p
	public String getText() {
	    return text.get();
	}
	public void setText(String text) {
	    this.text.set(text);
	}
	public StringProperty textProperty() {
	    return text;
	}
	
	// isShowTitle  -- g,s,p
	public int getIsShowTitle() {
	    return isShowTitle.get();
	}
	public void setIsShowTitle(int isShowTitle) {
	    this.isShowTitle.set(isShowTitle);
	}
	public IntegerProperty isShowTitleProperty() {
	    return isShowTitle;
	}
	
	// isShowDescr  -- g,s,p
	public int getIsShowDescr() {
	    return isShowDescr.get();
	}
	public void setIsShowDescr(int isShowDescr) {
	    this.isShowDescr.set(isShowDescr);
	}
	public IntegerProperty isShowDescrProperty() {
	    return isShowDescr;
	}
	
	// isShowText  -- g,s,p
	public int getIsShowText() {
	    return isShowText.get();
	}
	public void setIsShowText(int isShowText) {
	    this.isShowText.set(isShowText);
	}
	public IntegerProperty isShowTextProperty() {
	    return isShowText;
	}
	
	/**
	 * серіалізація цього обьекту
	 */
	public void serialize (String path, String fileName, String fileNameImage) {
		doBeforeSerialization ();

		FileUtil.writeImageFile(path + fileNameImage, image);
		
		try (ObjectOutputStream outputStream = 
        		new ObjectOutputStream(new FileOutputStream(path+fileName))) {
            outputStream.writeObject(this);
        } catch (IOException e) {
        	ShowAppMsg.showAlert("WARNING", "Копіювання у буфер обміну", 
    				"\"" + path+fileName + "\"", 
    				"Помилка запису файла.");
            e.printStackTrace();
        }
	}
	
	/**
	 * десеріалізація обьекту
	 */
	public static Info_ImageItem unserialize (String path, String fileName, String fileNameImage) {
		Info_ImageItem ii = null;
		
		try (ObjectInputStream inputStream = 
    			new ObjectInputStream(new FileInputStream(path+fileName))) {
            ii = (Info_ImageItem) inputStream.readObject();
    	} catch (IOException e) {
        	ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", 
    				"\"" + path+fileName + "\"", 
    				"Помилка читання файла.");
    	} catch (ClassNotFoundException e) {
    		ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", "Буфер пустий.", "");
    		return null;
        }
		
		try {
			ii.image = FileUtil.readImageFile (path + fileNameImage);
		} catch (KBase_ReadTextFileUTFEx e) {
			//e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "Читання файла з диска", e.msg, e.fileName);
		}
		
		ii.doAfterSerialization();
		
		return ii;
	}
	
	/**
	 * Потрібно запускати до серіалізації
	 */
	@Override
	public void doBeforeSerialization () {
		super.doBeforeSerialization();
		
		title_ser = getTitle();
		width_ser = getWidth();
		height_ser = getHeight();
		text_ser = getText();
		isShowTitle_ser = getIsShowTitle();
		isShowDescr_ser = getIsShowDescr();
		isShowText_ser = getIsShowText();
	}
	
	/**
	 * Потрібно запускати після серіалізації
	 */
	@Override
	public void doAfterSerialization () {
		super.doAfterSerialization();
		
		this.title            = new SimpleStringProperty(title_ser);
		this.width            = new SimpleIntegerProperty(width_ser);
		this.height           = new SimpleIntegerProperty(height_ser);
		this.text             = new SimpleStringProperty(text_ser);
		this.isShowTitle      = new SimpleIntegerProperty(isShowTitle_ser);
		this.isShowDescr      = new SimpleIntegerProperty(isShowDescr_ser);
		this.isShowText       = new SimpleIntegerProperty(isShowText_ser);
	}
}
