/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.entries;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.AddEntryGUI;
import com.asofterspace.accountant.AddPaidGUI;
import com.asofterspace.accountant.ConsistencyProblem;
import com.asofterspace.accountant.ConsistencyWarning;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.PaymentProblem;
import com.asofterspace.accountant.Problem;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.accounting.FinanceUtils;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.Utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;


public abstract class Entry {

	private static final String PRE_TAX_AMOUNT_KEY = "amount";
	private static final String POST_TAX_AMOUNT_KEY = "postTaxAmount";
	private static final String CURRENCY_KEY = "currency";
	private static final String TAXATION_PERCENT = "taxationPercent";
	private static final String DATE_KEY = "date";
	private static final String TITLE_KEY = "title";
	private static final String ORIGINATOR = "originator";
	private static final String RECEIVED_KEY = "received";
	private static final String RECEIVED_ON_DATE_KEY = "receivedOnDate";
	private static final String RECEIVED_ON_ACCOUNT_KEY = "receivedOnAccount";

	private Integer preTaxAmount;

	private Integer postTaxAmount;

	private Currency currency;

	private Integer taxationPercent;

	private Date date;

	private String title;

	private String originator;

	private boolean received;

	private Date receivedOnDate;

	private String receivedOnAccount;

	protected Month parent;


	/**
	 * Create an entry at runtime
	 */
	public Entry(Integer preTaxAmount, Currency currency, Integer taxationPercent, Integer postTaxAmount,
		Date date, String title, String originator, Month parent) {

		this.preTaxAmount = preTaxAmount;

		this.postTaxAmount = postTaxAmount;

		this.currency = currency;

		this.taxationPercent = taxationPercent;

		this.date = date;

		this.title = title;

		this.originator = originator;

		this.parent = parent;

		this.received = false;
	}

	/**
	 * Load an entry from a generic record
	 */
	public Entry(Record entryRecord, Month parent) {

		this.preTaxAmount = entryRecord.getInteger(PRE_TAX_AMOUNT_KEY);

		this.postTaxAmount = entryRecord.getInteger(POST_TAX_AMOUNT_KEY);

		this.currency = Currency.valueOf(entryRecord.getString(CURRENCY_KEY));

		this.taxationPercent = entryRecord.getInteger(TAXATION_PERCENT);

		this.date = DateUtils.parseDate(entryRecord.getString(DATE_KEY));

		this.title = entryRecord.getString(TITLE_KEY);

		this.originator = entryRecord.getString(ORIGINATOR);

		this.received = entryRecord.getBoolean(RECEIVED_KEY, false);

		this.receivedOnDate = null;
		String dateStr = entryRecord.getString(RECEIVED_ON_DATE_KEY);
		if (dateStr != null) {
			this.receivedOnDate = DateUtils.parseDate(dateStr);
		}

		this.receivedOnAccount = entryRecord.getString(RECEIVED_ON_ACCOUNT_KEY);

		this.parent = parent;
	}

	public Record toRecord() {

		Record result = new Record();

		result.set(PRE_TAX_AMOUNT_KEY, preTaxAmount);

		result.set(POST_TAX_AMOUNT_KEY, postTaxAmount);

		result.set(CURRENCY_KEY, currency);

		result.set(TAXATION_PERCENT, taxationPercent);

		result.set(DATE_KEY, DateUtils.serializeDate(date));

		result.set(TITLE_KEY, title);

		result.set(ORIGINATOR, originator);

		result.set(RECEIVED_KEY, received);

		// we actually want to explicitly write null if this is null...
		String dateStr = null;
		if (receivedOnDate != null) {
			dateStr = DateUtils.serializeDate(receivedOnDate);
		}
		result.set(RECEIVED_ON_DATE_KEY, dateStr);

		result.set(RECEIVED_ON_ACCOUNT_KEY, receivedOnAccount);

		return result;
	}

	public boolean usesPreTaxAmount() {
		return !hasPostTaxAmount();
	}

	public boolean hasPreTaxAmount() {
		return preTaxAmount != null;
	}

	public Integer getPreTaxAmount() {
		if (!usesPreTaxAmount()) {
			return FinanceUtils.calcPreTax(postTaxAmount, taxationPercent);
		}
		return preTaxAmount;
	}

	public String getPreTaxAmountAsText() {
		return FinanceUtils.formatMoney(getPreTaxAmount(), currency);
	}

