/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.entries;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.AddEntryGUI;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.Utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
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

	// this is the amount pre-tax!
	private Integer amount;

	private Currency currency;

	private Integer taxationPercent;

	private Date date;

	private String title;

	private String originator;

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
		if ((this.originator == null) || this.originator.equals("")) { // DEBUG
			this.originator = "Moya"; // DEBUG
		} // DEBUG

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
		curPanel.setLayout(new GridBagLayout());

		CopyByClickLabel curLabel = new CopyByClickLabel(getDateAsText());
		curLabel.setHorizontalAlignment(CopyByClickLabel.CENTER);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(0, 0, 0.1, 1.0));

		curLabel = new CopyByClickLabel(getTitle());
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.3, 1.0));

		curLabel = new CopyByClickLabel("[" + getCategoryOrCustomer() + "]");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(2, 0, 0.11, 1.0));

		curLabel = new CopyByClickLabel(getOriginator());
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(3, 0, 0.05, 1.0));

		curLabel = new CopyByClickLabel(getAmountAsText());
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(4, 0, 0.1, 1.0));

		curLabel = new CopyByClickLabel(getTaxPercentAsText());
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(5, 0, 0.1, 1.0));

		curLabel = new CopyByClickLabel(getPostTaxAmountAsText());
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(6, 0, 0.1, 1.0));

		curLabel = new CopyByClickLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(7, 0, 0.0, 1.0));

		JButton curButton = new JButton("Edit");
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(8, 0, 0.08, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddEntryGUI editEntryGUI = new AddEntryGUI(database.getGUI(), database, Entry.this);
				editEntryGUI.show();
			}
		});

		curLabel = new CopyByClickLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(9, 0, 0.0, 1.0));

		curButton = new JButton("Delete");
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(10, 0, 0.08, 1.0));
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

}
