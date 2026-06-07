
package app.lib;

import app.exceptions.KBase_ReadTextFileUTFEx;
import app.model.Params;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

/**
 * класс по по работе с файлами
 * 
 * @author Igor Makarevich
 * @version 0.3.003 (10.2017 - 13.02.2018)
 */
public class FileUtil {
	
	/**
	 * This is the conventional way of file copy in java.
	 * @param source
	 * @param dest
	 * @throws IOException
	 */
	public static void copyFile (File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}

	/**
     * Возвращает тип Файла (определяется по расширению).
     * @return 0 - тип не определен ; 1 - текстовый ; 2 - картинка
     */
    public static int getFileTypeByExt (String fileName) {
    	String strExt = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
    	int retVal = 0;
    	
    	// check file type : text or image
    	switch (strExt) {
    	case "html" :
    	case "css" :
    	case "js" :
    	case "txt" :
    		retVal = 1;
    		break;
    	case "png" :
    	case "gif" :
    	case "jpg" :
    	case "jpeg" :
    		retVal = 2;
    		break;
    	default :
    		retVal = 0;
    	}
    
    	return retVal;
    }
    /**
     * Возвращает расширение файла, взятое из имени
     */
    public static String getFileExt (String fileName) {
    	return fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
    }

    /**
     * Read binary file and return byte[]
     */
    public static byte[] readBinaryFile (String fileName) {
    	File file = new File(fileName);
    	byte[] retVal = null;
    	
    	try {
			retVal = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			//e.printStackTrace();
			ShowAppMsg.showAlert("ERROR", "readBinaryFile", "Error read file", fileName);
		}
    	
    	return retVal;
    }
    
    /**
     * Читает текстовый файл и возвращает String.
     * Файл должен быть в кодировке UTF-8
     */
    public static String readTextFileToString (String fileName) throws KBase_ReadTextFileUTFEx {
    	String retVal = null;
    	
    	try {
    		retVal = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)
					.collect(Collectors.joining(System.lineSeparator()));
    	} catch (UncheckedIOException em) {
			throw new KBase_ReadTextFileUTFEx (fileName, "Файл в кодировке отличной от UTF-8");
		} catch (IOException e) {
			throw new KBase_ReadTextFileUTFEx (fileName, "Ошибка чтения файла");
		}
    	
    	return retVal;
    }
    
    /**
     * Читає з диска картинку типа Image з файла у форматі PNG
     * @throws KBase_ReadTextFileUTFEx 
     */
    public static Image readImageFile (String fileName) throws KBase_ReadTextFileUTFEx {
    	Image image = null;
    	File fFile = new File(fileName);
    	BufferedImage bImage;
    	
		if(fFile != null){
			try {
				bImage = ImageIO.read(fFile);
				image = SwingFXUtils.toFXImage(bImage, null);
			} catch (IOException e) {
				//e.printStackTrace();
				//showAlert("WARNING", "Сохранение файла на диск", "Ошибка записи файла с картинкой.", 
            	//		"Файл "+ fileName +" не сохранен.");
				throw new KBase_ReadTextFileUTFEx (fileName, "Помилка читання файла.");
			}
        }
    	
    	return image;
    }
    
    /**
     * Сохраняется строка в текстовый файл с кодировкой UTF_8
     */
    public static void writeTextFile (String fileName, String text) {
    	//BufferedWriter writer = null;
    	Writer writer = null;
		
		try {
			//writer = new BufferedWriter( new FileWriter(FilePath));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
		    writer.write(text);

		} catch ( IOException e) {
			ShowAppMsg.showAlert("WARNING", "Сохранение файла на диск", "Ошибка записи текстового файла.", 
		             "Файл "+ fileName +" не сохранен.");
		}
		finally {
		    try {
		        if ( writer != null)  writer.close( );
		    } catch ( IOException e) {     }
		}
    }
    
    /**
     * Сохраняет картинку типа Image в файл в формате PNG
     */
    public static void writeImageFile (String fileName, Image image) {
    	File fFile = new File(fileName);
		if(fFile != null){
        	BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
            try {
              ImageIO.write(bImage, "png", fFile);
            } catch (IOException e) {
            	ShowAppMsg.showAlert("WARNING", "Сохранение файла на диск", "Ошибка записи файла с картинкой.", 
            			"Файл "+ fileName +" не сохранен.");
            	throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * Сохраняет набор байтов в двоичный файл
     */
    public static void writeBinaryFile (String fileName, byte[] fileBody) {
    	File fFile = new File(fileName);
		
    	if (fFile != null) {
    		try (FileOutputStream fos = new FileOutputStream(fFile)) {
                fos.write(fileBody, 0, fileBody.length);
            }
            catch(IOException e){
            	ShowAppMsg.showAlert("WARNING", "Сохранение файла на диск", "Ошибка записи бинарного файла (writeBinaryFile).", 
            			"Файл "+ fileName +" не сохранен.");
            	throw new RuntimeException(e);
            }
//    		finally {
//    			fos.close();
//    		}
		}
    }
    
    /**
     * повертаю користувацьку директорію для додатків
     * @return
     */
    public static Path getUserDir() {
    	//String vendor = "KBase"; 
    	//String app = "KBase_ee";
    	
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA"); // Roaming
            if (appData == null || appData.isBlank()) {
                appData = System.getProperty("user.home");
            }
            //return Paths.get(appData, vendor, app);
            return Paths.get(appData);
        } else if (os.contains("mac")) {
            //return Paths.get(System.getProperty("user.home"), "Library", "Application Support", vendor, app);
        	return Paths.get(System.getProperty("user.home"), "Library", "Application Support");
        } else { // Linux/Unix
            String xdg = System.getenv("XDG_CONFIG_HOME");
            if (xdg == null || xdg.isBlank()) {
                xdg = Paths.get(System.getProperty("user.home"), ".config").toString();
            }
            //return Paths.get(xdg, vendor, app);
            return Paths.get(xdg);
        }
    }
    
    /**
     * Якщо ім'я користувацького файла не містить повний шлях і в програмі вказаний префікс, 
     * то створюємо повний шлях
     * @param filename
     * @return
     */
    public static String getFullUserFileName (Params params, String filename) {
    	String prefix = params.getConfigSysVars().getItemValue("Program", "UserFilesPathPrefix");
    	
    	// немає префіксу
    	if (prefix.equals(""))  return filename;
    	
    	// шлях вже повний
    	Path path = Paths.get(filename);
        if (path.isAbsolute())  return filename;

        // get user dir
        String userDir = getUserDir().toString();
        prefix = prefix.replace("{HOMEDIR}", userDir);
        
        try {
			Files.createDirectories(Paths.get(prefix));
		} catch (IOException e) {
			ShowAppMsg.showAlert("ERROR", "Створення директорії", "Помилка при створенні директорії", prefix);
			e.printStackTrace();
		}
        
        return prefix +"/"+ filename;
    }
}
