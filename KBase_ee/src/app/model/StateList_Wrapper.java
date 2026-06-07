package app.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Вспомогательный класс для обёртывания списка параметров состояния приложения.
 * Используется для сохранения списка в XML.
 *
 * @author Igor Makarevich
 */
@XmlRootElement(name = "StateList")
public class StateList_Wrapper {
    private List<StateItem> list;

    @XmlElement(name = "StateItem")
    public List<StateItem> getList() {
        return list;
    }

    public void setList(List<StateItem> list) {
        this.list = list;
    }
}
