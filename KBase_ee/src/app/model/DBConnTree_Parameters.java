package app.model;

import app.exceptions.KBase_DublicateConnIdEx;
import app.lib.FileUtil;
import app.lib.KeyStorePrg;
import app.lib.LogFile;
import app.lib.ShowAppMsg;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Дерево підключень до БД.
 * Містить папки (з необмеженою вкладеністю) та підключення в корені.
 * Зберігається у файлі DBConnTree.xml. При першому запуску виконується
 * міграція зі старого файлу DBConnList.xml (плоский список).
 *
 * @author Ігор Макаревич
 */
public class DBConnTree_Parameters {
	public static final String FILE_NAME_NEW = "DBConnTree.xml";
	public static final String FILE_NAME_OLD = "DBConnList.xml";

	/** Коренева папка для ROOT (не зберігається, id = 0) */
	public static final int ROOT_FOLDER_ID = 0;

	Params params;

	/**
	 * Папки кореневого рівня
	 */
	public List<DBConnFolder> folders = new ArrayList<>();
	/**
	 * Підключення кореневого рівня (поза папками)
	 */
	public List<DBConn_Parameters> rootConnections = new ArrayList<>();

	private File file;

	/**
	 * Constructor.
	 */
	public DBConnTree_Parameters(Params params) {
		this.params = params;

		String filenameNew = FileUtil.getFullUserFileName(params, FILE_NAME_NEW);
		file = new File(filenameNew);
		if (!file.exists()) {
			if (!tryMigrateFromOldFile()) {
				try {
					File template = new File(FILE_NAME_NEW);
					if (template.exists()) {
						FileUtil.copyFile(template, file);
					} else {
						// порожнє дерево — буде створено при збереженні
					}
				} catch (IOException e) {
					e.printStackTrace();
					ShowAppMsg.showAlert(
							"ERROR",
							"Помилка",
							"Неможливо скопіювати файл " + file.getPath(),
							e.getMessage());
				}
			}
		}

		if (file.exists()) {
			loadFromFile();
		}
		// гарантуємо не-null списки після завантаження
		if (folders == null) {
			folders = new ArrayList<>();
		}
		if (rootConnections == null) {
			rootConnections = new ArrayList<>();
		}
		// для старих записів без опису — гарантуємо не-null
		for (DBConn_Parameters c : getAllConnections()) {
			if (c.getDescr() == null) {
				c.setDescr("");
			}
			if (c.getType() == null) {
				c.setType(DBConn_Parameters.TYPE_CONN_POSTGRES);
			}
		}
	}

	/**
	 * Спроба міграції зі старого плоского списку DBConnList.xml.
	 * @return true якщо міграція виконана
	 */
	private boolean tryMigrateFromOldFile() {
		String filenameOld = FileUtil.getFullUserFileName(params, FILE_NAME_OLD);
		File fileOldUser = new File(filenameOld);
		File fileOldTemplate = new File(FILE_NAME_OLD);
		File src = null;

		if (fileOldUser.exists()) {
			src = fileOldUser;
		} else if (fileOldTemplate.exists()) {
			src = fileOldTemplate;
		} else {
			return false;
		}

		try {
			JAXBContext context = JAXBContext.newInstance(DBConnList_Wrapper.class);
			Unmarshaller um = context.createUnmarshaller();
			DBConnList_Wrapper wrapper = (DBConnList_Wrapper) um.unmarshal(src);
			if (wrapper.getList() != null) {
				rootConnections.clear();
				folders.clear();
				for (DBConn_Parameters c : wrapper.getList()) {
					if (c.getDescr() == null) {
						c.setDescr("");
					}
					rootConnections.add(c);
				}
				// одразу зберігаємо у новому форматі
				saveToFile();
				ShowAppMsg.showAlert(
						"INFORMATION",
						"Міграція",
						"Виконано міграцію підключень",
						"Дані з " + src.getPath() + " перенесено у " + file.getPath());
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				LogFile.write("DBConnTree migration failed: " + e.getMessage());
			} catch (Exception le) {
				le.printStackTrace();
			}
			// не вважаємо фатальним — просто не мігрували
		}
		return false;
	}

