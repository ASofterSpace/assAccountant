/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.entries;

import com.asofterspace.toolbox.utils.Record;


/**
 * This represents an outgoing invoice (so one that we get paid for, yay! ^^)
 */
public class Outgoing extends Entry {

	private static final String CUSTOMER_KEY = "customer";

	private String customer;


	/**
	 * Load an outgoing invoice from a generic record
	 */
	public Outgoing(Record entryRecord) {
		super(entryRecord);

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
	public String getCategoryOrCustomer() {
		return getCustomer();
	}

}
