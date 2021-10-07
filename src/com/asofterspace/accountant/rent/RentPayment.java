/**
 * Unlicensed code created by A Softer Space, 2021
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.rent;

import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.Recordable;

import java.util.Date;


public class RentPayment implements Recordable {

	private final static String WHO = "who";

	private final static String DATE = "date";

	private final static String AMOUNT = "amount";

	private String who;

	private Date date;

	private int amount;


	public RentPayment(Record rec) {
		this.who = rec.getString(WHO);
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

	public String getWho() {
		return who;
	}

	public void setWho(String who) {
		this.who = who;
	}

	@Override
	public Record toRecord() {
		Record rec = Record.emptyObject();
		rec.set(WHO, who);
		rec.set(DATE, date);
		rec.set(AMOUNT, amount);
		return rec;
	}

}
