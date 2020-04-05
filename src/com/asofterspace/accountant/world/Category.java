/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.world;


public enum Category {

	// salaries for a softer space employees
	INTERNAL_SALARY,

	// money paid to external contractors
	EXTERNAL_SALARY,

	// travel expenses
	TRAVEL,

	// costs for cars, bikes, spacecraft, ...
	VEHICLE,

	// entertainment expenses include things like restaurant visits for business purposes
	ENTERTAINMENT,

	// expenses for office rooms, buildings, planets?
	LOCATIONS,

	// donations by a softer space
	DONATION,

	// anything else
	OTHER;


	public static Category fromString(String val) {

		if (val == null) {
			return null;
		}

		switch (val) {
			case "OTHER":
				return Category.OTHER;
			case "TRAVEL":
				return Category.TRAVEL;
			case "DONATION":
				return Category.DONATION;
		}

		return null;
	}
}
