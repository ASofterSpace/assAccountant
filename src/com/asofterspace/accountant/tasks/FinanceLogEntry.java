/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.utils.DateUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;


public class FinanceLogEntry {

	private Date createdOn;

	private List<FinanceLogEntryRow> rows;


	public FinanceLogEntry(Date date) {
		this.createdOn = date;
		this.rows = new ArrayList<>();
	}

	public void add(FinanceLogEntryRow row) {
		rows.add(row);
	}

	public Date getDate() {
		return createdOn;
	}

	public List<FinanceLogEntryRow> getRows() {
		return rows;
	}

	public JPanel createPanelOnGUI(Database database) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();
		Color textColor = new Color(0, 0, 0);

		JPanel curPanel = new JPanel();
		curPanel.setBackground(GUI.getBackgroundColor());
		curPanel.setLayout(new GridBagLayout());

		int i = 0;

		CopyByClickLabel curLabel = AccountingUtils.createSubHeadLabel(DateUtils.serializeDate(getDate()));
		curPanel.add(curLabel, new Arrangement(0, i, 1.0, 1.0));
		i++;

		for (FinanceLogEntryRow row : rows) {
			JPanel curCurPanel = row.createPanelOnGUI(database);
			curPanel.add(curCurPanel, new Arrangement(0, i, 1.0, 1.0));
			i++;
		}

		return curPanel;
	}

}
