/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.ConfigCtrl;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.timespans.TimeSpan;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.web.ServerRequestHandler;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.io.Directory;

import java.awt.GridBagLayout;

import javax.swing.JPanel;


public class YearTab extends TimeSpanTab {

	private Year year;

	private JPanel tab;


	public YearTab(Year year) {
		this.year = year;
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "";

		html += "<div class='relContainer'>";

		if (ConfigCtrl.invoiceLocationIsSet()) {
			html += "<span class='topleftAction button' ";
			html += "onclick='window.accountant.openInOS(" + getYear().getNum() + ", null)'>";
			html += "Open in OS</span>";
			String invoiceLocation = ConfigCtrl.getInvoiceLocation(getYear().getNum(), null);
			html += "<a class='topleftAction button' style='left:76pt;' target='_blank' ";
			html += "href='localhost:3013/?link=" + invoiceLocation + "'>Open in Browser</span>";
			html += "</a>";
		}

		html += "<a class='toprightAction button' style='right:150pt;' target='_blank' href='print_pdf_bwa_" +
			ServerRequestHandler.tabToLink(this) + "'>Print BWA</span>";
		html += "</a>";
		html += "<a class='toprightAction button' style='right:92pt;' target='_blank' href='print_pdf_euer_" +
			ServerRequestHandler.tabToLink(this) + "'>Print EÜR</span>";
		html += "</a>";
		html += "<span class='toprightAction button' onclick='window.accountant.exportCsvs(\"" + ServerRequestHandler.tabToLink(this) + "\")'>" +
				"Export to CSVs</span>";
		html += "</div>";

		html += "<div class='mainTitle'>" + year.toString() + " (yearly overview)</div>";

		html += AccountingUtils.createTimeSpanTabHtml(year, database, searchFor);

		if ("".equals(searchFor)) {
			html += AccountingUtils.createOverviewAndTaxInfoHtml(year);
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

		tab = new JPanel();
		tab.setBackground(GUI.getBackgroundColor());
		tab.setLayout(new GridBagLayout());

		JPanel topHUD = new JPanel();
		topHUD.setBackground(GUI.getBackgroundColor());
		topHUD.setLayout(new GridBagLayout());

		CopyByClickLabel nameLabel = AccountingUtils.createHeadLabel(year.toString() + " (yearly overview)");
		topHUD.add(nameLabel, new Arrangement(0, 0, 1.0, 1.0));

		tab.add(topHUD, new Arrangement(0, i, 1.0, 0.0));
		i++;


		i = AccountingUtils.createTimeSpanTabMainContent(year, tab, i, database, searchFor);


		if ("".equals(searchFor)) {
			i = AccountingUtils.createOverviewAndTaxInfo(year, tab, i);
		}


		JPanel footer = new JPanel();
		footer.setBackground(GUI.getBackgroundColor());
		tab.add(footer, new Arrangement(0, i, 1.0, 1.0));

		AccountingUtils.resetTabSize(tab, parentPanel);

		parentPanel.add(tab);
	}

	@Override
	public Directory exportCsvTo(Directory exportDir, Database database) {
		return AccountingUtils.exportTimeSpanCsvTo(exportDir, database, year, this);
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
		if (tab instanceof IncomeLogTab) {
			return 1;
		}
		if (tab instanceof CalculatorTab) {
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
		if (tab instanceof TimeSpanTab) {
			return ((TimeSpanTab) tab).getYear().getNum() - year.getNum();
		}
		return -1;
	}

	@Override
	public Year getYear() {
		return year;
	}

	@Override
	public TimeSpan getTimeSpan() {
		return year;
	}

	@Override
	public String toString() {
		return year.toString();
	}

}
