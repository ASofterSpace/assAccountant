/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.AssAccountant;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.transactions.BankAccount;
import com.asofterspace.accountant.transactions.BankTransaction;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.accounting.FinanceUtils;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.guiImages.ImagePanel;
import com.asofterspace.toolbox.images.ColorRGB;
import com.asofterspace.toolbox.images.DefaultImageFile;
import com.asofterspace.toolbox.images.GraphImage;
import com.asofterspace.toolbox.images.GraphTimeDataPoint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class BankStatementYearTab extends Tab {

	private JPanel tab;

	private Year year;

	private String title;


	public BankStatementYearTab(Year year) {
		this.year = year;

		this.title = "Bank Statements";
		if (year != null) {
			this.title = year.getNum() + " Bank Statements";
		}
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "<div class='mainTitle'>" + title + "</div>";

		Color textColor = new Color(0, 0, 0);


		List<GraphTimeDataPoint> timeData = new ArrayList<>();

		GraphImage graph = new GraphImage(1000, 600);
		graph.setForegroundColor(new ColorRGB(136, 170, 255));
		graph.setBackgroundColor(new ColorRGB(0, 0, 0, 0));
		graph.setDataColor(new ColorRGB(136, 170, 255));
		graph.setBaseYmin(0.0);

		html += "<div><img src='bank_statement_graph.png' /></div>";


		List<BankAccount> accounts = database.getBankAccounts();

		for (BankAccount account : accounts) {
			html += "<div>";
			html += "<div class='secondaryTitle'>" + account.getBank() + ", IBAN: " + account.getIban() + ", BIC: " + account.getBic() + "</div>";

			List<BankTransaction> transactions = account.getTransactions();
			boolean foundSome = false;
			int total = 0;
			for (BankTransaction transaction : transactions) {
				if (transaction.belongsTo(year) && transaction.matches(searchFor)) {
					foundSome = true;
					total += transaction.getAmount();
					timeData.add(new GraphTimeDataPoint(transaction.getDate(), transaction.getAmount()));
					html += transaction.createPanelInHtml(database);
				}
			}
			if (foundSome) {
				html += "<div class='line'>";

				html += AccountingUtils.createLabelHtml("Total, assuming +/- 0 at the top:", textColor, "", "text-align: right; width: 80%;");
				html += AccountingUtils.createLabelHtml(FinanceUtils.formatMoney(total, Currency.EUR), textColor, "", "text-align: right; width: 10%;");

				html += "</div>";

			} else {
				// for-else:
				html += "<div>For this account, no bank statements have been found!</div>";
			}

			html += "</div>";
		}
		// for-else:
		if (accounts.size() < 1) {
			html += "<div>No bank statements have been found!</div>";
		}


		graph.setIncludeTodayInTimeData(year == null);
		graph.setRelativeTimeDataPoints(timeData);

		double graphMin = graph.getMinimumValue();
		if (graphMin < 0) {
			graph.shiftValues(-graphMin);
		}

		DefaultImageFile graphFile = new DefaultImageFile(AssAccountant.getWebRoot(), "bank_statement_graph.png");
		graphFile.assign(graph);
		graphFile.saveTransparently();

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

		CopyByClickLabel nameLabel = AccountingUtils.createHeadLabel(title);
		topHUD.add(nameLabel, new Arrangement(0, 0, 1.0, 0.0));

		tab.add(topHUD, new Arrangement(0, i, 1.0, 0.0));
		i++;

		CopyByClickLabel curLabel;
		Color textColor = new Color(0, 0, 0);
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


		List<BankAccount> accounts = database.getBankAccounts();

		for (BankAccount account : accounts) {
			curLabel = AccountingUtils.createSubHeadLabel(
				account.getBank() + ", IBAN: " + account.getIban() + ", BIC: " + account.getBic()
			);
			tab.add(curLabel, new Arrangement(0, i, 1.0, 0.0));
			i++;

			List<BankTransaction> transactions = account.getTransactions();
			boolean foundSome = false;
			int total = 0;
			for (BankTransaction transaction : transactions) {
				if (transaction.belongsTo(year) && transaction.matches(searchFor)) {
					foundSome = true;
					total += transaction.getAmount();
					timeData.add(new GraphTimeDataPoint(transaction.getDate(), transaction.getAmount()));
					curPanel = transaction.createPanelOnGUI(database);
					tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
					i++;

					Dimension newSize = new Dimension(parentPanel.getWidth(), curPanel.getMinimumSize().height);
					curPanel.setPreferredSize(newSize);
				}
			}
			if (foundSome) {
				curPanel = new JPanel();
				curPanel.setBackground(GUI.getBackgroundColor());
				curPanel.setLayout(new GridBagLayout());

				curLabel = new CopyByClickLabel("");
				curLabel.setPreferredSize(defaultDimension);
				curPanel.add(curLabel, new Arrangement(0, 0, 0.1, 0.0));

				curLabel = AccountingUtils.createLabel("Total, assuming +/- 0 at the top:", textColor, "");
				curLabel.setHorizontalAlignment(JLabel.RIGHT);
				curPanel.add(curLabel, new Arrangement(1, 0, 0.7, 0.0));

				curLabel = new CopyByClickLabel(FinanceUtils.formatMoney(total, Currency.EUR));
				curLabel.setHorizontalAlignment(JLabel.RIGHT);
				curLabel.setPreferredSize(defaultDimension);
				curPanel.add(curLabel, new Arrangement(2, 0, 0.1, 0.0));

				curLabel = new CopyByClickLabel("");
				curLabel.setPreferredSize(defaultDimension);
				curPanel.add(curLabel, new Arrangement(3, 0, 0.0, 0.0));

				curLabel = new CopyByClickLabel("");
				curLabel.setMinimumSize(defaultDimension);
				curLabel.setPreferredSize(defaultDimension);
				curPanel.add(curLabel, new Arrangement(4, 0, 0.1, 0.0));

				curLabel = new CopyByClickLabel("");
				curLabel.setPreferredSize(defaultDimension);
				curPanel.add(curLabel, new Arrangement(5, 0, 0.0, 0.0));

				tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
				i++;

			} else {
				// for-else:
				curLabel = new CopyByClickLabel("For this account, no bank statements have been found!");
				tab.add(curLabel, new Arrangement(0, i, 1.0, 0.0));
				i++;
			}
		}
		// for-else:
		if (accounts.size() < 1) {
			curLabel = new CopyByClickLabel("No bank statements have been found!");
			tab.add(curLabel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}


		graph.setIncludeTodayInTimeData(year == null);
		graph.setRelativeTimeDataPoints(timeData);

		double graphMin = graph.getMinimumValue();
		if (graphMin < 0) {
			graph.shiftValues(-graphMin);
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
	public int getComparisonOrder() {
		return getYear().getNum() * 100;
	}

	public Year getYear() {
		return year;
	}

	@Override
	public String toString() {
		return title;
	}

}
