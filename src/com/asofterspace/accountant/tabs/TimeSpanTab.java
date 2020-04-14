/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.timespans.TimeSpan;
import com.asofterspace.accountant.timespans.Year;


public abstract class TimeSpanTab extends Tab {

	public abstract TimeSpan getTimeSpan();

	public abstract Year getYear();
}
