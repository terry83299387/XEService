/**
 * 
 */
package xextension.operation;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xextension.global.ConfigHelper;
import xextension.global.Configurations;
import xextension.global.IDGenerator;
import xextension.http.IHTTPSession;

/**
 * @author QiaoMingkui
 *
 */
public class VersionInfo extends Processor {
	private static final Logger logger = LogManager.getLogger(VersionInfo.class);

	public  static final String NAME				= "name";
	private static final String VERSION				= "version";
	private static final String COPYRIGHT			= "copyright";

	private static final String REFERER				= "referer";
	private static final String PROTOCOL_SEPARATOR	= "://";
	private static final String COLON				= ":";
	private static final String SLASH				= "/";

	public OperationResult doGet(IHTTPSession session) throws Exception {
		return this.doPost(session);
	}

	public OperationResult doPost(IHTTPSession session) throws Exception {
		storeXfinityServer(session);

		OperationResult result = new OperationResult(session);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		String respId = IDGenerator.nextId(this.getClass());
		result.setResponseId(respId);
		result.setExtraData(NAME, Configurations.NAME);
		result.setExtraData(VERSION, Configurations.VERSION);
		result.setExtraData(COPYRIGHT, Configurations.COPYRIGHT);

		return result;
	}

	private void storeXfinityServer(IHTTPSession session) {
		try {
			String referer = session.getHeader(REFERER);
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
