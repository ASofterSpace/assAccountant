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
	public int getComparisonOrder() {
		return (10000 * 100) + 1000;
	}

}
