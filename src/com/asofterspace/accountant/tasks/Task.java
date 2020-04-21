/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.StrUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;


/**
 * A task describes some action that will have to be taken at a certain time,
 * and whose execution can be logged
 */
public class Task {

	protected String title;

	// on which day of the month is this task scheduled?
	protected Integer scheduledOnDay;

	// in which month is this task scheduled?
	protected Integer scheduledInMonth;

	protected List<String> details;

	// has this task already been done?
	protected Boolean done;

	// for which date was this task instance released for the user to look at?
	// (this might be before or after the date the user actually first saw it,
	// e.g. the user have have seen it as a future task, or it may have been
	// generated days later... this is really the date in the calendar that
	// triggered the schedule for this task to generate this instance!)
	protected Integer releasedOnDay;
	protected Integer releasedInMonth;
	protected Integer releasedInYear;

	// when was this task done?
	protected Date doneDate;

	// what interesting things did the user encounter while doing this task?
	protected String doneLog;

	protected TaskCtrl taskCtrl;


	public Task(TaskCtrl taskCtrl, String title, Integer scheduledOnDay, Integer scheduledInMonth,
		List<String> details) {

		this.taskCtrl = taskCtrl;
		this.title = title;
		this.scheduledOnDay = scheduledOnDay;
		this.scheduledInMonth = scheduledInMonth;
		this.details = details;
	}

	public Task getNewInstance() {
		return new Task(taskCtrl, title, scheduledOnDay, scheduledInMonth, details);
	}

	public boolean isScheduledOn(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		if (scheduledOnDay != null) {
			if (!scheduledOnDay.equals(cal.get(Calendar.DAY_OF_MONTH))) {
				return false;
			}
		}

		if (scheduledInMonth != null) {
			if (!scheduledInMonth.equals(cal.get(Calendar.MONTH))) {
				return false;
			}
		}

		return true;
	}

	public String getTitle() {
		return title;
	}

	public Integer getScheduledOnDay() {
		return scheduledOnDay;
	}

	public Integer getScheduledInMonth() {
		return scheduledInMonth;
	}

	/**
	 * Return detailed instructions for the user such that they know what to do with this task
	 */
	public List<String> getDetails() {
		return details;
	}

	/**
	 * Actually return the instructions as shown to the user, with information replaced with
	 * actual info
	 */
	public List<JPanel> getDetailsToShowToUser(Database database) {
		List<JPanel> results = new ArrayList<>();
		if (details != null) {
			for (String detail : details) {
				detail = detail.replaceAll("%\\[DAY\\]", ""+releasedOnDay);
				detail = detail.replaceAll("%\\[MONTH\\]", ""+releasedInMonth);
				detail = detail.replaceAll("%\\[NAME_OF_MONTH\\]", DateUtils.monthNumToName(releasedInMonth));
				detail = detail.replaceAll("%\\[YEAR\\]", ""+releasedInYear);
				detail = detail.replaceAll("%\\[PREV_DAY\\]", ""+(releasedOnDay-1));
				int prevMonth = releasedInMonth - 1;
				if (prevMonth < 0) {
					prevMonth = 11;
				}
				detail = detail.replaceAll("%\\[PREV_MONTH\\]", ""+prevMonth);
				detail = detail.replaceAll("%\\[NAME_OF_PREV_MONTH\\]", DateUtils.monthNumToName(prevMonth));
				detail = detail.replaceAll("%\\[PREV_YEAR\\]", ""+(releasedInYear-1));

				JPanel curPanel = new JPanel();
				curPanel.setBackground(GUI.getBackgroundColor());
				curPanel.setLayout(new GridBagLayout());

				if (detail.contains("%[LIST_OUTGOING_UNPAID]")) {
					List<Outgoing> outgoings = database.getOutgoings();
					int i = 0;
					for (Outgoing outgoing : outgoings) {
						if (!outgoing.getReceived()) {
							JPanel curCurPanel = outgoing.createPanelOnGUI(database);
							curPanel.add(curCurPanel, new Arrangement(0, i, 1.0, 0.0));
							i++;
						}
					}
				} else {
					if (detail.trim().startsWith("%[CHECK]")) {
						JCheckBox checkBox = new JCheckBox();
						checkBox.setBackground(GUI.getBackgroundColor());
						curPanel.add(checkBox, new Arrangement(0, 0, 0.0, 1.0));
						CopyByClickLabel curLabel = AccountingUtils.createLabel(detail.replaceAll("%\\[CHECK\\]", ""),
							new Color(0, 0, 0), "");
						curPanel.add(curLabel, new Arrangement(1, 0, 1.0, 1.0));
					} else {
						CopyByClickLabel curLabel = AccountingUtils.createLabel(detail, new Color(0, 0, 0), "");
						curPanel.add(curLabel, new Arrangement(0, 0, 1.0, 1.0));
					}
				}

				results.add(curPanel);
			}
		}
		return results;
	}

