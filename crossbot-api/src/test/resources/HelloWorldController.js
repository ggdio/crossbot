(function(scope, controller){

	function fn_request(request) {
		controller.sendText(request, {
			"text": "Please choose an ITEM from the list below:",
			"menu": [
				{ text: "Item A", data: "0"},
				{ text: "Item B", data: "1"},
				{ text: "Item C", data: "2"},
				{ text: "Item D", data: "3"}
			]
		});
	}

	function fn_callback(callback) {
		var data = getCallbackData(callback);
		var collectorId = data[0];

		controller.editText(request, "Done [itemId=" + collectorId + "].");
	}
	
	function fn_method() {
		return "/helloworld";
	}
	
	if (typeof module !== 'undefined' && typeof module.exports !== 'undefined') {
		module.exports = scope;
	}

	scope.request = fn_request;
	scope.callback = fn_callback;
	scope.method = fn_method;

	return scope;

})({}, controller);