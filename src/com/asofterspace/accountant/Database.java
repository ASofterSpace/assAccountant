/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.utils.Record;

import java.util.ArrayList;
import java.util.List;


public class Database {

	private static final String YEARS_KEY = "years";

	private ConfigFile dbFile;

	private List<Year> years;


	public Database() throws JsonParseException {

		dbFile = new ConfigFile("database", true);
		years = new ArrayList<>();

		// create a default database file, if necessary
		if (dbFile.getAllContents().isEmpty()) {
			dbFile.setAllContents(new JSON("{\"" + YEARS_KEY + "\":[]}"));
		}

		Record root = dbFile.getAllContents();
		List<Record> yearRecs = root.getArray(YEARS_KEY);
		for (Record yearRec : yearRecs) {
			Year curYear = new Year(yearRec);
			years.add(curYear);
		}
	}

	public void save() {

		Record root = Record.emptyObject();
		Record yearRec = Record.emptyArray();
		root.set(YEARS_KEY, yearRec);

		for (Year year : years) {
			yearRec.append(year.toRecord());
		}

		dbFile.setAllContents(root);
	}
}
