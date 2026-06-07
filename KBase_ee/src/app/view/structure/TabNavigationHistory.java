package app.view.structure;

import java.util.ArrayList;

/**
 * 
 * @author IMakarevich
 */
public class TabNavigationHistory {
	private ArrayList <Integer> list;
	
	private boolean isDeleted;
	
	/**
	 * Constructor
	 */
	public TabNavigationHistory() {
		list = new ArrayList<>();
		isDeleted = false;
	}
	
	/**
	 * add element
	 * @param tabId
	 */
	public void add(int id) {
		for (int i=0; i<list.size(); i++) {
			if (list.get(i) == id) {
				list.remove(i);
			}
		}
		list.add(id);
	}
	
	/**
	 * Delete element
	 */
	public void delete(int id) {
		for (int i=0; i<list.size(); i++) {
			if (list.get(i) == id) {
				list.remove(i);
				isDeleted = true;
			}
		}
	}
	
	/**
	 * Get last element
	 * @return
	 */
	public int getLast() {
		if (list.size() > 0) {
			return list.get(list.size()-1);
		} else {
			return 0;
		}
	}
	
	/**
	 * getter for isDeleted
	 * @return
	 */
	public boolean getIsDeleted () {
		return isDeleted;
	}
	
	/**
	 * setter for isDeleted
	 */
	public void setIsDeleted (boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}
