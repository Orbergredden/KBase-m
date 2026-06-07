package app.model.business.template;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import app.lib.ShowAppMsg;

/**
 * Інформація яка передається при копі/пасті обьектів цього пакету 
 * @author imakarevich
 */
public class TemplateClipboardInfo implements Serializable {
	public static final String FILE_NAME      = "TemplateClipboard.ser";
	public static final String FILE_NAME_INFO = "TemplateClipboardInfo.ser";
	
	//for typeOper
	public static final int TYPE_OPER_COPY = 0;
	public static final int TYPE_OPER_CUT  = 1;
	
	//for typeObj
	public static final int TYPE_OBJ_FILE     = 0;
	public static final int TYPE_OBJ_STYLE    = 1;
	public static final int TYPE_OBJ_TEMPLATE = 2;
	
	/**
	 * Тип операції (Copy,Cut)
	 */
	private int typeOper;
	
	/**
	 * Тип обьекту (файл, стиль або шаблон) 
	 */
	private int typeObj;
	
	/**
	 * id з'єднання з БД
	 */
	private int dbCon_Id;
	
	/**
	 * Назва з'єднання з БД
	 */
	private String dbCon_Name; 
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TemplateClipboardInfo() {
		this(0, 0, 0, "");
	}
	
	/**
	 * Конструктор з даними
	 * @param
	 */
	public TemplateClipboardInfo (int typeOper, int typeObj, int dbCon_Id, String dbCon_Name) {
		this.typeOper         = typeOper;
		this.typeObj          = typeObj;
		this.dbCon_Id         = dbCon_Id;
		this.dbCon_Name       = dbCon_Name;
	}
	
	//######## getters and setters ###################################
	public int getTypeOper() {
		return typeOper;
	}

	public void setTypeOper(int typeOper) {
		this.typeOper = typeOper;
	}

	public int getTypeObj() {
		return typeObj;
	}

	public void setTypeObj(int typeObj) {
		this.typeObj = typeObj;
	}

	public int getDbCon_Id() {
		return dbCon_Id;
	}

	public void setDbCon_Id(int dbCon_Id) {
		this.dbCon_Id = dbCon_Id;
	}

	public String getDbCon_Name() {
		return dbCon_Name;
	}

	public void setDbCon_Name(String dbCon_Name) {
		this.dbCon_Name = dbCon_Name;
	}
	
	/**
	 * серіалізація цього обьекту
	 */
	public void serialize (String path, String fileName) {
    	try (ObjectOutputStream outputStream = 
        		new ObjectOutputStream(new FileOutputStream(path+fileName))) {
            outputStream.writeObject(this);
        } catch (IOException e) {
        	ShowAppMsg.showAlert("WARNING", "Копіювання у буфер обміну", 
    				"\"" + path+fileName + "\"", 
    				"Помилка запису файла.");
            //e.printStackTrace();
        }
	}
	
	/**
	 * десеріалізація обьекту
	 */
	public static TemplateClipboardInfo unserialize (String path, String fileName) {
		TemplateClipboardInfo ti = null;
		
		try (ObjectInputStream inputStream = 
    			new ObjectInputStream(new FileInputStream(path+fileName))) {
            ti = (TemplateClipboardInfo) inputStream.readObject();
    	} catch (IOException e) {
        	ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", 
    				"\"" + path+fileName + "\"", 
    				"Помилка читання файла.");
    	} catch (ClassNotFoundException e) {
    		ShowAppMsg.showAlert("WARNING", "Читаємо з буфера обміну", "Буфер пустий.", "");
        }

		return ti;
	}
}
