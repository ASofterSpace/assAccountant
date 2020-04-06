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
	public final static String VERSION_NUMBER = "0.0.0.1(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "5. April 2020";

	private static ConfigFile config;

	private static Database database;
	private static TabCtrl tabCtrl;


	/**
	 * TODO:
	 * add outgoing invoices with different tax amounts
	 * add incoming invoices with different tax amounts and having different kinds
	 * add outgoing donations
	 * output monthly accounting information
	 * output monthly accounting json that we can save long-term just in case
	 * output yearly accounting information
	 * output yearly accounting json
	 * output information on how to integrate accounting information with other programs
	 *   (online USt, third party ESt, etc.)
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

		System.out.println("Accountant out. Have a fun day! :)");
	}

}
