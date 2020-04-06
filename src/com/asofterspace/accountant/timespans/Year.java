/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.timespans;

import com.asofterspace.toolbox.utils.Record;

import java.util.ArrayList;
import java.util.List;


/**
 * This represents a whole year of accounting data (whoah!)
 */
public class Year {

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

	public int getNum() {
		return yearNum;
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
