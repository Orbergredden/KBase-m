package app.view.business;

import app.model.StateItem;
import app.model.StateList;
import app.model.business.Info_TextItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Контроллер инфо блока "Простой текст"
 * @author Igor Makarevich
 */
public class InfoEdit_Text_Controller extends InfoEdit_Simple_Controller {
	
	
	
	@FXML
	private CheckBox checkBox_isShowTitle;
	@FXML
	private TextField textField_title;
	@FXML
	private TextArea textArea_text;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public InfoEdit_Text_Controller () {
    	super();
    }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {       }
	
    /**
     * Инициализирует контролы значениями  
     */
    public void initControlsValue() {       
    	Info_TextItem iti = conn.db.info_TextGet(infoId);
	
    	//checkBox_isShowTitle.setSelected((iti.getIsShowTitle() > 0) ? true : false);
		checkBox_isShowTitle.setSelected(iti.getIsShowTitle() > 0);
    	textField_title.setText(iti.getTitle());
    	textArea_text.setText(iti.getText());
    	textArea_text.setWrapText(true);
    	textArea_text.requestFocus();    ///TODO

		//======== define listeners
        checkBox_isShowTitle.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
                parrentObj.setIsChanged(true);
            }
        });
        textField_title.textProperty().addListener((observable, oldValue, newValue) -> {
            parrentObj.setIsChanged(true);
		});
        textArea_text.textProperty().addListener((observable, oldValue, newValue) -> {
            parrentObj.setIsChanged(true);
        });
    }

	/**
	 * Проверка введенных значений
	 */
	public void check()  {     }
	
	/**
	 * уникальный ИД обьекта
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public int getOID() {
		return hashCode();
	}

	/**
	 * Название элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public String getName() {
		return "InfoEdit_Text_Controller";
	}

	/**
	 * контроллер элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public Object getController() {
		return this;
	}
	
	/**
     * Сохранение информации
     */
	public void save ()    {
		Info_TextItem iti = new Info_TextItem (
				infoId, 
				textField_title.getText(),
				textArea_text.getText(),
				(checkBox_isShowTitle.isSelected()) ? 1 : 0
				);
		conn.db.info_TextUpdate(iti);
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.            <br>
	 * Сохраняем состояние контролов в иерархической структуре
	 */
	public void saveControlsState (StateList stateList) {
		
		stateList.add(
				"caretPosition",
				Integer.toString(textArea_text.getCaretPosition()),
				null);
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Восстанавливаем состояние контролов из иерархической структуры
	 */
	public void restoreControlsState (StateList stateList) {
		
		for (StateItem si : stateList.list) {
			switch (si.getName()) {
				case "caretPosition" :
					textArea_text.positionCaret(Integer.parseInt(si.getParams()));
					break;
			}
		}
	}
}
