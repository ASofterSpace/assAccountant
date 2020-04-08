/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.timespans;

import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.world.Category;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.StrUtils;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;


/**
 * This represents a month of accounting data
 */
public class Month {

	private final static String MONTH_NUM_KEY = "monthNum";
	private final static String OUTGOING_KEY = "outgoings";
	private final static String INCOMING_KEY = "incomings";
	private final static String[] MONTH_NUM_TO_NAME = {
		"January",
		"February",
		"March",
		"April",
		"May",
		"June",
		"July",
		"August",
		"September",
		"October",
		"November",
		"December"
	};

	private Year year;

	private int monthNum;

	private List<Outgoing> outgoings;

	private List<Incoming> incomings;


	/**
	 * Create a new month
	 */
	public Month(int monthNum, Year year) {

		this.year = year;

		this.monthNum = monthNum;

		this.outgoings = new ArrayList<>();
		this.incomings = new ArrayList<>();
	}

	/**
	 * Load a month from a generic record
	 */
	public Month(Record monthRecord, Year year) {

		this.year = year;

		this.monthNum = monthRecord.getInteger(MONTH_NUM_KEY);

		this.outgoings = new ArrayList<>();
		this.incomings = new ArrayList<>();

		for (Record rec : monthRecord.getArray(OUTGOING_KEY)) {
			outgoings.add(new Outgoing(rec, this));
		}
		for (Record rec : monthRecord.getArray(INCOMING_KEY)) {
			incomings.add(new Incoming(rec, this));
		}
	}

	public Record toRecord() {

		Record result = Record.emptyArray();

		result.set(MONTH_NUM_KEY, (Integer) monthNum);

		Record outgoingRec = Record.emptyArray();
		result.set(OUTGOING_KEY, outgoingRec);
		Record incomingRec = Record.emptyArray();
		result.set(INCOMING_KEY, incomingRec);

		for (Outgoing entry : outgoings) {
			outgoingRec.append(entry.toRecord());
		}
		for (Incoming entry : incomings) {
			incomingRec.append(entry.toRecord());
		}

		return result;
	}

	public Year getYear() {
		return year;
	}

	public int getNum() {
		return monthNum;
	}

	public List<Outgoing> getOutgoings() {
		Collections.sort(outgoings, new Comparator<Outgoing>() {
			public int compare(Outgoing a, Outgoing b) {
				return b.getDate().compareTo(a.getDate());
			}
		});
		return outgoings;
	}

	public List<Incoming> getIncomings() {
		Collections.sort(incomings, new Comparator<Incoming>() {
			public int compare(Incoming a, Incoming b) {
				return b.getDate().compareTo(a.getDate());
			}
		});
		return incomings;
	}

	public void removeOutgoing(Outgoing remEntry) {
		outgoings.remove(remEntry);
	}

	public void removeIncoming(Incoming remEntry) {
		incomings.remove(remEntry);
	}

	public boolean addEntry(Date date, String title, Object catOrCustomerObj, String amountStr,
		Currency currency, String taxationPercentStr, boolean isIncoming) {

		Integer amountObj = StrUtils.parseMoney(amountStr);

		if (amountObj == null) {
			return complain("The text " + amountStr + " could not be parsed as amount of money!");
		}

		int amount = amountObj;

		int taxationPercent = 0;
		if (!"".equals(taxationPercentStr)) {
			taxationPercentStr = taxationPercentStr.replaceAll(" ", "");
			taxationPercentStr = taxationPercentStr.replaceAll("%", "");
			taxationPercentStr = taxationPercentStr.replaceAll("€", "");
			if (taxationPercentStr.contains(".")) {
				taxationPercentStr = taxationPercentStr.substring(0, taxationPercentStr.indexOf("."));
			}
			if (taxationPercentStr.contains(",")) {
				taxationPercentStr = taxationPercentStr.substring(0, taxationPercentStr.indexOf(","));
			}
			try {
				taxationPercent = Integer.parseInt(taxationPercentStr);
			} catch (NullPointerException | NumberFormatException e) {
				return complain("The text " + taxationPercentStr + " could not be parsed as integer!");
			}
		}

		if (catOrCustomerObj == null) {
			return complain("The no category or customer entered!");
		}

		String catOrCustomer = catOrCustomerObj.toString();

		if (isIncoming) {

			Category category = Category.fromString(catOrCustomer);

			if (category == null) {
				return complain("The text " + catOrCustomer + " could not be parsed as category!");
			}

			Incoming newIn = new Incoming(amount, currency, taxationPercent, date, title, category, this);
			incomings.add(newIn);

		} else {

			String customer = catOrCustomer;

			if ((customer == null) || "".equals(customer)) {
				return complain("The text " + catOrCustomer + " should contain a customer!");
			}

			Outgoing newOut = new Outgoing(amount, currency, taxationPercent, date, title, customer, this);
			outgoings.add(newOut);
		}

		return true;
	}

	private boolean complain(String complainAbout) {

		JOptionPane.showMessageDialog(
			null,
			complainAbout,
			Utils.getProgramTitle(),
			JOptionPane.ERROR_MESSAGE
		);

		// we return false, which can then immediately be returned by the caller
		return false;
	}

	@Override
	public String toString() {
		return year.toString() + ": " + MONTH_NUM_TO_NAME[monthNum];
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof Month) {
			// months are equal if they represent the same month - in the same year!
			Month otherMonth = (Month) other;
			if (monthNum == otherMonth.monthNum) {
				return year.getNum() == otherMonth.getYear().getNum();
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return monthNum;
	}
}
