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

	public EchoBack() {
	}

	public void doGet(Request request, Response response) {
		this.doPost(request, response);
	}

	public void doPost(Request request, Response response) {
		OperationResult result = new OperationResult(request);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		String respId = IDGenerator.nextId(this.getClass());
		result.setResponseId(respId);

		result.setExtraData("method", request.getMethod());
		result.setExtraData("url", request.getUrl());
		result.setExtraData("version", request.getVersion());

		Map<String, String> headers = request.getHeaders();
		result.setExtraData("headers", headers);

		Map<String, String> parameters = request.getParameters();
		result.setExtraData("parameters", parameters);

		String ret = result.toJsonString();
		response.print(ret);

		response.flush();
	}

}
