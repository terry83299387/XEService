package xextension.operation.run_app;

import java.io.File;
import java.io.IOException;

import xextension.global.Configurations;
import xextension.global.IDGenerator;
import xextension.http.IHTTPSession;
import xextension.operation.OperationResult;
import xextension.operation.Processor;

/**
 * Run a local application (program) in an individual process.
 * 
 * @author QiaoMingkui
 * 
 */
public class RunApp extends Processor {
	private static final String KITTY = "kitty.exe";

	public OperationResult doGet(IHTTPSession session) throws Exception {
		return this.doPost(session);
	}

	public OperationResult doPost(IHTTPSession session) throws Exception {
		String appName = session.getParameter("appName");
		if (appName == null || appName.trim().length() == 0) {
			throw new IllegalArgumentException("app name is unspecified");
		}
		if (appName.indexOf("\\") != -1 || appName.indexOf("/") != -1) {
			throw new IllegalArgumentException("app name is illegal");
		}
		appName = appName.trim();

		String workDir = appName;
		if (appName.lastIndexOf(".") != -1) {
			workDir = appName.substring(0, appName.lastIndexOf(".")).trim();
		}
		if (workDir.length() == 0) {
			throw new IllegalArgumentException("app name is illegal");
		}

		String path = "resources\\" + workDir + "\\" + appName;
		File app = new File(path);
		if (!app.exists()) {
			throw new IllegalAccessError("app does not exist");
		}

		// get args
		String args = null;
		if (KITTY.equals(appName)) {
			args = getKittyArgs(session);
		} else {
			args = otherAppArgs(session);
		}

		String cmd = path + (args == null ? "" : " " + args);

		try {
			// TODO 后续可以：终止进程，查询进程，写入输入、获取输出，查询/终止某一会话（浏览器窗口）期间打开的所有进程等
			/*Process p = */Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			throw new IllegalAccessError("can not open specified program");
		}

		OperationResult result = new OperationResult(session);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		String respId = IDGenerator.nextId(this.getClass());
		result.setResponseId(respId);

		return result;
	}

	private String getKittyArgs(IHTTPSession session) {
		String clientKey = session.getParameter("clientKey");
		String server    = session.getParameter("server");
		String port      = session.getParameter("port");
		String userName  = session.getParameter("userName");
		String password  = session.getParameter("password");
		String initCd    = session.getParameter("initCd");

		if (clientKey != null && clientKey.trim().length() > 0) {
			DesDecrypt decrypt = new DesDecrypt();
			try {
				userName = decrypt.decrypt(userName, clientKey);
				password = decrypt.decrypt(password, clientKey);
			} catch (Exception e) {
				
				throw new IllegalArgumentException("argument is illegal");
			}
		}

		StringBuilder args = new StringBuilder();
		args.append(server).append(" -l ").append(userName).append(" -pw ").append(password);
		if (port != null && port.trim().length() > 0) {
			args.append(" -P ").append(port);
		}
		if (initCd != null && initCd.trim().length() > 0) {
			args.append(" -cmd \"cd ").append(initCd).append("\"");
		}

		return args.toString();
	}

	private String otherAppArgs(IHTTPSession session) {
		return session.getParameter("args");
	}

}
