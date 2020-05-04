/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.entries.Entry;
import com.asofterspace.accountant.entries.Incoming;
import com.asofterspace.accountant.entries.Outgoing;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.tabs.TimeSpanTab;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.world.Category;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.barcodes.FinanceUtils;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.StrUtils;
import com.asofterspace.toolbox.Utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


public class AddEntryGUI {

	private GUI mainGUI;

	private Database database;

	private Entry editingEntry;

	private JDialog dialog;

	private JComboBox<String> customer;
	private JComboBox<String> category;
	private JComboBox<String> originator;
	private JRadioButton isIncoming;
	private JTextField dateText;
	private JTextField titleText;
	private JTextField amount;
	private JTextField amountPostTax;
	private JTextField taxPerc;

	private boolean ignoreTaxUpdates = false;
	private boolean lastTaxChangeWasPreTax = true;


	public AddEntryGUI(GUI mainGUI, Database database, Entry editingEntry) {

		this.mainGUI = mainGUI;

		this.database = database;

		this.editingEntry = editingEntry;

		this.dialog = createGUI();
	}

	private JDialog createGUI() {

		// Create the window
		final JDialog dialog = new JDialog(mainGUI.getMainFrame(), "Add Entry", true);
		GridLayout dialogLayout = new GridLayout(10, 1);
		dialogLayout.setVgap(8);
		dialog.setLayout(dialogLayout);
		dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		CopyByClickLabel explanationLabel = new CopyByClickLabel("Enter a new entry here to add it:");
		dialog.add(explanationLabel);

		JPanel curPanel;
		CopyByClickLabel curLabel;

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new CopyByClickLabel("Kind: ");
		ButtonGroup inOutGroup = new ButtonGroup();
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		isIncoming = new JRadioButton("Incoming (we have to pay)");
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
		curLabel = new CopyByClickLabel("Date in YYYY-MM-DD or DD. MM. YYYY: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		dateText = new JTextField(DateUtils.serializeDate(null));
		curPanel.add(dateText, new Arrangement(1, 0, 1.0, 1.0));
		dialog.add(curPanel);

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new CopyByClickLabel("Title: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		titleText = new JTextField();
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
		curLabel = new CopyByClickLabel("Category or Customer: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		customer = new JComboBox<>();
		customer.setEditable(true);
		curPanel.add(customer, new Arrangement(1, 0, 1.0, 1.0));
		customer.setVisible(false);
		category = new JComboBox<>(Category.getTexts());
		category.setEditable(false);
		curPanel.add(category, new Arrangement(1, 0, 1.0, 1.0));
		dialog.add(curPanel);
		refreshCustomers();

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new CopyByClickLabel("Originator: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		originator = new JComboBox<>(database.getOriginators().toArray(new String[0]));
		originator.setEditable(true);
		curPanel.add(originator, new Arrangement(1, 0, 1.0, 1.0));
		dialog.add(curPanel);

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new CopyByClickLabel("Amount before tax: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		amount = new JTextField();
		curPanel.add(amount, new Arrangement(1, 0, 1.0, 1.0));
		curLabel = new CopyByClickLabel(" €");
		curPanel.add(curLabel, new Arrangement(2, 0, 0.0, 1.0));
		dialog.add(curPanel);

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new CopyByClickLabel("Tax percentage: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		taxPerc = new JTextField();
		curPanel.add(taxPerc, new Arrangement(1, 0, 1.0, 1.0));
		curLabel = new CopyByClickLabel(" %");
		curPanel.add(curLabel, new Arrangement(2, 0, 0.0, 1.0));
		dialog.add(curPanel);

		curPanel = new JPanel();
		curPanel.setLayout(new GridBagLayout());
		curLabel = new CopyByClickLabel("Amount after tax: ");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.0, 1.0));
		amountPostTax = new JTextField();
		curPanel.add(amountPostTax, new Arrangement(1, 0, 1.0, 1.0));
		curLabel = new CopyByClickLabel(" €");
		curPanel.add(curLabel, new Arrangement(2, 0, 0.0, 1.0));
		dialog.add(curPanel);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = null;
		if (editingEntry == null) {
			buttonRowLayout = new GridLayout(1, 3);
		} else {
			buttonRowLayout = new GridLayout(1, 2);
		}
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		dialog.add(buttonRow);

		if (editingEntry == null) {
			JButton addButton = new JButton("Add This Entry");
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addEntry(false);
				}
			});
			buttonRow.add(addButton);
		}

		JButton addAndExitButton = new JButton("Add This Entry and Exit");
		if (editingEntry != null) {
			addAndExitButton.setText("Save Changes");
		}
		addAndExitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addEntry(true);
			}
		});
		buttonRow.add(addAndExitButton);

		JButton doneButton = new JButton("Done");
		if (editingEntry != null) {
			doneButton.setText("Cancel");
		}
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

		if (editingEntry != null) {
			dateText.setText(editingEntry.getDateAsText());
			titleText.setText(editingEntry.getTitle());
			if (editingEntry instanceof Incoming) {
				String searchFor = ((Incoming) editingEntry).getCategoryAsText();
				for (int i = 0; i < category.getItemCount(); i++) {
					if (category.getItemAt(i).equals(searchFor)) {
						category.setSelectedIndex(i);
					}
				}
				isOutgoing.setSelected(false);
				isIncoming.setSelected(true);
			} else {
				String searchFor = ((Outgoing) editingEntry).getCustomer();
				for (int i = 0; i < customer.getItemCount(); i++) {
					if (customer.getItemAt(i).equals(searchFor)) {
						customer.setSelectedIndex(i);
					}
				}
				isIncoming.setSelected(false);
				isOutgoing.setSelected(true);
			}
			if (editingEntry.hasPreTaxAmount()) {
				amount.setText(FinanceUtils.formatMoney(editingEntry.getPreTaxAmount()));
			} else {
				amount.setText("");
			}
			taxPerc.setText(""+editingEntry.getTaxPercent());
			if (editingEntry.hasPostTaxAmount()) {
				amountPostTax.setText(FinanceUtils.formatMoney(editingEntry.getPostTaxAmount()));
			} else {
				amountPostTax.setText("");
			}
			lastTaxChangeWasPreTax = editingEntry.usesPreTaxAmount();
			refreshTax();
			for (int i = 0; i < originator.getItemCount(); i++) {
				if (originator.getItemAt(i).equals(editingEntry.getOriginator())) {
					originator.setSelectedIndex(i);
				}
			}
		}

		DocumentListener preTaxUpdateListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				lastTaxChangeWasPreTax = true;
				refreshTax();
			}
			public void removeUpdate(DocumentEvent e) {
				lastTaxChangeWasPreTax = true;
				refreshTax();
			}
			public void insertUpdate(DocumentEvent e) {
				lastTaxChangeWasPreTax = true;
				refreshTax();
			}
		};

		DocumentListener generalTaxUpdateListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				refreshTax();
			}
			public void removeUpdate(DocumentEvent e) {
				refreshTax();
			}
			public void insertUpdate(DocumentEvent e) {
				refreshTax();
			}
		};

		DocumentListener afterTaxUpdateListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				lastTaxChangeWasPreTax = false;
				refreshTax();
			}
			public void removeUpdate(DocumentEvent e) {
				lastTaxChangeWasPreTax = false;
				refreshTax();
			}
			public void insertUpdate(DocumentEvent e) {
				lastTaxChangeWasPreTax = false;
				refreshTax();
			}
		};

		amount.getDocument().addDocumentListener(preTaxUpdateListener);
		taxPerc.getDocument().addDocumentListener(generalTaxUpdateListener);
		amountPostTax.getDocument().addDocumentListener(afterTaxUpdateListener);

		return dialog;
	}

	public synchronized void refreshTax() {

		if (ignoreTaxUpdates) {
			return;
		}

		ignoreTaxUpdates = true;
		boolean lastTaxChangeWasPreTaxOrig = lastTaxChangeWasPreTax;

		if (lastTaxChangeWasPreTax) {
			if (!amountPostTax.isFocusOwner()) {
				Integer amountPreTaxInt = StrUtils.parseMoney(amount.getText());
				Integer amountTaxInt = AccountingUtils.parseTaxes(taxPerc.getText());
				Integer amountPostTaxInt = FinanceUtils.calcPostTax(amountPreTaxInt, amountTaxInt);
				String target = "";
				if (amountPostTaxInt != null) {
					target = FinanceUtils.formatMoney(amountPostTaxInt);
				}
				if (!target.equals(amountPostTax.getText())) {
					amountPostTax.setText(target);
				}
			}
		} else {
			if (!amount.isFocusOwner()) {
				Integer amountTaxInt = AccountingUtils.parseTaxes(taxPerc.getText());
				Integer amountPostTaxInt = StrUtils.parseMoney(amountPostTax.getText());
				Integer amountPreTaxInt = FinanceUtils.calcPreTax(amountPostTaxInt, amountTaxInt);
				String target = "";
				if (amountPreTaxInt != null) {
					target = FinanceUtils.formatMoney(amountPreTaxInt);
				}
				if (!target.equals(amount.getText())) {
					amount.setText(target);
				}
			}
		}
		lastTaxChangeWasPreTax = lastTaxChangeWasPreTaxOrig;

		ignoreTaxUpdates = false;
	}

	public void show() {
		GuiUtils.centerAndShowWindow(dialog);
		refreshCustomers();
	}

	private void addEntry(boolean exitOnSuccess) {

		Object catOrCustomer = customer.getSelectedItem();
		if (isIncoming.isSelected()) {
			catOrCustomer = category.getSelectedItem();
		}

		String preTaxAmountStr = null;
		String postTaxAmountStr = null;

		if (lastTaxChangeWasPreTax) {
			preTaxAmountStr = amount.getText();
		} else {
			postTaxAmountStr = amountPostTax.getText();
		}

		// we add the new entry (no matter if we are editing a new one or editing an existing one)...
		if (database.addEntry(dateText.getText(), titleText.getText(), catOrCustomer,
			preTaxAmountStr, Currency.EUR, taxPerc.getText(), postTaxAmountStr,
			(String) originator.getSelectedItem(), isIncoming.isSelected())) {

			// ... and if we are editing an existing one, we delete the existing one
			// (think about this as the scifi transporter approach to editing ^^)
			if (editingEntry != null) {
				editingEntry.deleteFrom(database);
			}

			Date date = DateUtils.parseDate(dateText.getText());
			if (date != null) {
				Month month = database.getMonthFromEntryDate(date);
				TimeSpanTab curTab = mainGUI.getTabForTimeSpan(month);
				if (curTab != null) {
					mainGUI.showTabAndHighlightInTree(curTab);
				}
			}

			refreshCustomers();

			if (exitOnSuccess) {
				dialog.dispose();
			}
		}
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