	/**
	 * Усі підключення плоским списком (включаючи вкладені папки).
	 */
	public List<DBConn_Parameters> getAllConnections() {
		List<DBConn_Parameters> ret = new ArrayList<>();
		ret.addAll(rootConnections);
		for (DBConnFolder f : folders) {
			collectConnections(f, ret);
		}
		return ret;
	}

	private void collectConnections(DBConnFolder folder, List<DBConn_Parameters> out) {
		out.addAll(folder.getConnections());
		for (DBConnFolder sub : folder.getFolders()) {
			collectConnections(sub, out);
		}
	}

	/**
	 * Усі папки плоским списком (включаючи вкладені).
	 */
	public List<DBConnFolder> getAllFolders() {
		List<DBConnFolder> ret = new ArrayList<>();
		for (DBConnFolder f : folders) {
			collectFolders(f, ret);
		}
		return ret;
	}

	private void collectFolders(DBConnFolder folder, List<DBConnFolder> out) {
		out.add(folder);
		for (DBConnFolder sub : folder.getFolders()) {
			collectFolders(sub, out);
		}
	}

	/**
	 * Пошук підключення за connId.
	 */
	public DBConn_Parameters findConnectionById(int connId) {
		for (DBConn_Parameters c : rootConnections) {
			if (c.getConnId() == connId) {
				return c;
			}
		}
		for (DBConnFolder f : folders) {
			DBConn_Parameters r = findConnectionInFolder(f, connId);
			if (r != null) {
				return r;
			}
		}
		return null;
	}

	private DBConn_Parameters findConnectionInFolder(DBConnFolder folder, int connId) {
		for (DBConn_Parameters c : folder.getConnections()) {
			if (c.getConnId() == connId) {
				return c;
			}
		}
		for (DBConnFolder sub : folder.getFolders()) {
			DBConn_Parameters r = findConnectionInFolder(sub, connId);
			if (r != null) {
				return r;
			}
		}
		return null;
	}

	/**
	 * Пошук папки за folderId.
	 */
	public DBConnFolder findFolderById(int folderId) {
		if (folderId == ROOT_FOLDER_ID) {
			return null;
		}
		for (DBConnFolder f : folders) {
			DBConnFolder r = findFolderRecursive(f, folderId);
			if (r != null) {
				return r;
			}
		}
		return null;
	}

	private DBConnFolder findFolderRecursive(DBConnFolder folder, int folderId) {
		if (folder.getFolderId() == folderId) {
			return folder;
		}
		for (DBConnFolder sub : folder.getFolders()) {
			DBConnFolder r = findFolderRecursive(sub, folderId);
			if (r != null) {
				return r;
			}
		}
		return null;
	}

	/**
	 * Батьківська папка підключення (null = корінь).
	 */
	public DBConnFolder findParentFolderOfConnection(int connId) {
		for (DBConn_Parameters c : rootConnections) {
			if (c.getConnId() == connId) {
				return null;
			}
		}
		for (DBConnFolder f : folders) {
			DBConnFolder r = findParentOfConnectionRecursive(f, connId);
			if (r != null) {
				return r;
			}
			// перевіряємо чи прямо в цій папці
			for (DBConn_Parameters c : f.getConnections()) {
				if (c.getConnId() == connId) {
					return f;
				}
			}
		}
		return null;
	}

	private DBConnFolder findParentOfConnectionRecursive(DBConnFolder folder, int connId) {
		for (DBConnFolder sub : folder.getFolders()) {
			for (DBConn_Parameters c : sub.getConnections()) {
				if (c.getConnId() == connId) {
					return sub;
				}
			}
			DBConnFolder r = findParentOfConnectionRecursive(sub, connId);
			if (r != null) {
				return r;
			}
		}
		return null;
	}