	public void setPreTaxAmount(Integer preTaxAmount) {
		this.preTaxAmount = preTaxAmount;
		this.postTaxAmount = null;
	}

	public boolean hasPostTaxAmount() {
		return postTaxAmount != null;
	}

	public Integer getPostTaxAmount() {
		if (usesPreTaxAmount()) {
			return FinanceUtils.calcPostTax(preTaxAmount, taxationPercent);
		}
		return postTaxAmount;
	}

	public String getPostTaxAmountAsText() {
		return FinanceUtils.formatMoney(getPostTaxAmount(), currency);
	}

	public void setPostTaxAmount(Integer postTaxAmount) {
		this.postTaxAmount = postTaxAmount;
		this.preTaxAmount = null;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Integer getTaxAmount() {
		return getPostTaxAmount() - getPreTaxAmount();
	}

	public Integer getTaxPercent() {
		return taxationPercent;
	}

	public String getTaxPercentAsText() {
		return taxationPercent + "%";
	}

	public void setTaxPercent(Integer taxationPercent) {
		this.taxationPercent = taxationPercent;
	}

	public Date getDate() {
		return date;
	}

	public String getDateAsText() {
		return DateUtils.serializeDate(date);
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getOriginator() {
		return originator;
	}

	public boolean getReceived() {
		return received;
	}

	public void setReceived(boolean received) {
		this.received = received;
	}

	public Date getReceivedOnDate() {
		return receivedOnDate;
	}

	public void setReceivedOnDate(Date receivedOnDate) {
		this.receivedOnDate = receivedOnDate;
	}

	public String getReceivedOnAccount() {
		return receivedOnAccount;
	}

	public void setReceivedOnAccount(String receivedOnAccount) {
		this.receivedOnAccount = receivedOnAccount;
	}

	public boolean setPaidInfo(boolean receivedVal, String dateStr, String accountStr) {
		if ("".equals(dateStr)) {
			setReceivedOnDate(null);
		} else {
			setReceivedOnDate(DateUtils.parseDate(dateStr));
		}

		setReceived(receivedVal);

		setReceivedOnAccount(accountStr);

		return true;
	}

	public Month getParent() {
		return parent;
	}

	public abstract String getCategoryOrCustomer();

	/**
	 * Request our parent to drop us
	 */
	public abstract void deleteFrom(Database database);

	private Color getTextColor() {

		Color result = new Color(0, 0, 0);
		if (getReceived()) {
			result = new Color(0, 156, 0);
		}

		List<Problem> problems = new ArrayList<>();

		reportProblemsTo(problems);

		for (Problem problem : problems) {
			if (problem.isImportant()) {
				// set red and break such that we do not overwrite with yellow for warning later!
				result = new Color(196, 0, 0);
				break;
			} else {
				result = new Color(148, 148, 0);
			}
		}

		return result;
	}

	public String createPanelHtml(Database database) {

		// TODO - get buttons to actually work

		String html = "";

		Color textColor = getTextColor();
		String tooltip = "Not yet paid!";
		if (getReceived()) {
			tooltip = "Paid on " + DateUtils.serializeDate(getReceivedOnDate()) + " to " + getReceivedOnAccount();
		}

		html += "<div class='line'>";

		html += AccountingUtils.createLabelHtml(getDateAsText(), textColor, tooltip, "text-align: center; width: 10%;");
		html += AccountingUtils.createLabelHtml(getTitle(), textColor, tooltip, "text-align: left; width: 30%;");
		html += AccountingUtils.createLabelHtml("[" + getCategoryOrCustomer() + "]", textColor, tooltip, "text-align: left; width: 11%;");
		html += AccountingUtils.createLabelHtml(getOriginator(), textColor, tooltip, "text-align: right; width: 5%;");
		html += AccountingUtils.createLabelHtml(getPreTaxAmountAsText(), textColor, tooltip, "text-align: right; width: 10%;");
		html += AccountingUtils.createLabelHtml(getTaxPercentAsText(), textColor, tooltip, "text-align: right; width: 10%;");
		html += AccountingUtils.createLabelHtml(getPostTaxAmountAsText(), textColor, tooltip, "text-align: right; width: 10%;");

		/*
		JButton curButton = new JButton("Paid");
		curButton.addMouseListener(rowHighlighter);
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(8, 0, 0.05, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddPaidGUI addPaidGUI = new AddPaidGUI(database.getGUI(), database, Entry.this);
				addPaidGUI.show();
			}
		});

		curButton = new JButton("Edit");
		curButton.addMouseListener(rowHighlighter);
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(9, 0, 0.05, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddEntryGUI editEntryGUI = new AddEntryGUI(database.getGUI(), database, Entry.this);
				editEntryGUI.show();
			}
		});

		curButton = new JButton("Delete");
		curButton.addMouseListener(rowHighlighter);
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(10, 0, 0.06, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (GuiUtils.confirmDelete("entry '" + getTitle() + "'")) {
					Entry.this.deleteFrom(database);
				}
			}
		});
		*/

		html += "</div>";

		return html;
	}

	public JPanel createPanelOnGUI(Database database) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();
		Color textColor = getTextColor();
		String tooltip = "Not yet paid!";
		if (getReceived()) {
			tooltip = "Paid on " + DateUtils.serializeDate(getReceivedOnDate()) + " to " + getReceivedOnAccount();
		}

		JPanel curPanel = new JPanel();
		curPanel.setBackground(GUI.getBackgroundColor());
		curPanel.setLayout(new GridBagLayout());

		MouseAdapter rowHighlighter = AccountingUtils.getRowHighlighter(curPanel);

		CopyByClickLabel curLabel = AccountingUtils.createLabel(getDateAsText(), textColor, tooltip);
		curLabel.addMouseListener(rowHighlighter);
		curLabel.setHorizontalAlignment(CopyByClickLabel.CENTER);
		curPanel.add(curLabel, new Arrangement(0, 0, 0.1, 1.0));

		/*
		// This could be used to generate multi-line text, by default showing the paid on date:

		if (getReceived()) {
			curLabel = AccountingUtils.createLabel("<html>" + getTitle() + "<br>" +
				"&nbsp;&nbsp;&nbsp;Paid on " + DateUtils.serializeDate(getReceivedOnDate()) + " to " + getReceivedOnAccount() + "</html>",
				textColor,
				tooltip);
			curLabel.setPreferredSize(new Dimension((int) defaultDimension.getWidth(), 32));
		} else {
			curLabel = AccountingUtils.createLabel(getTitle(), textColor, tooltip);
		}
		curPanel.add(curLabel, new Arrangement(1, 0, 0.3, 1.0));
		*/

		// Buuut actually that gets quite confusing, so instead we just add a tooltip:
		curLabel = AccountingUtils.createLabel(getTitle(), textColor, tooltip);
		curLabel.addMouseListener(rowHighlighter);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.3, 1.0));

