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

	public static final String NAME					= "name";
	public static final String VERSION			= "version";
	public static final String COPYRIGHT		= "copyright";

	private static final String REFERER			= "Referer";
	private static final String PROTOCOL_SEPARATOR	= "://";
	private static final String COLON				= ":";
	private static final String SLASH				= "/";

	public void doGet(Request request, Response response) throws UnsupportedMethodException {
		this.doPost(request, response);
	}

	public void doPost(Request request, Response response) throws UnsupportedMethodException {
		storeXfinityServer(request);

		OperationResult result = new OperationResult(request);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		result.setExtraData(NAME, Configurations.NAME);
		result.setExtraData(VERSION, Configurations.VERSION);
		result.setExtraData(COPYRIGHT, Configurations.COPYRIGHT);

		response.print(result.toJsonString());
		response.flush();
	}

	private void storeXfinityServer(Request request) {
		try {
			String referer = request.getHeader(REFERER);
			if (referer != null && referer.trim().length() > 0) {
				URL url = new URL(referer);
				String protocol = url.getProtocol();
				String host     = url.getHost();
				int port        = url.getPort();

				String xfinityServer = protocol + PROTOCOL_SEPARATOR + host;
				if (port != -1) {
					xfinityServer += COLON + port;
				}
				xfinityServer += SLASH;

				String customerServer = ConfigHelper.getProperty(Configurations.CUSTOMER_AUTOUPDATE_SERVER);
				if (customerServer == null || !xfinityServer.equals(customerServer)) {
					logger.info("update autoupdate server to: " + xfinityServer + " (current: " + customerServer + ")");
					ConfigHelper.setProperty(Configurations.CUSTOMER_AUTOUPDATE_SERVER, xfinityServer);
				}
			}
		} catch (Exception e) {
			logger.warn("failed to update autoupdate server: ", e);
		}
	}

}
