/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.AddEntryGUI;
import com.asofterspace.accountant.AddPaidGUI;
import com.asofterspace.accountant.ConsistencyProblem;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.PaymentProblem;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;


public class OverviewTab extends Tab {

	private static final String TITLE = "Overview";

	private JPanel tab;


	public OverviewTab() {
	}

	@Override
	public void createTabOnGUI(final JPanel parentPanel, final Database database) {

		if (tab != null) {
			destroyTabOnGUI(parentPanel);
		}

		int i = 0;

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();

		tab = new JPanel();
		tab.setBackground(GUI.getBackgroundColor());
		tab.setLayout(new GridBagLayout());

		JPanel topHUD = new JPanel();
		topHUD.setBackground(GUI.getBackgroundColor());
		topHUD.setLayout(new GridBagLayout());

		CopyByClickLabel nameLabel = AccountingUtils.createHeadLabel(TITLE);
		topHUD.add(nameLabel, new Arrangement(0, 0, 1.0, 1.0));

		tab.add(topHUD, new Arrangement(0, i, 1.0, 0.0));
		i++;

		CopyByClickLabel curLabel;


		CopyByClickLabel outstandingTasksLabel = AccountingUtils.createSubHeadLabel("Outstanding Tasks:");
		tab.add(outstandingTasksLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		curLabel = new CopyByClickLabel("TODO!");
		tab.add(curLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;


		CopyByClickLabel unpaidInvoicesLabel = AccountingUtils.createSubHeadLabel("Unpaid Invoices:");
		tab.add(unpaidInvoicesLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		// display outgoing invoices which have been sent out more than six weeks ago and not yet
		// been set to having come in
		List<PaymentProblem> paymentProblems = database.getPaymentProblems();
		for (final PaymentProblem curProblem : paymentProblems) {

			JPanel curPanel = new JPanel();
			curPanel.setBackground(GUI.getBackgroundColor());
			curPanel.setLayout(new GridBagLayout());

			curLabel = new CopyByClickLabel(curProblem.getProblem());
			if (curProblem.isImportant()) {
				curLabel.setForeground(new Color(196, 0, 0));
			} else {
				curLabel.setForeground(new Color(148, 148, 0));
			}
			curLabel.setPreferredSize(defaultDimension);
			curPanel.add(curLabel, new Arrangement(0, 0, 0.8, 1.0));

			JButton curButton = new JButton("Paid");
			curButton.setPreferredSize(defaultDimension);
			curPanel.add(curButton, new Arrangement(1, 0, 0.1, 1.0));
			curButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AddPaidGUI addPaidGUI = new AddPaidGUI(database.getGUI(), database, curProblem.getEntry());
					addPaidGUI.show();
				}
			});

			curLabel = new CopyByClickLabel("");
			curLabel.setPreferredSize(defaultDimension);
			curPanel.add(curLabel, new Arrangement(2, 0, 0.0, 1.0));

			curButton = new JButton("Show");
			curButton.setPreferredSize(defaultDimension);
			curPanel.add(curButton, new Arrangement(3, 0, 0.1, 1.0));
			curButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					database.getGUI().showMonthTabForEntry(curProblem.getEntry());
				}
			});

			curLabel = new CopyByClickLabel("");
			curLabel.setPreferredSize(defaultDimension);
			curPanel.add(curLabel, new Arrangement(4, 0, 0.0, 1.0));

			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}
		// for-else:
		if (paymentProblems.size() < 1) {
			curLabel = new CopyByClickLabel("No problems have been found!");
			tab.add(curLabel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}


		// TODO - display information such as finance days that have last been done (or maybe
		// even get away from real "finance days", and instead have continuous financing going on,
		// this here always showing what is now left to do?)


		CopyByClickLabel consistencyChecksLabel = AccountingUtils.createSubHeadLabel("Consistency Checks:");
		tab.add(consistencyChecksLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		List<ConsistencyProblem> consistencyProblems = database.getConsistencyProblems();
		for (final ConsistencyProblem curProblem : consistencyProblems) {

			JPanel curPanel = new JPanel();
			curPanel.setBackground(GUI.getBackgroundColor());
			curPanel.setLayout(new GridBagLayout());

			curLabel = new CopyByClickLabel(curProblem.getProblem());
			if (curProblem.isImportant()) {
				curLabel.setForeground(new Color(196, 0, 0));
			} else {
				curLabel.setForeground(new Color(148, 148, 0));
			}
			curLabel.setPreferredSize(defaultDimension);
			curPanel.add(curLabel, new Arrangement(0, 0, 0.8, 1.0));

			JButton curButton = new JButton("Edit");
			curButton.setPreferredSize(defaultDimension);
			curPanel.add(curButton, new Arrangement(1, 0, 0.1, 1.0));
			curButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AddEntryGUI addEntryGUI = new AddEntryGUI(database.getGUI(), database, curProblem.getEntry());
					addEntryGUI.show();
				}
			});

			curLabel = new CopyByClickLabel("");
			curLabel.setPreferredSize(defaultDimension);
			curPanel.add(curLabel, new Arrangement(2, 0, 0.0, 1.0));

			curButton = new JButton("Show");
			curButton.setPreferredSize(defaultDimension);
			curPanel.add(curButton, new Arrangement(3, 0, 0.1, 1.0));
			curButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					database.getGUI().showMonthTabForEntry(curProblem.getEntry());
				}
			});

			curLabel = new CopyByClickLabel("");
			curLabel.setPreferredSize(defaultDimension);
			curPanel.add(curLabel, new Arrangement(4, 0, 0.0, 1.0));

			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}
		// for-else:
		if (consistencyProblems.size() < 1) {
			curLabel = new CopyByClickLabel("No problems have been found!");
			tab.add(curLabel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}


		JPanel footer = new JPanel();
		footer.setBackground(GUI.getBackgroundColor());
		tab.add(footer, new Arrangement(0, i, 1.0, 1.0));
		i++;

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
			return 1;
		}
		if (tab instanceof OverviewTab) {
			return 0;
		}
		return -1;
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
