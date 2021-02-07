
/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.web;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.AssAccountant;
import com.asofterspace.accountant.ConfigCtrl;
import com.asofterspace.accountant.Database;
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
					html.append("</style>");
					html.append("</head>");
					html.append("<body>");

					String title = tab.toString();
					String currentTimespanStr = title;
					String previousTimespanStr = "(kein Zeitraum)";
					List<TimeSpan> prevTimeSpans = new ArrayList<>();
					html.append("<div style='font-size:155%'>Betriebswirtschaftliche Auswertung</div>");
					html.append("<div style='font-size:115%'>Für ");
					if (tab instanceof YearTab) {
						html.append("das Kalenderjahr " + title);
						// get the previous year
						Year prevYear = tsTab.getTimeSpan().getYear().getPreviousYear();
						if (prevYear != null) {
							prevTimeSpans.add(prevYear);
							previousTimespanStr = "" + prevYear.getNum();
						}
					} else {
						Month curMonth = (Month) tsTab.getTimeSpan();
						currentTimespanStr = curMonth.getMonthNameDE() + " " +
							tsTab.getTimeSpan().getYear().getNum();
						html.append("den Monat " + currentTimespanStr);

						// get January of this year until current month
						// (except in January, where we compare to all of last year)
						if (curMonth.getNum() < 1) {
							// January
							Year prevYear = tsTab.getTimeSpan().getYear().getPreviousYear();
							if (prevYear != null) {
								prevTimeSpans.add(prevYear);
								previousTimespanStr = "Januar " + prevYear.getNum() + " - Dezember " + prevYear.getNum();
							}
						} else {
							// other months
							List<Month> allMonths = curMonth.getYear().getMonths();
							for (Month month : allMonths) {
								if (month.getNum() <= curMonth.getNum()) {
									prevTimeSpans.add(month);
								}
							}
							previousTimespanStr = "Januar " + curMonth.getYear().getNum() + " - " + currentTimespanStr;
						}
					}
					html.append("</div>");

					double colPositionenWidth = 25;
					int otherColAmount = 6;
					double otherColsWidth = (100 - colPositionenWidth) / otherColAmount;

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
					html.append("</div>");

					List<Incoming> incomings = tsTab.getTimeSpan().getIncomings();
					int inPreTaxTotal = 0;
					int inTaxTotal = 0;
					int inPostTaxTotal = 0;
					for (Incoming incoming : incomings) {
						inPreTaxTotal += incoming.getPreTaxAmount();
						inTaxTotal += incoming.getTaxAmount();
						inPostTaxTotal += incoming.getPostTaxAmount();
					}

					int prevInPostTaxTotal = 0;
					for (TimeSpan prevTimeSpan : prevTimeSpans) {
						List<Incoming> prevIncomings = prevTimeSpan.getIncomings();
						for (Incoming incoming : prevIncomings) {
							prevInPostTaxTotal += incoming.getPostTaxAmount();
						}
					}

					int gesamtLeistung = inPostTaxTotal;
					int gesamtKosten = 0;

					int prevGesamtLeistung = prevInPostTaxTotal;
					int prevGesamtKosten = 0;

					appendBwaLine(html, "Umsatzerlöse", inPostTaxTotal, gesamtLeistung, gesamtKosten,
						prevInPostTaxTotal, prevGesamtLeistung, prevGesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Bestandsveränderungen", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Aktivierte Eigenleistung", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Gesamtleistung", inPostTaxTotal, gesamtLeistung, gesamtKosten,
						prevInPostTaxTotal, prevGesamtLeistung, prevGesamtKosten, colPositionenWidth, otherColsWidth, "bold");

					appendBwaLine(html, "Material-/Wareneinsatz", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Rohertrag", inPostTaxTotal, gesamtLeistung, gesamtKosten,
						prevInPostTaxTotal, prevGesamtLeistung, prevGesamtKosten, colPositionenWidth, otherColsWidth, "bold");

					appendBwaLine(html, "Sonstige betriebliche Erlöse", 0, gesamtLeistung, gesamtKosten,
						0, prevGesamtLeistung, prevGesamtKosten, colPositionenWidth, otherColsWidth, "");

					appendBwaLine(html, "Betrieblicher Rohertrag", inPostTaxTotal, gesamtLeistung, gesamtKosten,
						prevInPostTaxTotal, prevGesamtLeistung, prevGesamtKosten, colPositionenWidth, otherColsWidth, "bold topborder bottomborder");





					html.append("<br><br><br><br><br><br>");






					html.append("<div class='bold entry' style='padding-top:25pt;'>Betriebsausgaben</div>");

					TimeSpan timeSpan = tsTab.getTimeSpan();

					int externalSalary = timeSpan.getOutTotalBeforeTax(Category.EXTERNAL_SALARY);
					int internalSalary = timeSpan.getOutTotalBeforeTax(Category.INTERNAL_SALARY);
					int vehicleCosts = timeSpan.getOutTotalBeforeTax(Category.VEHICLE);
					int travelCosts = timeSpan.getOutTotalBeforeTax(Category.TRAVEL);
					int locationCosts = timeSpan.getOutTotalBeforeTax(Category.LOCATIONS);
					int educationCosts = timeSpan.getOutTotalBeforeTax(Category.EDUCATION);
					int advertisementCosts = timeSpan.getOutTotalBeforeTax(Category.ADVERTISEMENTS);
					int infrastructureCosts = timeSpan.getOutTotalBeforeTax(Category.INFRASTRUCTURE);
					int entertainmentCosts = timeSpan.getOutTotalBeforeTax(Category.ENTERTAINMENT);
					int wareCosts = timeSpan.getOutTotalBeforeTax(Category.WARES);

					// this does NOT include donations, as we will not get donation amounts from timeSpan.getOutTotalBeforeTax() anyway, so we do not want to subtract them from it!
					// (in general, this is only the sum of non-special categories except other)
					int categoryTally = externalSalary + internalSalary + vehicleCosts + travelCosts + locationCosts +
						educationCosts + advertisementCosts + infrastructureCosts + entertainmentCosts + wareCosts;

					int otherCosts = timeSpan.getOutTotalBeforeTax() - categoryTally;

					if (wareCosts + otherCosts > 0) {
						html.append("<div class='entry sub'>Waren, Rohstoffe und Hilfsmittel:<span class='right'>" +
							FinanceUtils.formatMoneyDE(wareCosts + otherCosts) + " €</span></div>");
					}

					if (externalSalary > 0) {
						html.append("<div class='entry sub'>Fremdleistungen und Auftragsarbeiten:<span class='right'>" +
							FinanceUtils.formatMoneyDE(externalSalary) + " €</span></div>");
					}

					if (internalSalary > 0) {
						html.append("<div class='entry sub'>Personalkosten:<span class='right'>" +
							FinanceUtils.formatMoneyDE(internalSalary) + " €</span></div>");
					}

					if (vehicleCosts > 0) {
						html.append("<div class='entry sub'>Fahrzeugkosten und andere Fahrtkosten:</div>");

						html.append("<div class='entry subsub'>Leasingkosten:<span class='rightish'>" +
							FinanceUtils.formatMoneyDE(vehicleCosts) + " €</span></div>");

						html.append("<div class='entry subsub'>Sonstige tatsächliche Fahrtkosten:<span class='rightish'>" +
							FinanceUtils.formatMoneyDE(travelCosts) + " €</span></div>");

						html.append("<div class='entry sub'>Summe Fahrzeugkosten und andere Fahrtkosten:<span class='right'>" +
							FinanceUtils.formatMoneyDE(vehicleCosts + travelCosts) + " €</span></div>");
					} else {
						if (travelCosts > 0) {
							html.append("<div class='entry sub'>Fahrtkosten:<span class='right'>" +
								FinanceUtils.formatMoneyDE(travelCosts) + " €</span></div>");
						}
					}

					if (locationCosts > 0) {
						html.append("<div class='entry sub'>Raumkosten und sonstige Grundstücksaufwendungen:<span class='right'>" +
							FinanceUtils.formatMoneyDE(locationCosts) + " €</span></div>");
					}

					html.append("<div class='entry sub'>Sonstige Betriebsausgaben:</div>");

					if (educationCosts > 0) {
						html.append("<div class='entry subsub'>Fortbildungskosten:<span class='rightish'>" +
							FinanceUtils.formatMoneyDE(educationCosts) + " €</span></div>");
					}

					html.append("<div class='entry subsub'>Laufende EDV-Kosten:<span class='rightish'>" +
						FinanceUtils.formatMoneyDE(infrastructureCosts) + " €</span></div>");

					if (advertisementCosts + entertainmentCosts > 0) {
						html.append("<div class='entry subsub'>Werbekosten:<span class='rightish'>" +
							FinanceUtils.formatMoneyDE(advertisementCosts + entertainmentCosts) + " €</span></div>");
					}

					html.append("<div class='entry sub'>Summe sonstige Betriebsausgaben:<span class='right'>" +
						FinanceUtils.formatMoneyDE(educationCosts + infrastructureCosts + advertisementCosts + entertainmentCosts) + " €</span></div>");

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
						FinanceUtils.formatMoneyDE(inPostTaxTotal) + " €</span></div>");

					html.append("<div class='entry sub'>Abzüglich Summe der Betriebsausgaben:<span class='right'>" +
						FinanceUtils.formatMoneyDE(outTotal) + " €</span></div>");

					html.append("<div class='entry bold'>Gewinn:<span class='right bold'>" +
						FinanceUtils.formatMoneyDE(inPostTaxTotal - outTotal) + " €</span></div>");

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
					html.append("<head>");
					html.append("<meta charset='utf-8'>");
					html.append("<style>");
					html.append("div.entry {");
					html.append("  padding-bottom: 8pt;");
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
					html.append("</style>");
					html.append("</head>");
					html.append("<body>");

					String title = tab.toString();
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

					int externalSalary = timeSpan.getOutTotalBeforeTax(Category.EXTERNAL_SALARY);
					int internalSalary = timeSpan.getOutTotalBeforeTax(Category.INTERNAL_SALARY);
					int vehicleCosts = timeSpan.getOutTotalBeforeTax(Category.VEHICLE);
					int travelCosts = timeSpan.getOutTotalBeforeTax(Category.TRAVEL);
					int locationCosts = timeSpan.getOutTotalBeforeTax(Category.LOCATIONS);
					int educationCosts = timeSpan.getOutTotalBeforeTax(Category.EDUCATION);
					int advertisementCosts = timeSpan.getOutTotalBeforeTax(Category.ADVERTISEMENTS);
					int infrastructureCosts = timeSpan.getOutTotalBeforeTax(Category.INFRASTRUCTURE);
					int entertainmentCosts = timeSpan.getOutTotalBeforeTax(Category.ENTERTAINMENT);
					int wareCosts = timeSpan.getOutTotalBeforeTax(Category.WARES);

					// this does NOT include donations, as we will not get donation amounts from timeSpan.getOutTotalBeforeTax() anyway, so we do not want to subtract them from it!
					// (in general, this is only the sum of non-special categories except other)
					int categoryTally = externalSalary + internalSalary + vehicleCosts + travelCosts + locationCosts +
						educationCosts + advertisementCosts + infrastructureCosts + entertainmentCosts + wareCosts;

					int otherCosts = timeSpan.getOutTotalBeforeTax() - categoryTally;

					if (wareCosts + otherCosts > 0) {
						html.append("<div class='entry sub'>Waren, Rohstoffe und Hilfsmittel:<span class='right'>" +
							FinanceUtils.formatMoneyDE(wareCosts + otherCosts) + " €</span></div>");
					}

					if (externalSalary > 0) {
						html.append("<div class='entry sub'>Fremdleistungen und Auftragsarbeiten:<span class='right'>" +
							FinanceUtils.formatMoneyDE(externalSalary) + " €</span></div>");
					}

					if (internalSalary > 0) {
						html.append("<div class='entry sub'>Personalkosten:<span class='right'>" +
							FinanceUtils.formatMoneyDE(internalSalary) + " €</span></div>");
					}

					if (vehicleCosts > 0) {
						html.append("<div class='entry sub'>Fahrzeugkosten und andere Fahrtkosten:</div>");

						html.append("<div class='entry subsub'>Leasingkosten:<span class='rightish'>" +
							FinanceUtils.formatMoneyDE(vehicleCosts) + " €</span></div>");

						html.append("<div class='entry subsub'>Sonstige tatsächliche Fahrtkosten:<span class='rightish'>" +
							FinanceUtils.formatMoneyDE(travelCosts) + " €</span></div>");

						html.append("<div class='entry sub'>Summe Fahrzeugkosten und andere Fahrtkosten:<span class='right'>" +
							FinanceUtils.formatMoneyDE(vehicleCosts + travelCosts) + " €</span></div>");
					} else {
						if (travelCosts > 0) {
							html.append("<div class='entry sub'>Fahrtkosten:<span class='right'>" +
								FinanceUtils.formatMoneyDE(travelCosts) + " €</span></div>");
						}
					}

					if (locationCosts > 0) {
						html.append("<div class='entry sub'>Raumkosten und sonstige Grundstücksaufwendungen:<span class='right'>" +
							FinanceUtils.formatMoneyDE(locationCosts) + " €</span></div>");
					}

					html.append("<div class='entry sub'>Sonstige Betriebsausgaben:</div>");

					if (educationCosts > 0) {
						html.append("<div class='entry subsub'>Fortbildungskosten:<span class='rightish'>" +
							FinanceUtils.formatMoneyDE(educationCosts) + " €</span></div>");
					}

					html.append("<div class='entry subsub'>Laufende EDV-Kosten:<span class='rightish'>" +
						FinanceUtils.formatMoneyDE(infrastructureCosts) + " €</span></div>");

					if (advertisementCosts + entertainmentCosts > 0) {
						html.append("<div class='entry subsub'>Werbekosten:<span class='rightish'>" +
							FinanceUtils.formatMoneyDE(advertisementCosts + entertainmentCosts) + " €</span></div>");
					}

					html.append("<div class='entry sub'>Summe sonstige Betriebsausgaben:<span class='right'>" +
						FinanceUtils.formatMoneyDE(educationCosts + infrastructureCosts + advertisementCosts + entertainmentCosts) + " €</span></div>");

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

				indexContent = StrUtils.replaceAll(indexContent, "[[TABS]]", tabsHtml);

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

	private void appendBwaLine(StringBuilder html, String positionName, int value, int gesamtLeistung, int gesamtKosten,
		int prevValue, int prevGesamtLeistung, int prevGesamtKosten, double colPositionenWidth, double otherColsWidth,
		String divClass) {

		html.append("<div class='entry " + divClass + "'>");
		html.append("<span style='width: " + colPositionenWidth + "%; display: inline-block;'>");
		html.append(positionName);
		html.append("</span>");
		html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
		html.append(FinanceUtils.formatMoneyDE(value));
		html.append(" €</span>");
		html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
		if (gesamtLeistung > 0) {
			html.append(StrUtils.replaceAll(StrUtils.leftPadW(StrUtils.doubleToStr((value * 100.0) / gesamtLeistung, 2), 6), " ", "&nbsp;&nbsp;"));
		}
		html.append("</span>");
		html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
		if (gesamtKosten > 0) {
			html.append(StrUtils.replaceAll(StrUtils.leftPadW(StrUtils.doubleToStr((value * 100.0) / gesamtKosten, 2), 6), " ", "&nbsp;&nbsp;"));
		}
		html.append("</span>");
		html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
		html.append(FinanceUtils.formatMoneyDE(prevValue));
		html.append(" €</span>");
		html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
		if (prevGesamtLeistung > 0) {
			html.append(StrUtils.replaceAll(StrUtils.leftPadW(StrUtils.doubleToStr((prevValue * 100.0) / prevGesamtLeistung, 2), 6), " ", "&nbsp;&nbsp;"));
		}
		html.append("</span>");
		html.append("<span style='text-align: center; width: " + otherColsWidth + "%; display: inline-block;'>");
		if (prevGesamtKosten > 0) {
			html.append(StrUtils.replaceAll(StrUtils.leftPadW(StrUtils.doubleToStr((prevValue * 100.0) / prevGesamtKosten, 2), 6), " ", "&nbsp;&nbsp;"));
		}
		html.append("</span>");
		html.append("</div>");
	}

}
