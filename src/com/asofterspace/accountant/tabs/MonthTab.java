/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.timespans.Month;


public MonthTab extends TimeSpanTab {

	private Month month;


	public int compareTo(TimeSpanTab tab) {
		if (tab == null) {
			return 1;
		}
		if (tab instanceof MonthTab) {
			return month.getNum() - ((MonthTab) tab).getMonth().getNum();
		}
		if (tab instanceof YearTab) {
			int result = getYear().getNum() - ((YearTab) tab).getYear().getNum();
			if (result == 0) {
				return 1;
			}
		}
		return 1;
	}

	public Month getMonth() {
		return month;
	}

	@Override
	public Year getYear() {
		return month.getYear();
	}

}
