/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.timespans.TimeSpan;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.io.CsvFile;
import com.asofterspace.toolbox.io.CsvFileGerman;
import com.asofterspace.toolbox.io.Directory;

import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

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
		html += "<span class='toprightAction' onclick='window.accountant.exportCsvs(\"year_" + year + "\")'>" +
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

		TimeSpan timeSpan = year;

		Directory resultDir = new Directory(exportDir, year.toString());
		resultDir.clear();


		List<String> headlineCols = new ArrayList<>();
		headlineCols.add("Date");
		headlineCols.add("Title");
		headlineCols.add("Customer");
		headlineCols.add("Pre Tax Amount");
		headlineCols.add("Tax Percent");
		headlineCols.add("Post Tax Amount");
		headlineCols.add("Received On");

		CsvFileGerman csvFile = new CsvFileGerman(resultDir, "outgoing.csv");
		csvFile.setHeadLine(headlineCols);

		List<Outgoing> curOutgoings = timeSpan.getOutgoings();
		for (Outgoing cur : curOutgoings) {
			csvFile.appendContent(cur.createCsvLine(database));
		}

		csvFile.save();


		headlineCols = new ArrayList<>();
		headlineCols.add("Date");
		headlineCols.add("Title");
		headlineCols.add("Category");
		headlineCols.add("Pre Tax Amount");
		headlineCols.add("Tax Percent");
		headlineCols.add("Post Tax Amount");
		headlineCols.add("Paid On");

		csvFile = new CsvFileGerman(resultDir, "incoming.csv");
		csvFile.setHeadLine(headlineCols);

		List<Incoming> curEntries = timeSpan.getIncomings();
		for (Incoming cur : curEntries) {
			csvFile.appendContent(cur.createCsvLine(database));
		}

		csvFile.save();


		csvFile = new CsvFileGerman(resultDir, "donations.csv");
		csvFile.setHeadLine(headlineCols);

		curEntries = timeSpan.getDonations();
		for (Incoming cur : curEntries) {
			csvFile.appendContent(cur.createCsvLine(database));
		}

		csvFile.save();


		csvFile = new CsvFileGerman(resultDir, "personals.csv");
		csvFile.setHeadLine(headlineCols);

		curEntries = timeSpan.getPersonals();
		for (Incoming cur : curEntries) {
			csvFile.appendContent(cur.createCsvLine(database));
		}

		csvFile.save();


		return resultDir;
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
