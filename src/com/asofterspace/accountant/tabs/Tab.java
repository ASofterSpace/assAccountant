/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.Database;
import com.asofterspace.toolbox.io.Directory;

import javax.swing.JPanel;


public abstract class Tab {

	JPanel tab = null;


	/**
	 * Is this tab shown in the menu on the left (true) or is it hidden and
	 * only accessible through the operations tab (false)?
	 */
	public boolean isShownInMenu() {
		return true;
	}

	public void createTabOnGUI(JPanel parentPanel, Database database, String searchFor) {
	}

	public void destroyTabOnGUI(JPanel parentPanel) {
		if (tab != null) {
			parentPanel.remove(tab);
		}
	}

	public abstract String getHtmlGUI(Database database, String searchFor);

	/**
	 * Export this tab in one or more CSV files into a folder inside exportDir and return the
	 * directory entry for that folder
	 */
	public Directory exportCsvTo(Directory exportDir, Database database) {
		// by default, do nothing - as this is optional, and most tabs are not yet able to do this :)
		return null;
	}
}
