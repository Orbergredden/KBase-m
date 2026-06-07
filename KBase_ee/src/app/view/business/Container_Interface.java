package app.view.business;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * Интерфейс. Работа с внешним окружением различный сцен (редактор инфо блока, просмотр документа, ...)
 */
public interface Container_Interface {
	
	/**
	 * Создается и возвращается новый таб со всеми настройками
	 */
	default Tab createContainer (String title, String icon, String tooltip, AnchorPane pane, Object controller) {
		Tab tab = new Tab();
		
		tab.setTooltip(new Tooltip(tooltip));
		tab.setContent(pane);
		tab.setUserData(controller);         // запихиваем ссылку на контролер для внешних вызовов
	
		// выводим картинку и надпись
		HBox hbox = new HBox();
		Label label_Title = new Label(title);
		hbox.getChildren().add(new ImageView(new Image(icon)));
		hbox.getChildren().add(label_Title);
		tab.setGraphic(hbox);
		
		//-------- для редактирования заголовка
		final TextField textFieldTitle = new TextField();
		
		label_Title.setOnMouseClicked(new EventHandler<MouseEvent>() {  
			@Override  
			public void handle(MouseEvent event) {  
			    if (event.getClickCount() == 2) {  
			    	textFieldTitle.setText(label_Title.getText());  
			    	hbox.getChildren().remove(1);
			    	hbox.getChildren().add(1, textFieldTitle);
			    	textFieldTitle.selectAll();  
			    	textFieldTitle.requestFocus();  
			    }  
			}  
		}); 
		
		textFieldTitle.setOnAction(new EventHandler<ActionEvent>() {  
			@Override  
			public void handle(ActionEvent event) {  
				label_Title.setText(textFieldTitle.getText());  
				hbox.getChildren().remove(1);
		    	hbox.getChildren().add(1, label_Title);
			}  
		});
	
		textFieldTitle.focusedProperty().addListener(new ChangeListener<Boolean>() {  
			@Override  
			public void changed(ObservableValue<? extends Boolean> observable,  
			      Boolean oldValue, Boolean newValue) {  
			    if (! newValue) {  
			    	label_Title.setText(textFieldTitle.getText());
			    	hbox.getChildren().remove(1);
			    	hbox.getChildren().add(1, label_Title);
			    }  
			}  
		});  
		
		return tab;
	}
	
    /**
     * Показывает состояние инфо блока во внешнем контейнере - были несохраненные изменения или нет.
     */
    void showStateChanged(int oid, boolean isChanged);

    /**
     * Закрываем фрейм с контейнером
     */
    void closeContainer (int oid);
}
