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
			// 2016.8.10 QiaoMingkui: only use 1 port
			PORTS = [ 20052/*, 26126, 22862*/ ],
			OPERATORS = {
				echoBack               : 1,
				fileBrowser            : 2,
				fileTransfer           : 3,
				runApp                 : 4,
				versionInfo            : 5,
				remoteDesktop          : 6,
				fileOperator           : 7
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
			_initializing = false,
			latestVersion = {
				ver : '1.0',
				compatibility : '1.0' // inclusive
			},
			port          = -1,
			url           = null;

	// (private)
	function init(errorHandler) {
		if (_init || _initializing) return;

		_initializing = true;

		var i = 0;
		var _detectXeXtension = function() {
			if (i < PORTS.length) {
				port = PORTS[i++];
//				url = 'https://localhost:' + port + '/?jsoncallback=?';
				url = 'http://localhost:' + port + '/?jsoncallback=?';
				sendRequest(OPERATORS.versionInfo, null, null, function(resp, status, xhr, ex) {
					if (resp && resp.returnCode === RETURN_CODES.OPERATION_SUCCEED) {
						var verInfo = resp.extraData;
						if (verInfo && verInfo.name === NAME) {
							_init = true;
						} else {
							_detectXeXtension();
						}
					} else {
						_detectXeXtension();
					}
				});
			} else {
				_initializing = false;
				if (errorHandler) {
					errorHandler();
				}
			}
		};

		_detectXeXtension();
	}

	// (private)
	function _checkInit() {
		if (!_init) {
			if (!_initializing) {
				init(function() {
					Xfinity.message.alert(
						'此操作需要XeXtension，<a href="download/XfinityExtension.zip" target="_blank">下载</a>（无JRE，5.0MB），'
								+ '<a href="download/XfinityExtensionWithJRE.zip" target="_blank">下载</a>（含JRE，47.4MB）。'
								+ '安装遇到问题？查看《<a href="help/xextension-user-guide.jsp" target="_blank">安装与使用帮助</a>》',
						'提示'
					);
				});
			}
			return false;
		}

		return true;
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
		if (!_checkInit()) return;

		sendRequest(OPERATORS.versionInfo, null, null, _genDefReqHandler(callback, scope));
	}

	/**
	 * just for testing and debugging. 
	 * 
	 * @param params (optional) extra request parameters
	 * @param callback
	 * @param scope
	 */
	function echoBack(params, callback, scope) {
		if (!_checkInit()) return;

		sendRequest(OPERATORS.echoBack, null, params, function(resp, status, jqXHR, ex) {
			if (_isFunction(callback)) {
				var success = resp && resp.returnCode === RETURN_CODES.OPERATION_SUCCEED;
				resp = resp || {};
				var errorMsg = resp.exception || (ex && ex.message) || 'unknown';
				callback.call(scope, success, resp, errorMsg);
			}
		});
	}

	/**
	 * show a file browser dialog to let user choose local files. 
	 *  
	 * @param params (optional) extra request parameters
	 * @param callback
	 * @param scope
	 */
	function fileBrowser(params, callback, scope) {
		if (!_checkInit()) return;

		var respId;
		var _filesSelected = function(resp, status, jqXHR, ex) {
			if (!resp) {
				respId = null;
				if (_isFunction(callback)) {
					resp = resp || {};
					errorMsg = resp.exception || (ex && ex.message) || 'error occurs when send filebrowser request';
					callback.call(scope, false, resp.extraData, errorMsg);
				}
				return;
			}

			respId = resp.respId;
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
					Xfinity.message.alert('request failed!');
					break;
			}
		};

		sendRequest(OPERATORS.fileBrowser, null, params, _filesSelected);

		// return an deferred object
		var deferred = $.Deferred();
		deferred.cancel = function(callback, scope) {
			if (!respId) {
				if (_isFunction(callback)) {
					callback.call(scope, false, null, 'browser does not open');
				}
			} else {
				sendRequest(OPERATORS.fileBrowser, respId, {
					type : 'cancel'
				}, _genDefReqHandler(callback, scope));
			}
		};

		return deferred;
	}

	/**
	 * transfer files between local and clusters (upload and download). 
	 *  
	 * @param params (optional) extra request parameters
	 * @param callback
	 * @param scope
	 */
	function fileTransfer(params, callback, scope) {
		if (!_checkInit()) return;

		var respId;
		var genRequestHandler = function(callback, scope) {
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
				/*
				 * 目前先在服务端保存这些数据，请求时只要带上 reqId 即可
				 */
				sendRequest(OPERATORS.fileTransfer, respId, /*params*/{
					type : 'transferProgress'
				}, genRequestHandler(callback, scope));
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
		if (!_checkInit()) return;

		sendRequest(OPERATORS.runApp, null, params, _genDefReqHandler(callback, scope));
		// TODO return an object
	}

	function remoteDesktop(params, callback, scope) {
		if (!_checkInit()) return;

		sendRequest(OPERATORS.remoteDesktop, null, params, _genDefReqHandler(callback, scope));
		// TODO return an object
	}

	/**
	 * A set of local file operators, such as get the file size.
	 * 
	 * @param params (optional) extra request parameters
	 * @param callback
	 * @param scope
	 */
	function fileOperator(params, callback, scope) {
		if (!_checkInit()) return;

		sendRequest(OPERATORS.fileOperator, null, params, _genDefReqHandler(callback, scope));
	}

	init();

	var pluginManager = {
		isInit          : isInit,
		versionInfo     : versionInfo,
		echoBack        : echoBack,
		fileBrowser     : fileBrowser,
		fileTransfer    : fileTransfer,
		runApp          : runApp,
		remoteDesktop   : remoteDesktop,
		fileOperator    : fileOperator
	};

	return pluginManager;
})();
