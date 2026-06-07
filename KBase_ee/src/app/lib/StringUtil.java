package app.lib;

import javafx.scene.control.ComboBox;

/**
 * Данный класс предназначен для работы со строками различного формата.
 * 
 * @author Igor Makarevich
 */
public class StringUtil {
	/**
	 * Возвращает id из указанной строки формата "Название (id)".
	 */
	public static long getIdFromComboName (String comboName) {
		long retVal = 0;
		int posBegin= comboName.lastIndexOf("(");
		int posEnd  = comboName.lastIndexOf(")");
		String strResult = comboName.substring(posBegin+1, posEnd);
		
		retVal = Long.parseLong(strResult);
		
		return retVal;
	}
	
	/**
	 * знайти елемент у ComboBox за його ID та зробити його поточним
	 * @param comboBox
	 * @param id
	 */
	public static void selectComboBoxItemById(ComboBox<String> comboBox, long id) {
        for (String item : comboBox.getItems()) {
            if (item.matches(".*\\(" + id + "\\)$")) {  // Перевірка, чи рядок закінчується на "(Ід)"
                comboBox.setValue(item);
                return;
            }
        }
        //System.out.println("Item with ID " + id + " not found in ComboBox.");
    }
}
