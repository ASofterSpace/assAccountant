/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;


public class FinanceLogEntryRow {

	private String account;

	private Integer amount;


	public FinanceLogEntryRow(String account, Integer amount) {
		this.account = account;
		this.amount = amount;
	}

	public String getAccount() {
		return account;
	}

	public Integer getAmount() {
		return amount;
	}
}
