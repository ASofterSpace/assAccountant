/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.TimeSpan;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JPanel;


public class MonthTab extends TimeSpanTab {

	private Month month;

	private JPanel tab;


	public MonthTab(Month month) {
		this.month = month;
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "<div class='mainTitle'>" + month.toString() + "</div>";

		html += AccountingUtils.createTimeSpanTabHtml(month, database, searchFor);

		if ("".equals(searchFor)) {
			html += AccountingUtils.createOverviewAndTaxInfoHtml(month);
		}

		html += "<div class='footer'>&nbsp;</div>";

		return html;
	}

	@Override
	public void createTabOnGUI(JPanel parentPanel, Database database, String searchFor) {

		if (tab != null) {
			destroyTabOnGUI(parentPanel);
		}

		int i = 0;

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();

		tab = new JPanel();
		tab.setLayout(new GridBagLayout());
		tab.setBackground(GUI.getBackgroundColor());

		JPanel topHUD = new JPanel();
		topHUD.setBackground(GUI.getBackgroundColor());
		topHUD.setLayout(new GridBagLayout());

		CopyByClickLabel nameLabel = AccountingUtils.createHeadLabel(month.toString());
		topHUD.add(nameLabel, new Arrangement(0, 0, 1.0, 1.0));

		tab.add(topHUD, new Arrangement(0, i, 1.0, 0.0));
		i++;


		i = AccountingUtils.createTimeSpanTabMainContent(month, tab, i, database, searchFor);


		if ("".equals(searchFor)) {
			i = AccountingUtils.createOverviewAndTaxInfo(month, tab, i);
		}


		JPanel footer = new JPanel();
		footer.setBackground(GUI.getBackgroundColor());
		tab.add(footer, new Arrangement(0, i, 1.0, 1.0));

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
			return -1;
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
		if (tab instanceof BankStatementTab) {
			return 1;
		}
		if (tab instanceof BankStatementYearTab) {
			int result = ((BankStatementYearTab) tab).getYear().getNum() - getYear().getNum();
			if (result == 0) {
				return -1;
			}
			return result;
		}
		if (tab instanceof MonthTab) {
			int result = ((MonthTab) tab).getYear().getNum() - getYear().getNum();
			if (result == 0) {
				return ((MonthTab) tab).getMonth().getNum() - month.getNum();
			}
			return result;
		}
		if (tab instanceof YearTab) {
			int result = ((YearTab) tab).getYear().getNum() - getYear().getNum();
			if (result == 0) {
				return 1;
			}
			return result;
		}
		return -1;
	}

	public Month getMonth() {
		return month;
	}

	@Override
	public Year getYear() {
		return month.getYear();
	}

	@Override
	public TimeSpan getTimeSpan() {
		return month;
	}

	@Override
	public String toString() {
		return month.toString();
	}

}
