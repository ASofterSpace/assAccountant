/**
 * Unlicensed code created by A Softer Space, 2021
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.loans.Loan;
import com.asofterspace.accountant.loans.Payment;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.utils.DateUtils;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;


public class LoanTab extends Tab {

	private static final String TITLE = "Loans";


	public LoanTab() {
	}

	@Override
	public boolean isShownInMenu() {
		return true;
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		StringBuilder html = new StringBuilder();

		html.append("<div class='mainTitle'>" + TITLE + "</div>");

		List<Loan> loans = database.getLoans();

		int loanAmountActive = 0;
		int loanAmountInactive = 0;
		for (Loan loan : loans) {
			if (loan.getActive()) {
				loanAmountActive++;
			} else {
				loanAmountInactive++;
			}
		}
		int loanAmountSum = loanAmountActive + loanAmountInactive;
		html.append("<div>We have " + loanAmountSum + " loan" + (loanAmountSum == 1 ? "" : "s") +
			" in total (" + loanAmountActive + " active loan" + (loanAmountActive == 1 ? "" : "s") + ", " +
			loanAmountInactive + " inactive loan" + (loanAmountInactive == 1 ? "" : "s") + ")" +
			(loanAmountSum == 0 ? "." : ":") + "</div>");

		for (Loan loan : loans) {
			String extraStyle = "";
			if (!loan.getActive()) {
				extraStyle = " text-decoration: line-through; opacity: 0.6;";
			}
			html.append("<div class='line' style='margin-top: 15pt; font-weight: bold;" + extraStyle + "'>");
			html.append("<i>" + loan.getKindDisplayStr() + "</i> " + loan.getName());
			html.append(" (" + database.formatMoney(loan.getAmount()) + " €)");
			html.append("</div>");
			if (loan.getDetails() != null) {
				html.append("<div class='line' style='font-style: italic;" + extraStyle + "'>");
				html.append(loan.getDetails());
				html.append("</div>");
			}

			int amountPaid = 0;

			for (Payment payment : loan.getPayments()) {
				html.append("<div class='line' style='padding-left: 9pt;" + extraStyle + "'>");
				html.append(DateUtils.serializeDate(payment.getDate()));
				html.append(" ");
				html.append(database.formatMoney(payment.getAmount()) + " €");
				String comment = payment.getComment();
				if (comment != null) {
					html.append(" <i>" + comment + "</i>");
				}
				html.append("</div>");

				amountPaid += payment.getAmount();
			}

			html.append("<div class='line' style='" + extraStyle + "'>");
			html.append("Total paid so far: ");
			html.append(database.formatMoney(amountPaid));
			html.append(" € (");
			html.append(database.formatMoney(loan.getAmount() - amountPaid));
			html.append(" € left)");
			html.append("</div>");
		}

		html.append("<div class='footer'>&nbsp;</div>");

		return html.toString();
	}

	@Override
	public void createTabOnGUI(final JPanel parentPanel, final Database database, String searchFor) {

		tab = new JPanel();
		tab.setBackground(GUI.getBackgroundColor());
		tab.setLayout(new GridBagLayout());

		JPanel footer = new JPanel();
		footer.setBackground(GUI.getBackgroundColor());
		tab.add(footer, new Arrangement(0, 1, 1.0, 1.0));

		AccountingUtils.resetTabSize(tab, parentPanel);

		parentPanel.add(tab);
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
