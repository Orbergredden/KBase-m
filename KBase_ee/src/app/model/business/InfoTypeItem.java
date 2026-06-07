
package app.model.business;

import java.util.Date;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/*
 * Класс содержит информацию об одном типе информационного блока
 */
public class InfoTypeItem extends SimpleItem {
	private final StringProperty table_name;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public InfoTypeItem() {
		this(0, null, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public InfoTypeItem(long id, String name, String table_name, String descr) {
		super(id, name, descr);
		this.table_name      = new SimpleStringProperty(table_name);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public InfoTypeItem(
			long id, String name, String table_name, String descr,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, dateCreated, dateModified, userCreated, userModified);
		this.table_name      = new SimpleStringProperty(table_name);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public InfoTypeItem(InfoTypeItem item) {
		super((SimpleItem)item);
		this.table_name   = new SimpleStringProperty(item.getTableName());
	}
	
	// table_name  -- g,s,p
	public String getTableName() {
		return table_name.get();
	}
	public void setTableName(String table_name) {
		this.table_name.set(table_name);
	}
	public StringProperty tableNameProperty() {
			return table_name;
	}
}
