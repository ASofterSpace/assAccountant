/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.timespans;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.world.Category;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.accounting.FinanceUtils;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This represents a month of accounting data
 */
public class Month extends TimeSpan {

	private final static String MONTH_NUM_KEY = "monthNum";
	private final static String INCOMING_KEY = "incomings";
	private final static String OUTGOING_KEY = "outgoings";
	private final static String VAT_PREPAID_KEY = "vatPrepaymentsPaid";
	private final static String[] MONTH_NUM_TO_NAME = {
		"January",
		"February",
		"March",
		"April",
		"May",
		"June",
		"July",
		"August",
		"September",
		"October",
		"November",
		"December"};

	private Year year;

	private int monthNum;

	private List<Incoming> incomings;

	private List<Outgoing> outgoings;

	private Integer vatPrepaymentsPaidTotal;


	/**
	 * Create a new month
	 */
	public Month(int monthNum, Year year) {

		this.year = year;

		this.monthNum = monthNum;

		this.incomings = new ArrayList<>();
		this.outgoings = new ArrayList<>();
	}

	/**
	 * Load a month from a generic record
	 */
	public Month(Record monthRecord, Year year) {

		this.year = year;

		this.monthNum = monthRecord.getInteger(MONTH_NUM_KEY);

		this.incomings = new ArrayList<>();
		this.outgoings = new ArrayList<>();

		for (Record rec : monthRecord.getArray(INCOMING_KEY)) {
			incomings.add(new Incoming(rec, this));
		}
		for (Record rec : monthRecord.getArray(OUTGOING_KEY)) {
			outgoings.add(new Outgoing(rec, this));
		}

		this.vatPrepaymentsPaidTotal = monthRecord.getInteger(VAT_PREPAID_KEY);
	}

	@Override
	public Record toRecord() {

		Record result = Record.emptyArray();

		result.set(MONTH_NUM_KEY, (Integer) monthNum);

		Record incomingRec = Record.emptyArray();
		result.set(INCOMING_KEY, incomingRec);
		Record outgoingRec = Record.emptyArray();
		result.set(OUTGOING_KEY, outgoingRec);

		for (Incoming entry : incomings) {
			incomingRec.append(entry.toRecord());
		}
		for (Outgoing entry : outgoings) {
			outgoingRec.append(entry.toRecord());
		}

		result.set(VAT_PREPAID_KEY, vatPrepaymentsPaidTotal);

		return result;
	}

	@Override
	public Year getYear() {
		return year;
	}

	@Override
	public int getNum() {
		return monthNum;
	}

	@Override
	public List<Incoming> getIncomings() {
		AccountingUtils.sortEntries(incomings);
		return incomings;
	}

	@Override
	public List<Entry> getEntries() {
		List<Entry> result = new ArrayList<>();
		result.addAll(outgoings);
		result.addAll(incomings);
		return result;
	}

	@Override
	public List<Outgoing> getOutgoings() {
		AccountingUtils.sortEntries(outgoings);
		List<Outgoing> result = new ArrayList<>();
		for (Outgoing outgoing : outgoings) {
			if (!outgoing.getCategory().isSpecial()) {
				result.add(outgoing);
			}
		}
		return result;
	}

	@Override
	public List<Outgoing> getDonations() {
		AccountingUtils.sortEntries(outgoings);
		List<Outgoing> result = new ArrayList<>();
		for (Outgoing outgoing : outgoings) {
			if (outgoing.getCategory() == Category.DONATION) {
				result.add(outgoing);
			}
		}
		return result;
	}

	@Override
	public List<Outgoing> getPersonals() {
		AccountingUtils.sortEntries(outgoings);
		List<Outgoing> result = new ArrayList<>();
		for (Outgoing outgoing : outgoings) {
			if (outgoing.getCategory() == Category.PERSONAL) {
				result.add(outgoing);
			}
		}
		return result;
	}

	public void removeIncoming(Incoming remEntry) {
		incomings.remove(remEntry);
	}

	public void removeOutgoing(Outgoing remEntry) {
		outgoings.remove(remEntry);
	}

