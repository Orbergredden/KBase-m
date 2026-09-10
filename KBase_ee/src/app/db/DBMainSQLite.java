package app.db;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import app.exceptions.DataConnectionException;
import app.exceptions.DataQueryException;
import app.exceptions.KBase_DbConnEx;
import app.lib.DateConv;
import app.lib.KeyStorePrg;
import app.lib.ShowAppMsg;
import app.model.FindParams;
import app.model.FindResultItem;
import app.model.Params;
import app.model.business.SectionItem;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateStyleItem;

/**
 *
 * v.0.01.00.007 2025-02-06
 */
public class DBMainSQLite extends DBMain {
	private String user;
	private DateConv dateConv;

	/**
	 * Конструктор
	 * host - повний шлях до файлу БД
	 * port, name, user, password - не використовуються
	 */
	public DBMainSQLite (Params params, String host, String port, String name, 
			String user, KeyStorePrg.EncBlob password)  throws DataConnectionException {
		super(params, host, port, name, user, password);
		
		this.user = user;
		dateConv = new DateConv("yyyy-MM-dd","yyyy-MM-dd HH:mm:ss");
		
		dbURL = "jdbc:sqlite:"+ host;
		
		open();
	}
	
	/**
	 * Створюємо з'єднання з БД 
	 */
	void open () throws DataConnectionException {
		con = null;
		
		try {
    	    Class.forName("org.sqlite.JDBC");
    	} catch (ClassNotFoundException e) {
    		throw new DataConnectionException (
    				DataConnectionException.ERRCODE_DRIVER_NOT_FOUND, "open", 
    				"SQLite JDBC Driver is not found. Include it in your library path", 
    				e, 1, null, "ClassNotFoundException");
    	}
		
		try {
            con = DriverManager.getConnection(dbURL);
        } catch (SQLException e) {
        	throw new DataConnectionException (
    				DataConnectionException.ERRCODE_GET_CONNECTION, "open", 
    				"Помилка підключення (SQLException) "+dbURL, 
    				e, 1, null, "SQLException");
        }
    	
		if (con == null) {
			throw new DataConnectionException (
    				DataConnectionException.ERRCODE_GET_CONNECTION, "open", 
    				"Помилка підключення (con == null) "+dbURL, 
    				null, 1, null, null);
    	}
    	
		//------- check version
		String versionProgram = params.getConfigSys().getItemValue("DB", "Required version.KBase Lite");
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
	public String getCurrentUser () {
		return user;
		//return "KBase_User";
	}
	
	/**
	 * 
	 */
	protected long getNextId (String sequenceName) {
		String stm;
		long id;
		long nextValue;
		int step;
		long retVal = -1;
		
		checkConnect();
		
		try {
			stm = """
					select id, next_value, step
					  from sequences
					 where sequence_name = ?
					""";
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setString(1, sequenceName);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
			id        = rs.getLong("id");
			retVal    = rs.getLong("next_value");
			step      = rs.getInt("step");

            rs.close();
            pst.close();
            
            // increment sequence
            nextValue = retVal + step;
            
            stm = """
					update sequences
					   set next_value = ?, date_modified = ?
					 where id = ?
					""";
			pst = con.prepareStatement(stm);
			pst.setLong (1, nextValue);
			pstSetDate(pst, 2, new java.util.Date());
			pst.setLong (3, id);
			
			pst.executeUpdate();
            pst.close();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "getNextId("+ sequenceName +")");
    	}
		
		return retVal;
	}

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
		
