/**
 * Unlicensed code created by A Softer Space, 2021
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.loans.Payment;
import com.asofterspace.accountant.rent.RentData;
import com.asofterspace.accountant.rent.RentMonth;
import com.asofterspace.accountant.rent.RentPayment;
import com.asofterspace.toolbox.utils.DateUtils;

import java.util.List;


public class RentTab extends Tab {

	private static final String TITLE = "Rent";


	public RentTab() {
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		StringBuilder html = new StringBuilder();

		html.append("<div class='mainTitle'>" + TITLE + " Payments</div>");

		RentData rentData = database.getRentData();

		List<RentPayment> targetPayments = rentData.getTarget();

		int amount = targetPayments.size();
		html.append("<div>" + amount + " payment" + (amount == 1 ? " is" : "s are") + " expected each month" + (amount == 0 ? "." : ":") + "</div>");

		for (RentPayment payment : targetPayments) {
			html.append("<div class='line' style='font-weight: bold;'>");
			html.append(payment.getWho() + ": " + database.formatMoney(payment.getAmount()) + " €");
			html.append("</div>");
		}

		html.append("<div style='margin-top: 15pt;'>These payments have been made in the past:</div>");

		List<RentMonth> months = rentData.getMonths();

		for (RentMonth month : months) {
			String color = "";
			if (month.getVerifiedOkay()) {
				color = "color: #20CC00;";
			}

			html.append("<div class='line' style='margin-top: 15pt; font-weight: bold;" + color + "'>");
			html.append(DateUtils.getMonthNameEN(month.getDate()) + " " + DateUtils.getYear(month.getDate()));
			html.append("</div>");

			for (RentPayment payment : month.getPayments()) {
				html.append("<div class='line' style='" + color + "'>");
				html.append(payment.getWho() + ": " + database.formatMoney(payment.getAmount()) + " € on " + DateUtils.serializeDate(payment.getDate()));
				html.append("</div>");
			}
		}


		html.append("<div class='footer'>&nbsp;</div>");

		return html.toString();
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
