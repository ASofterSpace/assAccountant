/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tasks;

import com.asofterspace.accountant.Database;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class TaskCtrl {

	private final String TASKS = "tasks";
	private final String TASK_INSTANCES = "taskInstances";
	private final String LAST_TASK_GENERATION = "lastTaskGeneration";
	private final String FINANCE_LOGS = "financeLogs";
	private final String GENERIC = "generic";
	private final String FINANCE_OVERVIEW = "financeOverview";
	private final String KIND = "kind";
	private final String TITLE = "title";
	private final String DAY = "day";
	private final String MONTH = "month";
	private final String DETAILS = "details";
	private final String ON_DONE = "onDone";
	private final String DONE = "done";
	private final String RELEASED_ON_DAY = "releasedOnDay";
	private final String RELEASED_IN_MONTH = "releasedInMonth";
	private final String RELEASED_IN_YEAR = "releasedInYear";
	private final String DONE_DATE = "doneDate";
	private final String DONE_LOG = "doneLog";
	private final String DATE = "date";
	private final String ROWS = "rows";
	private final String AMOUNT = "amount";
	private final String ACCOUNT = "account";

	// contains one instance of each task, such that for a given day we can check which of
	// these potential tasks actually occurs on that day
	private List<Task> tasks;

	// contains the actual released and potentially worked on tasks
	private List<Task> taskInstances;

	private Date lastTaskGeneration;

	private List<FinanceLogEntry> financeLogs;

	private Database database;


	public TaskCtrl(Database database) {

		this.database = database;

		database.setTaskCtrl(this);

		loadFromDatabase();

		generateNewInstances(DateUtils.now());
	}

	private void loadFromDatabase() {

		Record root = database.getLoadedRoot();

		List<Record> taskRecordsInDatabase = root.getArray(TASKS);
		this.tasks = new ArrayList<>();
		for (Record curTask : taskRecordsInDatabase) {
			Task task = taskFromRecord(curTask);
			if (task != null) {
				tasks.add(task);
			}
		}

		List<Record> taskInstanceRecordsInDatabase = root.getArray(TASK_INSTANCES);
		this.taskInstances = new ArrayList<>();
		for (Record curTask : taskInstanceRecordsInDatabase) {
			Task task = taskInstanceFromRecord(curTask);
			if (task != null) {
				taskInstances.add(task);
			}
		}

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

		this.lastTaskGeneration = DateUtils.parseDate(root.getString(LAST_TASK_GENERATION));
	}

	public void generateNewInstances(Date until) {

		List<Date> daysToGenerate = DateUtils.listDaysFromTo(lastTaskGeneration, until);

		// we ignore the very first day that is returned,
		// as we already reported tasks for that one last time!
		for (int i = 1; i < daysToGenerate.size(); i++) {

			Date day = daysToGenerate.get(i);

			for (Task task : tasks) {
				if (task.isScheduledOn(day)) {
					releaseTaskOn(task, day);
				}
			}

			lastTaskGeneration = day;
		}
	}

	private void releaseTaskOn(Task task, Date day) {
		Task taskInstance = task.getNewInstance();
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		taskInstance.setDone(false);
		taskInstance.setReleasedOnDay(cal.get(Calendar.DAY_OF_MONTH));
		taskInstance.setReleasedInMonth(cal.get(Calendar.MONTH));
		taskInstance.setReleasedInYear(cal.get(Calendar.YEAR));
		taskInstance.setDoneDate(null);
		taskInstances.add(taskInstance);
	}

	private Task taskFromRecord(Record recordTask) {
		if (recordTask.getString(KIND).equals(FINANCE_OVERVIEW)) {
			return new FinanceOverviewTask(
				this,
				recordTask.getString(TITLE),
				recordTask.getInteger(DAY),
				DateUtils.monthNameToNum(recordTask.getString(MONTH)),
				recordTask.getArrayAsStringList(DETAILS),
				recordTask.getArrayAsStringList(ON_DONE)
			);
		}
		if (recordTask.getString(KIND).equals(GENERIC)) {
			return new Task(
				this,
				recordTask.getString(TITLE),
				recordTask.getInteger(DAY),
				DateUtils.monthNameToNum(recordTask.getString(MONTH)),
				recordTask.getArrayAsStringList(DETAILS),
				recordTask.getArrayAsStringList(ON_DONE)
			);
		}
		GuiUtils.complain("The task " + recordTask.getString(TITLE) + " could not be loaded!");
		return null;
	}

	private Task taskInstanceFromRecord(Record recordTask) {
		Task result = taskFromRecord(recordTask);
		if (result == null) {
			return null;
		}
		result.setDone(recordTask.getBoolean(DONE));
		result.setReleasedOnDay(recordTask.getInteger(RELEASED_ON_DAY));
		result.setReleasedInMonth(recordTask.getInteger(RELEASED_IN_MONTH));
		result.setReleasedInYear(recordTask.getInteger(RELEASED_IN_YEAR));
		result.setDoneDate(DateUtils.parseDate(recordTask.getString(DONE_DATE)));
		result.setDoneLog(recordTask.getString(DONE_LOG));
		return result;
	}

	public void saveIntoRecord(Record root) {
		root.set(TASKS, getTasksAsRecord());
		root.set(TASK_INSTANCES, getTaskInstancesAsRecord());
		root.set(FINANCE_LOGS, getFinanceLogsAsRecord());
		root.set(LAST_TASK_GENERATION, DateUtils.serializeDate(lastTaskGeneration));
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

	private Record taskToRecord(Task task) {
		Record taskRecord = Record.emptyObject();
		taskRecord.set(KIND, GENERIC);
		if (task instanceof FinanceOverviewTask) {
			taskRecord.set(KIND, FINANCE_OVERVIEW);
		}
		taskRecord.set(TITLE, task.getTitle());
		taskRecord.set(DAY, task.getScheduledOnDay());
		taskRecord.set(MONTH, DateUtils.monthNumToName(task.getScheduledInMonth()));
		taskRecord.set(DETAILS, task.getDetails());
		taskRecord.set(ON_DONE, task.getOnDone());
		return taskRecord;
	}

	private Record getTasksAsRecord() {
		Record base = Record.emptyArray();
		for (Task task : tasks) {
			Record taskRecord = taskToRecord(task);
			base.append(taskRecord);
		}
		return base;
	}

	private Record getTaskInstancesAsRecord() {
		Record base = Record.emptyArray();
		for (Task task : taskInstances) {
			Record taskRecord = taskToRecord(task);
			taskRecord.set(DONE, task.hasBeenDone());
			taskRecord.set(RELEASED_ON_DAY, task.getReleasedOnDay());
			taskRecord.set(RELEASED_IN_MONTH, task.getReleasedInMonth());
			taskRecord.set(RELEASED_IN_YEAR, task.getReleasedInYear());
			taskRecord.set(DONE_DATE, DateUtils.serializeDate(task.getDoneDate()));
			taskRecord.set(DONE_LOG, task.getDoneLog());
			base.append(taskRecord);
		}
		return base;
	}

	public List<Task> getTaskInstances() {

		Collections.sort(taskInstances, new Comparator<Task>() {
			public int compare(Task a, Task b) {
				if (a.getReleasedInYear().equals(b.getReleasedInYear())) {
					if (a.getReleasedInMonth().equals(b.getReleasedInMonth())) {
						if (a.getReleasedOnDay().equals(b.getReleasedOnDay())) {
							return a.getTitle().compareTo(b.getTitle());
						}
						return b.getReleasedOnDay() - a.getReleasedOnDay();
					}
					return b.getReleasedInMonth() - a.getReleasedInMonth();
				}
				return b.getReleasedInYear() - a.getReleasedInYear();
			}
		});

		return taskInstances;
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

	public boolean addAdHocTask(String title, String details, String dateStr) {

		Date scheduleDate = DateUtils.parseDate(dateStr);

		if (scheduleDate == null) {
			return false;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(scheduleDate);

		List<String> detailsList = new ArrayList<>();
		for (String detail : details.split("\n")) {
			detailsList.add(detail);
		}

		List<String> onDone = new ArrayList<>();

		// this is an ad-hoc task which is not scheduled ever
		Integer scheduledOnDay = null;
		Integer scheduledInMonth = null;

		Task newTask = new Task(this, title, scheduledOnDay, scheduledInMonth, detailsList, onDone);

		releaseTaskOn(newTask, scheduleDate);

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
