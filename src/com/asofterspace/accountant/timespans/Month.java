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
 * This represents a month of accounting data
 */
public class Month {

	private final static String MONTH_NUM_KEY = "monthNum";
	private final static String OUTGOING_KEY = "outgoings";
	private final static String INCOMING_KEY = "incomings";
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
		"December"
	};

	private Year year;

	private int monthNum;

	private List<Outgoing> outgoings;

	private List<Incoming> incomings;


	/**
	 * Create a new month
	 */
	public Month(int monthNum, Year year) {

		this.year = year;

		this.monthNum = monthNum;

		this.outgoings = new ArrayList<>();
		this.incomings = new ArrayList<>();
	}

	/**
	 * Load a month from a generic record
	 */
	public Month(Record monthRecord, Year year) {

		this.year = year;

		this.monthNum = monthRecord.getInteger(MONTH_NUM_KEY);

		this.outgoings = new ArrayList<>();
		this.incomings = new ArrayList<>();

		for (Record rec : monthRecord.getArray(OUTGOING_KEY)) {
			outgoings.add(new Outgoing(rec));
		}
		for (Record rec : monthRecord.getArray(INCOMING_KEY)) {
			incomings.add(new Incoming(rec));
		}
	}

	public Record toRecord() {

		Record result = Record.emptyArray();

		result.set(MONTH_NUM_KEY, (Integer) monthNum);

		Record outgoingRec = Record.emptyArray();
		result.set(OUTGOING_KEY, outgoingRec);
		Record incomingRec = Record.emptyArray();
		result.set(INCOMING_KEY, incomingRec);

		for (Outgoing entry : outgoings) {
			outgoingRec.append(entry.toRecord());
		}
		for (Incoming entry : incomings) {
			incomingRec.append(entry.toRecord());
		}

		return result;
	}

	public Year getYear() {
		return year;
	}

	public int getNum() {
		return monthNum;
	}

	public List<Outgoing> getOutgoings() {
		Collections.sort(outgoings, new Comparator<Outgoing>() {
			public int compare(Outgoing a, Outgoing b) {
				return a.getDate().compareTo(b.getDate());
			}
		});
		return outgoings;
	}

	public List<Incoming> getIncomings() {
		Collections.sort(incomings, new Comparator<Incoming>() {
			public int compare(Incoming a, Incoming b) {
				return a.getDate().compareTo(b.getDate());
			}
		});
		return incomings;
	}

	@Override
	public String toString() {
		return year.toString() + ": " + MONTH_NUM_TO_NAME[monthNum];
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
