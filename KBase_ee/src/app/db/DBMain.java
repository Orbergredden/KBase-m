package app.db;

import app.exceptions.DataConnectionException;
import app.exceptions.DataQueryException;
import app.exceptions.KBase_DbConnEx;
import app.lib.KeyStorePrg;
import app.lib.LogFileUser;
import app.lib.ShowAppMsg;
import app.model.FindParams;
import app.model.FindResultItem;
import app.model.Params;
import app.model.business.DictionaryItem;
import app.model.business.DocumentItem;
import app.model.business.IconItem;
import app.model.business.InfoHeaderItem;
import app.model.business.InfoTypeItem;
import app.model.business.Info_FileItem;
import app.model.business.Info_ImageItem;
import app.model.business.Info_TextItem;
import app.model.business.SectionFavoriteItem;
import app.model.business.SectionItem;
import app.model.business.template.TemplateFileItem;
import app.model.business.template.TemplateSimpleItem;
import app.model.business.template.TemplateStyleItem;
import app.model.business.template.TemplateThemeItem;
import app.model.business.template.TemplateItem;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * Основний (кореневий) клас для роботи з БД
 * v.2.00.00.018 2025-02-07
 * @author Igor Makarevich
 */
public abstract class DBMain {
	protected Params params;
	
	protected String dbURL;
	protected String host;
	protected String port;
	protected String name;
	protected String user; 
	protected KeyStorePrg.EncBlob password;
	
	// Дескриптор соединения с сервером БД
	protected Connection con;

	/**
	 * Конструктор
	 * @throw KBase_DbConnEx
	 * @param dbURL
     * @param user
     * @param password
     * 
     * OLD
	 */
	public DBMain (Params params, String dbURL, String user, KeyStorePrg.EncBlob password)  throws KBase_DbConnEx {
		this.params = params;
		this.dbURL = dbURL;
		this.user = user;
		this.password = password;
	}
	
	/**
	 * 
	 */
	public DBMain (Params params, String host, String port, String name, 
			String user, KeyStorePrg.EncBlob password)  throws DataConnectionException {
		this.params = params;
		this.host = host;
		this.port = port;
		this.name = name;
		this.user = user;
		this.password = password;
	}
	
	/**
	 * Створбємо з'єднання з БД 
	 */
	abstract void open () throws DataConnectionException;
	
	/**
	 * Закрываем соединение
	 */
	public void close() throws DataConnectionException {
		try {
			if (con != null) {  
				con.close();
			}
    	} catch (SQLException e) {
    		throw new DataConnectionException (
    				DataConnectionException.ERRCODE_CLOSE_CONNECTION, "close", 
    				"Помилка закриття з'єднання з БД "+dbURL, 
    				e, 1, null, "SQLException");
    	}
	}
	
	/**
	 * reconect
	 */
	public boolean reconnect() throws DataConnectionException {
		boolean retVal = true;
		
		close();
		open();
		
		return retVal;
	}
	
	/**
	 * Перевіряє наявність конекта до БД. - старий варіант з ігноруванням винятків
	 * У випадку відсутності конекту робить спробу переконектитися. 
	 */
	protected void checkConnect() {
		try {
			if ((con == null) || (! con.isValid(1))) {
				boolean isReconnected = reconnect();
				
				if (! isReconnected) { 
					// до сюди ніколи не доходить
					throw new DataConnectionException (
		    				DataConnectionException.ERRCODE_OTHERS, "checkConnect", 
		    				"Неможливо переконектитися "+dbURL, 
		    				null, 1, null, null);
				} else {
					LogFileUser.write(params, dbURL +" : Reconnect OK");
				}
			}
		} catch (DataConnectionException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Перевіряє наявність конекта до БД.
	 * У випадку відсутності конекту робить спробу переконектитися.
	 * якщо не переконектився, то передає ексепшен з методу open() 
	 */
	protected void checkConnectEx() throws DataConnectionException {
		try {
			if ((con == null) || (! con.isValid(1))) {
				boolean isReconnected = reconnect();
				
				if (! isReconnected)  
					// до сюди ніколи не доходить
					throw new DataConnectionException (
		    				DataConnectionException.ERRCODE_OTHERS, "checkConnect", 
		    				"Неможливо переконектитися (reconnect) "+dbURL, 
		    				null, 1, null, null);
				else
					LogFileUser.write(params, dbURL +" : Reconnect OK");
			}
		} catch (SQLException e) {
			throw new DataConnectionException (
    				DataConnectionException.ERRCODE_OTHERS, "isValid", 
    				"Неможливо переконектитися (isValid) "+dbURL, 
    				null, 1, null, null);
		}
	}
	
	/**
	 * Выполняет запрос и возвращает строку. 
	 * це щось старе
	 */
	public String executeQuery (String strSQL) {
		String retVal = "";
		
		try {
			String stm = strSQL;
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
			//retVal = rs.getString("path_name");
			retVal = rs.getString(1);
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		//System.out.println("execute query Failed");
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка выполнения executeQuery.");
    	}
		
		return retVal;
	}
	
	/**
	 * 
	 */
	abstract public String getCurrentUser ();
	
	/**
	 * 
	 */
	abstract long getNextId (String sequenceName);
	
	/**
	 * додає параметер-значення до запиту типу дата 
	 * @throws SQLException 
	 */
	protected void pstSetDate (PreparedStatement pst, int pos, java.util.Date date) throws SQLException {
		java.util.Date pstDate;
		
		if (date != null) {
			pstDate = date;
		} else {
			pstDate = new java.util.Date();
		}
		
		pst.setTimestamp(pos, new java.sql.Timestamp(pstDate.getTime()));
	}
	
	/**
	 * Словник. Додавання нового елемента.
	 */
	public void dictAdd (DictionaryItem i) {
		String stm;
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
            stm = """
            		INSERT INTO dict (id, section_id, type_id, 
            	                    name, value, descr, rating, reverse, status, 
            		                date_viewed, date_created, date_modified, user_created, user_modified) 
            		 	VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            		      """;
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setLong  (2, i.getSectionId());
            pst.setLong  (3, i.getTypeId());
            pst.setString(4, i.getName());
            pst.setString(5, i.getValue());
            pst.setString(6, i.getDescr());
            pst.setInt   (7, i.getRating());
            pst.setInt   (8, i.getReverse());
            pst.setInt   (9, i.getStatus());
            pstSetDate (pst, 10, i.getDateViewed());
            pstSetDate (pst, 11, i.getDateCreated());
            pstSetDate (pst, 12, i.getDateModified());
            if ((i.getUserCreated() != null) && (i.getUserCreated().length() > 0)) {
            	pst.setString(13, i.getUserCreated());
			} else {
				pst.setString(13, getCurrentUser());
			}
            if ((i.getUserModified() != null) && (i.getUserModified().length() > 0)) {
				pst.setString(14, i.getUserModified());
			} else {
				pst.setString(14, getCurrentUser());
			}
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з базою даних", 
					             "Помилка при додаванні нового елементу словника, dictAdd().");
		}
	}
	
