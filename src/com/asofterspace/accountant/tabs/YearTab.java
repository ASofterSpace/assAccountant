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
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;


public class YearTab extends TimeSpanTab {

	private Year year;

	private JPanel tab;


	public YearTab(Year year) {
		this.year = year;
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

		CopyByClickLabel nameLabel = new CopyByClickLabel(year.toString() + " (yearly overview)");
		nameLabel.setFont(new Font("Calibri", Font.PLAIN, 24));
		nameLabel.setPreferredSize(new Dimension(0, nameLabel.getPreferredSize().height*2));
		nameLabel.setHorizontalAlignment(CopyByClickLabel.CENTER);
		topHUD.add(nameLabel, new Arrangement(0, 0, 1.0, 1.0));

		tab.add(topHUD, new Arrangement(0, i, 1.0, 0.0));
		i++;


		CopyByClickLabel outgoingLabel = new CopyByClickLabel("Outgoing Invoices - that is, we get paid:");
		outgoingLabel.setFont(new Font("Calibri", Font.PLAIN, 20));
		outgoingLabel.setPreferredSize(new Dimension(0, outgoingLabel.getPreferredSize().height*2));
		outgoingLabel.setHorizontalAlignment(CopyByClickLabel.CENTER);
		tab.add(outgoingLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		JPanel curPanel;
		int totalBeforeTax = 0;
		int totalTax = 0;
		int totalAfterTax = 0;

		List<Outgoing> outgoings = year.getOutgoings();
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


		CopyByClickLabel incomingLabel = new CopyByClickLabel("Incoming Invoices - that is, we have to pay:");
		incomingLabel.setFont(new Font("Calibri", Font.PLAIN, 20));
		incomingLabel.setPreferredSize(new Dimension(0, incomingLabel.getPreferredSize().height*2));
		incomingLabel.setHorizontalAlignment(CopyByClickLabel.CENTER);
		tab.add(incomingLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		totalBeforeTax = 0;
		totalTax = 0;
		totalAfterTax = 0;

		List<Incoming> incomings = year.getIncomings();
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
		if (tab instanceof TimeSpanTab) {
			return ((TimeSpanTab) tab).getYear().getNum() - year.getNum();
		}
		return -1;
	}

	@Override
	public Year getYear() {
		return year;
	}

	@Override
	public String toString() {
		return year.toString();
	}

}
