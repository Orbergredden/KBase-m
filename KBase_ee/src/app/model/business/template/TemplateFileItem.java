
package app.model.business.template;

import app.exceptions.KBase_ReadTextFileUTFEx;
import app.lib.FileUtil;
import app.lib.ShowAppMsg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

/**
 * Класс содержит информацию об одном файле (или файловой директории) темы шаблонов.
 * @author Igor Makarevich
 * @version 2.00.00.001   06.04.2021
 */
public class TemplateFileItem extends TemplateSimpleItem implements Serializable {
	public static final int FILE_TYPE_DIR      = 0;
	public static final int FILE_TYPE_TEXT     = 1;
	public static final int FILE_TYPE_IMAGE    = 2;
	public static final int FILE_TYPE_BINARY   = 3;
	
	/**
	 * Id родительской файловой директории или 0, если файл (дирктория) находится в корне
	 */
	private transient LongProperty parentId;
	/**
	 * 0-файл, 1-директория ; (для необязательных файлов) 10 - файл, 11 - директория
	 */
	private transient IntegerProperty type;
	/**
	 * Тип файла : 1 - текстовый ; 2 - картинка ; 3 - бинарный
	 */
	private transient IntegerProperty fileType;
	
	private transient StringProperty fileName;
	/**
	 * Текст
	 */
	private transient StringProperty body;
	/**
	 * Картинка
	 */
	public transient Image bodyImage;
	
