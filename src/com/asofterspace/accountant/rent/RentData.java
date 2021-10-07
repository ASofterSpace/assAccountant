/**
 * Unlicensed code created by A Softer Space, 2021
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.rent;

import com.asofterspace.accountant.loans.Payment;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.Recordable;

import java.util.ArrayList;
import java.util.List;


public class RentData implements Recordable {

	private final static String MONTHS = "months";

	private final static String TARGET = "target";

	private List<RentMonth> months;

	private List<RentPayment> target;


	public RentData(Record rec) {
		this.months = new ArrayList<>();
		if (rec != null) {
			List<Record> monthRecs = rec.getArray(MONTHS);
			for (Record monthRec : monthRecs) {
				this.months.add(new RentMonth(monthRec));
			}
		}

		this.target = new ArrayList<>();
		if (rec != null) {
			List<Record> targetRecs = rec.getArray(TARGET);
			for (Record targetRec : targetRecs) {
				this.target.add(new RentPayment(targetRec));
			}
		}
	}

	public List<RentMonth> getMonths() {
		return months;
	}

	public void setMonths(List<RentMonth> months) {
		this.months = months;
	}

	public List<RentPayment> getTarget() {
		return target;
	}

	public void setTarget(List<RentPayment> target) {
		this.target = target;
	}

	@Override
	public Record toRecord() {
		Record rec = Record.emptyObject();
		rec.set(MONTHS, months);
		rec.set(TARGET, target);
		return rec;
	}

}
