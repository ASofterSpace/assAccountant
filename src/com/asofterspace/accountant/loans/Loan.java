/**
 * Unlicensed code created by A Softer Space, 2021
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.loans;

import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.Recordable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Loan implements Recordable {

	private final static String PAYMENTS = "payments";

	private final static String CREATED_ON = "createdOn";

	private final static String AMOUNT = "amount";

	private final static String NAME = "name";

	private String name;

	private int amount;

	private Date createdOn;

	private List<Payment> payments;


	public Loan(String name) {
		this.name = name;
	}

	public Loan(Record rec) {
		this.name = rec.getString(NAME);
		this.amount = rec.getInteger(AMOUNT, 0);
		this.createdOn = rec.getDate(CREATED_ON);
		this.payments = new ArrayList<>();
		List<Record> paymentRecs = rec.getArray(PAYMENTS);
		for (Record paymentRec : paymentRecs) {
			this.payments.add(new Payment(paymentRec));
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public List<Payment> getPayments() {
		return payments;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}

	@Override
	public Record toRecord() {
		Record rec = Record.emptyObject();
		rec.set(NAME, name);
		rec.set(AMOUNT, amount);
		rec.set(CREATED_ON, createdOn);
		rec.set(PAYMENTS, payments);
		return rec;
	}

}
