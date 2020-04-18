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
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.Utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;


public abstract class Entry {

	private static final String AMOUNT_KEY = "amount";
	private static final String CURRENCY_KEY = "currency";
	private static final String TAXATION_PERCENT = "taxationPercent";
	private static final String DATE_KEY = "date";
	private static final String TITLE_KEY = "title";
	private static final String ORIGINATOR = "originator";
	private static final String RECEIVED_KEY = "received";
	private static final String RECEIVED_ON_DATE_KEY = "receivedOnDate";
	private static final String RECEIVED_ON_ACCOUNT_KEY = "receivedOnAccount";

	// this is the amount pre-tax!
	private Integer amount;

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
	public Entry(Integer amount, Currency currency, Integer taxationPercent, Date date,
		String title, String originator, Month parent) {

		this.amount = amount;

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

		this.amount = entryRecord.getInteger(AMOUNT_KEY);

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

		result.set(AMOUNT_KEY, amount);

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

	public Integer getAmount() {
		return amount;
	}

	public String getAmountAsText() {
		return AccountingUtils.formatMoney(amount, currency);
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public Integer getPostTaxAmount() {
		return AccountingUtils.calcPostTax(amount, taxationPercent);
	}

	public String getPostTaxAmountAsText() {
		return AccountingUtils.formatMoney(getPostTaxAmount(), currency);
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
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

	public JPanel createPanelOnGUI(Database database) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();

		JPanel curPanel = new JPanel();
		curPanel.setBackground(GUI.getBackgroundColor());
		curPanel.setLayout(new GridBagLayout());

		CopyByClickLabel curLabel = createLabel(getDateAsText());
		curLabel.setHorizontalAlignment(CopyByClickLabel.CENTER);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(0, 0, 0.1, 1.0));

		curLabel = createLabel(getTitle());
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.3, 1.0));

		curLabel = createLabel("[" + getCategoryOrCustomer() + "]");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(2, 0, 0.11, 1.0));

		curLabel = createLabel(getOriginator());
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(3, 0, 0.05, 1.0));

		curLabel = createLabel(getAmountAsText());
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(4, 0, 0.1, 1.0));

		curLabel = createLabel(getTaxPercentAsText());
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(5, 0, 0.1, 1.0));

		curLabel = createLabel(getPostTaxAmountAsText());
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(6, 0, 0.1, 1.0));

		curLabel = new CopyByClickLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(7, 0, 0.0, 1.0));

		JButton curButton = new JButton("Paid");
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
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(10, 0, 0.06, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Entry.this.deleteFrom(database);
			}
		});

		curLabel = new CopyByClickLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(11, 0, 0.0, 1.0));

		return curPanel;
	}

	private CopyByClickLabel createLabel(String text) {

		CopyByClickLabel result = new CopyByClickLabel(text);

		result.setForeground(new Color(0, 0, 0));
		if (received) {
			result.setForeground(new Color(0, 156, 0));
		}

		List<Problem> problems = new ArrayList<>();

		reportProblemsTo(problems);

		for (Problem problem : problems) {
			if (problem.isImportant()) {
				// set red and break such that we do not overwrite with yellow for warning later!
				result.setForeground(new Color(196, 0, 0));
				break;
			} else {
				result.setForeground(new Color(148, 148, 0));
			}
		}

		return result;
	}

	public void reportProblemsTo(List<Problem> result) {

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
			switch (getTaxPercent()) {
				case 0:
				case 7:
				case 19:
					okay = true;
			}
			if (!okay) {
				result.add(new ConsistencyWarning(
					"For " + AccountingUtils.getEntryForLog(this) + ", an unusual tax of " +
					getTaxPercent() + "% has been selected.",
					this));
			}
		}
	}

	@Override
	public String toString() {
		return getAmountAsText() + " on " + getDateAsText() + " " + getTitle() + " [" + getCategoryOrCustomer() + "] ";
	}

}
