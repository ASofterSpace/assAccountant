/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.tasks.Task;
import com.asofterspace.toolbox.accounting.FinanceUtils;
import com.asofterspace.toolbox.gui.Arrangement;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JPanel;


public class CalculatorTab extends Tab {

	private static final String TITLE = "Calculator";

	private JPanel tab;


	public CalculatorTab() {
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "";

		html += "<div class='mainTitle'>" + TITLE + "</div>";

		int baseAmount = 23;

		html += "<div style='padding-bottom:10pt;'>I can help you with some calculations. :)</div>";
		html += "<div>For generating skyhook invoices, ";
		html += "it can be helpful to know the multiples of " + baseAmount + " €:</div>";
		html += "<div>";
		for (int m = 1; m <= 8*60; m++) {
			String text = FinanceUtils.formatMoney((baseAmount * 100 * m) / 60) + " €";
			int width = 95 / 5;
			html += "<div style='display: inline-block; width: " + width + "%; cursor: pointer;' ";
			html += "class='line' onclick='accountant.copyText(\"" + text + "\")'>";
			if (m >= 60) {
				html += (m / 60) + "h ";
			}
			html += (m % 60) + "m: ";
			html += text;
			html += "</div>";
		}
		html += "</div>";

		html += "<div class='footer'>&nbsp;</div>";

		return html;
	}

	@Override
	public void createTabOnGUI(final JPanel parentPanel, final Database database, String searchFor) {

		tab = new JPanel();
		tab.setBackground(GUI.getBackgroundColor());
		tab.setLayout(new GridBagLayout());

		JPanel footer = new JPanel();
		footer.setBackground(GUI.getBackgroundColor());
		tab.add(footer, new Arrangement(0, 1, 1.0, 1.0));

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
			return 1;
		}
		if (tab instanceof IncomeLogTab) {
			return 1;
		}
		if (tab instanceof CalculatorTab) {
			return 0;
		}
		return -1;
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
