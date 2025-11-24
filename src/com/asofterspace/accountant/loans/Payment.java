/**
 * Unlicensed code created by A Softer Space, 2021
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.loans;

import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.Recordable;

import java.util.Date;


public class Payment implements Recordable {

	private final static String DATE = "date";

	private final static String AMOUNT = "amount";

	private final static String COMMENT = "comment";

	private Date date;

	private int amount;

	private String comment;


	public Payment(Date date, int amount, String comment) {
		this.date = date;
		this.amount = amount;
		this.comment = comment;
	}

	public Payment(Record rec) {
		this.date = rec.getDate(DATE);
		this.amount = rec.getInteger(AMOUNT, 0);
		this.comment = rec.getString(COMMENT);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public Record toRecord() {
		Record rec = Record.emptyObject();
		rec.set(DATE, date);
		rec.set(AMOUNT, amount);
		if (comment != null) {
			if (!"".equals(comment)) {
				rec.set(COMMENT, comment);
			}
		}
		return rec;
	}

}
