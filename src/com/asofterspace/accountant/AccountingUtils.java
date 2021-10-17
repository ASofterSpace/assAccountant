/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.data.OutgoingOverviewData;
import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.tabs.Tab;
import com.asofterspace.accountant.timespans.TimeSpan;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.web.ServerRequestHandler;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.images.ColorRGB;
import com.asofterspace.toolbox.io.CsvFileGerman;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.StrUtils;
import com.asofterspace.toolbox.Utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class AccountingUtils {

	private static Database database;


	public static String createTotalPanelHtml(int totalBeforeTax, int totalTax, int totalAfterTax) {

		String html = "";

		html += "<div class='line'>";

		Color textColor = null;
		String tooltip = null;

		html += "<span style='width: 51%'>&nbsp;</span>";

		html += AccountingUtils.createLabelHtml("Total:", textColor, tooltip, "text-align: right; width: 5%;");
		html += AccountingUtils.createLabelHtml(database.formatMoney(totalBeforeTax, Currency.EUR), textColor, tooltip, "text-align: right; width: 8.5%;");
		html += AccountingUtils.createLabelHtml(database.formatMoney(totalTax, Currency.EUR), textColor, tooltip, "text-align: right; width: 7%;");
		html += AccountingUtils.createLabelHtml(database.formatMoney(totalAfterTax, Currency.EUR), textColor, tooltip, "text-align: right; width: 8.5%;");

		html += "</div>";

		return html;
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

		curLabel = new JLabel(database.formatMoney(totalBeforeTax, Currency.EUR));
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(4, 0, 0.1, 1.0));

		curLabel = new JLabel(database.formatMoney(totalTax, Currency.EUR));
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(5, 0, 0.1, 1.0));

		curLabel = new JLabel(database.formatMoney(totalAfterTax, Currency.EUR));
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

	private static List<String> createList(String text, int amount) {
		List<String> result = new ArrayList<>();
		result.add(CsvFileGerman.sanitizeForCsv(text));
		result.add(CsvFileGerman.sanitizeForCsv(amount / 100.0));
		return result;
	}

	public static String createOverviewPanelInHtml(String text, int amount) {

		String html = "<div class='line'>";
		html += AccountingUtils.createLabelHtml(text, null, "", "text-align: right; width: 50%;");
		html += AccountingUtils.createLabelHtml(database.formatMoney(amount, Currency.EUR), null, "", "text-align: left; width: 50%;");
		html += "</div>";
		return html;
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

		curLabel = new CopyByClickLabel(database.formatMoney(amount, Currency.EUR));
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.5, 1.0));

		return curPanel;
	}

	private static List<String> createList(String text1, int amount1, String text2, int amount2) {
		List<String> result = new ArrayList<>();
		result.add(CsvFileGerman.sanitizeForCsv(text1));
		result.add(CsvFileGerman.sanitizeForCsv(amount1 / 100.0));
		result.add(CsvFileGerman.sanitizeForCsv(text2));
		result.add(CsvFileGerman.sanitizeForCsv(amount2 / 100.0));
		return result;
	}

	public static String createOverviewPanelInHtml(String text1, int amount1, String text2, int amount2) {

		String html = "<div class='line'>";
		html += AccountingUtils.createLabelHtml(text1, null, "", "text-align: right; width: 50%;");
		html += AccountingUtils.createLabelHtml(database.formatMoney(amount1, Currency.EUR), null, "", "text-align: left; width: 10%;");
		html += AccountingUtils.createLabelHtml(text2, null, "", "text-align: right; width: 10%;");
		html += AccountingUtils.createLabelHtml(database.formatMoney(amount2, Currency.EUR), null, "", "text-align: left; width: 30%;");
		html += "</div>";
		return html;
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

		curLabel = new CopyByClickLabel(database.formatMoney(amount1, Currency.EUR));
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.1, 1.0));

		curLabel = new CopyByClickLabel(text2);
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(2, 0, 0.1, 1.0));

		curLabel = new CopyByClickLabel(database.formatMoney(amount2, Currency.EUR));
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(3, 0, 0.3, 1.0));

		return curPanel;
	}

	public static String createTimeSpanTabHtml(TimeSpan timeSpan, Database database, String searchFor) {

		String html = "";

		html += "<div class='secondaryTitle'>Incoming Payments - Sent Invoices:</div>";

		int totalBeforeTax = 0;
		int totalTax = 0;
		int totalAfterTax = 0;

		List<Incoming> incomings = timeSpan.getIncomings();
		for (Incoming cur : incomings) {
			if (cur.matches(searchFor)) {
				html += cur.createPanelHtml(database);
				totalBeforeTax += cur.getPreTaxAmount();
				totalAfterTax += cur.getPostTaxAmount();
			}
		}
		totalTax = totalAfterTax - totalBeforeTax;

		if ("".equals(searchFor)) {
			if ((totalBeforeTax != timeSpan.getInTotalBeforeTax()) ||
				(totalTax != timeSpan.getInTotalTax()) ||
				(totalAfterTax != timeSpan.getInTotalAfterTax())) {
				System.out.println("There was an incoming calculation mixup! Something is wrong! Cats and dogs! Oh no!");
			}
		}

		html += AccountingUtils.createTotalPanelHtml(totalBeforeTax, totalTax, totalAfterTax);


		html += "<div class='secondaryTitle'>Outgoing Payments - Received Invoices:</div>";

		totalBeforeTax = 0;
		totalTax = 0;
		totalAfterTax = 0;
		List<Outgoing> outgoings = timeSpan.getOutgoings();
		for (Outgoing cur : outgoings) {
			if (cur.matches(searchFor)) {
				html += cur.createPanelHtml(database);
				totalBeforeTax += cur.getPreTaxAmount();
				totalAfterTax += cur.getPostTaxAmount();
			}
		}
		totalTax = totalAfterTax - totalBeforeTax;

		if ("".equals(searchFor)) {
			if ((totalBeforeTax != timeSpan.getOutTotalBeforeTax()) ||
				(totalTax != timeSpan.getOutTotalTax()) ||
				(totalAfterTax != timeSpan.getOutTotalAfterTax())) {
				System.out.println("There was an outgoing calculation mixup! Something is wrong! Cats and dogs! Oh no!");
			}
		}

		html += AccountingUtils.createTotalPanelHtml(totalBeforeTax, totalTax, totalAfterTax);


		html += "<div class='secondaryTitle'>Outgoing Donations - Received Donation Invoices:</div>";

		totalBeforeTax = 0;
		totalTax = 0;
		totalAfterTax = 0;
		List<Outgoing> donations = timeSpan.getDonations();
		for (Outgoing cur : donations) {
			if (cur.matches(searchFor)) {
				html += cur.createPanelHtml(database);
				totalBeforeTax += cur.getPreTaxAmount();
				totalAfterTax += cur.getPostTaxAmount();
			}
		}
		totalTax = totalAfterTax - totalBeforeTax;

		if ("".equals(searchFor)) {
			if ((totalBeforeTax != timeSpan.getDonTotalBeforeTax()) ||
				(totalTax != timeSpan.getDonTotalTax()) ||
				(totalAfterTax != timeSpan.getDonTotalAfterTax())) {
				System.out.println("There was a donations calculation mixup! Something is wrong! Cats and dogs! Oh no!");
			}
		}

		html += AccountingUtils.createTotalPanelHtml(totalBeforeTax, totalTax, totalAfterTax);


		html += "<div class='secondaryTitle'>Outgoing Personal Expenses:</div>";

		totalBeforeTax = 0;
		totalTax = 0;
		totalAfterTax = 0;
		List<Outgoing> personals = timeSpan.getPersonals();
		for (Outgoing cur : personals) {
			if (cur.matches(searchFor)) {
				html += cur.createPanelHtml(database);
				totalBeforeTax += cur.getPreTaxAmount();
				totalAfterTax += cur.getPostTaxAmount();
			}
		}
		totalTax = totalAfterTax - totalBeforeTax;

		if ("".equals(searchFor)) {
			if ((totalBeforeTax != timeSpan.getPersTotalBeforeTax()) ||
				(totalTax != timeSpan.getPersTotalTax()) ||
				(totalAfterTax != timeSpan.getPersTotalAfterTax())) {
				System.out.println("There was a personals calculation mixup! Something is wrong! Cats and dogs! Oh no!");
			}
		}

		html += AccountingUtils.createTotalPanelHtml(totalBeforeTax, totalTax, totalAfterTax);


		return html;
	}

	public static int createTimeSpanTabMainContent(TimeSpan timeSpan, JPanel tab, int i, Database database, String searchFor) {

		CopyByClickLabel incomingLabel = AccountingUtils.createSubHeadLabel("Incoming Payments - Sent Invoices:");
		tab.add(incomingLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		JPanel curPanel;

		int totalBeforeTax = 0;
		int totalTax = 0;
		int totalAfterTax = 0;
		List<Incoming> incomings = timeSpan.getIncomings();
		for (Incoming cur : incomings) {
			if (cur.matches(searchFor)) {
				curPanel = cur.createPanelOnGUI(database);
				tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
				totalBeforeTax += cur.getPreTaxAmount();
				totalAfterTax += cur.getPostTaxAmount();
				i++;
			}
		}
		totalTax = totalAfterTax - totalBeforeTax;

		if ("".equals(searchFor)) {
			if ((totalBeforeTax != timeSpan.getInTotalBeforeTax()) ||
				(totalTax != timeSpan.getInTotalTax()) ||
				(totalAfterTax != timeSpan.getInTotalAfterTax())) {
				GuiUtils.complain("There was an incoming calculation mixup! Something is wrong! Cats and dogs! Oh no!");
			}
		}

		curPanel = AccountingUtils.createTotalPanelOnGUI(totalBeforeTax, totalTax, totalAfterTax);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;


		CopyByClickLabel outgoingLabel = AccountingUtils.createSubHeadLabel("Outgoing Payments - Received Invoices:");
		tab.add(outgoingLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		totalBeforeTax = 0;
		totalTax = 0;
		totalAfterTax = 0;
		List<Outgoing> outgoings = timeSpan.getOutgoings();
		for (Outgoing cur : outgoings) {
			if (cur.matches(searchFor)) {
				curPanel = cur.createPanelOnGUI(database);
				tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
				totalBeforeTax += cur.getPreTaxAmount();
				totalAfterTax += cur.getPostTaxAmount();
				i++;
			}
		}
		totalTax = totalAfterTax - totalBeforeTax;

		if ("".equals(searchFor)) {
			if ((totalBeforeTax != timeSpan.getOutTotalBeforeTax()) ||
				(totalTax != timeSpan.getOutTotalTax()) ||
				(totalAfterTax != timeSpan.getOutTotalAfterTax())) {
				GuiUtils.complain("There was an outgoing calculation mixup! Something is wrong! Cats and dogs! Oh no!");
			}
		}

		curPanel = AccountingUtils.createTotalPanelOnGUI(totalBeforeTax, totalTax, totalAfterTax);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;


		CopyByClickLabel donationLabel = AccountingUtils.createSubHeadLabel("Outgoing Donations - Received Donation Invoices::");
		tab.add(donationLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		totalBeforeTax = 0;
		totalTax = 0;
		totalAfterTax = 0;
		List<Outgoing> donations = timeSpan.getDonations();
		for (Outgoing cur : donations) {
			if (cur.matches(searchFor)) {
				curPanel = cur.createPanelOnGUI(database);
				tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
				totalBeforeTax += cur.getPreTaxAmount();
				totalAfterTax += cur.getPostTaxAmount();
				i++;
			}
		}
		totalTax = totalAfterTax - totalBeforeTax;

		if ("".equals(searchFor)) {
			if ((totalBeforeTax != timeSpan.getDonTotalBeforeTax()) ||
				(totalTax != timeSpan.getDonTotalTax()) ||
				(totalAfterTax != timeSpan.getDonTotalAfterTax())) {
				GuiUtils.complain("There was a donations calculation mixup! Something is wrong! Cats and dogs! Oh no!");
			}
		}

		curPanel = AccountingUtils.createTotalPanelOnGUI(totalBeforeTax, totalTax, totalAfterTax);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;


		CopyByClickLabel personalLabel = AccountingUtils.createSubHeadLabel("Outgoing Personal Expenses:");
		tab.add(personalLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		totalBeforeTax = 0;
		totalTax = 0;
		totalAfterTax = 0;
		List<Outgoing> personals = timeSpan.getPersonals();
		for (Outgoing cur : personals) {
			if (cur.matches(searchFor)) {
				curPanel = cur.createPanelOnGUI(database);
				tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
				totalBeforeTax += cur.getPreTaxAmount();
				totalAfterTax += cur.getPostTaxAmount();
				i++;
			}
		}
		totalTax = totalAfterTax - totalBeforeTax;

		if ("".equals(searchFor)) {
			if ((totalBeforeTax != timeSpan.getPersTotalBeforeTax()) ||
				(totalTax != timeSpan.getPersTotalTax()) ||
				(totalAfterTax != timeSpan.getPersTotalAfterTax())) {
				GuiUtils.complain("There was a personals calculation mixup! Something is wrong! Cats and dogs! Oh no!");
			}
		}

		curPanel = AccountingUtils.createTotalPanelOnGUI(totalBeforeTax, totalTax, totalAfterTax);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		return i;
	}

	public static List<List<String>> createOverviewAndTaxInfoCsvData(TimeSpan timeSpan) {

		List<List<String>> result = new ArrayList<>();

		result.add(createList("Total amount earned: ", timeSpan.getInTotalAfterTax(), "Of that VAT: ", timeSpan.getInTotalTax()));
		result.add(createList("Total amount spent: ", timeSpan.getOutTotalAfterTax(), "Of that VAT: ", timeSpan.getOutTotalTax()));
		result.add(createList("Total amount donated: ", timeSpan.getDonTotalAfterTax(), "Of that VAT: ", timeSpan.getDonTotalTax()));
		result.add(createList("Total amount of personal expenses: ", timeSpan.getPersTotalAfterTax(), "Of that VAT: ", timeSpan.getPersTotalTax()));

		if (timeSpan instanceof Year) {
			Year curYear = (Year) timeSpan;
			result.add(createList("ROUGHLY expected income tax payment: ", (int) curYear.getExpectedIncomeTax()));
			result.add(createList("Total amount earned in " + curYear.getNum() + ": ", (int) (timeSpan.getInTotalBeforeTax() - (timeSpan.getOutTotalBeforeTax() + timeSpan.getDonTotalBeforeTax() + timeSpan.getPersTotalBeforeTax() + curYear.getExpectedIncomeTax()))));
		}

		List<String> subList = new ArrayList<>();
		subList.add("");
		result.add(subList);
		subList = new ArrayList<>();
		subList.add("-------------- VAT / USt --------------");
		result.add(subList);

		result.add(createList("Total deductible already paid VAT / Gesamte abziehbare Vorsteuerbeträge: ", timeSpan.getDiscountablePreTax()));
		result.add(createList("Remaining VAT advance payment / Verbleibende Umsatzsteuer-Vorauszahlung: ", timeSpan.getRemainingVatPayments()));
		result.add(createList("Actual VAT advance payments made / An das Finanzamt abgeführte Umsatzsteuer: ", timeSpan.getVatPrepaymentsPaidTotal()));

		subList = new ArrayList<>();
		subList.add("");
		result.add(subList);
		subList = new ArrayList<>();
		subList.add("-------------- Income Tax / ESt --------------");
		result.add(subList);

		OutgoingOverviewData ood = new OutgoingOverviewData(timeSpan);

		result.add(createList("Total amount spent on items, raw materials etc.: ", ood.getWareCosts() + ood.getOtherCosts()));
		result.add(createList("Total external personnel and subcontractor costs: ", ood.getExternalSalary()));
		result.add(createList("Total internal personnel costs: ", ood.getInternalSalary()));
		result.add(createList("Total insurance costs: ", ood.getInsuranceCosts()));
		result.add(createList("Total vehicle costs: ", ood.getVehicleCosts()));
		result.add(createList("Total travel costs: ", ood.getTravelCosts()));
		result.add(createList("Total location / building costs: ", ood.getLocationCosts()));
		result.add(createList("Total education and conference costs: ", ood.getEducationCosts()));
		result.add(createList("Total amount spent on IT infrastructure: ", ood.getInfrastructureCosts()));
		result.add(createList("Total advertisement and branded item costs: ", ood.getAdvertisementCosts()));
		result.add(createList("Total entertainment (e.g. restaurant) costs: ", ood.getEntertainmentCosts()));

		return result;
	}

	public static String createOverviewAndTaxInfoHtml(TimeSpan timeSpan) {

		String html = "";

		html += "<div class='secondaryTitle'>Overview and Tax Information:</div>";

		html += AccountingUtils.createOverviewPanelInHtml("Total amount earned: ", timeSpan.getInTotalAfterTax(), "Of that VAT: ", timeSpan.getInTotalTax());
		html += AccountingUtils.createOverviewPanelInHtml("Total amount spent: ", timeSpan.getOutTotalAfterTax(), "Of that VAT: ", timeSpan.getOutTotalTax());
		html += AccountingUtils.createOverviewPanelInHtml("Total amount donated: ", timeSpan.getDonTotalAfterTax(), "Of that VAT: ", timeSpan.getDonTotalTax());
		html += AccountingUtils.createOverviewPanelInHtml("Total amount of personal expenses: ", timeSpan.getPersTotalAfterTax(), "Of that VAT: ", timeSpan.getPersTotalTax());

		if (timeSpan instanceof Year) {
			Year curYear = (Year) timeSpan;
			html += AccountingUtils.createOverviewPanelInHtml("ROUGHLY expected income tax payment: ", (int) curYear.getExpectedIncomeTax());
			html += AccountingUtils.createOverviewPanelInHtml("Total amount earned in " + curYear.getNum() + ": ", (int) (timeSpan.getInTotalBeforeTax() - (timeSpan.getOutTotalBeforeTax() + timeSpan.getDonTotalBeforeTax() + timeSpan.getPersTotalBeforeTax() + curYear.getExpectedIncomeTax())));
		}

		html += "<div style='text-align: center; padding-top: 5pt;'>-------------- VAT / USt --------------</div>";

		html += AccountingUtils.createOverviewPanelInHtml("Total deductible already paid VAT / Gesamte abziehbare Vorsteuerbeträge: ", timeSpan.getDiscountablePreTax());
		html += AccountingUtils.createOverviewPanelInHtml("Remaining VAT advance payment / Verbleibende Umsatzsteuer-Vorauszahlung: ", timeSpan.getRemainingVatPayments());
		html += AccountingUtils.createOverviewPanelInHtml("Actual VAT advance payments made / An das Finanzamt abgeführte Umsatzsteuer: ", timeSpan.getVatPrepaymentsPaidTotal());

		html += "<div style='text-align: center; padding-top: 5pt;'>-------------- Income Tax / ESt --------------</div>";

		OutgoingOverviewData ood = new OutgoingOverviewData(timeSpan);

		html += AccountingUtils.createOverviewPanelInHtml("Total amount spent on items, raw materials etc.: ", ood.getWareCosts() + ood.getOtherCosts());
		html += AccountingUtils.createOverviewPanelInHtml("Total external personnel and subcontractor costs: ", ood.getExternalSalary());
		html += AccountingUtils.createOverviewPanelInHtml("Total internal personnel costs: ", ood.getInternalSalary());
		html += AccountingUtils.createOverviewPanelInHtml("Total insurance costs: ", ood.getInsuranceCosts());
		html += AccountingUtils.createOverviewPanelInHtml("Total vehicle costs: ", ood.getVehicleCosts());
		html += AccountingUtils.createOverviewPanelInHtml("Total travel costs: ", ood.getTravelCosts());
		html += AccountingUtils.createOverviewPanelInHtml("Total location / building costs: ", ood.getLocationCosts());
		html += AccountingUtils.createOverviewPanelInHtml("Total education and conference costs: ", ood.getEducationCosts());
		html += AccountingUtils.createOverviewPanelInHtml("Total amount spent on IT infrastructure: ", ood.getInfrastructureCosts());
		html += AccountingUtils.createOverviewPanelInHtml("Total advertisement and branded item costs: ", ood.getAdvertisementCosts());
		html += AccountingUtils.createOverviewPanelInHtml("Total entertainment (e.g. restaurant) costs: ", ood.getEntertainmentCosts());

		return html;
	}

	public static int createOverviewAndTaxInfo(TimeSpan timeSpan, JPanel tab, int i) {

		CopyByClickLabel taxInfoLabel = AccountingUtils.createSubHeadLabel("Overview and Tax Information:");
		tab.add(taxInfoLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		JPanel curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount earned: ", timeSpan.getInTotalAfterTax(), "Of that VAT: ", timeSpan.getInTotalTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount spent: ", timeSpan.getOutTotalAfterTax(), "Of that VAT: ", timeSpan.getOutTotalTax());
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

			curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount earned in " + curYear.getNum() + ": ", (int) (timeSpan.getInTotalBeforeTax() - (timeSpan.getOutTotalBeforeTax() + timeSpan.getDonTotalBeforeTax() + timeSpan.getPersTotalBeforeTax() + curYear.getExpectedIncomeTax())));
			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}

		JLabel sep = new JLabel("-------------- VAT / USt --------------");
		sep.setHorizontalAlignment(JLabel.CENTER);
		tab.add(sep, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total deductible already paid VAT / Gesamte abziehbare Vorsteuerbeträge: ", timeSpan.getDiscountablePreTax());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Remaining VAT advance payment / Verbleibende Umsatzsteuer-Vorauszahlung: ", timeSpan.getRemainingVatPayments());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Actual VAT advance payments made / Umsatzsteuer-Vorauszahlungssoll: ", timeSpan.getVatPrepaymentsPaidTotal());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		sep = new JLabel("-------------- Income Tax / ESt --------------");
		sep.setHorizontalAlignment(JLabel.CENTER);
		tab.add(sep, new Arrangement(0, i, 1.0, 0.0));
		i++;

		OutgoingOverviewData ood = new OutgoingOverviewData(timeSpan);

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount spent on items, raw materials etc.: ", ood.getWareCosts() + ood.getOtherCosts());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total external personnel and subcontractor costs: ", ood.getExternalSalary());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total internal personnel costs: ", ood.getInternalSalary());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total insurance costs: ", ood.getInsuranceCosts());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total vehicle costs: ", ood.getVehicleCosts());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total travel costs: ", ood.getTravelCosts());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total location / building costs: ", ood.getLocationCosts());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total education and conference costs: ", ood.getEducationCosts());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total amount spent on IT infrastructure: ", ood.getInfrastructureCosts());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total advertisement and branded item costs: ", ood.getAdvertisementCosts());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curPanel = AccountingUtils.createOverviewPanelOnGUI("Total entertainment (e.g. restaurant) costs: ", ood.getEntertainmentCosts());
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		return i;
	}

	public static String createLabelHtml(String text, Color color, String tooltip, String style) {

		if (text == null) {
			text = "";
		}

		// we need a tiny bit more space in the columns, so let's print € instead of EUR everywhere!
		if (text.endsWith(" EUR")) {
			text = text.substring(0, text.length() - 4) + " €";
		}

		text = StrUtils.replaceAll(text, " ", "&nbsp;");

		// default color
		String colStr = "#88AAFF";
		if (color != null) {
			ColorRGB col = new ColorRGB(color);
			if (!"#000000".equals(col.toHexString())) {
				colStr = col.toHexString();
			}
			// all is awesome color
			if ("#009C00".equals(colStr)) {
				colStr = "#20CC00";
			}
			// warning color
			if ("#949400".equals(colStr)) {
				colStr = "#D4B430";
			}
			// error color
			if ("#C40000".equals(colStr)) {
				colStr = "#FF4020";
			}
		}

		String altStr = "";
		if ((tooltip != null) && !"".equals(tooltip)) {
			tooltip = StrUtils.replaceAll(tooltip, "\"", "");
			tooltip = StrUtils.replaceAll(tooltip, "'", "");
			altStr = " title='" + tooltip + "'";
		}

		return "<span style='color: " + colStr + "; " + style + "'" + altStr + ">" + text + "</span>";
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

	public static String getEntryForLog(Entry entry) {
		String result = "";
		if (entry instanceof Outgoing) {
			result += "outgoing payment (received invoice) ";
		}
		if (entry instanceof Incoming) {
			result += "incoming payment (sent invoice) ";
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
		newHeight += 600;
		Dimension newSize = new Dimension(parentPanel.getWidth(), newHeight);
		tab.setPreferredSize(newSize);
		parentPanel.setPreferredSize(newSize);
	}

	public static MouseAdapter getRowHighlighter(final JPanel curPanel) {
		return new MouseAdapter() {
			public void mouseExited(MouseEvent e) {
				curPanel.setBackground(new Color(255, 255, 255));
			}
			public void mouseEntered(MouseEvent e) {
				curPanel.setBackground(new Color(235, 215, 255));
			}
		};
	}

	public static Directory exportTimeSpanCsvTo(Directory exportDir, Database database, TimeSpan timeSpan, Tab tab) {

		Directory resultDir = new Directory(exportDir, ServerRequestHandler.tabToLink(tab));
		resultDir.clear();


		List<String> headlineCols = new ArrayList<>();
		headlineCols.add("Date");
		headlineCols.add("Title");
		headlineCols.add("Customer");
		headlineCols.add("Pre Tax Amount");
		headlineCols.add("Tax Percent");
		headlineCols.add("Post Tax Amount");
		headlineCols.add("Received On");

		CsvFileGerman csvFile = new CsvFileGerman(resultDir, "incoming.csv");
		csvFile.setHeadLine(headlineCols);

		List<Incoming> curIncomings = timeSpan.getIncomings();
		for (Incoming cur : curIncomings) {
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

		csvFile = new CsvFileGerman(resultDir, "outgoing.csv");
		csvFile.setHeadLine(headlineCols);

		List<Outgoing> curEntries = timeSpan.getOutgoings();
		for (Outgoing cur : curEntries) {
			csvFile.appendContent(cur.createCsvLine(database));
		}

		csvFile.save();


		csvFile = new CsvFileGerman(resultDir, "donations.csv");
		csvFile.setHeadLine(headlineCols);

		curEntries = timeSpan.getDonations();
		for (Outgoing cur : curEntries) {
			csvFile.appendContent(cur.createCsvLine(database));
		}

		csvFile.save();


		csvFile = new CsvFileGerman(resultDir, "personals.csv");
		csvFile.setHeadLine(headlineCols);

		curEntries = timeSpan.getPersonals();
		for (Outgoing cur : curEntries) {
			csvFile.appendContent(cur.createCsvLine(database));
		}

		csvFile.save();


		csvFile = new CsvFileGerman(resultDir, "tax_info.csv");

		csvFile.clearContent();

		List<List<String>> taxInfos = AccountingUtils.createOverviewAndTaxInfoCsvData(timeSpan);
		for (List<String> taxInfo : taxInfos) {
			csvFile.appendContent(taxInfo);
		}

		csvFile.save();


		return resultDir;
	}

	public static void setDatabase(Database databaseArg) {
		database = databaseArg;
	}

	public static Database getDatabase() {
		return database;
	}

	public static String getMonthLink(Date date) {
		if (date == null) {
			return "/overview";
		}

		return "/month_" + DateUtils.getYear(date) + "_" +
				DateUtils.getMonthNameEN(date).toLowerCase();
	}
}
