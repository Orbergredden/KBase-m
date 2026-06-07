
package app.exceptions;

/**
 * Базовый в данном приложении класс исключительной ситуации. - застарілий
 * @author Igor Makarevich
 */
public class KBase_Ex extends Exception {
	/**
	 * Код ошибки
	 */
	public int errCode;
	/**
	 * Метка типа ошибки (сигнатура).
	 */
	public String errSign;
	/**
	 * Текстовое сообщение.
	 */
	public String msg;


	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public KBase_Ex(int errCode, String errSign, String errMsg, Object obj) {
		super(errMsg);
		this.errCode = errCode;
		this.errSign = errSign;
		this.msg = errMsg;
	}
	
	/**
	 * Constructor. Без сигнатуры.
	 */
	public KBase_Ex(int errCode, String errMsg, Object obj) {
		super(errMsg);
		this.errCode = errCode;
		this.errSign = "";
		this.msg = errMsg;
	}
}
