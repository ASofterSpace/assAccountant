/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.Database;

import javax.swing.JPanel;


public abstract class Tab {

	public abstract void createTabOnGUI(JPanel parentPanel, Database database);

	public abstract void destroyTabOnGUI(JPanel parentPanel);

	public abstract int compareTo(Tab tab);
}
