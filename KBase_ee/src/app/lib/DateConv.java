
package app.lib;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * для KBase-ee
 * класс по переводу даты и времени из типа строки в дату и обратно. А также в LocalDate
 * v0.4.007 (04.2017 - 01.08.2017)
 * @author Igor Makarevich
 */
public class DateConv {
	/**
	 * Шаблон даты
	 */
	public String tmplDate;
	/**
	 * Шаблон даты и времени
	 */
	public String tmplDateTime;

	private SimpleDateFormat formatDate;
	private SimpleDateFormat formatDateTime;
	
	private DateTimeFormatter formatterDate;
	
	/**
	 * Коструктор
	 */
	public DateConv () {
		// formats for Date
		tmplDate = new String("dd.MM.yyyy");
		
		formatDate = new SimpleDateFormat();
		formatDate.applyPattern(tmplDate.toString());
		
		formatterDate = DateTimeFormatter.ofPattern(tmplDate.toString());
		
		// format for DateTime
		tmplDateTime = new String("dd.MM.yyyy HH:mm:ss");
		
		formatDateTime = new SimpleDateFormat();
		formatDateTime.applyPattern(tmplDateTime.toString());
	}
	
	/**
	 * Коструктор з форматами дати та часу
	 */
	public DateConv (String tmplDate, String tmplDateTime) {
		// formats for Date
		formatDate = new SimpleDateFormat();
		formatDate.applyPattern(tmplDate.toString());
		
		formatterDate = DateTimeFormatter.ofPattern(tmplDate.toString());
		
		// format for DateTime
		formatDateTime = new SimpleDateFormat();
		formatDateTime.applyPattern(tmplDateTime.toString());
	}

	/**
	 * Ковертирует дату из типа Date в LocalDate
	 * @param Date p_date
	 * @return date as LocalDate
	 */
	public LocalDate dateToLocalDate (Date p_date) {
		return p_date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	/**
	 * Ковертирует дату из типа Date в строку по указанному шаблону
	 * @param Date p_date
	 * @return date as String
	 */
	public String dateToStr (Date p_date) {
		return formatDate.format(p_date);
	}
	
	/**
	 * Ковертирует дату и время из типа Date в строку по указанному шаблону
	 * @param Date p_date
	 * @return date as String
	 */
	public String dateTimeToStr (Date p_date) {
		if (p_date == null) return null;
		else                return formatDateTime.format(p_date);
	}
	
	/**
	 * Ковертирует дату из типа LocalDate в строку по указанному шаблону
	 * @param LocalDate p_date
	 * @return date as String
	 */
	public String localDateToStr (LocalDate p_date) {
		return p_date.format(formatterDate);
	}
	
	/**
	 * Ковертирует дату из типа String в Date по указанному шаблону
	 * @param String p_date
	 * @return date as Date
	 */
	public Date strToDate (String p_date) {
		Date retVal = new Date();
		try {
			retVal = formatDate.parse(p_date);
		} catch (java.text.ParseException e) {
            e.printStackTrace();
        }
		
		return retVal;
	}
	
	/**
	 * Ковертирует дату и время из типа String в Date по указанному шаблону
	 * @param String p_date
	 * @return date as Date
	 */
	public Date strToDateTime (String p_date) {
		Date retVal = new Date();
		try {
			retVal = formatDateTime.parse(p_date);
		} catch (java.text.ParseException e) {
            e.printStackTrace();
        }
		
		return retVal;
	}
}
