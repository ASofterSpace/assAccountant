/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.world.Category;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.Utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


public class AddEntryGUI {

	private GUI mainGUI;

	private Database database;

	private JDialog dialog;

	private JComboBox<String> customer;
	private JComboBox<String> category;


	public AddEntryGUI(GUI mainGUI, Database database) {

		this.mainGUI = mainGUI;

		this.database = database;

		this.dialog = createGUI();
	}

	private JDialog createGUI() {

		// Create the window
		final JDialog dialog = new JDialog(mainGUI.getMainFrame(), "Add Entry", true);
		GridLayout dialogLayout = new GridLayout(8, 1);
		dialogLayout.setVgap(8);
		dialog.setLayout(dialogLayout);
		dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Enter a new entry here to add it:");
		dialog.add(explanationLabel);

		JPanel curPanel;
		JLabel curLabel;

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new JLabel("Kind: ");
		ButtonGroup inOutGroup = new ButtonGroup();
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		final JRadioButton isIncoming = new JRadioButton("Incoming (we have to pay)");
		inOutGroup.add(isIncoming);
		isIncoming.setSelected(true);
		isIncoming.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					customer.setVisible(false);
					category.setVisible(true);
				}
			}
		});
		curPanel.add(isIncoming, new Arrangement(1, 0, 1.0, 1.0));
		final JRadioButton isOutgoing = new JRadioButton("Outgoing (we get paid)");
		inOutGroup.add(isOutgoing);
		isOutgoing.setSelected(false);
		isOutgoing.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					customer.setVisible(true);
					category.setVisible(false);
				}
			}
		});
		curPanel.add(isOutgoing, new Arrangement(2, 0, 1.0, 1.0));
		dialog.add(curPanel);

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new JLabel("Date in YYYY-MM-DD or DD. MM. YYYY: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		final JTextField dateText = new JTextField(DateUtils.serializeDate(null));
		curPanel.add(dateText, new Arrangement(1, 0, 1.0, 1.0));
		dialog.add(curPanel);

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new JLabel("Title: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		final JTextField titleText = new JTextField();
		curPanel.add(titleText, new Arrangement(1, 0, 1.0, 1.0));
		dialog.add(curPanel);
		titleText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				autoselectCategory();
			}
			public void removeUpdate(DocumentEvent e) {
				autoselectCategory();
			}
			public void insertUpdate(DocumentEvent e) {
				autoselectCategory();
			}
			public void autoselectCategory() {
				Category cat = database.mapTitleToCategory(titleText.getText());
				String catStr = cat.getText();
				for (int i = 0; i < category.getItemCount(); i++) {
					String cur = category.getItemAt(i);
					if (catStr.equals(cur)) {
						category.setSelectedIndex(i);
					}
				}
			}
		});

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new JLabel("Category or Customer: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		customer = new JComboBox<>();
		customer.setEditable(true);
		curPanel.add(customer, new Arrangement(1, 0, 1.0, 1.0));
		customer.setVisible(false);
		category = new JComboBox<>(Category.getTexts());
		category.setEditable(false);
		curPanel.add(category, new Arrangement(1, 0, 1.0, 1.0));
		dialog.add(curPanel);

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new JLabel("Amount (both . and , taken as decimal separator): ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		final JTextField amount = new JTextField();
		curPanel.add(amount, new Arrangement(1, 0, 1.0, 1.0));
		curLabel = new JLabel(" €");
		curPanel.add(curLabel, new Arrangement(2, 0, 0.0, 1.0));
		dialog.add(curPanel);

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new JLabel("Tax percentage: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		final JTextField taxPerc = new JTextField();
		curPanel.add(taxPerc, new Arrangement(1, 0, 1.0, 1.0));
		curLabel = new JLabel(" %");
		curPanel.add(curLabel, new Arrangement(2, 0, 0.0, 1.0));
		dialog.add(curPanel);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 3);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		dialog.add(buttonRow);

		JButton addButton = new JButton("Add This Entry");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Object catOrCustomer = customer.getSelectedItem();
				if (isIncoming.isSelected()) {
					catOrCustomer = category.getSelectedItem();
				}

				database.addEntry(dateText.getText(), titleText.getText(), catOrCustomer,
					amount.getText(), Currency.EUR, taxPerc.getText(), isIncoming.isSelected());

				refreshCustomers();
			}
		});
		buttonRow.add(addButton);

		JButton addAndExitButton = new JButton("Add This Entry and Exit");
		addAndExitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Object catOrCustomer = customer.getSelectedItem();
				if (isIncoming.isSelected()) {
					catOrCustomer = category.getSelectedItem();
				}

				if (database.addEntry(dateText.getText(), titleText.getText(), catOrCustomer,
					amount.getText(), Currency.EUR, taxPerc.getText(), isIncoming.isSelected())) {

					refreshCustomers();

					dialog.dispose();
				}
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
		refreshCustomers();
	}

	private void refreshCustomers() {

		Object prev = customer.getSelectedItem();

		customer.removeAllItems();

		for (String curCustomer : database.getCustomers()) {
			customer.addItem(curCustomer);
		}

		if (prev != null) {
			String prevStr = prev.toString();
			for (int i = 0; i < customer.getItemCount(); i++) {
				String cur = customer.getItemAt(i);
				if (prev.equals(cur)) {
					customer.setSelectedIndex(i);
				}
			}
		}
	}

}
