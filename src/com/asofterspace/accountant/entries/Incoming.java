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
	private static final String INCO_KIND_KEY = "incoKind";

	private boolean isEhrenamtspauschale = false;
	private boolean isUebungsleiterinnenpauschale = false;

	private String customer;


	/**
	 * Create an incoming payment (sent invoice) at runtime
	 */
	public Incoming(Integer amount, Currency currency, Integer taxationPercent, Integer postTaxAmount,
		Date date, String title, String originator, String customer, String incoKind, Month parent) {

		super(amount, currency, taxationPercent, postTaxAmount, date, title, originator, parent);

		this.customer = customer;

		setPauschalenBasedOnIncoKind(incoKind);
	}

	/**
	 * Load an incoming payment (sent invoice) from a generic record
	 */
	public Incoming(Record entryRecord, Month parent) {
		super(entryRecord, parent);

		customer = entryRecord.getString(CUSTOMER_KEY);

		setPauschalenBasedOnIncoKind(entryRecord.getString(INCO_KIND_KEY));
	}

	private void setPauschalenBasedOnIncoKind(String incoKind) {
		isEhrenamtspauschale = false;
		isUebungsleiterinnenpauschale = false;
		if (incoKind != null) {
			isEhrenamtspauschale = incoKind.toLowerCase().startsWith("e");
			isUebungsleiterinnenpauschale = incoKind.toLowerCase().startsWith("u") || incoKind.toLowerCase().startsWith("ü");
		}
	}

	@Override
	public Record toRecord(boolean forDisplay) {

		Record result = super.toRecord(forDisplay);

		result.set(KIND_KEY, "in");

		result.set(CUSTOMER_KEY, customer);

		if (isEhrenamtspauschale) {
			result.set(INCO_KIND_KEY, "ehrenamtspauschale");
		}
		if (isUebungsleiterinnenpauschale) {
			result.set(INCO_KIND_KEY, "uebungsleiterinnenpauschale");
		}

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

	public boolean isSomeKindOfPauschale() {
		return isEhrenamtspauschale || isUebungsleiterinnenpauschale;
	}

	public boolean isEhrenamtspauschale() {
		return isEhrenamtspauschale;
	}

	public boolean isUebungsleiterinnenpauschale() {
		return isUebungsleiterinnenpauschale;
	}

}
