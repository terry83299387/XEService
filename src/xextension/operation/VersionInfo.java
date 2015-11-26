/**
 * 
 */
package xextension.operation;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xextension.global.ConfigHelper;
import xextension.global.Configurations;
import xextension.http.Request;
import xextension.http.Response;

/**
 * @author QiaoMingkui
 *
 */
public class VersionInfo extends Processor {
	private static final Logger logger = LogManager.getLogger(VersionInfo.class);

	public void doGet(Request request, Response response) throws UnsupportedMethodException {
		this.doPost(request, response);
	}

	public void doPost(Request request, Response response) throws UnsupportedMethodException {
		storeXfinityServer(request);

		OperationResult result = new OperationResult(request);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		result.setExtraData("name", Configurations.NAME);
		result.setExtraData("version", Configurations.VERSION);
		result.setExtraData("copyright", Configurations.COPYRIGHT);

		response.print(result.toJsonString());
		response.flush();
	}

	private void storeXfinityServer(Request request) {
		try {
			String referer = request.getHeader("Referer");
			if (referer != null && referer.trim().length() > 0) {
				URL url = new URL(referer);
				String protocol = url.getProtocol();
				String host     = url.getHost();
				int port        = url.getPort();

				String xfinityServer = protocol + "://" + host;
				if (port != -1) {
					xfinityServer += ":" + port;
				}
				xfinityServer += "/";

				String customer = ConfigHelper.getProperty("autoupdate.server");
				if (customer == null || !xfinityServer.equals(customer)) {
					logger.info("update autoupdate server to: " + xfinityServer + " (current: " + customer + ")");
					ConfigHelper.setProperty("autoupdate.server", xfinityServer);
				}
			}
		} catch (Exception e) {
			logger.warn("failed to update autoupdate server: ", e);
		}
	}

}
