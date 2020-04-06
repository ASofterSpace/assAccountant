/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.entries;

import com.asofterspace.accountant.world.Category;
import com.asofterspace.toolbox.utils.Record;


/**
 * This represents an incoming invoice (so one that we have to pay)
 */
public class Incoming extends Entry {

	private static final String CATEGORY_KEY = "category";

	private Category category;


	/**
	 * Load an incoming invoice from a generic record
	 */
	public Incoming(Record entryRecord) {
		super(entryRecord);

		category = Category.valueOf(entryRecord.getString(CATEGORY_KEY));
	}

	public Record toRecord() {

		Record result = super.toRecord();

		result.set(CATEGORY_KEY, category);

		return result;
	}
}