		curLabel = AccountingUtils.createLabel("[" + getCategoryOrCustomer() + "]", textColor, tooltip);
		curLabel.addMouseListener(rowHighlighter);
		curPanel.add(curLabel, new Arrangement(2, 0, 0.11, 1.0));

		curLabel = AccountingUtils.createLabel(getOriginator(), textColor, tooltip);
		curLabel.addMouseListener(rowHighlighter);
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curPanel.add(curLabel, new Arrangement(3, 0, 0.05, 1.0));

		curLabel = AccountingUtils.createLabel(getPreTaxAmountAsText(), textColor, tooltip);
		curLabel.addMouseListener(rowHighlighter);
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curPanel.add(curLabel, new Arrangement(4, 0, 0.1, 1.0));

		curLabel = AccountingUtils.createLabel(getTaxPercentAsText(), textColor, tooltip);
		curLabel.addMouseListener(rowHighlighter);
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curPanel.add(curLabel, new Arrangement(5, 0, 0.1, 1.0));

		curLabel = AccountingUtils.createLabel(getPostTaxAmountAsText(), textColor, tooltip);
		curLabel.addMouseListener(rowHighlighter);
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curPanel.add(curLabel, new Arrangement(6, 0, 0.1, 1.0));

		curLabel = AccountingUtils.createLabel("", textColor, tooltip);
		curLabel.addMouseListener(rowHighlighter);
		curPanel.add(curLabel, new Arrangement(7, 0, 0.0, 1.0));

