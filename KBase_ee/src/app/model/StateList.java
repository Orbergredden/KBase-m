package app.model;

import app.lib.FileUtil;
import app.lib.ShowAppMsg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Класс содержит список состояний элементов интерфейса программы
 * @author Igor Makarevich
 */
public class StateList {
	private Params params;
    /**
     * Наблюдаемый Список состояний
     */
    public ObservableList<StateItem> list = FXCollections.observableArrayList();

    private File file;

    /**
     * Constructor
     * визивається для створення дочірніх списків
     */
    public StateList() {
        file = null;
    }

    /**
     * Constructor
     * визивається для роботи з фалом стану за замовчанням (запис або завантаження)
     * @param params
     */
    public StateList(Params params) {
    	this.params = params;
    	file = new File(FileUtil.getFullUserFileName(params, "StateAppCurrent.xml"));
    }
    
    /**
     * Constructor
     * визивається тільки у випадках з повним шляхом у fileName
     */
    public StateList (String fileName) {
        file = new File(fileName);
    }
    
    /**
     * Constructor
     * визивається тільки у випадках з не повним шляхом у fileName
     * @param params
     */
    public StateList(Params params, String fileName) {
    	this.params = params;
    	file = new File(FileUtil.getFullUserFileName(params, fileName));
    }
    
    /**
     * Добавляем конфигурационный параметр в список
     * и возвращает на него ссылку
     */
    public StateItem add (String name, String params, StateList subItems) {
        StateItem si = new StateItem(name, params, subItems);
        this.list.add(si);
        return si;
    }

    /**
     * Загружает информацию из файла.
     * Текущая информация в списке будет заменена.
     */
    public void loadFromFile() {
        try {
            JAXBContext context = JAXBContext.newInstance(StateList_Wrapper.class);
            Unmarshaller um = context.createUnmarshaller();

            // Чтение XML из файла и демаршализация.
            StateList_Wrapper wrapper = (StateList_Wrapper) um.unmarshal(file);

            list.clear();
            list.addAll(wrapper.getList());
        } catch (Exception e) {                           // catches ANY exception
            /*e.printStackTrace();
            ShowAppMsg.showAlert(
                    "ERROR",
                    "Ошибка",
                    "Невозможно загрузить данные из файла " + file.getPath(),
                    e.getMessage());*/
        	System.out.println("Невозможно загрузить данные из файла " + file.getPath());
        	System.out.println(e.getMessage());
        }
    }

    /**
     * Сохраняет список в файле.
     */
    public void saveToFile() {
        try {
            JAXBContext context = JAXBContext.newInstance(StateList_Wrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Обёртываем наши данные
            StateList_Wrapper wrapper = new StateList_Wrapper();
            wrapper.setList(list);

            // Маршаллируем и сохраняем XML в файл.
            m.marshal(wrapper, file);
        } catch (Exception e) {                             // catches ANY exception
            ShowAppMsg.showAlert(
                    "ERROR",
                    "Ошибка",
                    "Невозможно сохранить данные",
                    "Невозможно сохранить данные в файл " + file.getPath());
        }
    }
}
