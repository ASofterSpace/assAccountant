/**
 * Unlicensed code created by A Softer Space, 2021
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.StrUtils;


public class ConfigCtrl {

	public final static String CONFIG_KEY_INVOICE_LOCATION_ON_DISK = "invoiceLocationOnDisk";


	private static String getInvoiceLocation() {
		return AssAccountant.getConfig().getValue(CONFIG_KEY_INVOICE_LOCATION_ON_DISK);
	}

	public static boolean invoiceLocationIsSet() {

		String diskLocation = getInvoiceLocation();

		if (diskLocation == null) {

			System.out.println("Sorry, the key " +
				CONFIG_KEY_INVOICE_LOCATION_ON_DISK + " in the configuration file " +
				AssAccountant.getConfig().getAbsoluteFilename() + " has not been set, and therefore " +
				"buttons about opening a monthly or yearly location cannot be shown!");

			return false;
		}

		return true;
	}

	public static String getInvoiceLocation(Integer year, Integer month) {

		String result = getInvoiceLocation();

		if (year == null) {
			return result;
		}

		result += "/" + year;

		if (month == null) {
			return result;
		}

		result += "/" + StrUtils.leftPad0(month + 1, 2) + " " + DateUtils.MONTH_NAMES[month].toLowerCase();

		return result;
	}

}
