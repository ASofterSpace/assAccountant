
/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.web;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.AssAccountant;
import com.asofterspace.accountant.ConfigCtrl;
import com.asofterspace.accountant.data.OutgoingOverviewData;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.Problem;
import com.asofterspace.accountant.TabCtrl;
import com.asofterspace.accountant.tabs.BankStatementTab;
import com.asofterspace.accountant.tabs.BankStatementYearTab;
import com.asofterspace.accountant.tabs.MonthTab;
import com.asofterspace.accountant.tabs.Tab;
import com.asofterspace.accountant.tabs.TimeSpanTab;
import com.asofterspace.accountant.tabs.YearTab;
import com.asofterspace.accountant.tasks.Task;
import com.asofterspace.accountant.tasks.TaskCtrl;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.TimeSpan;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.world.Category;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.accounting.FinanceUtils;
import com.asofterspace.toolbox.calendar.GenericTask;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.io.TextFile;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.StrUtils;
import com.asofterspace.toolbox.virtualEmployees.SideBarCtrl;
import com.asofterspace.toolbox.virtualEmployees.SideBarEntryForEmployee;
import com.asofterspace.toolbox.web.WebServer;
import com.asofterspace.toolbox.web.WebServerAnswer;
import com.asofterspace.toolbox.web.WebServerAnswerInJson;
import com.asofterspace.toolbox.web.WebServerAnswerWithText;
import com.asofterspace.toolbox.web.WebServerRequestHandler;

