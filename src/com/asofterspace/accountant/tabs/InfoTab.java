/**
 * Unlicensed code created by A Softer Space, 2021
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;


public class InfoTab extends Tab {

	private static final String TITLE = "Account Information";


	public InfoTab() {
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "";

		html += "<div class='mainTitle'>" + TITLE + "</div>";

		html += "<div>" + database.getInfo() + "</div>";

		html += "<div class='footer'>&nbsp;</div>";

		return html;
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
