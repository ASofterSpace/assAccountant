/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.tabs.TimeSpanTab;
import com.asofterspace.accountant.timespans.Month;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class AddPaidGUI {

	private GUI mainGUI;

	private Database database;

	private Entry editingEntry;

	private JDialog dialog;

	private JCheckBox received;
	private JTextField dateText;
	private JComboBox<String> account;


	public AddPaidGUI(GUI mainGUI, Database database, Entry editingEntry) {

		this.mainGUI = mainGUI;

		this.database = database;

		this.editingEntry = editingEntry;

		this.dialog = createGUI();
	}

	private JDialog createGUI() {

		// Create the window
		final JDialog dialog = new JDialog(mainGUI.getMainFrame(), "Add Paid by Date", true);
		GridLayout dialogLayout = new GridLayout(6, 1);
		dialogLayout.setVgap(8);
		dialog.setLayout(dialogLayout);
		dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		CopyByClickLabel explanationLabel = new CopyByClickLabel("You are adding a paid by date to this entry:");
		dialog.add(explanationLabel);

		CopyByClickLabel entryLabel = new CopyByClickLabel(editingEntry.toString());
		dialog.add(entryLabel);

		JPanel curPanel;
		CopyByClickLabel curLabel;

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new CopyByClickLabel("The Entry Has Actually Been Paid: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		received = new JCheckBox();
		curPanel.add(received, new Arrangement(1, 0, 1.0, 1.0));
		dialog.add(curPanel);

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new CopyByClickLabel("Paid by Date: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		dateText = new JTextField(DateUtils.serializeDate(null));
		curPanel.add(dateText, new Arrangement(1, 0, 1.0, 1.0));
		dialog.add(curPanel);

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new CopyByClickLabel("Paid to Account: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		account = new JComboBox<>();
		account.setEditable(true);
		curPanel.add(account, new Arrangement(1, 0, 1.0, 1.0));
		dialog.add(curPanel);
		refreshAccounts();

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		dialog.add(buttonRow);

		JButton addAndExitButton = new JButton("Save Changes");
		addAndExitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addPaidInfo(true);
			}
		});
		buttonRow.add(addAndExitButton);

		JButton doneButton = new JButton("Cancel");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonRow.add(doneButton);

		// Set the preferred size of the dialog
		int width = 700;
		int height = 300;
		dialog.setSize(width, height);
		dialog.setPreferredSize(new Dimension(width, height));

		if (editingEntry != null) {
			// always set this to true as we open the modal when we want to set it to paid...
			received.setSelected(true);
			Date date = editingEntry.getReceivedOnDate();
			if (date == null) {
				dateText.setText("");
			} else {
				dateText.setText(DateUtils.serializeDate(date));
			}
			String searchFor = editingEntry.getReceivedOnAccount();
			if (searchFor == null) {
				if (account.getItemCount() > 0) {
					account.setSelectedIndex(0);
				}
			} else {
				for (int i = 0; i < account.getItemCount(); i++) {
					if (account.getItemAt(i).equals(searchFor)) {
						account.setSelectedIndex(i);
					}
				}
			}
		}

		return dialog;
	}

	public void show() {
		GuiUtils.centerAndShowWindow(dialog);
		refreshAccounts();
	}

	private void addPaidInfo(boolean exitOnSuccess) {

		if (editingEntry.setPaidInfo(received.isSelected(), dateText.getText(), (String) account.getSelectedItem())) {

			Date date = editingEntry.getDate();
			if (date != null) {
				Month month = database.getMonthFromEntryDate(date);
				TimeSpanTab curTab = mainGUI.getTabForTimeSpan(month);
				if (curTab != null) {
					mainGUI.showTabAndHighlightInTree(curTab);
				}
			}

			refreshAccounts();

			if (exitOnSuccess) {
				dialog.dispose();
			}
		}
	}

	private void refreshAccounts() {

		Object prev = account.getSelectedItem();

		account.removeAllItems();

		for (String curAccount : database.getAccounts()) {
			account.addItem(curAccount);
		}

		if (prev != null) {
			String prevStr = prev.toString();
			for (int i = 0; i < account.getItemCount(); i++) {
				String cur = account.getItemAt(i);
				if (prev.equals(cur)) {
					account.setSelectedIndex(i);
				}
			}
		}
	}

}
