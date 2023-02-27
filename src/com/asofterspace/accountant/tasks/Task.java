/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tasks;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.AssAccountant;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.TimeSpan;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.accounting.FinanceUtils;
import com.asofterspace.toolbox.calendar.GenericTask;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;


/**
 * A task describes some action that will have to be taken at a certain time,
 * and whose execution can be logged
 */
public class Task extends GenericTask {

	protected TaskCtrl taskCtrl;

	// these are our current contect for showing us, which will be used by showDetails and hideDetails
	// if this task is shown on one panel, the user opens the TaskDetailEditGUI, and the task gets shown
	// on another panel, and the user saved in the TaskDetailEditGUI, only the new panel will contain a
	// refreshed view of the task (as only the latest of these properties here are used), but as we
	// only show tasks in one place for any view and never the same task in several places, this will
	// never lead to any problems
	private JPanel containerPanel;
	private JPanel parentPanel;
	private JPanel tab;
	private JButton detailsButton;
	private List<JComponent> addedLines;
	private JTextPane finLog;
	private JTextPane taskLog;
	private Color textColor;
	private Database database;

	// for Mari, ids are completely ephemeral - that is, when Mari is restarted, ids are re-assigned
	private String id;

	// just an id that is counted up for identifying checkboxes etc.
	private static int count_up_id = 0;


	public Task(String title, Integer scheduledOnXDayOfMonth, Integer scheduledOnDay, List<String> scheduledOnDaysOfWeek,
		List<Integer> scheduledInMonths, List<Integer> scheduledInYears,
		List<String> details, List<String> onDone, Boolean biweeklyEven, Boolean biweeklyOdd) {

		super(title, scheduledOnXDayOfMonth, scheduledOnDay, scheduledOnDaysOfWeek, scheduledInMonths, scheduledInYears,
			details, onDone, biweeklyEven, biweeklyOdd);

		this.taskCtrl = AssAccountant.getTaskCtrl();
	}

	public Task(GenericTask other) {
		super(other);

		this.taskCtrl = AssAccountant.getTaskCtrl();
	}

	public boolean hasId(String otherId) {
		if (id == null) {
			return false;
		}
		return id.equals(otherId);
	}

	public String getId() {
		if (id == null) {
			id = "" + UUID.randomUUID();
		}
		return id;
	}

	@Override
	public Task getNewInstance() {
		return new Task(this);
	}

	public boolean doDetailsExist() {
		if ((details == null) || (details.size() < 1)) {
			return false;
		}

		for (String detail : details) {
			if ((detail != null) && !detail.trim().equals("")) {
				return true;
			}
		}

		return false;
	}

