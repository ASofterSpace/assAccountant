window.accountant = {

	// id of the entry we are currently editing
	currentlyEditing: null,
	currentlyPaid: null,
	currentlyDeleting: null,

	lastTaxChangeWasPreTax: true,


	exportCsvs: function(tab) {
		var request = new XMLHttpRequest();
		request.open("POST", "exportCSVs", true);
		request.setRequestHeader("Content-Type", "application/json");

		request.onreadystatechange = function() {
			if (request.readyState == 4 && request.status == 200) {
				var result = JSON.parse(request.response);
				alert("Exported to: " + result.exportPath);
			}
		}

		var data = {
			tab: tab,
		};

		request.send(JSON.stringify(data));
	},

	changeFormat: function() {
		// reload the page with changed format
		if (window.currentFormat == "EN") {
			window.currentFormat = "DE";
		} else {
			window.currentFormat = "EN";
		}
		window.location.href = window.location.origin + window.location.pathname + "?format=" + window.currentFormat;
	},

	onResize: function() {

		var retry = false;

		var body = document.getElementById("body");
		if (body) {
			body.style.height = window.innerHeight + "px";
		} else {
			retry = true;
		}

		var mainContent = document.getElementById("mainContent");
		if (mainContent) {
			mainContent.style.height = (window.innerHeight - 31) + "px";
		} else {
			retry = true;
		}

		var tabList = document.getElementById("tabList");
		var mariAvatar = document.getElementById("mariAvatar");
		if (tabList && mariAvatar) {
			var topPx = mariAvatar.clientHeight + 25;
			tabList.style.top = topPx + "px";
			tabList.style.height = (window.innerHeight - (topPx + 25)) + "px";
			if (mariAvatar.clientHeight < 1) {
				retry = true;
			}
		} else {
			retry = true;
		}

		if (retry) {
			// if we could not fully resize now, then let's do it later...
			window.setTimeout(function() {
				window.accountant.onResize();
			}, 100);
		}
	},

	copyText: function(textToCopy) {

		var copyEl = document.getElementById("copyInputField");

		if (copyEl) {
			copyEl.value = textToCopy;

			copyEl.select();

			document.execCommand("copy");
		}
	},

	openInOS: function(year, month) {

		var request = new XMLHttpRequest();
		request.open("POST", "openInOS", true);
		request.setRequestHeader("Content-Type", "application/json");
		request.send('{"year": ' + year + ', "month": ' + month + '}');
	},

	showDetails: function(id) {

		var taskDetailsHolder = document.getElementById("task-details-" + id);
		if (taskDetailsHolder) {
			if (taskDetailsHolder.style.display == "block") {
				taskDetailsHolder.style.display = "none";
			} else {
				taskDetailsHolder.innerHTML = "Hmmm, let me think for a second...";
				taskDetailsHolder.style.display = "block";

				var request = new XMLHttpRequest();
				request.open("GET", "taskInstance?id=" + id, true);
				request.setRequestHeader("Content-Type", "application/json");

				request.onreadystatechange = function() {
					if (request.readyState == 4 && request.status == 200) {
						var result = JSON.parse(request.response);
						if (result.success) {
							taskDetailsHolder.innerHTML = result.detailsHtml;
						}
					}
				}

				request.send();
			}
		}
	},

	updateBankStatementView: function() {

		// set all to invisible
		var elems = document.getElementsByClassName("bankStatementContainer");
		for (var i = 0; i < elems.length; i++) {
			elems[i].style.display = 'none';
		}

		// if one is selected...
		var which = document.getElementById("bankStatementSelect").value;
		var elem = document.getElementById("bankStatementNum" + which);
		if (elem) {
			// ... set it to visible ...
			elem.style.display = 'block';
		} else {
			// ... otherwise, set all to visible
			for (var i = 0; i < elems.length; i++) {
				elems[i].style.display = 'block';
			}
		}
	},

	editEntry: function(id) {

		var request = new XMLHttpRequest();
		request.open("GET", "entry?id=" + id, true);
		request.setRequestHeader("Content-Type", "application/json");

		var outer = this;

		request.onreadystatechange = function() {
			if (request.readyState == 4 && request.status == 200) {
				var result = JSON.parse(request.response);
				if (result.success) {
					outer.showAddEntryModal();
					outer.resetAddEntryModal();

					if (result.kind == "in") {
						outer.aeSelectIncoming();
						outer.trySelect(outer.custToId(result.customer));
					} else {
						outer.aeSelectOutgoing();
						outer.trySelect(outer.catToId(result.category));
					}
					document.getElementById("aeDate").value = result.date;
					document.getElementById("aeTitle").value = result.title;
					outer.trySelect(outer.orgToId(result.originator));

					if (result.amount.indexOf(" ") >= 0) {
						result.amount = result.amount.substring(0, result.amount.indexOf(" "));
					}
					document.getElementById("aeBeforeTax").value = result.amount;
					document.getElementById("aeTax").value = result.taxationPercent;

					if (result.postTaxAmount.indexOf(" ") >= 0) {
						result.postTaxAmount = result.postTaxAmount.substring(0, result.postTaxAmount.indexOf(" "));
					}
					document.getElementById("aeAfterTax").value = result.postTaxAmount;

					outer.currentlyEditing = id;
				}
			}
		}

		request.send();
	},

	paidEntry: function(id) {

		var request = new XMLHttpRequest();
		request.open("GET", "entry?id=" + id, true);
		request.setRequestHeader("Content-Type", "application/json");

		var outer = this;

		request.onreadystatechange = function() {
			if (request.readyState == 4 && request.status == 200) {
				var result = JSON.parse(request.response);
				if (result.success) {
					outer.showPaidModal();

					document.getElementById("paidEntryContainer").innerHTML = result.date + " " + result.title + " " +
						result.amount + " " + result.taxationPercent + "% " + result.postTaxAmount;

					if (result.kind == "in") {
						document.getElementById("paidToFromLabel").innerHTML = "in to";
					} else {
						document.getElementById("paidToFromLabel").innerHTML = "out from";
					}

					if (result.received) {
						outer.paidSelectYepp();
					} else {
						outer.paidSelectNope();
					}

					document.getElementById("paidDate").value = result.receivedOnDate;
					outer.trySelect(outer.accToId(result.receivedOnAccount));

					outer.currentlyPaid = id;
				}
			}
		}

		request.send();
	},

	savePaidModal: function() {

		var request = new XMLHttpRequest();
		request.open("POST", "paidEntry", true);
		request.setRequestHeader("Content-Type", "application/json");

		request.onreadystatechange = function() {
			if (request.readyState == 4 && request.status == 200) {
				var result = JSON.parse(request.response);
				if (result.success) {
					window.location.reload(false);
				}
			}
		}

		var data = {
			"receivedOnDate": document.getElementById("paidDate").value,
			"receivedOnAccount": document.getElementById("paidAccount").value,
			"id": this.currentlyPaid,
		};

		if (document.getElementById("paidYepp").className == "button checked") {
			data.received = true;
		} else {
			data.received = false;
		}

		request.send(JSON.stringify(data));
	},

	showPaidModal: function() {
		var modal = document.getElementById("paidModal");
		if (modal) {
			modal.style.display = "block";

			document.getElementById("modalBackground").style.display = "block";
		}
	},

	closePaidModal: function() {
		var modal = document.getElementById("paidModal");
		if (modal) {
			modal.style.display = "none";
		}

		document.getElementById("modalBackground").style.display = "none";
	},

	deleteEntry: function(id) {

		var request = new XMLHttpRequest();
		request.open("GET", "entry?id=" + id, true);
		request.setRequestHeader("Content-Type", "application/json");

		var outer = this;

		request.onreadystatechange = function() {
			if (request.readyState == 4 && request.status == 200) {
				var result = JSON.parse(request.response);
				if (result.success) {
					outer.showDeleteModal();

					document.getElementById("delTitle").innerHTML = result.title;

					outer.currentlyDeleting = id;
				}
			}
		}

		request.send();
	},

	yesDeleteModal: function() {

		var request = new XMLHttpRequest();
		request.open("POST", "deleteEntry", true);
		request.setRequestHeader("Content-Type", "application/json");

		request.onreadystatechange = function() {
			if (request.readyState == 4 && request.status == 200) {
				var result = JSON.parse(request.response);
				if (result.success) {
					window.location.reload(false);
				}
			}
		}

		var data = {
			"id": this.currentlyDeleting,
		};

		request.send(JSON.stringify(data));
	},

	showDeleteModal: function() {
		var modal = document.getElementById("deleteModal");
		if (modal) {
			modal.style.display = "block";

			document.getElementById("modalBackground").style.display = "block";
		}
	},

	closeDeleteModal: function() {
		var modal = document.getElementById("deleteModal");
		if (modal) {
			modal.style.display = "none";
		}

		document.getElementById("modalBackground").style.display = "none";
	},

	showAddEntryModal: function() {
		var modal = document.getElementById("addEntryModal");
		if (modal) {
			modal.style.display = "block";

			this.currentlyEditing = null;
			this.last_link = null;

			document.getElementById("modalBackground").style.display = "block";
		}
	},

	resetAddEntryModal: function() {

		var DateUtils = toolbox.utils.DateUtils;

		this.aeSelectOutgoing();
		document.getElementById("aeDate").value = DateUtils.serializeDate(DateUtils.now());
		document.getElementById("aeTitle").value = "";
		document.getElementById("aeBeforeTax").value = "";
		document.getElementById("aeTax").value = "";
		document.getElementById("aeAfterTax").value = "";
		this.trySelect(this.orgToId("Moya"));
	},

	closeAddEntryModal: function() {
		var modal = document.getElementById("addEntryModal");
		if (modal) {
			modal.style.display = "none";
		}

		if (this.last_link == null) {
			// reload, as data might have changed while the modal was open...
			window.location.reload(false);
		} else {
			// ... or go to the correct page automagically if we actually made a change while
			// the modal was open!
			window.location = this.last_link;
		}
	},

	submitAddEntryModal: function(closeOnSubmit) {

		var request = new XMLHttpRequest();
		request.open("POST", "addEntry", true);
		request.setRequestHeader("Content-Type", "application/json");

		request.onreadystatechange = function() {
			if (request.readyState == 4 && request.status == 200) {
				var result = JSON.parse(request.response);
				// show some sort of confirmation
				if (result.success) {
					var entrySavedLabel = document.getElementById("entrySavedLabel");
					if (entrySavedLabel) {
						entrySavedLabel.style.display = "block";
						window.setTimeout(function () {
							entrySavedLabel.style.display = "none";
						}, 3000);
					}
					window.accountant.last_link = result.link;

					// we are now editing the NEW entry, no longer the old one!
					window.accountant.currentlyEditing = result.id;

					// if we want to close on submit...
					if (closeOnSubmit) {
						// ... go to the correct page automagically!
						window.accountant.closeAddEntryModal();
					}
				}
			}
		}

		var data = {
			"date": document.getElementById("aeDate").value,
			"title": document.getElementById("aeTitle").value,
			"originator": document.getElementById("aeOriginator").value,
			"amount": document.getElementById("aeBeforeTax").value,
			"taxationPercent": document.getElementById("aeTax").value,
			"postTaxAmount": document.getElementById("aeAfterTax").value,
			"lastTaxChangeWasPreTax": this.lastTaxChangeWasPreTax,
			"id": this.currentlyEditing,
		};

		var outgoing = document.getElementById("aeOutgoing");
		if (outgoing && (outgoing.className == "button checked")) {
			data.kind = "out";
			data.category = document.getElementById("aeCatCust").value;
		} else {
			data.kind = "in";
			data.customer = document.getElementById("aeCatCust").value;
		}

		request.send(JSON.stringify(data));
	},

	submitAndCloseAddEntryModal: function() {
		this.submitAddEntryModal(true);
	},

	aeCalcCategoryBasedOnTitle: function() {
		var title = document.getElementById("aeTitle");
		var outgoing = document.getElementById("aeOutgoing");
		if (title && outgoing) {
			if (outgoing.className == "button checked") {
				var titleStr = title.value;

				var request = new XMLHttpRequest();
				request.open("POST", "calcCategoryBasedOnTitle", true);
				request.setRequestHeader("Content-Type", "application/json");

				var outer = this;

				request.onreadystatechange = function() {
					if (request.readyState == 4 && request.status == 200) {
						var result = JSON.parse(request.response);
						if (result.success) {
							outer.trySelect(outer.catToId(result.category));
						}
					}
				}

				var data = {
					"title": titleStr
				};

				request.send(JSON.stringify(data));
			}
		}
	},

	aeSelectOutgoing: function() {
		var outgoing = document.getElementById("aeOutgoing");
		var incoming = document.getElementById("aeIncoming");
		var label = document.getElementById("aeCatCustLabel");
		if (outgoing && incoming) {
			outgoing.className = "button checked";
			incoming.className = "button unchecked";
		}
		if (label) {
			label.innerHTML = "Category:";
		}
		var cats = "";
		for (var i = 0; i < window.aeCategories.length; i++) {
			cats += "<option id='" + this.catToId(window.aeCategories[i]) + "' " +
				"value='" + window.aeCategories[i] + "'>" +
				window.aeCategories[i] + "</option>";
		}
		document.getElementById("aeCatCust").innerHTML = cats;
	},

	aeSelectIncoming: function() {
		var outgoing = document.getElementById("aeOutgoing");
		var incoming = document.getElementById("aeIncoming");
		var label = document.getElementById("aeCatCustLabel");
		if (outgoing && incoming) {
			outgoing.className = "button unchecked";
			incoming.className = "button checked";
		}
		if (label) {
			label.innerHTML = "Customer:";
		}
		var custs = "";
		for (var i = 0; i < window.aeCustomers.length; i++) {
			custs += "<option id='" + this.custToId(window.aeCustomers[i]) + "' " +
				"value='" + window.aeCustomers[i] + "'>" +
				window.aeCustomers[i] + "</option>";
		}
		document.getElementById("aeCatCust").innerHTML = custs;
	},

	paidSelectYepp: function() {
		var paidYepp = document.getElementById("paidYepp");
		var paidNope = document.getElementById("paidNope");
		if (paidYepp && paidNope) {
			paidYepp.className = "button checked";
			paidNope.className = "button unchecked";
		}
	},

	paidSelectNope: function() {
		var paidYepp = document.getElementById("paidYepp");
		var paidNope = document.getElementById("paidNope");
		if (paidYepp && paidNope) {
			paidYepp.className = "button unchecked";
			paidNope.className = "button checked";
		}
	},

	catToId: function(cat) {
		return this.somethingToId("aeCatOption", cat);
	},

	custToId: function(cust) {
		return this.somethingToId("aeCustOption", cust);
	},

	orgToId: function(org) {
		return this.somethingToId("aeOrgOption", org);
	},

	accToId: function(acc) {
		return this.somethingToId("paidAccOption", acc);
	},

	somethingToId: function(prefix, cat) {
		if (cat == null) {
			return null;
		}
		cat = cat.toUpperCase();
		var index = cat.indexOf(" ");
		if (index < 0) {
			return prefix + cat;
		}
		return prefix + cat.substring(0, index);
	},

	trySelect: function(id) {
		if (id == null) {
			return;
		}
		var el = document.getElementById(id);
		if (el) {
			el.selected = true;
		} else {
			el = document.getElementById(id + "S");
			if (el) {
				el.selected = true;
			}
		}
	},

	aeCalcTax: function() {
		if (this.lastTaxChangeWasPreTax) {
			this.aeCalcPostTax();
		} else {
			this.aeCalcPreTax();
		}
	},

	aeCalcPostTax: function() {
		this.lastTaxChangeWasPreTax = true;

		var request = new XMLHttpRequest();
		request.open("POST", "calcPostTax", true);
		request.setRequestHeader("Content-Type", "application/json");

		request.onreadystatechange = function() {
			if (request.readyState == 4 && request.status == 200) {
				var result = JSON.parse(request.response);
				if (result.success) {
					if (result.postTax == null) {
						result.postTax = "";
					}
					document.getElementById("aeAfterTax").value = result.postTax;
				}
			}
		}

		var data = {
			"preTax": document.getElementById("aeBeforeTax").value,
			"tax": document.getElementById("aeTax").value,
		};

		request.send(JSON.stringify(data));
	},

	aeCalcPreTax: function() {
		this.lastTaxChangeWasPreTax = false;

		var request = new XMLHttpRequest();
		request.open("POST", "calcPreTax", true);
		request.setRequestHeader("Content-Type", "application/json");

		request.onreadystatechange = function() {
			if (request.readyState == 4 && request.status == 200) {
				var result = JSON.parse(request.response);
				if (result.success) {
					if (result.preTax == null) {
						result.preTax = "";
					}
					document.getElementById("aeBeforeTax").value = result.preTax;
				}
			}
		}

		var data = {
			"postTax": document.getElementById("aeAfterTax").value,
			"tax": document.getElementById("aeTax").value,
		};

		request.send(JSON.stringify(data));
	},

}


window.addEventListener("resize", window.accountant.onResize);


window.accountant.onResize();

var orgs = "";
for (var i = 0; i < window.aeOriginators.length; i++) {
	orgs += "<option id='" + window.accountant.orgToId(window.aeOriginators[i]) + "' " +
		"value='" + window.aeOriginators[i] + "'>" +
		window.aeOriginators[i] + "</option>";
}
document.getElementById("aeOriginator").innerHTML = orgs;

var accs = "";
for (var i = 0; i < window.paidAccounts.length; i++) {
	accs += "<option id='" + window.accountant.accToId(window.paidAccounts[i]) + "' " +
		"value='" + window.paidAccounts[i] + "'>" +
		window.paidAccounts[i] + "</option>";
}
document.getElementById("paidAccount").innerHTML = accs;

window.accountant.resetAddEntryModal();
