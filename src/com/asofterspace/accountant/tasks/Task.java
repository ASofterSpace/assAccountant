/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tasks;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.AddEntryGUI;
import com.asofterspace.accountant.AssAccountant;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
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
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.IoUtils;
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
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;


/**
 * A task describes some action that will have to be taken at a certain time,
 * and whose execution can be logged
 */
public class Task extends GenericTask {

	protected TaskCtrl taskCtrl;

	// in case we get hidden and shown again, let us keep track of what the user entered so far...
	private String lastFinLogText = null;
	private String lastTaskLogText = null;

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


	public Task(String title, Integer scheduledOnDay, List<String> scheduledOnDaysOfWeek, List<Integer> scheduledInMonths,
		List<Integer> scheduledInYears, List<String> details, List<String> onDone) {

		super(title, scheduledOnDay, scheduledOnDaysOfWeek, scheduledInMonths, scheduledInYears, details, onDone);

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
	public String getDetailPanelInHtmlToShowToUser(Database database) {

		StringBuilder html = new StringBuilder();

		String detail = getDetailsToShowToUser(database);

		if (detail == null) {
			return "";
		}

		detail = detail.trim();

		String[] detailsAfterReplacement = detail.split("\n");

		for (String detailLine : detailsAfterReplacement) {
			html.append("<div class='line' onclick='accountant.copyText(\"");
			html.append(StrUtils.replaceAll(detailLine, "\"", "\" + '\"' + \"") + "\")'>");
			html.append(detailLine);
			html.append("</div>");
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

			if (detailLine.contains("%[LIST_INCOMING_UNPAID]")) {
				List<Incoming> incomings = database.getIncomings();
				int i = 0;
				for (Incoming incoming : incomings) {
					if (!incoming.getReceived()) {
						JPanel curCurPanel = incoming.createPanelOnGUI(database);
						curPanel.add(curCurPanel, new Arrangement(0, i, 1.0, 0.0));
						i++;
					}
				}
				specialRow = true;
			}

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
							if ("incoming".equals(entryData.getString("kind").toLowerCase())) {
								fakeEntry = new Incoming(entryData, null);
							} else {
								fakeEntry = new Outgoing(entryData, null);
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
				specialRow = true;
			}

			if (detailLine.trim().startsWith("%[ADD_GULP_BANK_STATEMENTS_BTN]")) {
				JButton gulpBankStatementsBtn = new JButton("Gulp Bank Statements");
				curPanel.add(gulpBankStatementsBtn, new Arrangement(0, 0, 1.0, 0.0));
				gulpBankStatementsBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {

						StringBuilder importStr = new StringBuilder();

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
						database.bulkImportBankStatements(importFiles);


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
						database.bulkImportBankStatements(importFiles);


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
						database.bulkImportBankStatements(importFiles);


						if (importStr.length() < 1) {
							importStr.append("\nNo files at all - sorry!");
						}
						GuiUtils.complain("Imported:\n" + importStr.toString());
					}
				});
				specialRow = true;
			}

			if (detailLine.trim().startsWith("%[CHECK]")) {
				JCheckBox checkBox = new JCheckBox();
				checkBox.setBackground(GUI.getBackgroundColor());
				curPanel.add(checkBox, new Arrangement(0, 0, 0.0, 0.0));
				CopyByClickLabel curLabel = AccountingUtils.createLabel(detailLine.replaceAll("%\\[CHECK\\]", ""),
					new Color(0, 0, 0), "");
				curPanel.add(curLabel, new Arrangement(1, 0, 1.0, 0.0));
				specialRow = true;
			}

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
					FinanceUtils.formatMoney(cur, Currency.EUR));
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
				FinanceUtils.formatMoney(cur, Currency.EUR));
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
					FinanceUtils.formatMoney(cur, Currency.EUR));
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
					FinanceUtils.formatMoney(curInc, Currency.EUR));
				detail = detail.replaceAll("%\\[" + complexKey + "REST\\]",
					FinanceUtils.formatMoney(curRest, Currency.EUR));
			}
		}
		return detail;
	}

	public String createPanelInHtml(Database database) {

		this.database = database;

		String html = "<div class='line'>";

		// TODO - figure out how to do this with html
		/*
		// if we can save last user input, that means that this task has already been shown before,
		// and we can still get the user input from the previously shown GUI parts before showing
		// the next ones!
		saveLastUserInput();
		*/

		textColor = new Color(0, 0, 0);

		html += AccountingUtils.createLabelHtml(getReleasedDateStr(), textColor, "", "text-align: left; width: 8%;");

		int titleWidth = 50;
		if (done && (doneDate != null)) {
			html += AccountingUtils.createLabelHtml(DateUtils.serializeDate(getDoneDate()), textColor, "", "text-align: left; width: 8%;");
			titleWidth = 42;
		}

		html += AccountingUtils.createLabelHtml(title, textColor, "", "text-align: left; width: " + titleWidth + "%;");

		html += "<span class='button' style='width:8%; float:right;' ";
		html += "onclick='accountant.showDetails(\"" + getId() + "\")'>Details</span>";

		// TODO add working buttons
		/*
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
		} else {
			// if it is not yet done, but we have a "last" text...
			if ((lastTaskLogText != null) && !("".equals(lastTaskLogText))) {
				// ... show this one!
				taskLog.setText(lastTaskLogText);
			}
		}
		int newHeight = taskLog.getPreferredSize().height + 48;
		if (newHeight < 128) {
			newHeight = 128;
		}
		taskLog.setPreferredSize(new Dimension(128, newHeight));

		finLog = new JTextPane();
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
				finLogText.append("\n\nCopy this to an external editor, modify it there, and copy it back in here (without this line) just before you click on [Done]!");
				finLog.setText(finLogText.toString());
			}
			// if this has not been done before, ...
			if (!done) {
				// ... but has been shown and filled with data before...
				if ((lastFinLogText != null) && !("".equals(lastFinLogText))) {
					// ... keep showing the data that the user previously entered!
					finLog.setText(lastFinLogText);
				} else {
					// ... or load the latest finance log keys, but do not assign values, to get a "fresh" finLog!
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
		*/

		html += "</div>";

		html += "<div id='task-details-" + getId() + "' style='display:none;'>";
		html += "</div>";

		return html;
	}

	public JPanel createPanelOnGUI(Database database, JPanel tab, JPanel parentPanel) {

		this.database = database;
		this.tab = tab;
		this.parentPanel = parentPanel;

		// if we can save last user input, that means that this task has already been shown before,
		// and we can still get the user input from the previously shown GUI parts before showing
		// the next ones!
		saveLastUserInput();

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
		} else {
			// if it is not yet done, but we have a "last" text...
			if ((lastTaskLogText != null) && !("".equals(lastTaskLogText))) {
				// ... show this one!
				taskLog.setText(lastTaskLogText);
			}
		}
		int newHeight = taskLog.getPreferredSize().height + 48;
		if (newHeight < 128) {
			newHeight = 128;
		}
		taskLog.setPreferredSize(new Dimension(128, newHeight));

		finLog = new JTextPane();
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
				finLogText.append("\n\nCopy this to an external editor, modify it there, and copy it back in here (without this line) just before you click on [Done]!");
				finLog.setText(finLogText.toString());
			}
			// if this has not been done before, ...
			if (!done) {
				// ... but has been shown and filled with data before...
				if ((lastFinLogText != null) && !("".equals(lastFinLogText))) {
					// ... keep showing the data that the user previously entered!
					finLog.setText(lastFinLogText);
				} else {
					// ... or load the latest finance log keys, but do not assign values, to get a "fresh" finLog!
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
						taskCtrl.removeFinanceLogForDate(doneDate);
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

		saveLastUserInput();

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

	private void saveLastUserInput() {

		if (finLog != null) {
			lastFinLogText = finLog.getText();
		}
		if (taskLog != null) {
			lastTaskLogText = taskLog.getText();
		}
	}

}
