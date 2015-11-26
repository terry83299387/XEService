/**
 * 
 */
package xextension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import xextension.autoupdate.AutoUpdater;
import xextension.global.Configurations;
import xextension.global.UIManager;
import xextension.service.XEService;

/**
 * @author QiaoMingkui
 *
 */
public class Main {
	static {
		System.setProperty("log4j.configurationFile", "resources\\log4j2.xml");
	}
	private static final Logger logger = LogManager.getLogger(Main.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*try {
			String workDir = System.getProperty("user.dir");
			String curClassPath = System.getProperty("java.class.path");
			String configClsPath = ConfigHelper.getProperty("classpath");
			logger.info("config class path: " + configClsPath);
			if (configClsPath != null && configClsPath.length() != 0) {
				String[] cps = configClsPath.split(";");
				StringBuilder classPath = new StringBuilder(curClassPath);
				String separator = System.getProperty("path.separator");
				String fileSeparator = System.getProperty("file.separator");
				for (String item : cps) {
					classPath.append(workDir).append(fileSeparator).append(item).append(separator);
				}
				System.setProperty("java.class.path", classPath.toString());
				logger.info("classpath: " + System.getProperty("java.class.path"));
			}
		} catch (IOException e) {
			logger.error("error occurs when try to set classpath", e);
		}*/

		try {
			AutoUpdater autoUpdater = new AutoUpdater();
			autoUpdater.autoUpdate();
		} catch (Exception e) {
			logger.warn("Update failed, an error occurs while trying to update", e);
		}

		// keep going no matter how update succeeds or not

		// check if service already started
		checkServiceAlreadyStarted();

		UIManager.initLookAndFeel();

		try {
			XEService service = new XEService();
			service.startService();
		} catch (Exception e) {
			logger.error("XeXtension service startup failed: ", e);
		}
	}

	private static void checkServiceAlreadyStarted() {
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		JSONObject json;
		String line;
		String name;
		for (int p : Configurations.CANDIDATE_PORTS) {
			try {
				connection = (HttpURLConnection) new URL(
						"http://localhost:" + p + "/?op=" + Configurations.VERSION_INFO).openConnection();
				connection.connect();
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        line = reader.readLine();
			} catch (IOException e) {
				continue;
			} finally {
        try {
        	if (reader != null) {
        		reader.close();
        		reader = null;
        	}
        	if (connection != null) {
        		connection.disconnect();
        		connection = null;
        	}
				} catch (IOException e) {
				}
			}

			try {
        json = new JSONObject(line);
        if (json != null) {
        	json = json.optJSONObject("extraData");
        }
      	if (json != null) {
        	name = json.optString("name");
	        if (Configurations.NAME.equals(name)) {
	        	logger.error("Startup failed, there already has a running service on port " + p);
	        	System.exit(1);
	        }
      	}
			} catch (JSONException e) {
				/* ignore */
			}

		} // end for loop
	}

}
