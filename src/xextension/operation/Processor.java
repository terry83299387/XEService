package xextension.operation;

import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xextension.global.Configurations;
import xextension.http.Request;
import xextension.http.Response;

/**
 * Abstract class which provides default action for handling requests.
 * 
 * Subclasses need to implement doGet & doPost which are used handle GET & POST requests.
 * 
 * @author QiaoMingkui
 * 
 */
public abstract class Processor implements Runnable {
	private static final Logger	logger = LogManager.getLogger(Processor.class);

	private Socket		connection;
	private Request		request;
	private Response	response;

	public Processor() {
	}

	/**
	 * Handle GET requests.
	 * 
	 * @param request
	 * @param response
	 */
	public abstract void doGet(Request request, Response response) throws Exception;

	/**
	 * Handle POST requests.
	 * 
	 * @param request
	 * @param response
	 */
	public abstract void doPost(Request request, Response response) throws Exception;

	/**
	 * Handle requests.
	 * 
	 * @param request
	 * @param response
	 * @throws UnsupportedMethodException
	 */
	public void doRequest(Request request, Response response) throws UnsupportedMethodException, Exception {
		if (request == null || response == null) return;

		response.setHeader(Configurations.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

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
			Response.responseError(Configurations.UNSUPPORTED_METHOD, e.getMessage(), request, response);
			logger.warn(request, e);
		} catch (Exception e) {
			Response.responseError(Configurations.UNKNOWN_ERROR, e.getMessage(), request, response);
			logger.warn(request, e);

		} finally {
			// ignore keep-alive as it may cause problems.
			// set Connection field to close in response to simply disable this feature.
			// (see Response.flush())
			request = null;
			response = null;
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
				}
			}
		}
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
