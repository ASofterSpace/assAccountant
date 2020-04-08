/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.toolbox.gui.Arrangement;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class MonthTab extends TimeSpanTab {

	private Month month;

	private JPanel tab;


	public MonthTab(Month month) {
		this.month = month;
	}

	@Override
	public void createTabOnGUI(JPanel parentPanel, Database database) {

		if (tab != null) {
			destroyTabOnGUI(parentPanel);
		}

		int i = 0;

		tab = new JPanel();
		tab.setLayout(new GridBagLayout());

		JPanel topHUD = new JPanel();
		topHUD.setLayout(new GridBagLayout());

		JLabel nameLabel = new JLabel(month.toString());
		nameLabel.setFont(new Font("Calibri", Font.PLAIN, 24));
		nameLabel.setPreferredSize(new Dimension(0, nameLabel.getPreferredSize().height*2));
		nameLabel.setHorizontalAlignment(JLabel.CENTER);
		topHUD.add(nameLabel, new Arrangement(0, 0, 1.0, 1.0));

		tab.add(topHUD, new Arrangement(0, i, 1.0, 0.0));
		i++;


		JLabel outgoingLabel = new JLabel("Outgoing Invoices - that is, we get paid:");
		outgoingLabel.setFont(new Font("Calibri", Font.PLAIN, 20));
		outgoingLabel.setPreferredSize(new Dimension(0, outgoingLabel.getPreferredSize().height*2));
		outgoingLabel.setHorizontalAlignment(JLabel.CENTER);
		tab.add(outgoingLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		JPanel curPanel;
		int totalBeforeTax = 0;
		int totalTax = 0;
		int totalAfterTax = 0;

		List<Outgoing> outgoings = month.getOutgoings();
		for (Outgoing cur : outgoings) {
			curPanel = cur.createPanelOnGUI(database);
			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;

			totalBeforeTax += cur.getAmount();
			totalTax += cur.getPostTaxAmount() - cur.getAmount();
			totalAfterTax += cur.getPostTaxAmount();
		}

		curPanel = AccountingUtils.createTotalPanelOnGUI(totalBeforeTax, totalTax, totalAfterTax);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;


		JLabel incomingLabel = new JLabel("Incoming Invoices - that is, we have to pay:");
		incomingLabel.setFont(new Font("Calibri", Font.PLAIN, 20));
		incomingLabel.setPreferredSize(new Dimension(0, incomingLabel.getPreferredSize().height*2));
		incomingLabel.setHorizontalAlignment(JLabel.CENTER);
		tab.add(incomingLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		totalBeforeTax = 0;
		totalTax = 0;
		totalAfterTax = 0;

		List<Incoming> incomings = month.getIncomings();
		for (Incoming cur : incomings) {
			curPanel = cur.createPanelOnGUI(database);
			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;

			totalBeforeTax += cur.getAmount();
			totalTax += cur.getPostTaxAmount() - cur.getAmount();
			totalAfterTax += cur.getPostTaxAmount();
		}

		curPanel = AccountingUtils.createTotalPanelOnGUI(totalBeforeTax, totalTax, totalAfterTax);
		tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;


		JPanel footer = new JPanel();
		tab.add(footer, new Arrangement(0, i, 1.0, 1.0));

		Dimension newSize = new Dimension(parentPanel.getWidth(), tab.getMinimumSize().height + 100);
		tab.setPreferredSize(newSize);
		parentPanel.setPreferredSize(newSize);

		parentPanel.add(tab);
	}

	@Override
	public void destroyTabOnGUI(JPanel parentPanel) {
		if (tab != null) {
			parentPanel.remove(tab);
		}
	}

	@Override
	public int compareTo(Tab tab) {
		if (tab == null) {
			return -1;
		}
		if (tab instanceof OverviewTab) {
			return 1;
		}
		if (tab instanceof MonthTab) {
			int result = ((MonthTab) tab).getYear().getNum() - getYear().getNum();
			if (result == 0) {
				return ((MonthTab) tab).getMonth().getNum() - month.getNum();
			}
			return result;
		}
		if (tab instanceof YearTab) {
			int result = ((YearTab) tab).getYear().getNum() - getYear().getNum();
			if (result == 0) {
				return 1;
			}
			return result;
		}
		return -1;
	}

	public Month getMonth() {
		return month;
	}

	@Override
	public Year getYear() {
		return month.getYear();
	}

	@Override
	public String toString() {
		return month.toString();
	}

}
