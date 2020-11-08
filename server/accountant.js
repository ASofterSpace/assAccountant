window.accountant = {

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

}


window.addEventListener("resize", window.accountant.onResize);


window.accountant.onResize();
