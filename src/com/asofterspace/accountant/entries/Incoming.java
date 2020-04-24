/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.entries;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.world.Category;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.utils.Record;

import java.util.Date;


/**
 * This represents an incoming invoice (so one that we have to pay)
 */
public class Incoming extends Entry {

	private static final String CATEGORY_KEY = "category";

	private Category category;


	/**
	 * Create an incoming invoice at runtime
	 */
	public Incoming(Integer amount, Currency currency, Integer taxationPercent, Date date,
		String title, String originator, Category category, Month parent) {

		super(amount, currency, taxationPercent, date, title, originator, parent);

		this.category = category;
	}

	/**
	 * Load an incoming invoice from a generic record
	 */
	public Incoming(Record entryRecord, Month parent) {
		super(entryRecord, parent);

		category = Category.valueOf(entryRecord.getString(CATEGORY_KEY));
	}

	@Override
	public Record toRecord() {

		Record result = super.toRecord();

		result.set(CATEGORY_KEY, category);

		return result;
	}

	public Category getCategory() {
		return category;
	}

	public String getCategoryAsText() {
		if (category == null) {
			return "";
		}
		return category.getText();
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	@Override
	public void deleteFrom(Database database) {
		if (parent != null) {
			parent.removeIncoming(this);
			database.save();
		}
	}

	@Override
	public String getCategoryOrCustomer() {
		return getCategoryAsText();
	}

}
