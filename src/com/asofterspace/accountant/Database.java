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
import com.asofterspace.accountant.world.Category;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Database {

	private static final String YEARS_KEY = "years";
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

	// the following two fields are used for storing information used during legacy bulk imports only
	private List<String> potentialCustomers;
	private Map<String, Category> titleToCategoryMapping;


	public Database(ConfigFile settings) throws JsonParseException {

		this.settingsFile = settings;
		this.currentBackup = settingsFile.getInteger(CURRENT_BACKUP_KEY, 0);

		this.dbFile = new ConfigFile("database", true);

		this.loadedRoot = loadFromFile(dbFile);

		// only used during bulk import of legacy data
		potentialCustomers = new ArrayList<>();
		potentialCustomers.add("TPZ-Vega");
		potentialCustomers.add("Recoded");
		potentialCustomers.add("Skyhook");
		potentialCustomers.add("SuperVision Earth");

		titleToCategoryMapping = new HashMap<>();

		titleToCategoryMapping.put("FF43", Category.PERSONAL);

		titleToCategoryMapping.put("Wikimedia", Category.DONATION);
		titleToCategoryMapping.put("Patreon", Category.DONATION);
		titleToCategoryMapping.put("Against Malaria", Category.DONATION);
		titleToCategoryMapping.put("Projekt Gutenberg", Category.DONATION);
		titleToCategoryMapping.put("Internet Archive", Category.DONATION);
		titleToCategoryMapping.put("StrongMinds", Category.DONATION);

		titleToCategoryMapping.put("Fiverr", Category.EXTERNAL_SALARY);

		titleToCategoryMapping.put("Fahrkarte", Category.TRAVEL);
		titleToCategoryMapping.put("Ticket", Category.TRAVEL);
		titleToCategoryMapping.put("Bahn", Category.TRAVEL);
		titleToCategoryMapping.put("Flixbus", Category.TRAVEL);
		titleToCategoryMapping.put("Travel", Category.TRAVEL);
		titleToCategoryMapping.put("RMV", Category.TRAVEL);
		titleToCategoryMapping.put("VBB", Category.TRAVEL);
		titleToCategoryMapping.put("SNCF", Category.TRAVEL);
		titleToCategoryMapping.put("inoui", Category.TRAVEL);

		titleToCategoryMapping.put("Google", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Gsuite", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("GSuite", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Speicherzentrum", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Speicheranbieter", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Microsoft", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Mindfactory", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Mobatek", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Oculus", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Computer Game", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Conrad", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Snapmaker", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Proengeno", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Vive", Category.INFRASTRUCTURE);
		titleToCategoryMapping.put("Vroo", Category.INFRASTRUCTURE);

		titleToCategoryMapping.put("ESAW", Category.EDUCATION);
		titleToCategoryMapping.put("Expo", Category.EDUCATION);

		titleToCategoryMapping.put("Sixt", Category.VEHICLE);

		titleToCategoryMapping.put("Druckerei", Category.ADVERTISEMENTS);
		titleToCategoryMapping.put("OnlinePrinters", Category.ADVERTISEMENTS);
	}

	private Record loadFromFile(ConfigFile fileToLoad) {

		this.years = new ArrayList<>();

		// create a default database file, if necessary
		if (fileToLoad.getAllContents().isEmpty()) {
			try {
				fileToLoad.setAllContents(new JSON("{\"" + YEARS_KEY + "\":[]}"));
			} catch (JsonParseException e) {
				System.err.println("JSON parsing failed internally: " + e);
			}
		}

		Record root = fileToLoad.getAllContents();
		List<Record> yearRecs = root.getArray(YEARS_KEY);
		for (Record yearRec : yearRecs) {
			Year curYear = new Year(yearRec, this);
			years.add(curYear);
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

	public List<Year> getYears() {
		return years;
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

	public boolean addYear(int yearNum) {
		for (Year cur : years) {
			if (cur.getNum() == yearNum) {
				return false;
			}
		}
		years.add(new Year(yearNum, this));
		save();
		return true;
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

	public void bulkImportIncomings(SimpleFile bulkFile) {

		List<String> lines = bulkFile.getContents();

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

	public Category mapTitleToCategory(String titleStr) {

		for (Map.Entry<String, Category> entry : titleToCategoryMapping.entrySet()) {
			if (titleStr.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return Category.OTHER;
	}

	public void bulkImportOutgoings(SimpleFile bulkFile) {

		List<String> lines = bulkFile.getContents();

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

	public void save() {

		Record root = Record.emptyObject();
		Record yearRec = Record.emptyArray();
		root.set(YEARS_KEY, yearRec);

		for (Year year : years) {
			yearRec.append(year.toRecord());
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
