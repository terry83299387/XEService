package xextension.service;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xextension.global.Configurations;
import xextension.http.IHTTPSession;
import xextension.http.HTTPD;
import xextension.http.Response;
import xextension.http.Response.Status;
import xextension.operation.OperationResult;

/**
 * XEService stands for Xfinity Extension Service.
 * 
 * @author QiaoMingkui
 * 
 */
public class XEService extends HTTPD {
	private static final Logger logger = LogManager.getLogger(XEService.class);

	public XEService() throws IllegalStateException, IOException {
		makeSecure(HTTPD.makeSSLSocketFactory("/xextension.jks", "1234#a#A".toCharArray()), null);
		String msg = "";
		boolean success = false;
		for (int p : Configurations.CANDIDATE_PORTS) {
			try {
				setMyPort(p);
				start(HTTPD.SOCKET_READ_TIMEOUT, false);
				logger.info("server starts on port: " + p);
				success = true;
				break;
			} catch (IOException e) {
				msg = e.getMessage();
			}
		}
		if (!success) {
			throw new IllegalStateException(msg);
		}
	}

	@Override
	public Response serve(IHTTPSession session) {
		String jsoncb = session.getParameter(Configurations.JSON_CALLBACK);
		ServiceDispatcher dispatcher = new ServiceDispatcher();
		OperationResult op = dispatcher.dispatchService(session);

		// response
		String content = jsoncb + "(" + op.toJsonString() + ")";
		Response resp = newFixedLengthResponse(Status.OK, Configurations.DEFAULT_CONTENT_TYPE, content);
		resp.addHeader("Server", Configurations.NAME + "/" + Configurations.VERSION);
		resp.addHeader(Configurations.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		return resp;
	}

}
