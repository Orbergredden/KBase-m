package app.view;

import app.Main;
import app.exceptions.DataConnectionException;
import app.exceptions.KBase_DublicateConnIdEx;
import app.lib.ConvertType;
import app.lib.ShowAppMsg;
import app.model.DBConn_Parameters;
import app.model.Params;
import app.model.DBConCur_Parameters;
import app.model.DBConnList_Parameters;

import java.io.IOException;
import java.time.LocalDate;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Контроллер окна списка параметров подключений к БД.
 * 
 * @author Игорь Макаревич
 */
public class DBConnList_Controller {
	private Params params;
	/**
	 *  Ссылка на главное приложение
	 */
    private Main mainApp;
	/**
	 * Список настроек подключений к БД
	 */
	public DBConnList_Parameters dbConnList;
	/**
	 * dialogStage данного окна
	 */
	private Stage dialogStage;
	
	@FXML
	private TableView<DBConn_Parameters> tableView_Connections;
	@FXML
    private TableColumn<DBConn_Parameters, String> tableColumn_ConnName;
	@FXML
    private TableColumn<DBConn_Parameters, String> tableColumn_Type;
	@FXML
    private TableColumn<DBConn_Parameters, String> tableColumn_Host;
	@FXML
    private TableColumn<DBConn_Parameters, String> tableColumn_Port;
	@FXML
    private TableColumn<DBConn_Parameters, String> tableColumn_Name;
	@FXML
    private TableColumn<DBConn_Parameters, String> tableColumn_Login;
	@FXML
    private TableColumn<DBConn_Parameters, Boolean> tableColumn_AutoConn;
	@FXML
    private TableColumn<DBConn_Parameters, LocalDate> tableColumn_lastConn;
	@FXML
    private TableColumn<DBConn_Parameters, Integer> tableColumn_counter;
	
	@FXML
	private MenuItem menuitem_Add;
	@FXML
	private MenuItem menuitem_Edit;
	@FXML
	private MenuItem menuitem_Delete;
	@FXML
	private MenuItem menuitem_Connect;
	@FXML
	private MenuItem menuitem_Disconnect;
	
	@FXML
	private Button button_Add;
	@FXML
	private Button button_Edit;
	@FXML
	private Button button_Delete;
	@FXML
	private Button button_Connect;
	@FXML
	private Button button_Disconnect;
	@FXML
	private Button button_Exit;
	
	// для отслеживания предыдущих активных строк
	private int _lastSelectedIndex = -1;
    private int _previousSelectedIndex = -1;

    //
	private Preferences prefs;

	//
	private TableColumn currentSortColumn;
	
	/**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public DBConnList_Controller () {     }
    
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {    }
	
    /**
     * Вызывается родительским обьектом, который передает параметры.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params) {
    	this.params = params;
    	
    	this.dbConnList = new DBConnList_Parameters(params);
        this.mainApp = params.getMain();
        this.dialogStage = params.getStageCur();
    	
        // init controls
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями из главного класса
     */
    private void initControlsValue() {
		prefs = Preferences.userNodeForPackage(DBConnList_Controller.class);

    	//======== Инициализация таблицы
    	tableColumn_AutoConn.setCellFactory(CheckBoxTableCell.forTableColumn(tableColumn_AutoConn));
    	
    	tableColumn_ConnName.setCellValueFactory(new PropertyValueFactory<>("connName"));
    	tableColumn_Type.setCellValueFactory(new PropertyValueFactory<>("type"));
    	tableColumn_Host.setCellValueFactory(new PropertyValueFactory<>("host"));
    	tableColumn_Port.setCellValueFactory(new PropertyValueFactory<>("port"));
    	tableColumn_Name.setCellValueFactory(new PropertyValueFactory<>("name"));
    	tableColumn_Login.setCellValueFactory(new PropertyValueFactory<>("login"));
    	tableColumn_AutoConn.setCellValueFactory(c -> c.getValue().autoConnProperty());
    	tableColumn_lastConn.setCellValueFactory(new PropertyValueFactory<>("lastConn"));
    	tableColumn_counter.setCellValueFactory(new PropertyValueFactory<>("counter"));
    	
    	tableView_Connections.setItems(dbConnList.dbConnListParam);

		//---- восстанавливаем сортировку таблицы по столбцу
		String sortColumnId = prefs.get("stageDBConnList_sortColumnId","");

		if (! sortColumnId.equals("")) {
			for (TableColumn column : tableView_Connections.getColumns()) {
				if (column.getId().equals(sortColumnId)) {
					String sortType = prefs.get("stageDBConnList_sortType","ASCENDING");

					currentSortColumn = column;
					tableView_Connections.getSortOrder().add(column);
					if (sortType.equals("DESCENDING")) column.setSortType(TableColumn.SortType.DESCENDING);
					else                               column.setSortType(TableColumn.SortType.ASCENDING);
					column.setSortable(true); // This performs a sort
				}
			}
		}

    	//======== Обработчик событий строки таблицы
    	tableView_Connections.setRowFactory( tv -> {
    	    TableRow<DBConn_Parameters> row = new TableRow<>();
    	    
    	    //-------- коннект при двойном клике
    	    row.setOnMouseClicked(event -> {
    	        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
    	        	//DBConn_Parameters rowData = row.getItem();
    	        	handleButtonConnect();
    	        }
    	    });
    	    return row ;
    	});
    	
