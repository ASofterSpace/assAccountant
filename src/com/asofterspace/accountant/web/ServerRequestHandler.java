/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.web;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.TabCtrl;
import com.asofterspace.accountant.tabs.BankStatementTab;
import com.asofterspace.accountant.tabs.BankStatementYearTab;
import com.asofterspace.accountant.tabs.MonthTab;
import com.asofterspace.accountant.tabs.Tab;
import com.asofterspace.accountant.tabs.YearTab;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.io.TextFile;
import com.asofterspace.toolbox.utils.StrUtils;
import com.asofterspace.toolbox.web.WebServer;
import com.asofterspace.toolbox.web.WebServerAnswer;
import com.asofterspace.toolbox.web.WebServerAnswerInJson;
import com.asofterspace.toolbox.web.WebServerRequestHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.List;


public class ServerRequestHandler extends WebServerRequestHandler {

	public final static String MARI_DATABASE_FILE = "../assAccountant/config/database.cnf";

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

				case "/example":
					answer = new WebServerAnswerInJson(new JSON("{\"foo\": \"bar\"}"));
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
	protected WebServerAnswer answerGet(String location, String[] arguments) {

		return null;
	}

	@Override
	protected String getWhitelistedLocationEquivalent(String location) {

		String result = super.getWhitelistedLocationEquivalent(location);

		if (result == null) {
			if (locationToTabKind(location) != null) {
				return location;
			}
		}

		return result;
	}

	@Override
	protected File getFileFromLocation(String location, String[] arguments) {

		// get project logo files from assWorkbench
		if (location.startsWith("/projectlogos/") && location.endsWith(".png") && !location.contains("..")) {
			location = location.substring("/projectlogos/".length());
			location = System.getProperty("java.class.path") + "/../../assWorkbench/server/projects/" + location;
			File result = new File(location);
			if (result.exists()) {
				return result;
			}
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

				TextFile indexBaseFile = new TextFile(webRoot, "index.htm");
				String indexContent = indexBaseFile.getContent();

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

				// remove the "Overview" title when directing to index.htm from the outside
				if ("overview".equals(tabKind) && locEquiv.contains("index")) {
					mainContent = mainContent.substring(mainContent.indexOf("</div>") + 6);
				}

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
			"bank_statements".equals(locEquiv) ||
			locEquiv.startsWith("year_") ||
			locEquiv.startsWith("month_") ||
			locEquiv.startsWith("bs_year_")) {
			return locEquiv;
		}

		return null;
	}

	private static String tabToLink(Tab tab) {
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

}
