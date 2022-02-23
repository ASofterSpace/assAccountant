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

	public FinanceOverviewTask(String title, Integer scheduledOnDay, List<String> scheduledOnDaysOfWeek, List<Integer> scheduledInMonths,
		List<Integer> scheduledInYears, List<String> details, List<String> onDone, Boolean biweeklyEven, Boolean biweeklyOdd) {

		super(title, scheduledOnDay, scheduledOnDaysOfWeek, scheduledInMonths, scheduledInYears, details, onDone,
			biweeklyEven, biweeklyOdd);
	}

	public FinanceOverviewTask(GenericTask other) {
		super(other);
	}

	@Override
	public FinanceOverviewTask getNewInstance() {
		return new FinanceOverviewTask(this);
	}
}
