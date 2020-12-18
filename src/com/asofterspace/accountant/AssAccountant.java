/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.tasks.TaskCtrl;
import com.asofterspace.accountant.web.Server;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonFile;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.Utils;
import com.asofterspace.toolbox.web.WebTemplateEngine;

import java.util.List;

import javax.swing.SwingUtilities;


public class AssAccountant {

	public final static String PROGRAM_TITLE = "assAccountant (Mari)";
	public final static String VERSION_NUMBER = "0.0.1.4(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "5. April 2020 - 18. December 2020";

	public final static String DATA_DIR = "config";
	public final static String SERVER_DIR = "server";
	public final static String WEB_ROOT_DIR = "deployed";

	private static ConfigFile config;

	private static Database database;
	private static TabCtrl tabCtrl;
	private static TaskCtrl taskCtrl;

	private static Directory webRoot;


	/**
	 * TODO:
	 * Export (as menu item), with Export as German CSV and Export as English CSV as sub-items
	 * make tax a dropdown edit field
	 * if we ever have more than 20 Mio € in any field, switch to using long instead of int... :)
	 * prevent undoing more than 64 steps, or redoing more steps than have been undone...
	 * if someone clicks on "Open on Disk" and the folder for that particular month (or even year)
	 *   does not yet exist, automatically create it, including all the subfolders (however, only
	 *   if a base location was actually specified in the config - not if the settings are just null!)
	 * also handle timesheets (and automatically base invoices on timesheets)
	 * automatically add years when the current date is later than an existing year
	 * generate incoming money - invoices to be sent (as pdfs?) with just a few clicks, and have them
	 *   logged immediately etc.
	 * automatically read outgoing emails and scan for incoming money - sent invoices
	 */
	public static void main(String[] args) {

		// let the Utils know in what program it is being used
		Utils.setProgramTitle(PROGRAM_TITLE);
		Utils.setVersionNumber(VERSION_NUMBER);
		Utils.setVersionDate(VERSION_DATE);

		if (args.length > 0) {
			if (args[0].replaceAll("-", "").toLowerCase().trim().equals("version")) {
				System.out.println(Utils.getFullProgramIdentifierWithDate());
				return;
			}

			if (args[0].replaceAll("-", "").toLowerCase().trim().equals("version_for_zip")) {
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

		System.out.println("Hi there, Mari here! :)");
		if (args.length < 1) {
			System.out.println("You have started me without any arguments, so I will just load up the GUI for you...");
		}
		System.out.println("Database has been loaded; I am ready!");

		tabCtrl = new TabCtrl(database);
		taskCtrl = new TaskCtrl(database);
		taskCtrl.init();

		boolean doExit = false;

		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				switch (args[i].replaceAll("-", "").trim().toLowerCase()) {
					case "help":
						System.out.println("You asked me to show the help; so I will do that and then exit...");
						System.out.println("");
						System.out.println("Here are all the configuration files that I use:");
						System.out.println("");
						System.out.println(config.getAbsoluteFilename() + " .. general settings file (JSON)");
						System.out.println(database.getDbFile().getAbsoluteFilename() + " .. main database file (JSON)");
						System.out.println("");
						System.out.println("Here is the list of all commandline arguments that I understand:");
						System.out.println("");
						System.out.println("help .. show this help, then exit");
						System.out.println("version .. show the version number of this program, then exit");
						System.out.println("version_for_zip .. show the condensed version number of this program, then exit");
						System.out.println("");
						System.out.println("drop_database .. drop the entire database");
						System.out.println("drop_bank_statements .. drop the bank statements from the database");
						System.out.println("import_bank_statements [folder] .. import (unencrypted!) bank statements from this folder, recursively");
						System.out.println("");
						System.out.println("exit .. after all other commands have been worked through, exit");
						System.out.println("");
						System.out.println("That was the help... kthxbye!");
						return;
					case "exit":
						System.out.println("You asked me to exit after being done with everything else... so I will!");
						doExit = true;
						break;
					case "drop_database":
						System.out.println("You asked me to drop the database, so it will be done...");
						database.drop();
						System.out.println("Done!");
						break;
					case "drop_bank_statements":
						System.out.println("You asked me to drop the bank statements, so it will be done...");
						database.dropBankStatements();
						System.out.println("Done!");
						break;
					case "import_bank_statements":
						i++;
						if (i < args.length) {
							Directory folder = new Directory(args[i]);
							System.out.println(
								"You asked me to import bank statements from the folder '" +
								folder.getAbsoluteDirname() + "'...");
							boolean recursively = true;
							List<File> filesToImport = folder.getAllFiles(recursively);
							database.bulkImportBankStatements(filesToImport);
							System.out.println("Done!");
						} else {
							System.out.println(
								"You asked me to import bank statements from a folder, " +
								"but did not give me a folder as next argument!");
							System.out.println(
								"Exiting as something went wrong...");
							System.exit(1);
						}
						break;
				}
			}
		}

		if (doExit) {
			System.out.println("Exiting, as you asked for it... byo!");
			return;
		}

		SwingUtilities.invokeLater(new GUI(database, tabCtrl, config));


		Directory dataDir = new Directory(System.getProperty("java.class.path") + "/../" + DATA_DIR);
		Directory serverDir = new Directory(System.getProperty("java.class.path") + "/../" + SERVER_DIR);
		webRoot = new Directory(System.getProperty("java.class.path") + "/../" + WEB_ROOT_DIR);

		try {

			JsonFile jsonConfigFile = new JsonFile(serverDir, "webengine.json");
			JSON jsonConfig = jsonConfigFile.getAllContents();
			jsonConfig.inc("version");
			jsonConfigFile.save(jsonConfig);

			List<String> whitelist = jsonConfig.getArrayAsStringList("files");

			System.out.println("Templating the web application...");

			WebTemplateEngine engine = new WebTemplateEngine(serverDir, jsonConfig);

			engine.compileTo(webRoot);


			System.out.println("Starting the server on port " + database.getPort() + "...");

			Server server = new Server(webRoot, serverDir, database, tabCtrl);

			server.setWhitelist(whitelist);

			boolean async = false;

			server.serve(async);


			System.out.println("Server done, all shut down and cleaned up! Have a nice day! :)");

		} catch (JsonParseException e) {

			System.out.println("Oh no! The input could not be parsed: " + e);
		}
	}

	public static TaskCtrl getTaskCtrl() {
		return taskCtrl;
	}

	public static Directory getWebRoot() {
		return webRoot;
	}

}
