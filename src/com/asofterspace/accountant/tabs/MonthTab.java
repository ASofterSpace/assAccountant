/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.toolbox.gui.Arrangement;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class MonthTab extends TimeSpanTab {

	private Month month;

	private JPanel tab;


	public MonthTab(Month month) {
		this.month = month;
	}

	@Override
	public void createTabOnGUI(JPanel parentPanel) {

		if (tab != null) {
			destroyTabOnGUI(parentPanel);
		}

		tab = new JPanel();
		tab.setLayout(new GridBagLayout());

		JPanel topHUD = new JPanel();
		topHUD.setLayout(new GridBagLayout());

		JLabel nameLabel = new JLabel(month.toString());
		nameLabel.setPreferredSize(new Dimension(0, nameLabel.getPreferredSize().height*2));
		nameLabel.setHorizontalAlignment(JLabel.CENTER);
		topHUD.add(nameLabel, new Arrangement(0, 0, 1.0, 1.0));

		tab.add(topHUD, new Arrangement(0, 0, 1.0, 1.0));

		parentPanel.add(tab);
	}

	@Override
	public void destroyTabOnGUI(JPanel parentPanel) {
		if (tab != null) {
			parentPanel.remove(tab);
		}
	}

	@Override
	public int compareTo(TimeSpanTab tab) {
		if (tab == null) {
			return 1;
		}
		if (tab instanceof MonthTab) {
			int result = tab.getYear().getNum() - getYear().getNum();
			if (result == 0) {
				return ((MonthTab) tab).getMonth().getNum() - month.getNum();
			}
			return result;
		}
		if (tab instanceof YearTab) {
			int result = tab.getYear().getNum() - getYear().getNum();
			if (result == 0) {
				return 1;
			}
			return result;
		}
		return 1;
	}

	public Month getMonth() {
		return month;
	}

	@Override
	public Year getYear() {
		return month.getYear();
	}

	@Override
	public String toString() {
		return month.toString();
	}

}
