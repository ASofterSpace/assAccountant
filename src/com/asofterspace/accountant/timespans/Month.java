/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.timespans;

import com.asofterspace.accountant.entries.Donation;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;


/**
 * This represents a month of accounting data
 */
public class Month {

	private List<Outgoing> outgoings;

	private List<Incoming> incomings;

	private List<Donation> donations;

}
