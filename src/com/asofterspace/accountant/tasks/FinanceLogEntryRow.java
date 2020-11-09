/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tasks;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.accounting.FinanceUtils;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class FinanceLogEntryRow {

	private String account;

	private Integer amount;


	public FinanceLogEntryRow(String account, Integer amount) {
		this.account = account;
		this.amount = amount;
	}

	public String getAccount() {
		return account;
	}

	public Integer getAmount() {
		return amount;
	}

	public String createPanelInHtml(Database database) {

		String html = "<div class='line'>";
		html += AccountingUtils.createLabelHtml(getAccount() + ": ", null, "", "text-align: right; width: 50%;");
		html += AccountingUtils.createLabelHtml(FinanceUtils.formatMoney(getAmount(), Currency.EUR), null, "", "text-align: right; width: 10%;");
		html += "</div>";

		return html;
	}

	public JPanel createPanelOnGUI(Database database) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();

		JPanel curPanel = new JPanel();
		curPanel.setBackground(GUI.getBackgroundColor());
		curPanel.setLayout(new GridBagLayout());

		CopyByClickLabel curLabel = new CopyByClickLabel(getAccount() + ": ");
		curLabel.setHorizontalAlignment(CopyByClickLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(0, 0, 0.5, 1.0));

		curLabel = new CopyByClickLabel(FinanceUtils.formatMoney(getAmount(), Currency.EUR));
		curLabel.setPreferredSize(defaultDimension);
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.1, 1.0));

		curLabel = new CopyByClickLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(2, 0, 0.4, 1.0));

		return curPanel;
	}

}
