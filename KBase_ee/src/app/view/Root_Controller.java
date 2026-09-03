
package app.view;

import app.Main;
import app.lib.AppDataObj;
import app.lib.ConvertType;
import app.lib.ShowAppMsg;
import app.model.AppItem_Interface;
import app.model.ConfigMainList;
import app.model.DBConCur_Parameters;
import app.model.DBConn_Parameters;
import app.model.Params;
import app.model.WinItem;
import app.module.scheduler.view.TaskList_Controller;
import app.view.business.IconsList_Controller;
import app.view.business.template.TemplateList_Controller;
import app.view.business.Container_Interface;
import app.view.structure.TabNavigationHistory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Контроллер для корневого макета. Корневой макет предоставляет базовый
 * макет приложения, содержащий строку меню и место, где будут размещены
 * остальные элементы JavaFX.
 * 
 * @author Игорь Макаревич
 */
public class Root_Controller implements Container_Interface {
	/**
	 * Пункт меню : відкрити збережений таб
	 */
	@FXML
	private MenuItem menuitem_OpenTab;
	/**
	 * Пункт меню : сохранение всей измененной информации в БД
	 */
	@FXML
	private MenuItem menuitem_SaveAll;
	/**
	 * Пункт меню : зберегти стан програми у вибраному файлі
	 */
	@FXML
	private MenuItem menuitem_SaveProgramState;
	
    /**
	 * Пункт меню со Списком Системної інформації
	 */
	@FXML
	private MenuItem menuitem_ConfigSysMainList;
	/**
	 * Пункт меню со Списком Системних налаштувань
	 */
	@FXML
	private MenuItem menuitem_ConfigSysVarsList;
	/**
	 * Пункт меню со Списком Користувацьких налаштувань
	 */
	@FXML
	private MenuItem menuitem_ConfigMainList;
	/**
     * Пункт меню со Списком соединений к источникам данных
     */
    @FXML
    private MenuItem menuitem_ConnectToDB;
	/**
	 * Пункт меню с Разделы документов
	 */
	@FXML
	private MenuItem menuitem_SectionsOfDocuments;
    /**
     * Пункт меню с Каталогом пиктограмм
     */
    @FXML
    private MenuItem menuitem_CatalogIcons;
    /**
     * Пункт меню с Каталогом шаблонов
     */
    @FXML
    private MenuItem menuitem_CatalogTemplates;
    /**
     * Пункт меню Планувальник, Перелік завдань
     */
    @FXML
    private MenuItem menuitem_Tasks;
    /**
     * Пункт меню О программе
     */
    @FXML
    private MenuItem menuitem_About;
    
    /**
     * Главный контейнер вкладок (TabPane)
     */
    @FXML
    private TabPane tabPane_Main; 
    
    /**
     * Для вывода на экран простого сообщения
     */
    @FXML
    private Label label_StatusBar_msg;

	/**
	 * Контейнер для лампочек активных коннектов
	 */
	@FXML
	private HBox containerConnectionIndicator;
	
	private Params params;
	
	private TabNavigationHistory tabNavigationHistory;
	
	/**
	 * Constructor 
	 */
	public Root_Controller() {
		tabNavigationHistory = new TabNavigationHistory();
	}

