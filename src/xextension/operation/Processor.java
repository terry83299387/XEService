package xextension.operation;

import java.net.Socket;

import xextension.global.Configurations;
import xextension.http.Request;
import xextension.http.Response;
import xextension.operation.file_browser.FileBrowseProcessor;

/**
 * Abstract class which provides default action for handling requests.
 * 
 * Subclasses need to implement doGet & doPost which are used handle GET & POST requests.
 * 
 * @author QiaoMingkui
 * 
 */
public abstract class Processor implements Runnable {
	private Socket connection;
	private Request request;
	private Response response;

	public Processor() {
	}

	/**
	 * Dispatch a service to process request.
	 * TODO 将本方法和getProcessor()提取为ServiceDispatcher类
	 * 
	 * @param connection
	 */
	public static void dispatchService(Socket connection) {
		Request request = null;
		Response response = null;
		String operatorParam = null;
		try {
			request = Request.parseRequest(connection.getInputStream());
			response = Response.getResponse(connection.getOutputStream());

			operatorParam = request.getParameter(Configurations.REQUEST_OPERATOR);
			int operator = Integer.parseInt(operatorParam.trim());
			Processor processor = getProcessor(operator);
			processor.setConnection(connection);
			processor.setRequest(request);
			processor.setResponse(response);

			// to run each processor in separate threads allows them to do long time work
			Thread t = new Thread(processor);
			t.start();

		} catch (NumberFormatException e) {
			errorResponse(Configurations.UNKNOWN_OPERATOR,
					"operator is not illegal:" + operatorParam, request, response);
		} catch (UnknownOperatorException e) {
			errorResponse(Configurations.UNKNOWN_OPERATOR, e.getMessage(), request, response);
		} catch (Exception e) {
			errorResponse(Configurations.UNKNOWN_ERROR, e.getMessage(), request, response);
		}
	}

	/**
	 * Return an appropriate Processor instance to handle the request.
	 * 
	 * @param operator
	 * @return
	 * @throws UnknownOperatorException
	 */
	public static Processor getProcessor(int operator) throws UnknownOperatorException {
		Processor processor = null;
		switch (operator) {
			case Configurations.VERSION_INFO:
				processor = new VersionInfo();
				break;
			case Configurations.FILE_BROWSER:
				processor = new FileBrowseProcessor();
				break;
			// case Configurations.FILE_TRANSFER: // TODO
			// processor = new FileTransferProcessor();
			// break;
			case Configurations.ECHO_BACK:
				processor = new EchoBack();
				break;
			default:
				throw new UnknownOperatorException("unknown operator:" + operator);
		}

		return processor;
	}

	/**
	 * Handle GET requests.
	 * 
	 * @param request
	 * @param response
	 * @throws UnsupportedMethodException
	 */
	public abstract void doGet(Request request, Response response) throws UnsupportedMethodException;

	/**
	 * Handle POST requests.
	 * 
	 * @param request
	 * @param response
	 * @throws UnsupportedMethodException
	 */
	public abstract void doPost(Request request, Response response) throws UnsupportedMethodException;

	/**
	 * Handle requests.
	 * 
	 * @param request
	 * @param response
	 * @throws UnsupportedMethodException
	 */
	public void doRequest(Request request, Response response) throws UnsupportedMethodException {
		if (request == null || response == null) return;

		response.setHeader("Access-Control-Allow-Origin", "*");

		String method = request.getMethod();
		if (Request.GET.equals(method)) {
			this.doGet(request, response);
		} else if (Request.POST.equals(method)) {
			this.doPost(request, response);
		} else {
			throw new UnsupportedMethodException("Unsupported method: " + method);
		}
	}

	@Override
	public void run() {
		try {
			this.doRequest(request, response);
		} catch (UnsupportedMethodException e) {
			errorResponse(Configurations.UNSUPPORTED_METHOD, e.getMessage(), request, response);
		} catch (Exception e) {
			errorResponse(Configurations.UNKNOWN_ERROR, e.getMessage(), request, response);

		} finally {
			String keepAlive = request.getHeader("Connection");
			request = null;
			response = null;
			if (connection != null && !"keep-alive".equals(keepAlive)) {
				try {
					// if keep-alive flag was not set, close socket connection
					connection.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/*
	 * unified error response handler.
	 */
	private static void errorResponse(int returnCode, String msg, Request request, Response response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		OperationResult result = new OperationResult(request);
		result.setReturnCode(returnCode);
		result.setException(msg);
		response.print(result.toJsonString());
		response.flush();
	}

	/**
	 * @return the connection
	 */
	public Socket getConnection() {
		return connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(Socket connection) {
		this.connection = connection;
	}

	/**
	 * @return the request
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(Request request) {
		this.request = request;
	}

	/**
	 * @return the response
	 */
	public Response getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(Response response) {
		this.response = response;
	}

}
