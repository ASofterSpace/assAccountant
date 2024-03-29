/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.ConfigCtrl;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.TimeSpan;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.web.ServerRequestHandler;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.io.Directory;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JPanel;


public class MonthTab extends TimeSpanTab {

	private Month month;


	public MonthTab(Month month) {
		this.month = month;
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "";

		html += "<div class='relContainer'>";

		if (ConfigCtrl.invoiceLocationIsSet()) {
			html += "<span class='topleftAction button' style='left:4pt;' ";
			html += "onclick='window.accountant.openInOS(" + getYear().getNum() + ", " + getMonth().getNum() + ")'>";
			html += "Open in OS</span>";
			String invoiceLocation = ConfigCtrl.getInvoiceLocation(getYear().getNum(), getMonth().getNum());
			html += "<a class='topleftAction button' style='left:76pt;' target='_blank' ";
			html += "href='http://localhost:3013/?link=" + invoiceLocation + "'>Open in Browser</span>";
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

		html += "<div class='mainTitle'>" + month.toString() + "</div>";

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
	public Directory exportCsvTo(Directory exportDir, Database database) {
		return AccountingUtils.exportTimeSpanCsvTo(exportDir, database, month, this);
	}

	@Override
	public void destroyTabOnGUI(JPanel parentPanel) {
		if (tab != null) {
			parentPanel.remove(tab);
		}
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