	// for Serialization
	private long parentId_ser;
	private int type_ser;
	private int fileType_ser;
	private String fileName_ser;
	private String body_ser;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TemplateFileItem() {
		this(0, 0, 0, 0, 0, null, null, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public TemplateFileItem(
			long id, long parentId, long themeId, int type, int fileType, String fileName, String descr, 
			String body, Image bodyImage) {
		super(id, fileName, descr, themeId, 
				((type < 10) ? ((type == 0) ? TYPE_ITEM_FILE : TYPE_ITEM_DIR_FILE) : 
					((type == 10) ? TYPE_ITEM_FILE_OPTIONAL : TYPE_ITEM_DIR_FILE_OPTIONAL)), 
				type);
		flag = new SimpleLongProperty(fileType);

		this.parentId         = new SimpleLongProperty(parentId);
		this.type             = new SimpleIntegerProperty(type);
		this.fileType         = new SimpleIntegerProperty(fileType);
		this.fileName         = new SimpleStringProperty(fileName);
		this.body             = new SimpleStringProperty(body);
		this.bodyImage        = bodyImage;
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public TemplateFileItem(
			long id, long parentId, long themeId, int type, int fileType, String fileName, String descr, 
			String body, Image bodyImage, 
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, fileName, descr, themeId, 
				((type < 10) ? ((type == 0) ? TYPE_ITEM_FILE : TYPE_ITEM_DIR_FILE) : 
					((type == 10) ? TYPE_ITEM_FILE_OPTIONAL : TYPE_ITEM_DIR_FILE_OPTIONAL)), 
				type,
			  dateCreated, dateModified, userCreated, userModified);
		flag = new SimpleLongProperty(fileType);

		this.parentId         = new SimpleLongProperty(parentId);
		this.type             = new SimpleIntegerProperty(type);
		this.fileType         = new SimpleIntegerProperty(fileType);
		this.fileName         = new SimpleStringProperty(fileName);
		this.body             = new SimpleStringProperty(body);
		this.bodyImage        = bodyImage;
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TemplateFileItem(TemplateFileItem item) {
		super((TemplateSimpleItem)item);
		this.parentId     = new SimpleLongProperty(item.getParentId());
		this.type         = new SimpleIntegerProperty(item.getType());
		this.fileType     = new SimpleIntegerProperty(item.getFileType());
		this.fileName     = new SimpleStringProperty(item.getFileName());
		this.body         = new SimpleStringProperty(item.getBody());
		this.bodyImage    = item.bodyImage;
	}
	
	// parentId  -- g,s,p
	public long getParentId() {
	    return parentId.get();
	}
	public void setParentId(long parentId) {
	    this.parentId.set(parentId);
	}
	public LongProperty parentIdProperty() {
	    return parentId;
	}
	
	// type  -- g,s,p
	public int getType() {
	    return type.get();
	}
	public void setType(int type) {
		setSubtypeItem(type);
		this.type.set(type);
	}
	public IntegerProperty typeProperty() {
	    return type;
	}
	
	// fileType  -- g,s,p
	public int getFileType() {
	    return fileType.get();
	}
	public void setFileType(int fileType) {
	    this.fileType.set(fileType);
	}
	public IntegerProperty fileTypeProperty() {
	    return fileType;
	}
	
	// fileName  -- g,s,p
	public String getFileName() {
		return fileName.get();
	}
	public void setFileName(String fileName) {
		this.fileName.set(fileName);
	}
	public StringProperty fileNameProperty() {
			return fileName;
	}
	
	// body -- g,s,p 
	public String getBody() {
		return body.get();
	}
	public void setBody(String body) {
		this.body.set(body);
	}
	public StringProperty bodyProperty() {
		return body;
	}
	
	/**
	 * Сохраняем файл в дисковый кеш.
	 */
	public void saveToDisk (String path) {
		String fullPath = path + getFileName();
		
		if ((getType() == 0) || (getType() == 10)) {   // file
			switch (getFileType()) {
			case FILE_TYPE_TEXT :
				FileUtil.writeTextFile(fullPath, getBody());
				break;
			case FILE_TYPE_IMAGE :
				FileUtil.writeImageFile(fullPath, bodyImage);
				break;
			default :
				ShowAppMsg.showAlert("WARNING", "Сохранение файла на диск", "Для данного типа файла сохранение не реализовано.", 
			             "Файл "+ fullPath +" не сохранен.");
			}
		} else {     // directory
			File fileDir = new File(fullPath);
			if (! fileDir.mkdirs()) {
				ShowAppMsg.showAlert("ERROR", "error", 
						"Помилка при створенні директорії на диску, TemplateFileItem.saveToDisk", 
						fullPath);
			}
		}
	}
	
	/**
	 * Читає файл з диска
	 */
	public void readFromDisk (String path) {
		String fullPath = path + getFileName();
	
		if ((getType() == 0) || (getType() == 10)) {   // file
			switch (getFileType()) {
			case FILE_TYPE_TEXT :
				try {
					setBody(FileUtil.readTextFileToString(fullPath));
				} catch (KBase_ReadTextFileUTFEx e) {
					//e.printStackTrace();
					ShowAppMsg.showAlert("WARNING", "Читання файла з диска", e.msg, e.fileName);
				}
				break;
			case FILE_TYPE_IMAGE :
				try {
					bodyImage = FileUtil.readImageFile (fullPath);
				} catch (KBase_ReadTextFileUTFEx e) {
					//e.printStackTrace();
					ShowAppMsg.showAlert("WARNING", "Читання файла з диска", e.msg, e.fileName);
				}
				break;
			default :
				ShowAppMsg.showAlert("WARNING", "Читання файла з диска", "Для даного типу файла читання не реалізовано.", 
			             "Файл "+ fullPath +" не прочитаний.");
			}
		}
	}
	
	/**
	 * серіалізація цього обьекту
	 */
	public void serialize (String path, String fileName) {
		doBeforeSerialization ();

		// якщо файл є картика, то зберігаємо на диск саму картинку
		if (getFileType() == FILE_TYPE_IMAGE) {
			saveToDisk (path);
		}
		
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
	public static TemplateFileItem unserialize (String path, String fileName) {
		TemplateFileItem ti = null;
		
		try (ObjectInputStream inputStream = 
    			new ObjectInputStream(new FileInputStream(path+fileName))) {
            ti = (TemplateFileItem) inputStream.readObject();
    	} catch (IOException e) {
        	ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", 
    				"\"" + path+fileName + "\"", 
    				"Помилка читання файла.");
    	} catch (ClassNotFoundException e) {
    		ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", "Буфер пустий.", "");
    		return null;
        }
		
		ti.doAfterSerialization();
		
		// якщо файл є картика, то читаємо з диска саму картинку
		if (ti.getFileType() == FILE_TYPE_IMAGE) {
			ti.readFromDisk (path);
		}
		
		return ti;
	}
	
	/**
	 * Потрібно запускати до серіалізації
	 */
	@Override
	public void doBeforeSerialization () {
		super.doBeforeSerialization();
		
		parentId_ser = getParentId();
		type_ser = getType();
		fileType_ser = getFileType();
		fileName_ser = getFileName();
		body_ser = getBody();
	}
	
	/**
	 * Потрібно запускати після серіалізації
	 */
	@Override
	public void doAfterSerialization () {
		super.doAfterSerialization();
		
		this.parentId         = new SimpleLongProperty(parentId_ser);
		this.type             = new SimpleIntegerProperty(type_ser);
		this.fileType         = new SimpleIntegerProperty(fileType_ser);
		this.fileName         = new SimpleStringProperty(fileName_ser);
		this.body             = new SimpleStringProperty(body_ser);
	}
}