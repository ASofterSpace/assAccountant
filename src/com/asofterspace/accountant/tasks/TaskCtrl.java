/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tasks;

import com.asofterspace.accountant.Database;
import com.asofterspace.toolbox.calendar.GenericTask;
import com.asofterspace.toolbox.calendar.TaskCtrlBase;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.IoUtils;
import com.asofterspace.toolbox.utils.DateHolder;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class TaskCtrl extends TaskCtrlBase {

	private final String FINANCE_LOGS = "financeLogs";
	private final String FINANCE_OVERVIEW = "financeOverview";

	private List<FinanceLogEntry> financeLogs;

	private Database database;


	public TaskCtrl(Database database) {

		this.database = database;

		database.setTaskCtrl(this);
	}

	public void init() {

		loadFromDatabase();

		generateNewInstances(DateUtils.now());
	}

	private void loadFromDatabase() {

		Record root = database.getLoadedRoot();

		loadFromRoot(root);

		List<Record> financeLogsRecordsInDatabase = root.getArray(FINANCE_LOGS);
		this.financeLogs = new ArrayList<>();
		for (Record curLog : financeLogsRecordsInDatabase) {
			FinanceLogEntry entry = new FinanceLogEntry(DateUtils.parseDate(curLog.getString(DATE)));
			List<Record> logRows = curLog.getArray(ROWS);
			for (Record logRow : logRows) {
				entry.add(new FinanceLogEntryRow(logRow.getString(ACCOUNT), logRow.getInteger(AMOUNT)));
			}
			financeLogs.add(entry);
		}
	}

	@Override
	public void saveIntoRecord(Record root) {
		super.saveIntoRecord(root);
		root.set(FINANCE_LOGS, getFinanceLogsAsRecord());
	}

	@Override
	protected GenericTask createTask(String title, Integer scheduledOnXDayOfMonth, Integer scheduledOnDay,
		List<String> scheduledOnDaysOfWeek, List<Integer> scheduledInMonths, List<Integer> scheduledInYears,
		List<String> details, List<String> onDone, Boolean biweeklyEven, Boolean biweeklyOdd) {

		return new Task(title, scheduledOnXDayOfMonth, scheduledOnDay, scheduledOnDaysOfWeek, scheduledInMonths,
			scheduledInYears, details, onDone, biweeklyEven, biweeklyOdd);
	}

	@Override
	protected GenericTask taskFromRecord(Record recordTask) {

		GenericTask task = super.taskFromRecord(recordTask);

		if (recordTask.getString(KIND).equals(FINANCE_OVERVIEW)) {
			return new FinanceOverviewTask(task);
		}
		if (recordTask.getString(KIND).equals(GENERIC)) {
			return new Task(task);
		}
		GuiUtils.complain("The task " + recordTask.getString(TITLE) + " could not be loaded!");
		return null;
	}

	private Record getFinanceLogsAsRecord() {
		Record base = Record.emptyArray();

		for (FinanceLogEntry logEntry : financeLogs) {
			Record entryRec = Record.emptyObject();
			entryRec.set(DATE, DateUtils.serializeDate(logEntry.getDate()));
			Record rowsRec = Record.emptyArray();
			for (FinanceLogEntryRow row : logEntry.getRows()) {
				Record curRowRec = Record.emptyObject();
				curRowRec.set(ACCOUNT, row.getAccount());
				curRowRec.set(AMOUNT, row.getAmount());
				rowsRec.append(curRowRec);
			}
			entryRec.set(ROWS, rowsRec);
			base.append(entryRec);
		}

		return base;
	}

	@Override
	public Record taskToRecord(GenericTask task) {
		Record taskRecord = super.taskToRecord(task);
		if (task instanceof FinanceOverviewTask) {
			taskRecord.set(KIND, FINANCE_OVERVIEW);
		}
		return taskRecord;
	}

	public List<FinanceLogEntry> getFinanceLogs() {

		Collections.sort(financeLogs, new Comparator<FinanceLogEntry>() {
			public int compare(FinanceLogEntry a, FinanceLogEntry b) {
				return b.getDate().compareTo(a.getDate());
			}
		});

		return financeLogs;
	}

	public void removeFinanceLogForDate(Date removeForDate) {
		List<FinanceLogEntry> newLogs = new ArrayList<>();
		for (FinanceLogEntry log : financeLogs) {
			if (!DateUtils.isSameDay(log.getDate(), removeForDate)) {
				newLogs.add(log);
			}
		}
		this.financeLogs = newLogs;
	}

	public void addFinanceLogEntry(FinanceLogEntry newEntry) {
		this.financeLogs.add(newEntry);
	}

	public void deleteTaskInstance(Task task) {
		for (int i = taskInstances.size() - 1; i >= 0; i--) {
			if (taskInstances.get(i).equals(task)) {
				taskInstances.remove(i);
			}
		}
	}

	public void deleteTaskInstanceById(String id) {
		for (int i = taskInstances.size() - 1; i >= 0; i--) {
			GenericTask genTask = taskInstances.get(i);
			if (genTask instanceof Task) {
				Task task = (Task) genTask;
				if (task.getId().equals(id)) {
					taskInstances.remove(i);
				}
			}
		}
	}

	public void setTaskInstanceToDoneById(String id, String taskLogText, String finLogText) {
		for (int i = taskInstances.size() - 1; i >= 0; i--) {
			GenericTask genTask = taskInstances.get(i);
			if (genTask instanceof Task) {
				Task task = (Task) genTask;
				if (task.getId().equals(id)) {
					task.setToDone(taskLogText, finLogText);
				}
			}
		}
	}

	/**
	 * Returns true if it worked and an ad hoc task was created, and false otherwise
	 */
	public boolean addAdHocTask(String title, String details, String dateStr) {

		Date scheduleDate = DateUtils.parseDate(dateStr);

		GenericTask addedTask = super.addAdHocTask(title, details, scheduleDate);

		if (addedTask == null) {
			return false;
		}

		database.save();

		return true;
	}

	public DateHolder getLastTaskGeneration() {
		return lastTaskGeneration;
	}

	public void save() {
		database.save();
	}

	public String gulpBankStatements() {

		StringBuilder importStr = new StringBuilder();
		StringBuilder errorStrs = new StringBuilder();

		Directory downloadsDir = new Directory("C:\\Users\\Moyaccercchi\\Downloads");
		boolean recursive = false;


		// SPARDA

		// find all Sparda bank statements in the Downloads folder
		List<File> downloadFiles = downloadsDir.getAllFiles(recursive);
		List<File> anyBankFiles = new ArrayList<>();
		List<File> bankStatements = new ArrayList<>();
		for (File file : downloadFiles) {
			String localName = file.getLocalFilename();
			if (!localName.endsWith(".pdf")) {
				continue;
			}
			if (!(localName.startsWith("1480748_") || localName.startsWith("5001480748_"))) {
				continue;
			}
			anyBankFiles.add(file);
			if (localName.contains("_Kontoauszug_")) {
				bankStatements.add(file);
				importStr.append("\n" + localName + " (Sparda bank statement)");
			} else {
				importStr.append("\n" + localName + " (Sparda generic file)");
			}
		}

		// put them into the official folder
		Directory spardaDir = new Directory("C:\\home\\official\\Sparda");
		for (File file : anyBankFiles) {
			file.moveTo(spardaDir);
		}

		// apply un-secure script
		IoUtils.execute(spardaDir.getAbsoluteDirname() + "\\0 decrypt pdfs.bat");

		// copy them into the assAccountant
		Directory accImportDir = new Directory("C:\\home\\prog\\asofterspace\\assAccountant\\import");
		Directory decryptDir = new Directory("C:\\home\\official\\Sparda\\decrypted");

		List<File> importFiles = new ArrayList<>();

		for (File file : bankStatements) {

			File decryptedFile = new File(decryptDir, file.getLocalFilename());

			decryptedFile.copyToDisk(accImportDir);

			importFiles.add(new File(accImportDir, file.getLocalFilename()));
		}

		// import them into the assAccountant
		errorStrs.append(database.bulkImportBankStatements(importFiles));


		// DKB

		// find all DKB bank statements in the Downloads folder
		downloadFiles = downloadsDir.getAllFiles(recursive);
		anyBankFiles = new ArrayList<>();
		bankStatements = new ArrayList<>();
		for (File file : downloadFiles) {
			String localName = file.getLocalFilename();
			if (!localName.endsWith(".pdf")) {
				continue;
			}
			if (!(localName.startsWith("Kontoauszug_1011709415_") ||
				  localName.startsWith("Kreditkartenabrechnung_4748xxxxxxxx7849_") ||
				  localName.startsWith("Depotauszug_vom_"))) {
				continue;
			}
			anyBankFiles.add(file);
			// currently, we can only import Kontoauszug_, but we should in future also be able to import the credit card stuff
			if (localName.contains("Kontoauszug_")) {
			// if (localName.contains("Kontoauszug_") || localName.contains("Kreditkartenabrechnung_")) {
				bankStatements.add(file);
				importStr.append("\n" + localName + " (DKB bank statement)");
			} else {
				importStr.append("\n" + localName + " (DKB generic file)");
			}
		}

		// put them into the official folder
		Directory dkbDir = new Directory("C:\\home\\official\\DKB");
		for (File file : anyBankFiles) {
			file.moveTo(dkbDir);
		}

		// copy them into the assAccountant
		importFiles = new ArrayList<>();

		for (File file : bankStatements) {

			file.copyToDisk(accImportDir);

			importFiles.add(new File(accImportDir, file.getLocalFilename()));
		}

		// import them into the assAccountant
		errorStrs.append(database.bulkImportBankStatements(importFiles));


		// n26

		// find all N26 bank statements in the Downloads folder
		downloadFiles = downloadsDir.getAllFiles(recursive);
		anyBankFiles = new ArrayList<>();
		bankStatements = new ArrayList<>();
		for (File file : downloadFiles) {
			String localName = file.getLocalFilename();
			if (!localName.endsWith(".pdf")) {
				continue;
			}
			if (!localName.startsWith("statement-")) {
				continue;
			}
			anyBankFiles.add(file);
			bankStatements.add(file);
			importStr.append("\n" + localName + " (n26 bank statement)");
		}

		// put them into the official folder
		Directory n26Dir = new Directory("C:\\home\\official\\n26");
		for (File file : anyBankFiles) {
			file.moveTo(n26Dir);
		}

		// find all N26 bank statement CSV in the Downloads folder
		downloadFiles = downloadsDir.getAllFiles(recursive);
		anyBankFiles = new ArrayList<>();
		bankStatements = new ArrayList<>();
		for (File file : downloadFiles) {
			String localName = file.getLocalFilename();
			if (!localName.endsWith(".csv")) {
				continue;
			}
			if (!localName.startsWith("n26-csv-transactions-")) {
				continue;
			}
			anyBankFiles.add(file);
			bankStatements.add(file);
			importStr.append("\n" + localName + " (n26 bank statement)");
		}

		// put them into the official folder
		for (File file : anyBankFiles) {
			file.moveTo(n26Dir);
		}

		// copy them into the assAccountant
		importFiles = new ArrayList<>();

		for (File file : bankStatements) {

			file.copyToDisk(accImportDir);

			importFiles.add(new File(accImportDir, file.getLocalFilename()));
		}

		// import them into the assAccountant
		errorStrs.append(database.bulkImportBankStatements(importFiles));


		if (importStr.length() < 1) {
			importStr.append("\nNo files at all - sorry!");
		}

		String result = "Imported:\n" + importStr.toString();
		String errorStr = errorStrs.toString();
		if (!"".equals(errorStrs)) {
			result += "\n\nErrors:" + errorStr;
		}
		return result;
	}

}
