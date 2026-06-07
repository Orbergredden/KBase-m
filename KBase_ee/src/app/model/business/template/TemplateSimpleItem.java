
package app.model.business.template;

import app.model.business.SimpleItem;

import java.io.Serializable;
import java.util.Date;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 * Базовый класс-элемент для для различных классов шаблонов
 * Класс содержит информацию о шаблоне, теме, стиле или файле.
 * Используется в смешанных списках.
 * @author Igor Makarevich
 * @version 2.00.00.003   04.04.2021 - 17.11.2021
 */
public class TemplateSimpleItem extends SimpleItem implements Serializable {
	// for "typeItem"
	public static final int TYPE_ITEM_ROOT                  = 0;
	public static final int TYPE_ITEM_DIR_THEME             = 10;
	public static final int TYPE_ITEM_THEME                 = 1;
	public static final int TYPE_ITEM_DIR_FILE              = 2;
	public static final int TYPE_ITEM_FILE                  = 3;
	public static final int TYPE_ITEM_DIR_FILE_OPTIONAL     = 4;
	public static final int TYPE_ITEM_FILE_OPTIONAL         = 5;
	public static final int TYPE_ITEM_DIR_STYLE             = 6;
	public static final int TYPE_ITEM_STYLE                 = 7;
	public static final int TYPE_ITEM_DIR_TEMPLATE          = 8;
	public static final int TYPE_ITEM_TEMPLATE              = 9;
	
	// subtypeItem for files
	public static final int SUBTYPE_FILE_TEXT               = 1;
	public static final int SUBTYPE_FILE_IMAGE              = 2;
	public static final int SUBTYPE_FILE_BINARY             = 3;
	
	protected transient LongProperty themeId;
	
	/**
	 * Тип записи: 
	 *  0 - корень
	 * 10 - раздел с темами
	 *  1 - тема
	 *  2 - раздел с обязательными файлами
	 *  3 - обязательный файл
	 *  4 - раздел с не обязательными файлами
	 *  5 - не обязательный файл
	 *  6 - раздел со стилями (какой именно вид стиля определяется дополнительной переменной) 
	 *  7 - стиль
	 *  8 - раздел с шаблонами
	 *  9 - шаблон
	 */
	protected transient IntegerProperty typeItem;
	
	/**
	 * Подтип записи.
	 * Для файла, стиля, шаблона - тип файла или директории 
	 */
	protected transient LongProperty subtypeItem;
	
	/**
	 * для различного использования в разных типах обьектов
	 * Для стиля - ид инфо блока или 0 для зарезервированных
	 * Для файла - тип файла (fileType)
	 * для шаблону - кількість звязків зі стилями
	 */
	protected transient LongProperty flag;

	/**
	 * для различного использования в разных типах обьектов
	 * Для стиля - ид шаблона или 0 если нет связки с шаблоном
	 */
	private long flag2;
	
	// for Serialization
	private long themeId_ser;
	private int typeItem_ser;
	private long subtypeItem_ser;
	private long flag_ser;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TemplateSimpleItem() {
		this(0, null, null, 0, 0, 0);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public TemplateSimpleItem(
			long id, String name, String descr, long themeId, int typeItem, long subtypeItem) {
		super(id, name, descr);
		this.themeId          = new SimpleLongProperty(themeId);
		this.typeItem         = new SimpleIntegerProperty(typeItem);
		this.subtypeItem      = new SimpleLongProperty(subtypeItem);
	}
	
	/**
	 * Конструктор с основными данными и с флагом
	 * @param
	 */
	public TemplateSimpleItem(
			long id, String name, String descr, long themeId, int typeItem, long subtypeItem, long flag) {
		super(id, name, descr);
		this.themeId          = new SimpleLongProperty(themeId);
		this.typeItem         = new SimpleIntegerProperty(typeItem);
		this.subtypeItem      = new SimpleLongProperty(subtypeItem);
		this.flag             = new SimpleLongProperty(flag);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public TemplateSimpleItem(
			long id, String name, String descr, long themeId, int typeItem, long subtypeItem,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, dateCreated, dateModified, userCreated, userModified);
		this.themeId          = new SimpleLongProperty(themeId);
		this.typeItem         = new SimpleIntegerProperty(typeItem);
		this.subtypeItem      = new SimpleLongProperty(subtypeItem);
	}
	
	/**
	 * Конструктор со всеми данными и с флагом
	 * @param
	 */
	public TemplateSimpleItem(
			long id, String name, String descr, long themeId, int typeItem, long subtypeItem, long flag,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, dateCreated, dateModified, userCreated, userModified);
		this.themeId          = new SimpleLongProperty(themeId);
		this.typeItem         = new SimpleIntegerProperty(typeItem);
		this.subtypeItem      = new SimpleLongProperty(subtypeItem);
		this.flag             = new SimpleLongProperty(flag);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TemplateSimpleItem(TemplateSimpleItem item) {
		super((SimpleItem)item);
		this.themeId      = new SimpleLongProperty(item.getThemeId());
		this.typeItem     = new SimpleIntegerProperty(item.getTypeItem());
		this.subtypeItem  = new SimpleLongProperty(item.getSubtypeItem());
		try {
			this.flag     = new SimpleLongProperty(item.getFlag());
		} catch (NullPointerException e) {
			this.flag     = new SimpleLongProperty(-1);
    	}
	}
	
	// themeId  -- g,s,p
	public long getThemeId() {
		return themeId.get();
	}
	public void setThemeId(long themeId) {
	    this.themeId.set(themeId);
	}
	public LongProperty themeIdProperty() {
		return themeId;
	}
	
	// typeItem  -- g,s,p
	public int getTypeItem() {
	    return typeItem.get();
	}
	public void setTypeItem(int typeItem) {
	    this.typeItem.set(typeItem);
	}
	public IntegerProperty typeItemProperty() {
	    return typeItem;
	}
	
	// subtypeItem  -- g,s,p
	public long getSubtypeItem() {
	    return subtypeItem.get();
	}
	public void setSubtypeItem(long subtypeItem) {
	    this.subtypeItem.set(subtypeItem);
	}
	public LongProperty subtypeItemProperty() {
	    return subtypeItem;
	}
	
	// flag  -- g,s,p
	public long getFlag() {
	    return flag.get();
	}
	public void setFlag(long flag) {
	    this.flag.set(flag);
	}
	public LongProperty flagProperty() {
	    return flag;
	}
	
	// flag2  -- g,s
	public long getFlag2() {
	    return flag2;
	}
	public void setFlag2(long flag2) {
	    this.flag2 = flag2;
	}
	
	/**
	 * Потрібно запускати до серіалізації
	 */
	@Override
	public void doBeforeSerialization () {
		super.doBeforeSerialization();
		
		themeId_ser = getThemeId();
		typeItem_ser = getTypeItem();
		subtypeItem_ser = getSubtypeItem();
		flag_ser = getFlag();
	}
	
	/**
	 * Потрібно запускати після серіалізації
	 */
	@Override
	public void doAfterSerialization () {
		super.doAfterSerialization();
		
		this.themeId          = new SimpleLongProperty(themeId_ser);
		this.typeItem         = new SimpleIntegerProperty(typeItem_ser);
		this.subtypeItem      = new SimpleLongProperty(subtypeItem_ser);
		this.flag             = new SimpleLongProperty(flag_ser);
		/*
		setThemeId(themeId_ser);
		setTypeItem(typeItem_ser);
		setSubtypeItem(subtypeItem_ser);
		setFlag(flag_ser);*/
	}
}
