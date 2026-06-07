
package app.lib;

import app.exceptions.DataConnectionException;
import app.exceptions.DataQueryException;
import app.exceptions.KBase_HtmlCompileEx;
import app.model.ConfigMainList;
import app.model.DBConCur_Parameters;
import app.model.Params;
import app.model.business.DocumentItem;
import app.model.business.InfoHeaderItem;
import app.model.business.Info_FileItem;
import app.model.business.Info_ImageItem;
import app.model.business.Info_TextItem;
import app.model.business.SectionItem;
import app.model.business.template.TemplateThemeItem;
import app.model.business.template.TemplateItem;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс содержит инструментарий по компиляции документов в HTML из инфо блоков с использованием шаблонов.
 * @author IMakarevich
 */
public class HtmlCompile {
	// tag delimiters
	static final String TAG_DELIMITER_LEFT  = "{[";    // 
	static final String TAG_DELIMITER_RIGHT = "]}";    //
	
	Params params;
	
	/**
     * Ковертор даты/времени
     */
    private DateConv dateConv;
    
	/**
	 * Настройки программы
	 */
	ConfigMainList config;
	/**
	 * Конектор к БД
	 */
	DBConCur_Parameters conn;
	/**
	 * Id раздела документа
	 */
	long sectionId;
	
	/**
	 * Ассоциативный массив тегов Имя-Значение в головному шаблоні
	 */
	Map<String,String> mapTags = new HashMap<String,String>();
	/**
	 * Асоціативний масив назв недер-блоків з шаблонів інфоблоків
	 */
	Map<String,String> mapHeadName = new HashMap<String,String>();
	/**
	 * Скомилированный html
	 */
	private String resultHtml;
	
	//
	SectionItem si;
	// Тип кеширования : 1 - документы кешируются на локальном диске; 2 - кешируются в БД; 3 - кешируются на диске только обязательные файлы
	int cacheType;
	//
	TemplateThemeItem tti;
	//
	TemplateItem templateMain;
	//
	boolean isDocumentCached;
	//
	DocumentItem di;
	
	/**
	 * Constructor 
	 * @param conn 
	 * @param sectionId
	 */
	public HtmlCompile (Params params, long sectionId) {
		dateConv = new DateConv();
		this.params = params;
		config = params.getConfig();
		conn = params.getConCur();
		this.sectionId = sectionId;
		
		si = conn.db.sectionGetById(sectionId);
		try {
			cacheType =
					(si.getCacheType() == 0) ?
					Integer.parseInt(conn.db.settingsGetValue("MAIN__CACHE_DOC__ENABLE")) :
					si.getCacheType();
		} catch (DataConnectionException | DataQueryException e) {
			e.writeLog(params);
			ShowAppMsg.showAlert(
					"ERROR", "Помилка при компіляції документу, "+
					"помилка при читанні типу кешування з таблиці налаштувань, встановлюю 1",
					Integer.toString(e.getErrCode())+" "+e.getErrSign(), e.getMsg());
			cacheType = 1;
		}
		tti = conn.db.templateThemeGetById(conn.db.sectionGetThemeId(sectionId, true));
		templateMain = conn.db.sectionGetTemplateMain(sectionId, tti.getId());
		
		isDocumentCached = conn.db.documentFindBySectionId(sectionId);
		di = (isDocumentCached) ? conn.db.documentGetBySectionId(sectionId) : null;
		
		resultHtml = new String("");
	}
	
	/**
	 * geter. Скомилированный html
	 */
	public String getResultHtml () {  return resultHtml;  }
	
	/**
	 * Компилируем.
	 */
	public void compile () throws KBase_HtmlCompileEx {
		String tmplMainBody = templateMain.getBody();
		int posBegin;
		int posEnd;
		String tagName;
		
		posBegin = tmplMainBody.indexOf(TAG_DELIMITER_LEFT);
		while (posBegin >= 0) {
			posEnd = tmplMainBody.indexOf(TAG_DELIMITER_RIGHT);
			// пишем текст слева от тега
			resultHtml += tmplMainBody.substring(0, posBegin);
			// получаем название тега
			tagName = tmplMainBody.substring(posBegin+2, posEnd);
			// удаляем из буфера шаблона тег
			tmplMainBody = tmplMainBody.substring(posEnd+2);
			
			//---- получаем значение тега-переменной
			if (! mapTags.containsKey(tagName))  setTagValue(tagName);
			resultHtml += mapTags.get(tagName);
			
			// for next iteration
			posBegin = tmplMainBody.indexOf(TAG_DELIMITER_LEFT);       // for next iteration
		}
		// дописываем хвост шаблона
		resultHtml += tmplMainBody;
		
		//System.out.println(resultHtml);

		saveToDB();
	}
	
