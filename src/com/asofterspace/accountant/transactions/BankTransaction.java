/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.transactions;

import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;

import java.util.Date;


/**
 * This represents a generic bank transaction which is not a main entry
 * reflected in the system (the assAccountant mainly handles invoices),
 * but also being monitored because why not :)
 */
public class BankTransaction {

	private static final String AMOUNT_KEY = "amount";
	private static final String TITLE_KEY = "title";
	private static final String DATE_KEY = "date";

	private Integer amount;

	private String title;

	private Date date;

	private BankAccount belongsTo;


	private BankTransaction() {
	}

	public BankTransaction(Integer amount, String title, Date date, BankAccount belongsTo) {
		this.amount = amount;
		this.title = title;
		this.date = date;
		this.belongsTo = belongsTo;
	}

	public static BankTransaction fromRecord(Record rec, BankAccount parent) {

		BankTransaction result = new BankTransaction();

		result.amount = rec.getInteger(AMOUNT_KEY);

		result.title = rec.getString(TITLE_KEY);

		result.date = DateUtils.parseDate(rec.getString(DATE_KEY));

		result.belongsTo = parent;

		return result;
	}

	public Record toRecord() {

		Record result = Record.emptyObject();

		result.set(AMOUNT_KEY, amount);

		result.set(TITLE_KEY, title);

		result.set(DATE_KEY, DateUtils.serializeDate(date));

		return result;
	}

	public Integer getAmount() {
		return amount;
	}

	public String getTitle() {
		return title;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof BankTransaction) {
			BankTransaction otherBankTransaction = (BankTransaction) other;
			if (!this.amount.equals(otherBankTransaction.amount)) {
				 return false;
			}
			if (!this.title.equals(otherBankTransaction.title)) {
				 return false;
			}
			if (!this.date.equals(otherBankTransaction.date)) {
				 return false;
			}
			if (!this.belongsTo.equals(otherBankTransaction.belongsTo)) {
				 return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return amount;
	}

}
