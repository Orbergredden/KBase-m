package app.exceptions;

public class DataQueryException extends DataAccessException {
	public static final int ERRCODE_OTHERS = 100;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DataQueryException(int errCode, String errSign, String msg, Exception exception, int level,
			AppException parent, String parentName) {
		super(errCode, errSign, msg, exception, level, parent, parentName);
	}

}
