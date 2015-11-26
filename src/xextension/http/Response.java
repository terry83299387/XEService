/**
 * 
 */
package xextension.http;

import static xextension.global.Configurations.DEFAULT_ENCODING;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import xextension.global.Configurations;

/**
 * This article introduces information about HTTP Message:
 * 
 * http://www.oschina.net/question/565065_81309
 * 
 * @author QiaoMingkui
 * 
 */
public class Response {
	private static final String HTTP_VERSION = "HTTP/1.1";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_LENGTH = "Content-Length";
	private static final String DATE = "Date";
	private static final String SERVER = "Server";
	private static final String CONNECTION = "Connection";
	private static final String DEFAULT_CONTENT_TYPE = "application/json";
	private static final String HEADER_SEPARATOR = ": ";
	private static final String SPACE = " ";
	private static final String NEW_LINE = "\r\n";

	private Writer				out;
	private Map<String, String>	headers	= new HashMap<String, String>();
	private String				status	= "200 OK";
	private StringBuilder		content	= new StringBuilder(1024);
	private String jsonCallback;

	public Response() {
	}

	public static Response getResponse(OutputStream outputStream) {
		Response response = new Response();
		try {
			response.out = new OutputStreamWriter(new BufferedOutputStream(outputStream), DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			// ignore, this should never gonna happen
		}
		return response;
	}

	public void setHeader(String name, String value) {
		if (name != null && name.length() != 0) {
			headers.put(name, value);
		}
	}

	public void setStatus(String status) {
		if (status != null) {
			this.status = status;
		}
	}

	public void print(String content) {
		if (content != null) {
			this.content.append(content);
		}
	}

	public void flush() {
		try {
			StringBuilder sb = new StringBuilder(1024);
			sb.append(HTTP_VERSION).append(SPACE).append(status).append(NEW_LINE);

			this.headers.put(SERVER, Configurations.NAME + SPACE + Configurations.VERSION);
			this.headers.put(CONNECTION, "Close"); // ignore keep-alive and always close the connection
			this.headers.put(DATE, new Date().toString());
			// default content type
			if (!this.headers.containsKey(CONTENT_TYPE)) {
				this.headers.put(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
			}
			// jquery getJson needs this format
			if (jsonCallback != null) {
				content = new StringBuilder(jsonCallback).append("(").append(content).append(")");
			}
			// content length. Note: it should be final encoded byte-length, not char-length
			int len = content.toString().getBytes(DEFAULT_ENCODING).length;
			this.headers.put(CONTENT_LENGTH, "" + len);

			Iterator<Entry<String, String>> headers = this.headers.entrySet().iterator();
			Entry<String, String> header;
			while (headers.hasNext()) {
				header = headers.next();
				sb.append(header.getKey()).append(HEADER_SEPARATOR).append(header.getValue()).append(NEW_LINE);
			}
			sb.append(NEW_LINE);
			sb.append(content);

			out.write(sb.toString());
			out.flush();
		} catch (IOException e) {
			// ignore (failed silently)
		}
	}

	/**
	 * @param jsonCallback the jsonCallback to set
	 */
	public void setJsonCallback(String jsonCallback) {
		this.jsonCallback = jsonCallback;
	}
}
