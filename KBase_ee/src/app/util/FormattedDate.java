package app.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Використовується для показу і сортування колонок таблиць типу Дата
 */
public class FormattedDate implements Comparable<FormattedDate> {
	private Date date;

    public FormattedDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        // Форматуємо дату в бажаний формат
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return sdf.format(date);
    }

    @Override
    public int compareTo(FormattedDate other) {
        // Порівнюємо за допомогою методу compareTo у класі Date
        return this.date.compareTo(other.getDate());
    }
}
