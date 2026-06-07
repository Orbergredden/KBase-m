
package app.lib;

import app.Main;
import app.exceptions.DataConnectionException;
import app.exceptions.DataQueryException;
import app.model.AppItem_Interface;
import app.model.DBConCur_Parameters;
import app.model.DBConn_Parameters;
import app.model.Params;
import app.model.StateItem;
import app.model.StateList;
import app.model.business.InfoHeaderItem;
import app.model.business.SectionItem;
import app.model.WinItem;
import app.view.business.DictionaryView_Controller;
import app.view.business.DocumentView_Controller;
import app.view.business.FindInfo_Controller;
import app.view.business.InfoEdit_Controller;
import app.view.business.SectionList_Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.prefs.Preferences;

/**
 * Данный класс предназначен для работы по различным обьектам программы.
 * Обьекты такие как Разделы, Стили, Шаблоны, ...
 * 
 * @author Igor Makarevich
 */
public class AppDataObj {
	/**
	 * Раздел. Возвращает id темы (по умолчанию, если не указана) для указанного раздела.
	 */
	public static long sectionGetDefaultTheme (Params params, long sectionId) {
		DBConCur_Parameters conn = params.getConCur();
		long retVal = 0;
		SectionItem si;
		
		//-------- ищем в таблице разделов
		si = conn.db.sectionGetById(sectionId);
		while ((si.getThemeId() == 0) && (si.getParentId() > 0)) {
			si = conn.db.sectionGetById(si.getParentId());
		}
		if (si.getThemeId() > 0) {
			retVal = si.getThemeId();
		} else {
			try {
				retVal = Long.parseLong(conn.db.settingsGetValue("SECTION_MAIN_THEME_DEFAULT"));
			} catch (DataConnectionException | DataQueryException e) {
				e.writeLog(params);
				ShowAppMsg.showAlert(
						"ERROR", "Помилка читання id теми для вказаного розділу, "+
						"помилка при читанні id теми з таблиці налаштувань",
						Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
			}
		}
		
		return retVal;
	}
	
	/**
	 * Повертає заголовок вказаного табу
	 */
	public static String getTabTitle (Tab tab) {
		HBox hbox = (HBox) tab.getGraphic();
		Node nodeTitle;
		if (hbox.getChildren().size() == 3) {
			nodeTitle = hbox.getChildren().get(2);
		} else {
			nodeTitle = hbox.getChildren().get(1);
		}
		
		return ((Label)nodeTitle).getText();
	}
	
	/**
	 * Зберігаємо стан таба в структуру stateList
	 */
	public static void saveTabState (StateList stateList, Tab tab) {
		AppItem_Interface appItem = (AppItem_Interface)tab.getUserData();
		// обьект текущего соединения
		DBConCur_Parameters conCur = null;            
		int dbConnId = 0;
		String tabTitle = AppDataObj.getTabTitle(tab);
		StateItem stateItem;
		
		if (appItem.getDbConnId() != 0) {
			conCur = appItem.getParams().getConnDB().conList.get(appItem.getParams().getConnDB().getIndexById(appItem.getDbConnId()));            
			dbConnId = conCur.param.getConnId();
		}
		
		stateList.add(
				"tabName",
				appItem.getName(),
				null);
		stateList.add(
				"tabDbConnId",
				Integer.toString(dbConnId),
				null);
		stateList.add(
				"tabAppItemId",
				Long.toString(appItem.getAppItemId()),
				null);
		stateList.add(
				"tabRootId",
				Long.toString(appItem.getRootId()),
				null);
		stateList.add(
				"tabCreateAction",
				"",
				null);
		stateList.add(
				"tabRenameTitleAction",
				tabTitle,
				null);
		stateItem = stateList.add(
				"tabSubItems",
				"",
				new StateList());
		appItem.saveControlsState(stateItem.subItems);
	}
	
	/**
	 * Открываем таб с деревом/веткой разделов
	 */
	public static void openSectionTree (Params params, long rootSectionId) {
		DBConn_Parameters conPar = params.getConCur().param;         // параметры текущего соединения
		final Tab tab;                                 // основной таб данного соединения и его основной контейнер
		AnchorPane paneMain = null;
		SectionList_Controller controller = null;      // контроллер сцены внутри таба
		String sectionName = (rootSectionId == 0) ? "усі" : params.getConCur().db.sectionGetById(rootSectionId).getName();

		//======== загружаем контроллер основного таба в AnchorPane
		try {
			// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/SectionList.fxml"));
			paneMain = loader.load();

			// Даём контроллеру доступ к главному прилодению.
			controller = loader.getController();
			controller.setParams(params, rootSectionId);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//======== создаем основной таб для этого соединения и добавляем туда фрейм
		tab = params.getObjContainer().createContainer (
				conPar.getConnName() + " : " + sectionName, 
				"file:resources/images/icon_Sections_16.png", 
				conPar.getConnName() + " : (" + rootSectionId + ") " + sectionName, 
				paneMain, 
				controller);
		tab.setContextMenu(createContextMenuForTab(params.getTabPane_Cur(), tab));

		params.getTabPane_Cur().getTabs().add(tab);
		params.getTabPane_Cur().getSelectionModel().select(tab);
	}
	
	/**
	 * Открываем отдельное окно для просмотра дерева/ветки разделов
	 */
	public void openSectionTreeInWin (Params params, long rootSectionId, Double... d_win) {
		AnchorPane paneForView = null;
		int winId = params.getWinList().getNextId();
		String containerName = "sectionTree_"+ rootSectionId +","+ winId;
		SectionList_Controller controller = null;
		Stage stage = new Stage();
		WinItem winItem = null;
		String sectionName = (rootSectionId == 0) ? "усі" : params.getConCur().db.sectionGetById(rootSectionId).getName();

		//---- загружаем контроллер в AnchorPane
		try {
			// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/SectionList.fxml"));
			paneForView = loader.load();

			// Отображаем сцену, содержащую корневой макет.
			Scene scene = new Scene(paneForView);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			stage.setScene(scene);

			// Даём контроллеру доступ к фрейму с текущим документом
			// и добавляем в список открытых окон
			controller = loader.getController();
			
			winItem = new WinItem (containerName, "SectionList_Controller", controller, stage, params.getWinList());
			params.getWinList().add(winItem);
			params.setStageCur(stage);
			
			controller.setParams(params, rootSectionId);
		} catch (IOException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "Просмотр дерева разделов", "Ошибка при открытии окна просмотра дерева разделов",
					e.getMessage());
		}

		//---- создаем окно и добавляем туда фрейм
		String winTitle = sectionName +" ["+rootSectionId+"]";

		// Создаём окно Stage.
		stage.setTitle(winTitle);
		//dialogStage.initModality(Modality.NONE);
		stage.initOwner(null);
		stage.getIcons().add(new Image("file:resources/images/icon_Sections_16.png"));

		if (d_win.length == 4) {
			stage.setWidth(d_win[0].doubleValue());
			stage.setHeight(d_win[1].doubleValue());
			stage.setX(d_win[2].doubleValue());
			stage.setY(d_win[3].doubleValue());
		}

		// Отображаем диалоговое окно и ждём, пока пользователь его не закроет
		//stage.showAndWait();
		stage.show();
	}
	
	/**
	 * Открываем таб для просмотра документа
	 */
	public static void openDocumentView (Params params, TreeItem<SectionItem> tsi) {
		AnchorPane paneForEdit = null;
		SectionItem si = null;
		SectionList_Controller parentObj = (SectionList_Controller)params.getParentObj();

		//-------- якщо розділ в параметрах не вказаний виходимо
		try {
			si = tsi.getValue();
		} catch (NullPointerException e) {
			// Створюємо StringWriter для збереження стек-трейсу
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);  // Записуємо стек-трейс у StringWriter

			LogFileUser.write(params, sw.toString());
			
			ShowAppMsg.showAlert("ERROR", "Перегляд документа", "AppDataObj.openDocumentView, не вказаний розділ",
					e.getMessage());
			return;
		}

		//--------
		if (si.getTypeId() == 1) {  // Document
			DocumentView_Controller controller = null;
			
			//---- загружаем контроллер таба в AnchorPane
			try {
				// Загружаем fxml-файл и создаём новую сцену
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("view/business/DocumentView_Layout.fxml"));
				paneForEdit = loader.load();

				// Даём контроллеру доступ к фрейму с текущим документом
				controller = loader.getController();
				
				//controller.setParrentObj(parrentObj, conn, objContainer, mainApp.params.getMainStage(), true);
				controller.setParams(params, true);
			} catch (IOException e) {
				e.printStackTrace();
				ShowAppMsg.showAlert("WARNING", "Просмотр документа", "Ошибка при открытии таба просмотра документа",
						e.getMessage());
			}

			//---- создаем основной таб для этого соединения и добавляем туда фрейм
			String tabTitle;
			if (si.getName().length() <= 20)
				tabTitle = si.getName() +" ["+si.getId()+"]";
			else
				tabTitle = si.getName().substring(0,20) +" ["+si.getId()+"]";
			
			Tab tab = params.getObjContainer().createContainer (
					tabTitle,
					"file:resources/images/icon_document_16.png",
					"Розділ : " + si.getName() + "\n"+
					//"Шлях : "+ parentObj.treeViewCtrl.getSectionPath (tsi, 0),
					"Шлях : "+ params.getConCur().db.sectionGetPathName(si.getId(), " / "),
					paneForEdit,
					controller);
			tab.setContextMenu(createContextMenuForTab(params.getTabPane_Cur(), tab));

			params.getTabPane_Cur().getTabs().add(tab);
			params.getTabPane_Cur().getSelectionModel().select(tab);
			
			controller.load(si.getId(), false);
		} else {    // Dictionary
			DictionaryView_Controller controller = null;
			
			//---- загружаем контроллер таба в AnchorPane
			try {
				// Загружаем fxml-файл и создаём новую сцену
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("view/business/DictionaryView.fxml"));
				paneForEdit = loader.load();

				// Даём контроллеру доступ к фрейму с текущим словником
				controller = loader.getController();
				
				//controller.setParrentObj(parrentObj, conn, objContainer, mainApp.params.getMainStage(), true);
				controller.setParams(params, true);
			} catch (IOException e) {
				e.printStackTrace();
				ShowAppMsg.showAlert("WARNING", "Перегляд словника", "Помилка при відкритті таба перегляда словника",
						e.getMessage());
			}

