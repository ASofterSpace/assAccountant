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

	// gets all incoming invoices not set to categories donation or personal
	public abstract List<Incoming> getIncomings();

	// gets all incoming invoices set to category donation
	public abstract List<Incoming> getDonations();

	// gets all incoming invoices set to category personal
	public abstract List<Incoming> getPersonals();

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

	public abstract int getPersTotalBeforeTax();

	public int getPersTotalTax() {
		return getPersTotalAfterTax() - getPersTotalBeforeTax();
	}

	public abstract int getPersTotalAfterTax();

	// pre-paid VAT discountable for own VAT payments
	public int getDiscountablePreTax() {
		return getInTotalTax() + getDonTotalTax();
	}

	public int getRemainingVatPayments() {
		int remainVATpay = getOutTotalTax() - (getInTotalTax() + getDonTotalTax());
		if (remainVATpay < 0) {
			remainVATpay = 0;
		}
		return remainVATpay;
	}

	// actually paid VAT (originally equal to getRemainingVatPayments(), but if after setting
	// the USt-Voranmeldung some new invoices come in, this will keep track of what was actually
	// paid, while the other will adjust itself)
	public abstract int getVatPrepaymentsPaidTotal();

}
