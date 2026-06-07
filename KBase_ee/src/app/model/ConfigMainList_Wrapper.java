package app.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Вспомогательный класс для обёртывания списка конфигурационных параметров.
 * Используется для сохранения списка в XML.
 * 
 * @author Igor Makarevich
 */
@XmlRootElement(name = "ConfigMainList")
public class ConfigMainList_Wrapper {
	private List<ConfigMainItem> list;

    @XmlElement(name = "ConfigMainItem")
    public List<ConfigMainItem> getList() {
        return list;
    }

    public void setList(List<ConfigMainItem> list) {
        this.list = list;
    }
}
