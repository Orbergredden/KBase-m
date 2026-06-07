package app.model;

import app.db.DBMain;
import app.db.DBMainFactory;
import app.exceptions.DataConnectionException;
import app.exceptions.KBase_DbConnEx;

/**
 * Класс активного соединения с БД
 * @author Igor Makarevich
 */
public class DBConCur_Parameters {
	/**
	 * Параметры подключения
	 */
	public DBConn_Parameters param;
	/**
	 * Id подключения. Нужен для идентификации в списке активных подключений
	 */
	public int Id;
	/**
	 * Обьект работы с БД
	 */
	public DBMain db;
	
	/**
	 * Конструктор
	 * @throws KBase_DbConnEx 
	 */
	public DBConCur_Parameters (Params params, DBConn_Parameters param, int Id) throws DataConnectionException {
		this.param = param;
		this.Id    = Id;
		
		db = (new DBMainFactory()).newDBMain(
				params,
				param.getType(), 
				param.getHost(), param.getPort(), param.getName(),
				param.getLogin(), param.getPassword());
	}
}
