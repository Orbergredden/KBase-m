
package app.model.business.template;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import app.lib.ShowAppMsg;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/*
 * Класс содержит информацию об одном стиле шаблона
 * @author Игорь Макаревич
 */
public class TemplateStyleItem extends TemplateSimpleItem implements Serializable {
	/**
	 * Id родительской директории стиля или 0, если стиль (дирктория) находится в корне
	 */
	private transient LongProperty parentId;
	/**
	 * 0-стиль, 1-директория ; (для зарезервированных) 10 - стиль, 11 - директория
	 */
	private transient IntegerProperty type;
	/**
	 * Id типа инфоблока
	 */
	private transient LongProperty infoTypeId;
	/**
	 * уникальный текстовый идентификатор для зарезервированных стилей
	 */
	private transient StringProperty tag;
	
	// for Serialization
	private long parentId_ser;
	private int type_ser;
	private long infoTypeId_ser;
	private String tag_ser;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TemplateStyleItem() {
		this(0, 0, 0, 0, null, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public TemplateStyleItem(long id, long parentId, int type, long infoTypeId, String name, String descr, String tag) {
		super(id, name, descr, 0,
				(((type == 0)||(type == 10)) ? TYPE_ITEM_STYLE : TYPE_ITEM_DIR_STYLE),
				type, infoTypeId);
		this.parentId     = new SimpleLongProperty(parentId);
		this.type         = new SimpleIntegerProperty(type);
		this.infoTypeId   = new SimpleLongProperty(infoTypeId);
		this.tag          = new SimpleStringProperty(tag);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public TemplateStyleItem(long id, long parentId, int type, long infoTypeId, String name, String descr, String tag,
                  			 Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, 0,
				(((type == 0)||(type == 10)) ? TYPE_ITEM_STYLE : TYPE_ITEM_DIR_STYLE),
				type, infoTypeId,
				dateCreated, dateModified, userCreated, userModified);
		this.parentId     = new SimpleLongProperty(parentId);
		this.type         = new SimpleIntegerProperty(type);
		this.infoTypeId   = new SimpleLongProperty(infoTypeId);
		this.tag          = new SimpleStringProperty(tag);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TemplateStyleItem(TemplateStyleItem item) {
		super((TemplateSimpleItem)item);
		this.parentId     = new SimpleLongProperty(item.getParentId());
		this.type         = new SimpleIntegerProperty(item.getType());
		this.infoTypeId   = new SimpleLongProperty(item.getInfoTypeId());
		this.tag          = new SimpleStringProperty(item.getTag());
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
	
	// infoTypeId  -- g,s,p
	public long getInfoTypeId() {
	    return infoTypeId.get();
	}
	public void setInfoTypeId(long infoTypeId) {
		setFlag (infoTypeId);
	    this.infoTypeId.set(infoTypeId);
	}
	public LongProperty infoTypeIdProperty() {
	    return infoTypeId;
	}
	
	// tag  -- g,s,p
	public String getTag() {
		return tag.get();
	}
	public void setTag(String tag) {
		this.tag.set(tag);
	}
	public StringProperty tagProperty() {
		return tag;
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
	public static TemplateStyleItem unserialize (String path, String fileName) {
		TemplateStyleItem ti = null;
		
		try (ObjectInputStream inputStream = 
    			new ObjectInputStream(new FileInputStream(path+fileName))) {
            ti = (TemplateStyleItem) inputStream.readObject();
    	} catch (IOException e) {
        	ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", 
    				"\"" + path+fileName + "\"", 
    				"Помилка читання файла.");
    	} catch (ClassNotFoundException e) {
    		ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", "Буфер пустий.", "");
    		return null;
        }
		
		ti.doAfterSerialization();
		
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
		infoTypeId_ser = getInfoTypeId();
		tag_ser = getTag();
	}
	
	/**
	 * Потрібно запускати після серіалізації
	 */
	@Override
	public void doAfterSerialization () {
		super.doAfterSerialization();
		
		this.parentId     = new SimpleLongProperty(parentId_ser);
		this.type         = new SimpleIntegerProperty(type_ser);
		this.infoTypeId   = new SimpleLongProperty(infoTypeId_ser);
		this.tag          = new SimpleStringProperty(tag_ser);
	}
}
