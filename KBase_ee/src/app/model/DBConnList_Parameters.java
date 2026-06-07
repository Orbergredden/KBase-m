package app.model;

import app.exceptions.KBase_DublicateConnIdEx;
import app.lib.FileUtil;
import app.lib.KeyStorePrg;
import app.lib.ShowAppMsg;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.xml.bind.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Класс содержит список настроек подключений к БД.
 * 
 * @author Игорь Макаревич
 */
public class DBConnList_Parameters {
	Params params;
	/**
	 * Наблюдаемый Список соединений (параметров соединений)
	 */
	public ObservableList<DBConn_Parameters> dbConnListParam = FXCollections.observableArrayList();
	
	private File file;
	
	/**
	 * Constructor
	 */
	public DBConnList_Parameters (Params params) {
		this.params = params;
		
		String filename = FileUtil.getFullUserFileName(params, "DBConnList.xml");
		file = new File(filename);
		if (! file.exists()) {
			try {
				FileUtil.copyFile(new File("DBConnList.xml"), file);
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
	 * Добавляем соединение в список
	 */
	public void add (String connName, String type, String host, String port, String name, String login, KeyStorePrg.EncBlob password,
			         boolean autoConn, LocalDate lastConn, int counter,  boolean colorEnable,
					 double colorTRed_A, double colorTGreen_A, double colorTBlue_A, double colorTOpacity_A,
					 double colorBRed_A, double colorBGreen_A, double colorBBlue_A, double colorBOpacity_A,
					 double colorTRed_N, double colorTGreen_N, double colorTBlue_N, double colorTOpacity_N,
					 double colorBRed_N, double colorBGreen_N, double colorBBlue_N, double colorBOpacity_N)
			throws KBase_DublicateConnIdEx {
		int connId = LocalDateTime.now().hashCode();
		
		// проходим в цикле по списку и сравниваем названия
		for (int i=0; i<dbConnListParam.size(); i++) {
			if (dbConnListParam.get(i).getConnId() == connId) {
				throw new KBase_DublicateConnIdEx (connId, connName, this);
			}
		}
		
		this.dbConnListParam.add(new DBConn_Parameters (connId, connName, type, host, port, name, login, password, 
				                                        autoConn, lastConn, counter, colorEnable,
				                                        colorTRed_A, colorTGreen_A, colorTBlue_A, colorTOpacity_A,
				                   	                    colorBRed_A, colorBGreen_A, colorBBlue_A, colorBOpacity_A,
				                   	                    colorTRed_N, colorTGreen_N, colorTBlue_N, colorTOpacity_N,
				                   	                    colorBRed_N, colorBGreen_N, colorBBlue_N, colorBOpacity_N));
	}
	
	/**
     * Загружает информацию о конектах из файла.
     * Текущая информация о конектах будет заменена.
     */
    public void loadFromFile() {
        try {
            JAXBContext context = JAXBContext.newInstance(DBConnList_Wrapper.class);
            Unmarshaller um = context.createUnmarshaller();

            // Чтение XML из файла и демаршализация.
            DBConnList_Wrapper wrapper = (DBConnList_Wrapper) um.unmarshal(file);

            dbConnListParam.clear();
            dbConnListParam.addAll(wrapper.getList());
        } catch (Exception e) { // catches ANY exception
        	ShowAppMsg.showAlert(
        			"ERROR", 
        			"Ошибка", 
        			"Невозможно загрузить данные", 
        			"Невозможно загрузить данные из файла " + file.getPath());
        }
    }
	
	/**
     * Сохраняет список подключений в файле.
     */
    public void saveToFile() {
        try {
            JAXBContext context = JAXBContext.newInstance(DBConnList_Wrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Обёртываем наши данные
            DBConnList_Wrapper wrapper = new DBConnList_Wrapper();
            wrapper.setList(dbConnListParam);

            // Маршаллируем и сохраняем XML в файл.
            m.marshal(wrapper, file);
        } catch (Exception e) { // catches ANY exception
        	ShowAppMsg.showAlert(
        			"ERROR", 
        			"Ошибка", 
        			"Невозможно сохранить данные", 
        			"Невозможно сохранить данные в файл " + file.getPath());
        }
    }
}
