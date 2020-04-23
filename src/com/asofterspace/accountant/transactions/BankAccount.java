/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.transactions;

import com.asofterspace.toolbox.utils.Record;

import java.util.ArrayList;
import java.util.List;


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

	private List<BankTransaction> transactions;


	public static BankAccount fromRecord(Record rec) {

		BankAccount result = new BankAccount();

		result.bank = rec.getString(BANK_KEY);

		result.iban = rec.getString(IBAN_KEY);

		result.accountHolder = rec.getString(OWNER_KEY);

		result.transactions = new ArrayList<>();

		List<Record> recs = rec.getArray(TRANSACTIONS_KEY);
		for (Record trans : recs) {
			result.transactions.add(BankTransaction.fromRecord(trans));
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

	public List<BankTransaction> getTransactions() {
		return transactions;
	}

}
