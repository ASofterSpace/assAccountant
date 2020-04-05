/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.world;


public enum Category {

	OTHER,
	TRAVEL,
	DONATION;

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
