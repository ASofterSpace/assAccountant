/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.transactions;

import com.asofterspace.accountant.AccountingUtils;
import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.toolbox.accounting.Currency;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.StrUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.GridBagLayout;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * This represents a generic bank transaction which is not a main entry
 * reflected in the system (the assAccountant mainly handles invoices),
 * but also being monitored because why not :)
 */
public class BankTransaction {

	private static final String AMOUNT_KEY = "amount";
	private static final String TITLE_KEY = "title";
	private static final String DATE_KEY = "date";

	private Integer amount;

	private String title;

	private Date date;

	private BankAccount belongsTo;


	private BankTransaction() {
	}

	public BankTransaction(Integer amount, String title, Date date, BankAccount belongsTo) {
		this.amount = amount;
		this.title = title;
		this.date = date;
		this.belongsTo = belongsTo;
	}

	public static BankTransaction fromRecord(Record rec, BankAccount parent) {

		BankTransaction result = new BankTransaction();

		result.amount = rec.getInteger(AMOUNT_KEY);

		result.title = rec.getString(TITLE_KEY);

		result.date = DateUtils.parseDate(rec.getString(DATE_KEY));

		result.belongsTo = parent;

		return result;
	}

	public Record toRecord() {

		Record result = Record.emptyObject();

		result.set(AMOUNT_KEY, amount);

		result.set(TITLE_KEY, title);

		result.set(DATE_KEY, DateUtils.serializeDate(date));

		return result;
	}

	public Integer getAmount() {
		return amount;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String newTitle) {
		this.title = newTitle;
	}

	public Date getDate() {
		return date;
	}

	public String createPanelInHtml(Database database) {

		String html = "";

		Color textColor = new Color(0, 0, 0);

		html += "<div class='line'>";

		html += AccountingUtils.createLabelHtml(DateUtils.serializeDate(getDate()), textColor, "", "text-align: left; width: 10%;");
		html += AccountingUtils.createLabelHtml(StrUtils.replaceAll(getTitle(), "\n", "<br>"), textColor, "", "text-align: left; width: 70%;");
		html += AccountingUtils.createLabelHtml(database.formatMoney(getAmount(), Currency.EUR), textColor, "", "text-align: right; width: 10%;");

		// TODO - add working buttons
		/*
		JButton curButton = new JButton("Prepare Entry");
		curButton.addMouseListener(rowHighlighter);
		curButton.setMinimumSize(defaultDimension);
		curButton.setPreferredSize(defaultDimension);
		curCurPanel.add(curButton, new Arrangement(4, 0, 0.1, 0.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Entry fakeEntry = null;
				if (amount > 0) {
					fakeEntry = new Incoming(null, Currency.EUR, 19, amount, getDate(), getTitle(), "", "", null);
				} else {
					fakeEntry = new Outgoing(null, Currency.EUR, 19, -amount, getDate(), getTitle(), "", null, null);
				}
				AddEntryGUI addEntryGUI = new AddEntryGUI(database.getGUI(), database, fakeEntry);
				addEntryGUI.show();
			}
		});
		*/

		html += "</div>";

		return html;
	}

	public JPanel createPanelOnGUI(Database database) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();
		Color textColor = new Color(0, 0, 0);
		CopyByClickLabel curLabel;

		JPanel curPanel = new JPanel();
		curPanel.setBackground(GUI.getBackgroundColor());
		curPanel.setLayout(new GridBagLayout());

		MouseAdapter rowHighlighter = AccountingUtils.getRowHighlighter(curPanel);

		String[] titleRows = getTitle().split("\n");

		int i = 0;

		JPanel curCurPanel = new JPanel();
		curCurPanel.setOpaque(false);
		curCurPanel.setLayout(new GridBagLayout());

		curLabel = AccountingUtils.createLabel(DateUtils.serializeDate(getDate()), textColor, "");
		curLabel.addMouseListener(rowHighlighter);
		curCurPanel.add(curLabel, new Arrangement(0, 0, 0.1, 0.0));

		curLabel = AccountingUtils.createLabel(titleRows[0], textColor, "");
		curLabel.addMouseListener(rowHighlighter);
		curCurPanel.add(curLabel, new Arrangement(1, 0, 0.7, 0.0));

		curLabel = AccountingUtils.createLabel(database.formatMoney(getAmount(), Currency.EUR), textColor, "");
		curLabel.addMouseListener(rowHighlighter);
		curLabel.setHorizontalAlignment(JLabel.RIGHT);
		curCurPanel.add(curLabel, new Arrangement(2, 0, 0.1, 0.0));

