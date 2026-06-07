package app.model.business;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import app.lib.ShowAppMsg;

/**
 * Інформація яка передається при копі/пасті документа та його інфоблоків  
 * @author imakarevich
 */
public class SectionClipboardInfo implements Serializable {
	public static final String FILE_NAME_INFO = "SectionClipboardInfo.ser";
	public static final String FILE_NAME      = "SectionClipboard.ser";
	public static final String FILE_NAME_ICON = "SectionClipboardIcon.png";
	public static final String FILE_PREFIX_INFO_HEADER = "SectionInfoHeader_";
	public static final String FILE_PREFIX_INFO_BLOCK  = "SectionInfoBlock_";
	public static final String FILE_PREFIX_INFO_FILE   = "SectionInfoFile_";
	public static final String FILE_POSTFIX            = ".ser";
	
	//for typeOper
	public static final int TYPE_OPER_COPY = 0;
	public static final int TYPE_OPER_CUT  = 1;
	
	/**
	 * Тип операції (Copy,Cut)
	 */
	private int typeOper;
	
	/**
	 * id з'єднання з БД
	 */
	private int dbCon_Id;
	
	/**
	 * Назва з'єднання з БД
	 */
	private String dbCon_Name; 
	
	/**
	 * Кількість інфоблоків у вибраного розділа
	 */
	private int numberOfInfoBlocks;

	/**
	 * Конструктор по умолчанию.
	 */
	public SectionClipboardInfo() {
		this(0, 0, "", 0);
	}
	
	/**
	 * Конструктор з даними
	 * @param
	 */
	public SectionClipboardInfo (int typeOper, int dbCon_Id, String dbCon_Name, int numberOfInfoBlocks) {
		this.typeOper         = typeOper;
		this.dbCon_Id         = dbCon_Id;
		this.dbCon_Name       = dbCon_Name;
		this.numberOfInfoBlocks = numberOfInfoBlocks;
	}
	
	//######## getters and setters ###################################
		public int getTypeOper() {
			return typeOper;
		}

		public void setTypeOper(int typeOper) {
			this.typeOper = typeOper;
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
		
		public int getNumberOfInfoBlocks() {
			return numberOfInfoBlocks;
		}

		public void setNumberOfInfoBlocks(int numberOfInfoBlocks) {
			this.numberOfInfoBlocks = numberOfInfoBlocks;
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
	public static SectionClipboardInfo unserialize (String path, String fileName) {
		SectionClipboardInfo ti = null;
		
		try (ObjectInputStream inputStream = 
    			new ObjectInputStream(new FileInputStream(path+fileName))) {
            ti = (SectionClipboardInfo) inputStream.readObject();
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
