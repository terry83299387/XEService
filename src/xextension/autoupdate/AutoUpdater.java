/**
 * 
 */
package xextension.autoupdate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xextension.service.XEService;

/**
 * @author QiaoMingkui
 * 
 */
public class AutoUpdater {
	static {
		System.setProperty("log4j.configurationFile", "resources\\log4j2.xml");
	}

	private static final Logger logger = LogManager.getLogger(AutoUpdater.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// auto-update
		try {
			String[] t = loadUpdateLog();

			if (t == null) {
				logger.info("can not load update log");
			} else {
				logger.info("update log load completed");
				autoUpdate(t[0], t[1]);
			}
		} catch (Exception e) {
			logger.error("Update failed, an error occurs when try to update", e);
		}

		// start service no matter update succeeds
		XEService.main(args);
	}

	private static void autoUpdate(String server, String updateLog) {
		// TODO
		System.out.println("server: " + server);
		System.out.println(updateLog);
	}

	private static String[] loadUpdateLog() throws IOException {
		Properties serverAddress = new Properties();
		serverAddress.load(new FileInputStream("resources\\server.properties"));
		String defaultAddress = serverAddress.getProperty("default");
		String customer = serverAddress.getProperty("customer");
		logger.info("Read server URLs: defaultAddress: " + defaultAddress + ", customer: " + customer);

		if (customer != null && customer.trim().length() != 0) {
			try {
				String updateLog = loadUpdateLogFromURL(customer);
				return new String[] {customer, updateLog};
			} catch (IOException e) {
				logger.info("An error occurs while trying to load update log by customer address: ", e);
				logger.info("Use default address instead.");
				return loadUpdateLogFromDefault(defaultAddress);
			}
		} else {
			logger.info("Customer address is not specified, use default instead.");
			return loadUpdateLogFromDefault(defaultAddress);
		}
	}

	private static String[] loadUpdateLogFromDefault(String addressStr) {
		String[] defaultAddresses = addressStr.split(";");
		String updateLog;
		for (String address : defaultAddresses) {
			try {
				updateLog = loadUpdateLogFromURL(address);
				return new String[] {address, updateLog};
			} catch (IOException e) {
				// ignore
			}
		}

		return null;
	}

	private static String loadUpdateLogFromURL(String server) throws IOException {
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			connection = (HttpURLConnection) new URL(server + "/xexupdate/update.log").openConnection();
			connection.connect();
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder lines = new StringBuilder(4 * 1024);
			String line;
			while ((line = reader.readLine()) != null) {
				lines.append(line);
			}

			return lines.toString();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			if (reader != null) {
				reader.close();
			}
		}
	}

}
