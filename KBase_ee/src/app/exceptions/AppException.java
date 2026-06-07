
package app.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import app.lib.LogFileUser;
import app.lib.ShowAppMsg;
import app.model.Params;

/**
 * Базовий в даному додатку класс винятків.
 * @author Igor Makarevich
 */
public class AppException extends Exception {
	/**
	 * Код помилки
	 */
	protected int errCode;
	/**
	 * сигнатура. Можна використовувати для групування типів помилок, наприклад назви методів бд-класів
	 */
	protected String errSign;
	/**
	 * Текстовое сообщение.
	 */
	protected String msg;
	
	/**
	 * початковий виняток
	 */
	protected Exception exception;
	/**
	 * равень вкладенності винятків
	 */
	protected int level;
	/**
	 * стек винятків
	 */
	protected AppException parent;
	/**
	 * назва батьківського винятку
	 */
	protected String parentName;
	
	//
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public AppException(int errCode, String errSign, String msg, Exception exception, int level, AppException parent, String parentName) {
		super(msg);
		this.errCode = errCode;
		this.errSign = errSign;
		this.msg = msg;
		this.exception = exception;
		this.level = level;
		this.parent = parent;
		this.parentName = parentName;
	}
	
	/**
	 * 
	 * @param title
	 * @param text
	 */
	public void showAlert (String title, String text) {
		ShowAppMsg.showAlert("ERROR", title, text, "Код "+ Integer.toString(errCode)+", "+errSign +"\n"+ msg);
	}
	
	/**
	 * Пишемо помилку в користувацький лог файл
	 * @return
	 */
	public void writeLog (Params params) {
		String str = Integer.toString(errCode) +" "+ errSign +"\n"+ msg;
		
		AppException ex = parent;
		String exName = parentName;
		while (ex != null) {
			str += "\n"+ exName +"\n"+ Integer.toString(ex.getErrCode()) +" "+ ex.getErrSign() +"\n"+ ex.getMsg();
			ex = ex.getParent();
			exName = ex.getParentName();
		}
		
		if (exception != null) {
			str += "\n" + getStackTraceAsString(exception);
			//str += "\n" + exception.getMessage();
		}
		
		LogFileUser.write(params, str);
	}
	
	/**
	 * Друкує стек викликів у строку
	 * @param ex
	 * @return
	 */
	public String getStackTraceAsString(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

	//-------- getter/setter
	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public String getErrSign() {
		return errSign;
	}

	public void setErrSign(String errSign) {
		this.errSign = errSign;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public AppException getParent() {
		return parent;
	}

	public void setParent(AppException parent) {
		this.parent = parent;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
}
