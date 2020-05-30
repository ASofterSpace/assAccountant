/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.world;


public enum Category {

	// wares, goods that we buy and resell, raw materials that we buy and refine, etc.
	WARES("Wares and Raw Materials", false),

	// salaries for a softer space employees
	INTERNAL_SALARY("Internal Salary", false),

	// money paid to external contractors
	EXTERNAL_SALARY("External Salary", false),

	// travel expenses
	TRAVEL("Travel Expenses", false),

	// costs for cars, bikes, spacecraft, ...
	VEHICLE("Vehicles", false),

	// entertainment expenses include things like restaurant visits for business purposes
	ENTERTAINMENT("Entertainment Costs", false),

	// expenses for office rooms, buildings, planets?
	LOCATIONS("Locations", false),

	// infrastructure necessary for us to perform our work (e.g. paper, ink, computer programs, ...)
	INFRASTRUCTURE("Infrastructure of IT Systems (Hardware, Software, Services etc.)", false),

	// education and conference attendance
	EDUCATION("Education and Conferences", false),

	// ads and branded items
	ADVERTISEMENTS("Advertisements and Branded Items", false),

	// personal spendings that are tracked anyway
	PERSONAL("Personal", true),

	// donations by a softer space
	DONATION("Donations", true),

	// anything else
	OTHER("Other", false);

	// when adding keys here, ENSURE that the textual representation and the key start with the same
	// word (until the first space or underscore, and ignoring trailing s), such that fromString works!


	// the textual representation of each category
	private String text;

	// the simplified representation of each category, that is,
	// internal, external, travel, vehicle, entertainment, location, donation, other
	private String startText;

	// is this a special category, or a regular one?
	private boolean isSpecialCategory;


	private Category(String text, boolean isSpecial) {

		this.text = text;

		this.startText = getStart(text);

		this.isSpecialCategory = isSpecial;
	}

	/**
	 * Accepts both the textual representation and the enum value as input
	 */
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

		if (val.contains("_")) {
			val = val.substring(0, val.indexOf("_"));
		}

		val = val.toLowerCase();

		if (val.endsWith("s")) {
			val = val.substring(0, val.length() - 1);
		}

		return val;
	}

	public boolean isSpecial() {
		return isSpecialCategory;
	}

}
