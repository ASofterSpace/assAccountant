/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.entries.Entry;


public class ConsistencyProblem extends Problem {

	public ConsistencyProblem(String problem, Entry entry) {
		super(problem, entry);
	}

	@Override
	public boolean isImportant() {
		return true;
	}
}
