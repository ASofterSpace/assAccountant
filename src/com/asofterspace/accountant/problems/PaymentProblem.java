/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant;

import com.asofterspace.accountant.entries.Entry;


public class PaymentProblem extends Problem {

	public PaymentProblem(String problem, Entry entry) {
		super(problem, entry);
	}

	public boolean isImportant() {
		return true;
	}
}
