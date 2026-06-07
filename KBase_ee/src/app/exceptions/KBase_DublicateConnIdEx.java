package app.exceptions;

/**
 * Класс исключительной ситуации. 
 * Возбуждается при добавлении в список нового соединения (настроек) к БД, 
 * у которого совпадает Id с существующим соединением в списке .
 * 
 * @author Igor Makarevich
 */
public class KBase_DublicateConnIdEx extends KBase_Ex {
	public int    connId;
	public String connName;

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public KBase_DublicateConnIdEx(int pConnId, String pConnName, Object obj) {
		super(0, "", "Id данного соединения уже существует в списке. Добавление невозможно.", obj);
		connId = pConnId;
		connName = pConnName;
	}
}
