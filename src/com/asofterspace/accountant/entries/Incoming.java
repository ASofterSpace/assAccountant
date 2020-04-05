/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.entries;

import com.asofterspace.accountant.world.Category;


/**
 * This represents an incoming invoice (so one that we have to pay)
 */
public class Incoming extends Entry {

	private static final CATEGORY_KEY = "category";

	private Category category;


	/**
	 * Load an incoming invoice from a generic record
	 */
	public Incoming(Record entryRecord) {
		super(entryRecord);
	}

	public Record toRecord() {

		Record result = super.toRecord();

		result.set(CATEGORY_KEY, category);

		return result;
	}
}
