/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.timespans;

import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.toolbox.utils.Record;

import java.util.List;


/**
 * This represents a generic timespan of accounting data
 */
public abstract class TimeSpan {

	public abstract Record toRecord();

	public abstract int getNum();

	public abstract List<Outgoing> getOutgoings();

	public abstract List<Incoming> getIncomings();

	public abstract int getOutTotalBeforeTax();

	public int getOutTotalTax() {
		return getOutTotalAfterTax() - getOutTotalBeforeTax();
	}

	public abstract int getOutTotalAfterTax();

	public abstract int getInTotalBeforeTax();

	public int getInTotalTax() {
		return getInTotalAfterTax() - getInTotalBeforeTax();
	}

	public abstract int getInTotalAfterTax();

}
