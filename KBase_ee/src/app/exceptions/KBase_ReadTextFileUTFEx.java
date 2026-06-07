package app.exceptions;

/**
 * Класс исключительной ситуации. 
 * Возбуждается при ошибке чтения тектового файла в формате UTF-8 функцией ...
 * 
 * @author Igor Makarevich
 */
public class KBase_ReadTextFileUTFEx extends KBase_Ex {
	public String fileName;

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public KBase_ReadTextFileUTFEx(String fileName, String msg) {
		super(0, "", msg + " ("+ fileName +") ", null);
		this.fileName = fileName;
	}
}
