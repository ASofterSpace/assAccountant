/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.tabs.BankStatementTab;
import com.asofterspace.accountant.tabs.BankStatementYearTab;
import com.asofterspace.accountant.tabs.CalculatorTab;
import com.asofterspace.accountant.tabs.FinanceLogTab;
import com.asofterspace.accountant.tabs.IncomeLogTab;
import com.asofterspace.accountant.tabs.InfoTab;
import com.asofterspace.accountant.tabs.LoanTab;
import com.asofterspace.accountant.tabs.MonthTab;
import com.asofterspace.accountant.tabs.OverviewTab;
import com.asofterspace.accountant.tabs.RentTab;
import com.asofterspace.accountant.tabs.Tab;
import com.asofterspace.accountant.tabs.TaskLogTab;
import com.asofterspace.accountant.tabs.TaxTab;
import com.asofterspace.accountant.tabs.YearTab;
import com.asofterspace.accountant.timespans.Month;
import com.asofterspace.accountant.timespans.Year;

import java.util.ArrayList;
import java.util.List;


public class TabCtrl {

	private Database database;

	private OverviewTab overviewTab;
	private TaskLogTab taskLogTab;
	private FinanceLogTab financeLogTab;
	private IncomeLogTab incomeLogTab;
	private RentTab rentTab;
	private LoanTab loanTab;
	private TaxTab taxTab;
	private CalculatorTab calculatorTab;
	private BankStatementTab bankStatementTab;
	private InfoTab infoTab;

	private List<Tab> tabList;


	public TabCtrl(Database database) {
		this.database = database;
		this.overviewTab = new OverviewTab();
		this.taskLogTab = new TaskLogTab();
		this.financeLogTab = new FinanceLogTab();
		this.incomeLogTab = new IncomeLogTab();
		this.rentTab = new RentTab();
		this.loanTab = new LoanTab();
		this.taxTab = new TaxTab();
		this.calculatorTab = new CalculatorTab();
		this.bankStatementTab = new BankStatementTab();
		this.infoTab = new InfoTab();

		this.tabList = createTabList();
	}

	private List<Tab> createTabList() {

		List<Tab> result = new ArrayList<>();

		result.add(overviewTab);
		result.add(taskLogTab);
		result.add(financeLogTab);
		result.add(incomeLogTab);
		result.add(rentTab);
		result.add(loanTab);
		result.add(taxTab);
		result.add(calculatorTab);
		result.add(infoTab);
		result.add(bankStatementTab);

		List<Year> years = database.getYears();
		for (Year year : years) {
			result.add(new YearTab(year));
			for (Month month : year.getMonths()) {
				result.add(new MonthTab(month));
			}
			result.add(new BankStatementYearTab(year));
		}

		for (Integer year : database.getBankStatementOnlyYears()) {
			result.add(new BankStatementYearTab(new Year(year, database)));
		}

		return result;
	}

	public List<Tab> getTabs() {
		return tabList;
	}

	public OverviewTab getOverviewTab() {
		return overviewTab;
	}
}