		curLabel = new CopyByClickLabel("");
		curLabel.addMouseListener(rowHighlighter);
		curLabel.setPreferredSize(defaultDimension);
		curCurPanel.add(curLabel, new Arrangement(3, 0, 0.0, 0.0));

		/*
		JButton curButton = new JButton("Prepare Entry");
		curButton.addMouseListener(rowHighlighter);
		curButton.setMinimumSize(defaultDimension);
		curButton.setPreferredSize(defaultDimension);
		curCurPanel.add(curButton, new Arrangement(4, 0, 0.1, 0.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Entry fakeEntry = null;
				if (amount > 0) {
					fakeEntry = new Incoming(null, Currency.EUR, 19, amount, getDate(), getTitle(), "", "", null);
				} else {
					fakeEntry = new Outgoing(null, Currency.EUR, 19, -amount, getDate(), getTitle(), "", null, null);
				}
				AddEntryGUI addEntryGUI = new AddEntryGUI(database.getGUI(), database, fakeEntry);
				addEntryGUI.show();
			}
		});
		*/

		curLabel = new CopyByClickLabel("");
		curLabel.addMouseListener(rowHighlighter);
		curLabel.setPreferredSize(defaultDimension);
		curCurPanel.add(curLabel, new Arrangement(5, 0, 0.0, 0.0));

		curPanel.add(curCurPanel, new Arrangement(0, i, 1.0, 0.0));
		i++;

		for (int s = 1; s < titleRows.length; s++) {

			curCurPanel = new JPanel();
			curCurPanel.setOpaque(false);
			curCurPanel.setLayout(new GridBagLayout());

			curLabel = new CopyByClickLabel("");
			curLabel.addMouseListener(rowHighlighter);
			curLabel.setPreferredSize(defaultDimension);
			curCurPanel.add(curLabel, new Arrangement(0, 0, 0.1, 0.0));

			curLabel = AccountingUtils.createLabel(titleRows[s], textColor, "");
			curLabel.addMouseListener(rowHighlighter);
			curCurPanel.add(curLabel, new Arrangement(1, 0, 0.7, 0.0));

			curLabel = new CopyByClickLabel("");
			curLabel.addMouseListener(rowHighlighter);
			curLabel.setPreferredSize(defaultDimension);
			curCurPanel.add(curLabel, new Arrangement(2, 0, 0.1, 0.0));

			curLabel = new CopyByClickLabel("");
			curLabel.addMouseListener(rowHighlighter);
			curLabel.setPreferredSize(defaultDimension);
			curCurPanel.add(curLabel, new Arrangement(3, 0, 0.0, 0.0));

			curLabel = new CopyByClickLabel("");
			curLabel.addMouseListener(rowHighlighter);
			curLabel.setMinimumSize(defaultDimension);
			curLabel.setPreferredSize(defaultDimension);
			curCurPanel.add(curLabel, new Arrangement(4, 0, 0.1, 0.0));

			curLabel = new CopyByClickLabel("");
			curLabel.addMouseListener(rowHighlighter);
			curLabel.setPreferredSize(defaultDimension);
			curCurPanel.add(curLabel, new Arrangement(5, 0, 0.0, 0.0));

			curPanel.add(curCurPanel, new Arrangement(0, i, 1.0, 0.0));
			i++;
		}

		return curPanel;
	}

	public boolean matches(String searchFor) {
		if ("".equals(searchFor)) {
			return true;
		}
		if (title.replace("\\n", "").toLowerCase().contains(searchFor.toLowerCase())) {
			return true;
		}
		return false;
	}

	public boolean belongsTo(Year year) {
		// every entry belongs to a wildcard year :)
		if (year == null) {
			return true;
		}
		if (getDate() == null) {
			return false;
		}
		return DateUtils.serializeDate(getDate()).substring(0, 4).equals(""+year.getNum());
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof BankTransaction) {
			BankTransaction otherBankTransaction = (BankTransaction) other;
			if (!this.amount.equals(otherBankTransaction.amount)) {
				 return false;
			}
			if (!this.title.equals(otherBankTransaction.title)) {
				 return false;
			}
			if (!this.date.equals(otherBankTransaction.date)) {
				 return false;
			}
			if (!this.belongsTo.equals(otherBankTransaction.belongsTo)) {
				 return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return amount;
	}

}
