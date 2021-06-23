/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.transactions;

import com.asofterspace.toolbox.utils.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * This represents a generic bank account which contains transactions
 */
public class BankAccount {

	private static final String BANK_KEY = "bank";
	private static final String IBAN_KEY = "iban";
	private static final String BIC_KEY = "bic";
	private static final String OWNER_KEY = "owner";
	private static final String TRANSACTIONS_KEY = "transactions";

	private String bank;

	private String iban;

	private String bic;

	private String accountHolder;

	private List<BankTransaction> transactions;


	private BankAccount() {
	}

	public BankAccount(String bank, String iban, String bic, String accountHolder) {
		this.bank = bank;
		this.iban = iban;
		this.bic = bic;
		this.accountHolder = accountHolder;
		this.transactions = new ArrayList<>();
	}

	public static BankAccount fromRecord(Record rec) {

		BankAccount result = new BankAccount();

		result.bank = rec.getString(BANK_KEY);

		result.iban = rec.getString(IBAN_KEY);

		result.bic = rec.getString(BIC_KEY);

		result.accountHolder = rec.getString(OWNER_KEY);

		result.transactions = new ArrayList<>();

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

		result.set(BIC_KEY, bic);

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
		if (iban == null) {
			return "N/A";
		}
		return iban;
	}

	public void setIbanOptionally(String newIban) {
		if (newIban != null) {
			this.iban = newIban;
		}
	}

	public String getBic() {
		if (bic == null) {
			return "N/A";
		}
		return bic;
	}

	public void setBicOptionally(String newBic) {
		if (newBic != null) {
			this.bic = newBic;
		}
	}

	public String getAccountHolder() {
		if (accountHolder == null) {
			return "N/A";
		}
		return accountHolder;
	}

	public void setAccountHolderOptionally(String newAccountHolder) {
		if (newAccountHolder != null) {
			this.accountHolder = newAccountHolder;
		}
	}

	public void addInfoOptionally(BankAccount other) {
		setAccountHolderOptionally(other.accountHolder);
		setIbanOptionally(other.iban);
		setBicOptionally(other.bic);
	}

	public List<BankTransaction> getTransactions() {

		List<BankTransaction> result = new ArrayList<>();
		result.addAll(transactions);

		sortTransactions(result);

		return result;
	}

	public void addTransaction(BankTransaction newTrans) {
		if (!transactions.contains(newTrans)) {
			transactions.add(newTrans);
		}
		sortTransactions(transactions);
	}

	public void addAllTransactions(List<BankTransaction> newTransactions) {
		for (BankTransaction newTrans : newTransactions) {
			if (!transactions.contains(newTrans)) {
				transactions.add(newTrans);
			}
		}
		sortTransactions(transactions);
	}

	private void sortTransactions(List<BankTransaction> transactionList) {

		Collections.sort(transactionList, new Comparator<BankTransaction>() {
			public int compare(BankTransaction a, BankTransaction b) {
				// break ties using the title (such that a higher number in a title gets sorted to the top)
				if (a.getDate().equals(b.getDate())) {
					return b.getTitle().compareTo(a.getTitle());
				}
				// usually, compare using the date
				return b.getDate().compareTo(a.getDate());
			}
		});
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
			// match null to any, or else require exact match
			if ((this.iban != null) && (otherBankAccount.iban != null)) {
				if (!this.iban.equals(otherBankAccount.iban)) {
					return false;
				}
			}
			// match null to any, or else require exact match
			if ((this.bic != null) && (otherBankAccount.bic != null)) {
				if (!this.bic.equals(otherBankAccount.bic)) {
					return false;
				}
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