import java.awt.Desktop;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class ServerRequestHandler extends WebServerRequestHandler {

	private Database database;

	private Directory serverDir;

	private TabCtrl tabCtrl;

	private Server accServer;


	public ServerRequestHandler(Server server, Socket request, Directory webRoot, Directory serverDir,
		Database db, TabCtrl tabCtrl) {

		super(server, request, webRoot);

		this.accServer = server;

		this.database = db;

		this.tabCtrl = tabCtrl;

		this.serverDir = serverDir;
	}

	@Override
	protected void handlePost(String fileLocation) throws IOException {

		String jsonData = receiveJsonContent();

		if (jsonData == null) {
			respond(400);
			return;
		}

		WebServerAnswer sideBarAnswer = SideBarCtrl.handlePost(fileLocation, jsonData);
		if (sideBarAnswer != null) {
			respond(200, sideBarAnswer);
			return;
		}


		// TODO :: catch some IO exceptions? (or make sure that none are thrown?)

		JSON json;
		try {
			json = new JSON(jsonData);
		} catch (JsonParseException e) {
			respond(400);
			return;
		}

		WebServerAnswer answer = new WebServerAnswerInJson("{\"success\": true}");

		try {

			switch (fileLocation) {

				case "/calcCategoryBasedOnTitle":
					Category cat = database.mapTitleToCategory(json.getString("title"));
					if (cat != null) {
						answer = new WebServerAnswerInJson("{\"success\": true, \"category\": \"" +
							cat.getText() + "\"}");
					}
					break;

				case "/exportCSVs":
					Directory exportDir = new Directory(AssAccountant.getWebRoot().getParentDirectory(), "export");
					String tabStr = json.getString("tab");
					Tab tab = linkToTab(tabStr);
					if (tab == null) {
						respond(400);
						break;
					}
					Directory exportedDir = tab.exportCsvTo(exportDir, database);
					answer = new WebServerAnswerInJson(new JSON(
						"{\"exportPath\": \"" + StrUtils.replaceAll(exportedDir.getCanonicalDirname(), "\\", "/") + "\"}"));
					GuiUtils.openFolder(exportedDir);
					break;

				case "/openInOS":
					String diskLocation = ConfigCtrl.getInvoiceLocation(
						json.getInteger("year"),
						json.getInteger("month")
					);
					Directory diskLocationFile = new Directory(diskLocation);
					try {
						Desktop.getDesktop().open(diskLocationFile.getJavaFile());
					} catch (IOException ex) {
						System.out.println("Sorry, the folder " +
							diskLocationFile.getAbsoluteDirname() + " could not be opened!");
					}
					break;

				case "/calcPostTax":
					Integer amountPreTaxInt = FinanceUtils.parseMoney(json.getString("preTax"));
					Integer amountTaxInt = AccountingUtils.parseTaxes(json.getString("tax"));
					Integer amountPostTaxInt = FinanceUtils.calcPostTax(amountPreTaxInt, amountTaxInt);
					if (amountPostTaxInt != null) {
						answer = new WebServerAnswerInJson("{\"success\": true, \"postTax\": \"" +
							database.formatMoney(amountPostTaxInt) + "\"}");
					}
					break;

				case "/calcPreTax":
					amountPostTaxInt = FinanceUtils.parseMoney(json.getString("postTax"));
					amountTaxInt = AccountingUtils.parseTaxes(json.getString("tax"));
					amountPreTaxInt = FinanceUtils.calcPreTax(amountPostTaxInt, amountTaxInt);
					if (amountPreTaxInt != null) {
						answer = new WebServerAnswerInJson("{\"success\": true, \"preTax\": \"" +
							database.formatMoney(amountPreTaxInt) + "\"}");
					}
					break;

				case "/deleteEntry":
					String editingId = json.getString("id");
					Entry editingEntry = database.getEntry(editingId);
					if (editingEntry != null) {
						editingEntry.deleteFrom(database);
					} else {
						answer = new WebServerAnswerInJson("{\"success\": false, \"error\": \"" +
							"The entry could not be found - and therefore, could not be deleted!\"}");
						respond(403, answer);
						return;
					}
					break;

				case "/paidEntry":
					editingId = json.getString("id");
					editingEntry = database.getEntry(editingId);
					if (editingEntry != null) {
						if (editingEntry.setPaidInfo(json.getBoolean("received"),
							json.getString("receivedOnDate"), json.getString("receivedOnAccount"))) {
							database.save();
						} else {
							answer = new WebServerAnswerInJson("{\"success\": false, \"error\": \"" +
								"There was some unspecified error!\"}");
							respond(403, answer);
							return;
						}
					} else {
						answer = new WebServerAnswerInJson("{\"success\": false, \"error\": \"" +
							"The entry could not be found - and therefore, its paid info could not be updated!\"}");
						respond(403, answer);
						return;
					}
					break;

				case "/addEntry":

					String catOrCustomer = json.getString("customer");
					if ("out".equals(json.getString("kind"))) {
						catOrCustomer = json.getString("category");
					}

					String preTaxAmountStr = null;
					String postTaxAmountStr = null;

					if (json.getBoolean("lastTaxChangeWasPreTax")) {
						preTaxAmountStr = json.getString("amount");
					} else {
						postTaxAmountStr = json.getString("postTaxAmount");
					}

					String dateStr = json.getString("date");
					Date date = DateUtils.parseDate(dateStr);
					if (date == null) {
						answer = new WebServerAnswerInJson("{\"success\": false, \"error\": \"" +
							"The text " + dateStr + " could not be parsed as date! " +
							"Please use YYYY-MM-DD or DD. MM. YYYY as date format.\"}");
						respond(403, answer);
						return;
					}

					// we add the new entry (no matter if we are editing a new one or editing an existing one)...
					if (database.addEntry(date, json.getString("title"), catOrCustomer,
						preTaxAmountStr, Currency.EUR, json.getString("taxationPercent"), postTaxAmountStr,
						json.getString("originator"), "out".equals(json.getString("kind")))) {

						// ... and if we are editing an existing one, we delete the existing one
						// (think about this as the scifi transporter approach to editing ^^)
						editingId = json.getString("id");
						editingEntry = database.getEntry(editingId);
						if (editingEntry != null) {
							editingEntry.deleteFrom(database);
						}

						answer = new WebServerAnswerInJson("{\"success\": true, \"link\": \"" +
							AccountingUtils.getMonthLink(date) + "\"}");
					}
					break;

				default:
					respond(404);
					return;
			}

		} catch (JsonParseException e) {
			respond(403);
			return;
		}

		respond(200, answer);
	}

	@Override
	protected WebServerAnswer answerGet(String location, Map<String, String> arguments) {

		if (location.equals("/exit")) {
			System.exit(0);
		}

		if ("/taskInstances".equals(location)) {

			JSON json = new JSON(Record.emptyArray());

			TaskCtrl taskCtrl = database.getTaskCtrl();
			taskCtrl.generateNewInstances(DateUtils.now());

			List<GenericTask> taskInstances = taskCtrl.getTaskInstances();
			Date fromDate = DateUtils.parseDate(arguments.get("from"));
			Date toDate = DateUtils.parseDate(arguments.get("to"));

			for (GenericTask taskInstance : taskInstances) {
				if (taskInstance.appliesTo(fromDate, toDate)) {
					json.append(taskCtrl.taskToRecord(taskInstance));
				}
			}

			WebServerAnswerInJson answer = new WebServerAnswerInJson(json);
			return answer;
		}

		if ("/tasks".equals(location)) {

			JSON json = new JSON(Record.emptyArray());

			TaskCtrl taskCtrl = database.getTaskCtrl();

			List<GenericTask> tasks = taskCtrl.getTasks();
			for (GenericTask task : tasks) {
				json.append(taskCtrl.taskToRecord(task));
			}

			WebServerAnswerInJson answer = new WebServerAnswerInJson(json);
			return answer;
		}

		if ("/taskInstance".equals(location)) {

			TaskCtrl taskCtrl = database.getTaskCtrl();

			String id = arguments.get("id");

			List<GenericTask> genericTasks = taskCtrl.getTaskInstances();
			for (GenericTask genericTask : genericTasks) {
				if (genericTask instanceof Task) {
					Task task = (Task) genericTask;
					if (task.hasId(id)) {
						JSON json = new JSON(taskCtrl.taskToRecord(task));
						json.set("success", true);
						json.set("detailsHtml", task.getDetailPanelInHtmlToShowToUser(database));
						WebServerAnswerInJson answer = new WebServerAnswerInJson(json);
						return answer;
					}
				}
			}
		}

		if ("/entry".equals(location)) {
			String id = arguments.get("id");
			Entry entry = database.getEntry(id);
			if (entry != null) {
				boolean forDisplay = true;
				JSON json = new JSON(entry.toRecord(forDisplay));
				json.set("success", true);
				WebServerAnswerInJson answer = new WebServerAnswerInJson(json);
				return answer;
			}
		}

		if ("/unacknowledged-problems".equals(location)) {

			String html = "";

			List<Problem> problems = database.getUnacknowledgedProblems();

			for (Problem curProblem : problems) {
				html += "<div class='line'>";
				html += AccountingUtils.createLabelHtml(curProblem.getProblem(), curProblem.getColor(), "", "text-align: left;");
				html += "</div>";
			}

			if ("".equals(html)) {
				html = "no problems";
			}

			WebServerAnswerWithText answer = new WebServerAnswerWithText(html);
			answer.setTextKind("html");
			return answer;
		}

		/* We are generating a BWA, a Betriebswirtschaftliche Auswertung */
		if (location.startsWith("/print_pdf_bwa_")) {
			Tab tab = linkToTab(location.substring(15));
			if (tab != null) {
				if (tab instanceof TimeSpanTab) {
					TimeSpanTab tsTab = (TimeSpanTab) tab;
					StringBuilder html = new StringBuilder();
					html.append("<html>");
					appendPdfPageHead(html);
					html.append("<body>");

					String title = tab.toString();
					String currentTimespanStr = title;
					String previousTimespanStr = "(kein Zeitraum)";
					String previous2TimespanStr = "(kein Zeitraum)";
					List<TimeSpan> prevTimeSpans = new ArrayList<>();
					List<TimeSpan> prev2TimeSpans = new ArrayList<>();
					html.append("<div class='printIndicator'>Im Querformat zu PDF drucken</div>");
					html.append("<div style='font-size:155%'>Betriebswirtschaftliche Auswertung</div>");
					html.append("<div style='font-size:115%'>Für ");
					if (tab instanceof YearTab) {
						html.append("das Kalenderjahr " + title);
						// get the previous year
						Year prevYear = tsTab.getTimeSpan().getYear().getPreviousYear();
						if (prevYear != null) {
							prevTimeSpans.add(prevYear);
							previousTimespanStr = "" + prevYear.getNum();
							Year prev2Year = prevYear.getPreviousYear();
							if (prev2Year != null) {
								prev2TimeSpans.add(prev2Year);
								previous2TimespanStr = "" + prev2Year.getNum();
							}
						}
					} else {
						Month curMonth = (Month) tsTab.getTimeSpan();
						currentTimespanStr = curMonth.getMonthNameDE() + " " +
							tsTab.getTimeSpan().getYear().getNum();
						html.append("den Monat " + currentTimespanStr);

						// get the same month in the previous year
						Year prevYear = tsTab.getTimeSpan().getYear().getPreviousYear();
						if (prevYear != null) {
							for (Month month : prevYear.getMonths()) {
								if (month.getNum() == curMonth.getNum()) {
									prevTimeSpans.add(month);
									previousTimespanStr = month.getMonthNameDE() + " " + month.getYear().getNum();
								}
							}
						}

						// get January of this year until current month
						// (except in January, where we compare to all of last year)
						if (curMonth.getNum() < 1) {
							// January
							if (prevYear != null) {
								prev2TimeSpans.add(prevYear);
								previous2TimespanStr = "Januar " + prevYear.getNum() + " - Dezember " + prevYear.getNum();
							}
						} else {
							// other months
							List<Month> allMonths = curMonth.getYear().getMonths();
							for (Month month : allMonths) {
								if (month.getNum() <= curMonth.getNum()) {
									prev2TimeSpans.add(month);
								}
							}
							previous2TimespanStr = "Januar " + curMonth.getYear().getNum() + " - " + currentTimespanStr;
						}
					}
					html.append(" (Vergleich mit " + previousTimespanStr + " und " + previous2TimespanStr + ")");
					html.append("</div>");

					double colPositionenWidth = 15;
					int otherColAmount = 9;
					double otherColsWidth = (99 - colPositionenWidth) / otherColAmount;

					html.append("<div class='bold entry bottomborder' style='padding-top:25pt;'>");
					html.append("<span style='width: " + colPositionenWidth + "%; display: inline-block;'>");
					html.append("Positionen");
					html.append("</span>");
					html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
					html.append(currentTimespanStr);
					html.append("</span>");
					html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
					html.append("% Gesamt-<br>leistung");
					html.append("</span>");
					html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
					html.append("% Gesamt-<br>kosten");
					html.append("</span>");
					html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
					html.append(previousTimespanStr);
					html.append("</span>");
					html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
					html.append("% Gesamt-<br>leistung");
					html.append("</span>");
					html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
					html.append("% Gesamt-<br>kosten");
					html.append("</span>");
					html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
					html.append(previous2TimespanStr);
					html.append("</span>");
					html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
					html.append("% Gesamt-<br>leistung");
					html.append("</span>");
					html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
					html.append("% Gesamt-<br>kosten");
					html.append("</span>");
					html.append("</div>");

					OutgoingOverviewData ood = new OutgoingOverviewData(tsTab.getTimeSpan());
					OutgoingOverviewData prevOod = new OutgoingOverviewData(prevTimeSpans);
					OutgoingOverviewData prev2Ood = new OutgoingOverviewData(prev2TimeSpans);

					int gesamtLeistung = ood.getInPostTaxTotal();
					int gesamtKosten = ood.getTotalCosts() - (ood.getInfrastructureCosts() + ood.getWareCosts());
					int rohertrag = gesamtLeistung - (ood.getInfrastructureCosts() + ood.getWareCosts());
					int overallTax = ood.getOutVatPrepaymentsPaid() + getIncomeTax(tsTab.getTimeSpan());

					int prevGesamtLeistung = prevOod.getInPostTaxTotal();
					int prevGesamtKosten = prevOod.getTotalCosts() - (prevOod.getInfrastructureCosts() + prevOod.getWareCosts());
					int prevRohertrag = prevGesamtLeistung - (prevOod.getInfrastructureCosts() + prevOod.getWareCosts());
					int prevOverallTax = prevOod.getOutVatPrepaymentsPaid();
					for (TimeSpan ts : prevTimeSpans) {
						prevOverallTax += getIncomeTax(ts);
					}

					int prev2GesamtLeistung = prev2Ood.getInPostTaxTotal();
					int prev2GesamtKosten = prev2Ood.getTotalCosts() - (prev2Ood.getInfrastructureCosts() + prev2Ood.getWareCosts());
					int prev2Rohertrag = prev2GesamtLeistung - (prev2Ood.getInfrastructureCosts() + prev2Ood.getWareCosts());
					int prev2OverallTax = prev2Ood.getOutVatPrepaymentsPaid();
					for (TimeSpan ts : prev2TimeSpans) {
						prev2OverallTax += getIncomeTax(ts);
					}

					appendBwaLine(html, "Umsatzerlöse", gesamtLeistung, gesamtLeistung, gesamtKosten,
						prevGesamtLeistung, prevGesamtLeistung, prevGesamtKosten, prev2GesamtLeistung, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Bestandsveränderungen", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Aktivierte Eigenleistung", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Gesamtleistung", gesamtLeistung, gesamtLeistung, gesamtKosten,
						prevGesamtLeistung, prevGesamtLeistung, prevGesamtKosten, prev2GesamtLeistung, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "bold");

					appendBwaLine(html, "Material-/Wareneinsatz", ood.getInfrastructureCosts() + ood.getWareCosts(), gesamtLeistung, gesamtKosten,
						prevOod.getInfrastructureCosts() + prevOod.getWareCosts(), prevGesamtLeistung, prevGesamtKosten, prev2Ood.getInfrastructureCosts() + prev2Ood.getWareCosts(), prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Rohertrag", rohertrag, gesamtLeistung, gesamtKosten,
						prevRohertrag, prevGesamtLeistung, prevGesamtKosten, prev2Rohertrag, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "bold");

					appendBwaLine(html, "Sonstige betriebliche Erlöse", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Betrieblicher Rohertrag", rohertrag, gesamtLeistung, gesamtKosten,
						prevRohertrag, prevGesamtLeistung, prevGesamtKosten, prev2Rohertrag, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "bold topborder bottomborder");


					html.append("<div class='bold entry' style='padding-top: 8pt;'>");
					html.append("<span style='width: " + colPositionenWidth + "%; display: inline-block;'>");
					html.append("Kostenarten:");
					html.append("</span>");
					html.append("</div>");

					appendBwaLine(html, "Personalkosten", ood.getInternalSalary(), gesamtLeistung, gesamtKosten,
						prevOod.getInternalSalary(), prevGesamtLeistung, prevGesamtKosten, prev2Ood.getInternalSalary(), prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Raumkosten", ood.getLocationCosts(), gesamtLeistung, gesamtKosten,
						prevOod.getLocationCosts(), prevGesamtLeistung, prevGesamtKosten, prev2Ood.getLocationCosts(), prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Betriebliche Steuern", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Versicherungen/Beiträge", ood.getInsuranceCosts(), gesamtLeistung, gesamtKosten,
						prevOod.getInsuranceCosts(), prevGesamtLeistung, prevGesamtKosten, prev2Ood.getInsuranceCosts(), prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Besondere Kosten", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Kfz-Kosten", ood.getVehicleCosts(), gesamtLeistung, gesamtKosten,
						prevOod.getVehicleCosts(), prevGesamtLeistung, prevGesamtKosten, prev2Ood.getVehicleCosts(), prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Werbe-/Reisekosten", ood.getAdvertisementCosts() + ood.getEntertainmentCosts() + ood.getTravelCosts() + ood.getEducationCosts(), gesamtLeistung, gesamtKosten,
						prevOod.getAdvertisementCosts() + prevOod.getEntertainmentCosts() + prevOod.getTravelCosts() + prevOod.getEducationCosts(), prevGesamtLeistung, prevGesamtKosten, prev2Ood.getAdvertisementCosts() + prev2Ood.getEntertainmentCosts() + prev2Ood.getTravelCosts() + prev2Ood.getEducationCosts(), prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Kosten der Warenabgabe", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Abschreibungen", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Reparatur/Instandhaltung", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					// Fremdleistungen und Auftragsarbeiten booked as part of other costs
					appendBwaLine(html, "Sonstige Kosten", ood.getOtherCosts() + ood.getExternalSalary(), gesamtLeistung, gesamtKosten,
						prevOod.getOtherCosts() + prevOod.getExternalSalary(), prevGesamtLeistung, prevGesamtKosten, prev2Ood.getOtherCosts() + prev2Ood.getExternalSalary(), prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Gesamtkosten", gesamtKosten, gesamtLeistung, gesamtKosten,
						prevGesamtKosten, prevGesamtLeistung, prevGesamtKosten, prev2GesamtKosten, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "bold topborder");

					appendBwaLine(html, "Betriebsergebnis (EBIT)", rohertrag - gesamtKosten, gesamtLeistung, gesamtKosten,
						prevRohertrag - prevGesamtKosten, prevGesamtLeistung, prevGesamtKosten, prev2Rohertrag - prev2GesamtKosten, prev2GesamtLeistung, prev2GesamtKosten,
						colPositionenWidth, otherColsWidth, "bold topborder bottomborder");

					appendBwaLine(html, "Zinsaufwand", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Sonstiger neutraler Aufwand", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Neutraler Aufwand", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "bold");

					appendBwaLine(html, "Zinserträge", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Sonstige neutrale Erträge", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Neutraler Ertrag", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, 0, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "bold");

					appendBwaLine(html, "Ergebnis vor Steuern", rohertrag - gesamtKosten, gesamtLeistung, gesamtKosten,
						prevRohertrag - prevGesamtKosten, prevGesamtLeistung, prevGesamtKosten, prev2Rohertrag - prev2GesamtKosten, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "bold topborder bottomborder");

					appendBwaLine(html, "Steuern vom Einkommen und Ertrag", overallTax, gesamtLeistung, gesamtKosten,
						prevOverallTax, prevGesamtLeistung, prevGesamtKosten, prev2OverallTax, prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Vorläufiges Ergebnis", rohertrag - (gesamtKosten + overallTax), gesamtLeistung, gesamtKosten,
						prevRohertrag - (prevGesamtKosten + prevOverallTax), prevGesamtLeistung, prevGesamtKosten, prev2Rohertrag - (prev2GesamtKosten + prev2OverallTax), prev2GesamtLeistung, prev2GesamtKosten, colPositionenWidth, otherColsWidth, "bold topborder bottomborder");


					html.append("<div style='padding-top:25pt;text-align: right;'><span><img style='width:215pt;' src='/pics/signature.png' /></span></div>");
					html.append("<span style='float:left;font-size: 115%;'>" + database.getLocation() + ", " + DateUtils.serializeDate(DateUtils.now()) + "</span>");
					html.append("<div style='text-align: right;padding-right: 85pt;font-size: 115%;'>" + database.getUserLegalName() + "</div>");
					html.append("</body>");
					html.append("</html>");
					WebServerAnswerWithText answer = new WebServerAnswerWithText(html.toString());
					answer.setTextKind("html");
					return answer;
				}
			}
		}

		/* We are generating a EÜR, which does not contain Privatentnahme or Privateinlage - they need to
		   be used as Verwendungszweck, but do NOT appear here, and this is the correct way to do it!

		   Also, our donations are not at all present in the EÜR - and this is perfectly reasonable:
		   They are actually private expenses, and in the future we should pay them from our private
		   account rather than from the company account! */
		if (location.startsWith("/print_pdf_euer_")) {
			Tab tab = linkToTab(location.substring(16));
			if (tab != null) {
				if (tab instanceof TimeSpanTab) {
					TimeSpanTab tsTab = (TimeSpanTab) tab;
					StringBuilder html = new StringBuilder();
					html.append("<html>");
					appendPdfPageHead(html);
					html.append("<body>");

					String title = tab.toString();
					html.append("<div class='printIndicator'>Im Hochformat zu PDF drucken</div>");
					html.append("<div style='font-size:155%'>Einnahmenüberschussrechnung</div>");
					html.append("<div style='font-size:115%'>Für ");
					if (tab instanceof YearTab) {
						html.append("das Kalenderjahr " + title);
					} else {
						html.append("den Monat " + ((Month) tsTab.getTimeSpan()).getMonthNameDE() + " " +
							tsTab.getTimeSpan().getYear().getNum());
					}
					html.append("</div>");

					html.append("<div class='bold entry' style='padding-top:25pt;'>Betriebseinnahmen</div>");

					List<Incoming> incomings = tsTab.getTimeSpan().getIncomings();
					int in0percTax = 0;
					int in16percTax = 0;
					int in19percTax = 0;
					int inOtherTax = 0;
					int in16percTaxTax = 0;
					int in19percTaxTax = 0;
					int inOtherTaxTax = 0;
					int inTotal = 0;
					for (Incoming incoming : incomings) {
						inTotal += incoming.getPostTaxAmount();
						switch (incoming.getTaxPercent()) {
							case 0:
								in0percTax += incoming.getPreTaxAmount();
								break;
							case 16:
								in16percTax += incoming.getPreTaxAmount();
								in16percTaxTax += incoming.getTaxAmount();
								break;
							case 19:
								in19percTax += incoming.getPreTaxAmount();
								in19percTaxTax += incoming.getTaxAmount();
								break;
							default:
								inOtherTax += incoming.getPreTaxAmount();
								inOtherTaxTax += incoming.getTaxAmount();
								break;
						}
					}

					html.append("<div class='entry sub'>Betriebseinnahmen - zum allgemeinen Steuersatz von 19%:<span class='right'>" +
						FinanceUtils.formatMoneyDE(in19percTax) + " €</span></div>");

					if (in16percTax > 0) {
						html.append("<div class='entry sub'>Betriebseinnahmen - zum Corona-bedingten Steuersatz von 16%:<span class='right'>" +
							FinanceUtils.formatMoneyDE(in16percTax) + " €</span></div>");
					}

					if (inOtherTax > 0) {
						html.append("<div class='entry sub'>Betriebseinnahmen - zu anderen Steuersätzen:<span class='right'>" +
							FinanceUtils.formatMoneyDE(inOtherTax) + " €</span></div>");
					}

					html.append("<div class='entry sub'>Betriebseinnahmen - umsatzsteuerfrei oder nicht steuerbar:<span class='right'>" +
						FinanceUtils.formatMoneyDE(in0percTax) + " €</span></div>");

					html.append("<div class='entry sub'>Vereinnahmte Umsatzsteuer sowie Umsatzsteuer auf Entnahmen:</div>");

					html.append("<div class='entry subsub'>Betriebseinnahmen 19%:<span class='right'>" +
						FinanceUtils.formatMoneyDE(in19percTaxTax) + " €</span></div>");

					if (in16percTaxTax > 0) {
						html.append("<div class='entry subsub'>Betriebseinnahmen 16%:<span class='right'>" +
							FinanceUtils.formatMoneyDE(in16percTaxTax) + " €</span></div>");
					}

					if (inOtherTaxTax > 0) {
						html.append("<div class='entry subsub'>Betriebseinnahmen von anderen Steuersätzen:<span class='right'>" +
							FinanceUtils.formatMoneyDE(inOtherTaxTax) + " €</span></div>");
					}

					html.append("<div class='bold entry'>Summe der anzusetzenden Betriebseinnahmen:<span class='right bold'>" +
							FinanceUtils.formatMoneyDE(inTotal) + " €</span></div>");

					if (inTotal != in0percTax + in16percTax + in19percTax + inOtherTax +
						in16percTaxTax + in19percTaxTax + inOtherTaxTax) {

						html.append("<div class='bold entry'>Es liegt ein Berechnungsfehler vor!</div>");
					}

					html.append("<div class='bold entry' style='padding-top:25pt;'>Betriebsausgaben</div>");

					TimeSpan timeSpan = tsTab.getTimeSpan();

					OutgoingOverviewData ood = new OutgoingOverviewData(timeSpan);

					if (ood.getWareCosts() + ood.getOtherCosts() > 0) {
						html.append("<div class='entry sub'>Waren, Rohstoffe und Hilfsmittel:<span class='right'>" +
							FinanceUtils.formatMoneyDE(ood.getWareCosts() + ood.getOtherCosts()) + " €</span></div>");
					}

					if (ood.getExternalSalary() > 0) {
						html.append("<div class='entry sub'>Fremdleistungen und Auftragsarbeiten:<span class='right'>" +
							FinanceUtils.formatMoneyDE(ood.getExternalSalary()) + " €</span></div>");
					}

					if (ood.getInternalSalary() > 0) {
						html.append("<div class='entry sub'>Personalkosten:<span class='right'>" +
							FinanceUtils.formatMoneyDE(ood.getInternalSalary()) + " €</span></div>");
					}

					if (ood.getInsuranceCosts() > 0) {
						html.append("<div class='entry sub'>Versicherungskosten:<span class='right'>" +
							FinanceUtils.formatMoneyDE(ood.getInsuranceCosts()) + " €</span></div>");
					}

					if (ood.getVehicleCosts() > 0) {
						html.append("<div class='entry sub'>Fahrzeugkosten und andere Fahrtkosten:</div>");

						html.append("<div class='entry subsub'>Leasingkosten:<span class='rightish'>" +
							FinanceUtils.formatMoneyDE(ood.getVehicleCosts()) + " €</span></div>");

						html.append("<div class='entry subsub'>Sonstige tatsächliche Fahrtkosten:<span class='rightish'>" +
							FinanceUtils.formatMoneyDE(ood.getTravelCosts()) + " €</span></div>");

						html.append("<div class='entry sub'>Summe Fahrzeugkosten und andere Fahrtkosten:<span class='right'>" +
							FinanceUtils.formatMoneyDE(ood.getVehicleCosts() + ood.getTravelCosts()) + " €</span></div>");
					} else {
						if (ood.getTravelCosts() > 0) {
							html.append("<div class='entry sub'>Fahrtkosten:<span class='right'>" +
								FinanceUtils.formatMoneyDE(ood.getTravelCosts()) + " €</span></div>");
						}
					}

					if (ood.getLocationCosts() > 0) {
						html.append("<div class='entry sub'>Raumkosten und sonstige Grundstücksaufwendungen:<span class='right'>" +
							FinanceUtils.formatMoneyDE(ood.getLocationCosts()) + " €</span></div>");
					}

					html.append("<div class='entry sub'>Sonstige Betriebsausgaben:</div>");

					if (ood.getEducationCosts() > 0) {
						html.append("<div class='entry subsub'>Fortbildungskosten:<span class='rightish'>" +
							FinanceUtils.formatMoneyDE(ood.getEducationCosts()) + " €</span></div>");
					}

					html.append("<div class='entry subsub'>Laufende EDV-Kosten:<span class='rightish'>" +
						FinanceUtils.formatMoneyDE(ood.getInfrastructureCosts()) + " €</span></div>");

					if (ood.getAdvertisementCosts() + ood.getEntertainmentCosts() > 0) {
						html.append("<div class='entry subsub'>Werbekosten:<span class='rightish'>" +
							FinanceUtils.formatMoneyDE(ood.getAdvertisementCosts() + ood.getEntertainmentCosts()) + " €</span></div>");
					}

					html.append("<div class='entry sub'>Summe sonstige Betriebsausgaben:<span class='right'>" +
						FinanceUtils.formatMoneyDE(ood.getEducationCosts() + ood.getInfrastructureCosts() + ood.getAdvertisementCosts() + ood.getEntertainmentCosts()) + " €</span></div>");

					int outTotal = timeSpan.getOutTotalBeforeTax();

					html.append("<div class='entry sub'>Abziehbare Vorsteuerbeträge:</div>");

					html.append("<div class='entry subsub'>Manuell ermittelte Vorsteuer:<span class='right'>" +
						FinanceUtils.formatMoneyDE(timeSpan.getDiscountablePreTax()) + " €</span></div>");
					outTotal += timeSpan.getDiscountablePreTax();

					html.append("<div class='entry sub'>An das Finanzamt abgeführte Umsatzsteuer:<span class='right'>" +
						FinanceUtils.formatMoneyDE(timeSpan.getVatPrepaymentsPaidTotal()) + " €</span></div>");
					outTotal += timeSpan.getVatPrepaymentsPaidTotal();

					html.append("<div class='entry bold'>Summe der anzusetzenden Betriebsausgaben:<span class='right bold'>" +
						FinanceUtils.formatMoneyDE(outTotal) + " €</span></div>");

					html.append("<div class='bold entry' style='padding-top:25pt;'>Ermittlung des Gewinns</div>");

					html.append("<div class='entry sub'>Summe der Betriebseinnahmen:<span class='right'>" +
						FinanceUtils.formatMoneyDE(inTotal) + " €</span></div>");

					html.append("<div class='entry sub'>Abzüglich Summe der Betriebsausgaben:<span class='right'>" +
						FinanceUtils.formatMoneyDE(outTotal) + " €</span></div>");

					html.append("<div class='entry bold'>Gewinn:<span class='right bold'>" +
						FinanceUtils.formatMoneyDE(inTotal - outTotal) + " €</span></div>");

					html.append("<div style='padding-top:25pt;text-align: right;'><span><img style='width:215pt;' src='/pics/signature.png' /></span></div>");
					html.append("<span style='float:left;font-size: 115%;'>" + database.getLocation() + ", " + DateUtils.serializeDate(DateUtils.now()) + "</span>");
					html.append("<div style='text-align: right;padding-right: 85pt;font-size: 115%;'>" + database.getUserLegalName() + "</div>");
					html.append("</body>");
					html.append("</html>");
					WebServerAnswerWithText answer = new WebServerAnswerWithText(html.toString());
					answer.setTextKind("html");
					return answer;
				}
			}
		}

		return null;
	}

	@Override
	protected String getWhitelistedLocationEquivalent(String location) {

		String result = super.getWhitelistedLocationEquivalent(location);

		if (result == null) {

			if (location.startsWith("/")) {
				location = location.substring(1);
			}

			switch (location) {
				case "bank_statement_graph.png":
				case "finance_log_graph.png":
				case "income_log_graph.png":
				case "income_log_graph_smoothened.png":
					return location;
			}

			if (locationToTabKind(location) != null) {
				return location;
			}
		}

		return result;
	}

	@Override
	protected File getFileFromLocation(String location, String[] arguments) {

		File sideBarImageFile = SideBarCtrl.getSideBarImageFile(location);
		if (sideBarImageFile != null) {
			return sideBarImageFile;
		}

		String locEquiv = getWhitelistedLocationEquivalent(location);

		// if no root is specified, then we are just not serving any files at all
		// and if no location equivalent is found on the whitelist, we are not serving this request
		if ((webRoot != null) && (locEquiv != null)) {

			// serves images and text files directly from the server dir, rather than the deployed dir
			if (locEquiv.toLowerCase().endsWith(".jpg") || locEquiv.toLowerCase().endsWith(".pdf") ||
				locEquiv.toLowerCase().endsWith(".png") || locEquiv.toLowerCase().endsWith(".stp") ||
				locEquiv.toLowerCase().endsWith(".txt") || locEquiv.toLowerCase().endsWith(".stpu") ||
				locEquiv.toLowerCase().endsWith(".json")) {

				File result = new File(serverDir, locEquiv);
				if (result.exists()) {
					return result;
				}
			}

			// answering a request for general information?
			String tabKind = locationToTabKind(locEquiv);

			if (tabKind != null) {

				System.out.println("Answering " + tabKind + " request...");

				for (String arg : arguments) {
					if (arg.startsWith("format=")) {
						database.setFormatStr(arg.substring(arg.indexOf("=") + 1));
					}
				}

				TaskCtrl taskCtrl = database.getTaskCtrl();
				taskCtrl.generateNewInstances(DateUtils.now());

				TextFile indexBaseFile = new TextFile(webRoot, "index.htm");
				String indexContent = indexBaseFile.getContent();

				indexContent = StrUtils.replaceAll(indexContent, "[[SIDEBAR]]",
					SideBarCtrl.getSidebarHtmlStr(new SideBarEntryForEmployee("Mari")));

				String tabsHtml = "<div id='tabList'>";

				List<Tab> tabs = tabCtrl.getTabs();

				Tab currentlySelectedTab = tabCtrl.getOverviewTab();

				for (Tab tab : tabs) {
					tabsHtml += "<a href='";
					tabsHtml += tabToLink(tab);
					tabsHtml += "'";
					if (tabKind.equals(tabToLink(tab))) {
						currentlySelectedTab = tab;
						tabsHtml += " class='selectedTab'";
					}
					tabsHtml += ">&nbsp;" + tab.toString() + "</a>";
					if (tab instanceof BankStatementYearTab) {
						tabsHtml += "<div>&nbsp;</div>";
					}
				}

				tabsHtml += "</div>";

				indexContent = StrUtils.replaceAll(indexContent, "[[FORMAT]]", database.getFormatStr());

				indexContent = StrUtils.replaceAll(indexContent, "[[CURDATE]]", DateUtils.serializeDate(DateUtils.now()));

				StringBuilder cats = new StringBuilder();
				cats.append("[");
				for (Category cat : Category.values()) {
					cats.append("\"" + cat.getText() + "\",");
				}
				cats.append("]");
				indexContent = StrUtils.replaceAll(indexContent, "[[AE_CATEGORIES]]", cats.toString());

				StringBuilder custs = new StringBuilder();
				custs.append("[");
				for (String cust : database.getCustomers()) {
					custs.append("\"" + cust + "\",");
				}
				custs.append("]");
				indexContent = StrUtils.replaceAll(indexContent, "[[AE_CUSTOMERS]]", custs.toString());

				StringBuilder orgs = new StringBuilder();
				orgs.append("[");
				for (String org : database.getOriginators()) {
					orgs.append("\"" + org + "\",");
				}
				orgs.append("]");
				indexContent = StrUtils.replaceAll(indexContent, "[[AE_ORIGINATORS]]", orgs.toString());

				StringBuilder accs = new StringBuilder();
				accs.append("[");
				for (String curAccount : database.getAccounts()) {
					accs.append("\"" + curAccount + "\",");
				}
				accs.append("]");
				indexContent = StrUtils.replaceAll(indexContent, "[[PAID_ACCOUNTS]]", accs.toString());

				indexContent = StrUtils.replaceAll(indexContent, "[[TABS]]", tabsHtml);

				indexContent = StrUtils.replaceAll(indexContent, "[[AVATAR_DESCRIPTION]]", SideBarCtrl.getAvatarDescription(new SideBarEntryForEmployee("Mari")));

				// TODO - enable actually searching for things
				String searchFor = "";

				String mainContent = currentlySelectedTab.getHtmlGUI(database, searchFor);

				indexContent = StrUtils.replaceAll(indexContent, "[[CONTENT]]", mainContent);

				locEquiv = "_" + locEquiv;
				if (!locEquiv.endsWith(".htm")) {
					locEquiv = locEquiv + ".htm";
				}
				TextFile indexFile = new TextFile(webRoot, locEquiv);
				indexFile.saveContent(indexContent);
			}

			// actually get the file
			return webRoot.getFile(locEquiv);
		}

		// if the file was not found on the whitelist, do not return it
		// - even if it exists on the server!
		return null;
	}

	private static String locationToTabKind(String locEquiv) {

		if (locEquiv.startsWith("/")) {
			locEquiv = locEquiv.substring(1);
		}

		if ("index.htm".equals(locEquiv) || "index".equals(locEquiv) || "overview".equals(locEquiv)) {
			return "overview";
		}

		if ("task_log".equals(locEquiv) ||
			"finance_log".equals(locEquiv) ||
			"income_log".equals(locEquiv) ||
			"bank_statements".equals(locEquiv) ||
			"calculator".equals(locEquiv) ||
			"taxes".equals(locEquiv) ||
			"rent".equals(locEquiv) ||
			"loans".equals(locEquiv) ||
			locEquiv.startsWith("year_") ||
			locEquiv.startsWith("month_") ||
			locEquiv.startsWith("bs_year_")) {
			return locEquiv;
		}

		return null;
	}

	public static String tabToLink(Tab tab) {
		String result = tab.toString().toLowerCase();
		result = StrUtils.replaceAll(result, ":", "");
		result = StrUtils.replaceAll(result, " ", "_");
		if ((tab instanceof BankStatementYearTab) && !(tab instanceof BankStatementTab)) {
			result = "bs_year_" + result;
		}
		if (tab instanceof YearTab) {
			result = "year_" + result;
		}
		if (tab instanceof MonthTab) {
			result = "month_" + result;
		}
		return result;
	}

	private Tab linkToTab(String linkStr) {

		List<Tab> tabs = tabCtrl.getTabs();

		for (Tab tab : tabs) {
			if (linkStr.equals(tabToLink(tab))) {
				return tab;
			}
		}

		return null;
	}

	private String adjustPercentageForBwa(double val) {
		return StrUtils.replaceAll(StrUtils.leftPadW(StrUtils.replaceAll(StrUtils.doubleToStr(val, 2), ".", ","), 6), " ", "&nbsp;&nbsp;");
	}

	private void appendBwaLine(StringBuilder html, String positionName, int value, int gesamtLeistung, int gesamtKosten,
		int prevValue, int prevGesamtLeistung, int prevGesamtKosten, int prev2Value, int prev2GesamtLeistung, int prev2GesamtKosten,
		double colPositionenWidth, double otherColsWidth, String divClass) {

		html.append("<div class='entry " + divClass + "'>");
		html.append("<span style='width: " + colPositionenWidth + "%; display: inline-block;'>");
		html.append(positionName);
		html.append("</span>");

		html.append("<span style='text-align: right; width: " + otherColsWidth + "%; display: inline-block;'>");
		html.append(FinanceUtils.formatMoneyDE(value));
		html.append(" €</span>");
		html.append("<span style='text-align: right; width: " + otherColsWidth + "%; display: inline-block;'>");
		if (gesamtLeistung > 0) {
			html.append(adjustPercentageForBwa((value * 100.0) / gesamtLeistung));
		}
		html.append("</span>");
		html.append("<span style='text-align: right; width: " + otherColsWidth + "%; display: inline-block;'>");
		if (gesamtKosten > 0) {
			html.append(adjustPercentageForBwa((value * 100.0) / gesamtKosten));
		}
		html.append("</span>");

		html.append("<span style='text-align: right; width: " + otherColsWidth + "%; display: inline-block;'>");
		html.append(FinanceUtils.formatMoneyDE(prevValue));
		html.append(" €</span>");
		html.append("<span style='text-align: right; width: " + otherColsWidth + "%; display: inline-block;'>");
		if (prevGesamtLeistung > 0) {
			html.append(adjustPercentageForBwa((prevValue * 100.0) / prevGesamtLeistung));
		}
		html.append("</span>");
		html.append("<span style='text-align: right; width: " + otherColsWidth + "%; display: inline-block;'>");
		if (prevGesamtKosten > 0) {
			html.append(adjustPercentageForBwa((prevValue * 100.0) / prevGesamtKosten));
		}
		html.append("</span>");

		html.append("<span style='text-align: right; width: " + otherColsWidth + "%; display: inline-block;'>");
		html.append(FinanceUtils.formatMoneyDE(prev2Value));
		html.append(" €</span>");
		html.append("<span style='text-align: right; width: " + otherColsWidth + "%; display: inline-block;'>");
		if (prev2GesamtLeistung > 0) {
			html.append(adjustPercentageForBwa((prev2Value * 100.0) / prev2GesamtLeistung));
		}
		html.append("</span>");
		html.append("<span style='text-align: right; width: " + otherColsWidth + "%; display: inline-block;'>");
		if (prev2GesamtKosten > 0) {
			html.append(adjustPercentageForBwa((prev2Value * 100.0) / prev2GesamtKosten));
		}
		html.append("</span>");

		html.append("</div>");
	}

	private void appendPdfPageHead(StringBuilder html) {
		html.append("<head>");
		html.append("<meta charset='utf-8'>");
		html.append("<style>");
		html.append("div.entry {");
		html.append("  padding-top: 4pt;");
		html.append("  padding-bottom: 4pt;");
		html.append("}");
		html.append("div.topborder {");
		html.append("  border-top: 1px solid #111;");
		html.append("}");
		html.append("div.bottomborder {");
		html.append("  border-bottom: 1px solid #111;");
		html.append("}");
		html.append("div.small_above {");
		html.append("  font-size: 80%;");
		html.append("}");
		html.append("span.explanation {");
		html.append("}");
		html.append("span.bold, div.bold {");
		html.append("  font-weight: bold;");
		html.append("}");
		html.append("span.right {");
		html.append("  float: right;");
		html.append("}");
		html.append("span.rightish {");
		html.append("  float: right;");
		html.append("  padding-right: 125pt;");
		html.append("}");
		html.append("div.sub {");
		html.append("  padding-left: 25pt;");
		html.append("}");
		html.append("div.subsub {");
		html.append("  padding-left: 50pt;");
		html.append("}");
		html.append("div.printIndicator {");
		html.append("  font-size:185%;");
		html.append("  position: fixed;");
		html.append("  text-align: center;");
		html.append("  width: 100%;");
		html.append("  padding: 20pt;");
		html.append("  opacity: 0.8;");
		html.append("  background: #FFF;");
		html.append("  visibility: hidden;");
		html.append("}");
		html.append("body:hover div.printIndicator {");
		html.append("  visibility: visible;");
		html.append("}");
		html.append("</style>");
		html.append("</head>");
	}

	private int getIncomeTax(TimeSpan ts) {

		Integer result = null;

		if (ts instanceof Year) {
			result = database.getIncomeTaxes().get(ts.getNum());

		} else if (ts instanceof Month) {
			result = database.getIncomeTaxes().get(ts.getYear().getNum());
			if (result != null) {
				result = result / 12;
			}
		}

		if (result == null) {
			return 0;
		}

		return result;
	}

}
