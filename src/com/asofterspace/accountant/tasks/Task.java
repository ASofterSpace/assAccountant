/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.toolbox.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * A task describes some action that will have to be taken at a certain time,
 * and whose execution can be logged
 */
public class Task {

	private String title;

	// on which day of the month is this task scheduled?
	private Integer scheduledOnDay;

	// in which month is this task scheduled?
	private Integer scheduledInMonth;

	private List<String> details;

	// has this task already been done?
	private Boolean done;

	// on which day of the month was this task actually created?
	private Integer releasedOnDay;

	// in which month was this task actually created?
	private Integer releasedInMonth;

	// in which year was this task actually released?
	private Integer releasedInYear;


	public Task(String title, Integer scheduledOnDay, Integer scheduledInMonth, List<String> details) {
		this.title = title;
		this.scheduledOnDay = scheduledOnDay;
		this.scheduledInMonth = scheduledInMonth;
		this.details = details;
	}

	public String getTitle() {
		return title;
	}

	public Integer getScheduledOnDay() {
		return scheduledOnDay;
	}

	public Integer getScheduledInMonth() {
		return scheduledInMonth;
	}

	/**
	 * Return detailed instructions for the user such that they know what to do with this task
	 */
	public List<String> getDetails() {
		return details;
	}

	/**
	 * Actually return the instructions as shown to the user, with information replaced with
	 * actual info
	 */
	public List<String> getDetailsToShowToUser() {
		List<String> results = new ArrayList<>();
		if (details != null) {
			for (String detail : details) {
				detail = detail.replaceAll("%[DAY]", ""+releasedOnDay);
				detail = detail.replaceAll("%[MONTH]", ""+releasedInMonth);
				detail = detail.replaceAll("%[NAME_OF_MONTH]", DateUtils.monthNumToName(releasedInMonth));
				detail = detail.replaceAll("%[YEAR]", ""+releasedInYear);
				detail = detail.replaceAll("%[PREV_DAY]", ""+(releasedOnDay-1));
				int prevMonth = releasedInMonth - 1;
				if (prevMonth < 0) {
					prevMonth = 11;
				}
				detail = detail.replaceAll("%[PREV_MONTH]", ""+prevMonth);
				detail = detail.replaceAll("%[NAME_OF_PREV_MONTH]", DateUtils.monthNumToName(prevMonth));
				detail = detail.replaceAll("%[PREV_YEAR]", ""+(releasedInYear-1));
				results.add(detail);
			}
		}
		return results;
	}

	public Boolean getDone() {
		return done;
	}

	public void setDone(Boolean done) {
		this.done = done;
	}

	public Integer getReleasedOnDay() {
		return releasedOnDay;
	}

	public void setReleasedOnDay(Integer releasedOnDay) {
		this.releasedOnDay = releasedOnDay;
	}

	public Integer getReleasedInMonth() {
		return releasedInMonth;
	}

	public void setReleasedInMonth(Integer releasedInMonth) {
		this.releasedInMonth = releasedInMonth;
	}

	public Integer getReleasedInYear() {
		return releasedInYear;
	}

	public void setReleasedInYear(Integer releasedInYear) {
		this.releasedInYear = releasedInYear;
	}
}
