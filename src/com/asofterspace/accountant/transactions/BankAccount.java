/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.transactions;

import com.asofterspace.toolbox.utils.Record;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This represents a generic bank account which contains transactions
 */
public class BankAccount {

	private static final String BANK_KEY = "bank";
	private static final String IBAN_KEY = "iban";
	private static final String OWNER_KEY = "owner";
	private static final String TRANSACTIONS_KEY = "transactions";

	private String bank;

	private String iban;

	private String accountHolder;

	// we use a set here, such that we automatically only get each transaction once
	private Set<BankTransaction> transactions;


	private BankAccount() {
	}

	public BankAccount(String bank, String iban, String accountHolder) {
		this.bank = bank;
		this.iban = iban;
		this.accountHolder = accountHolder;
		this.transactions = new HashSet<>();
	}

	public static BankAccount fromRecord(Record rec) {

		BankAccount result = new BankAccount();

		result.bank = rec.getString(BANK_KEY);

		result.iban = rec.getString(IBAN_KEY);

		result.accountHolder = rec.getString(OWNER_KEY);

		result.transactions = new HashSet<>();

		List<Record> recs = rec.getArray(TRANSACTIONS_KEY);
		for (Record trans : recs) {
			result.transactions.add(BankTransaction.fromRecord(trans, result));
		}

		return result;
	}

	public Record toRecord() {

		Record result = Record.emptyObject();

		result.set(BANK_KEY, bank);

		result.set(IBAN_KEY, iban);

		result.set(OWNER_KEY, accountHolder);

		Record transRec = Record.emptyArray();
		result.set(TRANSACTIONS_KEY, transRec);
		for (BankTransaction trans : transactions) {
			transRec.append(trans.toRecord());
		}

		return result;
	}

	public String getBank() {
		return bank;
	}

	public String getIban() {
		return iban;
	}

	public String getAccountHolder() {
		return accountHolder;
	}

	public Set<BankTransaction> getTransactions() {
		return transactions;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof BankAccount) {
			BankAccount otherBankAccount = (BankAccount) other;
			if (!this.bank.equals(otherBankAccount.bank)) {
				 return false;
			}
			if (!this.iban.equals(otherBankAccount.iban)) {
				 return false;
			}
			if (!this.accountHolder.equals(otherBankAccount.accountHolder)) {
				 return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.iban.hashCode();
	}

}
