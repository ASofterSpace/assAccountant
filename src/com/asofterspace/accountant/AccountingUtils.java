/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.timespans.TimeSpan;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.world.Category;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.Utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class AccountingUtils {

	public static String formatMoney(Integer amount, Currency currency) {

		if (amount == null) {
			return "N/A";
		}

		String result = "" + amount;

		// 1 to 001
		while (result.length() < 3) {
			result = "0" + result;
		}

		// 001 to 0.01
		result = result.substring(0, result.length() - 2) + "." + result.substring(result.length() - 2);

		// 2739.80 to 2,739.80
		if (result.length() > 6) {
			result = result.substring(0, result.length() - 6) + "," + result.substring(result.length() - 6);
		}
		// 2739,800.00 to 2,739,800.00
		if (result.length() > 10) {
			result = result.substring(0, result.length() - 10) + "," + result.substring(result.length() - 10);
		}

		// 0.01 to 0.01 EUR
		return result + " " + currency;
	}

	public static JPanel createTotalPanelOnGUI(int totalBeforeTax, int totalTax, int totalAfterTax) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();

		JPanel curPanel = new JPanel();
		curPanel.setBackground(GUI.getBackgroundColor());
		curPanel.setLayout(new GridBagLayout());

		JLabel curLabel = new JLabel("");
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(0, 0, 0.03, 1.0));

		curLabel = new JLabel("");
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.03, 1.0));

		curLabel = new JLabel("");
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(2, 0, 0, 1.0));

		curLabel = new JLabel("Total: ");
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(3, 0, 0.5, 1.0));

		curLabel = new JLabel(AccountingUtils.formatMoney(totalBeforeTax, Currency.EUR));
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(4, 0, 0.1, 1.0));

		curLabel = new JLabel(AccountingUtils.formatMoney(totalTax, Currency.EUR));
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(5, 0, 0.1, 1.0));

		curLabel = new JLabel(AccountingUtils.formatMoney(totalAfterTax, Currency.EUR));
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(6, 0, 0.1, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(7, 0, 0.0, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(8, 0, 0.08, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(9, 0, 0.0, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(10, 0, 0.08, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(11, 0, 0.0, 1.0));

		return curPanel;
	}

	public static JPanel createOverviewPanelOnGUI(String text, int amount) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();

		JPanel curPanel = new JPanel();
		curPanel.setBackground(GUI.getBackgroundColor());
		curPanel.setLayout(new GridBagLayout());

		CopyByClickLabel curLabel = new CopyByClickLabel(text);
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(0, 0, 0.5, 1.0));

		curLabel = new CopyByClickLabel(AccountingUtils.formatMoney(amount, Currency.EUR));
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.5, 1.0));

		return curPanel;
	}

	public static JPanel createOverviewPanelOnGUI(String text1, int amount1, String text2, int amount2) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();

		JPanel curPanel = new JPanel();
		curPanel.setBackground(GUI.getBackgroundColor());
		curPanel.setLayout(new GridBagLayout());

		CopyByClickLabel curLabel = new CopyByClickLabel(text1);
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(0, 0, 0.5, 1.0));

		curLabel = new CopyByClickLabel(AccountingUtils.formatMoney(amount1, Currency.EUR));
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.1, 1.0));

		curLabel = new CopyByClickLabel(text2);
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(2, 0, 0.1, 1.0));

		curLabel = new CopyByClickLabel(AccountingUtils.formatMoney(amount2, Currency.EUR));
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(3, 0, 0.3, 1.0));

		return curPanel;
	}

	public static int createTimeSpanTabMainContent(TimeSpan timeSpan, JPanel tab, int i, Database database) {

		CopyByClickLabel outgoingLabel = AccountingUtils.createSubHeadLabel("Outgoing Invoices - that is, we get paid:");
		tab.add(outgoingLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		JPanel curPanel;

		List<Outgoing> outgoings = timeSpan.getOutgoings();
		for (Outgoing cur : outgoings) {
			curPanel = cur.createPanelOnGUI(database);
			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}

		curPanel = AccountingUtils.createTotalPanelOnGUI(timeSpan.getOutTotalBeforeTax(), timeSpan.getOutTotalTax(), timeSpan.getOutTotalAfterTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;


		CopyByClickLabel incomingLabel = AccountingUtils.createSubHeadLabel("Incoming Invoices - that is, we have to pay:");
		tab.add(incomingLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		List<Incoming> incomings = timeSpan.getIncomings();
		for (Incoming cur : incomings) {
			curPanel = cur.createPanelOnGUI(database);
			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}

		curPanel = AccountingUtils.createTotalPanelOnGUI(timeSpan.getInTotalBeforeTax(), timeSpan.getInTotalTax(), timeSpan.getInTotalAfterTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;


		CopyByClickLabel donationLabel = AccountingUtils.createSubHeadLabel("Donations - which we also pay:");
		tab.add(donationLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		List<Incoming> donations = timeSpan.getDonations();
		for (Incoming cur : donations) {
			curPanel = cur.createPanelOnGUI(database);
			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}

		curPanel = AccountingUtils.createTotalPanelOnGUI(timeSpan.getDonTotalBeforeTax(), timeSpan.getDonTotalTax(), timeSpan.getDonTotalAfterTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;


		CopyByClickLabel personalLabel = AccountingUtils.createSubHeadLabel("Personal Expenses - which we also pay:");
		tab.add(personalLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		List<Incoming> personals = timeSpan.getPersonals();
		for (Incoming cur : personals) {
			curPanel = cur.createPanelOnGUI(database);
			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}

		curPanel = AccountingUtils.createTotalPanelOnGUI(timeSpan.getPersTotalBeforeTax(), timeSpan.getPersTotalTax(), timeSpan.getPersTotalAfterTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		return i;
	}

	public static int createOverviewAndTaxInfo(TimeSpan timeSpan, JPanel tab, int i) {

		CopyByClickLabel taxInfoLabel = AccountingUtils.createSubHeadLabel("Overview and Tax Information:");
		tab.add(taxInfoLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		JPanel curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount earned: ", timeSpan.getOutTotalAfterTax(), "Of that VAT: ", timeSpan.getOutTotalTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount spent: ", timeSpan.getInTotalAfterTax(), "Of that VAT: ", timeSpan.getInTotalTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount donated: ", timeSpan.getDonTotalAfterTax(), "Of that VAT: ", timeSpan.getDonTotalTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount of personal expenses: ", timeSpan.getPersTotalAfterTax(), "Of that VAT: ", timeSpan.getPersTotalTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		if (timeSpan instanceof Year) {

			Year curYear = (Year) timeSpan;

			curPanel = AccountingUtils.createOverviewPanelOnGUI("ROUGHLY expected income tax payment: ", (int) curYear.getExpectedIncomeTax());
			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;

			curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount earned in " + curYear.getNum() + ": ", (int) (timeSpan.getOutTotalBeforeTax() - (timeSpan.getInTotalBeforeTax() + timeSpan.getDonTotalBeforeTax() + timeSpan.getPersTotalBeforeTax() + curYear.getExpectedIncomeTax())));
			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}

		JLabel sep = new JLabel("-------------- VAT / USt --------------");
		sep.setHorizontalAlignment(JLabel.CENTER);
		tab.add(sep, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total deductible already paid VAT / Gesamte abziehbare Vorsteuerbeträge: ", timeSpan.getInTotalTax() + timeSpan.getDonTotalTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		int remainVATpay = timeSpan.getOutTotalTax() - (timeSpan.getInTotalTax() + timeSpan.getDonTotalTax());
		if (remainVATpay < 0) {
			remainVATpay = 0;
		}
		curPanel = AccountingUtils.createOverviewPanelOnGUI("Remaining VAT advance payment / Verbleibende Umsatzsteuer-Vorauszahlung: ", remainVATpay);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		sep = new JLabel("-------------- Income Tax / ESt --------------");
		sep.setHorizontalAlignment(JLabel.CENTER);
		tab.add(sep, new Arrangement(0, i, 1.0, 0.0));
		i++;

		int externalSalary = timeSpan.getInTotalBeforeTax(Category.EXTERNAL_SALARY);
		int internalSalary = timeSpan.getInTotalBeforeTax(Category.INTERNAL_SALARY);
		int vehicleCosts = timeSpan.getInTotalBeforeTax(Category.VEHICLE);
		int travelCosts = timeSpan.getInTotalBeforeTax(Category.TRAVEL);
		int locationCosts = timeSpan.getInTotalBeforeTax(Category.LOCATIONS);
		int educationCosts = timeSpan.getInTotalBeforeTax(Category.EDUCATION);
		int advertisementCosts = timeSpan.getInTotalBeforeTax(Category.ADVERTISEMENTS);
		int infrastructureCosts = timeSpan.getInTotalBeforeTax(Category.INFRASTRUCTURE);
		int entertainmentCosts = timeSpan.getInTotalBeforeTax(Category.ENTERTAINMENT);

		// this does NOT include donations, as we will not get donation amounts from timeSpan.getInTotalBeforeTax() anyway, so we do not want to subtract them from it!
		int categoryTally = externalSalary + internalSalary + vehicleCosts + travelCosts + locationCosts +
			educationCosts + advertisementCosts + infrastructureCosts + entertainmentCosts;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount spent on items, raw materials etc.: ", timeSpan.getInTotalBeforeTax() - categoryTally);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total external personnel and subcontractor costs: ", externalSalary);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total internal personnel costs: ", internalSalary);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total vehicle costs: ", vehicleCosts);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total travel costs: ", travelCosts);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total location / building costs: ", locationCosts);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total education and conference costs: ", educationCosts);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount spent on IT infrastructure: ", infrastructureCosts);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total advertisement and branded item costs: ", advertisementCosts);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total entertainment (e.g. restaurant) costs: ", entertainmentCosts);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		return i;
	}

	public static CopyByClickLabel createLabel(String text, Color color, String tooltip) {

		CopyByClickLabel result = new CopyByClickLabel(text);

		if (!"".equals(tooltip)) {
			result.setToolTipText(tooltip);
		}

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();
		result.setPreferredSize(defaultDimension);

		result.setForeground(color);

		return result;
	}

	public static CopyByClickLabel createHeadLabel(String text) {
		CopyByClickLabel result = new CopyByClickLabel(text);
		result.setFont(new Font("Calibri", Font.PLAIN, 24));
		result.setPreferredSize(new Dimension(0, result.getPreferredSize().height*2));
		result.setHorizontalAlignment(CopyByClickLabel.CENTER);
		return result;
	}

	public static CopyByClickLabel createSubHeadLabel(String text) {
		CopyByClickLabel result = new CopyByClickLabel(text);
		result.setFont(new Font("Calibri", Font.PLAIN, 20));
		result.setPreferredSize(new Dimension(0, result.getPreferredSize().height*2));
		result.setHorizontalAlignment(CopyByClickLabel.CENTER);
		result.setVerticalAlignment(CopyByClickLabel.BOTTOM);
		return result;
	}

	public static boolean complain(String complainAbout) {

		JOptionPane.showMessageDialog(
			null,
			complainAbout,
			Utils.getProgramTitle(),
			JOptionPane.ERROR_MESSAGE
		);

		// we return false, which can then immediately be returned by the caller
		return false;
	}

	public static String getEntryForLog(Entry entry) {
		String result = "";
		if (entry instanceof Incoming) {
			result += "incoming ";
		}
		if (entry instanceof Outgoing) {
			result += "outgoing ";
		}
		result += "'" + entry.getTitle() + "' from " + entry.getDateAsText();
		return result;
	}

	public static Integer parseTaxes(String taxationPercentStr) {

		if ("".equals(taxationPercentStr)) {
			return 0;
		}

		taxationPercentStr = taxationPercentStr.replaceAll(" ", "");
		taxationPercentStr = taxationPercentStr.replaceAll("%", "");
		taxationPercentStr = taxationPercentStr.replaceAll("€", "");
		if (taxationPercentStr.contains(".")) {
			taxationPercentStr = taxationPercentStr.substring(0, taxationPercentStr.indexOf("."));
		}
		if (taxationPercentStr.contains(",")) {
			taxationPercentStr = taxationPercentStr.substring(0, taxationPercentStr.indexOf(","));
		}
		try {
			return Integer.parseInt(taxationPercentStr);
		} catch (NullPointerException | NumberFormatException e) {
			// we will just return null and let the caller complain to the user (if necessary)
		}

		return null;
	}

	public static Integer calcPostTax(Integer amount, Integer taxationPercent) {
		if ((amount == null) || (taxationPercent == null)) {
			return null;
		}
		return (int) Math.round((amount * (100 + taxationPercent)) / 100.0);
	}

	/**
	 * Sort entries descending, so latest / highest at the top
	 */
	public static void sortEntries(List<? extends Entry> entries) {
		Collections.sort(entries, new Comparator<Entry>() {
			public int compare(Entry a, Entry b) {
				// break ties using the title (such that a higher number in a title gets sorted to the top)
				if (a.getDate().equals(b.getDate())) {
					return b.getTitle().compareTo(a.getTitle());
				}
				// usually, compare using the date
				return b.getDate().compareTo(a.getDate());
			}
		});
	}

	public static void resetTabSize(JPanel tab, JPanel parentPanel) {
		int newHeight = parentPanel.getParent().getHeight();
		if (tab.getMinimumSize().height + 100 > newHeight) {
			newHeight = tab.getMinimumSize().height + 100;
		}
		Dimension newSize = new Dimension(parentPanel.getWidth(), newHeight);
		tab.setPreferredSize(newSize);
		parentPanel.setPreferredSize(newSize);
	}
}
