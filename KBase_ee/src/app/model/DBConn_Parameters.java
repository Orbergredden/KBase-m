package app.model;

import app.lib.KeyStorePrg;
import app.util.LocalDateAdapter;

import java.time.LocalDate;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Класс содержит информацию о настройках одного подключения к БД.
 * 
 * @author Игорь Макаревич
 */
public class DBConn_Parameters {
	/**
	 * Тип з'єднання, для type
	 */
	public static final String TYPE_CONN_POSTGRES = "postgresql";
	public static final String TYPE_CONN_SQLITE   = "sqlite";
	
	/**
	 * Уникальное значение в списке соединений
	 */
	private final IntegerProperty connId;
	/**
	 * Название соединения в списке соединений
	 */
	private final StringProperty connName;
	// 
	private final StringProperty type;
	private final StringProperty host;
	private final StringProperty port;
	private final StringProperty name;
	private final StringProperty login;
	private KeyStorePrg.EncBlob password;
	private final BooleanProperty autoConn;
	/**
	 * Дата последнего соединения
	 */
	private final ObjectProperty<LocalDate> lastConn;
	/**
	 * Счетчик кол-ва соединений всего
	 */
	private final IntegerProperty counter;
	
	// colors
	private final BooleanProperty colorEnable;
	
	private final DoubleProperty colorTRed_A;
	private final DoubleProperty colorTGreen_A;
	private final DoubleProperty colorTBlue_A;
	private final DoubleProperty colorTOpacity_A;
	private final DoubleProperty colorBRed_A;
	private final DoubleProperty colorBGreen_A;
	private final DoubleProperty colorBBlue_A;
	private final DoubleProperty colorBOpacity_A;
	
	private final DoubleProperty colorTRed_N;
	private final DoubleProperty colorTGreen_N;
	private final DoubleProperty colorTBlue_N;
	private final DoubleProperty colorTOpacity_N;
	private final DoubleProperty colorBRed_N;
	private final DoubleProperty colorBGreen_N;
	private final DoubleProperty colorBBlue_N;
	private final DoubleProperty colorBOpacity_N;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public DBConn_Parameters() {
		this(0, null, null, null, null, null, null, null, false,
			 null, 0,
			 false, 0.0,0.0,0.0,0.0, 0.0,0.0,0.0,0.0, 0.0,0.0,0.0,0.0, 0.0,0.0,0.0,0.0);
	}
	
