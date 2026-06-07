package app.exceptions;

public class DataConnectionException extends DataAccessException {
	// for "errCode"
	public static final int ERRCODE_DRIVER_NOT_FOUND = 1;
	public static final int ERRCODE_SEQURITY = 2;
	public static final int ERRCODE_GET_CONNECTION = 3;
	public static final int ERRCODE_BAD_DB_VERSION = 4;
	public static final int ERRCODE_CLOSE_CONNECTION = 10;
	public static final int ERRCODE_OTHERS = 100;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DataConnectionException(int errCode, String errSign, String msg, Exception exception, int level,
			AppException parent, String parentName) {
		super(errCode, errSign, msg, exception, level, parent, parentName);
	}

}
