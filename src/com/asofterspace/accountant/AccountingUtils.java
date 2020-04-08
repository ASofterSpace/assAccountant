/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.timespans.TimeSpan;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.Utils;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class AccountingUtils {

	public static String formatMoney(int amount, Currency currency) {

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
		curPanel.setLayout(new GridBagLayout());

		JLabel curLabel = new JLabel("");
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(0, 0, 0.03, 1.0));

		curLabel = new JLabel("");
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.03, 1.0));

		curLabel = new JLabel("Total: ");
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(2, 0, 0.5, 1.0));

		curLabel = new JLabel(AccountingUtils.formatMoney(totalBeforeTax, Currency.EUR));
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(3, 0, 0.1, 1.0));

		curLabel = new JLabel(AccountingUtils.formatMoney(totalTax, Currency.EUR));
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(4, 0, 0.1, 1.0));

		curLabel = new JLabel(AccountingUtils.formatMoney(totalAfterTax, Currency.EUR));
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(5, 0, 0.1, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(6, 0, 0.0, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(7, 0, 0.08, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(8, 0, 0.0, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(9, 0, 0.08, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(10, 0, 0.0, 1.0));

		return curPanel;
	}

	public static JPanel createOverviewPanelOnGUI(String text, int amount) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();

		JPanel curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());

		CopyByClickLabel curLabel = new CopyByClickLabel(text);
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(0, 0, 0.6, 1.0));

		curLabel = new CopyByClickLabel(AccountingUtils.formatMoney(amount, Currency.EUR));
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.4, 1.0));

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

		return i;
	}

	public static int createOverviewAndTaxInfo(TimeSpan timeSpan, JPanel tab, int i) {

		CopyByClickLabel taxInfoLabel = AccountingUtils.createSubHeadLabel("Overview and Tax Information:");
		tab.add(taxInfoLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		JPanel curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount earned without any taxes: ", timeSpan.getOutTotalBeforeTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount spent without any taxes: ", timeSpan.getInTotalBeforeTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount donated without any taxes: ", timeSpan.getDonTotalBeforeTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total deductible already paid VAT / Gesamte abziehbare Vorsteuerbeträge für USt: ", timeSpan.getInTotalTax() + timeSpan.getDonTotalTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		int remainVATpay = timeSpan.getOutTotalTax() - (timeSpan.getInTotalTax() + timeSpan.getDonTotalTax());
		if (remainVATpay < 0) {
			remainVATpay = 0;
		}
		curPanel = AccountingUtils.createOverviewPanelOnGUI("Remaining VAT advance payment / Verbleibende Umsatzsteuer-Vorauszahlung: ", remainVATpay);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		return i;
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

}
