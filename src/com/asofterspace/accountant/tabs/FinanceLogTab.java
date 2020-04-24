/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.FinanceLogEntry;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.Task;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;


public class FinanceLogTab extends Tab {

	private static final String TITLE = "Finance Log";

	private JPanel tab;


	public FinanceLogTab() {
	}

	@Override
	public void createTabOnGUI(final JPanel parentPanel, final Database database, String searchFor) {

		if (tab != null) {
			destroyTabOnGUI(parentPanel);
		}

		int i = 0;

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();

		tab = new JPanel();
		tab.setBackground(GUI.getBackgroundColor());
		tab.setLayout(new GridBagLayout());

		JPanel topHUD = new JPanel();
		topHUD.setBackground(GUI.getBackgroundColor());
		topHUD.setLayout(new GridBagLayout());

		CopyByClickLabel nameLabel = AccountingUtils.createHeadLabel(TITLE);
		topHUD.add(nameLabel, new Arrangement(0, 0, 1.0, 1.0));

		tab.add(topHUD, new Arrangement(0, i, 1.0, 0.0));
		i++;

		CopyByClickLabel curLabel;
		JPanel curPanel;


		List<FinanceLogEntry> entries = database.getTaskCtrl().getFinanceLogs();
		boolean foundEntry = false;
		for (FinanceLogEntry entry : entries) {
			if (entry.getRows().size() > 0) {
				curPanel = entry.createPanelOnGUI(database);
				curPanel.setBackground(GUI.getBackgroundColor());
				tab.add(curPanel, new Arrangement(0, i, 1.0, 1.0));
				i++;

				Dimension newSize = new Dimension(parentPanel.getWidth(), curPanel.getMinimumSize().height);
				curPanel.setPreferredSize(newSize);
				foundEntry = true;
			}
		}
		// for-else:
		if (!foundEntry) {
			curLabel = new CopyByClickLabel("No finance logs have been found!");
			tab.add(curLabel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}


		JPanel footer = new JPanel();
		footer.setBackground(GUI.getBackgroundColor());
		tab.add(footer, new Arrangement(0, i, 1.0, 1.0));
		i++;

		AccountingUtils.resetTabSize(tab, parentPanel);

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
			return 1;
		}
		if (tab instanceof TaskLogTab) {
			return 1;
		}
		if (tab instanceof FinanceLogTab) {
			return 0;
		}
		return -1;
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
