/**
 * 
 */
package xextension.http;

import static xextension.global.Configurations.DEFAULT_ENCODING;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

/**
 * This article introduces information about HTTP Message:
 * 
 * http://www.oschina.net/question/565065_81309
 * 
 * @author QiaoMingkui
 * 
 */
public class Request {
	public static final String	POST	= "POST";
	public static final String	GET		= "GET";

	private static final String SPACE						= " ";
	private static final String PARAM_FLAG			= "?";
	private static final String PARAM_SEPARATOR	= "&";
	private static final String EQUAL						= "=";
	private static final String COLON						= ":";

	private String				method;
	private String				url;
	private String				version;
	private Map<String, String>	headers	= new HashMap<String, String>();
	private Map<String, String>	parameters;

	// private RequestData data; // does not implement yet

	public Request() {
	}

	/**
	 * Build a request instance from parsing the client request socket.
	 * 
	 * @param inputStream
	 * @return a request built with request socket
	 * @throws IOException
	 */
	public static Request parseRequest(InputStream inputStream) throws IOException {
		Request request = new Request();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, DEFAULT_ENCODING));
		String line;

		// request line
		String[] splits;
		line = reader.readLine();
		splits = line.split(SPACE);
		request.method = splits[0];
		request.url = splits[1];
		if (splits.length > 2) {
			request.version = splits[2];
		}

		// headers
		while ((line = reader.readLine()).length() != 0) {
			int pos = line.indexOf(COLON);
			request.headers.put(line.substring(0, pos).trim(), line.substring(pos + 1).trim());
		}

		// body (does not implement yet)

		return request;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("method").append(":").append(method)
				.append(", ").append("url").append(":").append(url)
				.append(", ").append("version").append(":").append(version).append("\n");

		// headers
		Iterator<Entry<String, String>> i = headers.entrySet().iterator();
		Entry<String, String> entry;
		sb.append("headers").append(":").append("\n");
		while(i.hasNext()) {
			entry = i.next();
			sb.append("\t").append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
		}

		// parameters
		getParameters();
		i = parameters.entrySet().iterator();
		sb.append("parameters").append(":").append("\n");
		while(i.hasNext()) {
			entry = i.next();
			sb.append("\t").append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
		}

		return sb.toString();
	}

	/**
	 * @return the request method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return the header
	 */
	public String getHeader(String name) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("name is empty");
		}
		return headers.get(name);
	}

	/**
	 * @return the headers
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * @return the parameter
	 */
	public String getParameter(String name) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("name is empty");
		}
		if (parameters == null) {
			parseParameters();
		}

		return parameters.get(name);
	}

	/**
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		if (parameters == null) {
			parseParameters();
		}

		return parameters;
	}

	private void parseParameters() {
		parameters = new HashMap<String, String>();

		int pos = url.lastIndexOf(PARAM_FLAG);
		if (pos == -1 || pos == url.length() - 1) {
			return;
		}
		String params = url.substring(pos + 1);

		StringTokenizer tokens = new StringTokenizer(params, PARAM_SEPARATOR);
		String param, name, value;
		try {
			while (tokens.hasMoreTokens()) {
				param = tokens.nextToken();
				pos = param.indexOf(EQUAL);
				if (param.length() != 0 && pos != -1) {
					name = param.substring(0, pos);
					// param may not have a value (in other words, which has an empty value), "p=" for instance
					if (pos == param.length()) {
						value = "";
					} else {
						value = param.substring(pos + 1);
					}
					parameters.put(URLDecoder.decode(name, DEFAULT_ENCODING),
							URLDecoder.decode(value, DEFAULT_ENCODING));
				}
			}
		} catch (UnsupportedEncodingException e) {
			// ignore, cause it should never gonna happen
		}
	}

}
