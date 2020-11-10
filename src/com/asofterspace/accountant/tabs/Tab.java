/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.Database;
import com.asofterspace.toolbox.io.Directory;

import javax.swing.JPanel;


public abstract class Tab {

	public abstract void createTabOnGUI(JPanel parentPanel, Database database, String searchFor);

	public abstract void destroyTabOnGUI(JPanel parentPanel);

	public abstract String getHtmlGUI(Database database, String searchFor);

	public abstract int compareTo(Tab tab);

	/**
	 * Export this tab in one or more CSV files into a folder inside exportDir and return the
	 * directory entry for that folder
	 */
	public Directory exportCsvTo(Directory exportDir, Database database) {
		// by default, do nothing - as this is optional, and most tabs are not yet able to do this :)
		return null;
	}
}
