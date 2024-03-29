/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.ConsistencyProblem;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.PaymentProblem;
import com.asofterspace.accountant.tasks.Task;
import com.asofterspace.toolbox.calendar.GenericTask;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.utils.Record;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;


public class OverviewTab extends Tab {

	private static final String TITLE = "Overview";


	public OverviewTab() {
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "";

		Color textColor = new Color(0, 0, 0);


		html += "<div style='padding-top:10pt; padding-bottom: 10pt;'>Hej " + database.getUsername() + "! :)</div>";


		String simplisticHtml = html;


		html += "<div>Well, fuck, there is stuff to do:</div>";


		List<GenericTask> tasks = database.getTaskCtrl().getTaskInstances();

		boolean tasksShown = false;
		for (GenericTask task : tasks) {
			if ((!task.hasBeenDone()) && task.matches(searchFor)) {
				tasksShown = true;
				break;
			}
		}

		/*
		if (tasksShown) {
			html += "<div class='line'>";
			html += AccountingUtils.createLabelHtml("Scheduled:", textColor, "", "text-align: left; width: 8%;");
			html += AccountingUtils.createLabelHtml("Title:", textColor, "", "text-align: left; width: 50%;");
			html += "</div>";
		}
		*/

		for (GenericTask task : tasks) {
			if ((!task.hasBeenDone()) && task.matches(searchFor)) {
				if (task instanceof Task) {
					html += "<br>";
					html += ((Task) task).createPanelInHtml(database);
				} else {
					// TODO notify this in the HTML somehow
					System.out.println("Expected a task but got " + task + "!");
				}
			}
		}


		// display incoming payments (sent invoices) which have been sent out more than six weeks ago and
		// not yet been set to having come in
		List<PaymentProblem> paymentProblems = database.getPaymentProblems();
		for (final PaymentProblem curProblem : paymentProblems) {

			html += "<br>";
			html += "<div class='line'>";

			html += AccountingUtils.createLabelHtml(curProblem.getProblem(), curProblem.getColor(), curProblem.getProblem(), "text-align: left; width: 80%;");

			html += "<span class='button' style='width:8%; float:right; margin-left:0.3%;' onclick='accountant.paidEntry(\"" + curProblem.getEntry().getId() + "\")'>Paid</span>";

			html += "<span class='button' style='width:8%; float:right; margin-left:0.3%;' onclick='window.location = \"" + curProblem.getEntry().getMonthLink() + "\"'>Show</span>";

			html += "</div>";
		}


		List<ConsistencyProblem> consistencyProblems = database.getUnacknowledgedConsistencyProblems();
		for (final ConsistencyProblem curProblem : consistencyProblems) {

			html += "<br>";
			html += "<div class='line'>";

			html += AccountingUtils.createLabelHtml(curProblem.getProblem(), curProblem.getColor(), curProblem.getProblem(), "text-align: left; width: 80%;");

			html += "<span class='button' style='width:8%; float:right; margin-left:0.3%;' onclick='accountant.editEntry(\"" + curProblem.getEntry().getId() + "\")'>Edit</span>";

			html += "<span class='button' style='width:8%; float:right; margin-left:0.3%;' onclick='window.location = \"" + curProblem.getEntry().getMonthLink() + "\"'>Show</span>";

			// TODO - add working button
			/*
			curButton = new JButton("Acknowledge");
			curButton.addMouseListener(rowHighlighter);
			curButton.setPreferredSize(defaultDimension);
			curPanel.add(curButton, new Arrangement(3, 0, 0.1, 1.0));
			curButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					database.acknowledge(curProblem.getProblem());
				}
			});
			*/

			html += "</div>";
		}


		if ((!tasksShown) && (paymentProblems.size() < 1) && (consistencyProblems.size() < 1)) {
			html = simplisticHtml;
			html += "<div>Nothing to be done, have a chill day!</div>";
		}

		html += "<div style='padding-top: 25pt;'>";

		html += "<div>";
		html += "Here is the monthly finance flow, btw. - we want there to be more on the left than on the right, you know? ;)";
		html += "</div>";

		html += "<div style='padding-top: 15pt;'>";
		int totalIn = 0;
		String LEFT_DIV_HTML = "<div style='display: inline-block; float: left; width: 49%; text-align: right;'>";
		html += LEFT_DIV_HTML;
		List<Record> monthlyIncoming = database.getMonthlyIncoming();
		for (Record rec : monthlyIncoming) {
			html += "<div class='line'>";
			html += rec.getString("title") + ": " + database.formatMoney(rec.getInteger("amount")) + " €";
			totalIn += rec.getInteger("amount");
			html += "</div>";
		}
		html += "</div>";
		int totalOut = 0;
		String RIGHT_DIV_HTML = "<div style='display: inline-block; padding-left: 1%; width: 49%; text-align: left;'>";
		html += RIGHT_DIV_HTML;
		List<Record> monthlyOutgoing = database.getMonthlyOutgoing();
		for (Record rec : monthlyOutgoing) {
			html += "<div class='line'>";
			html += rec.getString("title") + ": " + database.formatMoney(rec.getInteger("amount")) + " €";
			totalOut += rec.getInteger("amount");
			html += "</div>";
		}
		html += "</div>";
		html += "</div>";

		html += "<div>";
		html += LEFT_DIV_HTML;
		html += "<div class='line'>";
		html += "Total: " + database.formatMoney(totalIn) + " €";
		html += "</div>";
		html += "</div>";
		html += RIGHT_DIV_HTML;
		html += "<div class='line'>";
		html += "Total: " + database.formatMoney(totalOut) + " €";
		html += "</div>";
		html += "</div>";
		html += "</div>";

		html += "</div>";

		html += "<div class='footer'>&nbsp;</div>";

		return html;
	}

