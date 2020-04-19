/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
}
