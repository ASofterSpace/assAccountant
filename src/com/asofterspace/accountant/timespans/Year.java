/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.timespans;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.world.Category;
import com.asofterspace.toolbox.utils.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * This represents a whole year of accounting data (whoah!)
 */
public class Year extends TimeSpan {

	private static final String YEAR_NUM_KEY = "yearNum";

	private static final String MONTHS_KEY = "months";

	private int yearNum;

	private List<Month> months;

	private Database database;


	/**
	 * Create a new year
	 */
	public Year(int yearNum, Database database) {

		this.yearNum = yearNum;

		this.months = new ArrayList<>();

		for (int i = 0; i < 12; i++) {
			this.months.add(new Month(i, this));
		}

		this.database = database;
	}

	/**
	 * Load a year from a generic record
	 */
	public Year(Record yearRecord, Database database) {

		this.yearNum = yearRecord.getInteger(YEAR_NUM_KEY);

		this.months = new ArrayList<>();

		for (Record monthRec : yearRecord.getArray(MONTHS_KEY)) {
			months.add(new Month(monthRec, this));
		}

		this.database = database;
	}

	@Override
	public Record toRecord() {

		Record result = Record.emptyArray();

		result.set(YEAR_NUM_KEY, (Integer) yearNum);

		Record monthsRec = Record.emptyArray();
		result.set(MONTHS_KEY, monthsRec);

		for (Month month : months) {
			monthsRec.append(month.toRecord());
		}

		return result;
	}

	@Override
	public int getNum() {
		return yearNum;
	}

	public List<Month> getMonths() {
		Collections.sort(months, new Comparator<Month>() {
			public int compare(Month a, Month b) {
				return b.getNum() - a.getNum();
			}
		});
		return months;
	}

	@Override
	public List<Entry> getEntries() {
		List<Entry> result = new ArrayList<>();
		for (Month month : getMonths()) {
			result.addAll(month.getEntries());
		}
		return result;
	}

	@Override
	public List<Incoming> getIncomings() {
		List<Incoming> result = new ArrayList<>();
		for (Month month : getMonths()) {
			result.addAll(month.getIncomings());
		}
		return result;
	}

	@Override
	public List<Outgoing> getOutgoings() {
		List<Outgoing> result = new ArrayList<>();
		for (Month month : getMonths()) {
			result.addAll(month.getOutgoings());
		}
		return result;
	}

	@Override
	public List<Outgoing> getDonations() {
		List<Outgoing> result = new ArrayList<>();
		for (Month month : getMonths()) {
			result.addAll(month.getDonations());
		}
		return result;
	}

	@Override
	public List<Outgoing> getPersonals() {
		List<Outgoing> result = new ArrayList<>();
		for (Month month : getMonths()) {
			result.addAll(month.getPersonals());
		}
		return result;
	}

	@Override
	public int getInTotalBeforeTax() {
		int result = 0;
		for (Month month : months) {
			result += month.getInTotalBeforeTax();
		}
		return result;
	}

	@Override
	public int getInTotalAfterTax() {
		int result = 0;
		for (Month month : months) {
			result += month.getInTotalAfterTax();
		}
		return result;
	}

	@Override
	public int getOutTotalBeforeTax() {
		int result = 0;
		for (Month month : months) {
			result += month.getOutTotalBeforeTax();
		}
		return result;
	}

	@Override
	public int getOutTotalBeforeTax(Category category) {
		int result = 0;
		for (Month month : months) {
			result += month.getOutTotalBeforeTax(category);
		}
		return result;
	}

	@Override
	public int getOutTotalAfterTax() {
		int result = 0;
		for (Month month : months) {
			result += month.getOutTotalAfterTax();
		}
		return result;
	}

	@Override
	public int getDonTotalBeforeTax() {
		int result = 0;
		for (Month month : months) {
			result += month.getDonTotalBeforeTax();
		}
		return result;
	}

	@Override
	public int getDonTotalAfterTax() {
		int result = 0;
		for (Month month : months) {
			result += month.getDonTotalAfterTax();
		}
		return result;
	}

	@Override
	public int getPersTotalBeforeTax() {
		int result = 0;
		for (Month month : months) {
			result += month.getPersTotalBeforeTax();
		}
		return result;
	}

	@Override
	public int getPersTotalAfterTax() {
		int result = 0;
		for (Month month : months) {
			result += month.getPersTotalAfterTax();
		}
		return result;
	}

	// gets the expected income tax VERY ROUGHLY!
	public long getExpectedIncomeTax() {

		// we calculate the income tax as good as we can... and then we take 90% of that value to
		// account for stuff like insurances which are not contained in our numbers at all so far!
		return (getExpectedIncomeTaxProper() * 9) / 10;
	}

	private long getExpectedIncomeTaxProper() {

		double result = 0;
		double taxAmount = getInTotalBeforeTax() - (getOutTotalBeforeTax() + getDonTotalBeforeTax() + getPersTotalBeforeTax());

		// Freibetrag
		if (taxAmount <= 916800) {
			return (long) result;
		}

		double curTaxAmount = taxAmount;
		if (curTaxAmount > 1425400) {
			curTaxAmount = 1425400;
		}
		double y = (curTaxAmount - 916800) / 1000000;
		result = result + ((98014*y + 140000) * y);
		if (taxAmount <= 1425400) {
			return (long) result;
		}

		curTaxAmount = taxAmount;
		if (curTaxAmount > 5596000) {
			curTaxAmount = 5596000;
		}
		y = (curTaxAmount - 1425400) / 1000000;
		result = result + ((21616*y + 239700) * y) + 96558;
		if (taxAmount <= 5596000) {
			return (long) result;
		}

		curTaxAmount = taxAmount;
		if (curTaxAmount > 26532600) {
			curTaxAmount = 26532600;
		}
		result = result + ((42*(curTaxAmount-5596000))/100)+(147223/10);
		if (taxAmount <= 265326000) {
			return (long) result;
		}

		curTaxAmount = taxAmount;
		result = result + ((45*(curTaxAmount-26532600))/100)+(10265602/100);
		return (long) result;
	}

	@Override
	public int getVatPrepaymentsPaidTotal() {
		int result = 0;
		for (Month month : months) {
			result += month.getVatPrepaymentsPaidTotal();
		}
		return result;
	}

	@Override
	public Year getYear() {
		return this;
	}

	public Database getDatabase() {
		return database;
	}

	@Override
	public String toString() {
		return "" + yearNum;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof Year) {
			Year otherYear = (Year) other;
			return yearNum == otherYear.yearNum;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return yearNum;
	}
}