		pst.setString(pos, dateConv.dateTimeToStr(pstDate));
	}
	
	/**
	 * Пошук інформації в базі знань
	 * @param findParams
	 * @return
	 */
	public List<FindResultItem> findInfo (FindParams findParams) {


		ShowAppMsg.showAlert("WARNING", "db message",
				"findInfo() для SQLite поки що не реалізована",
				"");
		return null;

	}
	//TODO

	/**
	 * Пиктограмма. Удаление пиктограммы со всеми подчиненными пиктограммами.
	 * @param
	 */
	public void iconDelete (long id) throws KBase_DbConnEx {
		
		checkConnect();
		
		try {
			// типу зовнішній ключ : перевіряємо використання іконок в інших таблицях
			if (sectionCountIconId(id) == -1) {
				ShowAppMsg.showAlert("WARNING", "db error", 
						"Іконку(и) неможливо вилучити", 
			            "помилка (sectionCountIconId(id) == -1)");
				//return;
				throw new KBase_DbConnEx ("помилка (sectionCountIconId(id) == -1)", this);
			}
			if (sectionCountIconId(id) > 0) {
				ShowAppMsg.showAlert("INFORMATION", "db info", 
						"Іконку(и) неможливо вилучити, вона(и) використовуються в розділах документів", 
			            "");
				//return;
				throw new KBase_DbConnEx ("Іконку(и) неможливо вилучити, вона(и) використовуються в розділах документів", this);
			}
			if (info_FileCountIconId(id) > 0) {
				ShowAppMsg.showAlert("INFORMATION", "db info", 
						"Іконку(и) неможливо вилучити, вона(и) використовуються в інфоблоках типу Файл", 
			            "");
				//return;
				throw new KBase_DbConnEx ("Іконку(и) неможливо вилучити, вона(и) використовуються в інфоблоках типу Файл", this);
			}
			
			//---- deleting
			String stm;
			PreparedStatement pst;
			
			// delete from current_icon
			stm = """
					WITH RECURSIVE x AS (
                    	SELECT id
                          FROM icons
                         WHERE id = ?
                         UNION ALL
                        SELECT a.id
                          FROM icons a
                          JOIN x ON a.parent_id = x.id
                    )
                    DELETE FROM current_icon
                     WHERE icon_id IN (SELECT id FROM x)
					""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);
            pst.executeUpdate();
            
            // delete from icons
            stm = """
					WITH RECURSIVE child_icons(id) AS (
                       SELECT id FROM icons WHERE id = ?
                       UNION ALL
                       SELECT icons.id FROM icons
                         JOIN child_icons ON icons.parent_id = child_icons.id
                    ) 
                    DELETE FROM icons WHERE id IN (SELECT id FROM child_icons)
					""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);
            pst.executeUpdate();
            
            pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "iconDelete ("+ id +")");
    		throw new KBase_DbConnEx ("iconDelete ("+ id +")", this);
    	}
	}
	
	/**
	 * Инфо блок. Удаление одного инфо блока.
	 * @param
	 */
	public void infoDelete (long infoHeaderId) throws KBase_DbConnEx {
		String stm;
		PreparedStatement pst = null;
		ResultSet rs = null;
		long infoTypeId;
		long infoId;
		String tableName = null;
		
		checkConnect();
		
		try {
			// get info block information
			stm = """
					select infoTypeId, infoId
		              from info
		             where id = ?
		    """;
			pst = con.prepareStatement(stm);
	    	pst.setLong  (1, infoHeaderId);
		    rs = pst.executeQuery();
		    rs.next();
		    infoTypeId = rs.getLong("infoTypeId");
			infoId     = rs.getLong("infoId");
		    rs.close();
			pst.close();
			
			switch ((int)infoTypeId) {
			case 1 :		// простий текст
				tableName = "info_text";
				break;
			case 2 : 		// зображення
				tableName = "info_image";
				break;
			case 3 :		// файл
				tableName = "info_file";
				break;
			default :
				throw new KBase_DbConnEx (
						"infoDelete() : Not existing type of info block , infoTypeId = "+ infoTypeId, this);
			}
			
			// delete info block
			stm = "delete from "+ tableName +" where id = ?";
			pst = con.prepareStatement(stm);
            pst.setLong  (1, infoId);
            pst.executeUpdate();
            pst.close();
			
            // delete info header
        	stm = "delete from info where id = ?";
        	pst = con.prepareStatement(stm);
            pst.setLong  (1, infoHeaderId);
            pst.executeUpdate();
            pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при удалении инфо блока, infoDelete().", 
		             e.getMessage());
		} finally {
			// Закриваємо ресурси
			try {
				if (rs != null) rs.close();
				if (pst != null) pst.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Видаляє усі інфо блоки для вказаного розділа
	 * @param sectionId
	 */
	private void infoDeleteForSection (long sectionId) {
		String stm;
		PreparedStatement pst = null;
		ResultSet rs = null;
		
		checkConnect();
		
		try {
			stm = """
					select id
	                  from info
	                 where sectionId = ?
		    """;
			pst = con.prepareStatement(stm);
	    	pst.setLong  (1, sectionId);
		    rs = pst.executeQuery();
		    while (rs.next()) {
		    	infoDelete(rs.getLong("id"));
		    }
		    rs.close();
		    pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "infoDeleteForSection().", 
		             e.getMessage());
		} catch (KBase_DbConnEx e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "Помилка", "Помилка при вилученні інфо блоку", e.msg);
		} finally {
			// Закриваємо ресурси
			try {
				if (rs != null) rs.close();
				if (pst != null) pst.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Проверяет на дубль добавляемую позицию и при необходимости перенумеровывает всю последовательность блоков в разделе.
	 * Возвращает новое значение newPosition в списке после перенумерации.
	 */
	public long infoPositionCheckAndRenumber (long sectionId, long newPosition) {
		int counterPos = 10;
		String stm;
		PreparedStatement pst = null;
		ResultSet rs = null;
		long retVal = newPosition;
		PreparedStatement pst2 = null;
		
		int countR = 0;
		
		checkConnect();
		
		try {
			// проверяем, есть ли такая позиция уже в БД
			stm = """
					select count(*) as countR
					  from info
		             where sectionId = ? 
		               and position = ?  
			""";
			pst = con.prepareStatement(stm);
	    	pst.setLong  (1, sectionId);
		    pst.setLong  (2, newPosition);
		    rs = pst.executeQuery();
		    rs.next();
		    countR = rs.getInt("countR");
		    rs.close();
			pst.close();
			
			// если есть , делаем перенумерацию
		    if (countR > 0) {
		    	//
		    	stm = """
		    			select id
		    			  from info
                         where sectionId = ?
                           and position < ?
                         order by position
		    	""";
		    	pst = con.prepareStatement(stm);
		    	pst.setLong  (1, sectionId);
			    pst.setLong  (2, newPosition);
			    rs = pst.executeQuery();
			    while (rs.next()) {
			    	stm = """
			    			update info
		                       set position = ? 
		                     where id = ? 
			    	""";
			    	pst2 = con.prepareStatement(stm);
			    	pst2.setInt   (1, counterPos);
				    pst2.setLong  (2, rs.getLong("id"));
				    pst2.executeUpdate();
		            pst2.close();
			    	
					counterPos += 10;
			    }
			    rs.close();
			    pst.close();
			    
			    //
			    retVal = counterPos;
			    counterPos += 10;
			    
			    //
			    stm = """
		    			select id
		    			  from info
                         where sectionId = ?
                           and position >= ?
                         order by position
		    	""";
		    	pst = con.prepareStatement(stm);
		    	pst.setLong  (1, sectionId);
			    pst.setLong  (2, newPosition);
			    rs = pst.executeQuery();
			    while (rs.next()) {
			    	stm = """
			    			update info
		                       set position = ? 
		                     where id = ? 
			    	""";
			    	pst2 = con.prepareStatement(stm);
			    	pst2.setInt   (1, counterPos);
				    pst2.setLong  (2, rs.getLong("id"));
				    pst2.executeUpdate();
		            pst2.close();
			    	
					counterPos += 10;
			    }
			    rs.close();
			    pst.close();
		    }
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "infoPositionCheckAndRenumber");
		} finally {
			// Закриваємо ресурси
			try {
				if (rs != null) rs.close();
				if (pst != null) pst.close();
				if (pst2 != null) pst2.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		
		return retVal;
	}
	
	/**
	 * повертає скільки разів трапляються іконки з вказаної гілки іконок в таблиці інфоблоків типу "Файл"
	 * @param iconId
	 * @return
	 */
	private int info_FileCountIconId (long iconId) throws SQLException {
		checkConnect();
		
		String sql = "WITH RECURSIVE child_icons(id) AS ("
	               + "    SELECT id FROM icons WHERE id = ?"
	               + "    UNION ALL"
	               + "    SELECT icons.id FROM icons"
	               + "    JOIN child_icons ON icons.parent_id = child_icons.id"
	               + ") "
	               + "SELECT COUNT(*) AS countR FROM info_file "
	               + "WHERE icon_id IN (SELECT id FROM child_icons)";

	    try (PreparedStatement stmt = con.prepareStatement(sql)) {
	        stmt.setLong(1, iconId);
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt("countR");
	            }
	        }
	    }
	    return -1;
	}
	
	/**
	 * Раздел. Копирование всех инфо блоков с одного раздела в другой.
	 * @param
	 */
	public void sectionCopyInfoBlocks (long sectionSrcId, long sectionTrgId) {
		String stm;
		PreparedStatement pst = null;
		ResultSet rs = null;
		PreparedStatement pst2 = null;
		long nextId;
		
		checkConnect();
		
		try {
			stm = """
				  select id, sectionid, infotypeid, infoid, template_style_id,
                         position, name, descr,
                         date_created, date_modified, user_created, user_modified
                    from info
                   where sectionId = ? 
                   order by position
					""";
			pst = con.prepareStatement(stm);
	    	pst.setLong  (1, sectionSrcId);
		    rs = pst.executeQuery();
		    while (rs.next()) {
		    	//-------- copy info block
		    	switch (rs.getInt("infotypeid")) {
		    	case 1 :		// Простий текст
		    		nextId = getNextId("seq_info_text");
		    		
		    		stm = """
		    				insert into info_text (id, title, text, isshowtitle)
				            select ?, title, text, isShowTitle
				              from info_text
				             where id = ?
		    				""";
		    		pst2 = con.prepareStatement(stm);
			    	pst2.setLong  (1, nextId);
			    	pst2.setLong  (2, rs.getInt("infoid"));
			    	pst2.executeUpdate();
		            pst2.close();
		    		break;
		    	case 2 :		// Зображення
		    		nextId = getNextId("seq_info_image");
		    		
		    		stm = """
		    				insert into info_image (id, title, image, width, height, descr, text, 
		    				                        isshowtitle, isshowdescr, isshowtext)
				            select ?, title, image, width, height, descr, text, isshowtitle, isshowdescr, isshowtext
				              from info_Image
				             where id = ?
		    				""";
		    		pst2 = con.prepareStatement(stm);
			    	pst2.setLong  (1, nextId);
			    	pst2.setLong  (2, rs.getInt("infoid"));
			    	pst2.executeUpdate();
		            pst2.close();
		    		break;
		    	case 3 :		// Файл
		    		nextId = getNextId("seq_info_file");
		    		
		    		stm = """
		    				insert into info_file (id, title, file_body, file_name, icon_id, descr, text, 
		    				                       isshowtitle, isshowdescr, isshowtext)
				            select ?, title, file_body, file_name, icon_id, descr, text, 
				                   isshowtitle, isshowdescr, isshowtext
				              from info_file
				             where id = ?
		    				""";
		    		pst2 = con.prepareStatement(stm);
			    	pst2.setLong  (1, nextId);
			    	pst2.setLong  (2, rs.getInt("infoid"));
			    	pst2.executeUpdate();
		            pst2.close();
		    		break;
		    	default : 
		    		nextId = 0;
		    		ShowAppMsg.showAlert(
							"WARNING", "db error",
							"sectionCopyInfoBlocks : невідомий тип інфоблока",
							"infoHeaderId = "+ rs.getLong("id") +", infoTypeId = "+ rs.getInt("infotypeid"));
		    	}

		    	//-------- copy info header
		    	stm = """
		    			insert into info (id, sectionid, infotypeid, infoid, template_style_id,
                                          position, name, descr,
                                          date_created, date_modified, user_created, user_modified)
        	            values (?,?,?,?,?,?,?,?,?,?,?,?)
		    			""";
		    	pst2 = con.prepareStatement(stm);
		    	pst2.setLong  (1, getNextId("seq_info"));
		    	pst2.setLong  (2, sectionTrgId);
		    	pst2.setLong  (3, rs.getLong("infotypeid"));
		    	pst2.setLong  (4, nextId);
		    	pst2.setLong  (5, rs.getLong("template_style_id"));
		    	pst2.setInt   (6, rs.getInt("position"));
		    	pst2.setString(7, rs.getString("name"));
		    	pst2.setString(8, rs.getString("descr"));
		    	pst2.setString(9, rs.getString("date_created"));
		    	pst2.setString(10, rs.getString("date_modified"));
		    	pst2.setString(11, rs.getString("user_created"));
		    	pst2.setString(12, rs.getString("user_modified"));
		    	pst2.executeUpdate();
	            pst2.close();
		    }
		    rs.close();
		    pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert(
					"WARNING", "db error",
					"Ошибка при копировании всех инфо блоков с одного раздела в другой (sectionCopyInfoBlocks).",
					e.getMessage());
		} finally {
			// Закриваємо ресурси
			try {
				if (rs != null) rs.close();
				if (pst != null) pst.close();
				if (pst2 != null) pst2.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * повертає скільки разів трапляються іконки з вказаної гілки іконок в таблиці розділів
	 * @param iconId
	 * @return
	 */
	private int sectionCountIconId (long iconId) throws SQLException {
		checkConnect();
		
		String sql = "WITH RECURSIVE child_icons(id) AS ("
	               + "    SELECT id FROM icons WHERE id = ?"
	               + "    UNION ALL"
	               + "    SELECT icons.id FROM icons"
	               + "    JOIN child_icons ON icons.parent_id = child_icons.id"
	               + ") "
	               + "SELECT COUNT(*) AS countR FROM sections "
	               + "WHERE icon_id IN (SELECT id FROM child_icons)";

	    try (PreparedStatement stmt = con.prepareStatement(sql)) {
	        stmt.setLong(1, iconId);
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt("countR");
	            }
	        }
	    }
	    return -1;
	}
	
	/**
	 * Раздел. Удаление раздела со всеми подчиненными разделами.
	 * @param
	 */
	public void sectionDelete (long id) {
		String stm;
		PreparedStatement pst = null;
		ResultSet rs = null;
		PreparedStatement pst2 = null;
		
		checkConnect();
		
		try {
			// delete documents, dictionaries and info blocks
			stm = """
					WITH RECURSIVE x(id) AS (
	                 SELECT id
                       FROM sections
                      WHERE id = ?
                      UNION  ALL
                     SELECT a.id
                       FROM x
                       JOIN sections a ON a.parent_id = x.id)
                  select s.*
                    from sections s
                   where s.id in (select id from x)
		    """;
			pst = con.prepareStatement(stm);
	    	pst.setLong  (1, id);
		    rs = pst.executeQuery();
		    while (rs.next()) {
		    	if (rs.getInt("type_id") == 1) {		// document
		    		stm = "delete from documents where section_id = ?";
					pst2 = con.prepareStatement(stm);
		            pst2.setLong  (1, rs.getLong("id"));
		            pst2.executeUpdate();
		            pst2.close();
		    		
		    		infoDeleteForSection(rs.getLong("id"));
		    	}
		    	if (rs.getInt("type_id") == 2) {		// dictionary
		    		stm = "delete from dict where section_id = ?";
					pst2 = con.prepareStatement(stm);
		            pst2.setLong  (1, rs.getLong("id"));
		            pst2.executeUpdate();
		            pst2.close();
		    	}
		    }
		    rs.close();
		    pst.close();
		    
		    // delete sections
		    stm = """
					WITH RECURSIVE x(id) AS (
	                 SELECT id
                       FROM sections
                      WHERE id = ?
                      UNION  ALL
                     SELECT a.id
                       FROM x
                       JOIN sections a ON a.parent_id = x.id)
                  delete from sections 
                   where id in (select id from x)
		    """;
		    pst = con.prepareStatement(stm);
	    	pst.setLong  (1, id);
	    	pst.executeUpdate();
            pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "sectionDelete()", 
		             e.getMessage());
		} finally {
			// Закриваємо ресурси
			try {
				if (rs != null) rs.close();
				if (pst != null) pst.close();
				if (pst2 != null) pst2.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Возвращает id пиктограммы по умолчанию для указанного раздела
	 * @param sectionId ;
	 *        isRecursive : true - с проходом вверх до корня (и по умолчанию для всех разделов, если ничего не указано)
	 */
	public long sectionGetIconIdDefault (long sectionId, boolean isRecursive) {
		long retVal = 0;
		SectionItem si = sectionGetById (sectionId);
		
		if (isRecursive) {
			// проходим вверх по дереву к корню
			while ((si.getIconIdDef() == 0) && (si.getParentId() > 0)) {
				si = sectionGetById (si.getParentId());
			}
			
			// если дефолтной иконки нет в разделах, то берем по умолчанию
			if (si.getIconIdDef() == 0) {
				try {
				    retVal = Long.parseLong(settingsGetValue("SECTION_ICON_DEFAULT"));
				} catch (NumberFormatException e) {
					e.printStackTrace();
		    		ShowAppMsg.showAlert("WARNING", "NumberFormatException", 
		    				 "Помилка: Некоректний формат числа!",
		    				 "settingsGetValue(\"SECTION_ICON_DEFAULT\")");
				} catch (DataConnectionException | DataQueryException e) {
					e.writeLog(params);
					ShowAppMsg.showAlert(
							"ERROR", "Помилка читання id піктограми за замовчанням, "+
							"помилка при читанні id піктограми з таблиці налаштувань",
							Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
				}
			} else {
				retVal = si.getIconIdDef();
			}
		} else {
			retVal = si.getIconIdDef();
		}
		
		return retVal;
	}
	
	/**
	 * Возвращает цепочку имен разделов от указанного до самого верхнего родителя. 
	 */
	public String sectionGetPathName (long sectionId, String delimiter) {
		boolean isFirst = true;
		String retVal = "";
		String stm;
		
		checkConnect();
		
		try {
			stm = """
 					WITH RECURSIVE SectionPath ( id, parent_id, name ) AS 
                    (SELECT sc.id, sc.parent_id, sc.name 
                       FROM sections sc 
                      WHERE sc.id = ?
                     UNION 
                     SELECT sp.id, sp.parent_id, sp.name 
                       FROM sections sp 
                      INNER JOIN SectionPath ON (SectionPath.parent_id = sp.id) 
                    ) 
                    select * from SectionPath --order by parent_id desc
	        """;
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong  (1, sectionId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				if (isFirst) {
					isFirst = false;
					retVal = rs.getString("name");
				} else {
					retVal = rs.getString("name") + delimiter + retVal;
				}
			}
			
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
		SectionItem si = sectionGetById (sectionId);
		String templateTag = si.getTemplateMain();
		
		// проходим вверх по дереву к корню
		if ((templateTag == null) || (templateTag.length() == 0)) {
			while ((((si.getTemplateMain() == null) || (si.getTemplateMain().trim().length() == 0)) ||
			     	(((si.getTemplateMain() != null) && (si.getTemplateMain().trim().length() > 0)) &&
			         (si.getTemplateMainTree() == 0))) &&
				   (si.getParentId() > 0)
				  ) {
				si = sectionGetById (si.getParentId());
				templateTag = si.getTemplateMain();
			}
		}
		
		// если тега нет в разделах, то берем по умолчанию
		if ((templateTag == null) || (templateTag.length() == 0)) {
			try {
				templateTag = settingsGetValue("SECTION_TEMPLATE_MAIN_DEFAULT");
			} catch (DataConnectionException | DataQueryException e) {
				e.writeLog(params);
				ShowAppMsg.showAlert(
						"ERROR", "Помилка читання головного шаблона, "+
						"помилка при читанні шаблона з таблиці налаштувань",
						Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
			}
		}
		
		TemplateStyleItem styleI = templateStyleGetByTag (templateTag);
		
		return templateGet (themeId, styleI.getId());
	}
	
	/**
	 * Возвращает id темы для указанного раздела
	 * @param sectionId ;
	 *        isRecursive : true - с проходом вверх до корня (и по умолчанию для всех разделов, если ничего не указано)
	 */
	public long sectionGetThemeId (long sectionId, boolean isRecursive) {
		long retVal = 0;
		SectionItem si = sectionGetById (sectionId);
		
		if (isRecursive) {
			// проходим вверх по дереву к корню
			while ((si.getThemeId() == 0) && (si.getParentId() > 0)) {
				si = sectionGetById (si.getParentId());
			}
			
			// если темы нет в разделах, то берем по умолчанию
			if (si.getThemeId() == 0) {
				try {
				    retVal = Long.parseLong(settingsGetValue("SECTION_THEME_DEFAULT"));
				} catch (NumberFormatException e) {
					e.printStackTrace();
		    		ShowAppMsg.showAlert("WARNING", "NumberFormatException", 
		    				 "Помилка: Некоректний формат числа!",
		    				 "settingsGetValue(\"SECTION_THEME_DEFAULT\")");
				} catch (DataConnectionException | DataQueryException e) {
					e.writeLog(params);
					ShowAppMsg.showAlert(
							"ERROR", "Помилка читання id теми за замовчанням, "+
							"помилка при читанні id теми з таблиці налаштувань",
							Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
				}
			} else {
				retVal = si.getThemeId();
			}
		} else {
			retVal = si.getThemeId();
		}
		
		return retVal;
	}
	
	/**
	 * Перебудовуємо дерево, апдейтимо усі parent_id
	 */
	public void sectionFavoriteRebuildParentId ()
		throws DataConnectionException,DataQueryException {
		PreparedStatement pst = null;
		String stm;

		checkConnectEx();

		try {
			stm = """
				WITH RECURSIVE section_paths AS (
					SELECT
						s.id AS section_id,
						s.parent_id,
						s.id AS child_section_id,
						0 AS depth
					FROM sections s
					UNION ALL
					SELECT
						p.id AS section_id,
						p.parent_id,
						sp.child_section_id,
						sp.depth + 1
					FROM sections p
					JOIN section_paths sp
						ON sp.parent_id = p.id
				),
				candidate_parents AS (
					SELECT
						child_fav.id AS favorite_id,
						parent_fav.id AS new_parent_id,
						sp.depth
					FROM sections_favorite child_fav
					JOIN section_paths sp
						ON sp.child_section_id = child_fav.section_id
					JOIN sections_favorite parent_fav
						ON parent_fav.section_id = sp.section_id
						AND parent_fav."user" = child_fav."user"
					WHERE parent_fav.id <> child_fav.id
						AND child_fav."user" = ?
				),
				calculated AS (
				SELECT
					f.id AS favorite_id,
					(
						SELECT cp.new_parent_id
						FROM candidate_parents cp
						WHERE cp.favorite_id = f.id
						ORDER BY cp.depth ASC
						LIMIT 1
					) AS new_parent_id
				FROM sections_favorite f
				WHERE f."user" = ?
			)
			UPDATE sections_favorite
			SET parent_id = COALESCE((
				SELECT c.new_parent_id
				FROM calculated c
				WHERE c.favorite_id = sections_favorite.id
			), 0)
			WHERE "user" = ?
				AND id IN (
					SELECT favorite_id
					FROM calculated
				)
			""";
			pst = con.prepareStatement(stm);
			pst.setString(1, getCurrentUser());
			pst.setString(2, getCurrentUser());
			pst.setString(3, getCurrentUser());
	
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			throw new DataQueryException (
				DataQueryException.ERRCODE_OTHERS, "sectionFavoriteRebuildParentId",
				"Помилка при перебудуванні дерева Favorite, sectionFavoriteRebuildParentId (\""+user+"\") \n"+dbURL,
				e, 1, null, "SQLException");
		}
	}
	
	/**
	 * Файл для шаблона. Вилучення файла/директорії з усією ієрархією.
	 * @param
	 */
	public void templateFileDelete (long id) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			String stm = """
					WITH RECURSIVE x AS (
					SELECT id
					  FROM template_files
					 WHERE id = ?
					UNION ALL
					SELECT a.id
					  FROM template_files a
					  JOIN x ON a.parent_id = x.id
					)
					DELETE FROM template_files
					 WHERE id IN (SELECT id FROM x)
					""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);
            pst.executeUpdate();
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
		boolean isFirst = true;
		String retVal = "";
		
		checkConnect();
		
		try {
			String stm = """
                 					WITH RECURSIVE FilePath ( id, parent_id, file_name ) AS
					                (SELECT f1.id, f1.parent_id, f1.file_name 
					                   FROM template_files f1
					                  WHERE f1.id = ?
					                  UNION 
					                 SELECT f2.id, f2.parent_id, f2.file_name 
					                   FROM template_files f2
					                  INNER JOIN FilePath ON (FilePath.parent_id = f2.id) 
					                 ) 
					                 select * from FilePath --order by parent_id desc
					        """;
			PreparedStatement pst = con.prepareStatement(stm);
			pst.setLong  (1, fileId);
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
				if (isFirst) {
					isFirst = false;
					if (withFileName) {
						retVal = rs.getString("file_name");
					}
				} else {
					retVal = rs.getString("file_name") + delimiter + retVal;
				}
			}
			
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
	 * Повертає наступний id для таблиці поточних (та дефолтних) стилей шаблонів
	 */
	private long templateStyleCurrentNextId () {
		return getNextId("seq_current_style");
	}
	
	/**
	 * Стили шаблонов. Вилучення стиля/директорії з усією ієрархією та зв'язками з шаблонами по его Id.
	 */
	public void templateStyleDelete (long id) {
		String stm;
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			// вилучаємо поточні/дефолтні
			stm = """
            		WITH RECURSIVE x AS (
					SELECT id
					  FROM template_style
					 WHERE id = ?
					UNION ALL
					SELECT a.id
					  FROM template_style a
					  JOIN x ON a.parent_id = x.id
					)
					delete from current_style
					 where template_style_id in (select id from x)
            		""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);
            pst.executeUpdate();
            pst.close();
            
            // вилучаємо лінки
            stm = """
             		WITH RECURSIVE x AS (
 					SELECT id
 					  FROM template_style
 					 WHERE id = ?
 					UNION ALL
 					SELECT a.id
 					  FROM template_style a
 					  JOIN x ON a.parent_id = x.id
 					)
 					delete from template_style_link
 					 where style_id in (select id from x)
             		""";
             pst = con.prepareStatement(stm);
             pst.setLong  (1, id);
             pst.executeUpdate();
             pst.close();
			
             // вилучаємо стилі
             stm = """
              		WITH RECURSIVE x AS (
  					SELECT id
  					  FROM template_style
  					 WHERE id = ?
  					UNION ALL
  					SELECT a.id
  					  FROM template_style a
  					  JOIN x ON a.parent_id = x.id
  					)
  					delete from template_style
  					 where id in (select id from x)
              		""";
              pst = con.prepareStatement(stm);
              pst.setLong  (1, id);
              pst.executeUpdate();
              pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , templateStyleDelete ("+id+")", 
		             e.getMessage());
		} finally {
			// Закриваємо ресурси
			try {
				if (pst != null) pst.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Возвращает стиль по умолчанию в указанной теме для указанного типа инфо блока.
	 * Если такого стиля нет, возвращается null.
	 */
	public TemplateStyleItem templateStyleGetDefault (long themeId, long infoTypeId) {
		return templateStyleGet(templateStyleGetDefaultId (themeId, infoTypeId, 3));
	}
	
	/**
	 * функция возвращает ид дефолтного стиля для указанных типа инфо блока и темы. 
	 * если не найден , то возвращает 0
	 * searchLevel :
	 *  		1 - ищем только для текущего пользователя
	 * 			2 - еще и для всех (без пользователя)
	 *			3 - если ничего не найдено , возвращает стиль с минимальным ид
	 * (розширений метод з логікою як в постгресовій функції)
	 */
	private long templateStyleGetDefaultId (long themeId, long infoTypeId, int searchLevel) {
		String stm;
		PreparedStatement pst = null;
		ResultSet rs = null;
		long idDef = -1;
		
		checkConnect();
		
		try {
			// шукаємо для користувача 
			stm = """
					SELECT cs.template_style_id
					  FROM template_style s, current_style cs
					 WHERE s.id = cs.template_style_id
					   AND cs.theme_id = ?
					   AND s.infotype_id = ?
					   AND cs."user" = ?
					   AND cs.flag = 0
					""";
     		pst = con.prepareStatement(stm);
	    	pst.setLong  (1, themeId);
		    pst.setLong  (2, infoTypeId);
		    pst.setString(3, getCurrentUser());
		    rs = pst.executeQuery();

		    if (rs.next()) {	// Якщо запис знайдено
		        idDef = rs.getLong("template_style_id");
		    } else {			// Якщо записів немає
		        idDef = 0;
		    }
		    rs.close();
			pst.close();
			
			// шукаємо для всіх
			if ((idDef == 0) && (searchLevel > 1)) {
				stm = """
						SELECT cs.template_style_id
						  FROM template_style s, current_style cs
						 WHERE s.id = cs.template_style_id
						   AND cs.theme_id = ?
						   AND s.infotype_id = ?
						   AND cs."user" is null
						   AND cs.flag = 0
						""";
	     		pst = con.prepareStatement(stm);
		    	pst.setLong  (1, themeId);
			    pst.setLong  (2, infoTypeId);
			    rs = pst.executeQuery();

			    if (rs.next()) {	// Якщо запис знайдено
			        idDef = rs.getLong("template_style_id");
			    } else {			// Якщо записів немає
			        idDef = 0;
			    }
			    rs.close();
				pst.close();
			}
			
			// шукаємо мінімальний стиль (с шаблоном) в довіднику стилей
			if ((idDef == 0) && (searchLevel > 2)) {
				stm = """
						select min(it.id) as template_style_id
						  from template_style it
						 where it.infotype_id = ?
						   and exists (select 1
	                                     from template_style_link t
	                                    where t.style_id = it.id
	                                      and t.theme_id = ?
				          )
						""";
				pst = con.prepareStatement(stm);
				pst.setLong  (1, infoTypeId);
		    	pst.setLong  (2, themeId);
			    rs = pst.executeQuery();

			    if (rs.next()) {	// Якщо запис знайдено
			        idDef = rs.getLong("template_style_id");
			    } else {			// Якщо записів немає
			        idDef = 0;
			    }
			}
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", 
    				"Ошибка при работе с базой данных , templateStyleGetDefaultId ("+themeId+","+infoTypeId+","+searchLevel+")", 
		            e.getMessage());
		} finally {
			// Закриваємо ресурси
			try {
				if (rs != null) rs.close();
				if (pst != null) pst.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		
		return idDef;
	}
	
	/**
	 * функция устанавливает указанный стиль как дефолтный для указанной темы
	 */
	public void templateStyleSetDefault (long themeId, long templateStyleId) {
		String stm;
		PreparedStatement pst = null;
		ResultSet rs = null;
		long infoTypeId;
		long idDef;
		
		checkConnect();
		
		try {
			// находим ид типа инфо блока
			stm = """
					select t.infotype_id
			          from template_style t
			         where t.id = ?
					""";
     		pst = con.prepareStatement(stm);
	    	pst.setLong  (1, templateStyleId);
		    rs = pst.executeQuery();
		    rs.next();
		    infoTypeId = rs.getLong("infotype_id");
		    rs.close();
			pst.close();
			
			// если нет деф. стиля без клиента - инсертим
			idDef = templateStyleGetDefaultId (themeId, infoTypeId, 2);
			if (idDef == 0) {
				stm = """
	            		insert into current_style (id, "user", theme_id, template_style_id, flag,
	            		                           date_created, date_modified)
							values (?, null, ?, ?, 0, ?, ?)
	            		""";
	            pst = con.prepareStatement(stm);
	            pst.setLong  (1, templateStyleCurrentNextId());
	            pst.setLong  (2, themeId);
	            pst.setLong  (3, templateStyleId);
	            pstSetDate (pst, 4, new java.util.Date());
	            pstSetDate (pst, 5, new java.util.Date());
	            pst.executeUpdate();
	            pst.close();
			}
			
			// ищем есть ли для користувача уже дефолтный стиль 
			idDef = templateStyleGetDefaultId (themeId, infoTypeId, 1);
			
			// если есть - апдейтим, если нет - инсертим, если совпадает - ничего не делаем
			if (idDef == 0) {
				stm = """
	            		insert into current_style (id, "user", theme_id, template_style_id, flag,
	            		                           date_created, date_modified)
							values (?, ?, ?, ?, 0, ?, ?)
	            		""";
	            pst = con.prepareStatement(stm);
	            pst.setLong  (1, templateStyleCurrentNextId());
	            pst.setString(2, getCurrentUser());
	            pst.setLong  (3, themeId);
	            pst.setLong  (4, templateStyleId);
	            pstSetDate (pst, 5, new java.util.Date());
	            pstSetDate (pst, 6, new java.util.Date());
	            pst.executeUpdate();
	            pst.close();
			} else {
				if (idDef != templateStyleId) {
					stm = """
		            		UPDATE current_style
				               SET template_style_id = ?,
				                   date_modified = ?
				             WHERE theme_id = ?
				               AND template_style_id = ?
				               AND "user" = ?
		            		""";
		            pst = con.prepareStatement(stm);
		            pst.setLong  (1, templateStyleId);
		            pstSetDate (pst, 2, new java.util.Date());
		            pst.setLong  (3, themeId);
		            pst.setLong  (4, idDef);
		            pst.setString(5, getCurrentUser());
		            pst.executeUpdate();
		            pst.close();
				}
			}
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных, templateStyleSetDefault", 
		             e.getMessage());
		} finally {
			// Закриваємо ресурси
			try {
				if (rs != null) rs.close();
				if (pst != null) pst.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * функция удаляет указанный стиль как дефолтный для указанной темы
	 */
	public void templateStyleUnsetDefault (long themeId, long templateStyleId) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
            String stm = """
            		DELETE FROM current_style
                     WHERE theme_id = ?
                       AND template_style_id = ?
            		   AND "user" = ?
            		   AND flag = 0
            		""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, themeId);
            pst.setLong  (2, templateStyleId);
            pst.setString(3, getCurrentUser());
            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
        	e.printStackTrace();
        	ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных, templateStyleUnsetDefault", 
					             e.getMessage());
		}
	}
	
	/**
	 * Стили шаблонов. Удаление всех стилей.
	 */
	public void templateStylesDelete () {
		String stm;
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			// вилучаємо поточні/дефолтні
			stm = """
					delete from current_style
            		""";
            pst = con.prepareStatement(stm);
            pst.executeUpdate();
            pst.close();
            
            // вилучаємо лінки
            stm = """
 					delete from template_style_link
             		""";
             pst = con.prepareStatement(stm);
             pst.executeUpdate();
             pst.close();
			
             // вилучаємо стилі
             stm = """
  					delete from template_style
              		""";
              pst = con.prepareStatement(stm);
              pst.executeUpdate();
              pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных , templateStylesDelete ()", 
		             e.getMessage());
		} finally {
			// Закриваємо ресурси
			try {
				if (pst != null) pst.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Тема для шаблонов. Удаление темы.
	 */
	public void templateThemeDelete (long id) {
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
			// delete from template_style_link
            String stm = """
					delete from template_style_link  
					 where theme_id = ?
					""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);
            pst.executeUpdate();
			
            // delete from template_themes
            stm = """
					delete from template_themes  
					 where id = ?
					""";
            pst = con.prepareStatement(stm);
            pst.setLong  (1, id);
            pst.executeUpdate();
            
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
		String stm;
		PreparedStatement pst = null;
		
		checkConnect();
		
		try {
            // вилучаємо лінки
            stm = """
             		WITH RECURSIVE x AS (
 					SELECT id
 					  FROM template
 					 WHERE id = ?
 					UNION ALL
 					SELECT a.id
 					  FROM template a
 					  JOIN x ON a.parent_id = x.id
 					)
 					delete from template_style_link
 					 where template_id in (select id from x)
             		""";
             pst = con.prepareStatement(stm);
             pst.setLong  (1, id);
             pst.executeUpdate();
             pst.close();
			
             // вилучаємо стилі
             stm = """
              		WITH RECURSIVE x AS (
  					SELECT id
  					  FROM template
  					 WHERE id = ?
  					UNION ALL
  					SELECT a.id
  					  FROM template a
  					  JOIN x ON a.parent_id = x.id
  					)
  					delete from template
  					 where id in (select id from x)
              		""";
              pst = con.prepareStatement(stm);
              pst.setLong  (1, id);
              pst.executeUpdate();
              pst.close();
		} catch (SQLException e) {
    		e.printStackTrace();
    		ShowAppMsg.showAlert("WARNING", "db error", "Ошибка при работе с базой данных", 
		             "templateDelete ("+ id +")");
		} finally {
			// Закриваємо ресурси
			try {
				if (pst != null) pst.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * SQLite: перевірки доступу немає — завжди повертає true.
	 */
	@Override
	public boolean accessGet (int accessTypeId) {
		return true;
	}

	/**
	 * SQLite: очищення таблиць. Імена без схем (kbase./public.), бо SQLite не підтримує схеми.
	 * Порядок видалення відповідає залежностям FK.
	 */
	@Override
	public void dbClear (boolean clearDocuments, boolean clearInfo,
	                     boolean clearSections, boolean clearTemplates, boolean clearIcons) {
		checkConnect();
		PreparedStatement pst = null;
		try {
			con.setAutoCommit(false);

			// 1. Documents
			if (clearDocuments) {
				pst = con.prepareStatement("DELETE FROM documents");
				pst.executeUpdate(); pst.close();
			}

			// 2. Info blocks
			if (clearInfo) {
				pst = con.prepareStatement("DELETE FROM info_file");
				pst.executeUpdate(); pst.close();

				pst = con.prepareStatement("DELETE FROM info_image");
				pst.executeUpdate(); pst.close();

				pst = con.prepareStatement("DELETE FROM info_text");
				pst.executeUpdate(); pst.close();

				pst = con.prepareStatement("DELETE FROM dict");
				pst.executeUpdate(); pst.close();

				pst = con.prepareStatement("DELETE FROM info");
				pst.executeUpdate(); pst.close();
			}

			// 3. Sections
			if (clearSections) {
				pst = con.prepareStatement("DELETE FROM sections_favorite");
				pst.executeUpdate(); pst.close();

				pst = con.prepareStatement("DELETE FROM sections");
				pst.executeUpdate(); pst.close();
			}

			// 4. Templates
			if (clearTemplates) {
				pst = con.prepareStatement("DELETE FROM template_style_link");
				pst.executeUpdate(); pst.close();

				pst = con.prepareStatement("DELETE FROM current_style");
				pst.executeUpdate(); pst.close();

				pst = con.prepareStatement("DELETE FROM template_style");
				pst.executeUpdate(); pst.close();

				pst = con.prepareStatement("DELETE FROM template");
				pst.executeUpdate(); pst.close();

				pst = con.prepareStatement("DELETE FROM template_files");
				pst.executeUpdate(); pst.close();

				pst = con.prepareStatement("DELETE FROM template_themes");
				pst.executeUpdate(); pst.close();
			}

			// 5. Icons
			if (clearIcons) {
				pst = con.prepareStatement("DELETE FROM current_icon");
				pst.executeUpdate(); pst.close();

				pst = con.prepareStatement("DELETE FROM icons");
				pst.executeUpdate(); pst.close();
			}

			con.commit();

			// Скидання sequences
			dbClearResetSequences(clearDocuments, clearInfo, clearSections, clearTemplates, clearIcons);

		} catch (SQLException e) {
			try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
			e.printStackTrace();
			ShowAppMsg.showAlert("ERROR", "Помилка", "Помилка очищення бази даних", e.getMessage());
		} finally {
			try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
		}
	}

	/**
	 * SQLite: скидає sequences — оновлює next_value = 1 у таблиці sequences.
	 */
	@Override
	protected void dbClearResetSequences (boolean clearDocuments, boolean clearInfo,
	                                       boolean clearSections, boolean clearTemplates,
	                                       boolean clearIcons) {
		try {
			PreparedStatement pst;
			final String stm = "UPDATE sequences SET next_value = 1, date_modified = ? WHERE sequence_name = ?";

			if (clearDocuments) {
				pst = con.prepareStatement(stm);
				pstSetDate(pst, 1, new java.util.Date()); pst.setString(2, "seq_documents");
				pst.executeUpdate(); pst.close();
			}
			if (clearInfo) {
				for (String seq : new String[]{"seq_info", "seq_info_text", "seq_info_image", "seq_info_file", "seq_dict"}) {
					pst = con.prepareStatement(stm);
					pstSetDate(pst, 1, new java.util.Date()); pst.setString(2, seq);
					pst.executeUpdate(); pst.close();
				}
			}
			if (clearSections) {
				for (String seq : new String[]{"seq_sections", "seq_sections_favorite"}) {
					pst = con.prepareStatement(stm);
					pstSetDate(pst, 1, new java.util.Date()); pst.setString(2, seq);
					pst.executeUpdate(); pst.close();
				}
			}
			if (clearTemplates) {
				for (String seq : new String[]{"seq_template", "seq_template_files", "seq_template_themes",
				                               "seq_template_style", "seq_template_style_link", "seq_current_style"}) {
					pst = con.prepareStatement(stm);
					pstSetDate(pst, 1, new java.util.Date()); pst.setString(2, seq);
					pst.executeUpdate(); pst.close();
				}
			}
			if (clearIcons) {
				for (String seq : new String[]{"seq_icons", "seq_current_icon"}) {
					pst = con.prepareStatement(stm);
					pstSetDate(pst, 1, new java.util.Date()); pst.setString(2, seq);
					pst.executeUpdate(); pst.close();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "db error", "Помилка скидання sequences", e.getMessage());
		}
	}
}

