
package app.model.business;

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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

/**
 * Класс содержит информацию об одном информационном разделе.
 */
public class SectionItem extends SimpleItem implements Serializable {
	private transient LongProperty parentId;
	/**
	 * тип інформації розділу : 1 - документ, 2 - словник
	 */
	private int typeId;
	private transient LongProperty iconId; 
	public transient Image icon;
	private transient ObjectProperty<Date> dateModifiedInfo;
	
	private transient LongProperty iconIdRoot;  // Корневая иконка поддерева для выбора иконок для данного и дочерних разделов
	private transient LongProperty iconIdDef;   // Иконка по умолчанию для данного и дочерних разделов
	private transient LongProperty themeId;     // Тема (шаблонов) для показа документа
	private transient IntegerProperty cacheType;// Тип кеширования : 1 - документы кешируются на локальном диске; 2 - кешируются в БД; 3 - кешируются на диске только обязательные файлы
	
	private String templateMain;   // Тег стилю головного шаблона
	private int templateMainTree;  // 0 - стиль діє лише на ций розділ, 1 - розповсюджується також на всі підрозділи
	private long templateMainRoot; // Коренева директорія головних стилів
	
	private boolean isChildLoaded; // чи завантажена дочірня гілка з БД у додаток
	
	// for Serialization
	private long parentId_ser;
	private long iconId_ser;
	private Date dateModifiedInfo_ser;
	private long iconIdRoot_ser;
	private long iconIdDef_ser;
	private long themeId_ser;
	private int cacheType_ser;
	
