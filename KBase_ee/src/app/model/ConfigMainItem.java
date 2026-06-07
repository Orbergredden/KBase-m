package app.model;

import app.util.LocalDateAdapter;

import java.time.LocalDate;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import javafx.beans.property.*;

/**
 * Класс содержит одну настройку из списка основных настроек приложения.
 * @author Igor Makarevich
 */
public class ConfigMainItem {
	/**
	 * Уникальное значение в списке конфиг.параметров
	 */
	private final IntegerProperty id;
	/**
	 * Имя раздела
	 */
	private final StringProperty sectionName;
	/**
	 * Название конфиг. параметра
	 */
	private final StringProperty name;
	/**
	 * Описание данного параметра настроек
	 */
	private final StringProperty descr;
	/**
	 * Значение конфиг. параметра
	 */
	private final StringProperty value;
	/**
	 * Дата последнего изменения
	 */
	private final ObjectProperty<LocalDate> lastModified;
	/**
	 * Можно показывать в программе
	 */
	private final BooleanProperty isShow;
	/**
	 * Можно редактировать
	 */
	private final BooleanProperty isEditable;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public ConfigMainItem() {
		this(0, null, null, null, null,
				null, false, false);
	}
	
	/**
	 * Конструктор.
	 * @param
	 */
	public ConfigMainItem(int id, String sectionName, String name, String descr, String value,
						  LocalDate lastModified, boolean isShow, boolean isEditable) {
		this.id           = new SimpleIntegerProperty(id);
		this.sectionName  = new SimpleStringProperty(sectionName);
		this.name         = new SimpleStringProperty(name);
		this.descr        = new SimpleStringProperty(descr);
		this.value        = new SimpleStringProperty(value);
		this.lastModified = new SimpleObjectProperty<LocalDate>(lastModified);
		this.isShow       = new SimpleBooleanProperty(isShow);
		this.isEditable   = new SimpleBooleanProperty(isEditable);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public ConfigMainItem(ConfigMainItem par) {
		this.id           = new SimpleIntegerProperty(par.getId());
		this.sectionName  = new SimpleStringProperty(par.getSectionName());
		this.name         = new SimpleStringProperty(par.getName());
		this.descr        = new SimpleStringProperty(par.getDescr());
		this.value        = new SimpleStringProperty(par.getValue());
		this.lastModified = new SimpleObjectProperty<LocalDate>(par.getLastModified());
		this.isShow       = new SimpleBooleanProperty(par.getIsShow());
		this.isEditable   = new SimpleBooleanProperty(par.getIsEditable());
	}
	
	// id  -- g,s,p
	public int getId() {
        return id.get();
    }
    public void setId(int id) {
        this.id.set(id);
    }
    public IntegerProperty idProperty() {
        return id;
    }
	
    // sectionName -- g,s,p 
 	public String getSectionName() {
        return sectionName.get();
    }
    public void setSectionName(String sectionName) {
        this.sectionName.set(sectionName);
    }
    public StringProperty sectionNameProperty() {
        return sectionName;
    }
	
    // name -- g,s,p 
  	public String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public StringProperty nameProperty() {
        return name;
    }

	// descr -- g,s,p
	public String getDescr() { return descr.get(); }
	public void setDescr(String descr) { this.descr.set(descr); }
	public StringProperty descrProperty() { return descr; }
    
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
    
    // lastModified -- g,s,p
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getLastModified() {
        return lastModified.get();
    }
    public void setLastModified(LocalDate lastModified) {
        this.lastModified.set(lastModified);
    }
    public ObjectProperty<LocalDate> lastModifiedProperty() {
        return lastModified;
    }

	// isShow -- g,s,p
	public boolean getIsShow() {
		return isShow.get();
	}
	public void setIsShow(boolean isShow) {
		this.isShow.set(isShow);
	}
	public BooleanProperty isShowProperty() {
		return isShow;
	}

	// isEditable -- g,s,p
	public boolean getIsEditable() {
		return isEditable.get();
	}
	public void setIsEditable(boolean isEditable) {
		this.isEditable.set(isEditable);
	}
	public BooleanProperty isEditableProperty() {
		return isEditable;
	}
}
