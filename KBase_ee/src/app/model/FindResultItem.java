package app.model;

import java.time.LocalDateTime;

/**
 * містить один елемент результату пошуку інформації в базі знань
 */
public class FindResultItem {
	// в якому елементі був знайдений текст
	private String textType;
	
	private long sectionId;
	private String sectionName;
	private String sectionPathName;
	
	// ід та ім'я інформації. У випадку документу це Заголовок, у випадку словника це елемент словника
	private long infoId;
	private String infoName;
	
	private String text;
	private LocalDateTime dateCreated;
	private LocalDateTime dateModified;
	private String userCreated;
	private String userModified;
	
	/**
	 * Constructor
	 */
	public FindResultItem () {
		
	}

    /**
     * Конструктор, що ініціалізує всі поля
     */
    public FindResultItem(String textType,
            long sectionId,
            String sectionName,
            String sectionPathName,
            long infoId,
            String infoName,
            String text,
            LocalDateTime dateCreated,
            LocalDateTime dateModified,
            String userCreated,
            String userModified) {
        this.textType = textType;
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.sectionPathName = sectionPathName;
        this.infoId = infoId;
        this.infoName = infoName;
        this.text = text;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
        this.userCreated = userCreated;
        this.userModified = userModified;
    }

    public String getTextType() {
        return textType;
    }
    public void setTextType(String textType) {
        this.textType = textType;
    }
    public long getSectionId() {
        return sectionId;
    }
    public void setSectionId(long sectionId) {
        this.sectionId = sectionId;
    }
    public String getSectionName() {
        return sectionName;
    }
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
    public String getSectionPathName() {
        return sectionPathName;
    }
    public void setSectionPathName(String sectionPathName) {
        this.sectionPathName = sectionPathName;
    }
    public long getInfoId() {
        return infoId;
    }
    public void setInfoId(long infoId) {
        this.infoId = infoId;
    }
    public String getInfoName() {
        return infoName;
    }
    public void setInfoName(String infoName) {
        this.infoName = infoName;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
    public LocalDateTime getDateModified() {
        return dateModified;
    }
    public void setDateModified(LocalDateTime dateModified) {
        this.dateModified = dateModified;
    }
    public String getUserCreated() {
        return userCreated;
    }
    public void setUserCreated(String userCreated) {
        this.userCreated = userCreated;
    }
    public String getUserModified() {
        return userModified;
    }
    public void setUserModified(String userModified) {
        this.userModified = userModified;
    }
}