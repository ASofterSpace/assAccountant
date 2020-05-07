/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.TimeSpan;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.accounting.FinanceUtils;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
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

	// what should be done once this task is completed?
	protected List<String> onDone;

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
		List<String> details, List<String> onDone) {

		this.taskCtrl = taskCtrl;
		this.title = title;
		this.scheduledOnDay = scheduledOnDay;
		this.scheduledInMonth = scheduledInMonth;
		this.details = details;
		this.onDone = onDone;
	}

	public Task getNewInstance() {
		return new Task(taskCtrl, title, scheduledOnDay, scheduledInMonth, details, onDone);
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

	public List<String> getOnDone() {
		return onDone;
	}

	public Date getReleaseDate() {
		return DateUtils.parseDate(getReleasedInYear() + "-" + (getReleasedInMonth() + 1) + "-" + getReleasedOnDay());
	}

	public String getDetailsToShowToUser(Database database) {

		if ((details == null) || (details.size() < 1)) {
			return null;
		}

		Month curMonth = database.getMonthFromEntryDate(getReleaseDate());
		Month prevMonth = database.getPrevMonthFromEntryDate(getReleaseDate());
		Year curYear = database.getYearFromEntryDate(getReleaseDate());
		Year prevYear = database.getPrevYearFromEntryDate(getReleaseDate());

		// actually join all the individual lines to a big text first, and make all the replacements
		// just once for which this works (and afterwards split again for the more line-specific
		// replacements such as %[CHECK])
		String detail = StrUtils.join("\n", details);
		detail = detail.replaceAll("%\\[DATE_NOW\\]", DateUtils.serializeDate(new Date()));
		detail = detail.replaceAll("%\\[DATE\\]", releasedInYear+"-"+StrUtils.leftPad0(releasedInMonth+1, 2)+
			"-"+StrUtils.leftPad0(releasedOnDay, 2));
		detail = detail.replaceAll("%\\[DAY\\]", ""+releasedOnDay);
		detail = detail.replaceAll("%\\[DAY_2_DIG\\]", StrUtils.leftPad0(releasedOnDay, 2));
		detail = detail.replaceAll("%\\[MONTH\\]", ""+(releasedInMonth+1));
		detail = detail.replaceAll("%\\[MONTH_2_DIG\\]", StrUtils.leftPad0(releasedInMonth+1, 2));
		detail = detail.replaceAll("%\\[NAME_OF_MONTH\\]", DateUtils.monthNumToName(releasedInMonth));
		detail = detail.replaceAll("%\\[YEAR\\]", ""+releasedInYear);
		detail = detail.replaceAll("%\\[PREV_DAY\\]", ""+(releasedOnDay-1));
		detail = detail.replaceAll("%\\[PREV_DAY_2_DIG\\]", StrUtils.leftPad0(releasedOnDay-1, 2));
		int prevMonthNum = releasedInMonth - 1;
		if (prevMonthNum < 0) {
			prevMonthNum = 11;
			detail = detail.replaceAll("%\\[PREV_MONTHS_YEAR\\]", ""+(releasedInYear-1));
		} else {
			detail = detail.replaceAll("%\\[PREV_MONTHS_YEAR\\]", ""+releasedInYear);
		}
		detail = detail.replaceAll("%\\[PREV_MONTH\\]", ""+(prevMonthNum+1));
		detail = detail.replaceAll("%\\[PREV_MONTH_2_DIG\\]", StrUtils.leftPad0(prevMonthNum+1, 2));
		detail = detail.replaceAll("%\\[NAME_OF_PREV_MONTH\\]", DateUtils.monthNumToName(prevMonthNum));
		detail = detail.replaceAll("%\\[PREV_YEAR\\]", ""+(releasedInYear-1));

		detail = replaceDetailsFor(detail, "PREV_MONTH", prevMonth);
		detail = replaceDetailsFor(detail, "MONTH", curMonth);
		detail = replaceDetailsFor(detail, "PREV_YEAR", prevYear);
		detail = replaceDetailsFor(detail, "YEAR", curYear);

		return detail;
	}

	/**
	 * Actually return the instructions as shown to the user, with information replaced with
	 * actual info
	 */
	public List<JPanel> getDetailPanelsToShowToUser(Database database) {

		List<JPanel> results = new ArrayList<>();

		String detail = getDetailsToShowToUser(database);

		if (detail == null) {
			return results;
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
				String keyBefore = "%[ADD_ENTRY_BTN(";
				String keyAfter = ")]";
				int indexBefore = detailLine.indexOf(keyBefore) + keyBefore.length();
				int indexAfter = detailLine.lastIndexOf(keyAfter);
				if ((indexBefore >= 0) && (indexAfter >= 0) && (indexBefore <= indexAfter)) {
					detailLine = detailLine.substring(0, indexAfter);
					detailLine = detailLine.substring(indexBefore);
					try {
						final JSON entryData = new JSON(detailLine);
						JButton curAddEntryBtn = new JButton(entryData.getString("buttonText"));
						curPanel.add(curAddEntryBtn, new Arrangement(0, 0, 1.0, 0.0));
						curAddEntryBtn.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Entry fakeEntry = null;
								if ("outgoing".equals(entryData.getString("kind").toLowerCase())) {
									fakeEntry = new Outgoing(entryData, null);
								} else {
									fakeEntry = new Incoming(entryData, null);
								}
								AddEntryGUI addEntryGUI = new AddEntryGUI(database.getGUI(), database, fakeEntry);
								addEntryGUI.show();
							}
						});
					} catch (JsonParseException e) {
						CopyByClickLabel curLabel = AccountingUtils.createLabel(
							"Here should be a button, but I could not parse the json: " + detailLine, new Color(0, 0, 0), "");
						curPanel.add(curLabel, new Arrangement(0, 0, 1.0, 0.0));
					}
				} else {
					if (detailLine.trim().startsWith("%[CHECK]")) {
						JCheckBox checkBox = new JCheckBox();
						checkBox.setBackground(GUI.getBackgroundColor());
						curPanel.add(checkBox, new Arrangement(0, 0, 0.0, 0.0));
						CopyByClickLabel curLabel = AccountingUtils.createLabel(detailLine.replaceAll("%\\[CHECK\\]", ""),
							new Color(0, 0, 0), "");
						curPanel.add(curLabel, new Arrangement(1, 0, 1.0, 0.0));
					} else {
						CopyByClickLabel curLabel = AccountingUtils.createLabel(detailLine, new Color(0, 0, 0), "");
						curPanel.add(curLabel, new Arrangement(0, 0, 1.0, 0.0));
					}
				}
			}

			results.add(curPanel);
		}
		return results;
	}

	private String replaceDetailsFor(String detail, String timeSpanStr, TimeSpan timeSpan) {

		// USt Vorauszahlungssoll:
		if (detail.contains("%[TOTAL_PAID_VAT_PREPAYMENTS_" + timeSpanStr + "]")) {
			detail = detail.replaceAll("%\\[TOTAL_PAID_VAT_PREPAYMENTS_" + timeSpanStr + "\\]",
				FinanceUtils.formatMoney(timeSpan.getVatPrepaymentsPaidTotal(), Currency.EUR));
		}

		// Total deductible already paid VAT / USt Gesamte abziehbare Vorsteuerbeträge:
		if (detail.contains("%[VAT_TOTAL_DISCOUNTABLE_PRETAX_" + timeSpanStr + "]")) {
			detail = detail.replaceAll("%\\[VAT_TOTAL_DISCOUNTABLE_PRETAX_" + timeSpanStr + "\\]",
				FinanceUtils.formatMoney(timeSpan.getDiscountablePreTax(), Currency.EUR));
		}

		// Remaining VAT advance payment / Verbleibende Umsatzsteuer-Vorauszahlung:
		if (detail.contains("%[VAT_TOTAL_REMAINING_TAX_" + timeSpanStr + "]")) {
			detail = detail.replaceAll("%\\[VAT_TOTAL_REMAINING_TAX_" + timeSpanStr + "\\]",
				FinanceUtils.formatMoney(timeSpan.getRemainingVatPayments(), Currency.EUR));
		}

		if (detail.contains("%[VAT_TOTAL_OUTGOING_" + timeSpanStr + "_TAX_19%]")) {
			int cur = 0;
			for (Outgoing entry : timeSpan.getOutgoings()) {
				if (19 == (int) entry.getTaxPercent()) {
					cur += entry.getPreTaxAmount();
				}
			}
			detail = detail.replaceAll("%\\[VAT_TOTAL_OUTGOING_" + timeSpanStr + "_TAX_19%\\]",
				FinanceUtils.formatMoney(cur, Currency.EUR));
		}
		if (detail.contains("%[VAT_TOTAL_OUTGOING_" + timeSpanStr + "_TAX_7%]")) {
			int cur = 0;
			for (Outgoing entry : timeSpan.getOutgoings()) {
				if (7 == (int) entry.getTaxPercent()) {
					cur += entry.getPreTaxAmount();
				}
			}
			detail = detail.replaceAll("%\\[VAT_TOTAL_OUTGOING_" + timeSpanStr + "_TAX_7%\\]",
				FinanceUtils.formatMoney(cur, Currency.EUR));
		}
		if (detail.contains("%[VAT_TOTAL_OUTGOING_" + timeSpanStr + "_TAX_0%]")) {
			int cur = 0;
			for (Outgoing entry : timeSpan.getOutgoings()) {
				if (0 == (int) entry.getTaxPercent()) {
					cur += entry.getPreTaxAmount();
				}
			}
			detail = detail.replaceAll("%\\[VAT_TOTAL_OUTGOING_" + timeSpanStr + "_TAX_0%\\]",
				FinanceUtils.formatMoney(cur, Currency.EUR));
		}

		detail = replaceComplexVatInDetails(detail, "VAT_TOTAL_OUTGOING_" + timeSpanStr + "_TAX_0%_", timeSpan);

		if (detail.contains("%[VAT_TOTAL_OUTGOING_" + timeSpanStr + "_JUST_TAX]")) {
			int cur = 0;
			for (Outgoing entry : timeSpan.getOutgoings()) {
				cur += entry.getTaxAmount();
			}
			detail = detail.replaceAll("%\\[VAT_TOTAL_OUTGOING_" + timeSpanStr + "_JUST_TAX\\]",
				FinanceUtils.formatMoney(cur, Currency.EUR));
		}

		return detail;
	}

	public String replaceComplexVatInDetails(String detail, String complexKey, TimeSpan timeSpan) {
		String vat0complex = "%[" + complexKey;
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
				for (Outgoing entry : timeSpan.getOutgoings()) {
					if (0 == (int) entry.getTaxPercent()) {
						String curCustomer = entry.getCategoryOrCustomer().toLowerCase().trim();
						boolean includeThisOne = false;
						for (String incCustomer : included) {
							if (curCustomer.equals(incCustomer)) {
								includeThisOne = true;
							}
						}
						if (includeThisOne) {
							curInc += entry.getPreTaxAmount();
						} else {
							curRest += entry.getPreTaxAmount();
						}
					}
				}
				detail = detail.replaceAll("%\\[" + complexKey + "\\(.*\\)\\]",
					FinanceUtils.formatMoney(curInc, Currency.EUR));
				detail = detail.replaceAll("%\\[" + complexKey + "REST\\]",
					FinanceUtils.formatMoney(curRest, Currency.EUR));
			}
		}
		return detail;
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
							finLogText.append(FinanceUtils.formatMoney(row.getAmount(), Currency.EUR));
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
						for (JPanel detail : getDetailPanelsToShowToUser(database)) {
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
					setDoneLog(taskLog.getText());
				} else {
					Task.this.done = true;
					setDoneDate(new Date());
					String detailsForUser = getDetailsToShowToUser(database);
					if (detailsForUser == null) {
						setDoneLog(taskLog.getText());
					} else {
						StringBuilder originalDetails = new StringBuilder();
						originalDetails.append(taskLog.getText());
						originalDetails.append("\n\n");
						originalDetails.append("Original Details:");
						originalDetails.append("\n");
						originalDetails.append(detailsForUser);
						setDoneLog(originalDetails.toString());
					}
				}

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
							Integer amount = FinanceUtils.parseMoney(lineSplit[1]);
							entry.add(new FinanceLogEntryRow(lineSplit[0], amount));
							wroteARow = true;
							if (lineSplit.length > 2) {
								GuiUtils.complain("The line '" + line + "' contained more than one : sign!\n" +
									"It was parsed as " + lineSplit[0] + ": " + FinanceUtils.formatMoney(amount,
									Currency.EUR));
							}
						}
						if (lineSplit.length < 2) {
							GuiUtils.complain("The line '" + line + "' contained no : sign!\n" +
								"It was ignored.");
						}
					}
					if (!wroteARow) {
						GuiUtils.complain("It looks like the Finance Log section was not filled!\n" +
							"You can edit the Finance Log section in the task's details on the Task Log tab.");
					}
					taskCtrl.addFinanceLogEntry(entry);
				}

				if (Task.this.onDone != null) {
					for (String onDoneStr : Task.this.onDone) {
						if (onDoneStr == null) {
							continue;
						}
						switch (onDoneStr) {
							case "setVatPrepaymentsPaidForPrevMonth":
								Month prevMonth = database.getPrevMonthFromEntryDate(getReleaseDate());
								prevMonth.setVatPrepaymentsPaidTotal(prevMonth.getRemainingVatPayments());
								break;
							default:
								GuiUtils.complain("After finishing this task, the on-done-hook " + onDoneStr +
									" should be executed... but I do not know what this one means!");
								break;
						}
					}
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
				if (GuiUtils.confirmDelete("task '" + getTitle() + "'")) {
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

	public boolean matches(String searchFor) {
		if ("".equals(searchFor)) {
			return true;
		}
		if (getTitle().replace("\\n", "").toLowerCase().contains(searchFor.toLowerCase())) {
			return true;
		}
		if (details != null) {
			for (String detail : details) {
				if (detail.replace("\\n", "").toLowerCase().contains(searchFor.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
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
