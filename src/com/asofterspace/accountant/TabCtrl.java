/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.tabs.MonthTab;
import com.asofterspace.accountant.tabs.TimeSpanTab;
import com.asofterspace.accountant.tabs.YearTab;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.Year;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class TabCtrl {

	private Database database;


	public TabCtrl(Database database) {
		this.database = database;
	}

	public List<TimeSpanTab> getTabs() {

		List<TimeSpanTab> result = new ArrayList<>();

		List<Year> years = database.getYears();
		for (Year year : years) {
			result.add(new YearTab(year));
			for (Month month : year.getMonths()) {
				result.add(new MonthTab(month));
			}
		}

		Collections.sort(result, new Comparator<TimeSpanTab>() {
			public int compare(TimeSpanTab a, TimeSpanTab b) {
				return a.compareTo(b);
			}
		});

		return result;
	}
}