	/**
	 * Вычисляем значение тега и записываем его в список
	 * @throws KBase_HtmlCompileEx 
	 */
	String setTagValue (String tagName) throws KBase_HtmlCompileEx {
		String retVal = "";
	
		if (tagName.indexOf("(") == -1) {     // тег-похідна
			switch (tagName) {
			case "Title" :                    // заголовок документа (вверху, в начале документа)
				retVal = si.getName() +"(id = "+ si.getId() +")";
				break;
			case "Title_Path" :               // путь документа в базе данных
				retVal = conn.db.sectionGetPathName(sectionId, " > ");
				break;
			case "Section_Id" :
				retVal = String.valueOf(si.getId());
				break;
			case "Section_ParentId" :
				retVal = String.valueOf(si.getParentId());
				break;
			case "Section_Name" :
				retVal = si.getName();
				break;
			case "Section_Descr" :
				retVal = si.getDescr();
				break;
			case "Section_IconId" :
				retVal = String.valueOf(si.getIconId());
				break;
			case "Section_DateCreated" :
				retVal = dateConv.dateTimeToStr(si.getDateCreated());
				break;
			case "Section_DateModified" :
				retVal = dateConv.dateTimeToStr(si.getDateModified());
				break;
			case "Section_DateModifiedInfo" :
				if (si.getDateModifiedInfo() != null)
					retVal = dateConv.dateTimeToStr(si.getDateModifiedInfo());
				break;
			case "Section_UserCreated" :
				retVal = si.getUserCreated();
				break;
			case "Section_UserModified" :
				retVal = si.getUserModified();
				break;
			case "Section_IconIdRoot" :
				retVal = String.valueOf(si.getIconIdRoot());
				break;
			case "Section_IconIdDef" :
				retVal = String.valueOf(si.getIconIdDef());
				break;
			case "Section_StyleMain" :
				if (si.getTemplateMain() != null)
					retVal = si.getTemplateMain();
				break;
			case "Section_StyleMainTree" :
				retVal = String.valueOf(si.getTemplateMainTree());
				break;
			case "Section_StyleMainRoot" :
				retVal = String.valueOf(si.getTemplateMainRoot());
				break;
			case "Section_ThemeId" :
				retVal = String.valueOf(si.getThemeId());
				break;
				
			case "Insert_Info_Headers" :       // место вставки инфо блоков
				List<InfoHeaderItem> iL = conn.db.infoListBySectionId (sectionId);
	        	
	        	for (InfoHeaderItem i : iL) {
	        		retVal += compileInfoBlockHead(i);
	    		}
				break;
			case "Insert_Info_Blocks" :       // место вставки инфо блоков
				List<InfoHeaderItem> infoList = conn.db.infoListBySectionId (sectionId);
	        	
	        	for (InfoHeaderItem i : infoList) {
	        		retVal += compileInfoBlockBody(i);
	    		}
				break;
			default:
				throw new KBase_HtmlCompileEx (sectionId, "Неверное имя тега-переменной : "+ tagName, this);
			}
		} else {    // тег-функція
			String functionName = tagName.substring(0, tagName.indexOf("("));
		
			switch (functionName) {
			case "javaPreferences_get" :
				FromJS_Preferences fromJS_Preferences = new FromJS_Preferences();
				String var1 = tagName.substring(tagName.indexOf("(")+1, tagName.indexOf(","));
				String var2 = tagName.substring(tagName.indexOf(",")+1, tagName.indexOf(")"));
				
				retVal = fromJS_Preferences.get(var1, var2);
				
				break;
			default:
				throw new KBase_HtmlCompileEx (sectionId, "Тег-функція не підтримується : "+ functionName, this);
			}
		}
		mapTags.put(tagName, retVal);
		
		return retVal;
	}
	
