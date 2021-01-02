window.accountant = {

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

}


window.addEventListener("resize", window.accountant.onResize);


window.accountant.onResize();
