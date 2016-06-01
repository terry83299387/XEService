package xextension.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xextension.http.IHTTPSession;
import xextension.http.Method;

/**
 * Abstract class which provides default action for handling requests.
 * 
 * Subclasses need to implement doGet & doPost which are used handle GET & POST requests.
 * 
 * @author QiaoMingkui
 * 
 */
public abstract class Processor {
	private static final Logger	logger = LogManager.getLogger(Processor.class);

	public Processor() {
	}

	/**
	 * Handle GET requests.
	 * 
	 * @param session
	 * @return OperationResult the result of executing
	 */
	public abstract OperationResult doGet(IHTTPSession session) throws Exception;

	/**
	 * Handle POST requests.
	 * 
	 * @param session
	 * @return OperationResult the result of executing
	 */
	public abstract OperationResult doPost(IHTTPSession session) throws Exception;

	/**
	 * Handle requests.
	 * 
	 * @param session
	 * @return OperationResult the result of executing
	 * @throws UnsupportedMethodException
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	public OperationResult doRequest(IHTTPSession session) throws UnsupportedMethodException, Exception {
		if (session == null) throw new IllegalArgumentException("no session specificated");

		Method method = session.getMethod();
		if (method == Method.GET) {
			return this.doGet(session);
		} else if (method == Method.POST) {
			return this.doPost(session);
		} else {
			throw new UnsupportedMethodException("Unsupported method: " + method);
		}
	}


	/**
	 * unified error response handler.
	 */
	public static OperationResult responseError(int returnCode, String msg, IHTTPSession session) {
		OperationResult result = new OperationResult(session);
		result.setReturnCode(returnCode);
		result.setException(msg);
		return result;
	}

}
