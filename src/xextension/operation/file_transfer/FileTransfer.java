/**
 * 
 */
package xextension.operation.file_transfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xextension.global.Configurations;
import xextension.global.IDGenerator;
import xextension.http.Request;
import xextension.http.Response;
import xextension.operation.OperationResult;
import xextension.operation.Processor;
import cn.net.xfinity.applet.ftp.client.AppletProviderFasade;
import cn.net.xfinity.applet.ftp.client.FtpClientApplet;

/**
 * @author QiaoMingkui
 *
 */
public class FileTransfer extends Processor {
	private static final Logger	logger = LogManager.getLogger(FileTransfer.class);

	private static FtpClientApplet client;
	private static Map<String, Map<String, String>> params = new HashMap<String, Map<String, String>>();

	public void doGet(Request request, Response response) throws Exception {
		this.doPost(request, response);
	}

	public void doPost(Request request, Response response) throws Exception {
		String reqId = request.getParameter(Configurations.REQUEST_ID);
		OperationResult result = null;
		if (reqId == null) {
			result = _doTransfer(request, response);
		} else {
			result = _getProgress(request);
		}

		response.print(result.toJsonString());
		response.flush();
	}

	private OperationResult _doTransfer(Request request, Response response) throws Exception {
		String[] default_args = {
			"host=192.168.120.219",
			"user=4da8227d4fc1b6be",
			"passwd=af226407c556532664cc7605398d978c",
//			"files=d:/ipconfig.png",
//			"home=/home/linux/users/rdtest/jieliu/_eojfei000",
//			"dlgtype=Upload",
//			"module=job",
//			"rootpath=/home/linux/users/rdtest/jieliu",
//			"defaultpath=/home/linux/users/rdtest/jieliu",
			"port=31022",
			"fileTransferProtocol=Sftp",
			"servername=蜂鸟LinuxHPC",
			"clientkey=561tv4r3",
			"enableextend=true",
			"portaluser=jieliu",
			"serverclass=SftpTool",
			"language=zh_CN"
		};
		String files = request.getParameter("files");
		String home = request.getParameter("home");
		String dlgtype = request.getParameter("dlgtype");
		String module = request.getParameter("module");
		String rootpath = request.getParameter("rootpath");
		String defaultpath = request.getParameter("defaultpath");
		List<String> argList = new ArrayList<String>(Arrays.asList(default_args));
		argList.add("files=" + files);
		argList.add("home=" + home);
		argList.add("dlgtype=" + dlgtype);
		if (rootpath != null) argList.add("rootpath=" + rootpath);
		if (defaultpath != null) argList.add("defaultpath=" + defaultpath);

		String[] args = argList.toArray(default_args);

		boolean isJob = "job".equals(module);

		if (client == null) {
			try {
				initClient(args);
			} catch (Exception e) {
				client = null;
				throw e;
			}
		} else {
			setParams(args);
		}

		if (isJob) {
			client.initDirectTranfser();
		} else {
			client.initTransfer();
		}

		OperationResult result = new OperationResult(request);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		String respId = IDGenerator.nextId(this.getClass());
		result.setResponseId(respId);

		Map<String, String> params = new HashMap<String, String>();
		params.put("dlgtype", dlgtype);
		params.put("files", files);
		params.put("home", home);
		FileTransfer.params.put(respId, params);

		return result;
	}

	private OperationResult _getProgress(Request request) {
		if (client == null) {
			throw new IllegalStateException("transfer has not been initialized");
		}

		String reqId = request.getParameter(Configurations.REQUEST_ID);
		Map<String, String> params = FileTransfer.params.get(reqId);
		if (params == null) {
			throw new IllegalStateException("task not found");
		}

		String serverName = "蜂鸟LinuxHPC";
		String type       = params.get("dlgtype");
		String files      = params.get("files");
		String targetPath = params.get("home");

		String progress = client.getTaskPercentage(serverName, type, files, targetPath);
		int percentage = -1;
		String exception = null;
		try {
			percentage = Integer.parseInt(progress);
			if (percentage > 100) {
				percentage = 100;
			}
		} catch (NumberFormatException e) {
			exception = progress;
		}

		OperationResult result = new OperationResult(request);
		result.setResponseId(reqId);
		result.setExtraData("percentage", percentage);
		if (exception != null) {
			result.setReturnCode(Configurations.UNKNOWN_ERROR);
			result.setException(exception);
		} else {
			result.setReturnCode(Configurations.OPERATION_SUCCEED);
		}

		return result;
	}

	private void initClient(String[] args) {
		synchronized (FileTransfer.class) {
			if (client != null) {
				setParams(args);
				return;
			}

			client = new FtpClientApplet();
			client.isAppRun = true;
			AppletProviderFasade.registerApplet(client);
			setParams(args);
			client.initComponent(client);
			client.initService();
			JFrame frame = new JFrame();
			client.startWindowFrame(frame);
			frame.setVisible(true);
		}
	}

	private void setParams(String[] params) {
		client.clearParameters();
		for (String arg : params) {
			String[] nameValue = arg.split("=");
			if (nameValue.length == 2 && !"module".equals(nameValue[0])) {
				try {
					client.setParameter(nameValue[0], nameValue[1]);
				} catch (Exception e) {
				}
			}
		}
		client.initParameters();
	}

	public static void main(String[] args) throws Exception {
		FileTransfer trans = new FileTransfer();
		
		trans.doPost(null, null);
	}
}
