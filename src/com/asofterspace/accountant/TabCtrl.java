/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.tabs.MonthTab;
import com.asofterspace.accountant.tabs.OverviewTab;
import com.asofterspace.accountant.tabs.Tab;
import com.asofterspace.accountant.tabs.YearTab;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.Year;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class TabCtrl {

	private Database database;

	private OverviewTab overviewTab;


	public TabCtrl(Database database) {
		this.database = database;
		this.overviewTab = new OverviewTab();
	}

	public List<Tab> getTabs() {

		List<Tab> result = new ArrayList<>();

		result.add(overviewTab);

		List<Year> years = database.getYears();
		for (Year year : years) {
			result.add(new YearTab(year));
			for (Month month : year.getMonths()) {
				result.add(new MonthTab(month));
			}
		}

		Collections.sort(result, new Comparator<Tab>() {
			public int compare(Tab a, Tab b) {
				return a.compareTo(b);
			}
		});

		return result;
	}

	public OverviewTab getOverviewTab() {
		return overviewTab;
	}
}
