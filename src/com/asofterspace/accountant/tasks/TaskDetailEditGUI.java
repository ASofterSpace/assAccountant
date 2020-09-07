/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tasks;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.utils.StrUtils;
import com.asofterspace.toolbox.Utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;


public class TaskDetailEditGUI {

	private GUI mainGUI;

	private Database database;

	private Task task;

	private JDialog dialog;

	private JTextPane detailsText;


	public TaskDetailEditGUI(GUI mainGUI, Database database, Task task) {

		this.mainGUI = mainGUI;

		this.database = database;

		this.task = task;

		this.dialog = createGUI();
	}

	private JDialog createGUI() {

		// Create the window
		final JDialog dialog = new JDialog(mainGUI.getMainFrame(), "Edit Task Details", true);
		GridBagLayout dialogLayout = new GridBagLayout();
		dialog.setLayout(dialogLayout);
		dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		CopyByClickLabel explanationLabel = new CopyByClickLabel("Enter the new details:");
		dialog.add(explanationLabel, new Arrangement(0, 0, 1.0, 0.0));

		JPanel curPanel;
		CopyByClickLabel curLabel;

		dialog.add(new JLabel(" "), new Arrangement(0, 1, 1.0, 0.0));

		detailsText = new JTextPane();
		detailsText.setText(StrUtils.join("\n", task.getDetails()));
		dialog.add(detailsText, new Arrangement(0, 7, 1.0, 1.0));

		dialog.add(new JLabel(" "), new Arrangement(0, 8, 1.0, 0.0));

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = null;
		buttonRowLayout = new GridLayout(1, 3);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		dialog.add(buttonRow, new Arrangement(0, 9, 1.0, 0.0));

		JButton addButton = new JButton("Save");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveTaskDetails();
				dialog.dispose();
			}
		});
		buttonRow.add(addButton);

		JButton doneButton = new JButton("Cancel");
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

	private void saveTaskDetails() {

		List<String> detailsList = new ArrayList<>();
		String details = detailsText.getText();
		for (String detail : details.split("\n")) {
			detailsList.add(detail);
		}

		task.setDetails(detailsList);

		database.save();

		task.hideDetails();
		task.showDetails();
	}

}
