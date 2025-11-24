/**
 * Unlicensed code created by A Softer Space, 2021
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AssAccountant;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.web.ServerRequestHandler;

import java.util.List;


public class OperationsTab extends Tab {

	private static final String TITLE = "Operations";


	public OperationsTab() {
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "";

		html += "<div class='mainTitle'>" + TITLE + "</div>";

		html += "<div>";
		html += "Navigate to hidden tabs:";
		html += "</div>";

		List<Tab> tabs = AssAccountant.getTabCtrl().getTabs();

		for (Tab tab : tabs) {
			if (tab.isShownInMenu()) {
				continue;
			}
			html += "<div>";
			html += "<a style='color:#88AAFF;' href='";
			html += ServerRequestHandler.tabToLink(tab);
			html += "'>" + tab.toString() + "</a>";
			html += "</div>";
		}

		html += "<div>";
		html += "&nbsp;";
		html += "</div>";

		html += "<div style='margin-bottom: 6pt;'>";
		html += "Perform backend operations:";
		html += "</div>";

		html += "<div style='margin-bottom: 6pt;'>";
		html += "<span class='button' onclick='accountant.save()'>";
		html += "Save";
		html += "</span>";
		html += "&nbsp;&nbsp;&nbsp;";
		html += "<span class='button' onclick='accountant.generalUndo()'>";
		html += "General Undo";
		html += "</span>";
		html += "&nbsp;&nbsp;&nbsp;";
		html += "<span class='button' onclick='accountant.generalRedo()'>";
		html += "General Redo";
		html += "</span>";
		html += "</div>";

		html += "<div style='margin-bottom: 6pt;'>";
		html += "<span class='button' onclick='accountant.gulpBankStatements()'>";
		html += "Gulp Bank Statements";
		html += "</span>";
		html += "</div>";

		html += "<div style='margin-bottom: 6pt;'>";
		html += "<span class='button' onclick='accountant.showGUI()'>";
		html += "Show Legacy GUI";
		html += "</span>";
		html += "</div>";

		html += "<div class='footer'>&nbsp;</div>";

		return html;
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
