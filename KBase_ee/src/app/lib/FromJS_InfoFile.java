package app.lib;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import app.exceptions.DataConnectionException;
import app.exceptions.DataQueryException;
import app.model.Params;
import app.model.business.InfoHeaderItem;
import app.model.business.Info_FileItem;
import javafx.stage.FileChooser;

/**
 * Class for working with info block File from JavaScript
 * @author IMakarevich
 */
public class FromJS_InfoFile {
	private Params params;
	private Preferences prefs;
	
	/**
	 * Constructor
	 */
	public FromJS_InfoFile (Params params) {
		this.params = params;
		prefs = Preferences.userNodeForPackage(FromJS_InfoFile.class);
	}
	
	/**
	 * Save info block on disk
	 * @param infoHeaderId
	 */
	public void saveToDisk (String infoHeaderId, String fileName) {
		FileChooser fileChooser = new FileChooser();
    	String curDir;
    	
    	//---- open dialog SaveFile
    	fileChooser.setTitle("Save file to disk");
    	fileChooser.setInitialFileName(fileName);
    	
    	// Задаём фільтр розширень
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);
        
        // set directory
        curDir = prefs.get("FromJSInfoFile_CurDirNameForSaveFile", "");
        if (! curDir.equals("")) {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory()) {
                fileChooser.setInitialDirectory(dir);
            }
        }
        
        // Показываем диалог загрузки файла
        File file = fileChooser.showSaveDialog(params.getStageCur());
    	
    	//-------- save file
        if (file != null) {
        	try {
	        	InfoHeaderItem ihi = params.getConCur().db.infoGet(Long.parseLong(infoHeaderId));
	        	Info_FileItem ifi = params.getConCur().db.info_FileGet(ihi.getInfoId());
	        	
	        	try {
					ifi.saveToFile(file.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
					ShowAppMsg.showAlert("ERROR", "Error copy file", "Error copy file", 
							"from '"+ifi.getName()+"' to '"+file.getAbsolutePath()+"'");
				}
	        	
	        	curDir = file.getAbsolutePath();
	        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
	        	prefs.put("FromJSInfoFile_CurDirNameForSaveFile", curDir);
        	} catch (DataConnectionException | DataQueryException e) {
				e.writeLog(params);
				ShowAppMsg.showAlert(
	                    "ERROR", "Помилка при збереженні інфо блоку в файл.\n"+
	                    "Збереження скасовано.",
	                    Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
			}
        }
	}
}
