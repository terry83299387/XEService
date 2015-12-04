package xextension.operation;

import java.util.Map;

import xextension.global.Configurations;
import xextension.global.IDGenerator;
import xextension.http.Request;
import xextension.http.Response;

/**
 * This class is just for test.
 * 
 * @author QiaoMingkui
 * 
 */
public class EchoBack extends Processor {
	public static final String PARAMETERS		= "parameters";
	public static final String HEADERS			= "headers";
	public static final String VERSION			= "version";
	public static final String URL					= "url";
	public static final String METHOD				= "method";

	public EchoBack() {
	}

	public void doGet(Request request, Response response) throws Exception {
		this.doPost(request, response);
	}

	public void doPost(Request request, Response response) throws Exception {
		OperationResult result = new OperationResult(request);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		String respId = IDGenerator.nextId(this.getClass());
		result.setResponseId(respId);

		result.setExtraData(METHOD, request.getMethod());
		result.setExtraData(URL, request.getUrl());
		result.setExtraData(VERSION, request.getVersion());

		Map<String, String> headers = request.getHeaders();
		result.setExtraData(HEADERS, headers);

		Map<String, String> parameters = request.getParameters();
		result.setExtraData(PARAMETERS, parameters);

		String ret = result.toJsonString();
		response.print(ret);

		response.flush();
	}

}