	/**
     * Вызывается главным приложением, чтобы передать параметры и вернуть ссылку на контроллер
     * по сути это сетер
     */
    public void setParams(Params params) {
    	this.params = params;
    	this.params.setRootController(this);
    	this.params.setTabPane_Main(tabPane_Main);
    	
    	// init controls
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями 
     */
    private void initControlsValue() {
    	//======== main menu
    	menuitem_OpenTab.setGraphic(new ImageView(new Image("file:resources/images/icon_open_16.png")));
    	menuitem_SaveAll.setGraphic(new ImageView(new Image("file:resources/images/icon_save_all_24.png",16,16,false,false)));
    	menuitem_SaveProgramState.setGraphic(new ImageView(new Image("file:resources/images/icon_save_16.png")));
    	menuitem_ConfigSysMainList.setGraphic(new ImageView(new Image("file:resources/images/icon_setting_16.png")));
    	menuitem_ConfigSysVarsList.setGraphic(new ImageView(new Image("file:resources/images/icon_setting_16.png")));
		menuitem_ConfigMainList.setGraphic(new ImageView(new Image("file:resources/images/icon_setting_16.png")));
		menuitem_ConnectToDB.setGraphic(new ImageView(new Image("file:resources/images/icon_Connect_16.png")));
		menuitem_SectionsOfDocuments.setGraphic(new ImageView(new Image("file:resources/images/icon_Sections_16.png",16,16,false,false)));
		menuitem_CatalogIcons.setGraphic(new ImageView(new Image("file:resources/images/icon_CatalogIcons_16.png")));
    	menuitem_CatalogTemplates.setGraphic(new ImageView(new Image("file:resources/images/icon_templates/icon_CatalogTemplates_16.png")));
    	menuitem_Tasks.setGraphic(new ImageView(new Image("file:resources/images/scheduler/icon_scheduler_16.png")));
    	menuitem_About.setGraphic(new ImageView(new Image("file:resources/images/icon_About_16.png")));
    	
    	//======== Tabs
    	//изменение активного таба
    	tabPane_Main.getSelectionModel().selectedItemProperty().addListener(
    	    new ChangeListener<Tab>() {
    	        @Override
    	        public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
    	        	//-------- устанавливаем стили (не активные)
    	        	if (oldTab != null) {
						AppItem_Interface oldTabCtrl = (AppItem_Interface)oldTab.getUserData();

						if (oldTabCtrl.getDbConnId() != 0) {
							// обьект текущего соединения
							DBConCur_Parameters conOld =
									 params.getConnDB().conList.get(params.getConnDB().getIndexById(oldTabCtrl.getDbConnId()));
	    	            	// параметры текущего соединения
	    	            	DBConn_Parameters parOld = conOld.param;
	    	        		
	    	            	//if (parOld.getColorEnable()) {
	    	            	if (parOld.getColorEnable() && 
	    	            	    (! oldTabCtrl.getName().equals(AppItem_Interface.ELEMENT_TASK_LIST))) {
	    	            		oldTab.setStyle("-fx-background-color: #" + 
	    	            				ConvertType.colorToHex(new Color(parOld.getColorBRed_N(),  parOld.getColorBGreen_N(),
	    	            						parOld.getColorBBlue_N(), parOld.getColorBOpacity_N())) + 
	    	            				";");
	    	            		for(Node nodeIn : ((HBox)oldTab.getGraphic()).getChildren()) {
	    	            			if (nodeIn instanceof Label) {
	    	            				((Label)nodeIn).setStyle("-fx-text-fill: #" +
	    	            						ConvertType.colorToHex(new Color(parOld.getColorTRed_N(),  parOld.getColorTGreen_N(),
	    	    	            						parOld.getColorTBlue_N(), parOld.getColorTOpacity_N())) +
	    	            						";-fx-padding: 0 0 0 5px;");
	    	            			}
	    	            		}
	    	            	}
						}
    	        	}
    	        	
    	        	//-------- устанавливаем стили (активные)
    	        	if (newTab != null) {
						AppItem_Interface newTabCtrl = (AppItem_Interface)newTab.getUserData();

						if (newTabCtrl.getDbConnId() != 0) {
							// обьект текущего соединения
	    	            	DBConCur_Parameters conNew = 
	    	            			params.getConnDB().conList.get(params.getConnDB().getIndexById(newTabCtrl.getDbConnId()));
	    	            	// параметры текущего соединения
	    	            	DBConn_Parameters parNew = conNew.param;
	    	        		
	    	            	//if (parNew.getColorEnable()) {
	    	            	if (parNew.getColorEnable() && 
		    	            	    (! newTabCtrl.getName().equals(AppItem_Interface.ELEMENT_TASK_LIST))) {
	    	            		newTab.setStyle("-fx-background-color: #" + 
	    	            				        ConvertType.colorToHex(new Color(parNew.getColorBRed_A(),  parNew.getColorBGreen_A(),
	    	            						                                 parNew.getColorBBlue_A(), parNew.getColorBOpacity_A())) + 
	    	            				        ";");
	    	            		for(Node nodeIn : ((HBox)newTab.getGraphic()).getChildren()) {
	    	            			if (nodeIn instanceof Label) {
	    	            				((Label)nodeIn).setStyle("-fx-text-fill: #" +
	    	            						ConvertType.colorToHex(new Color(parNew.getColorTRed_A(),  parNew.getColorTGreen_A(),
	    	    	            						parNew.getColorTBlue_A(), parNew.getColorTOpacity_A())) +
	    	            						";-fx-padding: 0 0 0 5px;");
	    	            			}
	    	            		}
	    	            	}
						}
    	        	}
    	        	
    	        	//-------- adding a new tab to the history of active tabs (stack)
    	        	if (newTab != null) {
						AppItem_Interface newTabCtrl = (AppItem_Interface)newTab.getUserData();
    	        	
						if (! tabNavigationHistory.getIsDeleted()) {
							tabNavigationHistory.add(newTabCtrl.getOID());
						}
    	        	}
    	        }
    	    }
    	);
    	
    	// обробник для створення списка табів в попап меню
    	setupTabChangeListener(tabPane_Main);
    }
    
