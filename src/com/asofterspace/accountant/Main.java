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
	public final static String VERSION_NUMBER = "0.0.0.2(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "6. April 2020";

	private static ConfigFile config;

	private static Database database;
	private static TabCtrl tabCtrl;


	/**
	 * TODO:
	 * Export (as menu item), with Export as German CSV and Export as English CSV as sub-items
	 * Check Consistency (as menu item), maybe also automatically done on startup, checks e.g. for
	 *   all entries if their dates actually belong to the month and year that they say they belong to
	 * add input method for entries (independent of years and months, according to the date they are
	 *   automatically added where they need to go!)
	 * also handle timesheets (and automatically base invoices on timesheets)
	 * add outgoing invoices with different tax amounts
	 * add incoming invoices with different tax amounts and having different kinds
	 * add outgoing donations
	 * output monthly accounting information
	 * output monthly accounting json that we can save long-term just in case
	 * output yearly accounting information
	 * output yearly accounting json
	 * output information on how to integrate accounting information with other programs
	 *   (online USt, third party ESt, etc.)
	 * show if unsaved information exists (or do not even allow unsaved information, save
	 *   immediately? but how about undoing wrong changes?)
	 * automatically add years when the current date is later than an existing year
	 * perform and log finance days (1, 1.5 and 2 - each performance could be done through
	 *   the accountant, as much as possible being automated, and it having been done can
	 *   be logged, such that upon next startup it can be shown which next finance day is
	 *   now outstanding, if any, and which might come up over the coming weeks etc.)
	 * generate outgoing invoices with just a few clicks, and have them logged immediately
	 *   etc.
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
				config.setAllContents(new JSON("{}"));
			}
		} catch (JsonParseException e) {
			System.err.println("Loading the settings failed:");
			System.err.println(e);
			System.exit(1);
		}

		try {
			// load database
			database = new Database();
		} catch (JsonParseException e) {
			System.err.println("Loading the database failed:");
			System.err.println(e);
			System.exit(1);
		}

		System.out.println("Hi there! :)");
		System.out.println("Database has been loaded; I am ready!");

		tabCtrl = new TabCtrl(database);

		SwingUtilities.invokeLater(new GUI(database, tabCtrl, config));
	}

}
