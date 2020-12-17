/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.entries;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.utils.Record;

import java.util.Date;


/**
 * This represents an incoming payment (so an invoice we send, which we get paid for, yay! ^^)
 */
public class Incoming extends Entry {

	private static final String CUSTOMER_KEY = "customer";

	private String customer;


	/**
	 * Create an incoming payment (sent invoice) at runtime
	 */
	public Incoming(Integer amount, Currency currency, Integer taxationPercent, Integer postTaxAmount,
		Date date, String title, String originator, String customer, Month parent) {

		super(amount, currency, taxationPercent, postTaxAmount, date, title, originator, parent);

		this.customer = customer;
	}

	/**
	 * Load an incoming payment (sent invoice) from a generic record
	 */
	public Incoming(Record entryRecord, Month parent) {
		super(entryRecord, parent);

		customer = entryRecord.getString(CUSTOMER_KEY);
	}

	@Override
	public Record toRecord() {

		Record result = super.toRecord();

		result.set(CUSTOMER_KEY, customer);

		return result;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	@Override
	public void deleteFrom(Database database) {
		if (parent != null) {
			parent.removeIncoming(this);
			database.save();
		}
	}

	@Override
	public String getCategoryOrCustomer() {
		return getCustomer();
	}

}
