/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.timespans;

import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.toolbox.utils.Record;


/**
 * This represents a month of accounting data
 */
public class Month {

	private final static String MONTH_NUM_KEY = "monthNum";
	private final static String OUTGOING_KEY = "outgoings";
	private final static String INCOMING_KEY = "incomings";

	private int monthNum;

	private List<Outgoing> outgoings;

	private List<Incoming> incomings;


	/**
	 * Create a new month
	 */
	public Month(int monthNum) {

		this.monthNum = 0;

		this.outgoings = new ArrayList();
		this.incomings = new ArrayList();
	}

	/**
	 * Load a month from a generic record
	 */
	public Month(Record monthRecord) {

		this.monthNum = monthRecord.getInteger(MONTH_NUM_KEY);

		this.outgoings = new ArrayList();
		this.incomings = new ArrayList();

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

		for (Month entry : outgoings) {
			outgoingRec.append(entry.toRecord());
		}
		for (Month entry : incomings) {
			incomingRec.append(entry.toRecord());
		}

		return result;
	}

}
