/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.loans.Loan;
import com.asofterspace.accountant.rent.RentData;
import com.asofterspace.accountant.tasks.TaskCtrl;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.transactions.BankAccount;
import com.asofterspace.accountant.transactions.BankTransaction;
import com.asofterspace.accountant.world.Category;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.accounting.FinanceUtils;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.gui.GuiUtils;
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

	private final static String MONTHLY_OUTGOING = "monthlyOutgoing";
	private final static String MONTHLY_INCOMING = "monthlyIncoming";
	private static final String YEARS_KEY = "years";
	private static final String BULK_IMPORT_CUSTOMERS_KEY = "bulkImportCustomers";
	private static final String CATEGORY_MAPPINGS_KEY = "categoryMappings";
	private static final String CATEGORY_MAPPINGS_CONTAINS_KEY = "contains";
	private static final String CATEGORY_MAPPINGS_CATEGORY_KEY = "category";
	private static final String ACKNOWLEDGEMENTS_KEY = "acknowledgements";
	private final static String CUSTOMER_TO_SHORT_KEY_MAPPING = "customerToShortKeyMapping";
	private static final String BANK_ACCOUNTS_KEY = "bankAccounts";
	private static final String BACKUP_FILE_NAME = "database_backup_";
	private static final String CURRENT_BACKUP_KEY = "currentBackup";
	private static final String INCOME_TAXES_KEY = "incomeTaxes";
	private static final String RENT_KEY = "rent";
	private static final String LOANS_KEY = "loans";
	private static final String FORMAT_KEY = "format";
	private static final String INFO_KEY = "info";

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
	private List<String> acknowledgedProblems;
	private Map<String, Object> customerToShortKeyMapping;

	private List<Record> monthlyIncoming;
	private List<Record> monthlyOutgoing;

	private Integer port;

	private String username;

	private String userLegalName;

	private String location;

	private String formatStr;

	private String info;

	private List<Loan> loans;

	private RentData rentData;

	private Record root;


	public Database(ConfigFile settings) throws JsonParseException {

		this.settingsFile = settings;
		this.currentBackup = settingsFile.getInteger(CURRENT_BACKUP_KEY, 0);

		this.dbFile = new ConfigFile("database", true);

		this.loadedRoot = loadFromFile(dbFile);

		this.port = settingsFile.getInteger("port");

		this.username = settingsFile.getValue("username");

		this.userLegalName = settingsFile.getValue("userLegalName");

		this.location = settingsFile.getValue("location");
	}

	/**
	 * This function (and many others in here) are synchronized, such that when Hugo starts calling via
	 * the ServerRequestHandler, the results are only served when the database is fully loaded
	 */
	private synchronized Record loadFromFile(ConfigFile fileToLoad) {

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

		root = fileToLoad.getAllContents();

		// ensure that at least the current year and the year which the next month belongs to
		// are automatically added (yes, this means that from 1st December on, the next year
		// is already shown, just containing no data yet - unless we already start adding some)
		Date today = DateUtils.now();
		int curYearNum = DateUtils.getYear(today);
		int yearNextMonthNum = curYearNum;
		if (DateUtils.getMonth(today) > 11) {
			yearNextMonthNum++;
		}
		boolean encounteredCurYear = false;
		boolean encounteredYearNextMonth = false;

		// years containing months, themselves containing outgoing and incoming entries
		List<Record> yearRecs = root.getArray(YEARS_KEY);
		for (Record yearRec : yearRecs) {
			Year curYear = new Year(yearRec, this);
			years.add(curYear);

			if (curYear.getNum() == curYearNum) {
				encounteredCurYear = true;
			}
			if (curYear.getNum() == yearNextMonthNum) {
				encounteredYearNextMonth = true;
			}
		}

		if (!encounteredCurYear) {
			years.add(new Year(curYearNum, this));
		}
		if (!encounteredYearNextMonth) {
			years.add(new Year(yearNextMonthNum, this));
		}

		sortYears();

		// bank accounts containing transactions
		bankAccounts = new ArrayList<>();
		List<Record> recs = root.getArray(BANK_ACCOUNTS_KEY);
		for (Record rec : recs) {
			BankAccount cur = BankAccount.fromRecord(rec);
			BankAccount existing = null;
			for (BankAccount prev : bankAccounts) {
				if (prev.equals(cur)) {
					existing = prev;
				}
			}
			if (existing == null) {
				bankAccounts.add(cur);
			} else {
				existing.addAllTransactions(cur.getTransactions());
				existing.addInfoOptionally(cur);
			}
		}

		// only used during bulk import of legacy data
		potentialCustomers = root.getArrayAsStringList(BULK_IMPORT_CUSTOMERS_KEY);

		// outgoing money: map received invoice texts to received invoice categories
		titleToCategoryMapping = new HashMap<>();
		List<Record> catMappings = root.getArray(CATEGORY_MAPPINGS_KEY);
		for (Record catMapping : catMappings) {
			titleToCategoryMapping.put(
				catMapping.getString(CATEGORY_MAPPINGS_CONTAINS_KEY),
				Category.fromString(catMapping.getString(CATEGORY_MAPPINGS_CATEGORY_KEY))
			);
		}

		// read out acknowledgements
		acknowledgedProblems = root.getArrayAsStringList(ACKNOWLEDGEMENTS_KEY);

		customerToShortKeyMapping = root.getObjectMap(CUSTOMER_TO_SHORT_KEY_MAPPING);

		monthlyIncoming = root.getArray(MONTHLY_INCOMING);
		monthlyOutgoing = root.getArray(MONTHLY_OUTGOING);

		rentData = new RentData(root.get(RENT_KEY));

		List<Record> loanRecs = root.getArray(LOANS_KEY);
		loans = new ArrayList<>();
		for (Record loanRec : loanRecs) {
			loans.add(new Loan(loanRec));
		}

		formatStr = root.getString(FORMAT_KEY);
		if (formatStr == null) {
			formatStr = "EN";
		}

		info = root.getString(INFO_KEY);
		if (info == null) {
			info = "N/A";
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
		result.add("Other");
		for (Year year : years) {
			for (Month month : year.getMonths()) {
				for (Incoming incoming : month.getIncomings()) {
					if (incoming.getCustomer() != null) {
						result.add(incoming.getCustomer());
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns the originators that are contained in the database, newest one first
	 * (Because we want to maintain that ordering we are using a List rather than just
	 * a Set, even though we do want to keep all entries unique.)
	 */
	public List<String> getOriginators() {
		List<String> result = new ArrayList<>();
		for (Year year : years) {
			for (Month month : year.getMonths()) {
				for (Entry entry : month.getEntries()) {
					String originator = entry.getOriginator();
					if ((originator != null) && !result.contains(originator)) {
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

			if (gui != null) {
				gui.showTab(null);
				gui.regenerateTabList();
			}

		} catch (JsonParseException e) {
			System.err.println("JSON parsing failed for " + BACKUP_FILE_NAME + currentBackup + ": " + e);
		}
	}

	// adds an entry and returns the id of the new entry
	public String addEntry(Date date, String title, Object catOrCustomer, String amount,
		Currency currency, String taxationPercent, String postTaxAmount, String originator, String incoKind, boolean isOutgoing) {

		Month curMonth = getMonthFromEntryDate(date);

		if (curMonth == null) {
			return null;
		}

		String newId = curMonth.addEntry(date, title, catOrCustomer, amount, currency, taxationPercent,
			postTaxAmount, originator, incoKind, isOutgoing, this);

		if (newId != null) {
			save();
		}

		return newId;
	}

	public Year getYearFromEntryDate(Date date) {
		return getYearFromEntryDate(date, 0);
	}

	public Year getPrevYearFromEntryDate(Date date) {
		return getYearFromEntryDate(date, -1);
	}

	public Year getYearFromNumIfExists(int yearNum) {

		for (Year year : years) {
			if (year.getNum() == yearNum) {
				return year;
			}
		}

		return null;
	}

	private Year getYearFromEntryDate(Date date, int offset) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int yearNum = calendar.get(Calendar.YEAR);

		yearNum += offset;

		addYear(yearNum);
		Year curYear = null;
		for (Year year : years) {
			if (year.getNum() == yearNum) {
				curYear = year;
				break;
			}
		}
		if (curYear == null) {
			GuiUtils.complain("The entry could not be added as the year " + yearNum +
					" - which we just added! - went missing again...");
			return null;
		}

		return curYear;
	}

	public Month getMonthFromEntryDate(Date date) {
		return getMonthFromEntryDate(date, 0);
	}

	public Month getPrevMonthFromEntryDate(Date date) {
		return getMonthFromEntryDate(date, -1);
	}

	private Month getMonthFromEntryDate(Date date, int offset) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int yearNum = calendar.get(Calendar.YEAR);
		int monthNum = calendar.get(Calendar.MONTH);
		monthNum += offset;
		if (monthNum < 0) {
			monthNum += 12;
			yearNum--;
		}

		addYear(yearNum);
		Year curYear = null;
		for (Year year : years) {
			if (year.getNum() == yearNum) {
				curYear = year;
				break;
			}
		}
		if (curYear == null) {
			GuiUtils.complain("The entry could not be added as the year " + yearNum +
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
			GuiUtils.complain("The entry could not be added as the month " + monthNum +
					" is missing from year " + yearNum + "...");
			return null;
		}

		return curMonth;
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

				Category category = mapTitleToCategory(titleStr);

				Date date = DateUtils.parseDate(dateStr);
				if (date == null) {
					System.out.println("The text " + dateStr + " could not be parsed as date! " +
						"Please use YYYY-MM-DD or DD. MM. YYYY as date format.");
					break;
				}

				if (addEntry(date, titleStr, category.getText(), amountStr, Currency.EUR, taxationPercentStr, null, "", null, true) == null) {
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

				String customer = "";
				for (String potentialCustomer : potentialCustomers) {
					if (titleStr.contains(potentialCustomer)) {
						customer = potentialCustomer;
					}
				}

				Date date = DateUtils.parseDate(dateStr);
				if (date == null) {
					System.out.println("The text " + dateStr + " could not be parsed as date! " +
						"Please use YYYY-MM-DD or DD. MM. YYYY as date format.");
					break;
				}

				if (addEntry(date, titleStr, customer, amountStr, Currency.EUR, taxationPercentStr, null, "", null, false) == null) {
					// stop upon the first failure instead of showing a million error messages
					break;
				}
			}
		}

		save();
	}

	public String bulkImportBankStatements(List<File> bulkFiles) {

		StringBuilder result = new StringBuilder();

		for (File bulkFile : bulkFiles) {

			String filetype = bulkFile.getFiletype().toLowerCase();

			switch (filetype) {

				case "csv":
					result.append(bulkImportBankStatementsFromCsv(new CsvFile(bulkFile)));
					break;

				case "pdf":
					result.append(bulkImportBankStatementsFromPdf(new PdfFile(bulkFile)));
					break;

				default:
					result.append("\nThe file " + bulkFile.getFilename() + " has type " +
						filetype.toUpperCase() + ", which cannot be imported!");
					break;
			}
		}

		save();

		return result.toString();
	}

	@SuppressWarnings("fallthrough")
	public String bulkImportBankStatementsFromCsv(CsvFile csv) {

		String headLine = csv.getHeadLine();
		int offsetForOldCategory = 0;

		switch (headLine) {

			// old kind of CSV with Category
			case "\"Date\",\"Payee\",\"Account number\",\"Transaction type\",\"Payment reference\",\"Category\",\"Amount (EUR)\",\"Amount (Foreign Currency)\",\"Type Foreign Currency\",\"Exchange Rate\"":
				offsetForOldCategory = 1;

			// new kind of CSV without Category
			case "\"Date\",\"Payee\",\"Account number\",\"Transaction type\",\"Payment reference\",\"Amount (EUR)\",\"Amount (Foreign Currency)\",\"Type Foreign Currency\",\"Exchange Rate\"":
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
					int amount = FinanceUtils.parseMoney(line.get(5+offsetForOldCategory));
					curAccount.addTransaction(new BankTransaction(amount, content, date, curAccount));
					line = csv.getContentLineInColumns();
				}
				break;

			default:
				return "\nThe input file " + csv.getFilename() + " does not belong to a known bank!";
		}
		return "";
	}

	public String bulkImportBankStatementsFromPdf(PdfFile pdf) {

		List<PdfObject> objs = pdf.getObjects();

		StringBuilder pdfPlainText = new StringBuilder();

		for (PdfObject obj : objs) {

			try {
				String contentPiece = obj.getPlainStreamContent();
				if (contentPiece != null) {
					pdfPlainText.append(contentPiece);
					pdfPlainText.append("\n");
				}
			} catch (Exception e) {
				// whoops!
			}
		}

		String pdfText = pdfPlainText.toString();

		pdfText = pdfText.replaceAll("\\\\045", "%");
		pdfText = pdfText.replaceAll("\\\\334", "Ü");
		pdfText = pdfText.replaceAll("\\\\337", "ß");
		pdfText = pdfText.replaceAll("\\\\344", "ä");
		pdfText = pdfText.replaceAll("\\\\366", "ö");
		pdfText = pdfText.replaceAll("\\\\374", "ü");
		pdfText = pdfText.replaceAll("\\\\\\(", "(");
		pdfText = pdfText.replaceAll("\\\\\\)", ")");
		// do not leave backslashes in, as we don't want them to wreak havoc with stuff later...
		pdfText = pdfText.replaceAll("\\\\", "Ux");

		SimpleFile outFile = new SimpleFile("data/pdf_debug.txt"); // DEBUG
		outFile.saveContent(pdfText); // DEBUG

		if (pdfText.contains("(Sparda-) Tj\n21.35 0.00 Td (Bank ) Tj\n15.34 0.00 Td (Berlin ) Tj\n17.01 0.00 Td (eG) Tj") ||
			pdfText.contains("(Sparda-) Tj\n21.35 0.00 Td (Bank ) Tj\n15.35 0.00 Td (Berlin ) Tj\n17.01 0.00 Td (eG) Tj")) {
			return bulkImportBankStatementsFromPdfForSpardaUntil2018(pdf, pdfText);
		}
		if (pdfText.contains("\n[(Sparda-Bank Berlin eG)]TJ")) {
			return bulkImportBankStatementsFromPdfForSparda(pdf, pdfText);
		}
		if (pdfText.contains("\n(DEUTSCHE KREDITBANK AG)Tj")) {
			return bulkImportBankStatementsFromPdfForDKB(pdf, pdfText);
		}
		if (pdfText.contains("\n[(GLS Gemeinschaftsbank eG)]TJ")) {
			return bulkImportBankStatementsFromPdfForGLS(pdf, pdfText);
		}
		return "The input file " + pdf.getFilename() + " does not belong to a known bank!";
	}

	/**
	 * We have a text like:
	 * foo.bar (bla blubb)Tj
	 * foo.bar (blubb2)Tj
	 * ET
	 * foo.bar (bla blubb)Tj
	 *
	 * We call this with endsWith = ET
	 *
	 * Then the result will be:
	 * bla blubbblubb2
	 */
	private String concatPdfCellsUntil(String text, String endsWith) {

		String result = "";

		while (text.contains("\n")) {
			String line = text.substring(0, text.indexOf("\n"));
			text = text.substring(text.indexOf("\n") + 1);
			if (line.startsWith(endsWith)) {
				return result;
			}
			if (line.contains("(") && line.contains(")")) {
				line = line.substring(line.indexOf("(") + 1);
				line = line.substring(0, line.indexOf(")"));
				result += line;
			}
		}

		return result;
	}

	private String bulkImportBankStatementsFromPdfForSpardaUntil2018(PdfFile pdf, String pdfText) {

		String bank = "Sparda";
		String iban = null;
		String bic = null;
		String owner = null;
		int lastLeftPos = 0;
		int leftPos = 0;

		if (pdfText.contains("76.30 732.54 Td (IBAN: ) Tj")) {
			iban = pdfText.substring(pdfText.indexOf("76.30 732.54 Td (IBAN: ) Tj") + "76.30 732.54 Td (IBAN: ) Tj".length());
			iban = concatPdfCellsUntil(iban, "ET");
			iban = iban.replaceAll(" ", "");
		}
		if (pdfText.contains("76.30 740.75 Td (IBAN: ) Tj")) {
			iban = pdfText.substring(pdfText.indexOf("76.30 740.75 Td (IBAN: ) Tj") + "76.30 740.75 Td (IBAN: ) Tj".length());
			iban = concatPdfCellsUntil(iban, "ET");
			iban = iban.replaceAll(" ", "");
		}

		if (pdfText.contains("72.90 30.76 Td (BIC: ) Tj")) {
			bic = pdfText.substring(pdfText.indexOf("72.90 30.76 Td (BIC: ) Tj"));
			bic = bic.substring(bic.indexOf("(") + 1);
			bic = bic.substring(bic.indexOf("(") + 1);
			bic = bic.substring(0, bic.indexOf(")"));
			bic = bic.replaceAll(" ", "");
		}
		if (pdfText.contains("72.90 37.86 Td (BIC: ) Tj")) {
			bic = pdfText.substring(pdfText.indexOf("72.90 37.86 Td (BIC: ) Tj"));
			bic = bic.substring(bic.indexOf("(") + 1);
			bic = bic.substring(bic.indexOf("(") + 1);
			bic = bic.substring(0, bic.indexOf(")"));
			bic = bic.replaceAll(" ", "");
		}

		if ((iban == null) && (bic == null)) {
			GuiUtils.complain("Could read out neither IBAN nor BIC for " + pdf.getFilename() + "!");
		}

		if (pdfText.contains("/F0 12.0 Tf")) {
			owner = pdfText.substring(pdfText.indexOf("/F0 12.0 Tf"));
			owner = concatPdfCellsUntil(owner, "ET");
		}
		BankAccount curAccount = getOrAddBankAccount(bank, iban, bic, owner);

		if (pdfText.contains("76.30 667.63 Td (") || pdfText.contains("76.30 675.83 Td (")) {

			String transStr = null;
			if (pdfText.contains("76.30 667.63 Td (")) {
				transStr = pdfText.substring(pdfText.lastIndexOf("/F1 8.0 Tf", pdfText.indexOf("76.30 667.63 Td (")) - 1);
			}
			if (pdfText.contains("76.30 675.83 Td (")) {
				transStr = pdfText.substring(pdfText.lastIndexOf("/F1 8.0 Tf", pdfText.indexOf("76.30 675.83 Td (")) - 1);
			}

			// tracks occurrences of /F1 8.0 Tf
			// 0 .. date, 1 .. title, 2 .. second date, 3 .. amount
			int whatNow = 0;

			boolean properExit = false;
			Date curDate = null;
			Integer amount = null;
			String curEntryStr = null;
			BankTransaction lastTransaction;

			while (transStr.contains("/F1 8.0 Tf")) {

				if (transStr.indexOf("/F0 8.0 Tf") < transStr.indexOf("/F1 8.0 Tf")) {
					properExit = true;
					break;
				}

				transStr = transStr.substring(transStr.indexOf("/F1 8.0 Tf") + 2);

				String beforeStr = transStr.substring(0, transStr.indexOf("("));

				// before Str contains something like:
				// ...
				// 451.49 417.56 Td
				// we want to get the first of the two numbers
				beforeStr = beforeStr.substring(beforeStr.lastIndexOf("\n") + 1);
				beforeStr = beforeStr.substring(0, beforeStr.indexOf(" "));
				if (beforeStr.contains(".")) {
					beforeStr = beforeStr.substring(0, beforeStr.indexOf("."));
				}
				lastLeftPos = leftPos;
				leftPos = Integer.parseInt(beforeStr);

				String curStr = concatPdfCellsUntil(transStr, "ET");

				switch (whatNow) {
					case 0:
						// jump to the next page
						if (curStr.contains("Übertrag")) {
							transStr = transStr.substring(transStr.lastIndexOf("/F1 8.0 Tf", transStr.indexOf("76.30 700.98 Td (")) - 1);
							whatNow--;
							break;
						}
						// ignore empty fields (just jump over them - there is one just before the end anyway...)
						if ("".equals(curStr.trim())) {
							whatNow--;
							break;
						}
						curDate = DateUtils.parseDate(curStr);
						amount = null;
						break;
					case 1:
						curEntryStr = curStr;
						break;
					case 2:
						// if we have not advanced to the right, then we are in another line for the title
						if (lastLeftPos == leftPos) {
							curEntryStr += "\n" + curStr;
							whatNow--;
						}
						break;
					case 3:
						amount = FinanceUtils.parseMoney(curStr);
						break;
				}
				whatNow++;

				if (whatNow > 3) {
					if (amount == null) {
						GuiUtils.complain("The amount of a bank statement in " + pdf.getFilename() + " could not be read!");
					} else {
						if (curDate == null) {
							GuiUtils.complain("The date of a bank statement in " + pdf.getFilename() + " could not be read!");
						} else {
							lastTransaction = new BankTransaction(amount, curEntryStr, curDate, curAccount);
							curAccount.addTransaction(lastTransaction);
						}
					}
					whatNow = 0;
				}
			}
			if (!properExit) {
				GuiUtils.complain("We did not exit the parsing of " + pdf.getFilename() + " as expected, some entries may have been missed!");
			}
		}
		return "";
	}

	private String bulkImportBankStatementsFromPdfForSparda(PdfFile pdf, String pdfText) {

		// until March 2o21, bracketMode was false - now it is true, which will be detected based on the IBAN
		boolean bracketMode = false;

		String bank = "Sparda";
		String iban = null;
		String bic = null;
		String owner = null;
		if (pdfText.contains("[(IBAN: ")) {
			iban = pdfText.substring(pdfText.indexOf("[(IBAN: ") + 8);
			bic = iban.substring(iban.indexOf("BIC:") + 4);
			iban = iban.substring(0, iban.indexOf("BIC:"));
			if (iban.startsWith(")")) {
				iban = "(" + iban + ")";
				bracketMode = true;
				iban = getContentFromBrackets(iban);
			}
			iban = iban.replaceAll(" ", "");

			if (bracketMode) {
				bic = "(" + bic.substring(0, bic.indexOf("]TJ"));
				bic = getContentFromBrackets(bic);
			} else {
				bic = bic.substring(0, bic.indexOf(")"));
			}
			bic = bic.replaceAll(" ", "");
		}
		if (pdfText.contains("-29.8181 -1.2424 Td")) {
			owner = pdfText.substring(pdfText.indexOf("-29.8181 -1.2424 Td"));
			owner = owner.substring(owner.indexOf("[(") + 2);
			owner = owner.substring(0, owner.indexOf(")]"));
			if (bracketMode) {
				owner = "(" + owner + ")";
				owner = getContentFromBrackets(owner);
			}
		}
		BankAccount curAccount = getOrAddBankAccount(bank, iban, bic, owner);

		String VORGANG = "[(Vorgang)]TJ";
		if (bracketMode) {
			VORGANG = "[(Vorgan)-2.30769(g)]TJ";
		}

		if (pdfText.contains(VORGANG)) {
			String transStr = pdfText.substring(pdfText.indexOf(VORGANG) + VORGANG.length());
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
					if (bracketMode) {
						curEntryStr = "(" + curEntryStr + ")";
						curEntryStr = getContentFromBrackets(curEntryStr);
					}
					curDateStr = curEntryStr.substring(0, 6) + curYear;
					curDate = DateUtils.parseDate(curDateStr);
					curEntryStr = curEntryStr.substring(14);

					// search for the second-last " "
					int amountIndex = curEntryStr.lastIndexOf(" ");
					amountIndex = curEntryStr.lastIndexOf(" ", amountIndex - 1) + 1;
					String amountStr = curEntryStr.substring(amountIndex);

					// H: positive, S: negative
					if (amountStr.endsWith("S")) {
						amountStr = "-" + amountStr;
					}
					amountStr = amountStr.substring(0, amountStr.length() - 1);
					amount = FinanceUtils.parseMoney(amountStr);
					curEntryStr = curEntryStr.substring(0, amountIndex);
					curEntryStr = curEntryStr.trim();
					continue;
				}
				if (curEntryStr == null) {
					continue;
				}
				// regularly, we expect to end here by using the break
				if ((transStr.startsWith("                                            Übertrag")) ||
					(transStr.startsWith(" )-26060.66666(Übertrag )")) ||
					(transStr.startsWith("                                    neuer Kontostand vom")) ||
					(transStr.startsWith(" )-21212.18181(neuer )-0.36363(Kontostand )-0.66666(vom )"))) {
					// finalize previous entry
					if (curEntryStr != null) {
						curAccount.addTransaction(new BankTransaction(amount, curEntryStr, curDate, curAccount));
					}

					if (transStr.contains(VORGANG)) {
						transStr = transStr.substring(transStr.indexOf(VORGANG) + VORGANG.length());
						curEntryStr = null;
					} else {
						properExit = true;
						break;
					}
				} else {
					if (bracketMode) {
						curEntryStr += "\n" + getContentFromBrackets("(" + transStr.substring(0, transStr.indexOf(")]")).trim() + ")");
					} else {
						curEntryStr += "\n" + transStr.substring(0, transStr.indexOf(")]")).trim();
					}
				}
			}
			if (!properExit) {
				GuiUtils.complain("We did not exit the parsing of the PDF as expected, some entries may have been missed!");
			}
		}
		return "";
	}

	// takes in "bla (foo ) blubb (bar)" and returns "foo bar"
	private String getContentFromBrackets(String str) {
		StringBuilder result = new StringBuilder();
		while (str.contains("(")) {
			str = str.substring(str.indexOf("(") + 1);
			if (str.contains(")")) {
				result.append(str.substring(0, str.indexOf(")")));
				str = str.substring(str.indexOf(")") + 1);
			}
		}
		return result.toString();
	}

	private String bulkImportBankStatementsFromPdfForDKB(PdfFile pdf, String pdfText) {

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
							amount = null;
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
							amount = FinanceUtils.parseMoney(curStr);
						}
						break;
				}
				whatNow++;

				if (whatNow > 3) {
					if (amount == null) {
						GuiUtils.complain("The amount of a bank statement in " + pdf.getFilename() + " could not be read!");
					} else {
						if (curDate == null) {
							GuiUtils.complain("The date of a bank statement in " + pdf.getFilename() + " could not be read!");
						} else {
							lastTransaction = new BankTransaction(amount, curEntryStr, curDate, curAccount);
							curAccount.addTransaction(lastTransaction);
						}
					}
					whatNow = 0;
				}
			}
			if (!properExit) {
				GuiUtils.complain("We did not exit the parsing of " + pdf.getFilename() + " as expected, some entries may have been missed!");
			}
		}
		return "";
	}

	private String bulkImportBankStatementsFromPdfForGLS(PdfFile pdf, String pdfText) {

		String bank = "GLS";
		String iban = null;
		String bic = null;
		String owner = null;

		if (pdfText.contains("[(IBAN: ")) {
			iban = pdfText.substring(pdfText.indexOf("[(IBAN: ") + 8);
			bic = iban.substring(iban.indexOf("BIC: ") + 5);
			bic = bic.substring(0, bic.indexOf(")"));
			bic = bic.replaceAll(" ", "");
			iban = iban.substring(0, iban.indexOf("BIC:"));
			iban = iban.replaceAll(" ", "");
		}

		if (pdfText.contains("9.9 0 0 9.9 39.6 708.3 Tm")) {
			owner = pdfText.substring(pdfText.indexOf("9.9 0 0 9.9 39.6 708.3 Tm"));
			owner = owner.substring(owner.indexOf("[(") + 1);
			owner = owner.substring(0, owner.indexOf(")]"));
		}
		BankAccount curAccount = getOrAddBankAccount(bank, iban, bic, owner);

		if (pdfText.contains("-40.8 -1.3333 Td")) {

			String transStr = pdfText.substring(pdfText.indexOf("-40.8 -1.3333 Td") + 1);
			Date curDate = null;
			String curEntryStr = null;
			Integer amount = null;
			String curYear = null;
			int lastLeftPos = 0;
			int leftPos = 0;
			BankTransaction lastTransaction = null;

			if (pdfText.contains("2.9999 -3.0303 Td")) {
				curYear = pdfText.substring(pdfText.indexOf("2.9999 -3.0303 Td"));
				curYear = curYear.substring(curYear.indexOf("[(") + 2);
				curYear = curYear.substring(curYear.indexOf("/") + 1);
				curYear = curYear.substring(0, 4);
			}

			boolean properExit = false;

			while (transStr.contains("[(")) {

				if (transStr.contains("9 0 0 9 39.6 620.4 Tm")) {
					if (transStr.indexOf("9 0 0 9 39.6 620.4 Tm") < transStr.indexOf("0 -1.3333 Td")) {
						transStr = transStr.substring(transStr.indexOf("9 0 0 9 39.6 620.4 Tm"));
					}
				}

				transStr = transStr.substring(transStr.indexOf("[(") + 2);

				if (transStr.startsWith("Kontoabschluss vom ")) {
					properExit = true;
					break;
				}

				String curStr = transStr.substring(0, transStr.indexOf(")]TJ"));

				if ((curStr.charAt(2) == '.') && (curStr.charAt(5) == '.')) {
					String curDateStr = curStr.substring(0, 6) + curYear;
					curDate = DateUtils.parseDate(curDateStr);

					curEntryStr = curStr.substring(14);
					String amountStr = curEntryStr.substring(0, curEntryStr.lastIndexOf(" "));
					amountStr = amountStr.substring(amountStr.lastIndexOf(" ") + 1);
					if (curEntryStr.charAt(curEntryStr.length() - 1) == 'S') {
						amountStr = "-" + amountStr;
					}
					amount = FinanceUtils.parseMoney(amountStr);
					curEntryStr = curEntryStr.substring(0, curEntryStr.length() - (amountStr.length() + 1));
					curEntryStr = curEntryStr.trim();

					if (amount == null) {
						GuiUtils.complain("The amount of a bank statement in " + pdf.getFilename() + " could not be read!");
					} else {
						if (curDate == null) {
							GuiUtils.complain("The date of a bank statement in " + pdf.getFilename() + " could not be read!");
						} else {
							lastTransaction = new BankTransaction(amount, curEntryStr, curDate, curAccount);
							curAccount.addTransaction(lastTransaction);
						}
					}
				} else {
					curEntryStr += "\n" + curStr.trim();
					if (lastTransaction != null) {
						lastTransaction.setTitle(curEntryStr);
					}
				}
			}
			if (!properExit) {
				GuiUtils.complain("We did not exit the parsing of " + pdf.getFilename() + " as expected, some entries may have been missed!");
			}
		}
		return "";
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

	public synchronized List<Incoming> getIncomings() {
		List<Incoming> result = new ArrayList<>();
		for (Year year : getYears()) {
			result.addAll(year.getIncomings());
		}
		return result;
	}

	public synchronized List<Outgoing> getOutgoings() {
		List<Outgoing> result = new ArrayList<>();
		for (Year year : getYears()) {
			result.addAll(year.getOutgoings());
		}
		return result;
	}

	public synchronized List<Entry> getEntries() {
		List<Entry> result = new ArrayList<>();
		for (Year year : getYears()) {
			result.addAll(year.getEntries());
		}
		return result;
	}

	public synchronized Entry getEntry(String id) {
		for (Year year : getYears()) {
			for (Entry entry : year.getEntries()) {
				if (entry.hasId(id)) {
					return entry;
				}
			}
		}
		return null;
	}

	public synchronized List<Entry> getEntriesOrdered() {
		List<Entry> result = new ArrayList<>();
		for (Year year : getYears()) {
			result.addAll(year.getEntries());
		}
		AccountingUtils.sortEntries(result);
		return result;
	}

	public synchronized List<Problem> getProblems() {

		List<Problem> result = new ArrayList<>();

		for (Entry entry : getEntriesOrdered()) {
			entry.reportProblemsTo(result);
		}

		return result;
	}

	public synchronized List<Problem> getUnacknowledgedProblems() {

		List<Problem> result = new ArrayList<>();

		for (Problem problem : getProblems()) {
			if (!acknowledgedProblems.contains(problem.getProblem())) {
				result.add(problem);
			}
		}

		return result;
	}

	public synchronized List<PaymentProblem> getPaymentProblems() {

		List<PaymentProblem> result = new ArrayList<>();

		for (Problem problem : getProblems()) {
			if (problem instanceof PaymentProblem) {
				result.add((PaymentProblem) problem);
			}
		}

		return result;
	}

	public void acknowledge(String problemStr) {
		acknowledgedProblems.add(problemStr);
		save();
	}

	/**
	 * Get all un-acknowledged consistency problems
	 */
	public synchronized List<ConsistencyProblem> getUnacknowledgedConsistencyProblems() {

		List<ConsistencyProblem> result = new ArrayList<>();

		for (Problem problem : getProblems()) {
			if (problem instanceof ConsistencyProblem) {
				if (!acknowledgedProblems.contains(problem.getProblem())) {
					result.add((ConsistencyProblem) problem);
				}
			}
		}

		return result;
	}

	public Record getLoadedRoot() {
		return loadedRoot;
	}

	public File getDbFile() {
		return dbFile;
	}

	public void drop() {

		if (gui != null) {
			gui.showTab(null);
		}

		years = new ArrayList<>();

		save();
	}

	public void dropBankStatements() {

		bankAccounts = new ArrayList<>();

		save();
	}

	public Integer getPort() {
		if (port == null) {
			return 3011;
		}
		return port;
	}

	public String getUsername() {
		return username;
	}

	public void save() {

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

		root.set(ACKNOWLEDGEMENTS_KEY, acknowledgedProblems);

		root.set(CUSTOMER_TO_SHORT_KEY_MAPPING, customerToShortKeyMapping);

		root.set(MONTHLY_INCOMING, monthlyIncoming);
		root.set(MONTHLY_OUTGOING, monthlyOutgoing);

		root.set(RENT_KEY, rentData);

		root.set(LOANS_KEY, loans);

		root.set(FORMAT_KEY, formatStr);

		root.set(INFO_KEY, info);

		taskCtrl.saveIntoRecord(root);

		dbFile.setAllContents(root);

		currentBackup++;
		overflowCheckBackups();
		new ConfigFile(BACKUP_FILE_NAME + currentBackup, true, root);

		if (gui != null) {
			gui.refreshOpenTab();
		}
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

	public String getUserLegalName() {
		return userLegalName;
	}

	public String getLocation() {
		return location;
	}

	public String mapCustomerToShortKey(String customer) {

		if (customerToShortKeyMapping.get(customer) != null) {
			return customerToShortKeyMapping.get(customer).toString();
		}
		return customer.substring(0, 3).toUpperCase();
	}

	public List<Record> getMonthlyIncoming() {
		return monthlyIncoming;
	}

	public List<Record> getMonthlyOutgoing() {
		return monthlyOutgoing;
	}

	public Map<Integer, Integer> getIncomeTaxes() {
		Map<String, Record> incomeTaxMap = root.getValueMap(INCOME_TAXES_KEY);
		Map<Integer, Integer> result = new HashMap<>();
		for (Map.Entry<String, Record> entry : incomeTaxMap.entrySet()) {
			String key = entry.getKey();
			Record rec = entry.getValue();
			result.put(StrUtils.strToInt(key), rec.asInteger());
		}
		return result;
	}

	public RentData getRentData() {
		return rentData;
	}

	public void setRentData(RentData rentData) {
		this.rentData = rentData;
	}

	public List<Loan> getLoans() {
		return loans;
	}

	public void setLoans(List<Loan> loans) {
		this.loans = loans;
	}

	public String getFormatStr() {
		return formatStr;
	}

	public void setFormatStr(String formatStr) {
		this.formatStr = formatStr;
		// no need to save here - we could, but this is not important enough, honestly...
		// and it is better to have the advantage of sometimes not having saved yet (e.g. for editing details
		// of recurring entries which have not "really" been released yet ^^)
	}

	public String formatMoney(Integer amount) {
		if ("DE".equals(formatStr)) {
			return FinanceUtils.formatMoneyDE(amount);
		} else {
			return FinanceUtils.formatMoneyEN(amount);
		}
	}

	public String formatMoney(Integer amount, Currency currency) {
		if ("DE".equals(formatStr)) {
			return FinanceUtils.formatMoneyDE(amount, currency);
		} else {
			return FinanceUtils.formatMoneyEN(amount, currency);
		}
	}

	public String getInfo() {
		return info;
	}

}
