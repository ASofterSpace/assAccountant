/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.AssAccountant;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.tasks.Task;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.web.ServerRequestHandler;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.accounting.FinanceUtils;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.images.ColorRGB;
import com.asofterspace.toolbox.images.DefaultImageFile;
import com.asofterspace.toolbox.images.GraphImage;
import com.asofterspace.toolbox.images.GraphTimeDataPoint;
import com.asofterspace.toolbox.io.CsvFileGerman;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.utils.DateUtils;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;


public class IncomeLogTab extends Tab {

	private static final String TITLE = "Income Log";

	private JPanel tab;


	public IncomeLogTab() {
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "";

		html += "<div class='relContainer'>";
		html += "<span class='toprightAction' onclick='window.accountant.exportCsvs(\"" + ServerRequestHandler.tabToLink(this) + "\")'>" +
				"Export to CSVs</span>";
		html += "</div>";

		html += "<div class='mainTitle'>" + TITLE + "</div>";

		html += "<div>This shows our total income (sent invoices before any tax) per month, smoothed over three months.<br>";
		html += "An upwards slope is good, a sideways slope is okay.</div>";


		List<GraphTimeDataPoint> timeData = new ArrayList<>();

		GraphImage smoothGraph = new GraphImage(1000, 400);
		smoothGraph.setForegroundColor(new ColorRGB(120, 40, 220));
		smoothGraph.setBackgroundColor(new ColorRGB(0, 0, 0, 255));
		smoothGraph.setDataColor(new ColorRGB(160, 80, 255));
		smoothGraph.setBaseYmin(0.0);

		GraphImage graph = new GraphImage(1000, 400);
		graph.setForegroundColor(new ColorRGB(120, 40, 220));
		graph.setBackgroundColor(new ColorRGB(0, 0, 0, 255));
		graph.setDataColor(new ColorRGB(160, 80, 255));
		graph.setBaseYmin(0.0);

		List<Year> years = database.getYears();
		boolean foundEntry = false;
		String afterHtml = "";

		Date today = DateUtils.now();
		int curYear = DateUtils.getYear(today);
		int curMonth = DateUtils.getMonth(today);

		for (Year year : years) {
			for (Month month : year.getMonths()) {

				// only start showing months starting in June 2018
				if (year.getNum() == 2018) {
					if (month.getNum() < 6) {
						continue;
					}
				}

				// do not show months after the current one
				if (year.getNum() > curYear) {
					continue;
				}
				if (year.getNum() == curYear) {
					if (month.getNum() > curMonth) {
						continue;
					}
				}

				afterHtml += "<div class='line'>";
				afterHtml += "<span style='width: 38%; display: inline-block; text-align: right;'>";
				afterHtml += month.getMonthName() + " " + month.getYear() + ":&nbsp;&nbsp;&nbsp;";
				afterHtml += "</span>";
				afterHtml += "<span style='width: 20%; display: inline-block;'>";
				afterHtml += FinanceUtils.formatMoney(month.getInTotalAfterTax(), Currency.EUR);
				afterHtml += "</span>";

				List<Incoming> incomings = month.getIncomings();
				String sep = "";
				if (incomings.size() > 0) {
					afterHtml += "<span>(";
					for (Incoming incoming : incomings) {
						afterHtml += sep;
						sep = ", ";
						afterHtml += database.mapCustomerToShortKey(incoming.getCustomer());
					}
					afterHtml += ")</span>";
				}

				afterHtml += "</div>";

				foundEntry = true;

				timeData.add(new GraphTimeDataPoint(month.getEndDate(), month.getInTotalAfterTax()));
			}
			afterHtml += "<div>";
			afterHtml += "&nbsp;";
			afterHtml += "</div>";
		}

		// for-else:
		if (!foundEntry) {
			html += "<div>No finance logs have been found!</div>";
		} else {
			html += "<div><img src='income_log_graph_smoothened.png' /></div>";

			html += "<div>Here is the same data with only minor smoothening:</div>";

			html += "<div><img src='income_log_graph.png' /></div>";

			html += "<div>And finally, there is the original raw data:</div>";
			html += "<div>&nbsp;</div>";

			html += afterHtml;

			smoothGraph.setIncludeTodayInTimeData(true);
			smoothGraph.setAbsoluteTimeDataPoints(timeData);
			smoothGraph.smoothen(68);
			DefaultImageFile smoothGraphFile = new DefaultImageFile(AssAccountant.getWebRoot(), "income_log_graph_smoothened.png");
			smoothGraphFile.assign(smoothGraph);
			smoothGraphFile.saveTransparently();

			graph.setIncludeTodayInTimeData(true);
			graph.setAbsoluteTimeDataPoints(timeData);
			graph.smoothen(9);
			DefaultImageFile graphFile = new DefaultImageFile(AssAccountant.getWebRoot(), "income_log_graph.png");
			graphFile.assign(graph);
			graphFile.saveTransparently();
		}

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
	public Directory exportCsvTo(Directory exportDir, Database database) {

		Directory resultDir = new Directory(exportDir, ServerRequestHandler.tabToLink(this));
		resultDir.clear();


		List<String> headlineCols = new ArrayList<>();
		headlineCols.add("Month");
		headlineCols.add("Total Income");

		CsvFileGerman csvFile = new CsvFileGerman(resultDir, "finances.csv");
		csvFile.setHeadLine(headlineCols);

		List<Year> years = database.getYears();
		String afterHtml = "";
		for (Year year : years) {
			for (Month month : year.getMonths()) {
				if (year.getNum() == 2018) {
					if (month.getNum() < 6) {
						continue;
					}
				}
				List<String> cur = new ArrayList<>();
				cur.add(month.getMonthName() + " " + month.getYear());
				cur.add(CsvFileGerman.sanitizeForCsv(month.getInTotalAfterTax() / 100.0));
				csvFile.appendContent(cur);
			}
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
			return 0;
		}
		return -1;
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
