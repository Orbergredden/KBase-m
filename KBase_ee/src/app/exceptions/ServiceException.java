package app.exceptions;

/**
 * помилки бізнес-логіки
 */
public class ServiceException extends AppException {
	// for "errCode"
	public static final int ERRCODE_OTHERS = 100;

	/**
	 * */
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
	public ServiceException(int errCode, String errSign, String msg, Exception exception, int level,
			AppException parent, String parentName) {
		super(errCode, errSign, msg, exception, level, parent, parentName);
	}
}