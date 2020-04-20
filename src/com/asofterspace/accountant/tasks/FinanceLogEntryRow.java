/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.world.Currency;
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

	public JPanel createPanelOnGUI(Database database) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();
		Color textColor = new Color(0, 0, 0);

		JPanel curPanel = new JPanel();
		curPanel.setPreferredSize(defaultDimension);
		curPanel.setBackground(GUI.getBackgroundColor());
		curPanel.setLayout(new GridBagLayout());

		CopyByClickLabel curLabel = AccountingUtils.createLabel(getAccount() + ": ", textColor, "");
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curPanel.add(curLabel, new Arrangement(0, 0, 0.5, 1.0));

		curLabel = AccountingUtils.createLabel(AccountingUtils.formatMoney(getAmount(), Currency.EUR), textColor, "");
		curPanel.add(curLabel, new Arrangement(1, 0, 0.5, 1.0));

		return curPanel;
	}

}
