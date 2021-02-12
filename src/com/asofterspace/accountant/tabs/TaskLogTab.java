/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.tasks.Task;
import com.asofterspace.toolbox.calendar.GenericTask;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;


public class TaskLogTab extends Tab {

	private static final String TITLE = "Task Log";

	private JPanel tab;


	public TaskLogTab() {
	}

	@Override
	public String getHtmlGUI(Database database, String searchFor) {

		String html = "<div class='mainTitle'>" + TITLE + "</div>";

		Color textColor = new Color(0, 0, 0);


		List<GenericTask> tasks = database.getTaskCtrl().getTaskInstances();

		boolean tasksShown = false;
		for (GenericTask task : tasks) {
			if (task.hasBeenDone() && task.matches(searchFor)) {
				tasksShown = true;
				break;
			}
		}

		if (tasksShown) {
			html += "<div class='line'>";
			html += AccountingUtils.createLabelHtml("Scheduled:", textColor, "", "text-align: left; width: 8%;");
			html += AccountingUtils.createLabelHtml("Done:", textColor, "", "text-align: left; width: 8%;");
			html += AccountingUtils.createLabelHtml("Title:", textColor, "", "text-align: left; width: 42%;");
			html += "</div>";
		}

		for (GenericTask task : tasks) {
			if (task.hasBeenDone() && task.matches(searchFor)) {
				if (task instanceof Task) {
					html += ((Task) task).createPanelInHtml(database);
				} else {
					System.out.println("Expected a task but got " + task + "!");
				}
			}
		}

		// for-else:
		if (!tasksShown) {
			html += "<div>No previously performed tasks have been found in the log!</div>";
		}

		html += "<div class='footer'>&nbsp;</div>";

		return html;
	}

	@Override
	public void createTabOnGUI(final JPanel parentPanel, final Database database, String searchFor) {

		if (tab != null) {
			destroyTabOnGUI(parentPanel);
		}

		int i = 0;

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


		List<GenericTask> tasks = database.getTaskCtrl().getTaskInstances();

		boolean tasksShown = false;
		for (GenericTask task : tasks) {
			if (task.hasBeenDone() && task.matches(searchFor)) {
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

			curLabel = AccountingUtils.createLabel("Done:", textColor, "");
			curPanel.add(curLabel, new Arrangement(1, 0, 0.08, 1.0));

			curLabel = AccountingUtils.createLabel("Title:", textColor, "");
			curPanel.add(curLabel, new Arrangement(2, 0, 0.42, 1.0));

			curLabel = AccountingUtils.createLabel("", textColor, "");
			curPanel.add(curLabel, new Arrangement(3, 0, 0.0, 1.0));

			curLabel = AccountingUtils.createLabel("", textColor, "");
			curPanel.add(curLabel, new Arrangement(4, 0, 0.1, 1.0));

			curLabel = AccountingUtils.createLabel("", textColor, "");
			curPanel.add(curLabel, new Arrangement(5, 0, 0.05, 1.0));

			curLabel = AccountingUtils.createLabel("", textColor, "");
			curPanel.add(curLabel, new Arrangement(6, 0, 0.06, 1.0));

			curLabel = AccountingUtils.createLabel("", textColor, "");
			curPanel.add(curLabel, new Arrangement(7, 0, 0.0, 1.0));

			tab.add(curPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}

		for (GenericTask task : tasks) {
			if (task.hasBeenDone() && task.matches(searchFor)) {
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
			curLabel = new CopyByClickLabel("No previously performed tasks have been found in the log!");
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
	public void destroyTabOnGUI(JPanel parentPanel) {
		if (tab != null) {
			parentPanel.remove(tab);
		}
	}

	@Override
	public int getComparisonOrder() {
		return (10000 * 100) + 5000;
	}

	@Override
	public String toString() {
		return TITLE;
	}

}
