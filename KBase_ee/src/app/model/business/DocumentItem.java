
package app.model.business;

import app.lib.FileUtil;

import java.util.Date;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/*
 * Класс содержит информацию об одном скомпилированном документе
 * 
 * @author Игорь Макаревич
 */
public class DocumentItem extends SimpleItem {
	/**
	 * Id раздела
	 */
	private final LongProperty sectionId;
	/**
	 * Тело инфоблока
	 */
	private final StringProperty text;
	/**
	 * Тип кеширования : 1 - документы кешируются на локальном диске; 2 - кешируются в БД; 3 - кешируются на диске только обязательные файлы
	 */
	private final IntegerProperty type;
		
	/**
	 * Конструктор по умолчанию.
	 */
	public DocumentItem() {
		this(0, 0, null, 0);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public DocumentItem(long id, long sectionId, String text, int type) {
		super(id, null, null);
		this.sectionId   = new SimpleLongProperty(sectionId);
		this.text        = new SimpleStringProperty(text);
		this.type        = new SimpleIntegerProperty(type);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public DocumentItem(
			long id, long sectionId, String text, int type,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, null, null, dateCreated, dateModified, userCreated, userModified);
		this.sectionId   = new SimpleLongProperty(sectionId);
		this.text        = new SimpleStringProperty(text);
		this.type        = new SimpleIntegerProperty(type);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public DocumentItem(DocumentItem item) {
		super((SimpleItem)item);
		this.sectionId   = new SimpleLongProperty   (item.getSectionId());
		this.text        = new SimpleStringProperty (item.getText());
		this.type        = new SimpleIntegerProperty(item.getType());
	}

	// sectionId  -- g,s,p
	public long getSectionId() {
	    return sectionId.get();
	}
	public void setSectionId(long sectionId) {
	    this.sectionId.set(sectionId);
	}
	public LongProperty sectionIdProperty() {
	    return sectionId;
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
	
	/**
	 * Сохраняем файл на диск.
	 */
	public void saveToDisk (String FilePath) {
		FileUtil.writeTextFile(FilePath, getText());
	}
}
