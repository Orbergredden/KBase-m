package app.model;

import app.view.business.Container_Interface;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Список отдельно открытых окон в приложении
 * @author Igor Makarevich
 */
public class WinList implements Container_Interface {
    /**
     * Наблюдаемый Список отдельно открытых окон в приложении
     */
    public ObservableList<WinItem> items = FXCollections.observableArrayList();
    /**
     * Счетчик внутренних Id
     */
    private int maxId;

    /**
     * Конструктор
     */
    public WinList () {
        maxId = 0;
    }

    /**
     * Получаем следующий по счету id
     */
    public int getNextId () {
        return ++maxId;
    }

    /**
     * добавляем новое окно в список
     */
    public void add (WinItem winItem) {
        items.add(winItem);
    }

    /**
     * удаляем окно из списка
     */
    public void delete (int index) {
        items.remove(index);
    }

    /**
     * По item name получаем индекс в списке
     */
    public int getIndexByOID (int oid) {
        for (int i=0; i<items.size(); i++) {
            if (items.get(i).getController().getOID() == oid) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Реализуем метод интерфейса Container_Interface.
     * Показывает состояние инфо блока во внешнем контейнере - были несохраненные изменения или нет.
     */
    public void showStateChanged(int oid, boolean isChanged) {
        WinItem winItem = items.get(getIndexByOID(oid));
        Stage stage = winItem.getStage();

        stage.getIcons().clear();
        if (isChanged) {               // was change
            stage.getIcons().add(new Image("file:resources/images/icon_edited_16.png"));
        } else {
            stage.getIcons().add(new Image("file:resources/images/icon_edit_16.png"));
        }
        winItem.isChanged = isChanged;
    }

    /**
     * Реализуем метод интерфейса Container_Interface.
     * Закрываем фрейм с редактированием инфо блока
     */
    public void closeContainer (int oid) {
        int index = getIndexByOID(oid);
        Stage stage = items.get(index).getStage();

        delete(index);
        stage.close();
    }
}
