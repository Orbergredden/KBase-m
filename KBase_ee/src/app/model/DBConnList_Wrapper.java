package app.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Вспомогательный класс для обёртывания списка параметров подключений.
 * Используется для сохранения списка в XML.
 * 
 * @author Igor Makarevich
 */
@XmlRootElement(name = "DBConnList_Parameters")
public class DBConnList_Wrapper {
	private List<DBConn_Parameters> dbConnListParam;

    @XmlElement(name = "DBConn_Parameters")
    public List<DBConn_Parameters> getList() {
        return dbConnListParam;
    }

    public void setList(List<DBConn_Parameters> dbConnListParam) {
        this.dbConnListParam = dbConnListParam;
    }
}
