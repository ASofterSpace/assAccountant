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

	private Date date;

	private int amount;


	public Payment(Date date, int amount) {
		this.date = date;
		this.amount = amount;
	}

	public Payment(Record rec) {
		this.date = rec.getDate(DATE);
		this.amount = rec.getInteger(AMOUNT, 0);
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

	@Override
	public Record toRecord() {
		Record rec = Record.emptyObject();
		rec.set(DATE, date);
		rec.set(AMOUNT, amount);
		return rec;
	}

}
