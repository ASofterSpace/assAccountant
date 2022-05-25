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
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.web.ServerRequestHandler;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.images.ColorRGBA;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
		html += "<span class='toprightAction button' onclick='window.accountant.exportCsvs(\"" + ServerRequestHandler.tabToLink(this) + "\")'>" +
				"Export to CSVs</span>";
		html += "</div>";

		html += "<div class='mainTitle'>" + TITLE + "</div>";

		html += "<div>This shows our total income (sent invoices before any tax) per month, smoothed over three months.<br>";
		html += "An upwards slope is good, a sideways slope is okay.</div>";


		Map<Integer, Integer> incomeTaxes = database.getIncomeTaxes();


		List<GraphTimeDataPoint> timeData = new ArrayList<>();

		GraphImage smoothGraph = new GraphImage(1000, 400);
		smoothGraph.setForegroundColor(new ColorRGBA(136, 170, 255));
		smoothGraph.setBackgroundColor(new ColorRGBA(0, 0, 0, 0));
		smoothGraph.setDataColor(new ColorRGBA(136, 170, 255));
		smoothGraph.setBaseYmin(0.0);

		GraphImage graph = new GraphImage(1000, 400);
		graph.setForegroundColor(new ColorRGBA(136, 170, 255));
		graph.setBackgroundColor(new ColorRGBA(0, 0, 0, 0));
		graph.setDataColor(new ColorRGBA(136, 170, 255));
		graph.setBaseYmin(0.0);

		List<Year> years = database.getYears();
		boolean foundEntry = false;
		String afterHtml = "";
		String yearlyHtml = "";

		Date today = DateUtils.now();
		int curYear = DateUtils.getYear(today);
		int curMonth = DateUtils.getMonth(today);
		boolean veryFirstLine = true;

		for (Year year : years) {

			int averageSum = 0;
			int averageMonths = 0;
			HashSet<String> keysThisYear = new HashSet<>();

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

				// ignore the very first line, which is the current month, in the monthly average calculation
				if (!veryFirstLine) {
					averageMonths++;
					averageSum += month.getInTotalAfterTax();
				}
				veryFirstLine = false;

				afterHtml += "<div class='line'>";
				afterHtml += "<span style='width: 38%; display: inline-block; text-align: right;'>";
				afterHtml += month.getMonthName() + " " + month.getYear() + ":&nbsp;&nbsp;&nbsp;";
				afterHtml += "</span>";
				afterHtml += "<span style='width: 20%; display: inline-block;'>";
				afterHtml += database.formatMoney(month.getInTotalAfterTax(), Currency.EUR);
				afterHtml += "</span>";

				List<Incoming> incomings = month.getIncomings();
				String sep = "";
				if (incomings.size() > 0) {
					afterHtml += "<span>(";
					for (Incoming incoming : incomings) {
						afterHtml += sep;
						sep = ", ";
						String key = database.mapCustomerToShortKey(incoming.getCustomer());
						keysThisYear.add(key);
						afterHtml += key;
					}
					afterHtml += ")</span>";
				}

				afterHtml += "</div>";

				foundEntry = true;

				timeData.add(new GraphTimeDataPoint(month.getEndDate(), month.getInTotalAfterTax()));
			}

			// monthly average for the year
			String curHtml = "<div class='line'>";
			curHtml += "<span style='width: 38%; display: inline-block; text-align: right;'>";
			curHtml += "Average gross income over " + averageMonths + " months in " + year + ":&nbsp;&nbsp;&nbsp;";
			curHtml += "</span>";
			curHtml += "<span style='width: 20%; display: inline-block;'>";
			int average = 0;
			if (averageMonths > 0) {
				average = averageSum / averageMonths;
			}
			curHtml += database.formatMoney(average, Currency.EUR);
			curHtml += "</span>";
			curHtml += "<span>(";
			String sep = "";
			for (String key : keysThisYear) {
				curHtml += sep;
				sep = ", ";
				curHtml += key;
			}
			curHtml += ")</span>";
			curHtml += "</div>";

			yearlyHtml += curHtml;
			afterHtml += curHtml;

			int ongoingAverage = average;

			// monthly average taxe: USt
			afterHtml += "<div class='line'>";
			afterHtml += "<span style='width: 38%; display: inline-block; text-align: right;'>";
			afterHtml += "Average USt tax over " + averageMonths + " months in " + year + ":&nbsp;&nbsp;&nbsp;";
			afterHtml += "</span>";
			afterHtml += "<span style='width: 20%; display: inline-block;'>- ";
			average = 0;
			if (averageMonths > 0) {
				average = year.getInTotalTax() / averageMonths;
			}
			afterHtml += database.formatMoney(average, Currency.EUR);
			afterHtml += "</span>";
			afterHtml += "</div>";

			ongoingAverage -= average;

			// monthly average taxe: ESt
			afterHtml += "<div class='line'>";
			afterHtml += "<span style='width: 38%; display: inline-block; text-align: right;'>";
			afterHtml += "Average ";
			Integer incomeTaxAmount = incomeTaxes.get(year.getNum());
			if (incomeTaxAmount == null) {
				incomeTaxAmount = (int) year.getExpectedIncomeTax();
				afterHtml += "expected";
			} else {
				afterHtml += "actual";
			}
			afterHtml += " ESt tax over " + averageMonths + " months in " + year + ":&nbsp;&nbsp;&nbsp;";
			afterHtml += "</span>";
			afterHtml += "<span style='width: 20%; display: inline-block;'>- ";
			average = 0;
			if (averageMonths > 0) {
				average = incomeTaxAmount / averageMonths;
			}
			afterHtml += database.formatMoney(average, Currency.EUR);
			afterHtml += "</span>";
			afterHtml += "</div>";

			ongoingAverage -= average;

			// monthly average net
			curHtml = "<div class='line'>";
			curHtml += "<span style='width: 38%; display: inline-block; text-align: right;'>";
			curHtml += "Average net income over " + averageMonths + " months in " + year + ":&nbsp;&nbsp;&nbsp;";
			curHtml += "</span>";
			curHtml += "<span style='width: 20%; display: inline-block;'>";
			curHtml += database.formatMoney(ongoingAverage, Currency.EUR);
			curHtml += "</span>";
			curHtml += "</div>";

			yearlyHtml += curHtml;
			afterHtml += curHtml;

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

			html += "<div>Here is the overview for the different years:</div>";
			html += "<div>&nbsp;</div>";
			html += yearlyHtml;
			html += "<div>&nbsp;</div>";

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
	public int getComparisonOrder() {
		return (10000 * 100) + 3000;
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
