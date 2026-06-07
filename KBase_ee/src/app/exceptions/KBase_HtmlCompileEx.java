
package app.exceptions;

/**
 * Класс исключительной ситуации. 
 * Возбуждается при ошибке компиляции документа.
 * 
 * @author Igor Makarevich
 */
public class KBase_HtmlCompileEx extends KBase_Ex {
	public long sectionId;
	public String errorText;

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public KBase_HtmlCompileEx (long sectionId, String errorText, Object obj) {
		super(0, "", "Ошибка компиляции документа (sectionId = "+ sectionId +"). "+ errorText, obj);
		this.sectionId = sectionId;
		this.errorText = errorText;
	}
}
