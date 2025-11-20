/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.AssAccountant;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.tasks.FinanceLogEntry;
import com.asofterspace.accountant.tasks.Task;
import com.asofterspace.accountant.web.ServerRequestHandler;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.guiImages.ImagePanel;
import com.asofterspace.toolbox.images.ColorRGBA;
import com.asofterspace.toolbox.images.DefaultImageFile;
import com.asofterspace.toolbox.images.GraphImage;
import com.asofterspace.toolbox.images.GraphTimeDataPoint;
import com.asofterspace.toolbox.io.CsvFileGerman;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.utils.DateUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;


public class FinanceLogTab extends Tab {

	private static final String TITLE = "Finance Log";


	public FinanceLogTab() {
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "";

		html += "<div class='relContainer'>";
		html += "<span class='toprightAction button' onclick='window.accountant.exportCsvs(\"" + ServerRequestHandler.tabToLink(this) + "\")'>" +
				"Export to CSVs</span>";
		html += "</div>";

		html += "<div class='mainTitle'>" + TITLE + "</div>";

		html += "<div>This shows the total amount of money on all our accounts, sampled once per month.<br>";
		html += "An upwards slope is good, a sideways slope is okay.</div>";


		List<GraphTimeDataPoint> timeData = new ArrayList<>();

		GraphImage graph = new GraphImage(1000, 600);
		graph.setForegroundColor(new ColorRGBA(136, 170, 255));
		graph.setBackgroundColor(new ColorRGBA(0, 0, 0, 0));
		graph.setDataColor(new ColorRGBA(136, 170, 255));
		graph.setBaseYmin(0.0);


		List<FinanceLogEntry> entries = database.getTaskCtrl().getFinanceLogs();
		boolean foundEntry = false;
		String afterHtml = "";
		Date lastDate = null;
		Integer lastAmount = null;
		for (FinanceLogEntry entry : entries) {
			if (entry.getRows().size() > 0) {
				afterHtml += entry.createPanelInHtml(database);

				foundEntry = true;

				timeData.add(new GraphTimeDataPoint(entry.getDate(), entry.getTotalAmount()));

				if ((lastDate == null) || lastDate.before(entry.getDate())) {
					lastDate = entry.getDate();
					lastAmount = entry.getTotalAmount();
				}
			}
		}

		// let's add some days to the last date and add this as entry, such that we see a bit into the future
		// (this is necessary because we would otherwise cut off the current state due to smoothening)
		lastDate = DateUtils.addDays(lastDate, 15);
		timeData.add(new GraphTimeDataPoint(lastDate, lastAmount));

		// for-else:
		if (!foundEntry) {
			html += "<div>No finance logs have been found!</div>";
		} else {
			html += "<div><img src='finance_log_graph.png' /></div>";
			html += afterHtml;

			graph.setIncludeTodayInTimeData(true);
			graph.setAbsoluteTimeDataPoints(timeData);
			graph.smoothen(9);

			DefaultImageFile graphFile = new DefaultImageFile(AssAccountant.getWebRoot(), "finance_log_graph.png");
			graphFile.assign(graph);
			graphFile.saveTransparently();
		}

		html += "<div class='footer'>&nbsp;</div>";

		return html;
	}

	@Override
	public void createTabOnGUI(final JPanel parentPanel, final Database database, String searchFor) {

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

		CopyByClickLabel nameLabel = AccountingUtils.createHeadLabel(TITLE);
		topHUD.add(nameLabel, new Arrangement(0, 0, 1.0, 1.0));

		tab.add(topHUD, new Arrangement(0, i, 1.0, 0.0));
		i++;

		CopyByClickLabel curLabel;
		JPanel curPanel;


		List<GraphTimeDataPoint> timeData = new ArrayList<>();

		GraphImage graph = new GraphImage();
		graph.setBackgroundColor(new ColorRGBA(GUI.getBackgroundColor()));
		graph.setDataColor(new ColorRGBA(80, 0, 160));
		graph.setBaseYmin(0.0);
		ImagePanel graphPanel = new ImagePanel();
		graphPanel.setImage(graph);
		graphPanel.setMinimumHeight(500);
		tab.add(graphPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;


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

				timeData.add(new GraphTimeDataPoint(entry.getDate(), entry.getTotalAmount()));
			}
		}
		// for-else:
		if (!foundEntry) {
			curLabel = new CopyByClickLabel("No finance logs have been found!");
			tab.add(curLabel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}


		graph.setIncludeTodayInTimeData(true);
		graph.setAbsoluteTimeDataPoints(timeData);


		JPanel footer = new JPanel();
		footer.setBackground(GUI.getBackgroundColor());
		tab.add(footer, new Arrangement(0, i, 1.0, 1.0));
		i++;

		AccountingUtils.resetTabSize(tab, parentPanel);

		parentPanel.add(tab);
	}

	@Override
	public Directory exportCsvTo(Directory exportDir, Database database) {

		Directory resultDir = new Directory(exportDir, ServerRequestHandler.tabToLink(this));
		resultDir.clear();


		List<String> headlineCols = new ArrayList<>();
		headlineCols.add("Date");
		headlineCols.add("Total");

		CsvFileGerman csvFile = new CsvFileGerman(resultDir, "finances.csv");
		csvFile.setHeadLine(headlineCols);

		List<FinanceLogEntry> entries = database.getTaskCtrl().getFinanceLogs();

		for (FinanceLogEntry entry : entries) {
			if (entry.getRows().size() > 0) {
				List<String> cur = new ArrayList<>();
				cur.add(DateUtils.serializeDate(entry.getDate()));
				cur.add(CsvFileGerman.sanitizeForCsv(entry.getTotalAmount() / 100.0));
				csvFile.appendContent(cur);
			}
		}

		csvFile.save();

		return resultDir;
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
