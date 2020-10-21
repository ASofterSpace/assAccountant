/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tasks;

import com.asofterspace.toolbox.calendar.GenericTask;

import java.util.List;


/**
 * A task that has a finance overview attached, which will be logged in a special way
 */
public class FinanceOverviewTask extends Task {

	public FinanceOverviewTask(String title, Integer scheduledOnDay,
		List<Integer> scheduledInMonths, List<String> details, List<String> onDone) {

		super(title, scheduledOnDay, scheduledInMonths, details, onDone);
	}

	public FinanceOverviewTask(GenericTask other) {
		super(other);
	}

	@Override
	public FinanceOverviewTask getNewInstance() {
		return new FinanceOverviewTask(this);
	}
}
