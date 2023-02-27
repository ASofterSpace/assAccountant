/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;


public class CalculatorTab extends Tab {

	private static final String TITLE = "Calculator";


	public CalculatorTab() {
	}

	@Override
	public boolean isShownInMenu() {
		return false;
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "";

		html += "<div class='mainTitle'>" + TITLE + "</div>";

		int baseAmount = 30;

		html += "<div style='padding-bottom:10pt;'>I can help you with some calculations. :)</div>";
		html += "<div>For generating skyhook invoices, ";
		html += "it can be helpful to know the multiples of " + baseAmount + " €:</div>";
		html += "<div>";
		for (int m = 1; m <= 8*60; m++) {
			String text = database.formatMoney((baseAmount * 100 * m) / 60) + " €";
			int width = 95 / 5;
			html += "<div style='display: inline-block; width: " + width + "%; cursor: pointer;' ";
			html += "class='line' onclick='accountant.copyText(\"" + text + "\")'>";
			if (m >= 60) {
				html += (m / 60) + "h ";
			}
			html += (m % 60) + "m: ";
			html += text;
			html += "</div>";
		}
		html += "</div>";

		html += "<div class='footer'>&nbsp;</div>";

		return html;
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
