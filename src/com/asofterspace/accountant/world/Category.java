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

	// infrastructure necessary for us to perform our work (e.g. paper, ink, computer programs, ...)
	INFRASTRUCTURE("Infrastructure of IT Systems (Hardware, Software, Services etc.)"),

	// education and conference attendance
	EDUCATION("Education and Conferences"),

	// ads and branded items
	ADVERTISEMENTS("Advertisements and Branded Items"),

	// donations by a softer space
	DONATION("Donations"),

	// anything else
	OTHER("Other");


	// the textual representation of each category
	private String text;

	// the simplified representation of each category, that is,
	// internal, external, travel, vehicle, entertainment, location, donation, other
	private String startText;


	private Category(String text) {

		this.text = text;

		this.startText = getStart(text);
	}

	public static Category fromString(String from) {

		String startFrom = getStart(from);

		for (Category cat : Category.values()) {
			if (cat.startText.equals(startFrom)) {
				return cat;
			}
		}
		return null;
	}

	public String getText() {
		return text;
	}

	public static String[] getTexts() {
		String[] result = new String[Category.values().length];
		int i = 0;
		for (Category cat : Category.values()) {
			result[i] = cat.getText();
			i++;
		}
		return result;
	}

	private static String getStart(String val) {

		val = val.trim();

		if (val.contains(" ")) {
			 val = val.substring(0, val.indexOf(" "));
		 }

		 val = val.toLowerCase();

		 if (val.endsWith("s")) {
			 val = val.substring(0, val.length() - 1);
		 }

		return val;
	}

}
