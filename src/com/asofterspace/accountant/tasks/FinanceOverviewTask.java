/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import java.util.List;


/**
 * A task that has a finance overview attached, which will be logged in a special way
 */
public class FinanceOverviewTask extends Task {

	public FinanceOverviewTask(TaskCtrl taskCtrl, String title, Integer scheduledOnDay,
		Integer scheduledInMonth, List<String> details) {

		super(taskCtrl, title, scheduledOnDay, scheduledInMonth, details);
	}

	@Override
	public FinanceOverviewTask getNewInstance() {
		return new FinanceOverviewTask(taskCtrl, title, scheduledOnDay, scheduledInMonth, details);
	}
}