	private boolean isIconPresent_ser;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public SectionItem() {
		this(0, 0, 0, null, null, 0, null, 
				null, 0, 0, 
				0, 0, 0, 0,
				null, null, null, null, null);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public SectionItem(
			long id, long parentId, int typeId, String name, String descr, long iconId, Image icon,
			String templateMain, int templateMainTree, long templateMainRoot,
			long iconIdRoot, long iconIdDef, long themeId, int cacheType,
			//boolean isChildLoaded,
			Date dateCreated, Date dateModified, String userCreated, String userModified, Date dateModifiedInfo) {
		super(id, name, descr, dateCreated, dateModified, userCreated, userModified);
		this.parentId         = new SimpleLongProperty(parentId);
		this.typeId           = typeId;
		this.iconId           = new SimpleLongProperty(iconId);
		this.icon             = icon;
		
		this.templateMain     = templateMain;
		this.templateMainTree = templateMainTree;
		this.templateMainRoot = templateMainRoot;
		
		this.iconIdRoot       = new SimpleLongProperty(iconIdRoot);
		this.iconIdDef        = new SimpleLongProperty(iconIdDef);
		this.themeId          = new SimpleLongProperty(themeId);
		this.cacheType        = new SimpleIntegerProperty(cacheType);
		
		this.dateModifiedInfo = new SimpleObjectProperty<Date>(dateModifiedInfo);
		
		//this.isChildLoaded    = isChildLoaded;
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public SectionItem(SectionItem item) {
		super((SimpleItem)item);
		this.parentId     = new SimpleLongProperty(item.getParentId());
		this.typeId       = item.getTypeId();
		this.iconId       = new SimpleLongProperty(item.getIconId());
		this.icon         = item.icon;
		
		this.templateMain     = item.getTemplateMain();
		this.templateMainTree = item.getTemplateMainTree();
		this.templateMainRoot = item.getTemplateMainRoot();
		
		this.iconIdRoot       = new SimpleLongProperty(item.getIconIdRoot());
		this.iconIdDef        = new SimpleLongProperty(item.getIconIdDef());
		this.themeId          = new SimpleLongProperty(item.getThemeId());
		this.cacheType        = new SimpleIntegerProperty(item.getCacheType());
		
		this.dateModifiedInfo = new SimpleObjectProperty<Date>(item.getDateModifiedInfo());
		
		this.isChildLoaded = item.isChildLoaded();
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

	// typeId
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

		
	// iconId  -- g,s,p
    public long getIconId() {
    	return iconId.get();
	}
	public void setIconId(long iconId) {
	    this.iconId.set(iconId);
	}
	public LongProperty iconIdProperty() {
	    return iconId;
	}
	
    public String getTemplateMain() {
		return templateMain;
	}
	public void setTemplateMain(String templateMain) {
		this.templateMain = templateMain;
	}
	public int getTemplateMainTree() {
		return templateMainTree;
	}
	public void setTemplateMainTree(int templateMainTree) {
		this.templateMainTree = templateMainTree;
	}
	public long getTemplateMainRoot() {
		return templateMainRoot;
	}
	public void setTemplateMainRoot(long templateMainRoot) {
		this.templateMainRoot = templateMainRoot;
	}

	// iconIdRoot  -- g,s,p
    public long getIconIdRoot() {
    	return iconIdRoot.get();
	}
	public void setIconIdRoot(long iconIdRoot) {
	    this.iconIdRoot.set(iconIdRoot);
	}
	public LongProperty iconIdRootProperty() {
	    return iconIdRoot;
	}
	
	// iconIdDef  -- g,s,p
    public long getIconIdDef() {
    	return iconIdDef.get();
	}
	public void setIconIdDef(long iconIdDef) {
	    this.iconIdDef.set(iconIdDef);
	}
	public LongProperty iconIdDefProperty() {
	    return iconIdDef;
	}
	
	// themeId  -- g,s,p
    public long getThemeId() {
    	return themeId.get();
	}
	public void setThemeId(long themeId) {
	    this.themeId.set(themeId);
	}
	public LongProperty themeIdDefProperty() {
	    return themeId;
	}
	
	// cacheType  -- g,s,p
    public int getCacheType() {
    	return cacheType.get();
	}
	public void setCacheType(int cacheType) {
	    this.cacheType.set(cacheType);
	}
	public IntegerProperty cacheTypeProperty() {
	    return cacheType;
	}
	
	// dateModifiedInfo -- g,s,p
    public Date getDateModifiedInfo() {
        return dateModifiedInfo.get();
    }
    public void setDateModifiedInfo(Date dateModifiedInfo) {
        this.dateModifiedInfo.set(dateModifiedInfo);
    }
    public ObjectProperty<Date> dateModifiedInfoProperty() {
        return dateModifiedInfo;
    }

    // isChildLoaded
    public boolean isChildLoaded() {
		return isChildLoaded;
	}
	public void setChildLoaded(boolean isChildLoaded) {
		this.isChildLoaded = isChildLoaded;
	}
	
	/**
	 * серіалізація цього обьекту
	 */
	public void serialize (String path, String fileName, String fileNameIcon) {
		doBeforeSerialization ();

		if (isIconPresent_ser) {
			FileUtil.writeImageFile(path + fileNameIcon, icon);
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
	public static SectionItem unserialize (String path, String fileName, String fileNameIcon) {
		SectionItem si = null;
		
		try (ObjectInputStream inputStream = 
    			new ObjectInputStream(new FileInputStream(path+fileName))) {
            si = (SectionItem) inputStream.readObject();
    	} catch (IOException e) {
        	ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", 
    				"\"" + path+fileName + "\"", 
    				"Помилка читання файла.");
    	} catch (ClassNotFoundException e) {
    		ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", "Буфер пустий.", "");
    		return null;
        }
		
		if (si.isIconPresent_ser) {
			try {
				si.icon = FileUtil.readImageFile (path + fileNameIcon);
			} catch (KBase_ReadTextFileUTFEx e) {
				//e.printStackTrace();
				ShowAppMsg.showAlert("WARNING", "Читання файла з диска", e.msg, e.fileName);
			}
		}
		
		si.doAfterSerialization();
		
		return si;
	}
	
	/**
	 * Потрібно запускати до серіалізації
	 */
	@Override
	public void doBeforeSerialization () {
		super.doBeforeSerialization();
		
		parentId_ser = getParentId();
		iconId_ser = getIconId();
		dateModifiedInfo_ser = getDateModifiedInfo();
		iconIdRoot_ser = getIconIdRoot();
		iconIdDef_ser = getIconIdDef();
		themeId_ser = getThemeId();
		cacheType_ser = getCacheType();
		
		if (icon == null)  isIconPresent_ser = false;
		else               isIconPresent_ser = true;
	}
	
	/**
	 * Потрібно запускати після серіалізації
	 */
	@Override
	public void doAfterSerialization () {
		super.doAfterSerialization();
		
		this.parentId         = new SimpleLongProperty(parentId_ser);
		this.iconId           = new SimpleLongProperty(iconId_ser);
		this.dateModifiedInfo = new SimpleObjectProperty<Date>(dateModifiedInfo_ser);
		this.iconIdRoot       = new SimpleLongProperty(iconIdRoot_ser);
		this.iconIdDef        = new SimpleLongProperty(iconIdDef_ser);
		this.themeId          = new SimpleLongProperty(themeId_ser);
		this.cacheType        = new SimpleIntegerProperty(cacheType_ser);
	}
}
