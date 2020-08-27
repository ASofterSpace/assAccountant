/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.entries.Entry;


public abstract class Problem {

	private String problem;

	private Entry entry;


	public Problem(String problem, Entry entry) {
		this.problem = problem;
		this.entry = entry;
	}

	public String getProblem() {
		return problem;
	}

	public Entry getEntry() {
		return entry;
	}

	public abstract boolean isImportant();

}