	/**
	 * возвращает путь к каталогу кеша на диске с документами
	 */
	private String getDocPath () throws KBase_HtmlCompileEx {
		String retVal;
		
		Path path = Paths.get(
				FileUtil.getFullUserFileName(params, config.getItemValue("directories", "PathDirCache")) +
    			((conn.param.getConnId() > 0) ? conn.param.getConnId() : ("m"+(-1 * conn.param.getConnId()))) +"/"+
    			tti.getId());
        Path absolutePath = path.toAbsolutePath();

        try {
			retVal = absolutePath.toUri().toURL().toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new KBase_HtmlCompileEx (sectionId, "Ошибка формирования пути (URL) к документу при компиляции.", this);
		}
	
		return retVal;
	}
	
	/**
	 * Сохраняем скомпилированный документ в БД
	 */
	private void saveToDB() {
		DocumentItem dt;
		
		if (isDocumentCached) {         // update text
			dt = new DocumentItem (
					di.getId(),
					di.getSectionId(),
					resultHtml,
					cacheType,
					di.getDateCreated(),
					null,
					di.getUserCreated(),
					"");
			conn.db.documentUpdate(dt);
		} else {                        // insert document
			dt = new DocumentItem (
					conn.db.documentNextId(),
					sectionId,
					resultHtml,
					cacheType);
			conn.db.documentAdd(dt);
		}
	}
	
	/**
	 * Компіляція інфо блока, HEAD
	 */
	String compileInfoBlockHead (InfoHeaderItem infoHeader) throws KBase_HtmlCompileEx {
		TemplateItem template = conn.db.templateGet(tti.getId(), infoHeader);
		String tmplInfoBody = template.getBody();
		int posBeginHead;
		int posEndHead;
		String tmplHead;
		String tagName;
		int posBegin;
		int posEnd;
		Map<String,String> mapTagsInfoBlock = new HashMap<String,String>();
		String retVal = "";
		
		posBeginHead = tmplInfoBody.indexOf("<!--HEAD_BEGIN ");
		while (posBeginHead >= 0) {
			posEndHead = tmplInfoBody.indexOf("<!--HEAD_END ");
			
			//---- викусюємо з тексту назву блока
			int posNameHeadBegin = posBeginHead + "<!--HEAD_BEGIN ".length();
			int posNameHeadEnd = tmplInfoBody.indexOf("-->");
			String headName = tmplInfoBody.substring(posNameHeadBegin, posNameHeadEnd);
			
			// якщо в назві блоку є похідна, міняємо її на заначення
			posBegin = headName.indexOf(TAG_DELIMITER_LEFT);
			if (posBegin >= 0) {
				posEnd = headName.indexOf(TAG_DELIMITER_RIGHT);
				// получаем название тега
				tagName = headName.substring(posBegin+2, posEnd);
				// получаем значение тега-переменной
				if (! mapTagsInfoBlock.containsKey(tagName)) {
					setTagValue_Head(mapTagsInfoBlock, tagName, infoHeader, template);
				}
				headName = headName.substring(0, posBegin) +
						   mapTagsInfoBlock.get(tagName) +
						   headName.substring(posEnd+TAG_DELIMITER_RIGHT.length());
			}
			//System.out.println(headName);
			
			//---- компілюємо блок якщо його ще немає
			if (! mapHeadName.containsKey(headName)) {
				mapHeadName.put(headName, tmplInfoBody.substring(posBeginHead, posEndHead));
				tmplHead = mapHeadName.get(headName);
				 
				posBegin = tmplHead.indexOf(TAG_DELIMITER_LEFT);
				while (posBegin >= 0) {
					posEnd = tmplHead.indexOf(TAG_DELIMITER_RIGHT);
					// пишем текст слева от тега
					retVal += tmplHead.substring(0, posBegin);
					// получаем название тега
					tagName = tmplHead.substring(posBegin+2, posEnd);
					// удаляем из буфера шаблона тег
					tmplHead = tmplHead.substring(posEnd+2);
					
					//---- получаем значение тега-переменной
					if (! mapTagsInfoBlock.containsKey(tagName)) {
						setTagValue_Head(mapTagsInfoBlock, tagName, infoHeader, template);
					}
					retVal += mapTagsInfoBlock.get(tagName);
					
					// for next iteration
					posBegin = tmplHead.indexOf(TAG_DELIMITER_LEFT);       // for next iteration
				}
				// дописываем хвост шаблона
				retVal += tmplHead;
			}
			
			tmplInfoBody = tmplInfoBody.substring(posEndHead+"<!--HEAD_END ".length());
			tmplInfoBody = tmplInfoBody.substring(tmplInfoBody.indexOf("-->")+"-->".length());
			
			// for next iteration
			posBeginHead = tmplInfoBody.indexOf("<!--HEAD_BEGIN ");
		}
		
		return retVal;
	}
	
