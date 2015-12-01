<%@page language="java" pageEncoding="UTF-8"%>

<html>
<head>
<meta charset="utf-8" />
<title>Test XEService</title>
<!--[if lt IE 9]>
  <script src="lib/ie-support/html5shiv.min.js"></script>
  <script src="lib/ie-support/respond.min.js"></script>
<![endif]-->
</head>

<body style="margin:15px;">
<button id="versioninfo">Version Info</button>&nbsp;&nbsp;
<button id="echoback">Echo Back</button>&nbsp;&nbsp;
<button id="filebrowser">File Browser</button>&nbsp;&nbsp;
<button id="filebrowser_multi">File Browser (Multiple)</button>&nbsp;&nbsp;
<button id="filetransfer">File Transfer</button>&nbsp;&nbsp;
<button id="runapp">Run App</button>&nbsp;&nbsp;

<div id="result" style="margin: 20px 0px;">&nbsp;</div>


<script type="text/javascript" src="lib/jquery/jquery-1.11.1.min.js"></script>
<script type="text/javascript">
	var OPERATORS = {
		echoBack               : 1,
		fileBrowser            : 2,
		fileTransfer           : 3,
		runApp                 : 4,
		versionInfo            : 5
	};

	var RETURN_CODES = {
		OPERATION_SUCCEED      : 0,
		OPERATION_UNCOMPLETED  : 1,
		UNKNOWN_ERROR          : -1,
		UNKNOWN_OPERATOR       : 101,
		UNSUPPORTED_METHOD     : 102,
		UNKNOWN_ID             : 103,
		UNSUPPORT_OPERATION    : 104
	};

	$('#versioninfo').on('click', function() {
		sendRequest(OPERATORS.versionInfo, null, null, showVersionInfo);
	});

	$('#echoback').on('click', function() {
		sendRequest(OPERATORS.echoBack, null, null, showEchoBack);
	});

	$('#filebrowser').on('click', function() {
		sendRequest(OPERATORS.fileBrowser, null, null, getSelectedFile);
	});

	$('#filebrowser_multi').on('click', function() {
		sendRequest(OPERATORS.fileBrowser,
			null,
			{
				multi : true,
				defaultDir : 'd:\\'
			},
			getSelectedFile);
	});

	$('#filetransfer').on('click', function() {
		$('#result').html('<p style="color:red;">does not implement yet.</p>');
	});

	$('#runapp').on('click', function() {
		sendRequest(OPERATORS.runApp, null, {
			appName : 'kitty.exe',
			args    : '-l rdtest -pw liujie02 192.168.239.10 -cmd "cd mkqiao"'
		}, runAppResult);
	});

	function sendRequest(op, id, extraParams, callback) {
		var data = {
			op : op
		};
		if (id) data.reqId = id;
		if (extraParams) {
			for (var k in extraParams) {
				data[k] = extraParams[k];
			}
		}

		var a = $.getJSON('http://localhost:20052/?jsoncallback=?', data, callback);
		// request failed
		a.fail(function(jqXHR, textStatus, ex) {
			// TODO

			/*
			 * in ie, if service is down, the arguments will be:
			 * 
			 * testStatus: 'parsererror'
			 * ex.description: 'xxxx was not called' (xxxx is the name of jsoncallback function)
			 * ex.message: (same as ex.description)
			 * ex.name: 'Error'
			 */
			alert('failed');
		});

		var head = document.head || $('head')[0] || document.documentElement;
		var script = $(head).find('script')[0];

		// chrome
		var tOnError = script.onerror;
		script.onerror = function(evt) {
			alert('error');

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
				delete window[jsonCallback];
			}
			// clear console
			if (typeof console !== 'undefined' && typeof console.clear === 'function') {
				console.clear();
			}
		};
	}

	function showVersionInfo(response) {
		if (!response || response.returnCode !== RETURN_CODES.OPERATION_SUCCEED) {
			showError(response);
			return;
		}

		var info = response.extraData;
		$('#result').html('<h1>Version Info:</h1>'
			+ 'software name: ' + info.name + '<br>'
			+ 'version: ' + info.version + '<br>'
			+ 'copyright: ' + info.copyright + '<br>'
		);
	}

	function showEchoBack(response) {
		//console.log('request done, here is the response:');
		//console.log(response);

		if (!response || response.returnCode !== RETURN_CODES.OPERATION_SUCCEED) {
			showError(response);
			return;
		}

		var extraData = response.extraData;
		var tab = '&nbsp;&nbsp;&nbsp;&nbsp;';
		var str = '<p><b>response info</b><br>'
			+ tab + 'returnCode:' + response.returnCode + '<br>'
			+ tab + 'respId:' + response.respId + '<br>'
			+ tab + 'exception:' + (response.exception || '') + '</p>'
			+ '<p><b>request info</b><br>'
			+ tab + 'op:' + response.op + '<br>'
			+ tab + 'reqId:' + response.reqId + '<br>';

		var parameters = extraData.parameters;
		str += tab + 'other parameters:<br>'
		for (var name in parameters) {
			str += tab + tab + name + ':' + parameters[name] + '<br>';
		}
		str += '</p>';

		str += '<p><b>HTTP request data</b><br>'
			+ tab + extraData.method + '&nbsp;'
			+ extraData.url + '&nbsp;'
			+ extraData.version + '<br>';

		var headers = extraData.headers;
		for (var name in headers) {
			str += tab + name + ':' + headers[name] + '<br>';
		}
		str += '</p>';

		$('#result').html('<h1>request done</h1>' + str);
	}

	function getSelectedFile(response) {
		if (!response) return;

		var id = response.respId;
		var returnCode = response.returnCode;
		switch (returnCode) {
			case RETURN_CODES.OPERATION_SUCCEED:
				var data = response.extraData || {};
				var str = data.selectedFiles || '未选择任何文件';
				$('#result').html('<h1>select files:</h1>' + str.replace(/\|/g, '<br>'));
				break;
			case RETURN_CODES.OPERATION_UNCOMPLETED:
				setTimeout(function() {
					sendRequest(OPERATORS.fileBrowser, id, null, getSelectedFile);
				}, 100);
				break;
			case RETURN_CODES.UNSUPPORT_OPERATION:
				$('#result').html('<p style="color:red;">操作无法完成，原因：<br>' + response.exception + "</p>");
				break;
			case RETURN_CODES.UNKNOWN_ID:
			default:
				break;
		}
	}

	function runAppResult(response) {
		if (response && response.returnCode === RETURN_CODES.OPERATION_SUCCEED) {
			$('#result').html('<p>程序已启动</p>');
		} else {
			showError(response);
		}
	}

	function showError(response) {
		response = response || {};

		var returnCode = response.returnCode || 'no return code';
		var msg = response.exception || 'unknown exception';

		$('#result').html('<h1>request failed</h1>'
			+ '<p style="color:red;">returnCode: ' + returnCode + '<br>'
			+ 'error msg: ' + msg + '</p>');
	}

	/*$.getScript("http://localhost:37925/?callback=testcallback",
		function(data) {
			console.log('load script done');
		}
	);*/
</script>

</body>
</html>
