/**
 * 
 */
package xextension.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * @author QiaoMingkui
 *
 */
public class ConfigHelper {
	// config file related
	public static final String	CONFIG_FILE		= "resources" + File.separator + "config.properties";

	public static String getProperty(String name) throws IOException {
		Properties config = readConfig();
		return config.getProperty(name);
	}

	public static void setProperty(String name, String value) throws IOException {
		Properties config = readConfig();
		config.setProperty(name, value);
		writeConfig(config);
	}

	public static Properties readConfig() throws IOException {
		Properties config = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(CONFIG_FILE);
			config.load(input);
			return config;
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}

	public static void writeConfig(Properties config) throws IOException {
		OutputStream output = null;
		try {
			output = new FileOutputStream(CONFIG_FILE);
			config.store(output, null);
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}
}
