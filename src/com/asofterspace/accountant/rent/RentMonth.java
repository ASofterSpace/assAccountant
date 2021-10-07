/**
 * Unlicensed code created by A Softer Space, 2021
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.rent;

import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.Recordable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RentMonth implements Recordable {

	private final static String PAYMENTS = "payments";

	private final static String DATE = "date";

	private final static String VERIFIED_OKAY = "verifiedOkay";

	private List<RentPayment> payments;

	private Date date;

	private Boolean verifiedOkay;


	public RentMonth(Record rec) {
		this.verifiedOkay = rec.getBoolean(VERIFIED_OKAY, false);
		this.date = rec.getDate(DATE);
		this.payments = new ArrayList<>();
		List<Record> paymentRecs = rec.getArray(PAYMENTS);
		for (Record paymentRec : paymentRecs) {
			this.payments.add(new RentPayment(paymentRec));
		}
	}

	public List<RentPayment> getPayments() {
		return payments;
	}

	public void setPayments(List<RentPayment> payments) {
		this.payments = payments;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Boolean getVerifiedOkay() {
		return verifiedOkay;
	}

	public void setVerifiedOkay(Boolean verifiedOkay) {
		this.verifiedOkay = verifiedOkay;
	}

	@Override
	public Record toRecord() {
		Record rec = Record.emptyObject();
		rec.set(PAYMENTS, payments);
		rec.set(DATE, date);
		rec.set(VERIFIED_OKAY, verifiedOkay);
		return rec;
	}

}
