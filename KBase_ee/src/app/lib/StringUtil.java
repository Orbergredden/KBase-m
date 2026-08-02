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
	
	/**
	 * Шукаємо та повертаємо перше входження тексту
	 * @param multilineText
	 * @param searchText
	 * @param caseSensitive
	 * @return
	 */
	public static String findLineWithText(String multilineText, String searchText, boolean caseSensitive) {
		if (multilineText == null || searchText == null || searchText.isEmpty()) {
			return null;
		}

		String[] lines = multilineText.split("\\r?\\n");

		for (String line : lines) {
			if (caseSensitive) {
				if (line.contains(searchText)) {
					return line;
				}
			} else {
				if (line.toLowerCase().contains(searchText.toLowerCase())) {
					return line;
				}
			}
		}

		return null; // нічого не знайдено
	}
}
