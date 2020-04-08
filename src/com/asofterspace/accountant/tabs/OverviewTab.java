/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JPanel;


public class OverviewTab extends Tab {

	private static final String TITLE = "Overview";

	private JPanel tab;


	public OverviewTab() {
	}

	@Override
	public void createTabOnGUI(JPanel parentPanel, Database database) {

		if (tab != null) {
			destroyTabOnGUI(parentPanel);
		}

		int i = 0;

		tab = new JPanel();
		tab.setLayout(new GridBagLayout());

		JPanel topHUD = new JPanel();
		topHUD.setLayout(new GridBagLayout());

		CopyByClickLabel nameLabel = AccountingUtils.createHeadLabel(TITLE);
		topHUD.add(nameLabel, new Arrangement(0, 0, 1.0, 1.0));

		tab.add(topHUD, new Arrangement(0, i, 1.0, 0.0));
		i++;


		// TODO - display information such as outgoing invoices which have been sent out more
		// than six weeks ago and not yet been set to having come in

		// TODO - display information such as finance days that have last been done (or maybe
		// even get away from real "finance days", and instead have continuous financing going on,
		// this here always showing what is now left to do?)


		JPanel footer = new JPanel();
		tab.add(footer, new Arrangement(0, i, 1.0, 1.0));
		i++;

		Dimension newSize = new Dimension(parentPanel.getWidth(), tab.getMinimumSize().height + 100);
		tab.setPreferredSize(newSize);
		parentPanel.setPreferredSize(newSize);

		parentPanel.add(tab);
	}

	@Override
	public void destroyTabOnGUI(JPanel parentPanel) {
		if (tab != null) {
			parentPanel.remove(tab);
		}
	}

	@Override
	public int compareTo(Tab tab) {
		if (tab == null) {
			return 1;
		}
		if (tab instanceof OverviewTab) {
			return 0;
		}
		return -1;
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