	/**
	 * Конструктор
	 */
	public DBConn_Parameters(int connId, String connName,
			                 String type, String host, String port, String name, String login, KeyStorePrg.EncBlob password,
			                 boolean autoConn, LocalDate lastConn, int counter, boolean colorEnable,
			                 double colorTRed_A, double colorTGreen_A, double colorTBlue_A, double colorTOpacity_A,
			                 double colorBRed_A, double colorBGreen_A, double colorBBlue_A, double colorBOpacity_A,
			                 double colorTRed_N, double colorTGreen_N, double colorTBlue_N, double colorTOpacity_N,
			                 double colorBRed_N, double colorBGreen_N, double colorBBlue_N, double colorBOpacity_N) {
		this.connId   = new SimpleIntegerProperty(connId);
		this.connName = new SimpleStringProperty(connName);
		this.type     = new SimpleStringProperty(type);
		this.host     = new SimpleStringProperty(host);
		this.port     = new SimpleStringProperty(port);
		this.name     = new SimpleStringProperty(name);
		this.login    = new SimpleStringProperty(login);
		this.password = password;
		this.autoConn = new SimpleBooleanProperty(autoConn);
		
		this.lastConn = new SimpleObjectProperty<LocalDate>(lastConn);
		this.counter  = new SimpleIntegerProperty(counter);
		
		this.colorEnable   = new SimpleBooleanProperty(colorEnable);
		
		this.colorTRed_A     = new SimpleDoubleProperty(colorTRed_A);
		this.colorTGreen_A   = new SimpleDoubleProperty(colorTGreen_A);
		this.colorTBlue_A    = new SimpleDoubleProperty(colorTBlue_A);
		this.colorTOpacity_A = new SimpleDoubleProperty(colorTOpacity_A);
		
		this.colorBRed_A     = new SimpleDoubleProperty(colorBRed_A);
		this.colorBGreen_A   = new SimpleDoubleProperty(colorBGreen_A);
		this.colorBBlue_A    = new SimpleDoubleProperty(colorBBlue_A);
		this.colorBOpacity_A = new SimpleDoubleProperty(colorBOpacity_A);
		
		this.colorTRed_N     = new SimpleDoubleProperty(colorTRed_N);
		this.colorTGreen_N   = new SimpleDoubleProperty(colorTGreen_N);
		this.colorTBlue_N    = new SimpleDoubleProperty(colorTBlue_N);
		this.colorTOpacity_N = new SimpleDoubleProperty(colorTOpacity_N);
		
		this.colorBRed_N     = new SimpleDoubleProperty(colorBRed_N);
		this.colorBGreen_N   = new SimpleDoubleProperty(colorBGreen_N);
		this.colorBBlue_N    = new SimpleDoubleProperty(colorBBlue_N);
		this.colorBOpacity_N = new SimpleDoubleProperty(colorBOpacity_N);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public DBConn_Parameters(DBConn_Parameters connPar) {
		this.connId   = new SimpleIntegerProperty(connPar.getConnId());
		this.connName = new SimpleStringProperty(connPar.getConnName());
		this.type     = new SimpleStringProperty(connPar.getType());
		this.host     = new SimpleStringProperty(connPar.getHost());
		this.port     = new SimpleStringProperty(connPar.getPort());
		this.name     = new SimpleStringProperty(connPar.getName());
		this.login    = new SimpleStringProperty(connPar.getLogin());
		this.password = connPar.getPassword();
		this.autoConn = new SimpleBooleanProperty(connPar.getAutoConn());
		
		this.lastConn = new SimpleObjectProperty<LocalDate>(connPar.getLastConn());
		this.counter  = new SimpleIntegerProperty(connPar.getCounter());
		
		this.colorEnable   = new SimpleBooleanProperty(connPar.getColorEnable());
		
		this.colorTRed_A     = new SimpleDoubleProperty(connPar.getColorTRed_A());
		this.colorTGreen_A   = new SimpleDoubleProperty(connPar.getColorTGreen_A());
		this.colorTBlue_A    = new SimpleDoubleProperty(connPar.getColorTBlue_A());
		this.colorTOpacity_A = new SimpleDoubleProperty(connPar.getColorTOpacity_A());
		
		this.colorBRed_A     = new SimpleDoubleProperty(connPar.getColorBRed_A());
		this.colorBGreen_A   = new SimpleDoubleProperty(connPar.getColorBGreen_A());
		this.colorBBlue_A    = new SimpleDoubleProperty(connPar.getColorBBlue_A());
		this.colorBOpacity_A = new SimpleDoubleProperty(connPar.getColorBOpacity_A());
		
		this.colorTRed_N     = new SimpleDoubleProperty(connPar.getColorTRed_N());
		this.colorTGreen_N   = new SimpleDoubleProperty(connPar.getColorTGreen_N());
		this.colorTBlue_N    = new SimpleDoubleProperty(connPar.getColorTBlue_N());
		this.colorTOpacity_N = new SimpleDoubleProperty(connPar.getColorTOpacity_N());
		
		this.colorBRed_N     = new SimpleDoubleProperty(connPar.getColorBRed_N());
		this.colorBGreen_N   = new SimpleDoubleProperty(connPar.getColorBGreen_N());
		this.colorBBlue_N    = new SimpleDoubleProperty(connPar.getColorBBlue_N());
		this.colorBOpacity_N = new SimpleDoubleProperty(connPar.getColorBOpacity_N());
	}
	
	// connId  -- g,s,p
	public int getConnId() {
        return connId.get();
    }
    public void setConnId(int connId) {
        this.connId.set(connId);
    }
    public IntegerProperty connIdProperty() {
        return connId;
    }
	
	// connName -- g,s,p 
	public String getConnName() {
        return connName.get();
    }
    public void setConnName(String connName) {
        this.connName.set(connName);
    }
    public StringProperty connNameProperty() {
        return connName;
    }
    
    // type -- g,s,p
    public String getType() {
        return type.get();
    }
    public void setType(String type) {
        this.type.set(type);
    }
    public StringProperty typeProperty() {
        return type;
    }
    
    // host -- g,s,p
    public String getHost() {
        return host.get();
    }
    public void setHost(String host) {
        this.host.set(host);
    }
    public StringProperty hostProperty() {
        return host;
    }
    
    // port -- g,s,p
    public String getPort() {
        return port.get();
    }
    public void setPort(String port) {
        this.port.set(port);
    }
    public StringProperty portProperty() {
        return port;
    }
    
    // name -- g,s,p
    public String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public StringProperty nameProperty() {
        return name;
    }
    
    // login -- g,s,p
    public String getLogin() {
        return login.get();
    }
    public void setLogin(String login) {
        this.login.set(login);
    }
    public StringProperty loginProperty() {
        return login;
    }
    
    // password -- g,s
    public KeyStorePrg.EncBlob getPassword() {
        return password;
    }
    public void setPassword(KeyStorePrg.EncBlob password) {
        this.password = password;
    }
    
    // autoConn -- g,s,p
    public boolean getAutoConn() {
        return autoConn.get();
    }
    public void setAutoConn(boolean autoConn) {
        this.autoConn.set(autoConn);
    }
    public BooleanProperty autoConnProperty() {
        return autoConn;
    }
    
    // lastConn -- g,s,p
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getLastConn() {
        return lastConn.get();
    }
    public void setLastConn(LocalDate lastConn) {
        this.lastConn.set(lastConn);
    }
    public ObjectProperty<LocalDate> lastConnProperty() {
        return lastConn;
    }
    
    // counter -- g,s,p
    public int getCounter() {
        return counter.get();
    }
    public void setCounter(int counter) {
        this.counter.set(counter);
    }
    public IntegerProperty counterCodeProperty() {
        return counter;
    }
    
    // colorEnable -- g,s,p
    public boolean getColorEnable() {
        return colorEnable.get();
    }
    public void setColorEnable(boolean colorEnable) {
        this.colorEnable.set(colorEnable);
    }
    public BooleanProperty colorEnableProperty() {
        return colorEnable;
    }
    
    // colorTRed_A -- g,s,p
    public double getColorTRed_A() {
        return colorTRed_A.get();
    }
    public void setColorTRed_A(double colorTRed_A) {
        this.colorTRed_A.set(colorTRed_A);
    }
    public DoubleProperty colorTRedProperty_A() {
        return colorTRed_A;
    }
    
    // colorTGreen_A -- g,s,p
    public double getColorTGreen_A() {
        return colorTGreen_A.get();
    }
    public void setColorTGreen_A(double colorTGreen_A) {
        this.colorTGreen_A.set(colorTGreen_A);
    }
    public DoubleProperty colorTGreenProperty_A() {
        return colorTGreen_A;
    }
    
    // colorTBlue_A -- g,s,p
    public double getColorTBlue_A() {
        return colorTBlue_A.get();
    }
    public void setColorTBlue_A(double colorTBlue_A) {
        this.colorTBlue_A.set(colorTBlue_A);
    }
    public DoubleProperty colorTBlueProperty_A() {
        return colorTBlue_A;
    }
    
    // colorTOpacity_A -- g,s,p
    public double getColorTOpacity_A() {
        return colorTOpacity_A.get();
    }
    public void setColorTOpacity_A(double colorTOpacity_A) {
        this.colorTOpacity_A.set(colorTOpacity_A);
    }
    public DoubleProperty colorTOpacityProperty_A() {
        return colorTOpacity_A;
    }
    
    // colorBRed_A -- g,s,p
    public double getColorBRed_A() {
        return colorBRed_A.get();
    }
    public void setColorBRed_A(double colorBRed_A) {
        this.colorBRed_A.set(colorBRed_A);
    }
    public DoubleProperty colorBRedProperty_A() {
        return colorBRed_A;
    }
    
    // colorBGreen_A -- g,s,p
    public double getColorBGreen_A() {
        return colorBGreen_A.get();
    }
    public void setColorBGreen_A(double colorBGreen_A) {
        this.colorBGreen_A.set(colorBGreen_A);
    }
    public DoubleProperty colorBGreenProperty_A() {
        return colorBGreen_A;
    }
    
    // colorBBlue_A -- g,s,p
    public double getColorBBlue_A() {
        return colorBBlue_A.get();
    }
    public void setColorBBlue_A(double colorBBlue_A) {
        this.colorBBlue_A.set(colorBBlue_A);
    }
    public DoubleProperty colorBBlueProperty_A() {
        return colorBBlue_A;
    }
    
    // colorBOpacity_A -- g,s,p
    public double getColorBOpacity_A() {
        return colorBOpacity_A.get();
    }
    public void setColorBOpacity_A(double colorBOpacity_A) {
        this.colorBOpacity_A.set(colorBOpacity_A);
    }
    public DoubleProperty colorBOpacityProperty_A() {
        return colorBOpacity_A;
    }
    
    // colorTRed_N -- g,s,p
    public double getColorTRed_N() {
        return colorTRed_N.get();
    }
    public void setColorTRed_N(double colorTRed_N) {
        this.colorTRed_N.set(colorTRed_N);
    }
    public DoubleProperty colorTRedProperty_N() {
        return colorTRed_N;
    }
    
    // colorTGreen_N -- g,s,p
    public double getColorTGreen_N() {
        return colorTGreen_N.get();
    }
    public void setColorTGreen_N(double colorTGreen_N) {
        this.colorTGreen_N.set(colorTGreen_N);
    }
    public DoubleProperty colorTGreenProperty_N() {
        return colorTGreen_N;
    }
    
    // colorTBlue_N -- g,s,p
    public double getColorTBlue_N() {
        return colorTBlue_N.get();
    }
    public void setColorTBlue_N(double colorTBlue_N) {
        this.colorTBlue_N.set(colorTBlue_N);
    }
    public DoubleProperty colorTBlueProperty_N() {
        return colorTBlue_N;
    }
    
    // colorTOpacity -- g,s,p
    public double getColorTOpacity_N() {
        return colorTOpacity_N.get();
    }
    public void setColorTOpacity_N(double colorTOpacity_N) {
        this.colorTOpacity_N.set(colorTOpacity_N);
    }
    public DoubleProperty colorTOpacityProperty_N() {
        return colorTOpacity_N;
    }
    
    // colorBRed_N -- g,s,p
    public double getColorBRed_N() {
        return colorBRed_N.get();
    }
    public void setColorBRed_N(double colorBRed_N) {
        this.colorBRed_N.set(colorBRed_N);
    }
    public DoubleProperty colorBRedProperty_N() {
        return colorBRed_N;
    }
    
    // colorBGreen_N -- g,s,p
    public double getColorBGreen_N() {
        return colorBGreen_N.get();
    }
    public void setColorBGreen_N(double colorBGreen_N) {
        this.colorBGreen_N.set(colorBGreen_N);
    }
    public DoubleProperty colorBGreenProperty_N() {
        return colorBGreen_N;
    }
    
    // colorBBlue_N -- g,s,p
    public double getColorBBlue_N() {
        return colorBBlue_N.get();
    }
    public void setColorBBlue_N(double colorBBlue_N) {
        this.colorBBlue_N.set(colorBBlue_N);
    }
    public DoubleProperty colorBBlueProperty_N() {
        return colorBBlue_N;
    }
    
    // colorBOpacity_N -- g,s,p
    public double getColorBOpacity_N() {
        return colorBOpacity_N.get();
    }
    public void setColorBOpacity_N(double colorBOpacity_N) {
        this.colorBOpacity_N.set(colorBOpacity_N);
    }
    public DoubleProperty colorBOpacityProperty_N() {
        return colorBOpacity_N;
    }
}
