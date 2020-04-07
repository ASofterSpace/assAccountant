/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.GUI;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.accountant.world.Currency;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;


public class Database {

	private static final String YEARS_KEY = "years";
	private static final String BACKUP_FILE_NAME = "database_backup_";
	private static final String CURRENT_BACKUP_KEY = "currentBackup";

	private static int BACKUP_MAX = 64;

	private GUI gui;

	private ConfigFile settingsFile;

	// we take backups in a ring buffer on every save
	// (which we can use also to undo any action!)
	private int currentBackup;

	private ConfigFile dbFile;

	private List<Year> years;


	public Database(ConfigFile settings) throws JsonParseException {

		this.settingsFile = settings;
		this.currentBackup = settingsFile.getInteger(CURRENT_BACKUP_KEY, 0);

		this.dbFile = new ConfigFile("database", true);

		loadFromFile(dbFile);
	}

	private Record loadFromFile(ConfigFile fileToLoad) {

		this.years = new ArrayList<>();

		// create a default database file, if necessary
		if (fileToLoad.getAllContents().isEmpty()) {
			try {
				fileToLoad.setAllContents(new JSON("{\"" + YEARS_KEY + "\":[]}"));
			} catch (JsonParseException e) {
				System.err.println("JSON parsing failed internally: " + e);
			}
		}

		Record root = fileToLoad.getAllContents();
		List<Record> yearRecs = root.getArray(YEARS_KEY);
		for (Record yearRec : yearRecs) {
			Year curYear = new Year(yearRec);
			years.add(curYear);
		}
		return root;
	}

	public void setGUI(GUI gui) {
		this.gui = gui;
	}

	public List<Year> getYears() {
		return years;
	}

	public boolean addYear(int yearNum) {
		for (Year cur : years) {
			if (cur.getNum() == yearNum) {
				return false;
			}
		}
		years.add(new Year(yearNum));
		save();
		return true;
	}

	public void undo() {
		currentBackup--;
		applyUndoRedo();
	}

	public void redo() {
		currentBackup++;
		applyUndoRedo();
	}

	public void applyUndoRedo() {

		overflowCheckBackups();

		try {
			Record root = loadFromFile(new ConfigFile(BACKUP_FILE_NAME + currentBackup, true));

			// when undoing and redoing, we want to actually save what we did, so that if we
			// close and re-open the assAccountant, we still have stuff undone, and are not
			// back to the front of the queue!
			dbFile.setAllContents(root);

			gui.showTab(null);
			gui.regenerateTabList();

		} catch (JsonParseException e) {
			System.err.println("JSON parsing failed for " + BACKUP_FILE_NAME + currentBackup + ": " + e);
		}
	}

	public boolean addEntry(String dateStr, String text, String catOrCustomer, String amount,
		Currency currency, String taxationPercent, boolean isIncoming) {

		Date date = DateUtils.parseDate(dateStr);

		// TODO:
		// parse the date, find the correct year + month object, adding a year if none exists so far,
		// then parse all the other data and generate an Entry object, appending that one to the
		// Month, and then save the database... :)

/*
		try {

			int newYearNumInt = Integer.parseInt(newYearNum.trim());

			if (database.addYear(newYearNumInt)) {

				mainGUI.regenerateTabList();

				return true;

			} else {

				JOptionPane.showMessageDialog(
					null,
					"The year " + newYearNum + " already exists!",
					Utils.getProgramTitle(),
					JOptionPane.ERROR_MESSAGE
				);
			}

		} catch (NumberFormatException e) {

			JOptionPane.showMessageDialog(
				null,
				"The input " + newYearNum + " could not be parsed as a number!",
				Utils.getProgramTitle(),
				JOptionPane.ERROR_MESSAGE
			);
		}
*/
		return false;
	}

	public void save() {

		Record root = Record.emptyObject();
		Record yearRec = Record.emptyArray();
		root.set(YEARS_KEY, yearRec);

		for (Year year : years) {
			yearRec.append(year.toRecord());
		}

		dbFile.setAllContents(root);

		currentBackup++;
		overflowCheckBackups();
		new ConfigFile(BACKUP_FILE_NAME + currentBackup, true, root);

		gui.refreshOpenTab();
	}

	private void overflowCheckBackups() {

		if (currentBackup > BACKUP_MAX) {
			currentBackup = 0;
		}

		if (currentBackup < 0) {
			currentBackup = BACKUP_MAX;
		}

		// we check for backup overflow when we adjusted currentBackup... so may as well save it now :)
		settingsFile.set(CURRENT_BACKUP_KEY, currentBackup);
	}
}