	/**
	 * Батьківська папка папки (null = корінь).
	 */
	public DBConnFolder findParentFolderOfFolder(int folderId) {
		for (DBConnFolder f : folders) {
			if (f.getFolderId() == folderId) {
				return null;
			}
		}
		for (DBConnFolder f : folders) {
			DBConnFolder r = findParentOfFolderRecursive(f, folderId);
			if (r != null) {
				return r;
			}
		}
		return null;
	}

	private DBConnFolder findParentOfFolderRecursive(DBConnFolder folder, int folderId) {
		for (DBConnFolder sub : folder.getFolders()) {
			if (sub.getFolderId() == folderId) {
				return folder;
			}
			DBConnFolder r = findParentOfFolderRecursive(sub, folderId);
			if (r != null) {
				return r;
			}
		}
		return null;
	}

	/**
	 * Перевірка чи id вже зайнятий (папками або підключеннями).
	 */
	public boolean isIdExists(int id) {
		if (findConnectionById(id) != null) {
			return true;
		}
		if (findFolderById(id) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Генерація унікального id.
	 */
	public int generateUniqueId() {
		int id = LocalDateTime.now().hashCode();
		// у рідкісному випадку колізії — зсуваємо
		while (isIdExists(id) || id == ROOT_FOLDER_ID) {
			id++;
		}
		return id;
	}

	/**
	 * Додати папку.
	 * @param parentFolderId 0 — корінь
	 */
	public DBConnFolder addFolder(int parentFolderId, String folderName, String descr) throws KBase_DublicateConnIdEx {
		int folderId = generateUniqueId();
		DBConnFolder nf = new DBConnFolder(folderId, folderName, descr == null ? "" : descr);
		if (parentFolderId == ROOT_FOLDER_ID) {
			folders.add(nf);
		} else {
			DBConnFolder parent = findFolderById(parentFolderId);
			if (parent == null) {
				folders.add(nf);
			} else {
				parent.getFolders().add(nf);
			}
		}
		return nf;
	}

	/**
	 * Додати підключення у вказану папку (0 — корінь).
	 */
	public void add(int parentFolderId, String connName, String descr, String type, String host, String port, String name,
			String login, KeyStorePrg.EncBlob password,
			boolean autoConn, LocalDate lastConn, int counter, boolean colorEnable,
			double colorTRed_A, double colorTGreen_A, double colorTBlue_A, double colorTOpacity_A,
			double colorBRed_A, double colorBGreen_A, double colorBBlue_A, double colorBOpacity_A,
			double colorTRed_N, double colorTGreen_N, double colorTBlue_N, double colorTOpacity_N,
			double colorBRed_N, double colorBGreen_N, double colorBBlue_N, double colorBOpacity_N)
			throws KBase_DublicateConnIdEx {
		int connId = generateUniqueId();

		if (isIdExists(connId)) {
			throw new KBase_DublicateConnIdEx(connId, connName, null);
		}

		DBConn_Parameters np = new DBConn_Parameters(connId, connName, descr == null ? "" : descr,
				type, host, port, name, login, password,
				autoConn, lastConn, counter, colorEnable,
				colorTRed_A, colorTGreen_A, colorTBlue_A, colorTOpacity_A,
				colorBRed_A, colorBGreen_A, colorBBlue_A, colorBOpacity_A,
				colorTRed_N, colorTGreen_N, colorTBlue_N, colorTOpacity_N,
				colorBRed_N, colorBGreen_N, colorBBlue_N, colorBOpacity_N);

		if (parentFolderId == ROOT_FOLDER_ID) {
			rootConnections.add(np);
		} else {
			DBConnFolder parent = findFolderById(parentFolderId);
			if (parent == null) {
				rootConnections.add(np);
			} else {
				parent.getConnections().add(np);
			}
		}
	}

	/**
	 * Видалити підключення за connId.
	 */
	public boolean deleteConnection(int connId) {
		for (int i = 0; i < rootConnections.size(); i++) {
			if (rootConnections.get(i).getConnId() == connId) {
				rootConnections.remove(i);
				return true;
			}
		}
		for (DBConnFolder f : folders) {
			if (deleteConnectionRecursive(f, connId)) {
				return true;
			}
		}
		return false;
	}

	private boolean deleteConnectionRecursive(DBConnFolder folder, int connId) {
		List<DBConn_Parameters> list = folder.getConnections();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getConnId() == connId) {
				list.remove(i);
				return true;
			}
		}
		for (DBConnFolder sub : folder.getFolders()) {
			if (deleteConnectionRecursive(sub, connId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Видалити папку разом з усім вмістом.
	 */
	public boolean deleteFolder(int folderId) {
		for (int i = 0; i < folders.size(); i++) {
			if (folders.get(i).getFolderId() == folderId) {
				folders.remove(i);
				return true;
			}
		}
		for (DBConnFolder f : folders) {
			if (deleteFolderRecursive(f, folderId)) {
				return true;
			}
		}
		return false;
	}

	private boolean deleteFolderRecursive(DBConnFolder folder, int folderId) {
		List<DBConnFolder> list = folder.getFolders();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getFolderId() == folderId) {
				list.remove(i);
				return true;
			}
		}
		for (DBConnFolder sub : list) {
			if (deleteFolderRecursive(sub, folderId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Перемістити підключення в іншу папку.
	 */
	public boolean moveConnection(int connId, int newParentFolderId) {
		DBConn_Parameters c = findConnectionById(connId);
		if (c == null) {
			return false;
		}
		if (!deleteConnection(connId)) {
			return false;
		}
		if (newParentFolderId == ROOT_FOLDER_ID) {
			rootConnections.add(c);
		} else {
			DBConnFolder parent = findFolderById(newParentFolderId);
			if (parent == null) {
				rootConnections.add(c);
			} else {
				parent.getConnections().add(c);
			}
		}
		return true;
	}

	/**
	 * Перемістити папку в іншу папку (захист від циклів).
	 */
	public boolean moveFolder(int folderId, int newParentFolderId) {
		if (folderId == newParentFolderId) {
			return false;
		}
		DBConnFolder moving = findFolderById(folderId);
		if (moving == null) {
			return false;
		}
		// забороняємо переміщення в себе або у свого нащадка
		if (newParentFolderId != ROOT_FOLDER_ID) {
			DBConnFolder np = findFolderById(newParentFolderId);
			if (np == null) {
				return false;
			}
			if (isDescendant(moving, newParentFolderId)) {
				return false;
			}
		}
		if (!deleteFolder(folderId)) {
			return false;
		}
		if (newParentFolderId == ROOT_FOLDER_ID) {
			folders.add(moving);
		} else {
			DBConnFolder parent = findFolderById(newParentFolderId);
			if (parent == null) {
				folders.add(moving);
			} else {
				parent.getFolders().add(moving);
			}
		}
		return true;
	}

	private boolean isDescendant(DBConnFolder folder, int folderId) {
		for (DBConnFolder sub : folder.getFolders()) {
			if (sub.getFolderId() == folderId) {
				return true;
			}
			if (isDescendant(sub, folderId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Копіювати підключення в іншу папку.
	 * Копія отримує новий Id, суфікс " (копія)" в назві, скинуті лічильник і дату.
	 * @return копія або null (якщо джерело не знайдено)
	 */
	public DBConn_Parameters copyConnection(int connId, int targetFolderId) {
		DBConn_Parameters src = findConnectionById(connId);
		if (src == null) {
			return null;
		}
		DBConn_Parameters np = new DBConn_Parameters(src);
		np.setConnId(generateUniqueId());
		np.setConnName(src.getConnName() + " (копія)");
		np.setLastConn(null);
		np.setCounter(0);
		if (targetFolderId == ROOT_FOLDER_ID) {
			rootConnections.add(np);
		} else {
			DBConnFolder parent = findFolderById(targetFolderId);
			if (parent == null) {
				rootConnections.add(np);
			} else {
				parent.getConnections().add(np);
			}
		}
		return np;
	}

	/**
	 * Копіювати папку з усім вмістом в іншу папку.
	 * Усі вкладені папки і підключення отримують нові Id.
	 * @return копія або null (якщо джерело/ціль не знайдено або ціль усередині самої папки)
	 */
	public DBConnFolder copyFolder(int folderId, int targetFolderId) {
		DBConnFolder src = findFolderById(folderId);
		if (src == null) {
			return null;
		}
		if (targetFolderId != ROOT_FOLDER_ID) {
			if (findFolderById(targetFolderId) == null) {
				return null;
			}
			if (targetFolderId == folderId || isDescendant(src, targetFolderId)) {
				return null;
			}
		}
		Set<Integer> reserved = new HashSet<>();
		DBConnFolder np = deepCopyFolder(src, true, reserved);
		if (targetFolderId == ROOT_FOLDER_ID) {
			folders.add(np);
		} else {
			findFolderById(targetFolderId).getFolders().add(np);
		}
		return np;
	}

	/**
	 * Глибока копія папки з новими Id (суфікс " (копія)" лише на верхньому рівні).
	 */
	private DBConnFolder deepCopyFolder(DBConnFolder src, boolean withSuffix, Set<Integer> reserved) {
		DBConnFolder np = new DBConnFolder(newId(reserved),
				withSuffix ? src.getFolderName() + " (копія)" : src.getFolderName(),
				src.getDescr());
		for (DBConn_Parameters c : src.getConnections()) {
			DBConn_Parameters cp = new DBConn_Parameters(c);
			cp.setConnId(newId(reserved));
			cp.setLastConn(null);
			cp.setCounter(0);
			np.getConnections().add(cp);
		}
		for (DBConnFolder sub : src.getFolders()) {
			np.getFolders().add(deepCopyFolder(sub, false, reserved));
		}
		return np;
	}

	/**
	 * Новий Id з урахуванням вже зарезервованих у поточній операції.
	 */
	private int newId(Set<Integer> reserved) {
		int id = generateUniqueId();
		while (reserved.contains(id)) {
			id++;
		}
		reserved.add(id);
		return id;
	}

	/**
	 * Завантажує дерево з файлу.
	 * Поточні дані будуть замінені.
	 */
	public void loadFromFile() {
		try {
			JAXBContext context = JAXBContext.newInstance(DBConnTree_Wrapper.class);
			Unmarshaller um = context.createUnmarshaller();

			DBConnTree_Wrapper wrapper = (DBConnTree_Wrapper) um.unmarshal(file);

			folders.clear();
			rootConnections.clear();
			if (wrapper.getFolders() != null) {
				folders.addAll(wrapper.getFolders());
			}
			if (wrapper.getConnections() != null) {
				rootConnections.addAll(wrapper.getConnections());
			}
		} catch (Exception e) {
			e.printStackTrace();
			ShowAppMsg.showAlert(
					"ERROR",
					"Помилка",
					"Неможливо завантажити дані",
					"Неможливо завантажити дані з файлу " + file.getPath());
		}
	}

	/**
	 * Зберігає дерево підключень у файл.
	 */
	public void saveToFile() {
		try {
			JAXBContext context = JAXBContext.newInstance(DBConnTree_Wrapper.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			DBConnTree_Wrapper wrapper = new DBConnTree_Wrapper();
			wrapper.setFolders(folders);
			wrapper.setConnections(rootConnections);

			m.marshal(wrapper, file);
		} catch (Exception e) {
			e.printStackTrace();
			ShowAppMsg.showAlert(
					"ERROR",
					"Помилка",
					"Неможливо зберегти дані",
					"Неможливо зберегти дані у файл " + file.getPath());
		}
	}
}
