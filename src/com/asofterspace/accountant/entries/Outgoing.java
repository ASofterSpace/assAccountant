/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.entries;


/**
 * This represents an outgoing invoice (so one that we get paid for, yay! ^^)
 */
public class Outgoing extends Entry {

	/**
	 * Load an outgoing invoice from a generic record
	 */
	public Outgoing(Record entryRecord) {
		super(entryRecord);
	}

	public Record toRecord() {

		Record result = super.toRecord();

		return result;
	}
}
