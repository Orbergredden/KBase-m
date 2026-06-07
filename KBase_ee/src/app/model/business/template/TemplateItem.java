
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

/**
 * Класс содержит информацию об одном шаблоне документа (его инфо блока).
 * @author Игорь Макаревич
 * @version 2.00.00.001   11.04.2021
 */
public class TemplateItem extends TemplateSimpleItem implements Serializable {
	/**
	 * Id родительской директории шаблона или 0, если шаблон (дирктория) находится в корне
	 */
	private transient LongProperty parentId;
	/**
	 * 0-шаблон, 1-директория ; (для зарезервированных) 10 - шаблон, 11 - директория
	 */
	private transient IntegerProperty type;
	/**
	 * исходный код шаблона 
	 */
	private transient StringProperty body;
	
	// for Serialization
	private long parentId_ser;
	private int type_ser;
	private String body_ser;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TemplateItem() {
		this(0, 0, 0, null, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public TemplateItem(long id, long parentId, int type, String name, String descr, String body) {
		super(id, name, descr, 0, 
				(((type == 0)||(type == 10)) ? TYPE_ITEM_TEMPLATE : TYPE_ITEM_DIR_TEMPLATE),
				type, 0);
		this.parentId       = new SimpleLongProperty(parentId);
		this.type           = new SimpleIntegerProperty(type);
		this.body           = new SimpleStringProperty(body);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public TemplateItem(long id, long parentId, int type, String name, String descr, String body,
			            Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, 0, 
				(((type == 0)||(type == 10)) ? TYPE_ITEM_TEMPLATE : TYPE_ITEM_DIR_TEMPLATE),
				type, 0, dateCreated, dateModified, userCreated, userModified);
		this.parentId       = new SimpleLongProperty(parentId);
		this.type           = new SimpleIntegerProperty(type);
		this.body           = new SimpleStringProperty(body);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TemplateItem(TemplateItem item) {
		super((TemplateSimpleItem)item);
		this.parentId       = new SimpleLongProperty(item.getParentId());
		this.type           = new SimpleIntegerProperty(item.getType());
		this.body           = new SimpleStringProperty(item.getBody());
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
	    this.type.set(type);
	}
	public IntegerProperty typeProperty() {
	    return type;
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
	public static TemplateItem unserialize (String path, String fileName) {
		TemplateItem ti = null;
		
		try (ObjectInputStream inputStream = 
    			new ObjectInputStream(new FileInputStream(path+fileName))) {
            ti = (TemplateItem) inputStream.readObject();
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
		body_ser = getBody();
	}
	
	/**
	 * Потрібно запускати після серіалізації
	 */
	@Override
	public void doAfterSerialization () {
		super.doAfterSerialization();
		
		this.parentId       = new SimpleLongProperty(parentId_ser);
		this.type           = new SimpleIntegerProperty(type_ser);
		this.body           = new SimpleStringProperty(body_ser);
	}
}
