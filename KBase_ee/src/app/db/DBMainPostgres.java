package app.db;

import java.security.GeneralSecurityException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.AEADBadTagException;

import app.exceptions.DataConnectionException;
import app.exceptions.DataQueryException;
import app.exceptions.KBase_DbConnEx;
import app.lib.KeyStorePrg;
import app.lib.ShowAppMsg;
import app.model.FindParams;
import app.model.FindResultItem;
import app.model.Params;
import app.model.business.DictionaryItem;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateStyleItem;

/**
 * 
 */
public class DBMainPostgres extends DBMain {

	/**
	 * @param dbURL
	 * @param user
	 * @param password
	 * @throws KBase_DbConnEx
	 * 
	 * OLD
	 */
	public DBMainPostgres(Params params, String dbURL, String user, KeyStorePrg.EncBlob password) throws KBase_DbConnEx {
		super(params, dbURL, user, password);
	}

	/**
	 * 
	 */
	public DBMainPostgres (Params params, String host, String port, String name, 
			String user, KeyStorePrg.EncBlob password)  throws DataConnectionException {
		super(params, host, port, name, user, password);
		
		dbURL = "jdbc:postgresql://"+ host +":"+ port +"/"+ name;
		
		open();
	}
	
	/**
	 * Створюємо з'єднання з БД 
	 */
	void open () throws DataConnectionException {
		con = null;
		
		try {
    		Class.forName("org.postgresql.Driver");
    	} catch (ClassNotFoundException e) {
    		throw new DataConnectionException (
    				DataConnectionException.ERRCODE_DRIVER_NOT_FOUND, "open", 
    				"PostgreSQL JDBC Driver is not found. Include it in your library path", 
    				e, 1, null, "ClassNotFoundException");
    	}
    	
		char[] decrypted = null;
    	try {
   		    decrypted = KeyStorePrg.decrypt(params, password);
   		    con = DriverManager.getConnection(dbURL, user, new String(decrypted));
   		} catch (AEADBadTagException e) {
   		    // Неправильний ключ / IV / підробка / невірний AAD
   		    // Обробіть як "невалідні дані"
   			throw new DataConnectionException (
    				DataConnectionException.ERRCODE_SEQURITY, "open", 
    				"Помилка підключення (AEADBadTagException) "+dbURL, 
    				e, 1, null, "AEADBadTagException");
   		} catch (GeneralSecurityException e) {
   			throw new DataConnectionException (
    				DataConnectionException.ERRCODE_SEQURITY, "open", 
    				"Помилка підключення (GeneralSecurityException) "+dbURL, 
    				e, 1, null, "GeneralSecurityException");
   		} catch (SQLException e) {
   			throw new DataConnectionException (
    				DataConnectionException.ERRCODE_GET_CONNECTION, "open", 
    				"Помилка підключення (SQLException) "+dbURL, 
    				e, 1, null, "SQLException");
   		} catch (Exception e) {
   			throw new DataConnectionException (
    				DataConnectionException.ERRCODE_GET_CONNECTION, "open", 
    				"Помилка підключення (Exception) "+dbURL, 
    				e, 1, null, "Exception");
   		} finally {
   		    if (decrypted != null) Arrays.fill(decrypted, '\0'); // затерти, коли більше не потрібен
   		}
    	
    	if (con == null) {
    		throw new DataConnectionException (
    				DataConnectionException.ERRCODE_GET_CONNECTION, "open", 
    				"Помилка підключення (con == null) "+dbURL, 
    				null, 1, null, null);
    	}
    	
    	//-------- set search_path for schemas
    	executeQuery("SELECT pg_catalog.set_config('search_path', 'kbase,\"$user\",public', false);");
    	
    	//------- check version
		String versionProgram = params.getConfigSys().getItemValue("DB", "Required version.KBase Pg");
		String versionDB = null;

		try {
			versionDB = settingsGetValue("VERSION_DB_NUMBER");
		} catch (DataQueryException e) {
			throw new DataConnectionException (
					DataConnectionException.ERRCODE_BAD_DB_VERSION, "open",
					"Помилка читання версії БД "+dbURL,
					e.getException(), 2, e, "Exception");
		}

		versionProgram = versionProgram.substring(0, versionProgram.lastIndexOf("."));
		versionDB = versionDB.substring(0, versionDB.lastIndexOf("."));
		if (versionProgram.compareTo(versionDB) != 0) {
			throw new DataConnectionException (
					DataConnectionException.ERRCODE_BAD_DB_VERSION, "open",
					"Версія БД "+ versionDB +" не підтримується цією версією програми.\n"+dbURL,
					null, 1, null, null);
		}
	}
	
