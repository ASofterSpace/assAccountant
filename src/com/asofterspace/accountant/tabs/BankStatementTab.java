/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;


public class BankStatementTab extends BankStatementYearTab {

	public BankStatementTab() {
		super(null);
	}

	@Override
	public int compareTo(Tab tab) {
		if (tab == null) {
			return 1;
		}
		if (tab instanceof OverviewTab) {
			return 1;
		}
		if (tab instanceof TaskLogTab) {
			return 1;
		}
		if (tab instanceof FinanceLogTab) {
			return 1;
		}
		if (tab instanceof BankStatementTab) {
			return 0;
		}
		return -1;
	}

}
