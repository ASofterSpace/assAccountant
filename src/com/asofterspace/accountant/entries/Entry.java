/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.entries;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.gui.Arrangement;
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
import javax.swing.JLabel;
import javax.swing.JPanel;


public abstract class Entry {

	private static final String AMOUNT_KEY = "amount";
	private static final String CURRENCY_KEY = "currency";
	private static final String TAXATION_PERCENT = "taxationPercent";
	private static final String DATE_KEY = "date";
	private static final String TITLE_KEY = "title";

	// this is the amount pre-tax!
	private Integer amount;

	private Currency currency;

	private Integer taxationPercent;

	private Date date;

	private String title;

	protected Month parent;


	/**
	 * Load an entry from a generic record
	 */
	public Entry(Record entryRecord, Month parent) {

		this.amount = entryRecord.getInteger(AMOUNT_KEY);

		this.currency = Currency.valueOf(entryRecord.getString(CURRENCY_KEY));

		this.taxationPercent = entryRecord.getInteger(TAXATION_PERCENT);

		this.date = DateUtils.parseDate(entryRecord.getString(DATE_KEY));

		this.title = entryRecord.getString(TITLE_KEY);

		this.parent = parent;
	}

	public Record toRecord() {

		Record result = new Record();

		result.set(AMOUNT_KEY, amount);

		result.set(CURRENCY_KEY, currency);

		result.set(TAXATION_PERCENT, taxationPercent);

		result.set(DATE_KEY, DateUtils.serializeDate(date));

		result.set(TITLE_KEY, title);

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
		return (amount * (100 + taxationPercent)) / 100;
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

	public abstract String getCategoryOrCustomer();

	/**
	 * Request our parent to drop us
	 */
	public abstract void deleteFrom(Database database);

	public JPanel createPanelOnGUI(Database database) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();

		JPanel curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());

		JLabel curLabel = new JLabel(getDateAsText());
		curLabel.setHorizontalAlignment(JLabel.CENTER);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(0, 0, 0.1, 1.0));

		curLabel = new JLabel(getTitle());
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.35, 1.0));

		curLabel = new JLabel("[" + getCategoryOrCustomer() + "]");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(2, 0, 0.11, 1.0));

		curLabel = new JLabel(getAmountAsText());
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(3, 0, 0.1, 1.0));

		curLabel = new JLabel(getTaxPercentAsText());
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(4, 0, 0.1, 1.0));

		curLabel = new JLabel(getPostTaxAmountAsText());
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(5, 0, 0.1, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(6, 0, 0.0, 1.0));

		JButton curButton = new JButton("Edit");
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(7, 0, 0.08, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO
				System.err.println("Whoops, this button is not yet working!");
			}
		});

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(8, 0, 0.0, 1.0));

		curButton = new JButton("Delete");
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(9, 0, 0.08, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Entry.this.deleteFrom(database);
			}
		});

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(10, 0, 0.0, 1.0));

		return curPanel;
	}

}
