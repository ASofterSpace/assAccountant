/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.entries;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;

import java.util.Date;


/**
 * This represents an outgoing invoice (so one that we get paid for, yay! ^^)
 */
public class Outgoing extends Entry {

	private static final String CUSTOMER_KEY = "customer";

	private static final String RECEIVED_KEY = "received";

	private static final String RECEIVED_ON_DATE_KEY = "receivedOnDate";

	private static final String RECEIVED_ON_ACCOUNT_KEY = "receivedOnAccount";

	private String customer;

	private boolean received;

	private Date receivedOnDate;

	private String receivedOnAccount;


	/**
	 * Create an outgoing invoice at runtime
	 */
	public Outgoing(Integer amount, Currency currency, Integer taxationPercent, Date date,
		String title, String originator, String customer, Month parent) {

		super(amount, currency, taxationPercent, date, title, originator, parent);

		this.customer = customer;

		this.received = false;
	}

	/**
	 * Load an outgoing invoice from a generic record
	 */
	public Outgoing(Record entryRecord, Month parent) {
		super(entryRecord, parent);

		customer = entryRecord.getString(CUSTOMER_KEY);

		received = entryRecord.getBoolean(RECEIVED_KEY, false);

		receivedOnDate = null;
		String dateStr = entryRecord.getString(RECEIVED_ON_DATE_KEY);
		if (dateStr != null) {
			receivedOnDate = DateUtils.parseDate(dateStr);
		}

		receivedOnAccount = entryRecord.getString(RECEIVED_ON_ACCOUNT_KEY);
	}

	@Override
	public Record toRecord() {

		Record result = super.toRecord();

		result.set(CUSTOMER_KEY, customer);

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

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	@Override
	public void deleteFrom(Database database) {
		parent.removeOutgoing(this);
		database.save();
	}

	@Override
	public String getCategoryOrCustomer() {
		return getCustomer();
	}

}
