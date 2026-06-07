package app.model;

import app.lib.ShowAppMsg;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Класс-элемент. Указатели на обьект, расположенный в отдельном окне
 */
public class WinItem {
    private StringProperty name;
    private StringProperty className;
    private AppItem_Interface controller;
    private Stage stage;
    private WinList winList;

    boolean isChanged;

    /**
     * Конструктор по умолчанию.
     */
    public WinItem() {
        this(null, null, null, null, null);
    }

    /**
     * Конструктор с данными
     */
    public WinItem(String name, String className, AppItem_Interface controller, Stage stage, WinList winList) {
        this.name       = new SimpleStringProperty(name);
        this.className  = new SimpleStringProperty(className);
        this.controller = controller;
        this.stage      = stage;
        this.winList    = winList;

        init();
    }

    /**
     * Конструктор с обьектом
     */
    public WinItem(WinItem item) {
        this.name       = new SimpleStringProperty(item.getName());
        this.className  = new SimpleStringProperty(item.getClassName());
        this.controller = item.getController();
        this.stage      = item.getStage();
        this.winList    = item.getWinList();

        init();
    }

    /**
     * Начальная инициализация
     */
    private void init () {
        isChanged = false;

        // событие закрытия окна
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                if (isChanged &&
                    (! ShowAppMsg.showQuestion("CONFIRMATION", "Закрытие окна",
                        "Данные в окне не сохранены.", "Закрыть окно все равно ?")))
                    we.consume();
                else
                    winList.delete(winList.getIndexByOID(getController().getOID()));
            }
        });
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

    // className  -- g,s,p
    public String getClassName() {
        return className.get();
    }
    public void setClassName(String className) {
        this.className.set(className);
    }
    public StringProperty classNameProperty() {
        return className;
    }

    // controller  -- g,s
    public AppItem_Interface getController() {
        return controller;
    }
    public void setController(AppItem_Interface controller) { this.controller = controller; }

    // stage  -- g,s
    public Stage getStage() {
        return stage;
    }
    public void setStage(Stage stage) { this.stage = stage; }
    
    // winList  -- g,s
    public WinList getWinList() {
        return winList;
    }
    public void setWinList (Stage stage) { 
    	this.winList = winList; 
    }
}