    /**
     * обробник для створення списка табів в попап меню
     */
    private void setupTabChangeListener(TabPane tabPane) {
        ContextMenu contextMenu = new ContextMenu();
        ToggleGroup toggleGroup = new ToggleGroup();
        Map<Tab, RadioMenuItem> tabMenuMap = new HashMap<>();

        // Функція для оновлення меню вкладок
        Runnable updateTabMenu = () -> {
            contextMenu.getItems().clear();
            tabMenuMap.clear();

            for (Tab tab : tabPane.getTabs()) {
                // Отримуємо назву з Label, який знаходиться в HBox
                String tabTitle = "";
                if (tab.getGraphic() instanceof HBox) {
                    HBox hbox = (HBox) tab.getGraphic();
                    for (var node : hbox.getChildren()) {
                        if (node instanceof Label) {
                            tabTitle = ((Label) node).getText();
                            break;
                        }
                    }
                }

                RadioMenuItem menuItem = new RadioMenuItem(tabTitle);
                menuItem.setToggleGroup(toggleGroup);

                // Додаємо графіку до пункту меню
                if (tab.getGraphic() instanceof HBox) {
                    HBox hbox = (HBox) tab.getGraphic();
                    if (hbox.getChildren().get(0) instanceof ImageView) {
                        menuItem.setGraphic(new ImageView(((ImageView) hbox.getChildren().get(0)).getImage()));
                    }
                }

                // Обробник натискання на пункт меню
                menuItem.setOnAction(e -> tabPane.getSelectionModel().select(tab));
                contextMenu.getItems().add(menuItem);

                // Додаємо таб і відповідний пункт меню в мапу
                tabMenuMap.put(tab, menuItem);

                // Відмічаємо вибраний таб
                if (tab.equals(tabPane.getSelectionModel().getSelectedItem())) {
                    menuItem.setSelected(true);
                }
            }

            // Знайти кнопку для випадаючого меню вкладок
            Node dropDownButton = tabPane.lookup(".tab-header-area .tab-down-button");
            if (dropDownButton != null) {
                dropDownButton.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        contextMenu.show(dropDownButton, e.getScreenX(), e.getScreenY());
                    }
                });
            }
        };

        // Ініціалізуємо меню при старті
        updateTabMenu.run();

        // Додаємо обробник змін у вкладках
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> updateTabMenu.run());

        // Додаємо обробник змін активного табу
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            // Знімаємо відмітку з усіх пунктів меню
            toggleGroup.getToggles().forEach(toggle -> ((RadioMenuItem) toggle).setSelected(false));

            // Встановлюємо відмітку на пункт меню, що відповідає вибраному табу
            RadioMenuItem menuItem = tabMenuMap.get(newTab);
            if (menuItem != null) {
                menuItem.setSelected(true);
            }
        });

        // Додаємо обробник змін графіки вкладок
        tabPane.getTabs().forEach(tab -> {
            tab.graphicProperty().addListener((obs, oldVal, newVal) -> updateTabMenu.run());
        });
    }

	/**
	 * Создание индикаторов открытых коннектов
	 */
	public void createConnectionIndicators () {
		ToggleGroup groupRadio = new ToggleGroup();

		// очищаем контейнер от индикаторов
		containerConnectionIndicator.getChildren().clear();

		// создаем новый список индикаторов
		for (DBConCur_Parameters c : params.getConnDB().conList) {
			DBConn_Parameters p = c.param;
			RadioButton radioButtonCur = new RadioButton (p.getConnName());
			Label labelEmpty = new Label("   ");

			if (p.getColorEnable())
				radioButtonCur.setStyle
					("-fx-text-fill: #"+
							ConvertType.colorToHex(new Color(p.getColorTRed_A(),  p.getColorTGreen_A(),
									p.getColorTBlue_A(), p.getColorTOpacity_A())) +"; " +
							"-fx-background-color: #"+
							ConvertType.colorToHex(new Color(p.getColorBRed_A(),  p.getColorBGreen_A(),
									p.getColorBBlue_A(), p.getColorBOpacity_A())) +";"
					);
			radioButtonCur.setTooltip(new Tooltip(p.getConnName()));
			radioButtonCur.setToggleGroup(groupRadio);
			radioButtonCur.setSelected(true);
			radioButtonCur.setUserData(c);

			containerConnectionIndicator.getChildren().add(radioButtonCur);
			containerConnectionIndicator.getChildren().add(labelEmpty);
		}
	}

	/**
	 * Возвращает активный коннект
	 */
	public DBConCur_Parameters getActiveConnection () {
		DBConCur_Parameters retVal = null;

		for (Object o : containerConnectionIndicator.getChildren()) {
			if ((o instanceof RadioButton) && (((RadioButton) o).isSelected())) {
				retVal = (DBConCur_Parameters)((RadioButton) o).getUserData();
			}
		}

		return retVal;
	}

	/**
	 * Устанавливает активный коннект
	 */
	public void setActiveConnection (int id) {
		int curNum = 1;

		for (Object o : containerConnectionIndicator.getChildren()) {
			if (o instanceof RadioButton) {
				if (curNum == id) {
					((RadioButton) o).setSelected(true);
				}
				curNum++;
			}
		}
	}
	
	/**
     * Выводит сообщение в Статус Бар
     */
    public void statusBar_ShowMsg (String msg) {
    	label_StatusBar_msg.setText(" " + msg);
    }
	
    /**
	 * відкрити збережений таб
	 */
	@FXML
	private void handleOpenTab () {
		Preferences prefs = Preferences.userNodeForPackage(Root_Controller.class);
		FileChooser fileChooser = new FileChooser();
    	String curDir;

    	fileChooser.setTitle("Виберіть файл зі збереженим табом");
    	
    	// Задаём фильтр расширений
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML файли (*.xml)", "*.xml"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files (*)", "*"));
    	
        // set directory
        curDir = prefs.get("openTab_Dir", "");
        if (! curDir.equals("")) {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory()) {
                fileChooser.setInitialDirectory(dir);
            }
        }
        
        // Показываем диалог загрузки файла
        File file = fileChooser.showOpenDialog(params.getStageCur());
    	
        if (file != null) {
        	params.getMain().restoreControlsStateMain (file.toString());
        	
        	// save dir name
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("openTab_Dir", curDir);
        }
	}
    
	/**
	 * Сохраняем всю измененную информацию
	 */
	@FXML
	public void handleSaveAll () {
		AppItem_Interface appItem;
		
		// по всем табам
		for (int i=0; i<tabPane_Main.getTabs().size(); i++) {
			appItem = (AppItem_Interface)tabPane_Main.getTabs().get(i).getUserData();
			appItem.saveAll();
		}
		
		// по всем окнам
		for (WinItem wi : params.getWinList().items) {
			wi.getController().saveAll();
		}
	}
	
	/**
	 * зберегти стан програми у вибраному файлі
	 */
	@FXML
	public void handleSaveProgramState () {
		Preferences prefs = Preferences.userNodeForPackage(Root_Controller.class);
		
		//======== get file name
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Збереження стану програми в файл");
    	  
        //Set extension filter
    	fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML файли (*.xml)", "*.xml"));
        
        // set current dir and file
        String curDir = prefs.get("saveProgramState_CurDirName", "");
        if (! curDir.equals("")) {
            File dir = new File(curDir);
            if (dir.exists() && dir.isDirectory()) {
                fileChooser.setInitialDirectory(dir);
            }
        }
        
        String fileName = prefs.get("saveProgramState_FileName", "");
        if (! fileName.equals(""))
        	fileChooser.setInitialFileName(fileName);
        
        //Show save file dialog
        File file = fileChooser.showSaveDialog(params.getStageCur());
    	
        if (file != null) {
        	//перевіряємо існування файла з таким іменем і перепитуємо
        	/*if (file.exists()) {
        		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Збереження стану програми в файл",
						"Файл з ім'ям '"+ file.toString() +"' вже існує", "Перезаписати його ?")) {
        			return;
        		}
        	}*/
        	
        	// save
        	params.getMain().saveControlsStateMain (file.toString());
        	
            // save filename and dir name
        	fileName = file.getAbsolutePath();
        	fileName = fileName.substring(fileName.lastIndexOf(File.separator)+1, fileName.length());
        	curDir = file.getAbsolutePath();
        	curDir = curDir.substring(0, curDir.lastIndexOf(File.separator));
        	prefs.put("saveProgramState_CurDirName", curDir);
        	prefs.put("saveProgramState_FileName", fileName);
        }
	}
	
	/**
	 * Вікно зі списком "Системна інформація"
	 */
	@FXML
	private void handleConfigSysMainList() {
		showConfig("Системна інформація", params.getConfigSys());
	}
	
	/**
	 * Вікно зі списком "Системна інформація"
	 */
	@FXML
	private void handleConfigSysVarsList() {
		showConfig("Системні налаштування", params.getConfigSysVars());
	}

	/**
	 * Вікно зі списком "Користувацькі налаштування"
	 */
	@FXML
	private void handleConfigMainList() {
		showConfig("Користувацькі налаштування", params.getConfig());
	}
    
    /**
     * Закрывает приложение.
     */
    @FXML
    private void handleExit() {
    	if (params.getMain().beforeExit()) {
    		System.exit(0);
    	}
    }

	/**
	 * Подключаемся к БД. Есть возможность администрирования коннектов.
	 */
	@FXML
	private void handleConnectToDB() {
		Params params = new Params(this.params);
		
		try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/DBConnList_Layout.fxml"));
			AnchorPane page = loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Список параметров подключений");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_Connect_16.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(DBConnList_Controller.class);
			dialogStage.setWidth(prefs.getDouble("stageDBConnList_Width", 600));
			dialogStage.setHeight(prefs.getDouble("stageDBConnList_Height", 400));
			dialogStage.setX(prefs.getDouble("stageDBConnList_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageDBConnList_PosY", 0));
		
			// Даём контроллеру доступ к главному прилодению.
			DBConnList_Controller controller = loader.getController();

			params.setObjContainer(this);
			params.setTabPane_Cur(tabPane_Main);
			params.setStageCur(dialogStage);
			
	        controller.setParams(params);
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
		} catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Открывает таб с деревом разделов документов
	 */
	@FXML
	public void handleSectionsOfDocuments() {
		DBConCur_Parameters conCur = getActiveConnection();        // обьект текущего соединения

		//
		if (conCur == null) {
			//e.printStackTrace();
			ShowAppMsg.showAlert("INFORMATION", "Сообщение", "Нет активного соединения с источником данных", "Необходимо подключиться.");
			//handleConnectToDB();
			return;
		}

		//-------- открываем таб
		Params params = new Params(this.params);
		params.setConCur(conCur);
		params.setObjContainer(this);
		params.setTabPane_Cur(tabPane_Main);
		params.setStageCur(params.getMainStage());
		
		AppDataObj.openSectionTree (params, 0);
	}
	
    /**
     * Открывает таб со справочником пиктограмм (древовидный список иконок)
     */
    @FXML
    public void handleCatalogIcons() {
		IconsList_Controller controller = null;
		DBConCur_Parameters conCur = getActiveConnection();        // обьект текущего соединения
		Params params = new Params(this.params);

    	//
		if (conCur == null) {
			//e.printStackTrace();
			ShowAppMsg.showAlert("INFORMATION", "Сообщение", "Нет активного соединения с источником данных", "Необходимо подключиться.");
			//handleConnectToDB();
			return;
		}

    	// параметры текущего соединения
    	DBConn_Parameters conPar = conCur.param;
    	// новый добавляемый Таб с иконками
    	final Tab tab;
    	// основной контейнер для таба
    	AnchorPane page = null;
    	
    	//======== загружаем контроллер таба в AnchorPane
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/IconsList_Layout.fxml"));
			page = loader.load();
			
			// Даём контроллеру доступ к главному прилодению.
			controller = loader.getController();
			
			params.setConCur(conCur);
			params.setObjContainer(this);
			params.setTabPane_Cur(tabPane_Main);
			
	        controller.setParams(params);
    	} catch (IOException e) {
            e.printStackTrace();
        }
    	
    	//======== создаем основной таб для этого соединения и добавляем туда фрейм
    	tab = createContainer (
    			conPar.getConnName() + " - іконки", 
    			"file:resources/images/icon_CatalogIcons_16.png", 
    			conPar.getConnName() + " - іконки",
				page, 
				controller);
    	tab.setContextMenu(AppDataObj.createContextMenuForTab(params.getTabPane_Cur(), tab));
    	
    	tabPane_Main.getTabs().add(tab);
    	tabPane_Main.getSelectionModel().select(tab);
    }
    
    /**
     * Открывает таб со справочником шаблонов (древовидный список тем и шаблонов)
     */
    @FXML
    public void handleCatalogTemplates() {
		TemplateList_Controller controller = null;
		DBConCur_Parameters conCur = getActiveConnection();        // обьект текущего соединения
		Params params = new Params(this.params);

		//
		if (conCur == null) {
			ShowAppMsg.showAlert("INFORMATION", "Сообщение", "Немає активного з'єднання з істочником даних", "Необходимо подключиться.");
			//handleConnectToDB();
			return;
		}
    	
    	// параметры текущего соединения
    	DBConn_Parameters conPar = conCur.param;
    	// новый добавляемый Таб с шаблонами
    	final Tab tab;
    	// основной контейнер для таба
    	AnchorPane page = null;
    
    	//======== загружаем контроллер таба в AnchorPane
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/template/TemplateList.fxml"));
			page = loader.load();
			
			// Даём контроллеру доступ к главному прилодению.
			controller = loader.getController();
			
			params.setConCur(conCur);
			params.setObjContainer(this);
			params.setTabPane_Cur(tabPane_Main);
			
	        controller.setParams(params);
    	} catch (IOException e) {
            e.printStackTrace();
        }
    	
    	//======== создаем основной таб для этого соединения и добавляем туда фрейм
    	tab = createContainer (
    			conPar.getConnName() + " - шаблоны", 
    			"file:resources/images/icon_templates/icon_CatalogTemplates_16.png", 
    			conPar.getConnName() + " - шаблоны",
				page, 
				controller);
    	tab.setContextMenu(AppDataObj.createContextMenuForTab(params.getTabPane_Cur(), tab));
    	
    	tabPane_Main.getTabs().add(tab);
    	tabPane_Main.getSelectionModel().select(tab);
    }
    
    /**
     * Відкриває таб Планувальник, Перелік завдань
     */
    @FXML
    public void handleTasks() {
    	TaskList_Controller controller = null;
		DBConCur_Parameters conCur = getActiveConnection();        // обьект текущего соединения
		Params params = new Params(this.params);

		//
		//if (conCur == null) {
		//	ShowAppMsg.showAlert("INFORMATION", "Повідомлення", "Немає активного з'єднання з істочником даних", "Працюємо тільки локально.");
			//handleConnectToDB();
			//return;
		//}
		
		// таб з Завданнями можливо було відкривати тільки в одному екземплярі
		for (int i=0; i<tabPane_Main.getTabs().size(); i++) {
			if ((tabPane_Main.getTabs().get(i).getUserData() != null) && 
				(((AppItem_Interface)tabPane_Main.getTabs().get(i).getUserData()).getName() == 
					AppItem_Interface.ELEMENT_TASK_LIST)) {
				ShowAppMsg.showAlert("INFORMATION", "Повідомлення", 
						"Таб з Завданнями можливо відкривати тільки в одному екземплярі", "");
				return;
			}
		}
    	
    	// параметры текущего соединения
    	DBConn_Parameters conPar = null;
// не підфарбовуємо таб
//    	if (conCur != null)
//    		conPar = conCur.param;
    	// новый добавляемый Таб
    	final Tab tab;
    	// основной контейнер для таба
    	AnchorPane page = null;
    
    	//======== загружаем контроллер таба в AnchorPane
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("module/scheduler/view/TaskList.fxml"));
			page = loader.load();
			
			// Даём контроллеру доступ к главному прилодению.
			controller = loader.getController();
			
			params.setConCur(conCur);
			params.setObjContainer(this);
			params.setTabPane_Cur(tabPane_Main);
			
	        controller.setParams(params);
    	} catch (IOException e) {
            e.printStackTrace();
        }
    	
    	//======== создаем основной таб для этого соединения и добавляем туда фрейм
    	String tabName;
    	
    	if (conPar != null)
    		tabName = conPar.getConnName() + " - завдання";
    	else
    		tabName = "Завдання";
    	
    	tab = createContainer (
    			tabName, 
    			"file:resources/images/scheduler/icon_scheduler_16.png", 
    			tabName,
				page, 
				controller);
    	tab.setContextMenu(AppDataObj.createContextMenuForTab(params.getTabPane_Cur(), tab));
    	
    	tabPane_Main.getTabs().add(tab);
    	tabPane_Main.getSelectionModel().select(tab);
    }
    
    /**
     * Открывает диалоговое окно about.
     */
    @FXML
    private void handleAbout() {
    	ShowAppMsg.showAlert("INFORMATION", "О програмі", params.getConfigSys().getItemValue("AboutProgram", "name"), 
				 "Версія: "+params.getConfigSys().getItemValue("AboutProgram", "version")+
				 " ("+params.getConfigSys().getItemValue("AboutProgram", "BeginDate")+
				 " - "+params.getConfigSys().getItemValue("AboutProgram", "EndDate")+")\n" +
                "Кодова назва : "+params.getConfigSys().getItemValue("AboutProgram", "CodeName")+" \n" +
                "Автор: "+params.getConfigSys().getItemValue("AboutProgram", "Author")+"\n" +
                   "Сайт: "+params.getConfigSys().getItemValue("AboutProgram", "site"));
    }

	/**
	 * Реализуем метод интерфейса Container_Interface.
	 * Показывает состояние инфо блока во внешнем контейнере - были несохраненные изменения или нет.
	 */
	public void showStateChanged(int oid, boolean isChanged) {
		Tab ourTab;
		HBox hbox;
		String changingIconId = "changingIconId";

		//---- ищем таб по oid
		for (int i=0; i<tabPane_Main.getTabs().size(); i++) {
			//if ((tabPane_Main.getTabs().get(i).getId() != null) && tabPane_Main.getTabs().get(i).getId().equals(containerId)) {
			if ((tabPane_Main.getTabs().get(i).getUserData() != null) && 
				(((AppItem_Interface)tabPane_Main.getTabs().get(i).getUserData()).getOID() == oid)) {
				ourTab = tabPane_Main.getTabs().get(i);
				hbox = (HBox) ourTab.getGraphic();

				if (isChanged) {               // was change
					if ((hbox.getChildren().get(1).getId() == null) ||
							(! hbox.getChildren().get(1).getId().equals(changingIconId))) {
						ImageView imageView_changed;

						imageView_changed = new ImageView(new Image("file:resources/images/icon_edited_16.png"));
						imageView_changed.setId(changingIconId);

						hbox.getChildren().add(1, imageView_changed);
					}
				} else {                       // no change
					if ((hbox.getChildren().get(1).getId() != null) &&
							(hbox.getChildren().get(1).getId().equals(changingIconId))) {
						hbox.getChildren().remove(1);
					}
				}
			}
		}
	}

	/**
	 * Реализуем метод интерфейса Container_Interface.
	 * Закрываем фрейм с редактированием инфо блока
	 */
	public void closeContainer (int oid) {
		
		for (int i=0; i<tabPane_Main.getTabs().size(); i++) {
			if ((tabPane_Main.getTabs().get(i).getUserData() != null) && 
				(((AppItem_Interface)tabPane_Main.getTabs().get(i).getUserData()).getOID() == oid)) {
				
				tabNavigationHistory.delete(oid);
				
				tabPane_Main.getTabs().remove(i);
			}
		}
		
		tabNavigationHistory.setIsDeleted(false);
		int lastOId = tabNavigationHistory.getLast();
		
		if (lastOId != 0) {
			for (int i=0; i<tabPane_Main.getTabs().size(); i++) {
				if ((tabPane_Main.getTabs().get(i).getUserData() != null) && 
					(((AppItem_Interface)tabPane_Main.getTabs().get(i).getUserData()).getOID() == lastOId)) {
					
					// activate tab
					tabPane_Main.getSelectionModel().select(i);
				}
			}
		}
	}
	
	private void showConfig (String title, ConfigMainList configList) {
		try {
			// Загружаем fxml-файл и создаём новую сцену для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/ConfigMainList.fxml"));
			AnchorPane page = loader.load();

			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle(title);
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_setting_16.png"));

			Preferences prefs = Preferences.userNodeForPackage(ConfigMainList_Controller.class);
			dialogStage.setWidth(prefs.getDouble("ConfigMainList_Width", 600));
			dialogStage.setHeight(prefs.getDouble("ConfigMainList_Height", 400));
			dialogStage.setX(prefs.getDouble("ConfigMainList_PosX", 0));
			dialogStage.setY(prefs.getDouble("ConfigMainList_PosY", 0));

			// Даём контроллеру доступ к главному прилодению.
			ConfigMainList_Controller controller = loader.getController();
			controller.setMainApp(configList, dialogStage);

			// Отображаем диалоговое окно и ждём, пока пользователь его не закроет
			dialogStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
