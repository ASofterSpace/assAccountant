/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.Utils;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class AccountingUtils {

	public static String formatMoney(int amount, Currency currency) {

		String result = "" + amount;

		// 1 to 001
		while (result.length() < 3) {
			result = "0" + result;
		}

		// 001 to 0.01
		result = result.substring(0, result.length() - 2) + "." + result.substring(result.length() - 2);

		// 2739.80 to 2,739.80
		if (result.length() > 6) {
			result = result.substring(0, result.length() - 6) + "," + result.substring(result.length() - 6);
		}
		// 2739,800.00 to 2,739,800.00
		if (result.length() > 10) {
			result = result.substring(0, result.length() - 10) + "," + result.substring(result.length() - 10);
		}

		// 0.01 to 0.01 EUR
		return result + " " + currency;
	}

	public static JPanel createTotalPanelOnGUI(int totalBeforeTax, int totalTax, int totalAfterTax) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();

		JPanel curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());

		JLabel curLabel = new JLabel("");
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(0, 0, 0.03, 1.0));

		curLabel = new JLabel("");
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(1, 0, 0.03, 1.0));

		curLabel = new JLabel("Total: ");
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(2, 0, 0.5, 1.0));

		curLabel = new JLabel(AccountingUtils.formatMoney(totalBeforeTax, Currency.EUR));
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(3, 0, 0.1, 1.0));

		curLabel = new JLabel(AccountingUtils.formatMoney(totalTax, Currency.EUR));
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(4, 0, 0.1, 1.0));

		curLabel = new JLabel(AccountingUtils.formatMoney(totalAfterTax, Currency.EUR));
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(5, 0, 0.1, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(6, 0, 0.0, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(7, 0, 0.08, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(8, 0, 0.0, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(9, 0, 0.08, 1.0));

		curLabel = new JLabel("");
		curLabel.setPreferredSize(defaultDimension);
		curPanel.add(curLabel, new Arrangement(10, 0, 0.0, 1.0));

		return curPanel;
	}

	public static boolean complain(String complainAbout) {

		JOptionPane.showMessageDialog(
			null,
			complainAbout,
			Utils.getProgramTitle(),
			JOptionPane.ERROR_MESSAGE
		);

		// we return false, which can then immediately be returned by the caller
		return false;
	}
}
