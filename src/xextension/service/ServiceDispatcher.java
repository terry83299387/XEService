/**
 * 
 */
package xextension.service;

import java.net.Socket;

import xextension.global.Configurations;
import xextension.http.Request;
import xextension.http.Response;
import xextension.operation.EchoBack;
import xextension.operation.Processor;
import xextension.operation.UnknownOperatorException;
import xextension.operation.VersionInfo;
import xextension.operation.file_browser.FileBrowseProcessor;
import xextension.operation.run_app.RunApp;

/**
 * @author QiaoMingkui
 *
 */
public class ServiceDispatcher {

	/**
	 * Dispatch a service to process request.
	 * 
	 * @param connection
	 */
	public void dispatchService(Socket connection) {
		Request request = null;
		Response response = null;
		String operatorParam = null;
		try {
			request = Request.parseRequest(connection.getInputStream());
			response = Response.getResponse(connection.getOutputStream());
			response.setJsonCallback(request.getParameter(Configurations.JSON_CALLBACK));

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
			Response.responseError(Configurations.UNKNOWN_OPERATOR,
					"operator is not illegal:" + operatorParam, request, response);
		} catch (UnknownOperatorException e) {
			Response.responseError(Configurations.UNKNOWN_OPERATOR, e.getMessage(), request, response);
		} catch (Exception e) {
			Response.responseError(Configurations.UNKNOWN_ERROR, e.getMessage(), request, response);
		}
	}

	/**
	 * Return an appropriate Processor instance to handle the request.
	 * 
	 * @param operator
	 * @return
	 * @throws UnknownOperatorException
	 */
	public Processor getProcessor(int operator) throws UnknownOperatorException {
		Processor processor = null;
		switch (operator) {
			case Configurations.VERSION_INFO:
				processor = new VersionInfo();
				break;
			case Configurations.FILE_BROWSER:
				processor = new FileBrowseProcessor();
				break;
			case Configurations.RUN_APP:
				processor = new RunApp();
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
}
