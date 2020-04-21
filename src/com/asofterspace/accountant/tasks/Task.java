/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.StrUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
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

	public Date getReleaseDate() {
		return DateUtils.parseDate(getReleasedInYear() + "-" + (getReleasedInMonth() + 1) + "-" + getReleasedOnDay());
	}

	/**
	 * Actually return the instructions as shown to the user, with information replaced with
	 * actual info
	 */
	public List<JPanel> getDetailsToShowToUser(Database database) {

		Month curMonth = database.getMonthFromEntryDate(getReleaseDate());

		List<JPanel> results = new ArrayList<>();
		if (details != null) {
			// actually join all the individual lines to a big text first, and make all the replacements
			// just once for which this works (and afterwards split again for the more line-specific
			// replacements such as %[CHECK])
			String detail = StrUtils.join("\n", details);
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
			if (detail.contains("%[VAT_TOTAL_OUTGOING_MONTH_TAX_19%]")) {
				int cur = 0;
				for (Outgoing entry : curMonth.getOutgoings()) {
					if (19 == (int) entry.getTaxPercent()) {
						cur += entry.getAmount();
					}
				}
				detail = detail.replaceAll("%\\[VAT_TOTAL_OUTGOING_MONTH_TAX_19%\\]",
					AccountingUtils.formatMoney(cur, Currency.EUR));
			}
			if (detail.contains("%[VAT_TOTAL_OUTGOING_MONTH_TAX_0%]")) {
				int cur = 0;
				for (Outgoing entry : curMonth.getOutgoings()) {
					if (0 == (int) entry.getTaxPercent()) {
						cur += entry.getAmount();
					}
				}
				detail = detail.replaceAll("%\\[VAT_TOTAL_OUTGOING_MONTH_TAX_0%\\]",
					AccountingUtils.formatMoney(cur, Currency.EUR));
			}
			String vat0complex = "%[VAT_TOTAL_OUTGOING_MONTH_TAX_0%_";
			String vat0complexBracket = vat0complex + "(";
			if (detail.contains(vat0complexBracket)) {
				List<String> included = new ArrayList<>();
				String findInc = detail.substring(detail.indexOf(vat0complexBracket) + vat0complexBracket.length());
				if (findInc.contains(")")) {
					while ((findInc.indexOf(")") > findInc.indexOf(",")) && (findInc.indexOf(",") >= 0)) {
						included.add(findInc.substring(0, findInc.indexOf(",")).toLowerCase().trim());
						findInc = findInc.substring(findInc.indexOf(",") + 1);
					}
					included.add(findInc.substring(0, findInc.indexOf(")")).toLowerCase().trim());
					int curInc = 0;
					int curRest = 0;
					for (Outgoing entry : curMonth.getOutgoings()) {
						if (0 == (int) entry.getTaxPercent()) {
							String curCustomer = entry.getCategoryOrCustomer().toLowerCase().trim();
							boolean includeThisOne = false;
							for (String incCustomer : included) {
								if (curCustomer.equals(incCustomer)) {
									includeThisOne = true;
								}
							}
							if (includeThisOne) {
								curInc += entry.getAmount();
							} else {
								curRest += entry.getAmount();
							}
						}
					}
					detail = detail.replaceAll("%\\[VAT_TOTAL_OUTGOING_MONTH_TAX_0%_\\(.*\\)\\]",
						AccountingUtils.formatMoney(curInc, Currency.EUR));
					detail = detail.replaceAll("%\\[VAT_TOTAL_OUTGOING_MONTH_TAX_0%_REST\\]",
						AccountingUtils.formatMoney(curRest, Currency.EUR));
				}
			}
			if (detail.contains("%[VAT_TOTAL_DISCOUNTABLE_PRETAX]")) {
				detail = detail.replaceAll("%\\[VAT_TOTAL_DISCOUNTABLE_PRETAX\\]",
					AccountingUtils.formatMoney(curMonth.getDiscountablePreTax(), Currency.EUR));
			}
			if (detail.contains("%[VAT_TOTAL_REMAINING_TAX]")) {
				detail = detail.replaceAll("%\\[VAT_TOTAL_REMAINING_TAX\\]",
					AccountingUtils.formatMoney(curMonth.getRemainingVatPayments(), Currency.EUR));
			}

			String[] detailsAfterReplacement = detail.split("\n");

			for (String detailLine : detailsAfterReplacement) {

				JPanel curPanel = new JPanel();
				curPanel.setBackground(GUI.getBackgroundColor());
				curPanel.setLayout(new GridBagLayout());

				if (detailLine.contains("%[LIST_OUTGOING_UNPAID]")) {
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
					if (detailLine.trim().startsWith("%[CHECK]")) {
						JCheckBox checkBox = new JCheckBox();
						checkBox.setBackground(GUI.getBackgroundColor());
						curPanel.add(checkBox, new Arrangement(0, 0, 0.0, 1.0));
						CopyByClickLabel curLabel = AccountingUtils.createLabel(detailLine.replaceAll("%\\[CHECK\\]", ""),
							new Color(0, 0, 0), "");
						curPanel.add(curLabel, new Arrangement(1, 0, 1.0, 1.0));
					} else {
						CopyByClickLabel curLabel = AccountingUtils.createLabel(detailLine, new Color(0, 0, 0), "");
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

		MouseAdapter rowHighlighter = AccountingUtils.getRowHighlighter(curPanel);

		int h = 0;

		CopyByClickLabel curLabel = AccountingUtils.createLabel(getReleasedDateStr(), textColor, "");
		curLabel.addMouseListener(rowHighlighter);
		curPanel.add(curLabel, new Arrangement(h, 0, 0.08, 1.0));
		h++;

		double titleWidth = 0.5;
		if (done && (doneDate != null)) {
			curLabel = AccountingUtils.createLabel(DateUtils.serializeDate(getDoneDate()), textColor, "");
			curLabel.addMouseListener(rowHighlighter);
			curPanel.add(curLabel, new Arrangement(h, 0, 0.08, 1.0));
			titleWidth = 0.42;
			h++;
		}

		curLabel = AccountingUtils.createLabel(title, textColor, "");
		curLabel.addMouseListener(rowHighlighter);
		curPanel.add(curLabel, new Arrangement(h, 0, titleWidth, 1.0));
		h++;

		curLabel = AccountingUtils.createLabel("", textColor, "");
		curLabel.addMouseListener(rowHighlighter);
		curPanel.add(curLabel, new Arrangement(h, 0, 0.0, 1.0));
		h++;

		final List<JComponent> addedLines = new ArrayList<>();
		final JButton detailsButton = new JButton("Show Details");
		detailsButton.addMouseListener(rowHighlighter);
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
		if (doneLog != null) {
			taskLog.setText(doneLog);
		}
		int newHeight = taskLog.getPreferredSize().height + 48;
		if (newHeight < 128) {
			newHeight = 128;
		}
		taskLog.setPreferredSize(new Dimension(128, newHeight));

		final JTextPane finLog = new JTextPane();
		if (Task.this instanceof FinanceOverviewTask) {
			// if this was done before, load the finance log contents as filled in back then
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
			// if this has not been done before, load the latest finance log keys, but do not assing values
			if (!done) {
				List<FinanceLogEntry> entries = taskCtrl.getFinanceLogs();
				if (entries.size() > 0) {
					StringBuilder finLogText = new StringBuilder();
					FinanceLogEntry entry = entries.get(0);
					for (FinanceLogEntryRow row : entry.getRows()) {
						finLogText.append(row.getAccount());
						finLogText.append(": ");
						finLogText.append("\n");
					}
					finLog.setText(finLogText.toString());
				}
			}
		}
		newHeight = finLog.getPreferredSize().height + 48;
		if (newHeight < 128) {
			newHeight = 128;
		}
		finLog.setPreferredSize(new Dimension(128, newHeight));

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
		curButton.addMouseListener(rowHighlighter);
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
					boolean wroteARow = false;
					for (String line : finLogLines) {
						line = line.trim();
						if ("".equals(line)) {
							continue;
						}
						String[] lineSplit = line.split(":");
						if (lineSplit.length > 1) {
							Integer amount = StrUtils.parseMoney(lineSplit[1]);
							entry.add(new FinanceLogEntryRow(lineSplit[0], amount));
							wroteARow = true;
							if (lineSplit.length > 2) {
								AccountingUtils.complain("The line '" + line + "' contained more than one : sign!\n" +
									"It was parsed as " + lineSplit[0] + ": " + AccountingUtils.formatMoney(amount,
									Currency.EUR));
							}
						}
						if (lineSplit.length < 2) {
							AccountingUtils.complain("The line '" + line + "' contained no : sign!\n" +
								"It was ignored.");
						}
					}
					if (!wroteARow) {
						AccountingUtils.complain("It looks like the Finance Log section was not filled!\n" +
							"You can edit the Finance Log section in the task's details on the Task Log tab.");
					}
					taskCtrl.addFinanceLogEntry(entry);
				}
				taskCtrl.save();
			}
		});

		curButton = new JButton("Delete");
		curButton.addMouseListener(rowHighlighter);
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(h, 0, 0.06, 1.0));
		h++;
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (AccountingUtils.confirmDelete("task '" + getTitle() + "'")) {
					taskCtrl.deleteTaskInstance(Task.this);
					taskCtrl.save();
				}
			}
		});

		curLabel = AccountingUtils.createLabel("", textColor, "");
		curLabel.addMouseListener(rowHighlighter);
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