			//---- создаем основной таб для этого соединения и добавляем туда фрейм
			String tabTitle;
			if (si.getName().length() <= 20)
				tabTitle = si.getName() +" ["+si.getId()+"]";
			else
				tabTitle = si.getName().substring(0,20) +" ["+si.getId()+"]";
			
			Tab tab = params.getObjContainer().createContainer (
					tabTitle,
					"file:resources/images/icon_dictionary_16.png",
					"Розділ : " + si.getName() + "\n"+
					//"Шлях : "+ parentObj.treeViewCtrl.getSectionPath (tsi, 0),
					"Шлях : "+ params.getConCur().db.sectionGetPathName(si.getId(), " / "),
					paneForEdit,
					controller);
			tab.setContextMenu(createContextMenuForTab(params.getTabPane_Cur(), tab));

			params.getTabPane_Cur().getTabs().add(tab);
			params.getTabPane_Cur().getSelectionModel().select(tab);
			
			controller.load(si.getId());
		}
	}
	
	/**
	 * Открываем отдельное окно для просмотра документа
	 */
	public void openDocumentViewInWin (Params params, TreeItem<SectionItem> tsi, Double... d_win) {
		AnchorPane paneForView = null;
		int winId = params.getWinList().getNextId();
		SectionItem si = tsi.getValue();
		Stage stage = new Stage();
		WinItem winItem = null;
		
		if (si.getTypeId() == 1) {  // Document
			String containerName = "documentView_"+ si.getId() +","+ winId;
			DocumentView_Controller controller = null;

			//---- загружаем контроллер в AnchorPane
			try {
				// Загружаем fxml-файл и создаём новую сцену
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("view/business/DocumentView_Layout.fxml"));
				paneForView = loader.load();

				// Отображаем сцену, содержащую корневой макет.
				Scene scene = new Scene(paneForView);
				scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
				stage.setScene(scene);

				// Даём контроллеру доступ к фрейму с текущим документом
				// и добавляем в список открытых окон
				controller = loader.getController();
				
				winItem = new WinItem (containerName, "DocumentView_Controller", controller, stage, params.getWinList());
				params.getWinList().add(winItem);
				controller.setParams(params, true);
				
			} catch (IOException e) {
				e.printStackTrace();
				ShowAppMsg.showAlert("WARNING", "Просмотр документа", "Ошибка при открытии окна просмотра документа",
						e.getMessage());
			}

			//---- создаем окно и добавляем туда фрейм
			String winTitle = si.getName() +" ["+si.getId()+"]";

			// Создаём окно Stage.
			stage.setTitle(winTitle);
			//dialogStage.initModality(Modality.NONE);
			stage.initOwner(null);
			stage.getIcons().add(new Image("file:resources/images/icon_document_16.png"));

			if (d_win.length == 4) {
				stage.setWidth(d_win[0].doubleValue());
				stage.setHeight(d_win[1].doubleValue());
				stage.setX(d_win[2].doubleValue());
				stage.setY(d_win[3].doubleValue());
			}

			// Отображаем диалоговое окно и ждём, пока пользователь его не закроет
			//stage.showAndWait();
			stage.show();
			
			controller.load(si.getId(), false);
		} else {     // Dictionary
			String containerName = "dictionaryView_"+ si.getId() +","+ winId;
			DictionaryView_Controller controller = null;

			//---- загружаем контроллер в AnchorPane
			try {
				// Загружаем fxml-файл и создаём новую сцену
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("view/business/DictionaryView.fxml"));
				paneForView = loader.load();

				// Отображаем сцену, содержащую корневой макет.
				Scene scene = new Scene(paneForView);
				scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
				stage.setScene(scene);

				// Даём контроллеру доступ к фрейму с текущим документом
				// и добавляем в список открытых окон
				controller = loader.getController();
				
				winItem = new WinItem (containerName, "DictionaryView_Controller", controller, stage, params.getWinList());
				params.getWinList().add(winItem);
				controller.setParams(params, true);
				
			} catch (IOException e) {
				e.printStackTrace();
				ShowAppMsg.showAlert("WARNING", "Перегляд словника", "Помилка при відкритті вікна перегляду словника",
						e.getMessage());
			}

			//---- создаем окно и добавляем туда фрейм
			String winTitle = si.getName() +" ["+si.getId()+"]";

			// Создаём окно Stage.
			stage.setTitle(winTitle);
			//dialogStage.initModality(Modality.NONE);
			stage.initOwner(null);
			stage.getIcons().add(new Image("file:resources/images/icon_dictionary_16.png"));

			if (d_win.length == 4) {
				stage.setWidth(d_win[0].doubleValue());
				stage.setHeight(d_win[1].doubleValue());
				stage.setX(d_win[2].doubleValue());
				stage.setY(d_win[3].doubleValue());
			}

			// Отображаем диалоговое окно и ждём, пока пользователь его не закроет
			//stage.showAndWait();
			stage.show();
			
			controller.load(si.getId());
		}
	}

	/**
	 * Открываем таб для редактирования инфо блока
	 */
	public static void openEditInfo (Params params, InfoHeaderItem ihi) {
		AnchorPane paneForEdit = null;
		InfoEdit_Controller controller = null;

		//---- загружаем контроллер таба в AnchorPane
		try {
			// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/InfoEdit.fxml"));
			paneForEdit = loader.load();

			// Даём контроллеру доступ к фрейму с текущим документом
			controller = loader.getController();
			controller.setParams(params, ihi.getId());
		} catch (IOException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "Редактирование блока", "Ошибка при открытии таба редактирования инфо блока",
					e.getMessage());
		}
		paneForEdit.requestFocus();

		//---- создаем основной таб для этого соединения и добавляем туда фрейм
		String tabTitle;
		if (ihi.getName().length() <= 20)
			tabTitle = ihi.getName() +" ["+ihi.getSectionId() +","+ ihi.getId()+"]";
		else
			tabTitle = ihi.getName().substring(0,20) +" ["+ihi.getSectionId() +","+ ihi.getId()+"]";
		
		Tab tab = params.getObjContainer().createContainer (
				tabTitle, 
				"file:resources/images/icon_edit_16.png", 
				"Раздел : "+ params.getConCur().db.sectionGetById(ihi.getSectionId()).getName() +"\n"+
					"Блок : "+ ihi.getName(), 
				paneForEdit, 
				controller);
		tab.setContextMenu(createContextMenuForTab(params.getTabPane_Cur(), tab));

		params.getTabPane_Cur().getTabs().add(tab);
		params.getTabPane_Cur().getSelectionModel().select(tab);
	}

	/**
	 * Открываем отдельное окно для редактирования инфо блока
	 */
	public void openEditInfoInWin (Params params, InfoHeaderItem ihi, Double... d_win) {
		AnchorPane paneForEdit = null;
		int winId = params.getWinList().getNextId();
		String containerName = "editInfo_"+ ihi.getSectionId() +","+ ihi.getId() +","+ winId;
		final InfoEdit_Controller controller;
		Stage stage = new Stage();
		WinItem winItem = null;

		//---- загружаем контроллер в AnchorPane
		try {
			// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/InfoEdit.fxml"));
			paneForEdit = loader.load();

			// Отображаем сцену, содержащую корневой макет.
			Scene scene = new Scene(paneForEdit);
			scene.getStylesheets().add((getClass().getResource("/app/view/custom.css")).toExternalForm());
			stage.setScene(scene);

			// Даём контроллеру доступ к фрейму с текущим документом
			// и добавляем в список открытых окон
			controller = loader.getController();
			winItem = new WinItem (containerName, "InfoEdit_Controller", controller, stage, params.getWinList());
			params.getWinList().add(winItem);
			params.setObjContainer(params.getWinList());
			params.setStageCur(stage);
			controller.setParams(params, ihi.getId());
			
			// set hot keys
			/*KeyCombination kc = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
			Runnable rn = ()-> controller.handleButtonSave();
			scene.getAccelerators().put(kc, rn);*/
		} catch (IOException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "Редактирование блока", "Ошибка при открытии окна редактирования инфо блока",
					e.getMessage());
		}

		//---- создаем окно и добавляем туда фрейм
		String winTitle = ihi.getName() +" ["+ihi.getSectionId() +","+ ihi.getId()+"]";

		// Создаём окно Stage.
		stage.setTitle(winTitle);
		//dialogStage.initModality(Modality.NONE);
		stage.initOwner(null);
		stage.getIcons().add(new Image("file:resources/images/icon_edit_16.png"));

		if (d_win.length == 4) {
			stage.setWidth(d_win[0].doubleValue());
			stage.setHeight(d_win[1].doubleValue());
			stage.setX(d_win[2].doubleValue());
			stage.setY(d_win[3].doubleValue());
		}

		// Отображаем диалоговое окно и ждём, пока пользователь его не закроет
		//stage.showAndWait();
		stage.show();
	}
	
	/**
	 * Відкриваємо таб пошуку інформації
	 */
	public static void openFindInfo (Params params, TreeItem<SectionItem> tsi) {
		AnchorPane pane = null;
		SectionItem si = null;
		SectionList_Controller parentObj = (SectionList_Controller)params.getParentObj();

		//-------- якщо розділ в параметрах не вказаний виходимо
		try {
			si = tsi.getValue();
		} catch (NullPointerException e) {
			// Створюємо StringWriter для збереження стек-трейсу
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw); // Записуємо стек-трейс у StringWriter
			
			LogFileUser.write(params, sw.toString());
			
			ShowAppMsg.showAlert("ERROR", "Перегляд документа", "AppDataObj.openDocumentView, не вказаний розділ",
					e.getMessage());
			return;
		}

		//--------
		FindInfo_Controller controller = null;
	
		//---- загружаем контроллер таба в AnchorPane
		try {
			// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/FindInfo.fxml"));
			pane = loader.load();
			
			// Даём контроллеру доступ к фрейму с текущим словником
			controller = loader.getController();
			
			controller.setParams(params, si);
		} catch (IOException e) {
			e.printStackTrace();
			ShowAppMsg.showAlert("WARNING", "Пошук інформації", "Помилка при відкритті таба",
					e.getMessage());
		}

		//---- создаем основной таб и добавляем туда фрейм
		String tabTitle = "Пошук";

		Tab tab = params.getObjContainer().createContainer (
				tabTitle,
				"file:resources/images/icon_find_16.png",
				"Пошук інформації",
				pane,
				controller);
		tab.setContextMenu(createContextMenuForTab(params.getTabPane_Cur(), tab));

		params.getTabPane_Cur().getTabs().add(tab);
		params.getTabPane_Cur().getSelectionModel().select(tab);	
	}
	
	/**
	 * Створюємо контекстне (попап) меню для таба
	 */
	public static ContextMenu createContextMenuForTab(TabPane tabPane, Tab tab) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem moveLeft = new MenuItem("Move Left");
        moveLeft.setGraphic(new ImageView(new Image("file:resources/images/icon_previous_16.png")));
        moveLeft.setOnAction(event -> miMoveTab(tabPane, tab, -1));

        MenuItem moveRight = new MenuItem("Move Right");
        moveRight.setGraphic(new ImageView(new Image("file:resources/images/icon_next_16.png")));
        moveRight.setOnAction(event -> miMoveTab(tabPane, tab, 1));

        MenuItem miSave = new MenuItem("Зберегти стан табу у файл...");
        miSave.setGraphic(new ImageView(new Image("file:resources/images/icon_SaveToFile_16.png")));
        miSave.setOnAction(event -> miSaveToFile(tab));
        
        MenuItem miClose = new MenuItem("Close tab");
        miClose.setGraphic(new ImageView(new Image("file:resources/images/icon_close_16.png")));
        miClose.setOnAction(event -> ((AppItem_Interface)tab.getUserData()).close());
        
        contextMenu.getItems().addAll(
        		moveLeft, moveRight, 
        		new SeparatorMenuItem(),
        		miSave,
        		new SeparatorMenuItem(), 
        		miClose);

        return contextMenu;
    }
	
	/**
	 * Контекстне меню
	 * Передвигає таб вліво чи вправо відносно сусіднього таба
	 */
	private static void miMoveTab(TabPane tabPane, Tab tab, int direction) {
        int currentIndex = tabPane.getTabs().indexOf(tab);
        int newIndex = currentIndex + direction;

        if (newIndex >= 0 && newIndex < tabPane.getTabs().size()) {
            tabPane.getTabs().remove(currentIndex);
            tabPane.getTabs().add(newIndex, tab);
            tabPane.getSelectionModel().select(newIndex);
        }
    }
	
	/**
	 * Контекстне меню
	 * Зберігаємо стан таба у файл
	 */
	private static void miSaveToFile(Tab tab) {
		Preferences prefs = Preferences.userNodeForPackage(AppDataObj.class);
		String tabTitle = getTabTitle(tab);
		StateList stateList;
		
		//==== parse file name
		tabTitle = tabTitle.replace(":", "_");
		
		//==== get file name
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Збереження стану таба в файл");
    	  
        //Set extension filter
    	fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML файли стану таба (*.xml)", "*.xml"));
        
        // set current dir and file
        String curDir = prefs.get("saveTabState_CurDirName", "");
        if (! curDir.equals("")) 
        	fileChooser.setInitialDirectory(new File(curDir));
        
       	fileChooser.setInitialFileName(tabTitle);
        
        //Show save file dialog
        File file = fileChooser.showSaveDialog(null);
        //((AppItem_Interface)tab.getUserData()).getController().params.getStageCur()
		
        if (file != null) {
        	stateList = new StateList(file.toString());
        	saveTabState(stateList, tab);
        	stateList.saveToFile();
        	
            // save dir name
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("saveTabState_CurDirName", curDir);
        }
    }
}