    	//======== устанавливаем событие на изменение ячеек таблицы ConnName
    	tableColumn_ConnName.setCellFactory(new Callback<TableColumn<DBConn_Parameters, String>, 
    			                                         TableCell<DBConn_Parameters, String>>() {
            public TableCell<DBConn_Parameters, String> call(TableColumn<DBConn_Parameters, String> param) {
                return new TableCell<DBConn_Parameters, String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        try {
                        	DBConn_Parameters curRow = getTableView().getItems().get(getIndex());
                            
                            if (! curRow.getColorEnable()) {
                            	if ((item != null) && (! empty)) {
                            		setText(item);
                            		//setGraphic(null);
                            	}
                            } else {
                            	
                            	
                            	
                            	
                            	
                            	
                            	setStyle
                            			("-fx-text-fill: #"+ 
                                        ConvertType.colorToHex(new Color(curRow.getColorTRed_N(),  curRow.getColorTGreen_N(),
                                        		curRow.getColorTBlue_N(), curRow.getColorTOpacity_N())) +"; " +
                                       "-fx-background-color: #"+ 
                                        ConvertType.colorToHex(new Color(curRow.getColorBRed_N(),  curRow.getColorBGreen_N(),
                                        		curRow.getColorBBlue_N(), curRow.getColorBOpacity_N())) +";"
                            			);
                            	
                            	
                            	
                            	//this.setTextFill(new Color(curRow.getColorTRed_A(),  curRow.getColorTGreen_A(),
                            	//		                   curRow.getColorTBlue_A(), curRow.getColorTOpacity_A()));
                                
                                setText(item);
                                //setGraphic(null);
                            }
                        } catch (IndexOutOfBoundsException e) {
                        	//
                        }
                    }
                };
            }
        });
    	
    	// прослушка выбора текущей строки
    	tableView_Connections.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
    		_previousSelectedIndex = _lastSelectedIndex;
            _lastSelectedIndex = tableView_Connections.getSelectionModel().getSelectedIndex();
            
    		if ((oldSelection != null) && (_previousSelectedIndex != -1)) {
                
    			
    			
    			
    			/*
    			TableViewSelectionModel selectionModel = tableView_Connections.getSelectionModel();
    	        ObservableList selectedCells = selectionModel.getSelectedCells();
    	        TablePosition tablePosition = (TablePosition) selectedCells.get(0);
   	            tablePosition.getTableColumn().setStyle(
    			
    			
    			////((TableCell)tableView_Connections.getSelectionModel().getSelectedCells().get(0)).setStyle(
    			//tableView_Connections.getColumns().get(_previousSelectedIndex).setStyle(
    					"-fx-text-fill: #"+ 
                                ConvertType.colorToHex(new Color(oldSelection.getColorTRed_N(),  oldSelection.getColorTGreen_N(),
                                		oldSelection.getColorTBlue_N(), oldSelection.getColorTOpacity_N())) +"; " +
                               "-fx-background-color: #"+ 
                                ConvertType.colorToHex(new Color(oldSelection.getColorBRed_N(),  oldSelection.getColorBGreen_N(),
                                		oldSelection.getColorBBlue_N(), oldSelection.getColorBOpacity_N())) +";"
    					);
    			
    			*/
    			// - как получить строку по индексу
    			// - как из выбранной строки получить ячейку и поменять в ней стиль
    			

    			
    	    }
    		
    		if (newSelection != null) {
    	    

    			
    			
    			
    			
    			
    			/*
    			((TableCell)tableView_Connections.getColumns().get(0).getCellObservableValue(_lastSelectedIndex)).setStyle(
    					"-fx-text-fill: #"+ 
                                ConvertType.colorToHex(new Color(oldSelection.getColorTRed_A(),  oldSelection.getColorTGreen_A(),
                                		oldSelection.getColorTBlue_A(), oldSelection.getColorTOpacity_A())) +"; " +
                               "-fx-background-color: #"+ 
                                ConvertType.colorToHex(new Color(oldSelection.getColorBRed_A(),  oldSelection.getColorBGreen_A(),
                                		oldSelection.getColorBBlue_A(), oldSelection.getColorBOpacity_A())) +";"
    			);
    			*/
    			

    	    }
    	});
    	
    	// ContextMenu
    	menuitem_Add.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
    	menuitem_Edit.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_16.png")));
    	menuitem_Delete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	menuitem_Connect.setGraphic(new ImageView(new Image("file:resources/images/icon_Connect_16.png")));
    	menuitem_Disconnect.setGraphic(new ImageView(new Image("file:resources/images/icon_disconnect_16.png")));
    	
    	//======== buttons
    	button_Add.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
    	button_Edit.setGraphic(new ImageView(new Image("file:resources/images/icon_edit_16.png")));
    	button_Delete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	button_Connect.setGraphic(new ImageView(new Image("file:resources/images/icon_Connect_16.png")));
    	button_Disconnect.setGraphic(new ImageView(new Image("file:resources/images/icon_disconnect_16.png")));
    	button_Exit.setGraphic(new ImageView(new Image("file:resources/images/icon_close_16.png")));
    }

	/**
	 * Сохраняем текущее состояние фрейма и контролов
	 */
	private void saveState() {

        //-------- size and position
        prefs.putDouble("stageDBConnList_Width", dialogStage.getWidth());
        prefs.putDouble("stageDBConnList_Height",dialogStage.getHeight());
        prefs.putDouble("stageDBConnList_PosX",  dialogStage.getX());
        prefs.putDouble("stageDBConnList_PosY",  dialogStage.getY());

        //-------- sort
        if (tableView_Connections.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
			currentSortColumn = (TableColumn) tableView_Connections.getSortOrder().get(0);
            prefs.put("stageDBConnList_sortColumnId",currentSortColumn.getId());
            prefs.put("stageDBConnList_sortType",currentSortColumn.getSortType().toString());
        } else {
			prefs.remove("stageDBConnList_sortColumnId");
			prefs.remove("stageDBConnList_sortType");
		}
	}

	/**
     * Вызывается при нажатии на кнопке "Добавить"
     */
    @FXML
    private void handleButtonAdd() {
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/DBConnEdit_Layout.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Добавление нового подключения");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_Connect_16.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(DBConnEdit_Controller.class);
			dialogStage.setX(prefs.getDouble("stageDBConnEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageDBConnEdit_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			DBConnEdit_Controller controller = loader.getController();
	        controller.setParrentObj(params, this, 1, null, dialogStage);
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        //
	        tableView_Connections.refresh();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Вызывается при нажатии на кнопке "Редактировать"
     */
    @FXML
    private void handleButtonEdit() {
    	
    	if (tableView_Connections.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбрано соединение", "Выберите соединение для редактирования");
    		return;
    	}
    	
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/DBConnEdit_Layout.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Редактирование подключения");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_Connect_16.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(DBConnEdit_Controller.class);
			dialogStage.setX(prefs.getDouble("stageDBConnEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageDBConnEdit_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			DBConnEdit_Controller controller = loader.getController();
	        controller.setParrentObj(params, this, 2, tableView_Connections.getSelectionModel().getSelectedItem(), dialogStage);
	        
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        //
	        tableView_Connections.refresh();
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Вызывается при нажатии на кнопке "Удалить"
     */
    @FXML
    private void handleButtonDelete() {
    	
    	if (tableView_Connections.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбрано соединение", "Выберите соединение для удаления");
    		return;
    	}
    	
    	int row;
    	String connName = tableView_Connections.getSelectionModel().getSelectedItem().getConnName();
	
    	if (ShowAppMsg.showQuestion("CONFIRMATION", "Удаление соединения", 
    			                    "Удаление соединения '"+ connName +"'", "Удалить соединение ?")) {
    		row = tableView_Connections.getSelectionModel().getSelectedIndex();
    		//ShowAppMsg.showAlert("WARNING", "Test", "row index = "+row, "Yes");
    		
    		tableView_Connections.getItems().remove(row);
    		// save to disk
        	dbConnList.saveToFile();
    	}
    }
    
    /**
     * Вызывается при нажатии на кнопке "Подключиться".
     * Подключаемся и закрываемся
     */
    @FXML
    private void handleButtonConnect() {
    	DBConn_Parameters selectedConn = tableView_Connections.getSelectionModel().getSelectedItem();

        saveState();     // save state of size and position

    	if (selectedConn == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбрано соединение", "Выберите соединение.");
    		return;
    	}
    	
    	mainApp.dbConnect(new DBConn_Parameters(selectedConn));          // передаем не ссылку а значения

    	//-------- меняем дату последнего подключения и увеличиваем счетчик
    	selectedConn.setLastConn(LocalDate.now());
		selectedConn.setCounter(selectedConn.getCounter() + 1);
		
		// save to disk
    	dbConnList.saveToFile();
		
		//-------- exit
		// get a handle to the stage
        Stage stage = (Stage) button_Exit.getScene().getWindow();
        // close window
        stage.close();
    }
    
    /**
     * Викликається кнопкою або контекстним меню "Disconnect"
     * Закриваємо вибране з'єдняння
     */
    @FXML
    private void handleButtonDisconnect() {
    	DBConn_Parameters selectedConn = tableView_Connections.getSelectionModel().getSelectedItem();

        saveState();     // save state of size and position

    	if (selectedConn == null) {
    		ShowAppMsg.showAlert("WARNING", "Немає вибору", "Не обрано з'єдняння", "Оберіть з'єднання.");
    		return;
    	}
    	
    	// знаходимо та перевіряємо чи з'єдняння відкрите, та закриваємо
    	boolean isConnectOpen = false;
    	
    	for (DBConCur_Parameters con : params.getConnDB().conList) {
			if (con.param.getConnId() == selectedConn.getConnId()) {
				isConnectOpen = true;
				try {
					con.db.close();
				} catch (DataConnectionException e) {
					ShowAppMsg.showAlert("WARNING", "Помилка", e.getMsg(), "");
				}
			}
		}
    	if (! isConnectOpen) {
    		ShowAppMsg.showAlert("WARNING", "З'єднання", "З'єднання не відкрите", "");
    		return;
    	}
    }
    
    /**
     * Вызывается при нажатии на кнопке "Закрыть"
     */
    @FXML
    private void handleButtonExit() {

    	//-------- save state of size and position
    	saveState();
    	
    	//-------- close
        // get a handle to the stage
        Stage stage = (Stage) button_Exit.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
    
    /**
     * Редактирование записи в списке соединений. Если новая - добавляем, существующая - изменяем параметры.
     * @param
     */
    public void edit (int actionType, DBConn_Parameters newConnPar) {
    	DBConn_Parameters curConnPar;
    	
    	if (actionType == 1) {             // add
    		try {
            	dbConnList.add(newConnPar.getConnName(), newConnPar.getType(), newConnPar.getHost(), newConnPar.getPort(), 
            			       newConnPar.getName(), newConnPar.getLogin(), newConnPar.getPassword(), 
            			       newConnPar.getAutoConn(), null, 0, newConnPar.getColorEnable(), 
            			       newConnPar.getColorTRed_A(), newConnPar.getColorTGreen_A(), newConnPar.getColorTBlue_A(),newConnPar.getColorTOpacity_A(),
            			       newConnPar.getColorBRed_A(), newConnPar.getColorBGreen_A(), newConnPar.getColorBBlue_A(),newConnPar.getColorBOpacity_A(),
            			       newConnPar.getColorTRed_N(), newConnPar.getColorTGreen_N(), newConnPar.getColorTBlue_N(),newConnPar.getColorTOpacity_N(),
            			       newConnPar.getColorBRed_N(), newConnPar.getColorBGreen_N(), newConnPar.getColorBBlue_N(),newConnPar.getColorBOpacity_N());
    		} catch (KBase_DublicateConnIdEx e) {
    			ShowAppMsg.showAlert("WARNING", "Внимание", "Дублирование Id соединения", e.msg);
    		}
    	} else if (actionType == 2) {      // update
    		curConnPar = tableView_Connections.getSelectionModel().getSelectedItem();
    		
    		// проверяем, не поменялась ли как то текущая запись в таблице
    		if (curConnPar.getConnId() != newConnPar.getConnId()) {
    			ShowAppMsg.showAlert("WARNING", "Внимание", "Изменилась текущая запись в таблице.", "Изменение записи невозможно.");
    			return;
    		}
    		
    		// изменяем данные
    		curConnPar.setConnName (newConnPar.getConnName());
    		curConnPar.setType     (newConnPar.getType());
    		curConnPar.setHost     (newConnPar.getHost());
    		curConnPar.setPort     (newConnPar.getPort());
    		curConnPar.setName     (newConnPar.getName());
    		curConnPar.setLogin    (newConnPar.getLogin());
    		curConnPar.setPassword (newConnPar.getPassword());
    		curConnPar.setAutoConn (newConnPar.getAutoConn());
    		
    		curConnPar.setColorEnable  (newConnPar.getColorEnable());
    		
    		curConnPar.setColorTRed_A    (newConnPar.getColorTRed_A());
    		curConnPar.setColorTGreen_A  (newConnPar.getColorTGreen_A());
    		curConnPar.setColorTBlue_A   (newConnPar.getColorTBlue_A());
    		curConnPar.setColorTOpacity_A(newConnPar.getColorTOpacity_A());
    		
    		curConnPar.setColorBRed_A    (newConnPar.getColorBRed_A());
    		curConnPar.setColorBGreen_A  (newConnPar.getColorBGreen_A());
    		curConnPar.setColorBBlue_A   (newConnPar.getColorBBlue_A());
    		curConnPar.setColorBOpacity_A(newConnPar.getColorBOpacity_A());
    		
    		curConnPar.setColorTRed_N    (newConnPar.getColorTRed_N());
    		curConnPar.setColorTGreen_N  (newConnPar.getColorTGreen_N());
    		curConnPar.setColorTBlue_N   (newConnPar.getColorTBlue_N());
    		curConnPar.setColorTOpacity_N(newConnPar.getColorTOpacity_N());
    		
    		curConnPar.setColorBRed_N    (newConnPar.getColorBRed_N());
    		curConnPar.setColorBGreen_N  (newConnPar.getColorBGreen_N());
    		curConnPar.setColorBBlue_N   (newConnPar.getColorBBlue_N());
    		curConnPar.setColorBOpacity_N(newConnPar.getColorBOpacity_N());
    	}
    	
    	// меняем цвет для текущей строки
    	//tableView_Connections.getSelectionModel().getSelectedItem().setS     
    	
    	
    	
    	
    	
    	
    	// save to disk
    	dbConnList.saveToFile();
    }
}
