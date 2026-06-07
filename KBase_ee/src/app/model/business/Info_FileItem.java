package app.model.business;

import java.io.File;
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
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/*
 * Класс содержит тело информационного блока документа типа "Файл"
 * 
 * @author Игорь Макаревич
 */
public class Info_FileItem extends SimpleItem implements Serializable {
	/**
	 * Заголовок инфо блока
	 */
	private transient StringProperty title;
	/**
	 * тело файла
	 */
	private byte[] fileBody;
	/**
	 * Имя файла
	 */
	//private final StringProperty fileName; -- берем из SimpleItem.name
	/**
	 * иконка для показа типа файла 
	 */
	private transient LongProperty iconId;
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
	private long iconId_ser;
	private String text_ser;
	private int isShowTitle_ser;
	private int isShowDescr_ser;
	private int isShowText_ser;

	/**
	 * Конструктор по умолчанию.
	 */
	public Info_FileItem() {
		this(0, null, null, null, 0, null, null, 0, 0, 0);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public Info_FileItem(long id, String title, byte[] fileBody, String fileName,
			             long iconId, String descr, String text, 
			             int isShowTitle, int isShowDescr, int isShowText) {
		super(id, fileName, descr);
		this.title       = new SimpleStringProperty(title);
		this.fileBody    = fileBody;
		this.iconId      = new SimpleLongProperty(iconId);
		this.text        = new SimpleStringProperty(text);
		this.isShowTitle = new SimpleIntegerProperty(isShowTitle);
		this.isShowDescr = new SimpleIntegerProperty(isShowDescr);
		this.isShowText  = new SimpleIntegerProperty(isShowText);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public Info_FileItem(long id, String title, byte[] fileBody, String fileName,
            long iconId, String descr, String text, 
            int isShowTitle, int isShowDescr, int isShowText,
            Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, fileName, descr, dateCreated, dateModified, userCreated, userModified);
		this.title       = new SimpleStringProperty(title);
		this.fileBody    = fileBody;
		this.iconId      = new SimpleLongProperty(iconId);
		this.text        = new SimpleStringProperty(text);
		this.isShowTitle = new SimpleIntegerProperty(isShowTitle);
		this.isShowDescr = new SimpleIntegerProperty(isShowDescr);
		this.isShowText  = new SimpleIntegerProperty(isShowText);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public Info_FileItem(Info_FileItem item) {
		super((SimpleItem)item);
		this.title       = new SimpleStringProperty(item.getTitle());
		this.fileBody    = item.getFileBody();
		this.iconId      = new SimpleLongProperty(item.getIconId());
		this.text        = new SimpleStringProperty(item.getText());
		this.isShowTitle = new SimpleIntegerProperty(item.getIsShowTitle());
		this.isShowDescr = new SimpleIntegerProperty(item.getIsShowDescr());
		this.isShowText  = new SimpleIntegerProperty(item.getIsShowText());
	}
	
	/**
	 * Write item to file.
	 * @param filePath
	 * @throws IOException
	 */
	public void saveToFile (String filePath) throws IOException {
		File file = new File(filePath);
		
    	if (file != null) {
    		FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileBody, 0, fileBody.length);
   			fos.close();
		}
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
	
	// title  -- g,s
	public byte[] getFileBody() {
	    return fileBody;
	}
	public void setFileBody(byte[] fileBody) {
	    this.fileBody = fileBody;
	}
	
	// iconId  -- g,s,p
	public long getIconId() {
	    return iconId.get();
	}
	public void setIconId(long iconId) {
	    this.iconId.set(iconId);
	}
	public LongProperty idProperty() {
	    return iconId;
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
	public void serialize (String path, String fileName) {
		doBeforeSerialization ();

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
	public static Info_FileItem unserialize (String path, String fileName) {
		Info_FileItem fi = null;
		
		try (ObjectInputStream inputStream = 
    			new ObjectInputStream(new FileInputStream(path+fileName))) {
            fi = (Info_FileItem) inputStream.readObject();
    	} catch (IOException e) {
        	ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", 
    				"\"" + path+fileName + "\"", 
    				"Помилка читання файла.");
    	} catch (ClassNotFoundException e) {
    		ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", "Буфер пустий.", "");
    		return null;
        }
		
		fi.doAfterSerialization();
		
		return fi;
	}
	
	/**
	 * Потрібно запускати до серіалізації
	 */
	@Override
	public void doBeforeSerialization () {
		super.doBeforeSerialization();
		
		title_ser = getTitle();
		iconId_ser = getIconId();
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
		this.iconId           = new SimpleLongProperty(iconId_ser);
		this.text             = new SimpleStringProperty(text_ser);
		this.isShowTitle      = new SimpleIntegerProperty(isShowTitle_ser);
		this.isShowDescr      = new SimpleIntegerProperty(isShowDescr_ser);
		this.isShowText       = new SimpleIntegerProperty(isShowText_ser);
	}
}
