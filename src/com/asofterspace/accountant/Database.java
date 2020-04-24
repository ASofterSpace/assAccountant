/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.transactions.BankAccount;
import com.asofterspace.accountant.transactions.BankTransaction;
import com.asofterspace.accountant.world.Category;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.CsvFile;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.pdf.PdfFile;
import com.asofterspace.toolbox.pdf.PdfObject;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.StrUtils;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Database {

	private static final String YEARS_KEY = "years";
	private static final String BULK_IMPORT_CUSTOMERS_KEY = "bulkImportCustomers";
	private static final String CATEGORY_MAPPINGS_KEY = "categoryMappings";
	private static final String CATEGORY_MAPPINGS_CONTAINS_KEY = "contains";
	private static final String CATEGORY_MAPPINGS_CATEGORY_KEY = "category";
	private static final String BANK_ACCOUNTS_KEY = "bankAccounts";
	private static final String BACKUP_FILE_NAME = "database_backup_";
	private static final String CURRENT_BACKUP_KEY = "currentBackup";

	private static int BACKUP_MAX = 64;

	private GUI gui;
	private TaskCtrl taskCtrl;

	private ConfigFile settingsFile;

	// we take backups in a ring buffer on every save
	// (which we can use also to undo any action!)
	private int currentBackup;

	private ConfigFile dbFile;

	private Record loadedRoot;
	private List<Year> years;
	private List<BankAccount> bankAccounts;

	// the following two fields are used for storing information used during legacy bulk imports only
	private List<String> potentialCustomers;
	private Map<String, Category> titleToCategoryMapping;


	public Database(ConfigFile settings) throws JsonParseException {

		this.settingsFile = settings;
		this.currentBackup = settingsFile.getInteger(CURRENT_BACKUP_KEY, 0);

		this.dbFile = new ConfigFile("database", true);

		this.loadedRoot = loadFromFile(dbFile);
	}

	private Record loadFromFile(ConfigFile fileToLoad) {

		this.years = new ArrayList<>();

		// create a default database file, if necessary
		if (fileToLoad.getAllContents().isEmpty()) {
			try {
				fileToLoad.setAllContents(new JSON(
					"{\"" + YEARS_KEY + "\":[], " +
					"\"" + BULK_IMPORT_CUSTOMERS_KEY + "\":[], " +
					"\"" + CATEGORY_MAPPINGS_KEY + "\":[]}"
				));
			} catch (JsonParseException e) {
				System.err.println("JSON parsing failed internally: " + e);
			}
		}

		Record root = fileToLoad.getAllContents();

		// years containing months, themselves containing incoming and outgoing entries
		List<Record> yearRecs = root.getArray(YEARS_KEY);
		for (Record yearRec : yearRecs) {
			Year curYear = new Year(yearRec, this);
			years.add(curYear);
		}
		sortYears();

		// bank accounts containing transactions
		bankAccounts = new ArrayList<>();
		List<Record> recs = root.getArray(BANK_ACCOUNTS_KEY);
		for (Record rec : recs) {
			bankAccounts.add(BankAccount.fromRecord(rec));
		}

		// only used during bulk import of legacy data
		potentialCustomers = root.getArrayAsStringList(BULK_IMPORT_CUSTOMERS_KEY);

		// map incoming invoice texts to incoming invoice categories
		titleToCategoryMapping = new HashMap<>();
		List<Record> catMappings = root.getArray(CATEGORY_MAPPINGS_KEY);
		for (Record catMapping : catMappings) {
			titleToCategoryMapping.put(
				catMapping.getString(CATEGORY_MAPPINGS_CONTAINS_KEY),
				Category.fromString(catMapping.getString(CATEGORY_MAPPINGS_CATEGORY_KEY))
			);
		}
		return root;
	}

	public void setGUI(GUI gui) {
		this.gui = gui;
	}

	public GUI getGUI() {
		return gui;
	}

	public void setTaskCtrl(TaskCtrl taskCtrl) {
		this.taskCtrl = taskCtrl;
	}

	public TaskCtrl getTaskCtrl() {
		return taskCtrl;
	}

	// gets the main years for which we have entries etc.
	public List<Year> getYears() {
		return years;
	}

	// gets the years for which we have bank statements but no entries
	public List<Integer> getBankStatementOnlyYears() {
		List<Integer> result = new ArrayList<>();
		for (BankAccount bA : bankAccounts) {
			for (BankTransaction bT : bA.getTransactions()) {
				int curYear = DateUtils.getYear(bT.getDate());
				boolean foundYear = false;
				for (Year year : years) {
					if (curYear == year.getNum()) {
						foundYear = true;
						break;
					}
				}
				for (Integer year : result) {
					if (curYear == (int) year) {
						foundYear = true;
						break;
					}
				}
				if (!foundYear) {
					result.add(curYear);
				}
			}
		}
		return result;
	}

	public Set<String> getCustomers() {
		Set<String> result = new HashSet<>();
		for (Year year : years) {
			for (Month month : year.getMonths()) {
				for (Outgoing outgoing : month.getOutgoings()) {
					if (outgoing.getCustomer() != null) {
						result.add(outgoing.getCustomer());
					}
				}
			}
		}
		return result;
	}

	public Set<String> getOriginators() {
		Set<String> result = new HashSet<>();
		for (Year year : years) {
			for (Month month : year.getMonths()) {
				for (Entry entry : month.getEntries()) {
					if (entry.getOriginator() != null) {
						result.add(entry.getOriginator());
					}
				}
			}
		}
		return result;
	}

	public Set<String> getAccounts() {
		Set<String> result = new HashSet<>();
		for (Year year : years) {
			for (Month month : year.getMonths()) {
				for (Entry entry : month.getEntries()) {
					if (entry.getReceivedOnAccount() != null) {
						result.add(entry.getReceivedOnAccount());
					}
				}
			}
		}
		return result;
	}

	public List<BankAccount> getBankAccounts() {

		Collections.sort(bankAccounts, new Comparator<BankAccount>() {
			public int compare(BankAccount a, BankAccount b) {
				return b.getTransactions().size() - a.getTransactions().size();
			}
		});

		return bankAccounts;
	}

	public boolean addYear(int yearNum) {
		for (Year cur : years) {
			if (cur.getNum() == yearNum) {
				return false;
			}
		}
		years.add(new Year(yearNum, this));
		sortYears();
		save();
		return true;
	}

	private void sortYears() {
		Collections.sort(years, new Comparator<Year>() {
			public int compare(Year a, Year b) {
				return b.getNum() - a.getNum();
			}
		});
	}

	public void undo() {
		currentBackup--;
		applyUndoRedo();
	}

	public void redo() {
		currentBackup++;
		applyUndoRedo();
	}

	public void applyUndoRedo() {

		overflowCheckBackups();

		try {
			Record root = loadFromFile(new ConfigFile(BACKUP_FILE_NAME + currentBackup, true));

			// when undoing and redoing, we want to actually save what we did, so that if we
			// close and re-open the assAccountant, we still have stuff undone, and are not
			// back to the front of the queue!
			dbFile.setAllContents(root);

			gui.showTab(null);
			gui.regenerateTabList();

		} catch (JsonParseException e) {
			System.err.println("JSON parsing failed for " + BACKUP_FILE_NAME + currentBackup + ": " + e);
		}
	}

	public boolean addEntry(String dateStr, String title, Object catOrCustomer, String amount,
		Currency currency, String taxationPercent, String originator, boolean isIncoming) {

		Date date = DateUtils.parseDate(dateStr);
		if (date == null) {
			return AccountingUtils.complain("The text " + dateStr + " could not be parsed as date!\n" +
				"Please use YYYY-MM-DD or DD. MM. YYYY as date format.");
		}

		Month curMonth = getMonthFromEntryDate(date);

		if (curMonth == null) {
			return false;
		}

		if (curMonth.addEntry(date, title, catOrCustomer, amount, currency, taxationPercent, originator, isIncoming)) {

			save();

			return true;
		}

		return false;
	}

	public Month getMonthFromEntryDate(Date date) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int yearNum = calendar.get(Calendar.YEAR);
		int monthNum = calendar.get(Calendar.MONTH);

		addYear(yearNum);
		Year curYear = null;
		for (Year year : years) {
			if (year.getNum() == yearNum) {
				curYear = year;
				break;
			}
		}
		if (curYear == null) {
			AccountingUtils.complain("The entry could not be added as the year " + yearNum +
					" - which we just added! - went missing again...");
			return null;
		}

		Month curMonth = null;
		for (Month month : curYear.getMonths()) {
			if (month.getNum() == monthNum) {
				curMonth = month;
				break;
			}
		}
		if (curMonth == null) {
			AccountingUtils.complain("The entry could not be added as the month " + monthNum +
					" is missing from year " + yearNum + "...");
			return null;
		}

		return curMonth;
	}

	public void bulkImportIncomings(List<File> bulkFiles) {

		for (File bulkFile : bulkFiles) {

			SimpleFile simpleBulkFile = new SimpleFile(bulkFile);

			List<String> lines = simpleBulkFile.getContents();

			for (String line : lines) {

				line = line.trim();

				if ("".equals(line)) {
					continue;
				}

				// we now have a line such as:
				// 27.03.1998	Fahrkarte von A nach B	9,99 €	5%	10,49 €

				String dateStr = line.substring(0, line.indexOf("\t"));
				line = line.substring(line.indexOf("\t") + 1);

				String titleStr = line.substring(0, line.indexOf("\t"));
				line = line.substring(line.indexOf("\t") + 1);

				String amountStr = line.substring(0, line.indexOf("\t"));
				line = line.substring(line.indexOf("\t") + 1);

				String taxationPercentStr = line;
				if (line.indexOf("\t") >= 0) {
					taxationPercentStr = line.substring(0, line.indexOf("\t"));
				}

				Category category = mapTitleToCategory(titleStr);

				if (!addEntry(dateStr, titleStr, category.getText(), amountStr, Currency.EUR, taxationPercentStr, "", true)) {
					// stop upon the first failure instead of showing a million error messages
					break;
				}
			}
		}

		save();
	}

	public Category mapTitleToCategory(String titleStr) {

		for (Map.Entry<String, Category> entry : titleToCategoryMapping.entrySet()) {
			if (titleStr.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return Category.OTHER;
	}

	public void bulkImportOutgoings(List<File> bulkFiles) {

		for (File bulkFile : bulkFiles) {

			SimpleFile simpleBulkFile = new SimpleFile(bulkFile);

			List<String> lines = simpleBulkFile.getContents();

			for (String line : lines) {

				line = line.trim();

				if ("".equals(line)) {
					continue;
				}

				// we now have a line such as:
				// 27.03.1998	Fahrkarte von A nach B	9,99 €	5%	10,49 €

				String dateStr = line.substring(0, line.indexOf("\t"));
				line = line.substring(line.indexOf("\t") + 1);

				String titleStr = line.substring(0, line.indexOf("\t"));
				line = line.substring(line.indexOf("\t") + 1);

				String amountStr = line.substring(0, line.indexOf("\t"));
				line = line.substring(line.indexOf("\t") + 1);

				String taxationPercentStr = line;
				if (line.indexOf("\t") >= 0) {
					taxationPercentStr = line.substring(0, line.indexOf("\t"));
				}

				String customer = "";
				for (String potentialCustomer : potentialCustomers) {
					if (titleStr.contains(potentialCustomer)) {
						customer = potentialCustomer;
					}
				}

				if (!addEntry(dateStr, titleStr, customer, amountStr, Currency.EUR, taxationPercentStr, "", false)) {
					// stop upon the first failure instead of showing a million error messages
					break;
				}
			}
		}

		save();
	}

	public void bulkImportBankStatements(List<File> bulkFiles) {

		for (File bulkFile : bulkFiles) {

			String filetype = bulkFile.getFiletype().toLowerCase();

			switch (filetype) {

				case "csv":
					bulkImportBankStatementsFromCsv(new CsvFile(bulkFile));
					break;

				case "pdf":
					bulkImportBankStatementsFromPdf(new PdfFile(bulkFile));
					break;

				default:
					AccountingUtils.complain("The file " + bulkFile.getFilename() + " has type " +
						filetype.toUpperCase() + ", which cannot be imported!");
					break;
			}
		}

		save();
	}

	public void bulkImportBankStatementsFromCsv(CsvFile csv) {

		String headLine = csv.getHeadLine();

		switch (headLine) {

			case "\"Date\",\"Payee\",\"Account number\",\"Transaction type\",\"Payment reference\",\"Category\",\"Amount (EUR)\",\"Amount (Foreign Currency)\",\"Type Foreign Currency\",\"Exchange Rate\"":
				String bank = "n26";
				String iban = null;
				String bic = "NTSBDEB1XXX";
				String owner = null;

				BankAccount curAccount = getOrAddBankAccount(bank, iban, bic, owner);

				List<String> line = csv.getContentLineInColumns();
				while (line != null) {
					Date date = DateUtils.parseDate(line.get(0));
					String content = line.get(1);
					if (!"".equals(line.get(2).trim())) {
						content += "\n" + line.get(2);
					}
					if (!"".equals(line.get(4).trim())) {
						content += "\n" + line.get(4);
					}
					int amount = StrUtils.parseMoney(line.get(6));
					curAccount.addTransaction(new BankTransaction(amount, content, date, curAccount));
					line = csv.getContentLineInColumns();
				}
				break;

			default:
				AccountingUtils.complain("The input file " + csv.getFilename() + " does not belong to a known bank!");
				break;
		}
	}

	public void bulkImportBankStatementsFromPdf(PdfFile pdf) {

		List<PdfObject> objs = pdf.getObjects();

		StringBuilder pdfPlainText = new StringBuilder();

		for (PdfObject obj : objs) {

			try {
				pdfPlainText.append(obj.getPlainStreamContent());
				pdfPlainText.append("\n");
			} catch (Exception e) {
				// whoops!
			}
		}

		String pdfText = pdfPlainText.toString();

		SimpleFile outFile = new SimpleFile("data/pdf_debug.txt"); // DEBUG
		outFile.saveContent(pdfText); // DEBUG

		if (pdfText.contains("[(Sparda-Bank Berlin eG)]TJ")) {
			pdfText = pdfText.replaceAll("\\\\334", "Ü");
			pdfText = pdfText.replaceAll("\\\\337", "ß");
			pdfText = pdfText.replaceAll("\\\\344", "ä");
			pdfText = pdfText.replaceAll("\\\\374", "ü");
			pdfText = pdfText.replaceAll("\\\\\\(", "(");
			pdfText = pdfText.replaceAll("\\\\\\)", ")");
			// do not leave backslashes in, as we don't want them to wreak havoc with stuff later...
			pdfText = pdfText.replaceAll("\\\\", "Ux");

			String bank = "Sparda";
			String iban = null;
			String bic = null;
			String owner = null;
			if (pdfText.contains("[(IBAN: ")) {
				iban = pdfText.substring(pdfText.indexOf("[(IBAN: ") + 8);
				bic = iban.substring(iban.indexOf("BIC:") + 4);
				bic = bic.substring(0, bic.indexOf(")"));
				bic = bic.replaceAll(" ", "");
				iban = iban.substring(0, iban.indexOf("BIC:"));
				iban = iban.replaceAll(" ", "");
			}
			if (pdfText.contains("-29.8181 -1.2424 Td")) {
				owner = pdfText.substring(pdfText.indexOf("-29.8181 -1.2424 Td"));
				owner = owner.substring(owner.indexOf("[(") + 2);
				owner = owner.substring(0, owner.indexOf(")]"));
			}
			BankAccount curAccount = getOrAddBankAccount(bank, iban, bic, owner);

			if (pdfText.contains("[(Vorgang)]TJ")) {
				String transStr = pdfText.substring(pdfText.indexOf("[(Vorgang)]TJ") + 13);
				String curEntryStr = null;
				String curDateStr = null;
				Date curDate = null;
				Integer amount = null;
				String curYear = null;
				if (pdfText.contains("2.9999 -3.0303 Td")) {
					curYear = pdfText.substring(pdfText.indexOf("2.9999 -3.0303 Td"));
					curYear = curYear.substring(curYear.indexOf("[(") + 2);
					curYear = curYear.substring(curYear.indexOf("/") + 1);
					curYear = curYear.substring(0, curYear.indexOf(")]"));
				}

				boolean properExit = false;

				while (transStr.contains("[(")) {
					transStr = transStr.substring(transStr.indexOf("[(") + 2);
					if ((transStr.charAt(2) == '.') && (transStr.charAt(5) == '.') && (transStr.charAt(6) == ' ')) {
						if (curEntryStr != null) {
							// finalize previous entry
							curAccount.addTransaction(new BankTransaction(amount, curEntryStr, curDate, curAccount));
						}
						curEntryStr = transStr.substring(0, transStr.indexOf(")]"));
						curDateStr = curEntryStr.substring(0, 6) + curYear;
						curDate = DateUtils.parseDate(curDateStr);
						curEntryStr = curEntryStr.substring(14);
						String amountStr = curEntryStr.substring(curEntryStr.lastIndexOf("  ") + 2);
						// H: positive, S: negative
						if (amountStr.endsWith("S")) {
							amountStr = "-" + amountStr;
						}
						amountStr = amountStr.substring(0, amountStr.length() - 1);
						amount = StrUtils.parseMoney(amountStr);
						curEntryStr = curEntryStr.substring(0, curEntryStr.lastIndexOf("  "));
						curEntryStr = curEntryStr.trim();
						continue;
					}
					if (curEntryStr == null) {
						continue;
					}
					// regularly, we expect to end here by using the break
					if ((transStr.startsWith("                                            Übertrag")) ||
						(transStr.startsWith("                                    neuer Kontostand vom"))) {
						// finalize previous entry
						if (curEntryStr != null) {
							curAccount.addTransaction(new BankTransaction(amount, curEntryStr, curDate, curAccount));
						}

						if (transStr.contains("[(Vorgang)]TJ")) {
							transStr = transStr.substring(transStr.indexOf("[(Vorgang)]TJ") + 13);
							curEntryStr = null;
						} else {
							properExit = true;
							break;
						}
					} else {
						curEntryStr += "\n" + transStr.substring(0, transStr.indexOf(")]")).trim();
					}
				}
				if (!properExit) {
					AccountingUtils.complain("We did not exit the parsing of the PDF as expected, some entries may have been missed!");
				}
			}

		} else {
			if (pdfText.contains("(DEUTSCHE KREDITBANK AG)Tj")) {

				String bank = "DKB";
				String iban = null;
				String bic = null;
				String owner = null;
				if (pdfText.contains("\n(IBAN: ")) {
					iban = pdfText.substring(pdfText.indexOf("\n(IBAN: ") + 8);
					iban = iban.substring(0, iban.indexOf(")"));
					iban = iban.replaceAll(" ", "");
				}
				if (pdfText.contains("\n(BIC: ")) {
					bic = pdfText.substring(pdfText.indexOf("\n(BIC: ") + 7);
					bic = bic.substring(0, bic.indexOf(")"));
					bic = bic.replaceAll(" ", "");
				}
				if (pdfText.contains("/F4 10 Tf")) {
					owner = pdfText.substring(pdfText.indexOf("/F4 10 Tf"));
					owner = owner.substring(owner.indexOf("(") + 1);
					owner = owner.substring(0, owner.indexOf(")"));
				}
				BankAccount curAccount = getOrAddBankAccount(bank, iban, bic, owner);

				if (pdfText.contains("(Wir haben für Sie gebucht)Tj")) {
					pdfText = pdfText.replaceAll("\\\\\\(", "(");
					pdfText = pdfText.replaceAll("\\\\\\)", ")");
					// do not leave backslashes in, as we don't want them to wreak havoc with stuff later...
					pdfText = pdfText.replaceAll("\\\\", "Ux");

					String transStr = pdfText.substring(pdfText.indexOf("(Wir haben für Sie gebucht)Tj") + 13);
					Date curDate = null;
					String curEntryStr = null;
					Integer amount = null;
					String curYear = null;
					int lastLeftPos = 0;
					int leftPos = 0;
					BankTransaction lastTransaction = null;
					if (pdfText.contains("(Kontoauszug Nummer ")) {
						curYear = pdfText.substring(pdfText.indexOf("(Kontoauszug Nummer "));
						curYear = pdfText.substring(pdfText.indexOf(" / ") + 3);
						curYear = curYear.substring(0, 4);
					}

					boolean properExit = false;

					// tracks occurrences of /F4 9 Tf
					// 0 .. date, 1 .. second date, 2 .. title, 3 .. amount
					int whatNow = 0;

					while (transStr.contains("/F4 9 Tf")) {

						// here comes Alter Kontostand: - only printed after all statements are done
						if (transStr.indexOf("/F2 9 Tf") < transStr.indexOf("/F4 9 Tf")) {
							properExit = true;
							break;
						}

						String beforeStr = transStr.substring(0, transStr.indexOf("/F4 9 Tf"));

						// before Str contains something like:
						// ...
						// 1 0 0 1 451.49 417.56 Tm
						// we want to get the first of the two numbers, and e.g. if it is below
						// 500, we have a negative amount...
						beforeStr = beforeStr.substring(0, beforeStr.lastIndexOf(" Tm"));
						beforeStr = beforeStr.substring(0, beforeStr.lastIndexOf(" "));
						beforeStr = beforeStr.substring(beforeStr.lastIndexOf(" ") + 1);
						if (beforeStr.contains(".")) {
							beforeStr = beforeStr.substring(0, beforeStr.indexOf("."));
						}
						lastLeftPos = leftPos;
						leftPos = Integer.parseInt(beforeStr);

						transStr = transStr.substring(transStr.indexOf("/F4 9 Tf") + 2);
						transStr = transStr.substring(transStr.indexOf("\n(") + 2);
						String curStr = transStr.substring(0, transStr.indexOf(")Tj"));

						// in some older DKB PDFs, empty cells are explicitly written where no amounts are present
						if ("".equals(curStr)) {
							continue;
						}

						switch (whatNow) {
							case 0:
								// if one entry spilled over two pages, then the first thing that we get is not a valid date,
								// but instead the continuation of the title!
								if ((curStr.length() != 6) || (curStr.charAt(2) != '.') || (curStr.charAt(5) != '.')) {
									curEntryStr += "\n" + curStr;
									// in newer DKB PDFs, the /F4 0 Tf is not printed a second time, but instead the next lines
									// just follow... so we read them here!
									while (transStr.indexOf("\n(") < transStr.indexOf("/F4 9 Tf")) {
										transStr = transStr.substring(transStr.indexOf("\n(") + 2);
										curEntryStr += "\n" + transStr.substring(0, transStr.indexOf(")Tj"));
									}
									if (lastTransaction != null) {
										lastTransaction.setTitle(curEntryStr);
									}
									whatNow--;
								} else {
									curDate = DateUtils.parseDate(curStr + curYear);
								}
								break;
							case 2:
								curEntryStr = curStr;
								while (transStr.indexOf("\n(") < transStr.indexOf("/F4 9 Tf")) {
									transStr = transStr.substring(transStr.indexOf("\n(") + 2);
									curEntryStr += "\n" + transStr.substring(0, transStr.indexOf(")Tj"));
								}
								break;
							case 3:
								// if we have not advanced to the right, then we are in another line for the title
								// (this happens in older DKB PDFs, in newer ones the /F4 0 Tf is not printed another time)
								if (lastLeftPos == leftPos) {
									curEntryStr += "\n" + curStr;
									whatNow--;
								} else {
									if (leftPos < 500) {
										curStr = "-" + curStr;
									}
									amount = StrUtils.parseMoney(curStr);
								}
								break;
						}
						whatNow++;

						if (whatNow > 3) {
							if (amount == null) {
								AccountingUtils.complain("The amount of a bank statement in " + pdf.getFilename() + " could not be read!");
							} else {
								if (curDate == null) {
									AccountingUtils.complain("The date of a bank statement in " + pdf.getFilename() + " could not be read!");
								} else {
									lastTransaction = new BankTransaction(amount, curEntryStr, curDate, curAccount);
									curAccount.addTransaction(lastTransaction);
								}
							}
							whatNow = 0;
						}
					}
					if (!properExit) {
						AccountingUtils.complain("We did not exit the parsing of " + pdf.getFilename() + " as expected, some entries may have been missed!");
					}
				}

			} else {
				AccountingUtils.complain("The input file " + pdf.getFilename() + " does not belong to a known bank!");
			}
		}
	}

	private BankAccount getOrAddBankAccount(String bank, String iban, String bic, String owner) {

		BankAccount curAccount = new BankAccount(bank, iban, bic, owner);

		boolean alreadyExisting = false;

		for (BankAccount bankAccount : bankAccounts) {
			if (curAccount.equals(bankAccount)) {
				curAccount = bankAccount;
				alreadyExisting = true;
				break;
			}
		}

		curAccount.setIbanOptionally(iban);
		curAccount.setBicOptionally(bic);
		curAccount.setAccountHolderOptionally(owner);

		if (!alreadyExisting) {
			bankAccounts.add(curAccount);
		}

		return curAccount;
	}

	public List<Outgoing> getOutgoings() {
		List<Outgoing> result = new ArrayList<>();
		for (Year year : getYears()) {
			result.addAll(year.getOutgoings());
		}
		return result;
	}

	public List<Incoming> getIncomings() {
		List<Incoming> result = new ArrayList<>();
		for (Year year : getYears()) {
			result.addAll(year.getIncomings());
		}
		return result;
	}

	public List<Entry> getEntries() {
		List<Entry> result = new ArrayList<>();
		result.addAll(getOutgoings());
		result.addAll(getIncomings());
		return result;
	}

	public List<Entry> getEntriesOrdered() {
		List<Entry> result = new ArrayList<>();
		result.addAll(getOutgoings());
		result.addAll(getIncomings());
		AccountingUtils.sortEntries(result);
		return result;
	}

	public List<Problem> getProblems() {

		List<Problem> result = new ArrayList<>();

		for (Entry entry : getEntriesOrdered()) {
			entry.reportProblemsTo(result);
		}

		return result;
	}

	public List<PaymentProblem> getPaymentProblems() {

		List<PaymentProblem> result = new ArrayList<>();

		for (Problem problem : getProblems()) {
			if (problem instanceof PaymentProblem) {
				result.add((PaymentProblem) problem);
			}
		}

		return result;
	}

	public List<ConsistencyProblem> getConsistencyProblems() {

		List<ConsistencyProblem> result = new ArrayList<>();

		for (Problem problem : getProblems()) {
			if (problem instanceof ConsistencyProblem) {
				result.add((ConsistencyProblem) problem);
			}
		}

		return result;
	}

	public Record getLoadedRoot() {
		return loadedRoot;
	}

	public void drop() {

		gui.showTab(null);

		years = new ArrayList<>();

		save();
	}

	public void dropBankStatements() {

		bankAccounts = new ArrayList<>();

		save();
	}

	public void save() {

		Record root = Record.emptyObject();
		Record yearRec = Record.emptyArray();
		root.set(YEARS_KEY, yearRec);

		for (Year year : years) {
			yearRec.append(year.toRecord());
		}

		Record bankAccountsRec = Record.emptyArray();
		root.set(BANK_ACCOUNTS_KEY, bankAccountsRec);
		for (BankAccount bankAccount : bankAccounts) {
			bankAccountsRec.append(bankAccount.toRecord());
		}

		Record bulkImportCustomersRec = Record.emptyArray();
		root.set(BULK_IMPORT_CUSTOMERS_KEY, bulkImportCustomersRec);
		for (String potentialCustomer : potentialCustomers) {
			bulkImportCustomersRec.append(potentialCustomer);
		}

		Record titleToCategoryMappingRec = Record.emptyArray();
		root.set(CATEGORY_MAPPINGS_KEY, titleToCategoryMappingRec);
		for (Map.Entry<String, Category> mapping : titleToCategoryMapping.entrySet()) {
			Record mappingRec = Record.emptyObject();
			mappingRec.set(CATEGORY_MAPPINGS_CONTAINS_KEY, mapping.getKey());
			mappingRec.set(CATEGORY_MAPPINGS_CATEGORY_KEY, mapping.getValue().toString());
			titleToCategoryMappingRec.append(mappingRec);
		}

		taskCtrl.saveIntoRecord(root);

		dbFile.setAllContents(root);

		currentBackup++;
		overflowCheckBackups();
		new ConfigFile(BACKUP_FILE_NAME + currentBackup, true, root);

		gui.refreshOpenTab();
	}

	private void overflowCheckBackups() {

		if (currentBackup > BACKUP_MAX) {
			currentBackup = 0;
		}

		if (currentBackup < 0) {
			currentBackup = BACKUP_MAX;
		}

		// we check for backup overflow when we adjusted currentBackup... so may as well save it now :)
		settingsFile.set(CURRENT_BACKUP_KEY, currentBackup);
	}
}
