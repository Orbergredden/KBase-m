
package app.model.business;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import app.lib.ShowAppMsg;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/*
 * Класс содержит тело информационного блока документа типа "Простой текст"
 * 
 * @author Игорь Макаревич
 */
public class Info_TextItem extends SimpleItem implements Serializable {
	/**
	 * Заголовок инфо блока
	 */
	private transient StringProperty title;
	/**
	 * Текст инфоблока
	 */
	private transient StringProperty text;
	/**
	 * 1 - показывать заголовок ; 0 или NULL - не показывать
	 */
	private transient IntegerProperty isShowTitle;
	
	// for Serialization
	private String title_ser;
	private String text_ser;
	private int isShowTitle_ser;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public Info_TextItem() {
		this(0, null, null, 0);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public Info_TextItem(long id, String title, String text, int isShowTitle) {
		super(id, null, null);
		this.title       = new SimpleStringProperty(title);
		this.text        = new SimpleStringProperty(text);
		this.isShowTitle = new SimpleIntegerProperty(isShowTitle);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public Info_TextItem(
			long id, String title, String text, int isShowTitle,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, null, null, dateCreated, dateModified, userCreated, userModified);
		this.title       = new SimpleStringProperty(title);
		this.text        = new SimpleStringProperty(text);
		this.isShowTitle = new SimpleIntegerProperty(isShowTitle);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public Info_TextItem(Info_TextItem item) {
		super((SimpleItem)item);
		this.title       = new SimpleStringProperty(item.getTitle());
		this.text        = new SimpleStringProperty(item.getText());
		this.isShowTitle = new SimpleIntegerProperty(item.getIsShowTitle());
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
	public static Info_TextItem unserialize (String path, String fileName) {
		Info_TextItem ti = null;
		
		try (ObjectInputStream inputStream = 
    			new ObjectInputStream(new FileInputStream(path+fileName))) {
            ti = (Info_TextItem) inputStream.readObject();
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
		
		title_ser = getTitle();
		text_ser = getText();
		isShowTitle_ser = getIsShowTitle();
	}
	
	/**
	 * Потрібно запускати після серіалізації
	 */
	@Override
	public void doAfterSerialization () {
		super.doAfterSerialization();
		
		this.title         = new SimpleStringProperty(title_ser);
		this.text          = new SimpleStringProperty(text_ser);
		this.isShowTitle   = new SimpleIntegerProperty(isShowTitle_ser);
	}
}
