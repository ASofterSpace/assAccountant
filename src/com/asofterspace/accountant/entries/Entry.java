/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.entries;

import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;

import java.util.Date;


public abstract class Entry {

	private static final String AMOUNT_KEY = "amount";
	private static final String CURRENCY_KEY = "currency";
	private static final String TAXATION_PERCENT = "taxationPercent";
	private static final String DATE_KEY = "date";
	private static final String TITLE_KEY = "title";

	private Integer amount;

	private Currency currency;

	private Integer taxationPercent;

	private Date date;

	private String title;


	/**
	 * Load an entry from a generic record
	 */
	public Entry(Record entryRecord) {

		this.amount = entryRecord.getInteger(AMOUNT_KEY);

		this.currency = Currency.valueOf(entryRecord.getString(CURRENCY_KEY));

		this.taxationPercent = entryRecord.getInteger(TAXATION_PERCENT);

		this.date = DateUtils.parseDate(entryRecord.getString(DATE_KEY));

		this.title = entryRecord.getString(TITLE_KEY);
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

}