	@Override
	public void createTabOnGUI(final JPanel parentPanel, final Database database, String searchFor) {

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
		JPanel curPanel;
		Color textColor = new Color(0, 0, 0);


		CopyByClickLabel outstandingTasksLabel = AccountingUtils.createSubHeadLabel("Outstanding Tasks:");
		tab.add(outstandingTasksLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		List<GenericTask> tasks = database.getTaskCtrl().getTaskInstances();

		boolean tasksShown = false;
		for (GenericTask task : tasks) {
			if ((!task.hasBeenDone()) && task.matches(searchFor)) {
				tasksShown = true;
				break;
			}
		}

		if (tasksShown) {
			curPanel = new JPanel();
			curPanel.setBackground(GUI.getBackgroundColor());
			curPanel.setLayout(new GridBagLayout());

			curLabel = AccountingUtils.createLabel("Scheduled:", textColor, "");
			curPanel.add(curLabel, new Arrangement(0, 0, 0.08, 1.0));

			curLabel = AccountingUtils.createLabel("Title:", textColor, "");
			curPanel.add(curLabel, new Arrangement(1, 0, 0.5, 1.0));

			curLabel = AccountingUtils.createLabel("", textColor, "");
			curPanel.add(curLabel, new Arrangement(2, 0, 0.0, 1.0));

			curLabel = AccountingUtils.createLabel("", textColor, "");
			curPanel.add(curLabel, new Arrangement(3, 0, 0.1, 1.0));

			curLabel = AccountingUtils.createLabel("", textColor, "");
			curPanel.add(curLabel, new Arrangement(4, 0, 0.05, 1.0));

			curLabel = AccountingUtils.createLabel("", textColor, "");
			curPanel.add(curLabel, new Arrangement(5, 0, 0.06, 1.0));

			curLabel = AccountingUtils.createLabel("", textColor, "");
			curPanel.add(curLabel, new Arrangement(6, 0, 0.0, 1.0));

			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}

		for (GenericTask task : tasks) {
			if ((!task.hasBeenDone()) && task.matches(searchFor)) {
				if (task instanceof Task) {
					curPanel = ((Task) task).createPanelOnGUI(database, tab, parentPanel);
					curPanel.setBackground(GUI.getBackgroundColor());
					tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
					i++;
				} else {
					System.out.println("Expected a task but got " + task + "!");
				}
			}
		}
		// for-else:
		if (!tasksShown) {
			curLabel = new CopyByClickLabel("No outstanding tasks have been found!");
			tab.add(curLabel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}


		CopyByClickLabel unpaidInvoicesLabel = AccountingUtils.createSubHeadLabel("Unpaid Sent Invoices:");
		tab.add(unpaidInvoicesLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		// display incoming payments (sent invoices) which have been sent out more than six weeks ago and
		// not yet been set to having come in
		List<PaymentProblem> paymentProblems = database.getPaymentProblems();
		for (final PaymentProblem curProblem : paymentProblems) {

			curPanel = new JPanel();
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

			JButton curButton = new JButton("Show");
			curButton.setPreferredSize(defaultDimension);
			curPanel.add(curButton, new Arrangement(2, 0, 0.1, 1.0));
			curButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					database.getGUI().showMonthTabForEntry(curProblem.getEntry());
				}
			});

			curLabel = new CopyByClickLabel("");
			curLabel.setPreferredSize(defaultDimension);
			curPanel.add(curLabel, new Arrangement(3, 0, 0.0, 1.0));

			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}
		// for-else:
		if (paymentProblems.size() < 1) {
			curLabel = new CopyByClickLabel("No problems have been found!");
			tab.add(curLabel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}


		CopyByClickLabel consistencyChecksLabel = AccountingUtils.createSubHeadLabel("Consistency Checks:");
		tab.add(consistencyChecksLabel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		List<ConsistencyProblem> consistencyProblems = database.getUnacknowledgedConsistencyProblems();
		for (final ConsistencyProblem curProblem : consistencyProblems) {

			curPanel = new JPanel();
			MouseAdapter rowHighlighter = AccountingUtils.getRowHighlighter(curPanel);
			curPanel.setBackground(GUI.getBackgroundColor());
			curPanel.setLayout(new GridBagLayout());

			curLabel = new CopyByClickLabel(curProblem.getProblem());
			curLabel.addMouseListener(rowHighlighter);
			if (curProblem.isImportant()) {
				curLabel.setForeground(new Color(196, 0, 0));
			} else {
				curLabel.setForeground(new Color(148, 148, 0));
			}
			curLabel.setPreferredSize(defaultDimension);
			curPanel.add(curLabel, new Arrangement(0, 0, 0.8, 1.0));

			JButton curButton = new JButton("Show");
			curButton.addMouseListener(rowHighlighter);
			curButton.setPreferredSize(defaultDimension);
			curPanel.add(curButton, new Arrangement(2, 0, 0.08, 1.0));
			curButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					database.getGUI().showMonthTabForEntry(curProblem.getEntry());
				}
			});

			curButton = new JButton("Acknowledge");
			curButton.addMouseListener(rowHighlighter);
			curButton.setPreferredSize(defaultDimension);
			curPanel.add(curButton, new Arrangement(3, 0, 0.1, 1.0));
			curButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					database.acknowledge(curProblem.getProblem());
				}
			});

			curLabel = new CopyByClickLabel("");
			curLabel.addMouseListener(rowHighlighter);
			curLabel.setPreferredSize(defaultDimension);
			curPanel.add(curLabel, new Arrangement(4, 0, 0.0, 1.0));

			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}
		// for-else:
		if (consistencyProblems.size() < 1) {
			curLabel = new CopyByClickLabel("No unacknowledged problems have been found!");
			tab.add(curLabel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}


		JPanel footer = new JPanel();
		footer.setBackground(GUI.getBackgroundColor());
		tab.add(footer, new Arrangement(0, i, 1.0, 1.0));
		i++;

		AccountingUtils.resetTabSize(tab, parentPanel);

		parentPanel.add(tab);
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
