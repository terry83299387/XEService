/**
 * 
 */
package xextension.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xextension.global.Configurations;
import xextension.http.IHTTPSession;
import xextension.operation.EchoBack;
import xextension.operation.OperationResult;
import xextension.operation.Processor;
import xextension.operation.UnknownOperatorException;
import xextension.operation.UnsupportedMethodException;
import xextension.operation.VersionInfo;
import xextension.operation.file_browser.FileBrowser;
import xextension.operation.file_operator.FileOperator;
import xextension.operation.file_transfer.FileTransfer;
import xextension.operation.remote_desktop.RemoteDesktop;
import xextension.operation.run_app.RunApp;

/**
 * @author QiaoMingkui
 *
 */
public class ServiceDispatcher {
	private static final Logger	logger = LogManager.getLogger(ServiceDispatcher.class);

	/**
	 * Dispatch a service to process request.
	 * 
	 * @param connection
	 */
	public OperationResult dispatchService(IHTTPSession session) {
		String operatorParam = null;
		try {
			operatorParam = session.getParameter(Configurations.REQUEST_OPERATOR);
			int operator = Integer.parseInt(operatorParam.trim());
			Processor processor = getProcessor(operator);

			return processor.doRequest(session);

		} catch (NumberFormatException e) {
			logger.warn(session, e);
			return Processor.responseError(Configurations.UNKNOWN_OPERATOR,
					"operator is illegal:" + operatorParam, session);
		} catch (UnknownOperatorException e) {
			logger.warn(session, e);
			return Processor.responseError(Configurations.UNKNOWN_OPERATOR, e.getMessage(), session);
		} catch (UnsupportedMethodException e) {
			logger.warn(session, e);
			return Processor.responseError(Configurations.UNSUPPORTED_METHOD, e.getMessage(), session);
		} catch (Exception e) {
			logger.warn(session, e);
			return Processor.responseError(Configurations.UNKNOWN_ERROR,
					e.getClass().getName() + ": " + e.getMessage(), session);
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
			case Configurations.FILE_BROWSER:
				processor = new FileBrowser();
				break;
			case Configurations.FILE_TRANSFER:
				processor = new FileTransfer();
				break;
			case Configurations.VERSION_INFO:
				processor = new VersionInfo();
				break;
			case Configurations.RUN_APP:
				processor = new RunApp();
				break;
			case Configurations.FILE_OPERATOR:
				processor = new FileOperator();
				break;
			case Configurations.REMOTE_DESKTOP:
				processor = new RemoteDesktop();
				break;
			case Configurations.ECHO_BACK:
				processor = new EchoBack();
				break;
			default:
				throw new UnknownOperatorException("unknown operator:" + operator);
		}

		return processor;
	}
}