	/**
	 * Словник. Вилучення елемента словника.
	 * @param
	 */
	public void dictDelete (long id) {
		PreparedStatement pst = null;
		
		checkConnect();
	
		try {
			String stm = "DELETE FROM dict WHERE id = ?";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при вилученні елемента словника, dictDelete().", 
		             ex.getMessage());
        }
	}
	
	/**
	 * Словник. Вибираємо елемент по його id
	 */
	public DictionaryItem dictGetById (long id) {
		DictionaryItem retVal = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT id, section_id, type_id, name, value, descr, "+
	                     "       rating, reverse, status, "+
				         "       date_viewed, date_created, date_modified, user_created, user_modified "+
			             "  FROM dict " +
			             " WHERE id = ? ";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();

			java.util.Date dateTmpView;
			Timestamp timestampView = rs.getTimestamp("date_viewed");
			if (timestampView != null)  dateTmpView = new java.util.Date(timestampView.getTime());
			else                        dateTmpView = null;
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new DictionaryItem(
					rs.getLong("id"), 
         			rs.getLong("section_id"),
         			(int)rs.getLong("type_id"),
         			rs.getString("name"),
         			rs.getString("value"),
         			rs.getString("descr"),
         			rs.getInt("rating"),
         			rs.getInt("reverse"),
         			rs.getInt("status"),
         			dateTmpView,
         			dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified"));
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з базою даних", 
    				"dictGetById ("+id+")");
    	}
		
		return retVal;
	}
	
	/**
	 * Повертає список елементів сдщвника для вказаного розділу, сторінки та фільтру
	 */
	public List<DictionaryItem> dictListBySectionId (long sectionId) { 
		List<DictionaryItem> retVal = new ArrayList<DictionaryItem>();
		
		checkConnect();
		
		try {
			String stm = "SELECT id, section_id, type_id, name, value, descr, "+
		                 "       rating, reverse, status, "+
					     "       date_viewed, date_created, date_modified, user_created, user_modified "+
				         "  FROM dict " +
				         " WHERE section_id = ? " +
				         " ORDER BY name ";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpView;
				Timestamp timestampView = rs.getTimestamp("date_viewed");
				if (timestampView != null)  dateTmpView = new java.util.Date(timestampView.getTime());
				else                        dateTmpView = null;
				
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new DictionaryItem(
						rs.getLong("id"), 
						rs.getLong("section_Id"),
						rs.getInt("type_id"),
						rs.getString("name"),
						rs.getString("value"),
	         			rs.getString("descr"),
	         			rs.getInt("rating"),
	         			rs.getInt("reverse"),
	         			rs.getInt("status"),
	         			dateTmpView,
	         			dateTmpCre,
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "dictListBySectionId()");
    		e.printStackTrace();
    	}
	
		return retVal;
	}
	//tODO		add filter		add pageNumber (враховуючі колонку сортування)
	
	/**
	 * Видає наступний Id для додавання нового елемента словника
	 */
	public long dictNextId () {
		return getNextId("seq_dict");
	}
	
	/**
	 * Словник. Апдейтимо елемент.
	 */
	public void dictUpdate (DictionaryItem p) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
		
		try {
			stm = 	  "UPDATE dict " +
					  "   SET name = ?, value = ?, descr = ?, " +
					  "       rating = ?, reverse = ?, status = ?, "+
					  "       date_modified = ?, user_modified = ? " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setString(1, p.getName());
			pst.setString(2, p.getValue());
			pst.setString(3, p.getDescr());
            pst.setInt   (4, p.getRating());
            pst.setInt   (5, p.getReverse());
            pst.setInt   (6, p.getStatus());
            pstSetDate (pst, 7, new java.util.Date());
            pst.setString(8, getCurrentUser());
			pst.setLong  (9, p.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з базою даних", 
					             "Помилка при змені інформаціїї по елементу словника.");
		}
	}
	
	/**
	 * Документ. Добавление нового.
	 */
	public void documentAdd (DocumentItem i) {
		PreparedStatement pst = null;

		checkConnect();
		
		try {
            String stm = """
            		INSERT INTO documents (id, section_id, text, type,
            		                  date_created, date_modified, user_created, user_modified)
            			 VALUES(?, ?, ?, ?, ?, ?, ?, ?)
            		""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setLong  (2, i.getSectionId());
            pst.setString(3, i.getText());
            pst.setInt   (4, i.getType());
            pstSetDate (pst, 5, i.getDateCreated());
            pstSetDate (pst, 6, i.getDateModified());
            pst.setString(7, getCurrentUser());
            pst.setString(8, getCurrentUser());
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового скомпилированного документа (documentAdd).");
		}
	}
	
	/**
	 * Документ. true - документ присутствует в таблице кешированных документов.
	 */
	public boolean documentFindBySectionId (long sectionId) {
		boolean retVal = false;
	
		checkConnect();
		
		try {
			String stm = "SELECT count(id) as CountR " +
				         "  FROM documents " +
				         " WHERE section_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			if (rs.getLong("CountR") > 0)  retVal = true;
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных (documentFindBySectionId)", 
					e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Документ. Получаем документ по идентификатору раздела.
	 */
	public DocumentItem documentGetBySectionId (long sectionId) {
		DocumentItem retVal = null;
		
		checkConnect();
		
		try {
			String stm = "SELECT id, section_id, text, type,  " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM documents " +
				         " WHERE section_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new DocumentItem(
         			rs.getLong("id"), 
         			rs.getLong("section_id"),
         			rs.getString("text"),
         			rs.getInt("type"), 
         			dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified"));
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			ShowAppMsg.showAlert("WARNING", "db error", 
					"Ошибка при работе с базой данных, documentGetBySectionId (sectionId = "+ sectionId +")", 
					e.getMessage());
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Выдает следующий Id для добавления нового документа
	 */
	public long documentNextId () {
		return getNextId("seq_documents");
	}
	
	/**
	 * Документ. Изменение.
	 */
	public void documentUpdate (DocumentItem p) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
		
		try {
			stm = 	  "UPDATE documents " +
					  "   SET text = ?, type = ?, "+
					  "       date_modified = ?, user_modified = ? " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setString(1, p.getText());
			pst.setInt   (2, p.getType());
			pstSetDate (pst, 3, new java.util.Date());
            pst.setString(4, getCurrentUser());
			pst.setLong  (5, p.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении скомпилированного документа (documentUpdate()).");
		}
	}
	
	/**
	 * Пошук інформації в базі знань
	 * @param findParams
	 * @return
	 */
	abstract public List<FindResultItem> findInfo (FindParams findParams);

	/**
	 * Пиктограмма. Добавление новой.
	 */
	public void iconAdd (IconItem i) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
            String stm = """
            			INSERT INTO icons (id, parent_id, name, descr, image, 
            					date_created, date_modified, user_created, user_modified) 
            			 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)
            			 """;
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setLong  (2, i.getParentId());
            pst.setString(3, i.getName());
            pst.setString(4, i.getDescr());
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(i.image, null);
            ImageIO.write(bufferedImage, "png", outputStream);  // формат PNG
            byte[] imageBytes = outputStream.toByteArray();
            
            pst.setBytes(5, imageBytes);
            pstSetDate (pst, 6, i.getDateCreated());
            pstSetDate (pst, 7, i.getDateModified());
            pst.setString(8, getCurrentUser());
            pst.setString(9, getCurrentUser());
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении новой пиктограммы.");
        } catch (IOException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при добавлении новой пиктограммы (сохранение картинки).");
		}
	}
	
	/**
	 * Пиктограмма. Удаление пиктограммы со всеми подчиненными пиктограммами.
	 * @param
	 */
	public abstract void iconDelete (long id) throws KBase_DbConnEx;
	
	/**
	 * Пиктограмма. Сохраняем последнюю выбранную пиктограмму для текущего пользователя.
	 */
	@SuppressWarnings("resource")
	public void iconEditCurrent (long iconId) {
		String stm;
		int countR;
		
		checkConnect();
		
		try {
			//-------- check record for exist
			stm = """
					SELECT count(ci.id) CountR 
			          FROM current_icon ci  
                     WHERE ci."user" = ?
                  """;
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setString(1, getCurrentUser());
			ResultSet rs = pst.executeQuery();
			rs.next();

			countR = rs.getInt("CountR");
			
			rs.close();
			pst.close();
			
			//-------- insert style
			if (countR == 0) {
				stm = """
						INSERT INTO current_icon (id, "user", icon_id, date_created, date_modified) values 
						      			(?,?,?,?,?)
    					""";
				pst = con.prepareStatement(stm);
				pst.setLong  (1, getNextId ("seq_current_icon"));
				pst.setString(2, getCurrentUser());
				pst.setLong  (3, iconId);
				pstSetDate (pst, 4, new java.util.Date());
				pstSetDate (pst, 5, new java.util.Date());
			} else {
			//-------- update style
				stm = """
						UPDATE current_icon 
						   SET icon_id = ?, 
						       date_modified = ?
					     WHERE "user" = ?
					  """;
				pst = con.prepareStatement(stm);
				pst.setLong(1, iconId);
				pstSetDate (pst, 2, new java.util.Date());
				pst.setString(3, getCurrentUser());
			}
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных iconEditCurrent", 
					e.getMessage());
    	}
	}
	
	/**
	 * Пиктограмма. Получение информации по id
	 */
	public IconItem iconGetById (long id) {
		IconItem retVal = null;
		
		checkConnect();
		
		try {
			String stm = "SELECT id, parent_id, name, descr, image, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM icons " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			InputStream isImage = new ByteArrayInputStream(rs.getBytes("image"));
			
			retVal = new IconItem(
					rs.getLong("id"), 
         			rs.getLong("parent_id"),
         			rs.getString("name"),
         			rs.getString("descr"),
         			new Image(isImage),
         			dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified"));
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("get icon info (DBMain.iconGetById) : execute query Failed (id = "+ id +")");
    		//e.printStackTrace();
    	} catch (NullPointerException e) {
    		System.out.println("get icon info (DBMain.iconGetById) : NullPointerException (id = "+ id +")");
    		//e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Пиктограмма. Возвращает последнюю выбранную иконку для текущего пользователя.
	 */
	public IconItem iconGetCurrent () {
		IconItem retVal = null;
		
		checkConnect();
	
		try {
			String stm = """
					 SELECT i.id, i.parent_id, i.name, i.descr, i.image, 
	                        i.date_created, i.date_modified, i.user_created, i.user_modified 
			           FROM icons i, current_icon ci 
			          WHERE ci.icon_id = i.id 
			            AND ci."user" = ?
                    """;
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setString(1, getCurrentUser());
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				InputStream isImage = new ByteArrayInputStream(rs.getBytes("image"));
				
				retVal = new IconItem(
						rs.getLong("id"), 
	         			rs.getLong("parent_id"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			new Image(isImage),
	         			dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified"));
			}
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", 
    				"Ошибка при работе с базой данных , iconGetCurrent ()", 
		            e.getMessage());
    	}
		return retVal;
	}
	
	/**
	 * Пиктограмма. Возвращает изображение пиктограммы.
	 * @param
	 */
	public Image iconGetImageById (long id) {
		InputStream isImage = null;
		Image retVal;
		
		checkConnect();
		
		try {
			String stm = "SELECT image " +
					     "  FROM icons " +
					     " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				isImage = new ByteArrayInputStream(rs.getBytes("image"));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    	}
		
		if (isImage == null) retVal = null;
		else                 retVal = new Image(isImage);
		
		return retVal;
	}
	
	/**
	 * Пиктограмма. Возвращает название пиктограммы.
	 * @param
	 */
	public String iconGetNameById (long id) {
		String retVal = null;
		
		checkConnect();
		
		try {
			String stm = "SELECT name " +
					     "  FROM icons " +
					     " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				retVal = rs.getString("name");
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает список иконок для указанного родителя
	 */
	public List<IconItem> iconListByParentId (long parentId) {
		List<IconItem> retVal = new ArrayList<IconItem>();
		
		checkConnect();
		
		try {
			String stm = "SELECT id, parent_id, name, descr, image, " +
		                 "       date_created, date_modified, user_created, user_modified " +
					     "  FROM icons " +
					     " WHERE parent_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, parentId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				InputStream isImage = new ByteArrayInputStream(rs.getBytes("image"));
				
				retVal.add(new IconItem(
	         			rs.getLong("id"), 
	         			rs.getLong("parent_id"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			new Image(isImage),
	         			dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "iconListByParentId ("+ parentId +")");
    	}
		
		return retVal;
	}
	
	/**
	 * Меняет у текущей иконки родителя (перемещение иконки по дереву)
	 * @param currentId
	 * @param targetId
	 */
	public void iconMove (long currentId, long targetId) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
	
		try {
			stm = 	  "UPDATE icons " +
					  "   SET parent_id = ? " +
				      " WHERE id = ? " +
				      "";
			pst = con.prepareStatement(stm);
			pst.setLong (1, targetId);
			pst.setLong (2, currentId);
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при перемещении пиктограммы.");
		}
	}
	
	/**
	 * Выдает следующий Id для добавления новой иконки
	 */
	public long iconNextId () {
		return getNextId("seq_icons");
	}
	
	/**
	 * Пиктограмма. Возвращает кол-во потомков данной ноды-пиктограммы.
	 * @param
	 */
	public int iconGetNumberOfChildren (long id) {
		int retVal = 0;
		
		checkConnect();
		
		try {
			String stm = "SELECT count(*) CountR FROM icons where parent_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getInt("CountR");

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		System.out.println("execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Пиктограмма. Изменение пиктограммы.
	 * @param
	 */
	public void iconUpdate (IconItem i, boolean isImgUpdate) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
		
		if (isImgUpdate) {             // с изменением картинки
			try {
				stm = 	  "UPDATE icons " +
						  "   SET name = ?, descr = ?, image = ?, " +
						  "       date_modified = ?, user_modified = ? " +
					      " WHERE id = ? " +
					      ";";
				pst = con.prepareStatement(stm);
				pst.setString(1, i.getName());
				pst.setString(2, i.getDescr());
				
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(i.image, null);
	            ImageIO.write(bufferedImage, "png", outputStream);  // формат PNG
	            byte[] imageBytes = outputStream.toByteArray();
	            
	            pst.setBytes(3, imageBytes);
	            pstSetDate (pst, 4, new java.util.Date());
	            pst.setString(5, getCurrentUser());
	            pst.setLong  (6, i.getId());
				
	            pst.executeUpdate();
	            pst.close();
			} catch (SQLException ex) {
	        	ex.printStackTrace();
	        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
						             "Ошибка при изменении пиктограммы.");
	        } catch (IOException e) {
				e.printStackTrace();
				ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			             "Ошибка при изменении пиктограммы (сохранение новой картинки).");
			}
		} else {
			try {
				stm = 	  "UPDATE icons " +
						  "   SET name = ?, descr = ?, date_modified = ?, user_modified = ? " +
					      " WHERE id = ? " +
					      ";";
				pst = con.prepareStatement(stm);
				pst.setString(1, i.getName());
				pst.setString(2, i.getDescr());
				pstSetDate (pst, 3, new java.util.Date());
	            pst.setString(4, getCurrentUser());
				pst.setLong  (5, i.getId());
				
				pst.executeUpdate();
	            pst.close();
			} catch (SQLException ex) {
	        	ex.printStackTrace();
	        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
						             "Ошибка при изменении пиктограммы.");
			}
		}
	}
	
	/**
	 * Заголовок инфоблока. Добавление нового.
	 */
	public void infoAdd (InfoHeaderItem i) {
		String stm;
		PreparedStatement pst;
		
		checkConnect();
		
		try {
            stm = """
            		INSERT INTO info (id, sectionid, infotypeid, infoid, position, template_style_id, 
		                              name, descr,
		                              date_created, date_modified, user_created, user_modified)
            			 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            		""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setLong  (2, i.getSectionId());
            pst.setLong  (3, i.getInfoTypeId());
            pst.setLong  (4, i.getInfoId());
            pst.setLong  (5, i.getPosition());
            pst.setLong  (6, i.getTemplateStyleId());
            pst.setString(7, i.getName());
            pst.setString(8, i.getDescr());
            pstSetDate (pst,  9, i.getDateCreated());
            pstSetDate (pst, 10, i.getDateModified());
            if ((i.getUserCreated() != null) && (i.getUserCreated().length() > 0)) {
            	pst.setString(11, i.getUserCreated());
			} else {
				pst.setString(11, getCurrentUser());
			}
            if ((i.getUserModified() != null) && (i.getUserModified().length() > 0)) {
				pst.setString(12, i.getUserModified());
			} else {
				pst.setString(12, getCurrentUser());
			}
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового заголовка блока (infoAdd).");
		}
		
		if (i.getDateModified() == null) {
			sectionUpdateDateModifiedInfo (i.getSectionId());
        }
	}
	
	/**
	 * Подсчет кол-ва инфоблоков для указанного раздела.
	 */
	public long infoCount (long sectionId) {
		long retVal = 0;
		
		checkConnect();
		
		try {
			String stm = "SELECT count(id) as CountR " +
				         "  FROM info " +
				         " WHERE SectionId = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = rs.getLong("CountR");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных (infoCount)", e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Инфо блок. Удаление одного инфо блока.
	 * @param
	 * @throws KBase_DbConnEx 
	 */
	public abstract void infoDelete (long infoHeaderId) throws KBase_DbConnEx;
	
	/**
	 * Заголовок інфо блока. Получаємо информацію по id
	 */
	public InfoHeaderItem infoGet (long id) throws DataConnectionException,DataQueryException {
		InfoHeaderItem retVal = null;
		
		checkConnectEx();
	
		try {
			String stm = "SELECT id,sectionid,infotypeid,infoid,position,name,descr,template_style_id, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM info " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new InfoHeaderItem (
					rs.getLong("id"), 
					rs.getLong("sectionid"),
					rs.getLong("infoTypeId"),
					rs.getLong("template_style_id"),
					rs.getLong("infoId"),
					rs.getLong("position"),
					rs.getString("name"),
         			rs.getString("descr"),
         			dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			rs.close();
			pst.close();
		} catch (SQLException e) {
			throw new DataQueryException (
				DataQueryException.ERRCODE_OTHERS, "infoGet", 
				"Помилка в infoGet ("+id+") \n"+dbURL, 
				e, 1, null, "SQLException");
    	}
		
		return retVal;
	}
	
	/**
     * Заголовок инфо блока. Получение информации по infoTypeId и infoId
     */
    public InfoHeaderItem infoGet(long infoTypeId, long infoId) {
        InfoHeaderItem retVal = null;
        
        checkConnect();

        try {
            String stm = "SELECT id,sectionid,infotypeid,infoid,position,name,descr,template_style_id, " +
                    "       date_created, date_modified, user_created, user_modified " +
                    "  FROM info " +
                    " WHERE InfoTypeId = ?" +
                    "   AND InfoId = ? ";
            PreparedStatement pst = con.prepareStatement(stm);
            pst.setLong (1, infoTypeId);
            pst.setLong (2, infoId);
            ResultSet rs = pst.executeQuery();
            rs.next();

            java.util.Date dateTmpCre;
            Timestamp timestampCr = rs.getTimestamp("date_created");
            if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
            else                      dateTmpCre = null;

            java.util.Date dateTmpMod;
            Timestamp timestampMo = rs.getTimestamp("date_modified");
            if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
            else                      dateTmpMod = null;

            retVal = new InfoHeaderItem (
                    rs.getLong("id"),
                    rs.getLong("sectionid"),
                    rs.getLong("infoTypeId"),
                    rs.getLong("template_style_id"),
                    rs.getLong("infoId"),
                    rs.getLong("position"),
                    rs.getString("name"),
                    rs.getString("descr"),
                    dateTmpCre,
                    dateTmpMod,
                    rs.getString("user_created"),
                    rs.getString("user_modified")
            );
            rs.close();
            pst.close();
        } catch (SQLException e) {
            ShowAppMsg.showAlert(
                    "WARNING", "db error",
                    "Ошибка при работе с базой данных : infoGet(long infoTypeId, long infoId)",
                    e.getMessage());
            e.printStackTrace();
        }

        return retVal;
    }
    
    /**
	 * Возвращает максимальное значение позиции инфо блока в разделе (currentPosition = 0).
	 * Если currentPosition > 0, то ищется максимальное значение меньшее этого.
	 */
	public long infoGetMaxPosition (long sectionId, long currentPosition) {
		String stm;
		PreparedStatement pst;
		long retVal = 0;
		
		checkConnect();
		
		try {
			if (currentPosition == 0) {
				stm = "SELECT max(position) MaxPosition " +
				      "  FROM info " +
				      " WHERE SectionId = ?";
				pst = con.prepareStatement(stm);
				pst.setLong (1, sectionId);
			} else {   
				stm = "SELECT max(position) MaxPosition " +
					      "  FROM info " +
					      " WHERE SectionId = ?" +
					      "   AND position < ? ";
				pst = con.prepareStatement(stm);
				pst.setLong (1, sectionId);
				pst.setLong (2, currentPosition);
			}
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = rs.getLong("MaxPosition");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных (infoGetMaxPosition)", e.getMessage());
    	}
	
		return retVal;
	}
	
	/**
	 * Возвращает список заголовков инфо блоков для указанного раздела
	 */
	public List<InfoHeaderItem> infoListBySectionId (long sectionId) {
		List<InfoHeaderItem> retVal = new ArrayList<InfoHeaderItem>();
		
		checkConnect();
		
		try {
			String stm = "SELECT id, name, descr, position, " +
					     "       sectionid, infotypeid, infoid, template_style_id, " +
		                 "       date_created, date_modified, user_created, user_modified " +
					     "  FROM info " +
					     " WHERE sectionid = ? " +
					     " ORDER BY position ";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, sectionId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new InfoHeaderItem(
						rs.getLong("id"), 
						rs.getLong("sectionId"),
						rs.getLong("infoTypeId"),
						rs.getLong("template_style_id"),
						rs.getLong("infoId"),
						rs.getLong("position"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "infoListBySectionId()");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Выдает следующий Id для добавления нового заголовка инфо блока
	 */
	public long infoNextId () {
		return getNextId("seq_info");
	}
	
	/**
	 * Проверяет на дубль добавляемую позицию и при необходимости перенумеровывает всю последовательность блоков в разделе.
	 * Возвращает новое значение newPosition в списке после перенумерации.
	 */
	public abstract long infoPositionCheckAndRenumber (long sectionId, long newPosition);
	
	/**
	 * Инфо заголовок. Изменение.
	 */
	public void infoUpdate (InfoHeaderItem p) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
		
		try {
			stm = 	  "UPDATE info " +
					  "   SET sectionId = ?, infoTypeId = ?, infoId = ?, template_style_id = ?, " +
					  "       position = ?, name = ?, descr = ?, " +
					  "       date_modified = ?, user_modified = ? " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setLong  (1, p.getSectionId());
			pst.setLong  (2, p.getInfoTypeId());
			pst.setLong  (3, p.getInfoId());
			pst.setLong  (4, p.getTemplateStyleId());
			pst.setLong  (5, p.getPosition());
			pst.setString(6, p.getName());
			pst.setString(7, p.getDescr());
			pstSetDate (pst, 8, new java.util.Date());
            pst.setString(9, getCurrentUser());
			pst.setLong  (10, p.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении инфо заголовка (infoUpdate).");
		}
	}
	
	/**
	 * Инфо блок "Файл". 
	 * Добавление нового.
	 */
	public void info_FileAdd (Info_FileItem i, String fileName) {
		String stm;
		PreparedStatement pst;
		
		checkConnect();
		
		try {
			stm = "INSERT INTO info_file(id, title, file_body, file_name, icon_id, descr, text, " +
		                 "                       isShowTitle, isshowdescr, isshowtext) " + 
       			         "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setString(2, i.getTitle());
            
            if (fileName != null) {
            	File file = new File(fileName);
            	FileInputStream fis = new FileInputStream(file);
            	pst.setBinaryStream(3, fis, (int)file.length());
            } else {
            	pst.setBytes(3, i.getFileBody());
            }
            
            pst.setString(4, i.getName());     // fileName
            pst.setLong  (5, i.getIconId());
            pst.setString(6, i.getDescr());
            pst.setString(7, i.getText());
            pst.setInt   (8, i.getIsShowTitle());
            pst.setInt   (9, i.getIsShowDescr());
            pst.setInt   (10,i.getIsShowText());
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового блока (info_FileAdd).");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при добавлении нового блока (info_FileAdd).\n" +
					 "Не найден файл " + fileName);
		}
	}
	
	/**
	 * Инфо блок "Файл". 
	 * Возвращает тело инфо блока.
	 */
	public Info_FileItem info_FileGet (long id) {
		Info_FileItem retVal = null;
		
		checkConnect();
		
		try {
			String stm = "SELECT id, title, file_body, file_name, icon_id, descr, text, " +
		                 "       isshowtitle, isshowdescr, isshowtext " +
					     "  FROM info_file " +
					     " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = new Info_FileItem (
					rs.getLong("id"),
					rs.getString("title"),
					rs.getBytes("file_body"),
					rs.getString("file_name"),
					rs.getLong("icon_id"),
					rs.getString("descr"),
					rs.getString("text"),
         			rs.getInt("isshowtitle"),
         			rs.getInt("isshowdescr"),
         			rs.getInt("isshowtext")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при чтении Инфо блока \"Файл\" (info_FileGet).");
    	}
		
		return retVal;
	}
	
	/**
	 * Инфо блок "Файл".
	 * Выдает следующий Id для добавления нового блока
	 */
	public long info_FileNextId () {
		return getNextId("seq_info_file");
	}
	
	/**
	 * Инфо блок "Файл". 
	 * Modify an existing file.
	 */
	public void info_FileUpdate (Info_FileItem i, String fileName) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			String stm = 
					"UPDATE info_file " +
					"   SET title = ?, descr = ?, text = ?, isshowtitle = ?, isshowdescr = ?, isshowtext = ?, " +
					"       file_body = ?, file_name = ?, icon_id = ? " +
					" WHERE id = ? " +	
					"";
			
			pst = con.prepareStatement(stm);
			pst.setString(1, i.getTitle());
			pst.setString(2, i.getDescr());
            pst.setString(3, i.getText());		
            pst.setInt   (4, i.getIsShowTitle());
            pst.setInt   (5, i.getIsShowDescr());
            pst.setInt   (6, i.getIsShowText());
            
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            pst.setBinaryStream(7, fis, (int)file.length());
            
            pst.setString(8, i.getName());
            pst.setLong  (9, i.getIconId());
            pst.setLong  (10, i.getId());		
            
            pst.executeUpdate();
            pst.close();

            sectionUpdateDateModifiedInfo (infoGet((long)3, i.getId()).getSectionId());
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении инфо блока (info_FileUpdate).");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при изменении Файла, файл не найден (info_FileUpdate).");
		}
	}
	
	/**
	 * Инфо блок "Изображение". 
	 * Добавление нового.
	 */
	public void info_ImageAdd (Info_ImageItem i, String fileName) {
		String stm;
		PreparedStatement pst;
		
		checkConnect();
		
		try {
			stm = "INSERT INTO info_image(id, title, image, width, height, descr, text, " +
		                 "                       isShowTitle, isshowdescr, isshowtext) " + 
       			         "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setString(2, i.getTitle());
            
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            pst.setBinaryStream(3, fis, (int)file.length());
            
            pst.setInt   (4, i.getWidth());
            pst.setInt   (5, i.getHeight());
            pst.setString(6, i.getDescr());
            pst.setString(7, i.getText());
            pst.setInt   (8, i.getIsShowTitle());
            pst.setInt   (9, i.getIsShowDescr());
            pst.setInt   (10,i.getIsShowText());

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового блока (info_ImageAdd).");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при добавлении нового блока (info_ImageAdd).\n" +
					 "Не найден файл " + fileName);
		}
	}
	
	/**
	 * Инфо блок "Изображение". 
	 * Возвращает тело инфо блока.
	 */
	public Info_ImageItem info_ImageGet (long id) {
		Info_ImageItem retVal = null;
		
		checkConnect();
		
		try {
			String stm = "SELECT id, title, image, width, height, descr, text, " +
		                 "       isshowtitle, isshowdescr, isshowtext " +
					     "  FROM info_image " +
					     " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			InputStream isImage = new ByteArrayInputStream(rs.getBytes("image"));
			
			retVal = new Info_ImageItem (
					rs.getLong("id"),
					rs.getString("title"),
					new Image(isImage),
					rs.getInt("width"),
					rs.getInt("height"),
					rs.getString("descr"),
					rs.getString("text"),
         			rs.getInt("isshowtitle"),
         			rs.getInt("isshowdescr"),
         			rs.getInt("isshowtext")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при чтении Инфо блока \"Изображение\".");
    	}
		
		return retVal;
	}
	
	/**
	 * Инфо блок "Изображение".
	 * Выдает следующий Id для добавления нового блока
	 */
	public long info_ImageNextId () {
		return getNextId("seq_info_image");
	}
	
	/**
	 * Инфо блок "Изображение". 
	 * Изменение существующего.
	 */
	public void info_ImageUpdate (Info_ImageItem i, String imageFileName) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			String stm = 
					"UPDATE info_image " +
					"   SET title = ?, image = ?, width = ?, height = ?, " +
					"       descr = ?, text = ?, isshowtitle = ?, isshowdescr = ?, isshowtext = ? " +
					" WHERE id = ? " +	
					"";
			pst = con.prepareStatement(stm);
			pst.setString(1, i.getTitle());
			
			File file = new File(imageFileName);
            FileInputStream fis = new FileInputStream(file);
            pst.setBinaryStream(2, fis, (int)file.length());
            
            pst.setInt   (3, i.getWidth());
            pst.setInt   (4, i.getHeight());
            pst.setString(5, i.getDescr());
            pst.setString(6, i.getText());		
            pst.setInt   (7, i.getIsShowTitle());
            pst.setInt   (8, i.getIsShowDescr());
            pst.setInt   (9, i.getIsShowText());
            pst.setLong  (10, i.getId());
            
            pst.executeUpdate();
            pst.close();

            sectionUpdateDateModifiedInfo (infoGet((long)2, i.getId()).getSectionId());
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении инфо блока (info_ImageUpdate).");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при изменении Изображения, файл не найден (info_ImageUpdate).");
		}
	}
	
	/**
	 * Инфо блок "Простой текст". 
	 * Добавление нового.
	 */
	public void info_TextAdd (Info_TextItem i) {
		String stm;
		PreparedStatement pst;
		
		checkConnect();
		
		try {
            stm = """
            		INSERT INTO info_text (id, title, text, isShowTitle)  
      			        VALUES(?, ?, ?, ?)
            		""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setString(2, i.getTitle());
            pst.setString(3, i.getText());
            pst.setInt   (4, i.getIsShowTitle());
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового блока (info_TextAdd).");
		}
	}
	
	/**
	 * Инфо блок "Простой текст". 
	 * Возвращает тело инфо блока.
	 */
	public Info_TextItem info_TextGet (long id) {
		Info_TextItem retVal = null;
		
		checkConnect();
		
		try {
			String stm = "SELECT id, title, text, isshowtitle " +
				         "  FROM info_text " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = new Info_TextItem (
					rs.getLong("id"), 
         			rs.getString("title"),
         			rs.getString("text"),
         			rs.getInt("isshowtitle")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при чтении Инфо блока \"Простой текст\", info_TextGet().");
    	}
		
		return retVal;
	}
	
	/**
	 * Инфо блок "Простой текст".
	 * Выдает следующий Id для добавления нового блока
	 */
	public long info_TextNextId () {
		return getNextId("seq_info_text");
	}
	
	/**
	 * Инфо блок "Простой текст". 
	 * Изменение существующего.
	 */
	public void info_TextUpdate (Info_TextItem i) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			String stm = 
					"UPDATE info_text " +
					"   SET title = ?, text = ?, isShowTitle = ?" +
					" WHERE id = ? ";
            pst = con.prepareStatement(stm);
            pst.setString(1, i.getTitle());
            pst.setString(2, i.getText());
            pst.setInt   (3, i.getIsShowTitle());
            pst.setLong  (4, i.getId());
            
            pst.executeUpdate();
            pst.close();

            sectionUpdateDateModifiedInfo (infoGet((long)1, i.getId()).getSectionId());
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении инфо блока (info_TextUpdate).");
		}
	}
	
	/**
	 * Тип инфо блока. Получение информации по id
	 */
	public InfoTypeItem infoTypeGet (long id) {
		InfoTypeItem retVal = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT id, name, descr, table_name, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM infotype " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new InfoTypeItem (
					rs.getLong("id"), 
         			rs.getString("name"),
         			rs.getString("table_name"),
         			rs.getString("descr"),
					dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "infoTypeGet()");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает список информационных блоков
	 */
	public List<InfoTypeItem> infoTypeList () {
		List<InfoTypeItem> retVal = new ArrayList<InfoTypeItem>();
		
		checkConnect();
		
		try {
			String stm = "SELECT id, name, table_name, descr, " +
		                 "       date_created, date_modified, user_created, user_modified " +
					     "  FROM infotype " +
		                 " WHERE id > 0 ";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new InfoTypeItem(
	         			rs.getLong("id"), 
	         			rs.getString("name"),
	         			rs.getString("table_name"),
	         			rs.getString("descr"),
	         			dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
	         			));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "infoTypeList()");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Раздел. Добавление нового.
	 */
	public void sectionAdd (SectionItem i) {
		String stm;
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
            stm = """
            		INSERT INTO sections (id, parent_id, type_id, name, icon_id, descr, 
            	                          icon_id_root, icon_id_def, theme_id, cache_type, 
            	                          template_main, template_main_tree, template_main_root, 
            		                      date_created, date_modified, date_modified_info, user_created, user_modified) 
            			 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            		""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setLong  (2, i.getParentId());
            pst.setLong  (3, i.getTypeId());
            pst.setString(4, i.getName());
            pst.setLong  (5, i.getIconId());
            pst.setString(6, i.getDescr());
            pst.setLong  (7, i.getIconIdRoot());
            pst.setLong  (8, i.getIconIdDef());
            pst.setLong  (9, i.getThemeId());
            pst.setInt   (10, i.getCacheType());
            pst.setString(11, i.getTemplateMain());
            pst.setInt   (12, i.getTemplateMainTree());
            pst.setLong  (13, i.getTemplateMainRoot());
            pstSetDate (pst, 14, i.getDateCreated());
            pstSetDate (pst, 15, i.getDateModified());
            pstSetDate (pst, 16, i.getDateModifiedInfo());
            if ((i.getUserCreated() != null) && (i.getUserCreated().length() > 0)) {
            	pst.setString(17, i.getUserCreated());
			} else {
				pst.setString(17, getCurrentUser());
			}
            if ((i.getUserModified() != null) && (i.getUserModified().length() > 0)) {
				pst.setString(18, i.getUserModified());
			} else {
				pst.setString(18, getCurrentUser());
			}
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового раздела, sectionAdd().");
		}
	}
	
	/**
	 * Раздел. Копирование всех инфо блоков с одного раздела в другой.
	 * @param
	 */
	public abstract void sectionCopyInfoBlocks (long sectionSrcId, long sectionTrgId);
	
	/**
	 * Раздел. Удаление раздела со всеми подчиненными разделами.
	 * @param
	 */
	public abstract void sectionDelete (long id);
	
	/**
	 * Раздел. Получение информации по id
	 */
	public SectionItem sectionGetById (long id) {
		SectionItem retVal = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT id, parent_id, type_id, name, descr, icon_id, "+
					     "       template_main, template_main_tree, template_main_root, "+
					     "       icon_id_root, icon_id_def, theme_id, cache_type, "+
					     "       date_created, date_modified, user_created, user_modified, date_modified_info " +
			             "  FROM sections " +
			             " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			java.util.Date dateTmpModInfo;
			Timestamp timestampMoInf = rs.getTimestamp("date_modified_info");
			if (timestampMoInf != null)  dateTmpModInfo = new java.util.Date(timestampMoInf.getTime());
			else                         dateTmpModInfo = null;
			
			retVal = new SectionItem(
					rs.getLong("id"), 
         			rs.getLong("parent_id"),
         			rs.getInt("type_id"),
         			rs.getString("name"),
         			rs.getString("descr"),
         			rs.getLong("icon_id"),
         			(rs.getLong("icon_id") > 0) ? iconGetImageById (rs.getLong("icon_id")) : null,
         			rs.getString("template_main"),
         			rs.getInt("template_main_tree"),
         			rs.getLong("template_main_root"),
         			rs.getLong("icon_id_root"),
         			rs.getLong("icon_id_def"),
         			rs.getLong("theme_id"),
         			rs.getInt("cache_type"),
         			dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified"),
         			dateTmpModInfo);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "get section info (id = "+ id +")");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает id пиктограммы по умолчанию для указанного раздела
	 * @param sectionId ;
	 *        isRecursive : true - с проходом вверх до корня (и по умолчанию для всех разделов, если ничего не указано)
	 */
	public abstract long sectionGetIconIdDefault (long sectionId, boolean isRecursive);
	
	/**
	 * Раздел. Возвращает кол-во потомков данной ноды-раздела.
	 * @param
	 */
	public int sectionGetNumberOfChildren (long id) {
		int retVal = 0;
		
		checkConnect();
		
		try {
			String stm = "SELECT count(*) CountR FROM sections where parent_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getInt("CountR");

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , sectionGetNumberOfChildren ("+id+")", 
		             e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Разділ. Повертає кількість інфоблоків розділу.
	 * @param
	 */
	public int sectionGetNumberOfInfoBlocks (long id) {
		int retVal = 0;
		
		checkConnect();
		
		try {
			String stm = "select count(*) as CountR from info where sectionid = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = rs.getInt("CountR");

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , sectionGetNumberOfInfoBlocks ("+id+")", 
		             e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Повертає ланцюжок id розділів від вказаного до предка.
	 * Якщо розділ не входить в гілку цього предка, то робимо до самого рута id=0
	 */
	public List<Long> sectionGetPathIds (long rootId, long sectionId) {
	    List<Long> retVal = new java.util.ArrayList<>();
	    long currId = sectionId;
	    SectionItem si = null;

	    while ((currId != rootId) && (currId != 0)) {
	        retVal.add(0, currId);
	        si = sectionGetById(currId);
	        if (si == null) break;
	        currId = si.getParentId();
	    }

	    retVal.add(0, currId);

	    return retVal;
	}
	
	/**
	 * Возвращает цепочку имен разделов от указанного до самого верхнего родителя. 
	 */
	public abstract String sectionGetPathName (long sectionId, String delimiter);
	
	/**
	 * Повертає головний шаблон для вказаного розділа 
	 */
	public abstract TemplateItem sectionGetTemplateMain (long sectionId, long themeId);
	
	/**
	 * Возвращает id темы для указанного раздела
	 * @param sectionId ;
	 *        isRecursive : true - с проходом вверх до корня (и по умолчанию для всех разделов, если ничего не указано)
	 */
	public abstract long sectionGetThemeId (long sectionId, boolean isRecursive);

	/**
	 * шукаємо чи є вказаний Розділ в Дереві розділів
	 * Проблема : не шукає від самісінького кореню з id = 0
	 * @param rootId
	 * @param currentId
	 * @return
	 * @throws DataConnectionException
	 */
	public boolean sectionIsPresentInTree (long rootId, long currentId)
			throws DataConnectionException,DataQueryException {
		boolean retVal = false;

		checkConnectEx();

		try {
			String stm = """
					WITH RECURSIVE subtree(id) AS (
						-- Починаємо з кореневого елемента
						SELECT id
						FROM sections
						WHERE id = ?
						
						UNION ALL
						
						-- Рекурсивно додаємо дочірні вузли
						SELECT s.id
						FROM sections s
						JOIN subtree st ON s.parent_id = st.id
					)
					SELECT count(*) as cnt
						FROM subtree
					WHERE id = ?
					""";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, rootId);
			pst.setLong(2, currentId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = (rs.getInt("cnt") > 0) ? true : false;

			rs.close();
			pst.close();
		} catch (SQLException e) {
			throw new DataQueryException (
					DataQueryException.ERRCODE_OTHERS, "sectionIsPresentInTree",
					"Помилка в sectionIsPresentInTree ("+rootId+") \n"+dbURL,
					e, 1, null, "SQLException");
		}

		return retVal;
	}
	
	/**
	 * Возвращает список разделов для указанного родителя
	 */
	public List<SectionItem> sectionListByParentId (long parentId) {
		List<SectionItem> retVal = new ArrayList<SectionItem>();
		
		checkConnect();
		
		try {
			String stm = "SELECT id, parent_id, type_id, name, descr, icon_id, "+
				         "       template_main, template_main_tree, template_main_root, "+
				         "       icon_id_root, icon_id_def, theme_id, cache_type, "+
				         "       date_created, date_modified, user_created, user_modified, date_modified_info " +
		                 "  FROM sections " +
		                 " WHERE parent_id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, parentId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				java.util.Date dateTmpModInfo;
				Timestamp timestampMoInf = rs.getTimestamp("date_modified_info");
				if (timestampMoInf != null)  dateTmpModInfo = new java.util.Date(timestampMoInf.getTime());
				else                         dateTmpModInfo = null;
				
				retVal.add(new SectionItem(
						rs.getLong("id"), 
	         			rs.getLong("parent_id"),
	         			rs.getInt("type_id"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			rs.getLong("icon_id"),
	         			(rs.getLong("icon_id") > 0) ? iconGetImageById (rs.getLong("icon_id")) : null,
	         			rs.getString("template_main"),
	         			rs.getInt("template_main_tree"),
	         			rs.getLong("template_main_root"),
	         			rs.getLong("icon_id_root"),
	         			rs.getLong("icon_id_def"),
	         			rs.getLong("theme_id"),
	         			rs.getInt("cache_type"),
	         			dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified"),
	         			dateTmpModInfo));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка при получении списка подразделов, sectionListByParentId().");
    	}
		
		return retVal;
	}
	
	/**
	 * Меняет у текущего раздела родителя (перемещение раздела по дереву)
	 * @param currentId
	 * @param targetId
	 */
	public void sectionMove (long currentId, long targetId) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
	
		try {
			stm = 	  "UPDATE sections " +
					  "   SET parent_id = ?, date_modified = ? " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setLong (1, targetId);
			pstSetDate (pst, 2, new java.util.Date());
			pst.setLong (3, currentId);
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при перемещении раздела.");
		}
	}
	
	/**
	 * Выдает следующий Id для добавления нового раздела
	 */
	public long sectionNextId () {
		return getNextId("seq_sections");
	}
	
	/**
	 * Раздел. Изменение.
	 */
	public void sectionUpdate (SectionItem p) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
		
		try {
			stm = 	  "UPDATE sections " +
					  "   SET name = ?, icon_id = ?, descr = ?, "+
					  "       date_modified = ?, user_modified = ?, " +
					  "       icon_id_root = ?, icon_id_def = ?, theme_id = ?, cache_type = ?, " +
					  "       template_main = ?, template_main_tree = ?, template_main_root = ? "+
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setString(1, p.getName());
			pst.setLong  (2, p.getIconId());
			pst.setString(3, p.getDescr());
			pstSetDate (pst, 4, new java.util.Date());
            pst.setString(5, getCurrentUser());
			pst.setLong  (6, p.getIconIdRoot());
            pst.setLong  (7, p.getIconIdDef());
            pst.setLong  (8, p.getThemeId());
            pst.setInt   (9, p.getCacheType());
            pst.setString(10, p.getTemplateMain());
            pst.setInt   (11, p.getTemplateMainTree());
            pst.setLong  (12,p.getTemplateMainRoot());
			pst.setLong  (13, p.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении раздела.");
		}
	}
	
	/**
	 * Раздел. Обновление даты последней модификации инфо блоков.
	 */
	public void sectionUpdateDateModifiedInfo (long sectionId) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
		
		try {
			stm = 	  "UPDATE sections " +
					  "   SET date_modified_info = ? " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pstSetDate (pst, 1, new java.util.Date());
			pst.setLong (2, sectionId);
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении раздела (sectionUpdateDateModifiedInfo).");
		}
	}
	
	/**
	 * Заголовок-Фаворит. Додавання нового елемента.
	 */
	public void sectionFavoriteAdd (long sectionId)
			throws DataConnectionException,DataQueryException {
		String stm;
		PreparedStatement pst = null;

		checkConnectEx();

		try {
			stm = """
					INSERT INTO sections_favorite (id, parent_id, section_id, "user", date_created)
						VALUES(?, 0, ?, ?, ?)
					""";
			pst = con.prepareStatement(stm);
			pst.setLong (1, getNextId("seq_sections_favorite"));
			pst.setLong (2, sectionId);
			pst.setString(3, getCurrentUser());
			pstSetDate (pst, 4, null);

			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			throw new DataQueryException (
				DataQueryException.ERRCODE_OTHERS, "sectionFavoriteAdd",
				"Помилка в sectionFavoriteAdd ("+sectionId+") \n"+dbURL,
				e, 1, null, "SQLException");
		}
	}
	
	/**
	 * Заголовок-Фаворит. Вилучення елемента.
	 */
	public void sectionFavoriteDelete (long id, boolean withSubTree)
			throws DataConnectionException,DataQueryException {
		String stm;
		PreparedStatement pst = null;

		checkConnectEx();

		try {
			if (withSubTree) {
				stm = """
						WITH RECURSIVE x(id) AS (
							SELECT id
							FROM sections_favorite
							WHERE id = ?
							UNION ALL
							SELECT a.id
							FROM x
							JOIN sections_favorite a ON a.parent_id = x.id)
						delete from sections_favorite
						where id in (select id from x)
						""";
			} else {
				stm = """
						delete from sections_favorite
						where id = ?
						""";
			}
			pst = con.prepareStatement(stm);
			pst.setLong (1, id);

			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			throw new DataQueryException (
				DataQueryException.ERRCODE_OTHERS, "sectionFavoriteAdd",
				"Помилка в sectionFavoriteAdd ("+id+", "+withSubTree+") \n"+dbURL,
				e, 1, null, "SQLException");
		}
	}
	//TODO
	
	/**
	 * шукаємо чи є вказаний Розділ в Дереві Favorite
	 * @return
	 * @throws DataConnectionException
	 */
	public boolean sectionFavoriteIsPresent (long sectionId)
			throws DataConnectionException,DataQueryException {
		boolean retVal = false;

		checkConnectEx();

		try {
			String stm = """
					select count(*) as cnt
					  from sections_favorite
					 where "user" = ?
					   and section_id = ?
					""";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setString(1, getCurrentUser());
			pst.setLong (2, sectionId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = (rs.getInt("cnt") > 0) ? true : false;

			rs.close();
			pst.close();
		} catch (SQLException e) {
			throw new DataQueryException (
				DataQueryException.ERRCODE_OTHERS, "sectionFavoriteIsPresent",
				"Помилка в sectionFavoriteIsPresent ("+sectionId+") \n"+dbURL,
				e, 1, null, "SQLException");
		}

		return retVal;
	}
	
	/**
	 * Повертає список розділів для вказаного предка
	 */
	public List<SectionFavoriteItem> sectionFavoriteListByParentId (long parentId) {
		List<SectionFavoriteItem> retVal = new ArrayList<SectionFavoriteItem>();
		
		checkConnect();
		
		try {
			String stm = """
					SELECT id, COALESCE(parent_id, 0) as parent_id, section_id, date_created
					FROM sections_favorite
					WHERE "user" = ?
					  and COALESCE(parent_id, 0) = ?
					""";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setString(1, getCurrentUser());
			pst.setLong (2, parentId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null) dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                     dateTmpCre = null;
				
				retVal.add(new SectionFavoriteItem(
						rs.getLong("id"),
						rs.getLong("parent_id"),
						rs.getInt("section_id"),
						sectionGetById(rs.getInt("section_id")),
						dateTmpCre));
			}
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Помилка при роботі з базою даних",
					"Помилка при отриманні списка підрозділів, sectionFavoriteListByParentId().");
		}
		
		return retVal;
	}
	
	/**
	 * Перебудовуємо дерево, апдейтимо усі parent_id
	 */
	abstract public void sectionFavoriteRebuildParentId ()
		throws DataConnectionException,DataQueryException;

	/**
	 * Установки. Получаем значение по алиасу
	 */
	public String settingsGetValue (String alias) throws DataConnectionException,DataQueryException {
		String retVal = null;
		
		checkConnectEx();
		
		try {
			String stm = "SELECT value " +
				         "  FROM settings " +
				         " WHERE alias = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setString (1, alias);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getString("value");
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		throw new DataQueryException (
                DataQueryException.ERRCODE_OTHERS, "settingsGetValue",
                "Помилка в settingsGetValue ("+alias+") \n"+dbURL,
                e, 1, null, "SQLException");
    	}
		
		return retVal;
	}
	
	/**
	 * Установки. Проверяем наличие установки по алиасу.
	 */
	public boolean settingsIsPresent (String alias) {
		boolean retVal = false;
		
		checkConnect();
		
		try {
			String stm = "SELECT count(*) CountR FROM settings where alias = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setString (1, alias);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
			if (rs.getInt("CountR") > 0)  retVal = true;
			else                          retVal = false;

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "settingsIsPresent ("+ alias +")");
    	}
		
		return retVal;
	}
	
	/**
	 * Установки. Сохраняем установку (добавляем или изменяем). Поиск существующей установки делается по алиасу.
	 * @param
	 * 
	 * not used
	 */
	public void settingsSave (String alias, String section, String subject, String name, String value, String descr) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		if (! settingsIsPresent(alias)) {            // INSERT
			try {
	            String stm = """
	            		INSERT INTO settings (id, alias, section, subject, name, value, descr,
	            		            date_created, date_modified, user_created, user_modified)
	            			 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
	            		""";
	            pst = con.prepareStatement(stm);
	            pst.setLong  (1, getNextId("seq_settings"));
	            pst.setString(2, alias);
	            pst.setString(3, section);
	            pst.setString(4, subject);
	            pst.setString(5, name);
	            pst.setString(6, value);
	            pst.setString(7, descr);
	            pstSetDate (pst, 8, new java.util.Date());
	            pstSetDate (pst, 9, new java.util.Date());
				pst.setString(10, getCurrentUser());
				pst.setString(11, getCurrentUser());
	            pst.executeUpdate();
	            pst.close();
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
						             "Ошибка при добавлении новой установки(настройки), settingsSave().");
			}
		} else {                                     // UPDATE
			try {
				String stm = "UPDATE settings " +
						     "   SET value = ?, descr = ?, "+
						     "       date_modified = ?, user_modified = ? " +
					         " WHERE alias = ? " +
					         ";";
				pst = con.prepareStatement(stm);
				pst.setString(1, value);
				pst.setString(2, descr);
				pstSetDate (pst, 3, new java.util.Date());
				pst.setString(4, getCurrentUser());
				pst.setString(5, alias);
				
				pst.executeUpdate();
	            pst.close();
			} catch (SQLException ex) {
	        	ex.printStackTrace();
	        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
						             "Ошибка при изменении установки, settingsSave().");
			}
		}
	}
	
	/**
	 * Директория Файлов для шаблонов. Добавление новой.
	 * Додається без боді.
	 */
	public void templateFileAdd (TemplateFileItem i) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			if ((i.getType() == 1) || (i.getType() == 11)) {
				String stm = """
						INSERT INTO template_files (id, parent_id, theme_id, type, file_type, file_name, descr,
						                            date_created, date_modified, user_created, user_modified)  
      			         VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
						""";
				pst = con.prepareStatement(stm);
				pst.setLong  (1, i.getId());
				pst.setLong  (2, i.getParentId());
				pst.setLong  (3, i.getThemeId());
				pst.setInt   (4, i.getType());
				pst.setInt   (5, i.getFileType());
				pst.setString(6, i.getFileName());
				pst.setString(7, i.getDescr());
				pstSetDate (pst, 8, i.getDateCreated());
	            pstSetDate (pst, 9, i.getDateModified());
	            pst.setString(10, getCurrentUser());
	            pst.setString(11, getCurrentUser());
			
				pst.executeUpdate();
				pst.close();
			} else {
				ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			             "Ошибка при добавлении новой директории файлов шаблонов : тип " + i.getType() + " не определен.");
			}
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении новой директории файлов шаблонов.");
		}
	}
	
	/**
	 * Файл для шаблонов. Добавление нового.
	 */
	public void templateFileAdd (TemplateFileItem i, String fileName) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			if (i.getFileType() == TemplateFileItem.FILE_TYPE_TEXT) {          // text file
				String stm = """
						insert into template_files (id, parent_id, theme_id, type, file_type, file_name, descr, body,
						                            date_created, date_modified, user_created, user_modified)
						     values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
						""";
				pst = con.prepareStatement(stm);
	            pst.setLong  (1, i.getId());
	            pst.setLong  (2, i.getParentId());
	            pst.setLong  (3, i.getThemeId());
	            pst.setInt   (4, i.getType());
	            pst.setInt   (5, i.getFileType());
	            pst.setString(6, i.getFileName());
	            pst.setString(7, i.getDescr());
				pst.setString(8, i.getBody());
				pstSetDate (pst, 9, i.getDateCreated());
	            pstSetDate (pst,10, i.getDateModified());
	            pst.setString(11, getCurrentUser());
	            pst.setString(12, getCurrentUser());
				
				pst.executeUpdate();
	            pst.close();
			} else if (i.getFileType() == TemplateFileItem.FILE_TYPE_IMAGE) {   // picture file
				String stm = """
						INSERT INTO template_files (id, parent_id, theme_id, type, file_type, file_name, descr, body_bin,
						                            date_created, date_modified, user_created, user_modified)
      			         	 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
						""";
				pst = con.prepareStatement(stm);
				pst.setLong  (1, i.getId());
	            pst.setLong  (2, i.getParentId());
	            pst.setLong  (3, i.getThemeId());
	            pst.setInt   (4, i.getType());
	            pst.setInt   (5, i.getFileType());
	            pst.setString(6, i.getFileName());
	            pst.setString(7, i.getDescr());
	            
	            pstSetDate (pst, 9, i.getDateCreated());
	            pstSetDate (pst,10, i.getDateModified());
	            pst.setString(11, getCurrentUser());
	            pst.setString(12, getCurrentUser());
	            
				try {
					File file = new File(fileName);
					FileInputStream fis = new FileInputStream(file);
					pst.setBinaryStream(8, fis, (int)file.length());
					
					pst.executeUpdate();
	           		pst.close();
	           		fis.close();
				} catch (FileNotFoundException ex) {
					ShowAppMsg.showAlert("ERROR", "Добавление файла для шаблона", 
										 "Файл "+ fileName +" не найден", "Добавление файла в БД прервано.");
				} catch (IOException ex) {
					ShowAppMsg.showAlert("ERROR", "Добавление файла для шаблона", 
							 "Не получается прочитать файл "+ fileName, "Добавление файла в БД прервано.");
				} finally {
					pst.close();
				}
			}
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового файла шаблонов.");
		}
	}
	
	/**
	 * Файл для шаблона. Вилучення файла/директорії з усією ієрархією.
	 * @param
	 */
	public abstract void templateFileDelete (long id);
	
	/**
	 * Файл для шаблона. Получение информации по themeId и fileName 
	 * (перетягнув зі старої версії шаблонів, де у файлів не було директорій)
	 */
	public TemplateFileItem templateFileGet (long themeId, String fileName) {
		TemplateFileItem retVal = null;
		Image isImage = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT id, parent_id, theme_id, type, file_type, file_name, descr, body, body_bin, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM template_files " +
				         " WHERE theme_id = ? " +
				         "   AND file_name = ? ";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			pst.setString(2, fileName);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			if (rs.getInt("file_type") == TemplateFileItem.FILE_TYPE_IMAGE)
				isImage = new Image(new ByteArrayInputStream(rs.getBytes("body_bin")));
			
			retVal = new TemplateFileItem (
					rs.getLong("id"),
					rs.getLong("parent_id"),
         			rs.getLong("theme_id"),
         			rs.getInt("type"),
         			rs.getInt("file_type"),
         			rs.getString("file_name"),
         			rs.getString("descr"),
         			rs.getString("body"),
         			isImage,
					dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "templateFileGet (long themeId, String fileName)");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Файл (или директория) для шаблона. Получение информации по id
	 */
	public TemplateFileItem templateFileGetById (long id) {
		TemplateFileItem retVal = null;
		Image isImage = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT id, parent_id, theme_id, type, file_type, file_name, descr, body, body_bin, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM template_files " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			if (rs.getInt("file_type") == TemplateFileItem.FILE_TYPE_IMAGE) 
				isImage = new Image(new ByteArrayInputStream(rs.getBytes("body_bin")));
			
			retVal = new TemplateFileItem (
					rs.getLong("id"), 
					rs.getLong("parent_id"),
         			rs.getLong("theme_id"),
         			rs.getInt("type"),
         			rs.getInt("file_type"),
         			rs.getString("file_name"),
         			rs.getString("descr"),
         			rs.getString("body"),
         			isImage,
					dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "templateFileGetById (id)");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает цепочку имен директорій от указанного файла(директорії) до самого верхнего родителя.
	 * withFileName - шлях з кінцевим іменем файла (директорії) чи без 
	 */
	public abstract String templateFileGetPathName (long fileId, String delimiter, boolean withFileName);
	
	/**
	 * Возвращает true, если в указанной директорії є файл (чи директорія) с таким именем.
	 */
	public boolean templateFileIsExistNameInDir (
			long curFileId, long parentId, long themeId, int type, String fileName) {
		boolean retVal = true;
		
		String subQuery = (type < 10) ? " and f.type < 10 " : " and f.type >= 10 ";
		
		checkConnect();
	
		try {
			String stm = "select count(*) CountR "+
					     "  FROM template_files f "+
					     " where f.id <> ? "+
					     "   and f.parent_id = ? "+
					     "   and f.theme_id = ? "+
					     subQuery +
					     "   and upper(f.file_name) = upper(?) ";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong  (1, curFileId);
			pst.setLong  (2, parentId);
			pst.setLong  (3, themeId);
			pst.setString(4, fileName);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
            retVal = (rs.getInt("CountR") > 0) ? true : false;

            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "templateFileIsExistNameInDir()");
    		e.printStackTrace();
    	}
		return retVal;
	}
	
	/**
	 * Возващает список файлов и директорий по id родительской директории. 
	 * @param parentId
	 * @return
	 */
	public List<TemplateSimpleItem> templateFileListByParent (TemplateSimpleItem parentItem) {
		List<TemplateSimpleItem> retVal = new ArrayList<TemplateSimpleItem>();
		PreparedStatement pst = null;
		
		checkConnect();
	
		try {
			String subSql = "";
			if (parentItem.getId() == 0) {
				switch (parentItem.getTypeItem()) {
				case TemplateSimpleItem.TYPE_ITEM_DIR_FILE :
					subSql = " and type < 10 ";
					break;
				case TemplateSimpleItem.TYPE_ITEM_DIR_FILE_OPTIONAL :
					subSql = " and type >= 10 ";
					break;
				default :
					subSql = " and type < 10 ";    // что то присвоили на всякий случай
				}
			}
			
			String stm = "select id, theme_id, type, file_type, file_name, descr, " +
		                 "       date_created, date_modified, user_created, user_modified " + 
					     "  from template_files " +
					     " where parent_id = ? " +
					     "   and theme_id = ? " +
					     subSql; 
			pst = con.prepareStatement(stm);
			pst.setLong (1, parentItem.getId());
			pst.setLong (2, parentItem.getThemeId());
				
			ResultSet rs = pst.executeQuery();
		
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				TemplateFileItem tfi = new TemplateFileItem (
						rs.getLong("id"), 
						parentItem.getId(),
	         			rs.getLong("theme_id"),
	         			rs.getInt("type"),
	         			rs.getInt("file_type"),
	         			rs.getString("file_name"),
	         			rs.getString("descr"),
	         			"",
	         			null,
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						);
				retVal.add(tfi);
			}
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "templateFileListByParent("+parentItem.getId()+")");
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает список під-директорій і файлів для вказаної директорії.
	 * Элементы списка типа TemplateFileItem
	 * type : 0 - обов'язкові файли з їх директоріями ; 10 - не обов'язкові
	 */
	public List<TemplateFileItem> templateFileListByType (long parentId, long themeId, int type) {
		List<TemplateFileItem> retVal = new ArrayList<TemplateFileItem>();
		Image isImage = null;
		
		checkConnect();
	
		try {
			String subSql = "";
			if (parentId == 0) {
				switch (type) {
				case 0 :
					subSql = " and type < 10 ";
					break;
				case 10 :
					subSql = " and type >= 10 ";
					break;
				default :
					subSql = " and type < 10 ";    // что то присвоили на всякий случай
				}
			}
			
			String stm = 
					 "select id, type, file_type, file_name, descr, body, body_bin, " +
					 "       date_created, date_modified, user_created, user_modified " + 
				     "  from template_files " +
				     " where parent_id = ? " +
				     "   and theme_id = ? " +
				     subSql +
				     " order by type desc, file_name ";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong(1, parentId);
			pst.setLong(2, themeId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				if (rs.getInt("file_type") == TemplateFileItem.FILE_TYPE_IMAGE) 
					isImage = new Image(new ByteArrayInputStream(rs.getBytes("body_bin")));
				
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new TemplateFileItem(
						rs.getLong("id"),
						parentId,
						themeId,
	         			rs.getInt("type"),
	         			rs.getInt("file_type"),
	         			rs.getString("file_name"),
	         			rs.getString("descr"),
	         			rs.getString("body"),
	         			isImage,
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "templateFileListByType()");
    	}
	
		return retVal;
	}
	
	/**
	 * Выдает следующий Id для добавления нового файла (или директории) для шаблонов
	 */
	public long templateFileNextId () {
		return getNextId("seq_template_files");
	}
	
	/**
	 * Обновление информации директории файлов для шаблонов
	 */
	public void templateFileUpdate (TemplateFileItem tf) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
		
		try {
			if ((tf.getType() == 1) || (tf.getType() == 11)) {
				stm = 	"UPDATE template_files " +
						"   SET parent_id = ?, theme_id = ?, type = ?, file_type = ?, " +
						"       file_name = ?, descr = ?, date_modified = ?, user_modified = ? " +
						" WHERE id = ? " +
						";";
				pst = con.prepareStatement(stm);
				pst.setLong  (1, tf.getParentId());
				pst.setLong  (2, tf.getThemeId());
				pst.setInt   (3, tf.getType());
				pst.setInt   (4, tf.getFileType());
				pst.setString(5, tf.getFileName());
				pst.setString(6, tf.getDescr());
				pstSetDate (pst, 7, new java.util.Date());
	            pst.setString(8, getCurrentUser());
				pst.setLong  (9, tf.getId());
				
				pst.executeUpdate();
				pst.close();
			} else {
				ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
			             "Ошибка при обновлении директории файлов шаблонов : тип " + tf.getType() + " не определен.");
			}
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при обновлении директории файлов шаблонов.");
		}
	}
	
	/**
	 * Обновление информации файла для шаблонов
	 */
	public void templateFileUpdate (TemplateFileItem fi, String fileNameImage) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
		
		if ((fi.getType() != 0) && (fi.getType() != 10)) {
			ShowAppMsg.showAlert("WARNING", "db warning", "Ошибка при работе с базой данных", 
		             "Ошибка при обновлении файлов шаблонов : тип " + fi.getType() + " не определен.");
			return;
		}
	
		if (fi.getFileType() == TemplateFileItem.SUBTYPE_FILE_TEXT) {              // обновляем с текстом
			try {
				stm = 	"update template_files " +
						"   set parent_id = ?, theme_id = ?, type = ?, " +
						"       file_type = ?, file_name = ?, descr = ?, body = ?, " +
						"       date_modified = ?, user_modified = ? " +
						" where id = ? " +
						";";
				pst = con.prepareStatement(stm);
				pst.setLong  (1, fi.getParentId());
				pst.setLong  (2, fi.getThemeId());
				pst.setInt   (3, fi.getType());
				pst.setInt   (4, fi.getFileType());
				pst.setString(5, fi.getFileName());
				pst.setString(6, fi.getDescr());
				pst.setString(7, fi.getBody());
				pstSetDate (pst, 8, new java.util.Date());
	            pst.setString(9, getCurrentUser());
				pst.setLong  (10, fi.getId());
				
				pst.executeUpdate();
	            pst.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
	        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
						             "Ошибка при обновлении файла для шаблонов (с текстом).");
			}
		} else {
			if (fileNameImage.equals("") || (fileNameImage == null)) {       // обновляем без содержимого файла картинки
				try {
					stm = 	"update template_files " +
							"   set parent_id = ?, theme_id = ?, type = ?, " +
							"       file_type = ?, file_name = ?, descr = ?, " +
							"       date_modified = ?, user_modified = ? " +
							" where id = ? " +
							";";
					pst = con.prepareStatement(stm);
					pst.setLong  (1, fi.getParentId());
					pst.setLong  (2, fi.getThemeId());
					pst.setInt   (3, fi.getType());
					pst.setInt   (4, fi.getFileType());
					pst.setString(5, fi.getFileName());
					pst.setString(6, fi.getDescr());
					pstSetDate (pst, 7, new java.util.Date());
		            pst.setString(8, getCurrentUser());
					pst.setLong  (9, fi.getId());
					
					pst.executeUpdate();
		            pst.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
		        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
							             "Ошибка при обновлении файла для шаблонов (без картинки).");
				}
			} else {
				// обновляем с картинкой
				try {
					stm = 	"update template_files " +
							"   set parent_id = ?, theme_id = ?, type = ?, " +
							"       file_type = ?, file_name = ?, descr = ?, body_bin = ?, " +
							"       date_modified = ?, user_modified = ? " +
							" where id = ? " +
							";";
					pst = con.prepareStatement(stm);
					pst.setLong  (1, fi.getParentId());
					pst.setLong  (2, fi.getThemeId());
					pst.setInt   (3, fi.getType());
					pst.setInt   (4, fi.getFileType());
					pst.setString(5, fi.getFileName());
					pst.setString(6, fi.getDescr());
					
					File file = new File(fileNameImage);
		            FileInputStream fis = new FileInputStream(file);
		            pst.setBinaryStream(7, fis, (int)file.length());
					
		            pstSetDate (pst, 8, new java.util.Date());
		            pst.setString(9, getCurrentUser());
		            pst.setLong  (10, fi.getId());
					
					pst.executeUpdate();
		            pst.close();
		            fis.close();
				} catch (SQLException ex) {
		        	ex.printStackTrace();
		        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
							             "Ошибка при обновлении файла для шаблонов (с картинкой).");
		        } catch (IOException e) {
					e.printStackTrace();
					ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
				             "Ошибка при изменении картинки для шаблонов.");
				}
			}
		}
	}
	
	/**
	 * Файлы для шаблонов. Удаление всех файлов указанной темы.
	 * @param
	 */
	public void templateFilesDelete (long themeId) {
		PreparedStatement pst = null;
		
		checkConnect();
	
		try {
			String stm = "DELETE FROM template_files WHERE theme_id = ? ";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, themeId);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
        	e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , templateFilesDelete ("+themeId+")", 
		             e.getMessage());
        }
	}
	
	/**
	 * Стиль шаблонов. Добавление нового.
	 */
	public void templateStyleAdd (TemplateStyleItem i) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
            String stm = """
            		INSERT INTO template_style (id, parent_id, type, infotype_id, name, descr, tag,
            		                            date_created, date_modified, user_created, user_modified)  
            			 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            			 """;
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setLong  (2, i.getParentId());
            pst.setLong  (3, i.getType());
            pst.setLong  (4, i.getInfoTypeId());
            pst.setString(5, i.getName());
            pst.setString(6, i.getDescr());
            pst.setString(7, i.getTag());
            pstSetDate (pst, 8, new java.util.Date());
            pstSetDate (pst, 9, new java.util.Date());
            pst.setString(10, getCurrentUser());
            pst.setString(11, getCurrentUser());
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового стиля шаблона.");
		}
	}
	
	/**
	 * Стили шаблонов. Вилучення стиля/директорії з усією ієрархією та зв'язками з шаблонами по его Id.
	 */
	public abstract void templateStyleDelete (long id);
	
	/**
	 * Стиль шаблонов. Обновление последнего используемого стиля для темы, типа блока и пользователя.
	 */
	@SuppressWarnings("resource")
	public void templateStyleEditCurrent (long themeId, long infoTypeId, long styleId, int flag) {
		String stm;
		int countR;
		
		checkConnect();
		
		try {
			//-------- check record with the style for exist
			stm = "SELECT count(cs.id) CountR " +
			      "  FROM template_style s, current_style cs " + 
	              " WHERE s.id = cs.template_style_id " +
	              "   AND cs.theme_id = ? " +
	              "   AND s.infotype_id = ? " +
	              "   AND cs.\"user\" = ? " +
	              "   AND cs.flag = ? " +
	              ";";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			pst.setLong (2, infoTypeId);
			pst.setString(3, getCurrentUser());
			pst.setInt  (4, flag);
			ResultSet rs = pst.executeQuery();
			rs.next();
	
			countR = rs.getInt("CountR");
			
			rs.close();
			pst.close();
			
			//-------- insert style
			if (countR == 0) {
				stm = """
	            		insert into current_style (id, "user", theme_id, template_style_id, flag,
	            		                           date_created, date_modified)
							values (?, ?, ?, ?, ?, ?, ?)
	            		""";
				pst = con.prepareStatement(stm);
				pst.setLong  (1, getNextId("seq_current_style"));
				pst.setString(2, getCurrentUser());
				pst.setLong  (3, themeId);
				pst.setLong  (4, styleId);
				pst.setInt   (5, flag);
				pstSetDate (pst, 6, new java.util.Date());
	            pstSetDate (pst, 7, new java.util.Date());
			} else {
			//-------- update style
				stm = 	  "UPDATE current_style " +
						  "   SET template_style_id = ?, "+
						  "       date_modified = ? " +
					      " WHERE theme_id = ? " +
						  "   AND template_style_id = ? " +
					      "   AND flag = ? " +
					      ";";
				pst = con.prepareStatement(stm);
				pst.setLong(1, styleId);
				pstSetDate (pst, 2, new java.util.Date());
				pst.setLong(3, themeId);
				pst.setLong(4, templateStyleGetCurrent (themeId, infoTypeId, flag).getId()); // old current style
				pst.setInt (5, flag);
			}
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных templateStyleEditCurrent", 
					e.getMessage());
		}
	}
	
	/**
	 * Стиль шаблонов. Получение информации по id
	 */
	public TemplateStyleItem templateStyleGet (long id) {
		TemplateStyleItem retVal = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT id, parent_id, type, infotype_id, name, descr, tag, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM template_style " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new TemplateStyleItem (
					rs.getLong("id"), 
         			rs.getLong("parent_id"),
         			rs.getInt("type"),
         			rs.getLong("infotype_id"),
         			rs.getString("name"),
         			rs.getString("descr"),
         			rs.getString("tag"),
					dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , templateStyleGet ("+id+")", 
		             e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Стиль шаблонов. Получение информации по Тегу
	 */
	public TemplateStyleItem templateStyleGetByTag (String tag) {
		TemplateStyleItem retVal = null;
		
		checkConnect();

		if (! templateStyleTagIsPresent (tag)) return retVal;
		
		try {
			String stm = "SELECT id, parent_id, type, infotype_id, name, descr, tag, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM template_style " +
				         " WHERE tag = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setString(1, tag);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new TemplateStyleItem (
					rs.getLong("id"), 
         			rs.getLong("parent_id"),
         			rs.getInt("type"),
         			rs.getLong("infotype_id"),
         			rs.getString("name"),
         			rs.getString("descr"),
         			rs.getString("tag"),
					dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , templateStyleGetByTag (\""+tag+"\")", 
		             e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает текущий стиль согласно флагу в указанной теме для указанного типа инфо блока.
	 */
	public TemplateStyleItem templateStyleGetCurrent (long themeId, long infoTypeId, int flag) {
		TemplateStyleItem retVal = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT s.id, s.parent_id, s.type, s.infotype_id, s.name, s.descr, s.tag, " +
		                 "       s.date_created, s.date_modified, s.user_created, s.user_modified " +
				         "  FROM template_style s, current_style cs " + 
                         " WHERE s.id = cs.template_style_id " +
                         "   AND cs.theme_id = ? " +
                         "   AND s.infotype_id = ? " +
                         "   AND cs.\"user\" = ? " +
                         "   AND cs.flag = ? " +
                         ";";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			pst.setLong (2, infoTypeId);
			pst.setString(3, getCurrentUser());
			pst.setInt  (4, flag);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal = new TemplateStyleItem (
						rs.getLong("id"), 
	         			rs.getLong("parent_id"),
	         			rs.getInt("type"),
	         			rs.getLong("infotype_id"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			rs.getString("tag"),
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						);
			}
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", 
    				"Ошибка при работе с базой данных , templateStyleGetCurrent ("+themeId+","+infoTypeId+","+flag+")", 
		            e.getMessage());
    	}
		return retVal;
	}
	
	/**
	 * Возвращает стиль по умолчанию в указанной теме для указанного типа инфо блока.
	 * Если такого стиля нет, возвращается null.
	 */
	public abstract TemplateStyleItem templateStyleGetDefault (long themeId, long infoTypeId);
	
	/**
	 * Возвращает дату установки признака По умолчанию на стилі
	 */
	public java.util.Date templateStyleGetDefaultDateModified (long themeId, long templateStyleId) {
		java.util.Date retVal = null;
		
		checkConnect();
		
		try {
			String stm = "SELECT cs.date_modified " +
				         "  FROM current_style cs " + 
                         " WHERE cs.template_style_id = ? " +
				         "   AND cs.theme_id = ? " +
                         "   AND cs.\"user\" = ? " +
                         "   AND cs.flag = 0 " +
                         ";";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, templateStyleId);
			pst.setLong (2, themeId);
			pst.setString(3, getCurrentUser());
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  retVal = new java.util.Date(timestampMo.getTime());
			}
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", 
    				"Ошибка при работе с базой данных , templateStyleGetDefaultDateModified ("+templateStyleId+")", 
		            e.getMessage());
    	}
		return retVal;
	}
	
	/**
	 * Стиль. Повертає ід шаблона з лінку. Якщо лінку немає, то 0
	 */
	public long templateStyleGetLinkTemplateId (long themeId, long styleId) {
		long retVal = 0;
		
		checkConnect();
		
		try {
			String stm = "select count(*) CountR "+
					     "  from template_style_link  "+
					     " where theme_id = ? "+
					     "   and style_id = ? ";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			pst.setLong (2, styleId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = rs.getLong("CountR");
			
			rs.close();
			pst.close();
			
			if (retVal == 0)  return retVal;
			
			// get templateId
			stm = "select template_Id "+
				     "  from template_style_link  "+
				     " where theme_id = ? "+
				     "   and style_id = ? ";
			pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			pst.setLong (2, styleId);
			rs = pst.executeQuery();
			rs.next();

			retVal = rs.getLong("template_Id");
		
			rs.close();
			pst.close();
		} catch (SQLException e) {
			ShowAppMsg.showAlert("WARNING", "db error", 
    				"Ошибка при работе с базой данных , templateStyleGetLinkTemplateId ("+themeId+", "+styleId+")", 
		            e.getMessage());
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Проверяем, является ли указанный стиль дефолтным для темы и пользователя
	 */
	public boolean templateStyleIsDefault (long themeId, long templateStyleId) { 
		boolean retVal = false;
		
		checkConnect();
	
		try {
			String stm = "SELECT count(id) as CountR " +
				         "  FROM current_style " +
				         " WHERE theme_id = ? " +
				         "   AND template_style_id = ? " +
				         "   AND \"user\" = ? " +
                         "   AND flag = 0 " +
				         ";";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, themeId);
			pst.setLong (2, templateStyleId);
			pst.setString(3, getCurrentUser());
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = (rs.getLong("CountR") > 0) ? true : false;
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных, templateStyleIsDefault()", 
					e.getMessage());
    	}
		return retVal;
	}
	
	/**
	 * Перевіряємо чи використовується стиль або його підстилі в інфо блоках
	 */
	public boolean templateStyleIsUsed (long templateStyleId) { 
		boolean retVal = false;
		
		checkConnect();
	
		try {
			String stm = """
					WITH RECURSIVE x AS (
					SELECT id
					  FROM template_style
					 WHERE id = ?
					UNION ALL
					SELECT a.id
					  FROM template_style a
					  JOIN x ON a.parent_id = x.id
					)
					select count(*) CountR
					  from info
					 where template_style_id in (select id from x)
					""";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, templateStyleId);
			ResultSet rs = pst.executeQuery();
			rs.next();

			retVal = (rs.getLong("CountR") > 0) ? true : false;
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных, templateStyleIsUsed()", 
					e.getMessage());
    	}
		return retVal;
	}
	
	/**
	 * Возвращает список стилей и директорий родительской директории стилей или типа инфоблока. 
	 * @param parentId
	 * @return
	 */
	public List<TemplateSimpleItem> templateStyleListByParent (TemplateSimpleItem parentItem) {
		List<TemplateSimpleItem> retVal = new ArrayList<TemplateSimpleItem>();
		PreparedStatement pst = null;
		
		checkConnect();
	
		try {
			String subSql = "";
			if (parentItem.getId() == 0) {
				if (parentItem.getSubtypeItem() < 10) {
					subSql = " and s.type < 10 ";
				} else {
					subSql = " and s.type >= 10 ";
				}
			}
			
			String stm = "select s.id, s.type, s.name, s.descr, " +
						 "       coalesce(t.template_id,0) as flag2, "+
	                 	 "       s.date_created, s.date_modified, s.user_created, s.user_modified " + 
	                 	 "  from template_style s " +
	                 	 "  left join template_style_link t     on t.style_id = s.id "+
	                 	 "                                     and t.theme_id = ? "+
	                 	 " where s.parent_id = ? " +
	                 	 "   and s.infotype_id = ? "+
	                 	 subSql;
			pst = con.prepareStatement(stm);
			pst.setLong (1, parentItem.getThemeId());
			pst.setLong (2, parentItem.getId());
			pst.setLong (3, parentItem.getFlag());
				
			ResultSet rs = pst.executeQuery();
		
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				int typeItem;
				switch (rs.getInt("type")) {
				case  0 :
				case 10 :
					typeItem = TemplateSimpleItem.TYPE_ITEM_STYLE;
					break;
				case  1 :
				case 11 :
					typeItem = TemplateSimpleItem.TYPE_ITEM_DIR_STYLE;
					break;
				default :
					typeItem = parentItem.getTypeItem();  // что нибудь присвоим
				}
				
				TemplateSimpleItem newItem = new TemplateSimpleItem(
	         			rs.getLong("id"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			parentItem.getThemeId(),
	         			typeItem,
	         			rs.getInt("type"),
	         			parentItem.getFlag(),    // info type id
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						);
				newItem.setFlag2(rs.getLong("flag2"));
				retVal.add(newItem);
			}
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "templateStyleListByParent("+parentItem.getId()+")");
    	}
		
		return retVal;
	}
	
	/**
	 * Выдает следующий Id для добавления нового стиля шаблона
	 */
	public long templateStyleNextId () {
		return getNextId("seq_template_style");
	}
	
	/**
	 * функция устанавливает указанный стиль как дефолтный для указанной темы
	 */
	public abstract void templateStyleSetDefault (long themeId, long templateStyleId);
	
	/**
	 * Стиль шаблонів. Чи існує вказаний тег стиля.
	 */
	public boolean templateStyleTagIsPresent (String tag) {
		boolean retVal = false;
		
		checkConnect();
		
		try {
			String stm = "SELECT count(*) as CountR " +
				         "  FROM template_style " +
				         " WHERE tag = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setString (1, tag);
			ResultSet rs = pst.executeQuery();
			rs.next();

			if (rs.getInt("CountR") > 0)  retVal = true;
			else                          retVal = false;
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных (templateStyleTagIsPresent)", e.getMessage());
    	}
		
		return retVal;
	}
	
	/**
	 * функция удаляет указанный стиль как дефолтный для указанной темы
	 */
	public abstract void templateStyleUnsetDefault (long themeId, long templateStyleId);
	
	/**
	 * Стиль шаблонов. Изменение.
	 */
	public void templateStyleUpdate (TemplateStyleItem p) {
		PreparedStatement pst;
		String stm;
		
		checkConnect();
		
		try {
			stm = 	  "UPDATE template_style " +
					  "   SET parent_id = ?, type = ?, infotype_id = ?, " +
					  "       name = ?, descr = ?, tag = ?, " +
					  "       date_modified = ?, user_modified = ? " +
				      " WHERE id = ? " +
				      ";";
			pst = con.prepareStatement(stm);
			pst.setLong  (1, p.getParentId());
			pst.setInt   (2, p.getType());
			pst.setLong  (3, p.getInfoTypeId());
			pst.setString(4, p.getName());
			pst.setString(5, p.getDescr());
			pst.setString(6, p.getTag());
			pstSetDate (pst, 7, new java.util.Date());
            pst.setString(8, getCurrentUser());
			pst.setLong  (9, p.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении стиля шаблонов (templateStyleUpdate).");
		}
	}
	
	/**
	 * Стили шаблонов. Удаление всех стилей.
	 */
	public abstract void templateStylesDelete ();
	
	/**
	 * Тема для шаблонов. Добавление новой.
	 */
	public void templateThemeAdd (TemplateThemeItem i) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			String stm = """
					INSERT INTO template_themes (
									id, name, descr, 
									date_created, date_modified, user_created, user_modified)  
					  			 VALUES(?, ?, ?, ?, ?, ?, ?)
					""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, i.getId());
            pst.setString(2, i.getName());
            pst.setString(3, i.getDescr());
            pstSetDate (pst, 4, i.getDateCreated());
            pstSetDate (pst, 5, i.getDateModified());
            pst.setString(6, getCurrentUser());
            pst.setString(7, getCurrentUser());
            
            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении новой темы шаблонов.");
		}
	}
	
	/**
	 * Тема для шаблонов. Подсчет количества тем
	 */
	public long templateThemeCount () {
		long retVal = 0;
		
		checkConnect();
	
		try {
			String stm = "SELECT count(*) as CountR " +
				         "  FROM template_themes " +
						 "";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			retVal = rs.getLong("CountR"); 
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("templateThemeCount : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Тема для шаблонов. Удаление темы.
	 */
	public abstract void templateThemeDelete (long id);
	
	/**
	 * Тема для шаблонов. Получение информации по id
	 */
	public TemplateThemeItem templateThemeGetById (long id) {
		TemplateThemeItem retVal = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT id, name, descr, " +
		                 "       date_created, date_modified, user_created, user_modified " +
				         "  FROM template_themes " +
				         " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new TemplateThemeItem(
         			rs.getLong("id"), 
         			rs.getString("name"),
         			rs.getString("descr"),
         			dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified"));
			retVal.setThemeId(retVal.getId());
			retVal.setTypeItem(TemplateSimpleItem.TYPE_ITEM_THEME);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		System.out.println("get templateTheme info : execute query Failed");
    		e.printStackTrace();
    	}
		
		return retVal;
	}
	
	/**
	 * Возвращает список тем шаблонов
	 */
	public List<TemplateThemeItem> templateThemesList () {
		List<TemplateThemeItem> retVal = new ArrayList<TemplateThemeItem>();
		
		checkConnect();
		
		try {
			String stm = "SELECT id, name, descr, " +
		                 "       date_created, date_modified, user_created, user_modified " +
					     "  FROM template_themes ";
			PreparedStatement pst = con.prepareStatement(stm);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new TemplateThemeItem(
	         			rs.getLong("id"), 
	         			rs.getString("name"),
	         			rs.getString("descr"),
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						));
			}
			
            rs.close();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "Ошибка получения списка тем (templateThemesList).");
    	}
		
		return retVal;
	}
	
	/**
	 * Выдает следующий Id для добавления новой темы шаблонов
	 */
	public long templateThemeNextId () {
		return getNextId("seq_template_themes");
	}
	
	/**
	 * Тема для шаблонов. Изменение.
	 */
	public void templateThemeUpdate (TemplateThemeItem p) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
		
		try {
			stm = """
					  UPDATE template_themes 
					     SET name = ?, descr = ?, 
					         date_modified = ?, user_modified = ? 
				       WHERE id = ? 
				  """;
			pst = con.prepareStatement(stm);
			pst.setString(1, p.getName());
			pst.setString(2, p.getDescr());
			pstSetDate (pst, 3, new java.util.Date());
            pst.setString(4, getCurrentUser());
			pst.setLong  (5, p.getId());
			
			pst.executeUpdate();
            pst.close();
		} catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при изменении темы шаблонов.");
		}
	}
	
	/**
	 * Шаблон. Добавление нового.
	 */
	public void templateAdd (TemplateItem i) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
				String stm = """
						INSERT INTO template (id, parent_id, type, name, descr, body,
									          date_created, date_modified, user_created, user_modified) 
           			         VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
						""";
				pst = con.prepareStatement(stm);
	            pst.setLong  (1, i.getId());
	            pst.setLong  (2, i.getParentId());
	            pst.setInt   (3, i.getType());
	            pst.setString(4, i.getName());
	            pst.setString(5, i.getDescr());
				pst.setString(6, i.getBody());
				pstSetDate (pst, 7, new java.util.Date());
	            pstSetDate (pst, 8, new java.util.Date());
	            pst.setString( 9, getCurrentUser());
	            pst.setString(10, getCurrentUser());				
				
				pst.executeUpdate();
	            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при добавлении нового шаблона.");
		}
	}
	
	/**
	 * Шаблон. Вилучення шаблону/директорії з усією ієрархією та зв'язками зі стилями.
	 * @param
	 */
	public abstract void templateDelete (long id);
	
	/**
	 * Шаблон. Получение информации по id
	 */
	public TemplateItem templateGet (long id) {
		TemplateItem retVal = null;
		
		checkConnect();
	
		try {
			String stm = "SELECT t.id, t.parent_id, t.type, t.name, t.descr, t.body, "+
					     "       t.date_created, t.date_modified, t.user_created, t.user_modified " +
					     "  FROM template t " +
					     " WHERE t.id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			java.util.Date dateTmpCre;
			Timestamp timestampCr = rs.getTimestamp("date_created");
			if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
			else                      dateTmpCre = null;
			
			java.util.Date dateTmpMod;
			Timestamp timestampMo = rs.getTimestamp("date_modified");
			if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
			else                      dateTmpMod = null;
			
			retVal = new TemplateItem (
					rs.getLong("id"), 
         			rs.getLong("parent_id"),
         			rs.getInt("type"),
         			rs.getString("name"),
         			rs.getString("descr"),
         			rs.getString("body"),
					dateTmpCre, 
         			dateTmpMod,
         			rs.getString("user_created"),
         			rs.getString("user_modified")
					);
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "templateGet ("+ id +")");
    	}
		
		return retVal;
	}
	
	/**
	 * Шаблон. Получение информации по themeId и templateStyleId
	 */
	public TemplateItem templateGet (long themeId, long templateStyleId) {
		TemplateItem retVal = null;
		
		checkConnect();
	
		try {
			String stm = "select t.id, t.parent_id, t.type, t.name, t.descr, t.body, " +
					     "       t.date_created, t.date_modified, t.user_created, t.user_modified " +
					     "  from template t " +
					     "  join template_style_link tsl   on tsl.template_id = t.id " + 
					 	 "                                and tsl.style_id = ? " +
					 	 "                                and tsl.theme_id = ? " +
					     ";";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, templateStyleId);
			pst.setLong (2, themeId);
			ResultSet rs = pst.executeQuery();
			//rs.next();
			
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal = new TemplateItem (
						rs.getLong("id"), 
	         			rs.getLong("parent_id"),
	         			rs.getInt("type"),
						rs.getString("name"),
	         			rs.getString("descr"),
	         			rs.getString("body"),
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						);
			}
				
			rs.close();
			pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "templateGet ("+ themeId +", "+ templateStyleId +")");
    	}
		
		return retVal;
	}
	
	/**
	 * Шаблон. Получение информации по themeId и InfoHeaderItem
	 */
	public TemplateItem templateGet (long themeId, InfoHeaderItem infoHeader) {
		long templateStyleId = 0;
		TemplateItem retVal = null;
		
		checkConnect();
		
		if (infoHeader.getTemplateStyleId() <= 0) {
			templateStyleId = templateStyleGetDefault(themeId, infoHeader.getInfoTypeId()).getId();
		} else {
			templateStyleId = infoHeader.getTemplateStyleId();
		}
		
		retVal = templateGet (themeId, templateStyleId);
		
		return retVal;
	}
	
	/**
	 * Шаблон. Перевіряємо по id чи існує такий шаблон (або директорія шаблонів)
	 */
	public boolean templateIsPresent (long id) {
		boolean retVal = false;
		
		checkConnect();
	
		try {
			String stm = "SELECT count(*) as cnt " +
					     "  FROM template " +
					     " WHERE id = ?";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			if (rs.getInt("cnt") > 0) {
				retVal = true; 
			}
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
	    	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "templateIsPresent ("+ id +")");
		}
		
		return retVal;
	}
	
	/*
	 * Вилучення зв'язку між стилем та шаблоном для конкретної схеми
	 */
	public void templateLinkDelete (long themeId, long templateStyleId) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			String stm = "DELETE FROM template_style_link "+
					     " WHERE theme_id = ? "+
					     "   and style_id = ? ";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, themeId);
            pst.setLong  (2, templateStyleId);

            pst.executeUpdate();
            pst.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "templateLinkDelete ("+ themeId+','+templateStyleId +")");
        }
	}
	
	/**
	 * Шаблон. Перевіряємо по themeId и templateStyleId чи існує звязок між стилем та шаблоном і такий шаблон
	 */
	public boolean templateLinkIsPresent (long themeId, long templateStyleId) {
		boolean retVal = false;
		
		checkConnect();
	
		try {
			String stm = "select count(*) as cnt " +
				         "  from template t " +
				         "  join template_style_link tsl   on tsl.template_id = t.id " + 
				 	     "                                and tsl.style_id = ? " +
				 	     "                                and tsl.theme_id = ? " +
				         ";";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong (1, templateStyleId);
			pst.setLong (2, themeId);
			ResultSet rs = pst.executeQuery();
			rs.next();
			
			if (rs.getInt("cnt") > 0) {
				retVal = true; 
			}
			
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
	    	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "templateIsLinkPresent ("+ themeId +", "+ templateStyleId +")");
		}
		
		return retVal;
	}
	
	/*
	 * Створення/оновлення зв'язку між стилем та шаблоном для конкретної схеми
	 */
	public void templateLinkSet (long themeId, long templateStyleId, long templateId) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
		
		if (! templateLinkIsPresent (themeId, templateStyleId)) {   // insert
			try {
				stm = """
						insert into template_style_link (id, style_id, theme_id, template_id,
								date_created, date_modified, user_created, user_modified)
							values (?, ?, ?, ?, ?, ?, ?, ?)
						""";
				pst = con.prepareStatement(stm);
				pst.setLong  (1, getNextId("seq_template_style_link"));
		        pst.setLong  (2, templateStyleId);
		        pst.setLong  (3, themeId);
		        pst.setLong  (4, templateId);
		        pstSetDate (pst, 5, new java.util.Date());
	            pstSetDate (pst, 6, new java.util.Date());
	            pst.setString(7, getCurrentUser());
	            pst.setString(8, getCurrentUser());
		        pst.executeUpdate();
		        pst.close();
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
						             "Помилка створення зв'язку між стилем та шаблоном, templateLinkSet()");
			}
		} else {     // update
			try {
				stm = 	"UPDATE template_style_link " +
						"   SET template_Id = ?, " +
						"       date_modified = ?, user_modified = ? " +
						" WHERE style_id = ? " +
						"   AND theme_id = ? " +
						";";
				pst = con.prepareStatement(stm);
				pst.setLong  (1, templateId);
				pstSetDate (pst, 2, new java.util.Date());
	            pst.setString(3, getCurrentUser());
				pst.setLong  (4, templateStyleId);
		        pst.setLong  (5, themeId);
				pst.executeUpdate();
	            pst.close();
			} catch (SQLException e) {
				e.printStackTrace();
	        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
						             "Помилка зміни зв'язку між стилем та шаблоном, templateLinkSet().");
			}
		}
	}
	
	/**
	 * Возващает список шаблонов и директорий шаблонов по id родительской директории. 
	 * @param parentId
	 * @return
	 */
	public List<TemplateSimpleItem> templateListByParent (long parentId) {
		List<TemplateSimpleItem> retVal = new ArrayList<TemplateSimpleItem>();
		PreparedStatement pst = null;
		
		checkConnect();
	
		try {
			String stm = "select t.id, t.type, "+ 
				         "       (select count(*) from template_style_link s where s.template_id = t.id) as flag, "+
		                 "       t.name, t.descr, t.date_created, t.date_modified, t.user_created, t.user_modified "+  
	                     "  from template t "+ 
	                     " where t.parent_id = ? ";
			pst = con.prepareStatement(stm);
			pst.setLong (1, parentId);
				
			ResultSet rs = pst.executeQuery();
		
			while (rs.next()) {
				java.util.Date dateTmpCre;
				Timestamp timestampCr = rs.getTimestamp("date_created");
				if (timestampCr != null)  dateTmpCre = new java.util.Date(timestampCr.getTime());
				else                      dateTmpCre = null;
				
				java.util.Date dateTmpMod;
				Timestamp timestampMo = rs.getTimestamp("date_modified");
				if (timestampMo != null)  dateTmpMod = new java.util.Date(timestampMo.getTime());
				else                      dateTmpMod = null;
				
				retVal.add(new TemplateSimpleItem(
	         			rs.getLong("id"),
	         			rs.getString("name"),
	         			rs.getString("descr"),
	         			0,
	         			((rs.getInt("type")==0)||(rs.getInt("type")==10)) ? 
	         					TemplateSimpleItem.TYPE_ITEM_TEMPLATE : TemplateSimpleItem.TYPE_ITEM_DIR_TEMPLATE,
	         			rs.getInt("type"),
	         			rs.getLong("flag"),
						dateTmpCre, 
	         			dateTmpMod,
	         			rs.getString("user_created"),
	         			rs.getString("user_modified")
						));
			}
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "templateListByParent("+parentId+")");
    	}
		
		return retVal;
	}
	
	/*
	 * Шаблон. Повертає текстовий список стилів повязаних з цим шаблоном
	 */
	public List<String> TemplateListLinks (long id) {
		List<String> retVal = new ArrayList<String>();
		PreparedStatement pst = null;
		
		checkConnect();
	
		try {
			String stm = "select l.style_id ||' '|| s.name ||'('|| t.name ||')' style "
					+ "  from template_style_link l "
					+ "  join template_style s      on s.id = l.style_id "
					+ "  join template_themes t     on t.id = l.theme_id "
					+ " where template_id = ? ";
			pst = con.prepareStatement(stm);
			pst.setLong (1, id);
			
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				retVal.add(rs.getString("style"));
			}
			
            rs.close();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "TemplateListLinks("+id+")");
		}
		
		return retVal;
	}
	
	/**
	 * Выдает следующий Id для добавления нового шаблона
	 */
	public long templateNextId () {
		return getNextId("seq_template");
	}
	
	/**
	 * Обновление шаблона
	 */
	public void templateUpdate (TemplateItem tip) {
		PreparedStatement pst = null;
		String stm;
		
		checkConnect();
	
		try {
			stm = 	"UPDATE template " +
					"   SET parent_id = ?, type = ?, " +
					"       name = ?, descr = ?, body = ?, " +
					"       date_modified = ?, user_modified = ? " +
					" WHERE id = ? " +
					";";
			pst = con.prepareStatement(stm);
			pst.setLong  (1, tip.getParentId());
			pst.setInt   (2, tip.getType());
			pst.setString(3, tip.getName());
			pst.setString(4, tip.getDescr());
			pst.setString(5, tip.getBody());
			pstSetDate (pst, 6, new java.util.Date());
            pst.setString(7, getCurrentUser());
			pst.setLong  (8, tip.getId());
			pst.executeUpdate();
            pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
					             "Ошибка при обновлении шаблона, templateUpdate().");
		}
	}
}