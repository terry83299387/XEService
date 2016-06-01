package xextension.operation;

import java.util.Map;

import xextension.global.Configurations;
import xextension.global.IDGenerator;
import xextension.http.IHTTPSession;

/**
 * This class is just for test.
 * 
 * @author QiaoMingkui
 * 
 */
public class EchoBack extends Processor {
	private static final String PARAMETERS			= "parameters";
	private static final String HEADERS				= "headers";
	private static final String VERSION				= "version";
	private static final String URL					= "url";
	private static final String METHOD				= "method";

	public EchoBack() {
	}

	public OperationResult doGet(IHTTPSession session) throws Exception {
		return this.doPost(session);
	}

	public OperationResult doPost(IHTTPSession session) throws Exception {
		OperationResult result = new OperationResult(session);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		String respId = IDGenerator.nextId(this.getClass());
		result.setResponseId(respId);

		result.setExtraData(METHOD, session.getMethod());
		result.setExtraData(URL, session.getUri());
		result.setExtraData(VERSION, session.getProtocolVersion());

		Map<String, String> headers = session.getHeaders();
		result.setExtraData(HEADERS, headers);

		Map<String, String> parameters = session.getParms();
		result.setExtraData(PARAMETERS, parameters);

		return result;
	}

}
