
package app.lib;

import app.exceptions.KBase_Ex;
import app.model.ConfigMainList;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.DocumentItem;
import app.model.business.IconItem;
import app.model.business.InfoHeaderItem;
import app.model.business.Info_FileItem;
import app.model.business.Info_ImageItem;
import app.model.business.template.TemplateFileItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

/**
 * Класс отвечает за кеширование документов и обязательных файлов на диске
 * @author Igor Makarevich
 */
public class FileCache {
	private Params params;
	/**
	 * Настройки программы
	 */
	ConfigMainList config;
	/**
	 * Соединения к БД
	 */
	DBConCur_Parameters conn;
	/**
	 * Id темы шаблонов
	 */
	long themeId;
	/**
	 * Путь директории для кеширования
	 */
	private String path;
	/**
	 * Имя файла документа, который кешировался последним
	 */
	private String docFileName;

	/**
     * Конструктор.
     */
    public FileCache (Params params, long themeId) {
    	this.params = params;
    	config = params.getConfig();
    	conn = params.getConCur();
    	this.themeId = themeId;
    	
    	path = FileUtil.getFullUserFileName(params, config.getItemValue("directories", "PathDirCache")) +
    			((conn.param.getConnId() > 0) ? conn.param.getConnId() : ("m"+(-1 * conn.param.getConnId()))) +"/"+
    			themeId +"/";
    }
	
	/**
	 * Проверяет наличие директории. Если ее нет, тогда создаем дерево директорій і файлів.
	 */
	public void createDirAndFiles() throws KBase_Ex {
		File fDocDir = new File(path);
		
		if (! fDocDir.exists()) {
			if (! fDocDir.mkdirs())
				throw new KBase_Ex (-1, "FileCache.createDirAndFiles", "Невозможно создать директорию "+ path, this);
			
			File fDocDirFiles = new File(path +"_files/");
			if (! fDocDirFiles.mkdirs()) 
				throw new KBase_Ex (-2, "FileCache.createDirAndFiles", "Невозможно создать директорию "+ path +"_files/", this);
			
			// create subdirectories and files
			createDirAndFiles_Recursive(0, themeId, 0, path+"_files/");
		}
	}
	
	private void createDirAndFiles_Recursive (long parentId, long themeId, int type, String curPath) {
		List<TemplateFileItem> filesList = conn.db.templateFileListByType(parentId, themeId, type);
		
		for (TemplateFileItem i : filesList) {
			if ((i.getType() == 0) || (i.getType() == 10)) {   // file
				i.saveToDisk(curPath);
			} else {     // directory
				String curPathNew = curPath+i.getFileName()+"/";
				File fileDir = new File(curPathNew);
				if (fileDir.mkdirs()) {
					createDirAndFiles_Recursive (i.getId(), themeId, type, curPathNew);
				} else {
					ShowAppMsg.showAlert("ERROR", "error", 
							"Помилка при створенні директорії на диску, FileCache.createDirAndFiles_Recursive", 
							curPathNew);
				}
			}
		}
	}
	
	/**
	 * Создает файл документа на диске, если такого файла нет или он устарел.
	 */
	public void createFileDoc (DocumentItem doc) {
		docFileName = "doc_"+ doc.getSectionId() + ".html";
		boolean isNeedFileCache = false;
    	File f = new File(path + docFileName);
		
		if (f.exists()) {
			if (doc.getDateModified().compareTo(new Date(f.lastModified())) > 0) {
				isNeedFileCache = true;
			}
		} else {
			isNeedFileCache = true;
		}
		
		if (isNeedFileCache) {
			doc.saveToDisk(path + docFileName);
		}
	}
	
	/**
	 * Если уже есть директория кеша с файлами, тогда записываем указанный файл (чи директорію).
	 */
	public void createTemplateFile (TemplateFileItem tf) {
		File fDocDir = new File(path+"_files/");
		String curPath = path +"_files/"+ conn.db.templateFileGetPathName(tf.getId(), "/", false);
		//System.out.println("curPath = " + curPath);
	
		if (fDocDir.exists()) {
			tf.saveToDisk(curPath);
		}
	}
	
	/**
	 * Записывает файлы инфо блоков "Изображение" и "Файл" для указанного раздела.
	 */
	public void createFilesOfInfoBlocksForDoc (long sectionId) throws KBase_Ex {
		String sFilesDir = path + "files_" + Long.toString(sectionId) + "/";
		File fFilesDir = new File (sFilesDir); 
	
		if (! fFilesDir.exists()) {
			if (! fFilesDir.mkdirs())
				throw new KBase_Ex (-3, "FileCache.createFilesOfInfoBlocksForDoc", "Невозможно создать директорию "+ fFilesDir.getName(), this);
		}
		
		for (InfoHeaderItem ih : conn.db.infoListBySectionId(sectionId)) {
			if (ih.getInfoTypeId() == 2) {            // Изображение
				Info_ImageItem ii = conn.db.info_ImageGet(ih.getInfoId());
				FileUtil.writeImageFile(sFilesDir+"image_" + Long.toString(ih.getId()) + ".png", ii.image);
			} else if (ih.getInfoTypeId() == 3) {     // Файл
				Info_FileItem ii = conn.db.info_FileGet(ih.getInfoId());
				FileUtil.writeBinaryFile(
						sFilesDir+"file_" + Long.toString(ih.getId()) + "." + FileUtil.getFileExt(ii.getName()), 
						ii.getFileBody());
				// write icon file
				IconItem ic = conn.db.iconGetById(ii.getIconId());
				if (ic != null) {
					FileUtil.writeImageFile(sFilesDir + "file_" + Long.toString(ih.getId()) + "_image.png", ic.image);
				} else {	// копируем иконку по умолчанию
					Path src = Paths.get("resources/images/icon_file_unknown.png");
			        Path dest = Paths.get(sFilesDir + "file_" + Long.toString(ih.getId()) + "_image.png");
			        try {
						Files.copy(src, dest);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Геттер path
	 */
	public String getPath () {  return path;  }
	
	/**
	 * Геттер DocFileName
	 */
	public String getDocFileName () {  return docFileName;  }
}
