/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.Utils;

import javax.swing.SwingUtilities;


public class Main {

	public final static String PROGRAM_TITLE = "assAccountant";
	public final static String VERSION_NUMBER = "0.0.0.5(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "5. April 2020 - 20. April 2020";

	private static ConfigFile config;

	private static Database database;
	private static TabCtrl tabCtrl;
	private static TaskCtrl taskCtrl;


	/**
	 * TODO:
	 * Export (as menu item), with Export as German CSV and Export as English CSV as sub-items
	 * make tax a dropdown edit field
	 * pre-select category based on title
	 * if we ever have more than 20 Mio € in any field, switch to using long instead of int... :)
	 * prevent undoing more than 64 steps, or redoing more steps than have been undone...
	 * if someone clicks on "Open on Disk" and the folder for that particular month (or even year)
	 *   does not yet exist, automatically create it, including all the subfolders (however, only
	 *   if a base location was actually specified in the config - not if the settings are just null!)
	 * also handle timesheets (and automatically base invoices on timesheets)
	 * add to outgoing invoices a "date paid" which can be null if not yet paid, or a date can be
	 *   entered for when it was paid
	 * edit existing entries
	 * output monthly accounting json that we can save long-term just in case
	 * output yearly accounting information
	 * output yearly accounting json
	 * output information on how to integrate accounting information with other programs
	 *   (online USt, third party ESt, etc.)
	 * automatically add years when the current date is later than an existing year
	 * perform and log finance days (1, 1.5 and 2 - each performance could be done through
	 *   the accountant, as much as possible being automated, and it having been done can
	 *   be logged, such that upon next startup it can be shown which next finance day is
	 *   now outstanding, if any, and which might come up over the coming weeks etc.)
	 * generate outgoing invoices (as pdfs?) with just a few clicks, and have them logged
	 *   immediately etc.
	 * automatically read incoming emails and scan for incoming invoices
	 */
	public static void main(String[] args) {

		// let the Utils know in what program it is being used
		Utils.setProgramTitle(PROGRAM_TITLE);
		Utils.setVersionNumber(VERSION_NUMBER);
		Utils.setVersionDate(VERSION_DATE);

		if (args.length > 0) {
			if (args[0].equals("--version")) {
				System.out.println(Utils.getFullProgramIdentifierWithDate());
				return;
			}

			if (args[0].equals("--version_for_zip")) {
				System.out.println("version " + Utils.getVersionNumber());
				return;
			}
		}

		try {
			// load config
			config = new ConfigFile("settings", true);

			// create a default config file, if necessary
			if (config.getAllContents().isEmpty()) {
				config.setAllContents(new JSON("{\"currentBackup\": 0}"));
			}
		} catch (JsonParseException e) {
			System.err.println("Loading the settings failed:");
			System.err.println(e);
			System.exit(1);
		}

		try {
			// load database
			database = new Database(config);
		} catch (JsonParseException e) {
			System.err.println("Loading the database failed:");
			System.err.println(e);
			System.exit(1);
		}

		System.out.println("Hi there! :)");
		System.out.println("Database has been loaded; I am ready!");

		tabCtrl = new TabCtrl(database);
		taskCtrl = new TaskCtrl(database);

		SwingUtilities.invokeLater(new GUI(database, tabCtrl, config));
	}

}
