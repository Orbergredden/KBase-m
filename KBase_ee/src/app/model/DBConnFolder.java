package app.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Папка в дереві підключень до БД.
 * Може містити вкладені папки (необмежена вкладеність) та підключення.
 *
 * @author Ігор Макаревич
 */
public class DBConnFolder {
	/**
	 * Унікальний ідентифікатор папки (в межах дерева)
	 */
	private final IntegerProperty folderId;
	/**
	 * Назва папки
	 */
	private final StringProperty folderName;
	/**
	 * Опис папки (довільний текст)
	 */
	private final StringProperty descr;

	/**
	 * Вкладені папки
	 */
	private List<DBConnFolder> folders = new ArrayList<>();
	/**
	 * Підключення в цій папці
	 */
	private List<DBConn_Parameters> connections = new ArrayList<>();

	/**
	 * Конструктор за замовчуванням (для JAXB).
	 */
	public DBConnFolder() {
		this(0, "", "");
	}

	/**
	 * Конструктор.
	 */
	public DBConnFolder(int folderId, String folderName, String descr) {
		this.folderId = new SimpleIntegerProperty(folderId);
		this.folderName = new SimpleStringProperty(folderName);
		this.descr = new SimpleStringProperty(descr);
	}

	/**
	 * Конструктор копіювання (поверхневий для списків).
	 */
	public DBConnFolder(DBConnFolder src) {
		this.folderId = new SimpleIntegerProperty(src.getFolderId());
		this.folderName = new SimpleStringProperty(src.getFolderName());
		this.descr = new SimpleStringProperty(src.getDescr());
		this.folders = src.getFolders();
		this.connections = src.getConnections();
	}

	// folderId -- g,s,p
	public int getFolderId() {
		return folderId.get();
	}
	public void setFolderId(int folderId) {
		this.folderId.set(folderId);
	}
	public IntegerProperty folderIdProperty() {
		return folderId;
	}

	// folderName -- g,s,p
	public String getFolderName() {
		return folderName.get();
	}
	public void setFolderName(String folderName) {
		this.folderName.set(folderName);
	}
	public StringProperty folderNameProperty() {
		return folderName;
	}

	// descr -- g,s,p
	public String getDescr() {
		return descr.get();
	}
	public void setDescr(String descr) {
		this.descr.set(descr);
	}
	public StringProperty descrProperty() {
		return descr;
	}

	// folders -- g,s (для JAXB)
	@XmlElement(name = "DBConnFolder")
	public List<DBConnFolder> getFolders() {
		if (folders == null) {
			folders = new ArrayList<>();
		}
		return folders;
	}
	public void setFolders(List<DBConnFolder> folders) {
		this.folders = folders;
	}

	// connections -- g,s (для JAXB)
	@XmlElement(name = "DBConn_Parameters")
	public List<DBConn_Parameters> getConnections() {
		if (connections == null) {
			connections = new ArrayList<>();
		}
		return connections;
	}
	public void setConnections(List<DBConn_Parameters> connections) {
		this.connections = connections;
	}

	@Override
	public String toString() {
		return getFolderName();
	}
}
