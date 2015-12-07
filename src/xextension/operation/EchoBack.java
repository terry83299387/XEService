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
	private static final String PARAMETERS		= "parameters";
	private static final String HEADERS				= "headers";
	private static final String VERSION				= "version";
	private static final String URL						= "url";
	private static final String METHOD				= "method";

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
