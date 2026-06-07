package app.model.business;

import java.util.Date;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

/**
 * Класс содержит информацию об одной пиктограмме.
 * 
 * @author Игорь Макаревич
 */
public class IconItem extends SimpleItem {
	private final LongProperty parentId;
	/**
	 * Изображение пиктограммы
	 */
	public Image image;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public IconItem() {
		this(0, 0, null, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 */
	public IconItem(
			long id, long parentId, String name, String descr, Image image) {
		super(id, name, descr);
		this.parentId     = new SimpleLongProperty(parentId);
		this.image        = image;
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public IconItem(
			long id, long parentId, String name, String descr, Image image,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, dateCreated, dateModified, userCreated, userModified);
		this.parentId     = new SimpleLongProperty(parentId);
		this.image        = image;
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public IconItem(IconItem item) {
		super((SimpleItem)item);
		this.parentId     = new SimpleLongProperty(item.getParentId());
		this.image        = item.image;
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
	
    // image -- g,s,p 
   	/*public Image getImage() {
        return image.get();
    }
    public void setImage(String image) {
        this.image.set(image);
    }*/
}
