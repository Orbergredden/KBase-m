package app.model;

import app.exceptions.DataConnectionException;
import app.exceptions.KBase_DbConnEx;
import app.lib.ShowAppMsg;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Список активных соединений с БД
 * @author Igor Makarevich
 */
public class DBConCurList_Parameters {
	/**
	 * Наблюдаемый Список активных соединений
	 */
	public ObservableList<DBConCur_Parameters> conList = FXCollections.observableArrayList();
	/**
	 * Id текущего соединения (для внешних взаимодействий)
	 */
	public int curId;
	/**
	 * Счетчик внутренних Id
	 */
	private int maxId;
	
	/**
	 * Конструктор
	 */
	public DBConCurList_Parameters () {
		maxId = 0;
	}
	
	/**
	 * добавляем новое соединение в список
	 */
	public int add (Params params, DBConn_Parameters dbConnPar) throws DataConnectionException {
		int connId = ++maxId;
		
		this.conList.add(new DBConCur_Parameters(params, dbConnPar, connId));
		
		return connId;
	}
	
	/**
	 * закрываем все соединения с БД и удаляем всё из списка активных соединений
	 */
	public void clear () {
	
		//-------- закрываем соединения с БД
		for (DBConCur_Parameters conn : conList) {
			try {
				conn.db.close();
			} catch (DataConnectionException e) {
				e.printStackTrace();
			}
		}
		
		//-------- очищаем список активных соединений
		conList.clear();
		curId = 0;
	}
	
	/**
	 * удаляем соединение из списка
	 */
	public void delete (int connId) {
		int index = getIndexById(connId);
		
		// обрабатываем исключение, но запись из списка все равно удаляем
		try {
			conList.get(index).db.close();
		} catch (DataConnectionException e) {
			//e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "Закрываем соединение с БД", "Соединение будет удалено из списка", e.getMsg());
		}
		
		conList.remove(index);
	}
	
	/**
	 * Получаем индекс записи в списке по ИД соединения
	 */
	public int getIndexById(int id) {
		
		for (int i=0; i<conList.size(); i++) {
			if (conList.get(i).Id == id) {
				return i;
			}
		}
		
		return -1;              // не нашли - будет исключение
	}

	/**
	 * Получаем соединение из списка по ID конфигурации соединения.
	 * Если таокое соединение не создано, возвращаем null
	 */
	public DBConCur_Parameters getConnByParamConnId (int connId) {
		DBConCur_Parameters retVal = null;

		for (DBConCur_Parameters conn : conList) {
			if (conn.param.getConnId() == connId) {
				retVal = conn;
			}
		}
		return retVal;
	}
}