	/**
	 * 
	 */
	protected String getCurrentUser () {
		String retVal = "";
		
		checkConnect();
		
		try {
			String stm = "select \"current_user\"()";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getString(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "getCurrentUser()");
    	}
		
		return retVal;
	}
	
	/**
	 * 
	 */
	protected long getNextId (String sequenceName) {
		long retVal = -1;
		
		checkConnect();
		
		try {
			String stm = "select nextval(?);";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setString(1, sequenceName);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getLong(1);

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "getNextId("+ sequenceName +")");
    	}
		
		return retVal;
	}

	/**
	 * Пошук інформації в базі знань
	 * @param findParams
	 * @return
	 */
	public List<FindResultItem> findInfo (FindParams findParams) {
		List<FindResultItem> retVal = new ArrayList<FindResultItem>();

		checkConnect();

		try {
			String stm = """
					select f.* from kbase.find_info(
						objSection => ?,
						objInfoHeader => ?,
						objDictionary => ?,
						objText => ?,
						objImage => ?,
						objFile => ?,
						search_text => ?,
						text_ignore_registr => ?
						) f
					""";
			PreparedStatement pst = con.prepareStatement(stm);
			int i = 1;
			pst.setBoolean(i++, findParams.isObjSection());
			pst.setBoolean(i++, findParams.isObjInfoHeader());
			pst.setBoolean(i++, findParams.isObjDictionary());
			pst.setBoolean(i++, findParams.isObjText());
			pst.setBoolean(i++, findParams.isObjImage());
			pst.setBoolean(i++, findParams.isObjFile());
			pst.setString (i++, findParams.getText());
			pst.setBoolean(i++, findParams.isTextIgnoreRegistr());
			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				retVal.add(new FindResultItem(
						rs.getString("texttype"),               // textType
						rs.getLong("sectionid"),                // sectionId
						rs.getString("sectionname"),            // sectionName
						rs.getString("sectionpathname"),        // sectionPathName
						rs.getLong("infoid"),                   // infoId
						rs.getString("infoname"),               // infoName
						rs.getString("text"),                   // text
						rs.getObject("date_created", LocalDateTime.class), // dateCreated
						rs.getObject("date_modified", LocalDateTime.class), // dateModified
						rs.getString("user_created"),           // userCreated
						rs.getString("user_modified")           // userModified
						));
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных",
					"findInfo()");
			e.printStackTrace();
		}

		return retVal;
	}
	//TODO

	/**
	 * Пиктограмма. Удаление пиктограммы со всеми подчиненными пиктограммами.
	 * @param
	 */
	public void iconDelete (long id) throws KBase_DbConnEx {
		PreparedStatement pst = null;
		
		checkConnect();
	
		try {
			String stm = """
					WITH RECURSIVE x AS ( 
						SELECT id 
					     FROM icons 
					    WHERE id = ? 
					   UNION  ALL 
					   SELECT a.id 
					     FROM x 
					     JOIN icons a ON a.parent_id = x.id 
					) 
					DELETE FROM icons a 
					 USING  x 
					 WHERE a.id = x.id
					""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	
        	//ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			//		             "Ошибка при удалении пиктограммы.");
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             ex.getMessage());
        	throw new KBase_DbConnEx ("iconDelete ("+ id +")", this);
        }
	}
	
	/**
	 * Инфо блок. Удаление одного инфо блока.
	 * @param
	 */
	public void infoDelete (long infoHeaderId) {
		PreparedStatement pst = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT info_delete1(?);";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, infoHeaderId);

            ResultSet rs = pst.executeQuery();
			rs.next();
			
			rs.close();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при удалении инфо блока, infoDelete().", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Проверяет на дубль добавляемую позицию и при необходимости перенумеровывает всю последовательность блоков в разделе.
	 * Возвращает новое значение newPosition в списке после перенумерации.
	 */
	public long infoPositionCheckAndRenumber (long sectionId, long newPosition) {
		String stm;
		long retVal = 0;
		
		checkConnect();
		
		stm = "SELECT InfoPositionCheckAndRenumber (?,?) as position_new;";
			
		try {
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			pst.setLong (2, newPosition);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getLong("position_new");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "infoPositionCheckAndRenumber");
		}
		
		return retVal;
	}
	
	/**
	 * Раздел. Копирование всех инфо блоков с одного раздела в другой.
	 * @param
	 */
	public void sectionCopyInfoBlocks (long sectionSrcId, long sectionTrgId) {
		PreparedStatement pst = null;
		
		checkConnect();

		try {
			String stm = "SELECT section_copyInfoBlocks(?,?);";
			pst = con.prepareStatement(stm);
			pst.setLong  (1, sectionSrcId);
			pst.setLong  (2, sectionTrgId);

			ResultSet rs = pst.executeQuery();
			rs.next();

			rs.close();
			pst.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
			ShowAppMsg.showAlert(
					"WARNING", "db error",
					"Ошибка при копировании всех инфо блоков с одного раздела в другой (sectionCopyInfoBlocks).",
					ex.getMessage());
		}
	}
	
	/**
	 * Раздел. Удаление раздела со всеми подчиненными разделами.
	 * @param
	 */
	public void sectionDelete (long id) {
		PreparedStatement pst = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT section_delete(?);";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);

            ResultSet rs = pst.executeQuery();
			rs.next();
			
			rs.close();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при удалении раздела, sectionDelete().", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Возвращает id пиктограммы по умолчанию для указанного раздела
	 * @param sectionId ;
	 *        isRecursive : true - с проходом вверх до корня (и по умолчанию для всех разделов, если ничего не указано)
	 */
	public long sectionGetIconIdDefault (long sectionId, boolean isRecursive) {
		String stm;
		long retVal = 0;
		
		checkConnect();
		
		if (isRecursive) {
			stm = "SELECT Section_GetIconIdDefault (?) as icon_id_def;";
			
		} else {
			stm = "SELECT icon_id_def  FROM sections  WHERE id = ?";
		}
		
		try {
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getLong("icon_id_def");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при получении id пиктограммы по умолчанию для раздела ( sectionGetIconIdDefault("+ sectionId +") ).");
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает цепочку имен разделов от указанного до самого верхнего родителя. 
	 */
	public String sectionGetPathName (long sectionId, String delimiter) {
		String retVal = "";
		
		checkConnect();
		
		try {
			String stm = "SELECT section_get_pathname(?,?) AS path_name ;";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong  (1, sectionId);
			pst.setString(2, delimiter);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
			retVal = rs.getString("path_name");
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при получении пути раздела ( sectionGetPathName() ).");
    	}
		
		return retVal;
	}
	
	/**
	 * Повертає головний шаблон для вказаного розділа 
	 */
	public TemplateItem sectionGetTemplateMain (long sectionId, long themeId) {
		String stm;
		String styleTag;
		TemplateStyleItem si;
		TemplateItem retTI = null;
		
		checkConnect();
		
		stm = "SELECT section_get_styleMain_tag (?) as template_id ";
		
		try {
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			styleTag = rs.getString("template_id");
			
			rs.close();
			pst.close();
			
			si = templateStyleGetByTag (styleTag);
			retTI = templateGet (themeId, si.getId());
		} catch (SQLException e) {
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , sectionGetTemplateMain ("+sectionId+")", 
		             e.getMessage());
    		e.printStackTrace();
    	}
		
		return retTI;
	}
	
	/**
	 * Возвращает id темы для указанного раздела
	 * @param sectionId ;
	 *        isRecursive : true - с проходом вверх до корня (и по умолчанию для всех разделов, если ничего не указано)
	 */
	public long sectionGetThemeId (long sectionId, boolean isRecursive) {
		String stm;
		long retVal = 0;
		
		checkConnect();
		
		if (isRecursive) {
			stm = "SELECT Section_GetThemeId (?) as theme_id;";
			
		} else {
			stm = "SELECT theme_id  FROM sections  WHERE id = ?";
		}
		
		try {
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getLong("theme_id");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , sectionGetThemeId ("+sectionId+")", 
		             e.getMessage());
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Файл для шаблона. Вилучення файла/директорії з усією ієрархією.
	 * @param
	 */
	public void templateFileDelete (long id) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			String stm = "SELECT TemplateFile_delete (?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);

            ResultSet rs = pst.executeQuery();
            rs.close();
            pst.close();
        } catch (SQLException e) {
        	e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , templateFileDelete ("+id+")", 
		             e.getMessage());
        }
	}
	
	/**
	 * Возвращает цепочку имен директорій от указанного файла(директорії) до самого верхнего родителя.
	 * withFileName - шлях з кінцевим іменем файла (директорії) чи без 
	 */
	public String templateFileGetPathName (long fileId, String delimiter, boolean withFileName) {
		String retVal = "";
		
		checkConnect();
		
		try {
			String stm = "SELECT template_file_get_pathname(?,?,?) AS path_name ;";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong  (1, fileId);
			pst.setString(2, delimiter);
			pst.setInt   (3, ((withFileName) ? 0 : 1));
			ResultSet rs = pst.executeQuery();
			
			rs.next();
			retVal = rs.getString("path_name");
			
	        rs.close();
	        pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при получении пути ( templateFileGetPathName() ).");
		}
		
		return retVal;
	}
	
	/**
	 * Стили шаблонов. Вилучення стиля/директорії з усією ієрархією та зв'язками з шаблонами по его Id.
	 */
	public void templateStyleDelete (long id) {
		PreparedStatement pst = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT TemplateStyle_delete (?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);

            ResultSet rs = pst.executeQuery();
            rs.close();
            pst.close();
        } catch (SQLException e) {
        	e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , templateStyleDelete ("+id+")", 
		             e.getMessage());
        }
	}
	
	/**
	 * Возвращает стиль по умолчанию в указанной теме для указанного типа инфо блока.
	 * Если такого стиля нет, возвращается null.
	 */
	public TemplateStyleItem templateStyleGetDefault (long themeId, long infoTypeId) {
		long styleId = -1;
		
		checkConnect();
	
		try {
			String stm = "SELECT TemplateStyle_getIdDefault(?,?,3) AS id ;";
     		PreparedStatement pst = con.prepareStatement(stm);
	    	pst.setLong  (1, themeId);
		    pst.setLong  (2, infoTypeId);
		    ResultSet rs = pst.executeQuery();
			
		    rs.next();
		    styleId = rs.getLong("id");
		    
		    rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", 
    				"Ошибка при работе с базой данных , templateStyleGetDefault ("+themeId+","+infoTypeId+")", 
		            e.getMessage());
    	}
		
		return templateStyleGet(styleId);
	}
	
	/**
	 * функция устанавливает указанный стиль как дефолтный для указанной темы
	 */
	public void templateStyleSetDefault (long themeId, long templateStyleId) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
            String stm = "SELECT TemplateStyle_setdefault (?, ?);";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, themeId);
            pst.setLong  (2, templateStyleId);
            ResultSet rs = pst.executeQuery();
            
            rs.close();
            pst.close();
        } catch (SQLException e) {
        	e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных, templateStyleSetDefault", 
					             e.getMessage());
		}
	}
	
	/**
	 * функция удаляет указанный стиль как дефолтный для указанной темы
	 */
	public void templateStyleUnsetDefault (long themeId, long templateStyleId) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
            String stm = "SELECT TemplateStyle_unsetdefault (?, ?);";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, themeId);
            pst.setLong  (2, templateStyleId);
            ResultSet rs = pst.executeQuery();
            
            rs.close();
            pst.close();
        } catch (SQLException e) {
        	e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных, templateStyleUnsetDefault", 
					             e.getMessage());
		}
	}
	
	/**
	 * Стили шаблонов. Удаление всех стилей.
	 * !!! не тестував
	 */
	public void templateStylesDelete () {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			String stm = "SELECT TemplateStyles_delete ()";
            pst = con.prepareStatement(stm);

            ResultSet rs = pst.executeQuery();
            rs.close();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "TemplateStyles_delete ()");
        }
	}
	
	/**
	 * Тема для шаблонов. Удаление темы.
	 */
	public void templateThemeDelete (long id) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			String stm = "SELECT TemplateTheme_delete (?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);

            ResultSet rs = pst.executeQuery();
            rs.close();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "templateThemeDelete ("+ id +")");
        }
	}
	
	/**
	 * Шаблон. Вилучення шаблону/директорії з усією ієрархією та зв'язками зі стилями.
	 * @param
	 */
	public void templateDelete (long id) {
		PreparedStatement pst = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT Template_delete (?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);

            ResultSet rs = pst.executeQuery();
            rs.close();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "templateDelete ("+ id +")");
        }
	}
}
