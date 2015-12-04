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
<button id="remotedesktop">Remote Desktop</button>&nbsp;&nbsp;

<div id="result" style="margin: 20px 0px;">&nbsp;</div>


<script type="text/javascript" src="lib/jquery/jquery-1.11.1.min.js"></script>
<script type="text/javascript" src="js/PluginManager.js"></script>
<script type="text/javascript">
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
		PluginManager.versionInfo(showVersionInfo);
	});

	$('#echoback').on('click', function() {
		PluginManager.echoBack(null, showEchoBack);
	});

	$('#filebrowser').on('click', function() {
		PluginManager.fileBrowser(null, getSelectedFile);
	});

	$('#filebrowser_multi').on('click', function() {
		PluginManager.fileBrowser(
			{
				multi : true,
				defaultDir : 'd:\\'
			},
			getSelectedFile);
	});

	$('#filetransfer').on('click', function() {
		PluginManager.fileTransfer({
			files   : 'd:/ipconfig.png',
			home    : '/home/linux/users/rdtest/jieliu/_eojfei000',
			dlgtype : 'Upload',
			module  : 'job',
			rootpath: '/home/linux/users/rdtest/jieliu',
			defaultpath : '/home/linux/users/rdtest/jieliu',
		}, fileTransferResult)
	});

	$('#runapp').on('click', function() {
		PluginManager.runApp({
			appName : 'kitty.exe',
			args    : '-l rdtest -pw liujie02 192.168.239.10 -cmd "cd mkqiao"'
		}, runAppResult);
	});

	$('#remotedesktop').on('click', function() {
		PluginManager.remoteDesktop(null, {
		}, remoteDesktopResult);
	});

	function showVersionInfo(success, data, ex) {
		if (!success) {
			showError(ex);
			return;
		}

		$('#result').html('<h1>Version Info:</h1>'
			+ 'software name: ' + data.name + '<br>'
			+ 'version: ' + data.version + '<br>'
			+ 'copyright: ' + data.copyright + '<br>'
		);
	}

	function showEchoBack(success, resp, ex) {
		if (!success) {
			showError(ex);
			return;
		}

		var extraData = resp.extraData;
		var tab = '&nbsp;&nbsp;&nbsp;&nbsp;';
		var str = '<p><b>resp info</b><br>'
			+ tab + 'returnCode:' + resp.returnCode + '<br>'
			+ tab + 'respId:' + resp.respId + '<br>'
			+ tab + 'exception:' + (resp.exception || '') + '</p>'
			+ '<p><b>request info</b><br>'
			+ tab + 'op:' + resp.op + '<br>'
			+ tab + 'reqId:' + resp.reqId + '<br>';

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

	function getSelectedFile(success, data, ex) {
		if (!success) {
			showError('<p style="color:red;">操作无法完成，原因：<br>' + ex + '</p>');
			return;
		}

		var files = data.selectedFiles || '未选择任何文件';
		$('#result').html('<h1>select files:</h1>' + files.replace(/\|/g, '<br>'));
	}

	function runAppResult(success, data, ex) {
		if (!success) {
			showError(ex);
			return;
		}

		$('#result').html('<p>程序已启动</p>');
	}

	function remoteDesktopResult(success, data, ex) {
		if (!success) {
			showError(ex);
			return;
		}

		$('#result').html('<p>远程桌面已启动</p>');
	}

	function fileTransferResult(success, data, ex) {
		if (!success) {
			showError(ex);
			return;
		}

		$('#result').html('<p>文件开始传输</p>');
	}

	function showError(ex) {
		var msg = resp.exception || 'unknown exception';

		$('#result').html('<h1>request failed</h1>'
			+ '<p style="color:red;">error msg: ' + msg + '</p>');
	}
</script>

</body>
</html>
