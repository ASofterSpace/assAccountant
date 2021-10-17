window.accountant = {

	// id of the entry we are currently editing
	currentlyEditing: null,

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
					document.getElementById("aeBeforeTax").value = result.amount;
					document.getElementById("aeTax").value = result.taxationPercent;
					document.getElementById("aeAfterTax").value = result.postTaxAmount;
				}
			}
		}

		request.send();
	},

	showAddEntryModal: function() {
		var modal = document.getElementById("addEntryModal");
		if (modal) {
			modal.style.display = "block";

			this.currentlyEditing = null;

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

		// reload, as data might have changed while the modal was open...
		window.location.reload(false);
	},

	submitAddEntryModal: function() {

	},

	submitAndCloseAddEntryModal: function() {
		this.submitAddEntryModal();
		this.closeAddEntryModal();
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
			cats += "<option id='" + this.catToId(window.aeCategories[i]) + "'>" +
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
			custs += "<option id='" + this.custToId(window.aeCustomers[i]) + "'>" +
				window.aeCustomers[i] + "</option>";
		}
		document.getElementById("aeCatCust").innerHTML = custs;
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

	somethingToId: function(prefix, cat) {
		cat = cat.toUpperCase();
		var index = cat.indexOf(" ");
		if (index < 0) {
			return prefix + cat;
		}
		return prefix + cat.substring(0, index);
	},

	trySelect: function(id) {
		var el = document.getElementById(id);
		if (el) {
			el.selected = true;
		}
	},

}


window.addEventListener("resize", window.accountant.onResize);


window.accountant.onResize();

var orgs = "";
for (var i = 0; i < window.aeOriginators.length; i++) {
	orgs += "<option id='" + window.accountant.orgToId(window.aeOriginators[i]) + "'>" +
		window.aeOriginators[i] + "</option>";
}
document.getElementById("aeOriginator").innerHTML = orgs;

window.accountant.resetAddEntryModal();
