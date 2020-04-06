/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.world;


public enum Category {

	// salaries for a softer space employees
	INTERNAL_SALARY("Internal Salary"),

	// money paid to external contractors
	EXTERNAL_SALARY("External Salary"),

	// travel expenses
	TRAVEL("Travel Expenses"),

	// costs for cars, bikes, spacecraft, ...
	VEHICLE("Vehicles"),

	// entertainment expenses include things like restaurant visits for business purposes
	ENTERTAINMENT("Entertainment Costs"),

	// expenses for office rooms, buildings, planets?
	LOCATIONS("Locations"),

	// donations by a softer space
	DONATION("Donations"),

	// anything else
	OTHER("Other");


	private String text;


	private Category(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
