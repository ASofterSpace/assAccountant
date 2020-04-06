/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.timespans.Year;


public class YearTab extends TimeSpanTab {

	private Year year;


	@Override
	public int compareTo(TimeSpanTab tab) {
		if (tab == null) {
			return 1;
		}
		return year.getNum() - tab.getYear().getNum();
	}

	@Override
	public Year getYear() {
		return year;
	}

}
