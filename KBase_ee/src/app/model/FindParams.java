package app.model;

/**
 * Параметри пошуку інформації в БД знань
 */
public class FindParams {
	// в яких типах обєктів шукаємо текст
	private boolean objSection;
	private boolean objInfoHeader;
	private boolean objDictionary;
	private boolean objText;
	private boolean objImage;
	private boolean objFile;
	
	// тест який шукаємо
	private String text;
	// чи ігнорувати при пошуку регістр букв
	private boolean isTextIgnoreRegistr;
	
	/**
	 * 
     */
	public FindParams () {
		
	}
	
	public boolean isObjSection() {
		return objSection;
	}
	public void setObjSection(boolean objSection) {
		this.objSection = objSection;
	}
	public boolean isObjInfoHeader() {
		return objInfoHeader;
	}
    public void setObjInfoHeader(boolean objInfoHeader) {
        this.objInfoHeader = objInfoHeader;
    }
    public boolean isObjDictionary() {
        return objDictionary;
    }
    public void setObjDictionary(boolean objDictionary) {
        this.objDictionary = objDictionary;
    }
    public boolean isObjText() {
        return objText;
    }
    public void setObjText(boolean objText) {
        this.objText = objText;
    }
    public boolean isObjImage() {
        return objImage;
    }
    public void setObjImage(boolean objImage) {
        this.objImage = objImage;
    }
    public boolean isObjFile() {
        return objFile;
    }
    public void setObjFile(boolean objFile) {
        this.objFile = objFile;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public boolean isTextIgnoreRegistr() {
        return isTextIgnoreRegistr;
    }
    public void setTextIgnoreRegistr(boolean isTextIgnoreRegistr) {
        this.isTextIgnoreRegistr = isTextIgnoreRegistr;
    }
}