	/**
	 * Компіляція інфо блока, BODY
	 */
	String compileInfoBlockBody (InfoHeaderItem infoHeader) throws KBase_HtmlCompileEx {
		TemplateItem template = conn.db.templateGet(tti.getId(), infoHeader);
		String tmplInfoBody = template.getBody();
		int posBegin;
		int posEnd;
		String tagName;
		Map< String, String > mapTagsInfoBlock = new HashMap< String, String >();
		String retVal = "";
	
		if (tmplInfoBody.indexOf("<!--BODY_BEGIN ") >= 0) {
			tmplInfoBody = tmplInfoBody.substring(tmplInfoBody.indexOf("<!--BODY_BEGIN "));
		}
		
		posBegin = tmplInfoBody.indexOf(TAG_DELIMITER_LEFT);
		while (posBegin >= 0) {
			posEnd = tmplInfoBody.indexOf(TAG_DELIMITER_RIGHT);
			// пишем текст слева от тега
			retVal += tmplInfoBody.substring(0, posBegin);
			// получаем название тега
			tagName = tmplInfoBody.substring(posBegin+2, posEnd);
			// удаляем из буфера шаблона тег
			tmplInfoBody = tmplInfoBody.substring(posEnd+2);
			
			//---- получаем значение тега-переменной
			if (! mapTagsInfoBlock.containsKey(tagName)) {
				switch ((int)infoHeader.getInfoTypeId()) {
	    		case 1 :                            // Простой текст
	    			setTagValue_Text(mapTagsInfoBlock, tagName, infoHeader, template);
	    			break;
	    		case 2 :                            // Изображение
	    			setTagValue_Image(mapTagsInfoBlock, tagName, infoHeader, template);
	    			break;
	    		case 3 :                            // Файл	
	    			setTagValue_File(mapTagsInfoBlock, tagName, infoHeader, template);
	    			break;
	    		default:
	    			throw new KBase_HtmlCompileEx (sectionId, "Не обработан инфоблок типа id="+ infoHeader.getInfoTypeId(), this);
	    		}
			}
			retVal += mapTagsInfoBlock.get(tagName);
			
			// for next iteration
			posBegin = tmplInfoBody.indexOf(TAG_DELIMITER_LEFT);       // for next iteration
		}
		// дописываем хвост шаблона
		retVal += tmplInfoBody;
		
		return retVal;
	}
	
	/**
	 * Вычисляем значение тега для хідер блока и записываем его в список данного инфо блока
	 * @throws KBase_HtmlCompileEx 
	 */
	String setTagValue_Head (Map<String, String> mapTags_Text, String tagName, InfoHeaderItem infoHeader,
			                 TemplateItem template) 
			throws KBase_HtmlCompileEx {
		String retVal = "";
	
		switch (tagName) {
		case "templateId" :
			retVal = Long.toString(template.getId());
			break;
		default:
			throw new KBase_HtmlCompileEx (sectionId, 
					"Неверное имя тега-переменной для инфо блока \"Простой текст\" : "+ tagName, this);
		}
		mapTags_Text.put(tagName, retVal);
		
		return retVal;
	}
	
	/**
	 * Вычисляем значение тега для инфо блока "Простой текст" и записываем его в список данного инфо блока
	 * @throws KBase_HtmlCompileEx 
	 */
	String setTagValue_Text (Map<String, String> mapTags_Text, String tagName, InfoHeaderItem infoHeader,
			                 TemplateItem template) 
			throws KBase_HtmlCompileEx {
		Info_TextItem infoText = conn.db.info_TextGet(infoHeader.getInfoId());
		String retVal = "";
	
		switch (tagName) {
		case "templateId" :
			retVal = Long.toString(template.getId());
			break;
		case "InfoBlockId" :
			retVal = Long.toString(infoHeader.getId());
			break;
		case "isShowDescr" :
			retVal = Integer.toString(infoText.getIsShowTitle());
			break;
		case "InfoDescr" :
			String descr = infoText.getTitle();
			retVal = descr.replaceAll("\"", "&quot;");
			//retVal = descr.replaceAll("1", "z");
			break;
		case "kbase_Info_Text" :
			retVal = infoText.getText();
			break;
		default:
			throw new KBase_HtmlCompileEx (sectionId, 
					"Неверное имя тега-переменной для инфо блока \"Простой текст\" : "+ tagName, this);
		}
		mapTags_Text.put(tagName, retVal);
		
		return retVal;
	}
	