		JButton curButton = new JButton("Paid");
		curButton.addMouseListener(rowHighlighter);
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(8, 0, 0.05, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddPaidGUI addPaidGUI = new AddPaidGUI(database.getGUI(), database, Entry.this);
				addPaidGUI.show();
			}
		});

		curButton = new JButton("Edit");
		curButton.addMouseListener(rowHighlighter);
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(9, 0, 0.05, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddEntryGUI editEntryGUI = new AddEntryGUI(database.getGUI(), database, Entry.this);
				editEntryGUI.show();
			}
		});

		curButton = new JButton("Delete");
		curButton.addMouseListener(rowHighlighter);
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(10, 0, 0.06, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (GuiUtils.confirmDelete("entry '" + getTitle() + "'")) {
					Entry.this.deleteFrom(database);
				}
			}
		});

		curLabel = AccountingUtils.createLabel("", textColor, tooltip);
		curLabel.addMouseListener(rowHighlighter);
		curPanel.add(curLabel, new Arrangement(11, 0, 0.0, 1.0));

		return curPanel;
	}

	public void reportProblemsTo(List<Problem> result) {

		if (parent == null) {
			return;
		}

		Database database = parent.getDatabase();

		if (this instanceof Incoming) {
			if (((Incoming) this).getCategory() != database.mapTitleToCategory(getTitle())) {
				result.add(new ConsistencyProblem(
					"For " + AccountingUtils.getEntryForLog(this) + ", the selected category (" +
					((Incoming) this).getCategory().getText() + ") differs from the automatically detected one (" +
					database.mapTitleToCategory(this.getTitle()).getText() + ").",
					this));
			}
		}

		if (this instanceof Outgoing) {
			if (!getReceived()) {
				Date sixWeeksAgo = DateUtils.daysInTheFuture(-6*7);
				if (getDate().before(sixWeeksAgo)) {
					result.add(new PaymentProblem(
						"The " + AccountingUtils.getEntryForLog(this) + " has not yet been paid!",
						this));
				}
			}
		}

		if (getDate() == null) {
			result.add(new ConsistencyProblem(
				"For " + AccountingUtils.getEntryForLog(this) + ", no date has been selected.",
				this));
		} else {
			Month correctMonth = database.getMonthFromEntryDate(getDate());
			Month selectedMonth = getParent();
			if (!correctMonth.equals(selectedMonth)) {
				result.add(new ConsistencyProblem(
					AccountingUtils.getEntryForLog(this) + " has been filed in " + selectedMonth +
					" but actually belongs to " + correctMonth + ".",
					this));
			}
		}

		if (getTaxPercent() == null) {
			result.add(new ConsistencyProblem(
				"For " + AccountingUtils.getEntryForLog(this) + ", no tax amount has been selected.",
				this));
		} else {
			boolean okay = false;
			Date startCovidVAT = DateUtils.parseDate("2020-07-01"); // CovidVAT started to be used
			Date expectCovidVAT = DateUtils.parseDate("2020-08-01"); // CovidVAT expected to be used for all entries
			Date endCovidVAT = DateUtils.parseDate("2020-12-31"); // CovidVAT usage ended
			if ((date == null) || date.before(startCovidVAT) || date.after(endCovidVAT)) {
				// regular German VAT
				switch (getTaxPercent()) {
					case 0:
					case 7:
					case 19:
						okay = true;
				}
			} else {
				if (date.before(expectCovidVAT)) {
					// regular German VAT or CovidVAT both fine for one month
					switch (getTaxPercent()) {
						case 0:
						case 5:
						case 7:
						case 16:
						case 19:
							okay = true;
					}
				} else {
					// German VAT during Covid-19 times
					switch (getTaxPercent()) {
						case 0:
						case 5:
						case 16:
							okay = true;
					}
				}
			}
			if (!okay) {
				result.add(new ConsistencyWarning(
					"For " + AccountingUtils.getEntryForLog(this) + ", an unusual tax of " +
					getTaxPercent() + "% has been selected.",
					this));
			}
		}

		// if the title contains patreon and the entry is from between 1st Jan 2020 and 31st Oct 2020...
		if (getTitle().toLowerCase().contains("patreon") &&
			((date == null) || (date.after(DateUtils.parseDate("2020-01-01")) && date.before(DateUtils.parseDate("2020-10-31"))))) {
			result.add(new ConsistencyProblem(
				"The " + AccountingUtils.getEntryForLog(this) + " is about Patreon, which gives no invoices, so should never appear!",
				this));
		}
	}

	public boolean matches(String searchFor) {
		if ("".equals(searchFor)) {
			return true;
		}
		if (getTitle().replace("\\n", "").toLowerCase().contains(searchFor.toLowerCase())) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return getPreTaxAmountAsText() + " on " + getDateAsText() + " " + getTitle() + " [" + getCategoryOrCustomer() + "] ";
	}

}
