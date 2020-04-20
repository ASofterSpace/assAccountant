/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.CopyByClickLabel;
import com.asofterspace.toolbox.utils.DateUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;


/**
 * A task describes some action that will have to be taken at a certain time,
 * and whose execution can be logged
 */
public class Task {

	protected String title;

	// on which day of the month is this task scheduled?
	protected Integer scheduledOnDay;

	// in which month is this task scheduled?
	protected Integer scheduledInMonth;

	protected List<String> details;

	// has this task already been done?
	protected Boolean done;

	// for which date was this task instance released for the user to look at?
	// (this might be before or after the date the user actually first saw it,
	// e.g. the user have have seen it as a future task, or it may have been
	// generated days later... this is really the date in the calendar that
	// triggered the schedule for this task to generate this instance!)
	protected Integer releasedOnDay;
	protected Integer releasedInMonth;
	protected Integer releasedInYear;

	// when was this task done?
	protected Date doneDate;

	protected TaskCtrl taskCtrl;


	public Task(TaskCtrl taskCtrl, String title, Integer scheduledOnDay, Integer scheduledInMonth,
		List<String> details) {

		this.taskCtrl = taskCtrl;
		this.title = title;
		this.scheduledOnDay = scheduledOnDay;
		this.scheduledInMonth = scheduledInMonth;
		this.details = details;
	}

	public Task getNewInstance() {
		return new Task(taskCtrl, title, scheduledOnDay, scheduledInMonth, details);
	}

	public boolean isScheduledOn(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		if (scheduledOnDay != null) {
			if (!scheduledOnDay.equals(cal.get(Calendar.DAY_OF_MONTH))) {
				return false;
			}
		}

		if (scheduledInMonth != null) {
			if (!scheduledInMonth.equals(cal.get(Calendar.MONTH))) {
				return false;
			}
		}

		return true;
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

	public Boolean hasBeenDone() {
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

	public String getReleasedDateStr() {
		String day = ""+getReleasedOnDay();
		if (day.length() < 2) {
			day = "0" + day;
		}
		String month = ""+(getReleasedInMonth()+1);
		if (month.length() < 2) {
			month = "0" + month;
		}
		return getReleasedInYear() + "-" + month + "-" + day;
	}

	public Date getDoneDate() {
		return doneDate;
	}

	public void setDoneDate(Date doneDate) {
		this.doneDate = doneDate;
	}

	public JPanel createPanelOnGUI(Database database) {

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();
		Color textColor = new Color(0, 0, 0);

		JPanel curPanel = new JPanel();
		curPanel.setBackground(GUI.getBackgroundColor());
		curPanel.setLayout(new GridBagLayout());

		CopyByClickLabel curLabel = createLabel(getReleasedDateStr(), textColor, "");
		curPanel.add(curLabel, new Arrangement(0, 0, 0.08, 1.0));

		curLabel = createLabel(title, textColor, "");
		curPanel.add(curLabel, new Arrangement(1, 0, 0.5, 1.0));

		curLabel = createLabel("", textColor, "");
		curPanel.add(curLabel, new Arrangement(2, 0, 0.0, 1.0));

		JButton curButton = new JButton("Show Details");
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(3, 0, 0.1, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AccountingUtils.complain("Not yet implemented!");
			}
		});

		curButton = new JButton("Done");
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(4, 0, 0.05, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Task.this.done = true;
				setDoneDate(new Date());
				// setDoneDate(DateUtils.parseDate(getReleasedDateStr())); // DEBUG
				taskCtrl.save();
			}
		});

		curButton = new JButton("Delete");
		curButton.setPreferredSize(defaultDimension);
		curPanel.add(curButton, new Arrangement(5, 0, 0.06, 1.0));
		curButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				taskCtrl.deleteTaskInstance(Task.this);
				taskCtrl.save();
			}
		});

		curLabel = createLabel("", textColor, "");
		curPanel.add(curLabel, new Arrangement(6, 0, 0.0, 1.0));

		return curPanel;
	}

	private CopyByClickLabel createLabel(String text, Color color, String tooltip) {

		CopyByClickLabel result = new CopyByClickLabel(text);

		if (!"".equals(tooltip)) {
			result.setToolTipText(tooltip);
		}

		Dimension defaultDimension = GUI.getDefaultDimensionForInvoiceLine();
		result.setPreferredSize(defaultDimension);

		result.setForeground(color);

		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof Task) {
			Task otherTask = (Task) other;
			if (this.title.equals(otherTask.title) &&
				this.done.equals(otherTask.done) &&
				this.releasedOnDay.equals(otherTask.releasedOnDay) &&
				this.releasedInMonth.equals(otherTask.releasedInMonth) &&
				this.releasedInYear.equals(otherTask.releasedInYear)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		if ((done == null) || done.equals(false)) {
			return -1;
		}
		return this.releasedOnDay + 64 * this.releasedInMonth + 1024 * this.releasedInYear;
	}

}
