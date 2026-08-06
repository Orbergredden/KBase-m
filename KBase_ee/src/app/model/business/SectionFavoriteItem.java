package app.model.business;

import java.util.Date;

/**
 * Елемент в дереві Favorite
 */
public class SectionFavoriteItem {
	private long id;
	private long parentId;
	private long sectionId;
	private SectionItem section;
	private Date dateCreated;

	/**
	 * Конструктор по замовчанню.
	 */
	public SectionFavoriteItem() {
		this(0, 0, 0, null, null);
	}

	/**
	 * Конструктор
	 * @param
	 */
	public SectionFavoriteItem (long id, long parentId, long sectionId, SectionItem section,
			Date dateCreated) {
		this.id = id;
		this.parentId = parentId;
		this.sectionId = sectionId;
		this.section = section;
		this.dateCreated = dateCreated;
	}

	/**
	 * Конструктор
	 * @param
	 */
	public SectionFavoriteItem (SectionFavoriteItem item) {
		this.id = item.getId();
		this.parentId = item.getParentId();
		this.sectionId = item.getSectionId();
		this.section = item.getSection();
		this.dateCreated = item.getDateCreated();
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getParentId() {
		return parentId;
	}
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	public long getSectionId() {
		return sectionId;
	}
	public void setSectionId(long sectionId) {
		this.sectionId = sectionId;
	}
	public SectionItem getSection() {
		return section;
	}
	public void setSection(SectionItem section) {
		this.section = section;
	}

	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
}