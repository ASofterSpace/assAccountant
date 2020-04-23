/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.transactions;

import java.util.List;


/**
 * This represents a generic bank account which contains transactions
 */
public class BankAccount {

	private String bank;

	private String iban;

	private String accountHolder;

	private List<BankTransaction> transactions;

}
