
package app.model.business;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import app.lib.ShowAppMsg;

/**
 * Клас містить інформацію про один елемент словника.
 */
public class DictionaryItem extends SimpleItem  implements Serializable {
	// for "typeId"
	public static final int TYPE_ITEM_WORD       = 1;
	public static final int TYPE_ITEM_PHRASE     = 2;
	public static final int TYPE_ITEM_TEXT       = 3;

	/**
	 * In which section this element is located
	 */
	private long sectionId;
	/**
	 * Тип елементу : 1 - word, 2 - phrase, 3 - text
	 */
	private int typeId;
	/**
	 * Значення елемента
	 */
	private String value; 

	/**
	 * це типу як часто його показувати при навчанні
	 */
	private int rating;
	/**
	 * 0 - показуємо назву, 1 - показуємо дескріпшн
	 */
	private int reverse;
	/**
	 * 0 - не включаємо в навчання, 1 - включаємо
	 */
	private int status;
	/**
	 * Дата останього показу
	 */
	private Date dateViewed;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public DictionaryItem() {
		this(0, 0, 0, null, null, null, 
			 0, 0, 0, null,
 			 null, null, null, null);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public DictionaryItem(
			long id, long sectionId, int typeId,  
			String name, String value, String descr,
            int rating, int reverse, int status, Date dateViewed,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, dateCreated, dateModified, userCreated, userModified);
		this.sectionId    = sectionId;
		this.typeId       = typeId;
		this.value        = value;
		this.rating       = rating;
		this.reverse      = reverse;
		this.status       = status;
		this.dateViewed   = dateViewed;
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public DictionaryItem(DictionaryItem item) {
		super((SimpleItem)item);
		this.sectionId   = item.getSectionId();
		this.typeId      = item.getTypeId();
		this.value       = item.getValue();
		this.rating      = item.getRating();
		this.reverse     = item.getReverse();
		this.status      = item.getStatus();
		this.dateViewed  = item.getDateViewed();
	}
	
	public long getSectionId() {
		return sectionId;
	}
	public void setSectionId(long sectionId) {
		this.sectionId = sectionId;
	}
	
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public int getReverse() {
		return reverse;
	}
	public void setReverse(int reverse) {
		this.reverse = reverse;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public Date getDateViewed() {
		return dateViewed;
	}

	public void setDateViewed(Date dateViewed) {
		this.dateViewed = dateViewed;
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
	public static DictionaryItem unserialize (String path, String fileName) {
		DictionaryItem di = null;
		
		try (ObjectInputStream inputStream = 
    			new ObjectInputStream(new FileInputStream(path+fileName))) {
            di = (DictionaryItem) inputStream.readObject();
    	} catch (IOException e) {
        	ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", 
    				"\"" + path+fileName + "\"", 
    				"Помилка читання файла.");
    	} catch (ClassNotFoundException e) {
    		ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", "Буфер пустий.", "");
    		return null;
        }
		
		di.doAfterSerialization();
		
		return di;
	}
}
