/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.tabs;

import com.asofterspace.accountant.timespans.Year;
import com.asofterspace.toolbox.gui.Arrangement;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class YearTab extends TimeSpanTab {

	private Year year;

	private JPanel tab;


	public YearTab(Year year) {
		this.year = year;
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

		JLabel nameLabel = new JLabel(year.toString() + " (yearly overview)");
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
		return tab.getYear().getNum() - year.getNum();
	}

	@Override
	public Year getYear() {
		return year;
	}

	@Override
	public String toString() {
		return year.toString();
	}

}
