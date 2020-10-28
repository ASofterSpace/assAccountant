window.accountant = {

	onResize: function() {
		var body = document.getElementById("body");
		if (body) {
			body.style.height = window.innerHeight + "px";
		}
	},

}


window.addEventListener("resize", window.accountant.onResize);


window.accountant.onResize();
