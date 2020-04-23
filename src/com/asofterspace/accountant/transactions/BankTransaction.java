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


	public static BankTransaction fromRecord(Record rec) {

		BankTransaction result = new BankTransaction();

		result.amount = rec.getInteger(AMOUNT_KEY);

		result.title = rec.getString(TITLE_KEY);

		result.date = DateUtils.parseDate(rec.getString(DATE_KEY));

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

}
