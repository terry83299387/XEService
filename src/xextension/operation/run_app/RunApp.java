package xextension.operation.run_app;

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

	public void doGet(Request request, Response response) {
		this.doPost(request, response);
	}

	public void doPost(Request request, Response response) {
		// kitty startup parameters:
		// [-l username] [-pw password] [-P port] server [-cmd "cd xxxx"]
		try {
			String appName = request.getParameter("appName"); // "resources\\kitty.exe"
			if (appName == null || appName.trim().length() == 0) {
				throw new IllegalArgumentException("app name is unspecified");
			}

			appName = "resources\\" + appName;
			String args = request.getParameter("args");
			String cmd = appName + (args == null ? "" : " " + args);
			Process p = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			throw new IllegalAccessError("can not open specified program.");
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
