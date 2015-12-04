/**
 * plugin manager.
 * 
 * To use this PluginManager, following additional variables are needed:
 * 
 * currentUserName : file transfer needs this global variable
 * language : file transfer needs this global variable
 * 
 * All APIs include callback and scope parameters:
 * 
 * (1) callback: (function, optional) callback being called while request either succeeds or fails.
 * 
 * 		following parameters will be passed through when callback is called:
 *
 * 			success: (boolean) true if request succeeds, false does not
 * 			data: (object) responsed data
 * 			ex: (string) error message, if error occurs when send request.
 * 
 * (2) scope: (object, optional) the scope of callback call (callback's "this" object)
 * 
 * @type PluginManager
 * @author qiaomingkui
 */
var PluginManager = (function() {
	var NAME  = 'XeXtension',
			PORTS = [ 20052, 26126, 22862 ],
			OPERATORS = {
				echoBack               : 1,
				fileBrowser            : 2,
				fileTransfer           : 3,
				runApp                 : 4,
				versionInfo            : 5,
				remoteDesktop          : 6
			},
			RETURN_CODES = {
				OPERATION_SUCCEED      : 0,
				OPERATION_UNCOMPLETED  : 1,
				UNKNOWN_ERROR          : -1,
				UNKNOWN_OPERATOR       : 101,
				UNSUPPORTED_METHOD     : 102,
				UNKNOWN_ID             : 103,
				UNSUPPORT_OPERATION    : 104
			};

	var _init    		  = false,
			latestVersion = {
				ver : '1.0',
				compatibility : '1.0' // inclusive
			},
			port          = -1,
			url           = null;

	// (private)
	function init() {
		function callback(resp, status, xhr, ex) {
			if (resp && resp.returnCode === RETURN_CODES.OPERATION_SUCCEED) {
				var verInfo = resp.extraData;
				if (verInfo && verInfo.name === NAME) {
					_init = true;
				} else {
					_sendRequest();
				}
			} else {
				_sendRequest();
			}
		}

		var i = 0;
		function _sendRequest() {
			if (i < PORTS.length) {
				port = PORTS[i];
				url = 'http://localhost:' + port + '/?jsoncallback=?';
				i++;
				sendRequest(OPERATORS.versionInfo, null, null, callback);
			} else {
				alert('It seems XeXtension hasn\'t been installed on your system'); // TODO show error info and download url
			}
		}

		_sendRequest();
	}

	// (private)
	/*
	 * @param id (optional) request id
	 * 		If it is a continuous request (e.g.: query uploading progress),
	 * 		the request id will be set to the response id of last request.
	 */
	function sendRequest(op, id, params, callback) {
		var data = {
			op : op
		};
		if (id != null) data.reqId = id;
		if (params) {
			for (var k in params) {
				data[k] = params[k];
			}
		}

		// send request
		var jqXHR = $.getJSON(url, data, callback);

		jqXHR.fail(function(jqXHR, status, ex) {
			/*
			 * in ie8, if service is down, the arguments will be:
			 * 
			 * status: 'parsererror'
			 * ex.description: 'xxxx was not called' (xxxx is the name of jsoncallback function)
			 * ex.message: (same as ex.description)
			 * ex.name: 'Error'
			 * 
			 * Note: in ie8, jquery will do those clean jobs when request failed
			 */
			callback(null, status, jqXHR, ex);
		});

		// in chrome and some other browsers, if service is down, the jqXHR's fail callback won't be called
		// in other words, getJSON request failed silently.
		// thus we must capture errors when request failed in this way.
		// (JSONP in IE 8 succeeds even though service is down and thus request is failed)
		var head = document.head || $('head')[0] || document.documentElement; // copy from jquery
		var script = $(head).find('script')[0];
		var tOnError = script.onerror;
		script.onerror = function(evt) {
			// do clean
			// delete script node
			if (script.parentNode) {
				script.parentNode.removeChild(script);
			}
			// delete jsonCallback global function
			var src = script.src || '';
			var idx = src.indexOf('jsoncallback=');
			if (idx != -1) {
				var idx2 = src.indexOf('&');
				if (idx2 == -1) {
					idx2 = src.length;
				}
				var jsonCallback = src.substring(idx + 13, idx2);
				if (jsonCallback) {
					delete window[jsonCallback];
				}
			}

			// 
			callback(null, 'error', jqXHR, {
				message: 'error occurs when send request'
			});
		};
	}

	// (private) generate default request handler
	function _genDefReqHandler(callback, scope) {
		return function(resp, status, jqXHR, ex) {
			if (_isFunction(callback)) {
				var success = resp && resp.returnCode === RETURN_CODES.OPERATION_SUCCEED;
				resp = resp || {};
				var errorMsg = resp.exception || (ex && ex.message) || 'unknown';
				callback.call(scope, success, resp.extraData, errorMsg);
			}
		};
	}

	// (private)
	function _isFunction(func) {
		return typeof(func) === 'function';
	}

	// (private)
	function _checkInit(callback, scope) {
		if (!_init) {
			if (_isFunction(callback)) {
				callback.call(scope, null, 'error', null, {
					message     : 'PluginManager has not been initialized'
				});
			}
			return false;
		}

		return true;
	}

	/**
	 * return if PluginManager has been initialized.
	 */
	function isInit() {
		return _init;
	}

	/**
	 * get version info of XeXtension. 
	 * 
	 * @param callback
	 * @param scope
	 */
	function versionInfo(callback, scope) {
		if (!_checkInit(callback, scope)) return;

		sendRequest(OPERATORS.versionInfo, null, null, _genDefReqHandler(callback, scope));
		// TODO return an object
	}

	/**
	 * just for testing and debugging. 
	 * 
	 * @param params (optional) extra request parameters
	 * @param callback
	 * @param scope
	 */
	function echoBack(params, callback, scope) {
		if (!_checkInit(callback, scope)) return;

		sendRequest(OPERATORS.echoBack, null, params, function(resp, status, jqXHR, ex) {
			if (_isFunction(callback)) {
				var success = resp && resp.returnCode === RETURN_CODES.OPERATION_SUCCEED;
				resp = resp || {};
				var errorMsg = resp.exception || (ex && ex.message) || 'unknown';
				callback.call(scope, success, resp, errorMsg);
			}
		});
		// TODO return an object
	}

	/**
	 * show a file browser dialog to let user choose local files. 
	 *  
	 * @param params (optional) extra request parameters
	 * @param callback
	 * @param scope
	 */
	function fileBrowser(params, callback, scope) {
		if (!_checkInit(callback, scope)) return;
		
		function _filesSelected(resp, status, jqXHR, ex) {
			if (!resp) {
				if (_isFunction(callback)) {
					resp = resp || {};
					errorMsg = resp.exception || (ex && ex.message) || 'error occurs when send filebrowser request';
					callback.call(scope, false, resp.extraData, errorMsg);
				}
				return;
			}

			var respId = resp.respId;
			var returnCode = resp.returnCode;
			switch (returnCode) {
				case RETURN_CODES.OPERATION_SUCCEED:
				case RETURN_CODES.UNKNOWN_ID:
					if (_isFunction(callback)) {
						var success = resp.returnCode === RETURN_CODES.OPERATION_SUCCEED;
						var errorMsg = resp.exception || (ex && ex.message) || 'unknown';
						callback.call(scope, success, resp.extraData, errorMsg);
					}
					break;
				case RETURN_CODES.OPERATION_UNCOMPLETED:
					setTimeout(function() {
						sendRequest(OPERATORS.fileBrowser, respId, params, _filesSelected);
					}, 0);
					break;
				case RETURN_CODES.UNSUPPORT_OPERATION:
				default:
					// these cases should not happen
					alert('request failed!');
					break;
			}
		}

		sendRequest(OPERATORS.fileBrowser, null, params, _filesSelected);
		// TODO return an object
	}

	/**
	 * TODO transfer files between local and clusters (upload and download). 
	 *  
	 * @param params (optional) extra request parameters
	 * @param callback
	 * @param scope
	 */
	function fileTransfer(params, callback, scope) {
		if (!_checkInit(callback, scope)) return;

		var respId;
		function genRequestHandler(callback, scope) {
			return function(resp, status, jqXHR, ex) {
				var success = resp && resp.returnCode === RETURN_CODES.OPERATION_SUCCEED;
				resp = resp || {};
				respId = resp.respId;
				if (_isFunction(callback)) {
					var errorMsg = resp.exception || (ex && ex.message) || 'unknown';
					callback.call(scope, success, resp.extraData, errorMsg);
				}
			}
		};
		sendRequest(OPERATORS.fileTransfer, null, params, genRequestHandler(callback, scope));

		// return an deferred object
		var deferred = $.Deferred();
		deferred.progress = function(callback, scope) {
			if (!respId) {
				if (_isFunction(callback)) {
					callback.call(scope, false, null, 'transfer does not start');
				}
			} else {
				sendRequest(OPERATORS.fileTransfer, respId, null, genRequestHandler(callback, scope));
			}
		};

		return deferred;
	}

	/**
	 * start and run a local program. 
	 *  
	 * @param params (optional) extra request parameters
	 * @param callback
	 * @param scope
	 */
	function runApp(params, callback, scope) {
		if (!_checkInit(callback, scope)) return;

		sendRequest(OPERATORS.runApp, null, params, _genDefReqHandler(callback, scope));
		// TODO return an object
	}

	function remoteDesktop(params, callback, scope) {
		if (!_checkInit(callback, scope)) return;

		sendRequest(OPERATORS.remoteDesktop, params, _genDefReqHandler(callback, scope));
		// TODO return an object
	}

	init();

	return {
		isInit          : isInit,
		versionInfo     : versionInfo,
		echoBack        : echoBack,
		fileBrowser     : fileBrowser,
		fileTransfer    : fileTransfer,
		runApp          : runApp,
		remoteDesktop   : remoteDesktop
	};
})();
