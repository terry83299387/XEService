package xextension.operation.run_app;

import java.io.File;
import java.io.IOException;

import xextension.global.Configurations;
import xextension.global.IDGenerator;
import xextension.http.Request;
import xextension.http.Response;
import xextension.operation.OperationResult;
import xextension.operation.Processor;

/**
 * Run a local application (program) in an individual process.
 * 
 * @author QiaoMingkui
 * 
 */
public class RunApp extends Processor {
	public static final String PARAMETERS		= "parameters";
	public static final String HEADERS			= "headers";
	public static final String VERSION			= "version";
	public static final String URL					= "url";
	public static final String METHOD				= "method";

	public RunApp() {
	}

	public void doGet(Request request, Response response) throws Exception {
		this.doPost(request, response);
	}

	public void doPost(Request request, Response response) throws Exception {
		// kitty startup parameters:
		// [-l username] [-pw password] [-P port] server [-cmd "cd xxxx"]
		String appName = request.getParameter("appName");
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

		String args = request.getParameter("args");
		String cmd = path + (args == null ? "" : " " + args);

		try {
			// TODO 后续可以：终止进程，查询进程，写入输入、获取输出，查询/终止某一会话（浏览器窗口）期间打开的所有进程等
			/*Process p = */Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			throw new IllegalAccessError("can not open specified program");
		}

		OperationResult result = new OperationResult(request);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		String respId = IDGenerator.nextId(this.getClass());
		result.setResponseId(respId);
		String ret = result.toJsonString();
		response.print(ret);
		response.flush();
	}

}
