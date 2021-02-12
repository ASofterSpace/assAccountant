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

	/**
	 * Get the comparison order; higher numbers are sorted further towards the top!
	 */
	public abstract int getComparisonOrder();

	public int compareTo(Tab tab) {
		if (tab == null) {
			return 1;
		}
		return tab.getComparisonOrder() - getComparisonOrder();
	}

	/**
	 * Export this tab in one or more CSV files into a folder inside exportDir and return the
	 * directory entry for that folder
	 */
	public Directory exportCsvTo(Directory exportDir, Database database) {
		// by default, do nothing - as this is optional, and most tabs are not yet able to do this :)
		return null;
	}
}
