
package app.model;

import java.util.Date;

import app.model.business.SimpleItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Класс содержит одну настройку из списка настроек БД приложения.
 * @author Igor Makarevich
 */
public class SettingItem extends SimpleItem {
	private final StringProperty alias;
	private final StringProperty section;
	private final StringProperty subject;
	private final StringProperty value;

	/**
	 * Конструктор по умолчанию.
	 */
	public SettingItem() {
		this(0, null, null, null, null, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public SettingItem(int id, String alias, String section, String subject, String name, String value, String descr) {
		super(id, name, descr);
		this.alias   = new SimpleStringProperty(alias);
		this.section = new SimpleStringProperty(section);
		this.subject = new SimpleStringProperty(subject);
		this.value   = new SimpleStringProperty(value);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public SettingItem(
			int id, String alias, String section, String subject, String name, String value, String descr,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, dateCreated, dateModified, userCreated, userModified);
		this.alias   = new SimpleStringProperty(alias);
		this.section = new SimpleStringProperty(section);
		this.subject = new SimpleStringProperty(subject);
		this.value   = new SimpleStringProperty(value);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public SettingItem(SettingItem par) {
		super((SimpleItem)par);
		this.alias   = new SimpleStringProperty(par.getAlias());
		this.section = new SimpleStringProperty(par.getSection());
		this.subject = new SimpleStringProperty(par.getSubject());
		this.value   = new SimpleStringProperty(par.getValue());
	}
	
	// alias -- g,s,p 
	public String getAlias() {
	    return alias.get();
	}
	public void setAlias(String alias) {
	    this.alias.set(alias);
	}
	public StringProperty aliasProperty() {
	    return alias;
	}
	
	// section -- g,s,p 
	public String getSection() {
		return section.get();
	}
	public void setSection(String section) {
		this.section.set(section);
	}
	public StringProperty sectionProperty() {
		return section;
	}
	
	// subject -- g,s,p 
	public String getSubject() {
		return subject.get();
	}
	public void setSubject(String subject) {
		this.subject.set(subject);
	}
	public StringProperty subjectProperty() {
		return subject;
	}
	
	// value -- g,s,p 
	public String getValue() {
		return value.get();
	}
	public void setValue(String value) {
		this.value.set(value);
	}
	public StringProperty valueProperty() {
		return value;
	}
}