	/**
	 * Вычисляем значение тега для инфо блока "Изображение" и записываем его в список данного инфо блока
	 * @throws KBase_HtmlCompileEx 
	 */
	String setTagValue_Image (Map< String, String > mapTags_Image, String tagName, InfoHeaderItem infoHeader, 
			                  TemplateItem template) 
			throws KBase_HtmlCompileEx {
		Info_ImageItem infoImage = conn.db.info_ImageGet(infoHeader.getInfoId());
		String retVal = "";
	
		switch (tagName) {
		case "templateId" :
			retVal = Long.toString(template.getId());
			break;
		case "InfoBlockId" :
			retVal = Long.toString(infoHeader.getId());
			break;
		case "InfoImageTitle" :
			String title = infoImage.getTitle();
			retVal = title.replaceAll("\"", "&quot;");
			break;
		case "InfoImagePath" :
			//retVal = getDocPath() + "files_" + Long.toString(sectionId) + 
			//	     "/image_" + Long.toString(infoHeader.getId()) + ".png";
			retVal = "files_" + Long.toString(sectionId) + 
				     "/image_" + Long.toString(infoHeader.getId()) + ".png";
			break;
		case "InfoImageWidth" :
			retVal = Integer.toString(infoImage.getWidth());
			break;
		case "InfoImageHeight" :
			retVal = Integer.toString(infoImage.getHeight());
			break;
		case "InfoImageDescr" :
			String descr = infoImage.getDescr();
			retVal = descr.replaceAll("\"", "&quot;");
			break;
		case "InfoImageText" :
			retVal = infoImage.getText();
			break;
		case "isShowTitle" :
			retVal = Integer.toString(infoImage.getIsShowTitle());
			break;
		case "isShowDescr" :
			retVal = Integer.toString(infoImage.getIsShowDescr());
			break;
		case "isShowText" :
			retVal = Integer.toString(infoImage.getIsShowText());
			break;
		default:
			throw new KBase_HtmlCompileEx (sectionId, 
					"Неверное имя тега-переменной для инфо блока \"Изображение\" : "+ tagName, this);
		}
		mapTags_Image.put(tagName, retVal);
		
		return retVal;
	}
	
	/**
	 * Вычисляем значение тега для инфо блока "Файл" и записываем его в список данного инфо блока
	 * @throws KBase_HtmlCompileEx 
	 */
	String setTagValue_File (Map< String, String > mapTags_File, String tagName, InfoHeaderItem infoHeader, 
			                 TemplateItem template) 
			throws KBase_HtmlCompileEx {
		Info_FileItem infoFile = conn.db.info_FileGet(infoHeader.getInfoId());
		String retVal = "";
	
		switch (tagName) {
		case "templateId" :
			retVal = Long.toString(template.getId());
			break;
		case "InfoBlockId" :
			retVal = Long.toString(infoHeader.getId());
			break;
		case "InfoFileTitle" :
			String title = infoFile.getTitle();
			retVal = title.replaceAll("\"", "&quot;");
			break;
		case "InfoFilePath" :
			retVal = "files_" + Long.toString(sectionId) + 
				     "/file_" + Long.toString(infoHeader.getId()) + "." + FileUtil.getFileExt(infoFile.getName());
			break;
		case "InfoFileName" :
			retVal = infoFile.getName();
			break;
		case "InfoIconPath" :
			retVal = "files_" + Long.toString(sectionId) +
			         "/file_" + Long.toString(infoHeader.getId()) + "_image.png";
			break;
		case "InfoFileDescr" :
			String descr = infoFile.getDescr();
			retVal = descr.replaceAll("\"", "&quot;");
			break;
		case "InfoFileText" :
			retVal = infoFile.getText();
			break;
		case "isShowTitle" :
			retVal = Integer.toString(infoFile.getIsShowTitle());
			break;
		case "isShowDescr" :
			retVal = Integer.toString(infoFile.getIsShowDescr());
			break;
		case "isShowText" :
			retVal = Integer.toString(infoFile.getIsShowText());
			break;
		default:
			throw new KBase_HtmlCompileEx (sectionId, 
					"Неверное имя тега-переменной для инфо блока \"Файл\" : "+ tagName, this);
		}
		mapTags_File.put(tagName, retVal);
		
		return retVal;
	}
}
