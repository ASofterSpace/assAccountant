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
						document.getElementById("aeCatCust").innerHTML = "<option>" + result.customer  + "</option>";
					} else {
						outer.aeSelectOutgoing();
						document.getElementById("aeCatCust").innerHTML = "<option>" + result.category + "</option>";
					}
					document.getElementById("aeDate").value = result.date;
					document.getElementById("aeTitle").value = result.title;
					document.getElementById("aeOriginator").innerHTML = "<option>" + result.originator + "</option>";
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
		document.getElementById("aeCatCust").innerHTML = "";
		document.getElementById("aeOriginator").innerHTML = "";
		document.getElementById("aeBeforeTax").value = "";
		document.getElementById("aeTax").value = "";
		document.getElementById("aeAfterTax").value = "";
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
	},

}


window.addEventListener("resize", window.accountant.onResize);


window.accountant.onResize();