	public Boolean hasBeenDone() {
		return done;
	}

	public void setDone(Boolean done) {
		this.done = done;
	}

	public Integer getReleasedOnDay() {
		return releasedOnDay;
	}

	public void setReleasedOnDay(Integer releasedOnDay) {
		this.releasedOnDay = releasedOnDay;
	}

	public Integer getReleasedInMonth() {
		return releasedInMonth;
	}

	public void setReleasedInMonth(Integer releasedInMonth) {
		this.releasedInMonth = releasedInMonth;
	}

	public Integer getReleasedInYear() {
		return releasedInYear;
	}

	public void setReleasedInYear(Integer releasedInYear) {
		this.releasedInYear = releasedInYear;
	}

	public String getReleasedDateStr() {
		String day = ""+getReleasedOnDay();
		if (day.length() < 2) {
			day = "0" + day;
		}
		String month = ""+(getReleasedInMonth()+1);
		if (month.length() < 2) {
			month = "0" + month;
		}
		return getReleasedInYear() + "-" + month + "-" + day;
	}

	public Date getDoneDate() {
		return doneDate;
	}

	public void setDoneDate(Date doneDate) {
		this.doneDate = doneDate;
	}

	public String getDoneLog() {
		return doneLog;
	}

	public void setDoneLog(String doneLog) {
		this.doneLog = doneLog;
	}

	public JPanel createPanelOnGUI(Database database, JPanel tab, JPanel parentPanel) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();
		Color textColor = new Color(0, 0, 0);

		final JPanel containerPanel = new JPanel();
		containerPanel.setBackground(GUI.getBackgroundColor());
		containerPanel.setLayout(new GridBagLayout());

		JPanel curPanel = new JPanel();
		curPanel.setBackground(GUI.getBackgroundColor());
		curPanel.setLayout(new GridBagLayout());

		int h = 0;

		CopyByClickLabel curLabel = AccountingUtils.createLabel(getReleasedDateStr(), textColor, "");
		curPanel.add(curLabel, new Arrangement(h, 0, 0.08, 1.0));
		h++;

		double titleWidth = 0.5;
		if (done && (doneDate != null)) {
			curLabel = AccountingUtils.createLabel(DateUtils.serializeDate(getDoneDate()), textColor, "");
			curPanel.add(curLabel, new Arrangement(h, 0, 0.08, 1.0));
			titleWidth = 0.42;
			h++;
		}

		curLabel = AccountingUtils.createLabel(title, textColor, "");
		curPanel.add(curLabel, new Arrangement(h, 0, titleWidth, 1.0));
		h++;

		curLabel = AccountingUtils.createLabel("", textColor, "");
		curPanel.add(curLabel, new Arrangement(h, 0, 0.0, 1.0));
		h++;

		final List<JComponent> addedLines = new ArrayList<>();
		final JButton detailsButton = new JButton("Show Details");
		if ((details == null) || (details.size() < 1)) {
			detailsButton.setText("Add Details");
			// actually keep the details button enabled in case the user want to add a log
			// detailsButton.setEnabled(false);
		}
		if (done) {
			detailsButton.setText("Show Details");
		}
		detailsButton.setPreferredSize(defaultDimension);
		curPanel.add(detailsButton, new Arrangement(h, 0, 0.1, 1.0));
		h++;

		final JTextPane taskLog = new JTextPane();
		taskLog.setPreferredSize(new Dimension(128, 128));
		if (doneLog != null) {
			taskLog.setText(doneLog);
		}

		final JTextPane finLog = new JTextPane();
		finLog.setPreferredSize(new Dimension(128, 128));
		if (done && (doneDate != null)) {
			StringBuilder finLogText = new StringBuilder();
			List<FinanceLogEntry> entries = taskCtrl.getFinanceLogs();
			for (FinanceLogEntry entry : entries) {
				if (DateUtils.isSameDay(entry.getDate(), doneDate)) {
					for (FinanceLogEntryRow row : entry.getRows()) {
						finLogText.append(row.getAccount());
						finLogText.append(": ");
						finLogText.append(AccountingUtils.formatMoney(row.getAmount(), Currency.EUR));
						finLogText.append("\n");
					}
				}
			}
			finLog.setText(finLogText.toString());
		}

		detailsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!detailsButton.getText().startsWith("Hide")) {
					detailsButton.setText("Hide Details");

					int i = 1;

					if ((details == null) || (details.size() < 1)) {
						JPanel curPanel = new JPanel();
						curPanel.setBackground(GUI.getBackgroundColor());
						curPanel.setLayout(new GridBagLayout());

						CopyByClickLabel curLabel = AccountingUtils.createLabel("There are no details for this task!", textColor, "");
						curPanel.add(curLabel, new Arrangement(0, 0, 1.0, 1.0));

						containerPanel.add(curPanel, new Arrangement(0, i, 1.0, 1.0));
						addedLines.add(curPanel);
						i++;
					} else {
						for (JPanel detail : getDetailsToShowToUser(database)) {
							containerPanel.add(detail, new Arrangement(0, i, 1.0, 1.0));
							addedLines.add(detail);
							i++;
						}
					}

					if (Task.this instanceof FinanceOverviewTask) {
						CopyByClickLabel curLabel = AccountingUtils.createLabel("", textColor, "");
						containerPanel.add(curLabel, new Arrangement(0, i, 1.0, 1.0));
						addedLines.add(curLabel);
						i++;

						curLabel = AccountingUtils.createLabel("Finance Log - on each line put an Account: Amount, " +
							"so e.g. Iron Bank: 3.14 €:", textColor, "");
						containerPanel.add(curLabel, new Arrangement(0, i, 1.0, 1.0));
						addedLines.add(curLabel);
						i++;

						containerPanel.add(finLog, new Arrangement(0, i, 1.0, 1.0));
						addedLines.add(finLog);
						i++;
					}

					CopyByClickLabel curLabel = AccountingUtils.createLabel("", textColor, "");
					containerPanel.add(curLabel, new Arrangement(0, i, 1.0, 1.0));
					addedLines.add(curLabel);
					i++;

					curLabel = AccountingUtils.createLabel("Task Log - for logging anything interesting that happened " +
						"while doing this task:", textColor, "");
					containerPanel.add(curLabel, new Arrangement(0, i, 1.0, 1.0));
					addedLines.add(curLabel);
					i++;

					containerPanel.add(taskLog, new Arrangement(0, i, 1.0, 1.0));
					addedLines.add(taskLog);
					i++;

					containerPanel.setBorder(BorderFactory.createEtchedBorder());

				} else {
					if ((details == null) || (details.size() < 1)) {
						detailsButton.setText("Add Details");
					} else {
						detailsButton.setText("Show Details");
					}
					if (Task.this.done) {
						detailsButton.setText("Show Details");
					}

					// hide the detail lines again
					for (JComponent line : addedLines) {
						containerPanel.remove(line);
					}

					// and clear them (such that we do not attempt to remove them again)
					addedLines.clear();

					containerPanel.setBorder(BorderFactory.createEmptyBorder());
				}

				AccountingUtils.resetTabSize(tab, parentPanel);
			}
		});

		JButton curButton = new JButton("Done");
		if (done) {
			curButton.setText("Save");
		}
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(h, 0, 0.05, 1.0));
		h++;
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Task.this.done) {
					if (Task.this instanceof FinanceOverviewTask) {
						taskCtrl.removeFinanceLogForDate(doneDate);
					}
				} else {
					Task.this.done = true;
					setDoneDate(new Date());
				}
				setDoneLog(taskLog.getText());
				if (Task.this instanceof FinanceOverviewTask) {
					FinanceLogEntry entry = new FinanceLogEntry(doneDate);
					String[] finLogLines = finLog.getText().split("\n");
					for (String line : finLogLines) {
						String[] lineSplit = line.split(":");
						if (lineSplit.length > 1) {
							Integer amount = StrUtils.parseMoney(lineSplit[1]);
							entry.add(new FinanceLogEntryRow(lineSplit[0], amount));
						}
					}
					taskCtrl.addFinanceLogEntry(entry);
				}
				taskCtrl.save();
			}
		});

		curButton = new JButton("Delete");
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(h, 0, 0.06, 1.0));
		h++;
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				taskCtrl.deleteTaskInstance(Task.this);
				taskCtrl.save();
			}
		});

		curLabel = AccountingUtils.createLabel("", textColor, "");
		curPanel.add(curLabel, new Arrangement(h, 0, 0.0, 1.0));
		h++;

		containerPanel.add(curPanel, new Arrangement(0, 0, 1.0, 1.0));

		return containerPanel;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof Task) {
			Task otherTask = (Task) other;
			if (this.title.equals(otherTask.title) &&
				this.done.equals(otherTask.done) &&
				this.releasedOnDay.equals(otherTask.releasedOnDay) &&
				this.releasedInMonth.equals(otherTask.releasedInMonth) &&
				this.releasedInYear.equals(otherTask.releasedInYear)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		if ((done == null) || done.equals(false)) {
			return -1;
		}
		return this.releasedOnDay + 64 * this.releasedInMonth + 1024 * this.releasedInYear;
	}

}
