/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tasks;

import com.asofterspace.accountant.Database;
import com.asofterspace.toolbox.calendar.GenericTask;
import com.asofterspace.toolbox.calendar.TaskCtrlBase;
import com.asofterspace.toolbox.gui.GuiUtils;
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
	protected GenericTask createTask(String title, Integer scheduledOnDay, List<String> scheduledOnDaysOfWeek,
		List<Integer> scheduledInMonths, List<Integer> scheduledInYears, List<String> details, List<String> onDone) {

		return new Task(title, scheduledOnDay, scheduledOnDaysOfWeek, scheduledInMonths,
			scheduledInYears, details, onDone);
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
	protected Record taskToRecord(GenericTask task) {
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

	public Date getLastTaskGeneration() {
		return lastTaskGeneration;
	}

	public void save() {
		database.save();
	}

}
