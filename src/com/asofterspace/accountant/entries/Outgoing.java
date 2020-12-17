/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.entries;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.world.Category;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.utils.Record;

import java.util.Date;


/**
 * This represents an outgoing payment (so an invoice we receive that we have to pay)
 */
public class Outgoing extends Entry {

	private static final String CATEGORY_KEY = "category";

	private Category category;


	/**
	 * Create an outgoing payment (received invoice) at runtime
	 */
	public Outgoing(Integer amount, Currency currency, Integer taxationPercent, Integer postTaxAmount,
		Date date, String title, String originator, Category category, Month parent) {

		super(amount, currency, taxationPercent, postTaxAmount, date, title, originator, parent);

		this.category = category;
	}

	/**
	 * Load an outgoing payment (received invoice) from a generic record
	 */
	public Outgoing(Record entryRecord, Month parent) {
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
			parent.removeOutgoing(this);
			database.save();
		}
	}

	@Override
	public String getCategoryOrCustomer() {
		return getCategoryAsText();
	}

}
