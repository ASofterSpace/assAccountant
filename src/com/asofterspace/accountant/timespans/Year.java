/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.timespans;

import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
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


	/**
	 * Create a new year
	 */
	public Year(int yearNum) {

		this.yearNum = yearNum;

		this.months = new ArrayList<>();

		for (int i = 0; i < 12; i++) {
			this.months.add(new Month(i, this));
		}
	}

	/**
	 * Load a year from a generic record
	 */
	public Year(Record yearRecord) {

		this.yearNum = yearRecord.getInteger(YEAR_NUM_KEY);

		this.months = new ArrayList<>();

		for (Record monthRec : yearRecord.getArray(MONTHS_KEY)) {
			months.add(new Month(monthRec, this));
		}
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
	public List<Outgoing> getOutgoings() {
		List<Outgoing> result = new ArrayList<>();
		for (Month month : getMonths()) {
			result.addAll(month.getOutgoings());
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
	public int getOutTotalBeforeTax() {
		int result = 0;
		for (Month month : months) {
			result += month.getOutTotalBeforeTax();
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
