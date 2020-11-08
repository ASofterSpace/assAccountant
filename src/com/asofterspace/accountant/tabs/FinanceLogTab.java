/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.tasks.FinanceLogEntry;
import com.asofterspace.accountant.tasks.Task;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.guiImages.ImagePanel;
import com.asofterspace.toolbox.images.ColorRGB;
import com.asofterspace.toolbox.images.GraphImage;
import com.asofterspace.toolbox.images.GraphTimeDataPoint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;


public class FinanceLogTab extends Tab {

	private static final String TITLE = "Finance Log";

	private JPanel tab;


	public FinanceLogTab() {
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		// TODO - everything

		String html = "";

		html += "<div class='footer'>&nbsp;</div>";

		return html;
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


		List<GraphTimeDataPoint> timeData = new ArrayList<>();

		GraphImage graph = new GraphImage();
		graph.setBackgroundColor(new ColorRGB(GUI.getBackgroundColor()));
		graph.setDataColor(new ColorRGB(80, 0, 160));
		graph.setBaseYmin(0.0);
		ImagePanel graphPanel = new ImagePanel(graph);
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
