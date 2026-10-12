package app.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Допоміжний клас для обгортання дерева підключень.
 * Використовується для збереження дерева у XML (DBConnTree.xml).
 *
 * @author Igor Makarevich
 */
@XmlRootElement(name = "DBConnTree_Parameters")
public class DBConnTree_Wrapper {
	private List<DBConnFolder> folders = new ArrayList<>();
	private List<DBConn_Parameters> connections = new ArrayList<>();

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
}
