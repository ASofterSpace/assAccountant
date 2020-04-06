/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.timespans.Year;


public class YearTab extends TimeSpanTab {

	private Year year;


	public YearTab(Year year) {
		this.year = year;
	}

	@Override
	public int compareTo(TimeSpanTab tab) {
		if (tab == null) {
			return 1;
		}
		return tab.getYear().getNum() - year.getNum();
	}

	@Override
	public Year getYear() {
		return year;
	}

	@Override
	public String toString() {
		return year.toString();
	}

}
