package app.module.scheduler;

import app.model.Params;

/**
 * Фабричний клас створення обьєктів Контролів Завдань
 */
public class TaskControlFactory {
	public TaskControl newTaskControl (long taskId, long typeId, Params params, int initFlag) {
		TaskControl retVal = null;
		
		switch ((int)typeId) {
		case TaskItem.TYPE_ITEM_TASK_APP_CONSOLE :  //Консольний додаток
			retVal = new TaskControl_AppConsole(taskId, params, initFlag);
			break;
		case TaskItem.TYPE_ITEM_TASK_PRG_SAVE_STATE :  //(prg) Збереження стану програми
			retVal = new TaskControl_Prg_SaveState(taskId, params, initFlag);
			break;
		case TaskItem.TYPE_ITEM_TASK_PRG_SAVE_ALL_CHANGES :  //(prg) Збереження усіх редагувань
			retVal = new TaskControl_Prg_SaveChanges(taskId, params, initFlag);
			break;
		case TaskItem.TYPE_ITEM_TASK_MSGBASE_SHOW_MESSAGES :  //(msgBase) Показ повідомлень
			retVal = new TaskControl_MsgBase_ShowMessage(taskId, params, initFlag);
			break;
		case TaskItem.TYPE_ITEM_TASK_JAR_PLUGIN :  //JAR-plugin
			retVal = new TaskControl_Jar_Plugin(taskId, params, initFlag);
			break;
		}
		
		return retVal;
	}
}
