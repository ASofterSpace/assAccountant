/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.toolbox.accounting.FinanceUtils;
import com.asofterspace.toolbox.gui.Arrangement;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.util.Map;

import javax.swing.JPanel;


public class TaxTab extends Tab {

	private static final String TITLE = "Taxes";

	private JPanel tab;


	public TaxTab() {
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "";

		html += "<div class='mainTitle'>" + TITLE + "</div>";

		html += "<div>Here are the taxes per year:</div>";

		// width of one cell
		int cw = 20;

		html += "<div class='line'>";
		html += "<div>";
		html += "<div style='width: " + cw + "%; display: inline-block; text-align: center;'></div>";
		html += "<div style='width: " + cw + "%; display: inline-block; text-align: center;'>VAT /</div>";
		html += "<div style='width: " + cw + "%; display: inline-block; text-align: center;'>Expected Income Tax /</div>";
		html += "</div>";
		html += "<div>";
		html += "<div style='width: " + cw + "%; display: inline-block; text-align: center;'></div>";
		html += "<div style='width: " + cw + "%; display: inline-block; text-align: center;'>Umsatzsteuer:</div>";
		html += "<div style='width: " + cw + "%; display: inline-block; text-align: center;'>Erwartete Einkommenssteuer:</div>";
		html += "</div>";
		html += "</div>";

		Map<Integer, Integer> incomeTaxes = database.getIncomeTaxes();

		for (Year year : database.getYears()) {
			html += "<div class='line'>";
			html += "<div style='width: " + cw + "%; display: inline-block; text-align: right;'>" + year + "</div>";
			html += "<div style='width: " + cw + "%; display: inline-block; text-align: right;'>" + FinanceUtils.formatMoney(year.getInTotalTax()) + " €</div>";
			html += "<div style='width: " + cw + "%; display: inline-block; text-align: right;'>" + FinanceUtils.formatMoney((int) year.getExpectedIncomeTax()) + " €</div>";
			html += "<div style='width: " + cw + "%; display: inline-block; text-align: right;'>";
			Integer actualIncomeTax = incomeTaxes.get(year.getNum());
			if (actualIncomeTax == null) {
				html += "(unknown)";
			} else {
				html += FinanceUtils.formatMoney(actualIncomeTax) + " €";
			}
			html += "</div>";
			html += "</div>";
		}

		html += "<br>";

		html += "<div>(To enter income tax, edit the key 'incomeTaxes' in the database and add the value of the ESt-Bescheid, including Solidaritätszuschlag.)</div>";

		html += "<div class='footer'>&nbsp;</div>";

		return html;
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
	public void destroyTabOnGUI(JPanel parentPanel) {
		if (tab != null) {
			parentPanel.remove(tab);
		}
	}

	@Override
	public int getComparisonOrder() {
		return (10000 * 100) + 2500;
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
