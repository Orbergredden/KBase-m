package app.db;

import app.exceptions.DataConnectionException;
import app.lib.KeyStorePrg;
import app.model.DBConn_Parameters;
import app.model.Params;

/**
 * Фабричний клас створення обьєктів дочірніх до DBMain
 */
public class DBMainFactory {
	public DBMain newDBMain (Params params, String type, String host, String port, String name, 
			String user, KeyStorePrg.EncBlob password) throws DataConnectionException { 
		DBMain retVal = null;
		
		switch (type) {
		case DBConn_Parameters.TYPE_CONN_POSTGRES : 
			retVal = new DBMainPostgres (params, host, port, name, user, password);
			break;
		case DBConn_Parameters.TYPE_CONN_SQLITE :
			retVal = new DBMainSQLite (params, host, port, name, null, null);
			break;
		}
		
		return retVal;
	}
}
