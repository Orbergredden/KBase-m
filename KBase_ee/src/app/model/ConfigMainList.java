package app.model;

import app.lib.FileUtil;
import app.lib.ShowAppMsg;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Класс содержит список главных настроек приложения
 * @author Igor Makarevich
 */
public class ConfigMainList {
	Params params;
	/**
	 * Наблюдаемый Список настроек
	 */
	public ObservableList<ConfigMainItem> list = FXCollections.observableArrayList();
	
	private File file;
	
	/**
	 * Constructor
	 * Використовується для користувацького конфігу
	 */
	public ConfigMainList(Params params) {
		this.params = params;
		
		String filename = FileUtil.getFullUserFileName(params, "ConfigMain.xml");
		file = new File(filename);
		if (! file.exists()) {
			try {
				FileUtil.copyFile(new File("ConfigMain.xml"), file);
			} catch (IOException e) {
				e.printStackTrace();
				ShowAppMsg.showAlert(
	        			"ERROR", 
	        			"Ошибка", 
	        			"Неможливо скопіювати файл" + file.getPath(),
						e.getMessage());
			}
		}
		
		loadFromFile();
	}
	
	/**
	 * Constructor
	 * Використовується зараз тільки для системного конфігу
	 */
	public ConfigMainList(String configFileName) {
		file = new File(configFileName);
		
		loadFromFile();
	}
	
	/**
	 * Добавляем конфигурационный параметр в список
	 */
	public void add (String sectionName, String name, String descr, String value,
					 LocalDate lastModified, boolean isShow, boolean isEditable) {
		int id = LocalDateTime.now().hashCode();
		
		this.list.add(new ConfigMainItem(id, sectionName, name, descr, value,
				lastModified, isShow, isEditable));
	}

	/**
	 * Добавляем конфигурационный параметр в список
	 */
	public void add (String sectionName, String name, String value, LocalDate lastModified) {
		add(sectionName, name, "", value, lastModified, false, false);
	}

	/*
	 * Получаем элемент из списка по секции и имени
	 */
	public ConfigMainItem getItem (String sectionName, String name) {

		for (int i=0; i<list.size(); i++) {
			if (list.get(i).getSectionName().equals(sectionName) && list.get(i).getName().equals(name)) {
				return list.get(i);
			}
		}

		return null;
	}

	/*
	 * Получаем значение элемента из списка по секции и имени
	 */
	public String getItemValue (String sectionName, String name) {
	
		for (int i=0; i<list.size(); i++) {
			if (list.get(i).getSectionName().equals(sectionName) && list.get(i).getName().equals(name)) {
				return list.get(i).getValue();
			}
		}
		
		return null;
	}

	/**
     * Загружает информацию о конфигурации из файла.
     * Текущая информация заменена.
	 */
    public void loadFromFile() {
        try {
            JAXBContext context = JAXBContext.newInstance(ConfigMainList_Wrapper.class);

            Unmarshaller um = context.createUnmarshaller();

            // Чтение XML из файла и демаршализация.
            ConfigMainList_Wrapper wrapper = (ConfigMainList_Wrapper) um.unmarshal(file);

            list.clear();
            list.addAll(wrapper.getList());
        } catch (Exception e) {                           // catches ANY exception
			e.printStackTrace();
        	ShowAppMsg.showAlert(
        			"ERROR", 
        			"Ошибка", 
        			"Невозможно загрузить данные из файла " + file.getPath(),
					e.getMessage());
        }
    }

    /**
     * Сохраняет список подключений в файле.
     */
    public void saveToFile() {
        try {
            JAXBContext context = JAXBContext.newInstance(ConfigMainList_Wrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Обёртываем наши данные
            ConfigMainList_Wrapper wrapper = new ConfigMainList_Wrapper();
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

	/**
	 * Обновляем значение в настройке указанной по разделу и имени
	 */
	public void updateItemValue (String sectionName, String name, String value) {
		for (ConfigMainItem i : list) {
			if (i.getSectionName().equals(sectionName) && i.getName().equals(name)) {
				i.setValue(value);
			}
		}
	}

	/**
	 * Обновляем описание в настройке указанной по разделу и имени
	 */
	public void updateItemDescr (String sectionName, String name, String descr) {
		for (ConfigMainItem i : list) {
			if (i.getSectionName().equals(sectionName) && i.getName().equals(name)) {
				i.setDescr(descr);
			}
		}
	}


}
