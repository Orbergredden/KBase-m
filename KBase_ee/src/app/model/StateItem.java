package app.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Класс-элемент для хранения какой либо единицы состояния интерфейса программы
 * @author Igor Makarevich
 */
public class StateItem {
   private StringProperty name;
   private StringProperty params;
   public StateList subItems;

    /**
     * Конструктор по умолчанию.
     */
    public StateItem() {
        this(null, null, null);
    }

    /**
     * Конструктор с данными
     * @param
     */
    public StateItem(String name, String params, StateList subItems) {
        this.name     = new SimpleStringProperty(name);
        this.params   = new SimpleStringProperty(params);
        this.subItems = subItems;
    }

    /**
     * Конструктор с обьектом
     * @param
     */
    public StateItem(StateItem item) {
        this.name     = new SimpleStringProperty(item.getName());
        this.params   = new SimpleStringProperty(item.getParams());
        this.subItems = subItems;
    }

    // name  -- g,s,p
    public String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public StringProperty nameProperty() {
        return name;
    }

    // name  -- g,s,p
    public String getParams() {
        return params.get();
    }
    public void setParams(String params) {
        this.params.set(params);
    }
    public StringProperty paramsProperty() {
        return params;
    }
}
