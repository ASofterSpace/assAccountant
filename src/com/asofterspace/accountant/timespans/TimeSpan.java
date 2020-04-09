/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.timespans;

import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.world.Category;
import com.asofterspace.toolbox.utils.Record;

import java.util.List;


/**
 * This represents a generic timespan of accounting data
 */
public abstract class TimeSpan {

	public abstract Record toRecord();

	public abstract int getNum();

	// gets all outgoing invoices
	public abstract List<Outgoing> getOutgoings();

	// gets on incoming invoices not set to category donation
	public abstract List<Incoming> getIncomings();

	// gets on incoming invoices set to category donation
	public abstract List<Incoming> getDonations();

	// before tax means before applying VAT (USt)
	public abstract int getOutTotalBeforeTax();

	public int getOutTotalTax() {
		return getOutTotalAfterTax() - getOutTotalBeforeTax();
	}

	// after tax means after applying VAT (USt)
	public abstract int getOutTotalAfterTax();

	public abstract int getInTotalBeforeTax();

	public abstract int getInTotalBeforeTax(Category category);

	public int getInTotalTax() {
		return getInTotalAfterTax() - getInTotalBeforeTax();
	}

	public abstract int getInTotalAfterTax();

	public abstract int getDonTotalBeforeTax();

	public int getDonTotalTax() {
		return getDonTotalAfterTax() - getDonTotalBeforeTax();
	}

	public abstract int getDonTotalAfterTax();

}
