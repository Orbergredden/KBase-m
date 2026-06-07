package app.exceptions;

public class DataAccessException extends AppException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param errCode
	 * @param errSign
	 * @param msg
	 * @param exception
	 * @param level
	 * @param parent
	 */
	public DataAccessException(int errCode, String errSign, String msg, Exception exception, int level,
			AppException parent, String parentName) {
		super(errCode, errSign, msg, exception, level, parent, parentName);
	}
}
