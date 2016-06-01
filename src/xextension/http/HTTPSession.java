package xextension.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import javax.net.ssl.SSLException;

class HTTPSession implements IHTTPSession {
	/**
	 * logger to log to.
	 */
	private static final Logger LOG = Logger.getLogger(HTTPSession.class.getName());

	private static final int REQUEST_BUFFER_LEN = 512;

	private static final int MEMORY_STORE_LIMIT = 1024;

	public static final int BUFSIZE = 8192;

	public static final int MAX_HEADER_SIZE = 1024;

	private final TempFileManager tempFileManager;

	private final OutputStream outputStream;

	private final BufferedInputStream inputStream;

	private int splitbyte;

	private int rlen;

	private String uri;

	private Method method;

	private Map<String, String> parms;

	private Map<String, String> headers;

	private CookieHandler cookies;

	private String queryParameterString;

	private String remoteIp;

	private String remoteHostname;

	private String protocolVersion;

	private HTTPD httpd;

	public HTTPSession(TempFileManager tempFileManager, InputStream inputStream, OutputStream outputStream) {
		this.tempFileManager = tempFileManager;
		this.inputStream = new BufferedInputStream(inputStream, HTTPSession.BUFSIZE);
		this.outputStream = outputStream;
	}

	public HTTPSession(TempFileManager tempFileManager, InputStream inputStream, OutputStream outputStream,
			InetAddress inetAddress) {
		this.tempFileManager = tempFileManager;
		this.inputStream = new BufferedInputStream(inputStream, HTTPSession.BUFSIZE);
		this.outputStream = outputStream;
		this.remoteIp = inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress() ? "127.0.0.1"
				: inetAddress.getHostAddress().toString();
		this.remoteHostname = inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress() ? "localhost"
				: inetAddress.getHostName().toString();
		this.headers = new HashMap<String, String>();
	}

