/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.Utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;


public class AddTaskGUI {

	private GUI mainGUI;

	private Database database;

	private JDialog dialog;

	private JTextField titleText;
	private JTextField dateText;
	private JTextPane detailsText;


	public AddTaskGUI(GUI mainGUI, Database database) {

		this.mainGUI = mainGUI;

		this.database = database;

		this.dialog = createGUI();
	}

	private JDialog createGUI() {

		// Create the window
		final JDialog dialog = new JDialog(mainGUI.getMainFrame(), "Add Ad-hoc Task", true);
		GridBagLayout dialogLayout = new GridBagLayout();
		dialog.setLayout(dialogLayout);
		dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		CopyByClickLabel explanationLabel = new CopyByClickLabel("Enter a new ad-hoc task here to add it:");
		dialog.add(explanationLabel, new Arrangement(0, 0, 1.0, 0.0));

		JPanel curPanel;
		CopyByClickLabel curLabel;

		dialog.add(new JLabel(" "), new Arrangement(0, 1, 1.0, 0.0));

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new CopyByClickLabel("Title: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		titleText = new JTextField();
		curPanel.add(titleText, new Arrangement(1, 0, 1.0, 1.0));
		dialog.add(curPanel, new Arrangement(0, 2, 1.0, 0.0));

		dialog.add(new JLabel(" "), new Arrangement(0, 3, 1.0, 0.0));

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new CopyByClickLabel("Release Date: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		dateText = new JTextField(DateUtils.serializeDate(new Date()));
		curPanel.add(dateText, new Arrangement(1, 0, 1.0, 1.0));
		dialog.add(curPanel, new Arrangement(0, 4, 1.0, 0.0));

		dialog.add(new JLabel(" "), new Arrangement(0, 5, 1.0, 0.0));

		curLabel = new CopyByClickLabel("Details:");
		dialog.add(curLabel, new Arrangement(0, 6, 1.0, 0.0));

		detailsText = new JTextPane();
		dialog.add(detailsText, new Arrangement(0, 7, 1.0, 1.0));

		dialog.add(new JLabel(" "), new Arrangement(0, 8, 1.0, 0.0));

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = null;
		buttonRowLayout = new GridLayout(1, 3);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		dialog.add(buttonRow, new Arrangement(0, 9, 1.0, 0.0));

		JButton addButton = new JButton("Add This Task");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addTask(false);
			}
		});
		buttonRow.add(addButton);

		JButton addAndExitButton = new JButton("Add This Task and Exit");
		addAndExitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addTask(true);
			}
		});
		buttonRow.add(addAndExitButton);

		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonRow.add(doneButton);

		// Set the preferred size of the dialog
		int width = 700;
		int height = 400;
		dialog.setSize(width, height);
		dialog.setPreferredSize(new Dimension(width, height));

		return dialog;
	}

	public void show() {
		GuiUtils.centerAndShowWindow(dialog);
	}

	private void addTask(boolean exitOnSuccess) {

		String title = titleText.getText();
		String details = detailsText.getText();
		String date = dateText.getText();

		if (database.getTaskCtrl().addAdHocTask(title, details, date)) {

			if (exitOnSuccess) {
				dialog.dispose();
			}
		}
	}

}
