
package app.model.business.template;

import java.util.Date;

/**
 * Класс содержит информацию об одной теме шаблонов документов.
 * 
 * @author Игорь Макаревич
 */
public class TemplateThemeItem extends TemplateSimpleItem {
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TemplateThemeItem() {
		this(0, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public TemplateThemeItem(long id, String name, String descr) {
		super(id, name, descr, id, TYPE_ITEM_THEME, 0);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public TemplateThemeItem(long id, String name, String descr,
							 Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, id, TYPE_ITEM_THEME, 0, dateCreated, dateModified, userCreated, userModified);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TemplateThemeItem(TemplateThemeItem item) {
		super((TemplateSimpleItem)item);
	}
}