	public boolean addEntry(Date date, String title, Object catOrCustomerObj, String amountStr,
		Currency currency, String taxationPercentStr, String postTaxAmountStr, String originator, boolean isOutgoing) {

		Integer amountObj = FinanceUtils.parseMoney(amountStr);
		Integer postTaxAmountObj = FinanceUtils.parseMoney(postTaxAmountStr);

		if ((amountObj == null) && (postTaxAmountObj == null)) {
			return GuiUtils.complain("The texts " + amountStr + " and " + postTaxAmountStr +
				" could both not be parsed as amounts of money!");
		}

		Integer taxationPercent = AccountingUtils.parseTaxes(taxationPercentStr);
		if (taxationPercent == null) {
			return GuiUtils.complain("The text " + taxationPercentStr + " could not be parsed as integer!");
		}

		if (catOrCustomerObj == null) {
			return GuiUtils.complain("The no category or customer entered!");
		}

		String catOrCustomer = catOrCustomerObj.toString();

		if (isOutgoing) {

			Category category = Category.fromString(catOrCustomer);

			if (category == null) {
				return GuiUtils.complain("The text " + catOrCustomer + " could not be parsed as category!");
			}

			Outgoing newIn = new Outgoing(amountObj, currency, taxationPercent, postTaxAmountObj,
				date, title, originator, category, this);
			outgoings.add(newIn);

		} else {

			String customer = catOrCustomer;

			if ((customer == null) || "".equals(customer)) {
				return GuiUtils.complain("The text " + catOrCustomer + " should contain a customer!");
			}

			Incoming newOut = new Incoming(amountObj, currency, taxationPercent, postTaxAmountObj,
				date, title, originator, customer, this);
			incomings.add(newOut);
		}

		return true;
	}

	public String getMonthName() {
		return MONTH_NUM_TO_NAME[monthNum];
	}

	@Override
	public int getInTotalBeforeTax() {
		int result = 0;
		for (Incoming cur : incomings) {
			result += cur.getPreTaxAmount();
		}
		return result;
	}

	@Override
	public int getInTotalAfterTax() {
		int result = 0;
		for (Incoming cur : incomings) {
			result += cur.getPostTaxAmount();
		}
		return result;
	}

	@Override
	public int getOutTotalBeforeTax() {
		int result = 0;
		for (Outgoing cur : getOutgoings()) {
			result += cur.getPreTaxAmount();
		}
		return result;
	}

	@Override
	public int getOutTotalBeforeTax(Category category) {
		int result = 0;
		for (Outgoing cur : outgoings) {
			if (cur.getCategory() == category) {
				result += cur.getPreTaxAmount();
			}
		}
		return result;
	}

	@Override
	public int getOutTotalAfterTax() {
		int result = 0;
		for (Outgoing cur : getOutgoings()) {
			result += cur.getPostTaxAmount();
		}
		return result;
	}

	@Override
	public int getDonTotalBeforeTax() {
		int result = 0;
		for (Outgoing cur : getDonations()) {
			result += cur.getPreTaxAmount();
		}
		return result;
	}

	@Override
	public int getDonTotalAfterTax() {
		int result = 0;
		for (Outgoing cur : getDonations()) {
			result += cur.getPostTaxAmount();
		}
		return result;
	}

	@Override
	public int getPersTotalBeforeTax() {
		int result = 0;
		for (Outgoing cur : getPersonals()) {
			result += cur.getPreTaxAmount();
		}
		return result;
	}

	@Override
	public int getPersTotalAfterTax() {
		int result = 0;
		for (Outgoing cur : getPersonals()) {
			result += cur.getPostTaxAmount();
		}
		return result;
	}

	public void setVatPrepaymentsPaidTotal(int newVal) {
		this.vatPrepaymentsPaidTotal = newVal;
	}

	@Override
	public int getVatPrepaymentsPaidTotal() {
		// the current value is better than no value at all...
		if (vatPrepaymentsPaidTotal == null) {
			return getRemainingVatPayments();
		}
		return vatPrepaymentsPaidTotal;
	}

	public Database getDatabase() {
		return year.getDatabase();
	}

	@Override
	public String toString() {
		return year.toString() + ": " + getMonthName();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof Month) {
			// months are equal if they represent the same month - in the same year!
			Month otherMonth = (Month) other;
			if (monthNum == otherMonth.monthNum) {
				return year.getNum() == otherMonth.getYear().getNum();
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return monthNum;
	}
}
