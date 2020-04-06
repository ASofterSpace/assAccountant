/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;




public class TabCtrl {

	private Database database;


	public TabCtrl(Database database) {
		this.database = database;
	}

	public List<TimeSpanTab> getTabs() {

		List<TimeSpanTab> result = new ArrayList<>();

		// TODO :: actually get tabs from the database

		Collections.sort(result, new Comparator<Year>() {
			public int compare(TimeSpanTab a, TimeSpanTab b) {
				return a.compareTo(b);
			}
		});

		return result;
	}
}
