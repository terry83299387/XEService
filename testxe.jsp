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
<button id="close_filebrowser">close File Browser</button>
<br/><br/>
<button id="filetransfer">File Transfer</button>&nbsp;&nbsp;
<button id="runapp">Run App</button>&nbsp;&nbsp;
<button id="remotedesktop">Remote Desktop</button>&nbsp;&nbsp;
<button id="getfilesize">Get File Size</button>&nbsp;&nbsp;

<div id="result" style="margin: 20px 0px;">&nbsp;</div>


<script type="text/javascript" src="lib/jquery/jquery-1.11.1.min.js"></script>
<script type="text/javascript" src="js/message.js"></script>
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

	var fileBrowser;

	$('#versioninfo').on('click', function() {
		PluginManager.versionInfo(showVersionInfo);
	});

	$('#echoback').on('click', function() {
		PluginManager.echoBack(null, showEchoBack);
	});

	$('#filebrowser').on('click', function() {
		fileBrowser = PluginManager.fileBrowser(null, getSelectedFile);
	});

	$('#filebrowser_multi').on('click', function() {
		fileBrowser = PluginManager.fileBrowser(
			{
				multi : true,
				defaultDir : 'd:\\'
			},
			getSelectedFile);
	});

	$('#close_filebrowser').on('click', function() {
		if (fileBrowser) {
			fileBrowser.cancel();
			fileBrowser = null;
		}
	});

	$('#filetransfer').on('click', function() {
		PluginManager.fileTransfer({
			host        : '192.168.120.219',
			port        : '31022',
			user        : '4da8227d4fc1b6be',
			passwd      : 'af226407c556532664cc7605398d978c',
			clientKey   : '561tv4r3',
			protocol    : 'Sftp',
			serverName  : '蜂鸟LinuxHPC',
			files       : 'd:/ipconfig.png',
			home        : '/home/linux/users/rdtest/jieliu/_eojfei000',
			type        : 'upload',
			module      : 'job',
			rootPath    : '/home/linux/users/rdtest/jieliu',
			defaultPath : '/home/linux/users/rdtest/jieliu',
			enableExtend: false,
			portalUser  : 'jieliu',
			serverClass : 'SftpTool',
			language    : 'zh_CN'
		}, fileTransferResult)
	});

	$('#runapp').on('click', function() {
		PluginManager.runApp({
			appName : 'kitty.exe',
			clientKey : '5ofw2v6m',
			server    : '192.168.120.219',
			userName  : '236076e1c5a0e618',
			password  : '4c06b98ad3074cfe2d07ae1983f047df',
			initCd    : '/home/linux/users/rdtest/mkqiao',
			port      : '31022'
		}, runAppResult);
	});

	$('#remotedesktop').on('click', function() {
		PluginManager.remoteDesktop({
			"connectAddr": "xfinity.net.cn",
			"connectPort": 6000,
			"clientkey": "vlf73hu0",
			"hostUserName": "b234ca3a71d24747",
			"password": "2ae2fb1d08a33b88374be83bfb52495a",
			"appName": "CFX",
			"appVersion": "11.0",
			"appInitParams": "",
			"serverInitParams": " -depth 24 -geometry 1440x900",
			"acquirement": "A1",
			"clusterName": "蜂鸟LinuxHPC",
			"displayUserName": "testuser2",
			"workDir": "/home/linux/users/rdtest/testuser2"
		}, remoteDesktopResult, null);
	});

	$('#getfilesize').on('click', function() {
		var prettyFileSize = function(size) {
			size = +size;
			if (size < 1024) {
				return '';
			}
			if (size < 1024 * 1024) {
				return ' (' + (size / 1024).toFixed(2) + 'KB)';
			}
			if (size < 1024 * 1024 * 1024) {
				return ' (' + (size / 1024 / 1024).toFixed(2) + 'MB)';
			}
			if (size < 1024 * 1024 * 1024 * 1024) {
				return ' (' + (size / 1024 / 1024 / 1024).toFixed(2) + 'GB)';
			}
		};

		// 1. select a file
		PluginManager.fileBrowser(null, function(success, data, ex) {
			if (!success) {
				showError('<p style="color:red;">操作无法完成，原因：<br>' + ex + '</p>');
				return;
			}
			var file = data.selectedFiles;
			if (!file) {
				showError('<p style="color:red;">操作无法完成，未选择任何文件</p>');
				return;
			}

			// 2. get file size
			PluginManager.fileOperator({
				type : 'fileSize',
				file : file
			}, function(success, data, ex) {
				if (!success) {
					showError('<p style="color:red;">操作无法完成，原因：<br>' + ex + '</p>');
					return;
				}
				$('#result').html('<p>file: ' + file + '<br>size: ' + data.size + 'B' + prettyFileSize(data.size) + '</p>');
			});
		});
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
		fileBrowser = null;

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
		$('#result').html('<h1>request failed</h1>'
			+ '<p style="color:red;">error msg: ' + ex + '</p>');
	}
</script>

</body>
</html>
