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

	private final static String ACTIVE = "active";

	private final static String KIND = "kind";
	private final static String THEY_OWE = "THEY_OWE";
	private final static String WE_OWE = "WE_OWE";

	private final static String PAYMENTS = "payments";

	private final static String CREATED_ON = "createdOn";

	private final static String AMOUNT = "amount";

	private final static String DETAILS = "details";

	private final static String NAME = "name";


	private boolean active;

	private String kind;

	private String name;

	private String details;

	private int amount;

	private Date createdOn;

	private List<Payment> payments;


	public Loan(String name) {
		this.name = name;
	}

	public Loan(Record rec) {

		this.active = rec.getBoolean(ACTIVE, true);

		this.kind = rec.getString(KIND);
		if (!THEY_OWE.equals(this.kind)) {
			this.kind = WE_OWE;
		}

		this.name = rec.getString(NAME);
		this.details = rec.getString(DETAILS);
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

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
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

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getKind() {
		return kind;
	}

	public String getKindDisplayStr() {
		if (THEY_OWE.equals(kind)) {
			return "[WE ARE OWED - they need to pay]";
		}
		return "[WE OWE THIS - we need to pay]";
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	@Override
	public Record toRecord() {
		Record rec = Record.emptyObject();
		rec.set(ACTIVE, active);
		rec.set(KIND, kind);
		rec.set(NAME, name);
		rec.set(DETAILS, details);
		rec.set(AMOUNT, amount);
		rec.set(CREATED_ON, createdOn);
		rec.set(PAYMENTS, payments);
		return rec;
	}

}