	/**
	 * Decodes the sent headers and loads the data into Key/value pairs
	 */
	private void decodeHeader(BufferedReader in, Map<String, String> pre, Map<String, String> parms,
			Map<String, String> headers) throws ResponseException {
		try {
			// Read the request line
			String inLine = in.readLine();
			if (inLine == null) {
				return;
			}

			StringTokenizer st = new StringTokenizer(inLine);
			if (!st.hasMoreTokens()) {
				throw new ResponseException(Response.Status.BAD_REQUEST,
						"BAD REQUEST: Syntax error. Usage: GET /example/file.html");
			}

			pre.put("method", st.nextToken());

			if (!st.hasMoreTokens()) {
				throw new ResponseException(Response.Status.BAD_REQUEST,
						"BAD REQUEST: Missing URI. Usage: GET /example/file.html");
			}

			String uri = st.nextToken();

			// Decode parameters from the URI
			int qmi = uri.indexOf('?');
			if (qmi >= 0) {
				decodeParms(uri.substring(qmi + 1), parms);
				uri = HTTPD.decodePercent(uri.substring(0, qmi));
			} else {
				uri = HTTPD.decodePercent(uri);
			}

			// If there's another token, its protocol version,
			// followed by HTTP headers.
			// NOTE: this now forces header names lower case since they are
			// case insensitive and vary by client.
			if (st.hasMoreTokens()) {
				protocolVersion = st.nextToken();
			} else {
				protocolVersion = "HTTP/1.1";
				LOG.log(Level.FINE, "no protocol version specified, strange. Assuming HTTP/1.1.");
			}
			String line = in.readLine();
			while (line != null && !line.trim().isEmpty()) {
				int p = line.indexOf(':');
				if (p >= 0) {
					headers.put(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
				}
				line = in.readLine();
			}

			pre.put("uri", uri);
		} catch (IOException ioe) {
			throw new ResponseException(Response.Status.INTERNAL_ERROR,
					"SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);
		}
	}

	/**
	 * Decodes the Multipart Body data and put it into Key/Value pairs.
	 */
	private void decodeMultipartFormData(ContentType contentType, ByteBuffer fbuf, Map<String, String> parms,
			Map<String, String> files) throws ResponseException {
		int pcount = 0;
		try {
			int[] boundaryIdxs = getBoundaryPositions(fbuf, contentType.getBoundary().getBytes());
			if (boundaryIdxs.length < 2) {
				throw new ResponseException(Response.Status.BAD_REQUEST,
						"BAD REQUEST: Content type is multipart/form-data but contains less than two boundary strings.");
			}

			byte[] partHeaderBuff = new byte[MAX_HEADER_SIZE];
			for (int boundaryIdx = 0; boundaryIdx < boundaryIdxs.length - 1; boundaryIdx++) {
				fbuf.position(boundaryIdxs[boundaryIdx]);
				int len = (fbuf.remaining() < MAX_HEADER_SIZE) ? fbuf.remaining() : MAX_HEADER_SIZE;
				fbuf.get(partHeaderBuff, 0, len);
				BufferedReader in = new BufferedReader(
						new InputStreamReader(new ByteArrayInputStream(partHeaderBuff, 0, len),
								Charset.forName(contentType.getEncoding())),
						len);

				int headerLines = 0;
				// First line is boundary string
				String mpline = in.readLine();
				headerLines++;
				if (mpline == null || !mpline.contains(contentType.getBoundary())) {
					throw new ResponseException(Response.Status.BAD_REQUEST,
							"BAD REQUEST: Content type is multipart/form-data but chunk does not start with boundary.");
				}

				String partName = null, fileName = null, partContentType = null;
				// Parse the reset of the header lines
				mpline = in.readLine();
				headerLines++;
				while (mpline != null && mpline.trim().length() > 0) {
					Matcher matcher = HTTPD.CONTENT_DISPOSITION_PATTERN.matcher(mpline);
					if (matcher.matches()) {
						String attributeString = matcher.group(2);
						matcher = HTTPD.CONTENT_DISPOSITION_ATTRIBUTE_PATTERN.matcher(attributeString);
						while (matcher.find()) {
							String key = matcher.group(1);
							if ("name".equalsIgnoreCase(key)) {
								partName = matcher.group(2);
							} else if ("filename".equalsIgnoreCase(key)) {
								fileName = matcher.group(2);
								// add these two line to support multiple
								// files uploaded using the same field Id
								if (!fileName.isEmpty()) {
									if (pcount > 0)
										partName = partName + String.valueOf(pcount++);
									else
										pcount++;
								}
							}
						}
					}
					matcher = HTTPD.CONTENT_TYPE_PATTERN.matcher(mpline);
					if (matcher.matches()) {
						partContentType = matcher.group(2).trim();
					}
					mpline = in.readLine();
					headerLines++;
				}
				int partHeaderLength = 0;
				while (headerLines-- > 0) {
					partHeaderLength = scipOverNewLine(partHeaderBuff, partHeaderLength);
				}
				// Read the part data
				if (partHeaderLength >= len - 4) {
					throw new ResponseException(Response.Status.INTERNAL_ERROR,
							"Multipart header size exceeds MAX_HEADER_SIZE.");
				}
				int partDataStart = boundaryIdxs[boundaryIdx] + partHeaderLength;
				int partDataEnd = boundaryIdxs[boundaryIdx + 1] - 4;

				fbuf.position(partDataStart);
				if (partContentType == null) {
					// Read the part into a string
					byte[] data_bytes = new byte[partDataEnd - partDataStart];
					fbuf.get(data_bytes);
					parms.put(partName, new String(data_bytes, contentType.getEncoding()));
				} else {
					// Read it into a file
					String path = saveTmpFile(fbuf, partDataStart, partDataEnd - partDataStart, fileName);
					if (!files.containsKey(partName)) {
						files.put(partName, path);
					} else {
						int count = 2;
						while (files.containsKey(partName + count)) {
							count++;
						}
						files.put(partName + count, path);
					}
					parms.put(partName, fileName);
				}
			}
		} catch (ResponseException re) {
			throw re;
		} catch (Exception e) {
			throw new ResponseException(Response.Status.INTERNAL_ERROR, e.toString());
		}
	}

	private int scipOverNewLine(byte[] partHeaderBuff, int index) {
		while (partHeaderBuff[index] != '\n') {
			index++;
		}
		return ++index;
	}

	/**
	 * Decodes parameters in percent-encoded URI-format ( e.g.
	 * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given Map.
	 * NOTE: this doesn't support multiple identical keys due to the simplicity
	 * of Map.
	 */
	private void decodeParms(String parms, Map<String, String> p) {
		if (parms == null) {
			this.queryParameterString = "";
			return;
		}

		this.queryParameterString = parms;
		StringTokenizer st = new StringTokenizer(parms, "&");
		while (st.hasMoreTokens()) {
			String e = st.nextToken();
			int sep = e.indexOf('=');
			if (sep >= 0) {
				p.put(HTTPD.decodePercent(e.substring(0, sep)).trim(),
						HTTPD.decodePercent(e.substring(sep + 1)));
			} else {
				p.put(HTTPD.decodePercent(e).trim(), "");
			}
		}
	}

	@Override
	public void execute() throws IOException {
		Response r = null;
		try {
			// Read the first 8192 bytes.
			// The full header should fit in here.
			// Apache's default header limit is 8KB.
			// Do NOT assume that a single read will get the entire header
			// at once!
			byte[] buf = new byte[HTTPSession.BUFSIZE];
			this.splitbyte = 0;
			this.rlen = 0;

			int read = -1;
			this.inputStream.mark(HTTPSession.BUFSIZE);
			try {
				read = this.inputStream.read(buf, 0, HTTPSession.BUFSIZE);
			} catch (SSLException e) {
				throw e;
			} catch (IOException e) {
				HTTPD.safeClose(this.inputStream);
				HTTPD.safeClose(this.outputStream);
				throw new SocketException("Httpd Shutdown");
			}
			if (read == -1) {
				// socket was been closed
				HTTPD.safeClose(this.inputStream);
				HTTPD.safeClose(this.outputStream);
				throw new SocketException("Httpd Shutdown");
			}
			while (read > 0) {
				this.rlen += read;
				this.splitbyte = findHeaderEnd(buf, this.rlen);
				if (this.splitbyte > 0) {
					break;
				}
				read = this.inputStream.read(buf, this.rlen, HTTPSession.BUFSIZE - this.rlen);
			}

			if (this.splitbyte < this.rlen) {
				this.inputStream.reset();
				this.inputStream.skip(this.splitbyte);
			}

			this.parms = new HashMap<String, String>();
			if (null == this.headers) {
				this.headers = new HashMap<String, String>();
			} else {
				this.headers.clear();
			}

			// Create a BufferedReader for parsing the header.
			BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, this.rlen)));

			// Decode the header into parms and header java properties
			Map<String, String> pre = new HashMap<String, String>();
			decodeHeader(hin, pre, this.parms, this.headers);

			if (null != this.remoteIp) {
				this.headers.put("remote-addr", this.remoteIp);
				this.headers.put("http-client-ip", this.remoteIp);
			}

			this.method = Method.lookup(pre.get("method"));
			if (this.method == null) {
				throw new ResponseException(Response.Status.BAD_REQUEST,
						"BAD REQUEST: Syntax error. HTTP verb " + pre.get("method") + " unhandled.");
			}

			this.uri = pre.get("uri");

			this.cookies = new CookieHandler(this.headers);

			String connection = this.headers.get("connection");
			boolean keepAlive = "HTTP/1.1".equals(protocolVersion)
					&& (connection == null || !connection.matches("(?i).*close.*"));

			// Ok, now do the serve()

			// TODO: long body_size = getBodySize();
			// TODO: long pos_before_serve = this.inputStream.totalRead()
			// (requires implementation for totalRead())
			r = httpd.serve(this);
			// TODO: this.inputStream.skip(body_size -
			// (this.inputStream.totalRead() - pos_before_serve))

			if (r == null) {
				throw new ResponseException(Response.Status.INTERNAL_ERROR,
						"SERVER INTERNAL ERROR: Serve() returned a null response.");
			} else {
				String acceptEncoding = this.headers.get("accept-encoding");
				this.cookies.unloadQueue(r);
				r.setRequestMethod(this.method);
				r.setGzipEncoding(
						httpd.useGzipWhenAccepted(r) && acceptEncoding != null && acceptEncoding.contains("gzip"));
				r.setKeepAlive(keepAlive);
				r.send(this.outputStream);
			}
			if (!keepAlive || r.isCloseConnection()) {
				throw new SocketException("Httpd Shutdown");
			}
		} catch (SocketException e) {
			// throw it out to close socket object (finalAccept)
			throw e;
		} catch (SocketTimeoutException ste) {
			// treat socket timeouts the same way we treat socket exceptions
			// i.e. close the stream & finalAccept object by throwing the
			// exception up the call stack.
			throw ste;
		} catch (SSLException ssle) {
			Response resp = HTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, HTTPD.MIME_PLAINTEXT,
					"SSL PROTOCOL FAILURE: " + ssle.getMessage());
			resp.send(this.outputStream);
			HTTPD.safeClose(this.outputStream);
		} catch (IOException ioe) {
			Response resp = HTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, HTTPD.MIME_PLAINTEXT,
					"SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			resp.send(this.outputStream);
			HTTPD.safeClose(this.outputStream);
		} catch (ResponseException re) {
			Response resp = HTTPD.newFixedLengthResponse(re.getStatus(), HTTPD.MIME_PLAINTEXT, re.getMessage());
			resp.send(this.outputStream);
			HTTPD.safeClose(this.outputStream);
		} finally {
			HTTPD.safeClose(r);
			this.tempFileManager.clear();
		}
	}

	/**
	 * Find byte index separating header from body. It must be the last byte of
	 * the first two sequential new lines.
	 */
	private int findHeaderEnd(final byte[] buf, int rlen) {
		int splitbyte = 0;
		while (splitbyte + 1 < rlen) {

			// RFC2616
			if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && splitbyte + 3 < rlen
					&& buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n') {
				return splitbyte + 4;
			}

			// tolerance
			if (buf[splitbyte] == '\n' && buf[splitbyte + 1] == '\n') {
				return splitbyte + 2;
			}
			splitbyte++;
		}
		return 0;
	}

	/**
	 * Find the byte positions where multipart boundaries start. This reads a
	 * large block at a time and uses a temporary buffer to optimize (memory
	 * mapped) file access.
	 */
	private int[] getBoundaryPositions(ByteBuffer b, byte[] boundary) {
		int[] res = new int[0];
		if (b.remaining() < boundary.length) {
			return res;
		}

		int search_window_pos = 0;
		byte[] search_window = new byte[4 * 1024 + boundary.length];

		int first_fill = (b.remaining() < search_window.length) ? b.remaining() : search_window.length;
		b.get(search_window, 0, first_fill);
		int new_bytes = first_fill - boundary.length;

		do {
			// Search the search_window
			for (int j = 0; j < new_bytes; j++) {
				for (int i = 0; i < boundary.length; i++) {
					if (search_window[j + i] != boundary[i])
						break;
					if (i == boundary.length - 1) {
						// Match found, add it to results
						int[] new_res = new int[res.length + 1];
						System.arraycopy(res, 0, new_res, 0, res.length);
						new_res[res.length] = search_window_pos + j;
						res = new_res;
					}
				}
			}
			search_window_pos += new_bytes;

			// Copy the end of the buffer to the start
			System.arraycopy(search_window, search_window.length - boundary.length, search_window, 0, boundary.length);

			// Refill search_window
			new_bytes = search_window.length - boundary.length;
			new_bytes = (b.remaining() < new_bytes) ? b.remaining() : new_bytes;
			b.get(search_window, boundary.length, new_bytes);
		} while (new_bytes > 0);
		return res;
	}

	@Override
	public CookieHandler getCookies() {
		return this.cookies;
	}

	@Override
	public final String getHeader(String name) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("name is empty");
		}
		return this.headers.get(name);
	}

	@Override
	public final Map<String, String> getHeaders() {
		return this.headers;
	}

	@Override
	public final InputStream getInputStream() {
		return this.inputStream;
	}

	@Override
	public final Method getMethod() {
		return this.method;
	}

	@Override
	public final String getParameter(String name) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("name is empty");
		}
		return this.parms.get(name);
	}

	@Override
	public final Map<String, String> getParms() {
		return this.parms;
	}

	@Override
	public String getQueryParameterString() {
		return this.queryParameterString;
	}

	private RandomAccessFile getTmpBucket() {
		try {
			TempFile tempFile = this.tempFileManager.createTempFile(null);
			return new RandomAccessFile(tempFile.getName(), "rw");
		} catch (Exception e) {
			throw new Error(e); // we won't recover, so throw an error
		}
	}

	@Override
	public final String getUri() {
		return this.uri;
	}

	@Override
	public final String getProtocolVersion() {
		return this.protocolVersion;
	}

	/**
	 * Deduce body length in bytes. Either from "content-length" header or read
	 * bytes.
	 */
	public long getBodySize() {
		if (this.headers.containsKey("content-length")) {
			return Long.parseLong(this.headers.get("content-length"));
		} else if (this.splitbyte < this.rlen) {
			return this.rlen - this.splitbyte;
		}
		return 0;
	}

	@Override
	public void parseBody(Map<String, String> files) throws IOException, ResponseException {
		RandomAccessFile randomAccessFile = null;
		try {
			long size = getBodySize();
			ByteArrayOutputStream baos = null;
			DataOutput requestDataOutput = null;

			// Store the request in memory or a file, depending on size
			if (size < MEMORY_STORE_LIMIT) {
				baos = new ByteArrayOutputStream();
				requestDataOutput = new DataOutputStream(baos);
			} else {
				randomAccessFile = getTmpBucket();
				requestDataOutput = randomAccessFile;
			}

			// Read all the body and write it to request_data_output
			byte[] buf = new byte[REQUEST_BUFFER_LEN];
			while (this.rlen >= 0 && size > 0) {
				this.rlen = this.inputStream.read(buf, 0, (int) Math.min(size, REQUEST_BUFFER_LEN));
				size -= this.rlen;
				if (this.rlen > 0) {
					requestDataOutput.write(buf, 0, this.rlen);
				}
			}

			ByteBuffer fbuf = null;
			if (baos != null) {
				fbuf = ByteBuffer.wrap(baos.toByteArray(), 0, baos.size());
			} else {
				fbuf = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile.length());
				randomAccessFile.seek(0);
			}

			// If the method is POST, there may be parameters
			// in data section, too, read it:
			if (Method.POST.equals(this.method)) {
				ContentType contentType = new ContentType(this.headers.get("content-type"));
				if (contentType.isMultipart()) {
					String boundary = contentType.getBoundary();
					if (boundary == null) {
						throw new ResponseException(Response.Status.BAD_REQUEST,
								"BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
					}
					decodeMultipartFormData(contentType, fbuf, this.parms, files);
				} else {
					byte[] postBytes = new byte[fbuf.remaining()];
					fbuf.get(postBytes);
					String postLine = new String(postBytes, contentType.getEncoding()).trim();
					// Handle application/x-www-form-urlencoded
					if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType.getContentType())) {
						decodeParms(postLine, this.parms);
					} else if (postLine.length() != 0) {
						// Special case for raw POST data => create a
						// special files entry "postData" with raw content
						// data
						files.put("postData", postLine);
					}
				}
			} else if (Method.PUT.equals(this.method)) {
				files.put("content", saveTmpFile(fbuf, 0, fbuf.limit(), null));
			}
		} finally {
			HTTPD.safeClose(randomAccessFile);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("method").append(":").append(method).append(", ").append("uri").append(":").append(uri).append(", ")
				.append("version").append(":").append(protocolVersion).append("\n");

		// headers
		Iterator<Entry<String, String>> i = headers.entrySet().iterator();
		Entry<String, String> entry;
		sb.append("headers").append(":").append("\n");
		while (i.hasNext()) {
			entry = i.next();
			sb.append("\t").append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
		}

		// parameters
		i = parms.entrySet().iterator();
		sb.append("parameters").append(":").append("\n");
		while (i.hasNext()) {
			entry = i.next();
			sb.append("\t").append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
		}

		return sb.toString();
	}

	/**
	 * Retrieves the content of a sent file and saves it to a temporary file.
	 * The full path to the saved file is returned.
	 */
	private String saveTmpFile(ByteBuffer b, int offset, int len, String filename_hint) {
		String path = "";
		if (len > 0) {
			FileOutputStream fileOutputStream = null;
			try {
				TempFile tempFile = this.tempFileManager.createTempFile(filename_hint);
				ByteBuffer src = b.duplicate();
				fileOutputStream = new FileOutputStream(tempFile.getName());
				FileChannel dest = fileOutputStream.getChannel();
				src.position(offset).limit(offset + len);
				dest.write(src.slice());
				path = tempFile.getName();
			} catch (Exception e) { // Catch exception if any
				throw new Error(e); // we won't recover, so throw an error
			} finally {
				HTTPD.safeClose(fileOutputStream);
			}
		}
		return path;
	}

	@Override
	public String getRemoteIpAddress() {
		return this.remoteIp;
	}

	@Override
	public String getRemoteHostName() {
		return this.remoteHostname;
	}

	/**
	 * @return the httpd
	 */
	public HTTPD getHttpd() {
		return httpd;
	}

	/**
	 * @param httpd
	 *            the httpd to set
	 */
	public void setHttpd(HTTPD httpd) {
		this.httpd = httpd;
	}
}
