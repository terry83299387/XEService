package xextension.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import xextension.global.Configurations;
import xextension.global.UIManager;
import xextension.operation.Processor;

/**
 * XEService stands for Xfinity Extension Service.
 * 
 * @author QiaoMingkui
 * 
 */
public class XEService {
	private ServerSocket	server;

	public XEService() {
		String msg = "";
		for (int p : Configurations.CANDIDATE_PORTS) {
			try {
				server = new ServerSocket(p);
				break;
			} catch (IOException e) {
				msg = e.getMessage();
			}
		}

		if (server == null) {
			// TODO record errors

			throw new IllegalStateException(
					"XeXtension service's startup is failed, and the error info is :" + msg);
		}
	}

	public void startService() {
		Socket connection = null;
		while (true) {
			try {
				connection = server.accept();
				Processor.dispatchService(connection);
			} catch (Exception e) {
				// TODO shutdown or ignore (?)
			}
		}
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {
		// check if service already started
		checkServiceAlreadyStarted();

		UIManager.initLookAndFeel();

		XEService service = new XEService();
		System.out.println("server starts on port: " + service.server.getLocalPort());
		service.startService();
	}

	private static void checkServiceAlreadyStarted() {
		HttpURLConnection connection;
		BufferedReader reader;
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
        reader.close();
        connection.disconnect();

			} catch (IOException e) {
				continue;
			}

			try {
        json = new JSONObject(line);
        if (json != null) {
        	json = json.optJSONObject("extraData");
        }
      	if (json != null) {
        	name = json.optString("name");
	        if (Configurations.NAME.equals(name)) {
	        	System.out.println("Startup failed, there already has a running service on port " + p);
	        	System.exit(1);
	        }
      	}
			} catch (JSONException e) {/* ignore */}

		} // end for loop
	}
}
