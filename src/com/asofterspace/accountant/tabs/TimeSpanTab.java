/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.timespans.Year;


public abstract class TimeSpanTab {

	public abstract int compareTo(TimeSpanTab tab);

	public abstract Year getYear();
}