	public String getDetailsToShowToUser(Database database) {

		if (!doDetailsExist()) {
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
	public String getDetailPanelInHtmlToShowToUser(Database database) {

		StringBuilder html = new StringBuilder();

		String detail = getDetailsToShowToUser(database);

		if (detail == null) {
			return "";
		}

		detail = detail.trim();

		String[] detailsAfterReplacement = detail.split("\n");

		for (String detailLine : detailsAfterReplacement) {

			boolean copyLineOnClick = true;

			if (detailLine.contains("%[CHECK]")) {
				detailLine = StrUtils.replaceFirst(detailLine, "%[CHECK]", "<input type='checkbox' id='check_" + count_up_id + "'>");
				detailLine = "<label for='check_" + count_up_id + "'>" + detailLine + "</label>";
				count_up_id++;
				copyLineOnClick = false;
			}

			if (detailLine.trim().startsWith("%[ADD_GULP_BANK_STATEMENTS_BTN]")) {
				detailLine = StrUtils.replaceAll(detailLine,
					"%[ADD_GULP_BANK_STATEMENTS_BTN]",
					"<span class='button' onclick='accountant.gulpBankStatements();'>" +
					"Gulp Bank Statements</span>");
				copyLineOnClick = false;
			}

			if (detailLine.contains("%[LIST_INCOMING_UNPAID]")) {
				boolean showedAny = false;
				List<Incoming> incomings = database.getIncomings();
				for (Incoming incoming : incomings) {
					if (!incoming.getReceived()) {
						html.append(incoming.createPanelHtml(database));
						showedAny = true;
					}
				}
				if (showedAny) {
					// already appended the output directly to the html,
					// no need to continue in this loop iteration
					continue;
				} else {
					detailLine = "All invoices have been paid - none are outstanding!";
				}
			}

			String keyBefore = "%[ADD_ENTRY_BTN(";
			String keyAfter = ")]";
			int indexBefore = detailLine.indexOf(keyBefore) + keyBefore.length();
			int indexAfter = detailLine.lastIndexOf(keyAfter);
			if ((indexBefore >= 0) && (indexAfter >= 0) && (indexBefore <= indexAfter)) {
				detailLine = detailLine.substring(0, indexAfter);
				detailLine = detailLine.substring(indexBefore);
				try {
					JSON entryData = new JSON(detailLine);
					detailLine = "<span class='button' onclick='var d = " + StrUtils.replaceAll(detailLine, "'", "\\'") +
						"; accountant.showAddEntryModal(d);'>" +
						entryData.getString("buttonText") + "</span>";
					copyLineOnClick = false;
				} catch (JsonParseException e) {
					detailLine = "(Here should be a button, but the data could not be parsed: " + detailLine + ")";
				}
			}

			if (copyLineOnClick && (detailLine.trim().startsWith("http://") || detailLine.trim().startsWith("https://"))) {
				html.append("<div class='line'>");
				html.append("<a target='_blank' href='" + detailLine.trim() + "'>" + detailLine + "</a>");
				html.append("</div>");
			} else {
				html.append("<div class='line'");

				if (copyLineOnClick) {
					html.append(" onclick='accountant.copyText(\"");
					html.append(StrUtils.replaceAll(detailLine.trim(), "\"", "\" + '\"' + \"") + "\")'");
				}

				html.append(">");

				if (detailLine.equals("")) {
					detailLine = "&nbsp;";
				}
				html.append(detailLine);
				html.append("</div>");
			}
		}

		html.append("<div>&nbsp;</div>" +
			"<div><span onclick='accountant.editDetails(\"" + getId() +
			"\")' class='button'>Edit Details</span></div>" +
			"<div>&nbsp;</div>");

		html.append("<div>Task log:</div>");
		html.append("<textarea id='task-log-" + getId() + "'>");
		// if the task is already done...
		if (doneLog != null) {
			// ... set the saved task log text!
			html.append(doneLog);
		}
		html.append("</textarea>");

		if (Task.this instanceof FinanceOverviewTask) {
			html.append("<div>&nbsp;</div>");
			html.append("<div>Finance log:</div>");
			html.append("<textarea id='task-finance-log-" + getId() + "'>");
			StringBuilder finLogText = new StringBuilder();
			List<FinanceLogEntry> entries = taskCtrl.getFinanceLogs();
			// if this was done before, load the finance log contents as filled in back then
			if (done && (getDoneDate() != null)) {
				for (FinanceLogEntry entry : entries) {
					if (DateUtils.isSameDay(entry.getDate(), getDoneDate())) {
						for (FinanceLogEntryRow row : entry.getRows()) {
							finLogText.append(row.getAccount());
							finLogText.append(": ");
							finLogText.append(database.formatMoney(row.getAmount(), Currency.EUR));
							finLogText.append("\n");
						}
					}
				}
			} else {
				// if not, then load the latest finance log keys, but do not assign values, to get a "fresh" finLog!
				if (entries.size() > 0) {
					FinanceLogEntry entry = entries.get(0);
					for (FinanceLogEntryRow row : entry.getRows()) {
						finLogText.append(row.getAccount());
						finLogText.append(": ");
						finLogText.append("\n");
					}
				}
			}
			html.append(finLogText);
			html.append("</textarea>");
			html.append("<div>Copy this to an external editor, modify it there, and copy it back in here " +
				"just before you click on [Done]!</div>");
		}


		return html.toString();
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

			boolean specialRow = false;

			if (!specialRow) {
				CopyByClickLabel curLabel = AccountingUtils.createLabel(detailLine, new Color(0, 0, 0), "");
				curPanel.add(curLabel, new Arrangement(0, 0, 1.0, 0.0));
			}

			results.add(curPanel);
		}
		return results;
	}

	private String replaceDetailsFor(String detail, String timeSpanStr, TimeSpan timeSpan) {

		// USt Vorauszahlungssoll:
		if (detail.contains("%[TOTAL_PAID_VAT_PREPAYMENTS_" + timeSpanStr + "]")) {
			detail = detail.replaceAll("%\\[TOTAL_PAID_VAT_PREPAYMENTS_" + timeSpanStr + "\\]",
				database.formatMoney(timeSpan.getVatPrepaymentsPaidTotal(), Currency.EUR));
		}

		// Total deductible already paid VAT / USt Gesamte abziehbare Vorsteuerbeträge:
		if (detail.contains("%[VAT_TOTAL_DISCOUNTABLE_PRETAX_" + timeSpanStr + "]")) {
			detail = detail.replaceAll("%\\[VAT_TOTAL_DISCOUNTABLE_PRETAX_" + timeSpanStr + "\\]",
				database.formatMoney(timeSpan.getDiscountablePreTax(), Currency.EUR));
		}

		// Remaining VAT advance payment / Verbleibende Umsatzsteuer-Vorauszahlung:
		if (detail.contains("%[VAT_TOTAL_REMAINING_TAX_" + timeSpanStr + "]")) {
			detail = detail.replaceAll("%\\[VAT_TOTAL_REMAINING_TAX_" + timeSpanStr + "\\]",
				database.formatMoney(timeSpan.getRemainingVatPayments(), Currency.EUR));
		}

		int[] taxAmounts = {0, 5, 7, 16, 19};

		// handle stuff like %[VAT_TOTAL_INCOMING_PREV_MONTH_TAX_16%]
		for (int curTaxAmount : taxAmounts) {
			if (detail.contains("%[VAT_TOTAL_INCOMING_" + timeSpanStr + "_TAX_" + curTaxAmount + "%]")) {
				int cur = 0;
				for (Incoming entry : timeSpan.getIncomings()) {
					if (curTaxAmount == entry.getTaxPercent()) {
						cur += entry.getPreTaxAmount();
					}
				}
				detail = detail.replaceAll("%\\[VAT_TOTAL_INCOMING_" + timeSpanStr + "_TAX_" + curTaxAmount + "%\\]",
					database.formatMoney(cur, Currency.EUR));
			}
		}

		detail = replaceComplexVatInDetails(detail, "VAT_TOTAL_INCOMING_" + timeSpanStr + "_TAX_0%_", timeSpan);

		// handle stuff like %[VAT_TOTAL_INCOMING_PREV_MONTH_JUST_TAX]
		if (detail.contains("%[VAT_TOTAL_INCOMING_" + timeSpanStr + "_JUST_TAX]")) {
			int cur = 0;
			for (Incoming entry : timeSpan.getIncomings()) {
				cur += entry.getTaxAmount();
			}
			detail = detail.replaceAll("%\\[VAT_TOTAL_INCOMING_" + timeSpanStr + "_JUST_TAX\\]",
				database.formatMoney(cur, Currency.EUR));
		}

		// handle stuff like %[VAT_TOTAL_INCOMING_PREV_MONTH_TAX_16%_JUST_TAX]
		for (int curTaxAmount : taxAmounts) {
			if (detail.contains("%[VAT_TOTAL_INCOMING_" + timeSpanStr + "_TAX_" + curTaxAmount + "%_JUST_TAX]")) {
				int cur = 0;
				for (Incoming entry : timeSpan.getIncomings()) {
					if (curTaxAmount == entry.getTaxPercent()) {
						cur += entry.getTaxAmount();
					}
				}
				detail = detail.replaceAll("%\\[VAT_TOTAL_INCOMING_" + timeSpanStr + "_TAX_" + curTaxAmount + "%_JUST_TAX\\]",
					database.formatMoney(cur, Currency.EUR));
			}
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
				for (Incoming entry : timeSpan.getIncomings()) {
					if (0 == entry.getTaxPercent()) {
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
					database.formatMoney(curInc, Currency.EUR));
				detail = detail.replaceAll("%\\[" + complexKey + "REST\\]",
					database.formatMoney(curRest, Currency.EUR));
			}
		}
		return detail;
	}

	public void setToDone(String taskLogText, String finLogText) {

		if (this.done) {
			if (this instanceof FinanceOverviewTask) {
				taskCtrl.removeFinanceLogForDate(getDoneDate());
			}
			setDoneLog(taskLog.getText());
		} else {
			this.done = true;
			setDoneDate(new Date());
			String detailsForUser = getDetailsToShowToUser(database);
			if (detailsForUser == null) {
				setDoneLog(taskLog.getText());
			} else {
				StringBuilder originalDetails = new StringBuilder();
				originalDetails.append(taskLogText);
				originalDetails.append("\n\n");
				originalDetails.append("Original Details:");
				originalDetails.append("\n");
				originalDetails.append(detailsForUser);
				setDoneLog(originalDetails.toString());
			}
		}

		if (this instanceof FinanceOverviewTask) {
			FinanceLogEntry entry = new FinanceLogEntry(getDoneDate());
			String[] finLogLines = finLogText.split("\n");
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
							"It was parsed as " + lineSplit[0] + ": " + database.formatMoney(amount,
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

		if (this.onDone != null) {
			for (String onDoneStr : this.onDone) {
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

	public String createPanelInHtml(Database database) {

		this.database = database;

		String html = "<div class='line'>";

		textColor = new Color(0, 0, 0);

		html += AccountingUtils.createLabelHtml(getReleasedDateStr(), textColor, "", "text-align: left; width: 8%;");

		int titleWidth = 50;
		if (done && (getDoneDate() != null)) {
			html += AccountingUtils.createLabelHtml(DateUtils.serializeDate(getDoneDate()), textColor, "", "text-align: left; width: 8%;");
			titleWidth = 42;
		}

		html += AccountingUtils.createLabelHtml(title, textColor, "", "text-align: left; width: " + titleWidth + "%;");

		html += "<span class='button' style='width:6%; float:right;' ";
		html += "onclick='accountant.deleteTask(\"" + getId() + "\", \"" +
			StrUtils.replaceAll(title, "\"", "\\\"") + "\")'>";
		html += "Delete";
		html += "</span>";

		html += "<span class='button' style='width:6%; float:right; margin-right: 4pt;' ";
		html += "onclick='accountant.setTaskDone(\"" + getId() + "\")'>";
		html += "Done";
		html += "</span>";

		html += "<span class='button' style='width:6%; float:right; margin-right: 4pt;' ";
		html += "onclick='accountant.showDetails(\"" + getId() + "\")'>";
		html += "Details";
		html += "</span>";
		html += "</div>";

		html += "<div id='task-details-" + getId() + "' class='taskDetails' style='display:none;'>";
		html += "</div>";

		return html;
	}

	public JPanel createPanelOnGUI(Database database, JPanel tab, JPanel parentPanel) {

		this.database = database;
		this.tab = tab;
		this.parentPanel = parentPanel;

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();
		textColor = new Color(0, 0, 0);

		containerPanel = new JPanel();
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
		if (done && (getDoneDate() != null)) {
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

		addedLines = new ArrayList<>();
		detailsButton = new JButton("Show Details");
		detailsButton.addMouseListener(rowHighlighter);
		if ((details == null) || (details.size() < 1)) {
			detailsButton.setText("Add Details");
			// actually keep the details button enabled in case the user wants to add a log
			// detailsButton.setEnabled(false);
		}
		if (done) {
			detailsButton.setText("Show Details");
		}
		detailsButton.setPreferredSize(defaultDimension);
		curPanel.add(detailsButton, new Arrangement(h, 0, 0.1, 1.0));
		h++;

		taskLog = new JTextPane();
		// if the task is already done...
		if (doneLog != null) {
			// ... set the saved task log text!
			taskLog.setText(doneLog);
		}
		int newHeight = taskLog.getPreferredSize().height + 48;
		if (newHeight < 128) {
			newHeight = 128;
		}
		taskLog.setPreferredSize(new Dimension(128, newHeight));

		finLog = new JTextPane();
		if (Task.this instanceof FinanceOverviewTask) {
			// if this was done before, load the finance log contents as filled in back then
			if (done && (getDoneDate() != null)) {
				StringBuilder finLogText = new StringBuilder();
				List<FinanceLogEntry> entries = taskCtrl.getFinanceLogs();
				for (FinanceLogEntry entry : entries) {
					if (DateUtils.isSameDay(entry.getDate(), getDoneDate())) {
						for (FinanceLogEntryRow row : entry.getRows()) {
							finLogText.append(row.getAccount());
							finLogText.append(": ");
							finLogText.append(database.formatMoney(row.getAmount(), Currency.EUR));
							finLogText.append("\n");
						}
					}
				}
				finLogText.append("\n\nCopy this to an external editor, modify it there, and copy it back in here (without this line) just before you click on [Done]!");
				finLog.setText(finLogText.toString());
			} else {
				// ... then load the latest finance log keys, but do not assign values, to get a "fresh" finLog!
				List<FinanceLogEntry> entries = taskCtrl.getFinanceLogs();
				StringBuilder finLogText = new StringBuilder();
				if (entries.size() > 0) {
					FinanceLogEntry entry = entries.get(0);
					for (FinanceLogEntryRow row : entry.getRows()) {
						finLogText.append(row.getAccount());
						finLogText.append(": ");
						finLogText.append("\n");
					}
				}
				finLog.setText(finLogText.toString());
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
					showDetails();
				} else {
					hideDetails();
				}
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
						taskCtrl.removeFinanceLogForDate(getDoneDate());
					}
					setDoneLog(taskLog.getText());
				} else {
					Task.this.done = true;
					Date now = DateUtils.now();
					setDoneDate(now);
					setSetToDoneDateTime(now);
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
					FinanceLogEntry entry = new FinanceLogEntry(getDoneDate());
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
									"It was parsed as " + lineSplit[0] + ": " + database.formatMoney(amount,
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

		curLabel = AccountingUtils.createLabel("", textColor, "");
		curLabel.addMouseListener(rowHighlighter);
		curPanel.add(curLabel, new Arrangement(h, 0, 0.0, 1.0));
		h++;

		containerPanel.add(curPanel, new Arrangement(0, 0, 1.0, 1.0));

		return containerPanel;
	}

	public void showDetails() {

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

		JPanel detailEditPanel = new JPanel();
		detailEditPanel.setBackground(GUI.getBackgroundColor());
		detailEditPanel.setLayout(new GridBagLayout());
		JButton detailEditButton = new JButton("Edit Task Details");
		detailEditPanel.add(detailEditButton);
		containerPanel.add(detailEditPanel, new Arrangement(0, i, 1.0, 1.0));
		addedLines.add(detailEditPanel);
		i++;

		detailEditButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TaskDetailEditGUI taskEditGUI = new TaskDetailEditGUI(database.getGUI(), database, Task.this);
				taskEditGUI.show();
			}
		});

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

		AccountingUtils.resetTabSize(tab, parentPanel);
	}

	public void hideDetails() {

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

		AccountingUtils.resetTabSize(tab, parentPanel);
	}

}
