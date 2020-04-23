/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.transactions;

import java.util.Date;


/**
 * This represents a generic bank transaction which is not a main entry
 * reflected in the system (the assAccountant mainly handles invoices),
 * but also being monitored because why not :)
 */
public class BankTransaction {

	private Integer amount;

	private String title;

	private Date date;

}
