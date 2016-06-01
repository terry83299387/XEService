package xextension.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The runnable that will be used for every new client connection.
 */
public class ClientHandler implements Runnable {
  /**
   * logger to log to.
   */
  private static final Logger LOG = Logger.getLogger(ClientHandler.class.getName());

  private final InputStream inputStream;

  private final Socket acceptSocket;

  private HTTPD httpd;

  public ClientHandler(InputStream inputStream, Socket acceptSocket) {
      this.inputStream = inputStream;
      this.acceptSocket = acceptSocket;
  }

  public void close() {
      HTTPD.safeClose(this.inputStream);
      HTTPD.safeClose(this.acceptSocket);
  }

  @Override
  public void run() {
      OutputStream outputStream = null;
      try {
          outputStream = this.acceptSocket.getOutputStream();
          TempFileManager tempFileManager = httpd.getTempFileManagerFactory().create();
          HTTPSession session = new HTTPSession(tempFileManager, this.inputStream, outputStream, this.acceptSocket.getInetAddress());
          while (!this.acceptSocket.isClosed()) {
          	session.setHttpd(httpd);
            session.execute();
          }
      } catch (Exception e) {
          // When the socket is closed by the client,
          // we throw our own SocketException
          // to break the "keep alive" loop above. If
          // the exception was anything other
          // than the expected SocketException OR a
          // SocketTimeoutException, print the
          // stacktrace
          if (!(e instanceof SocketException && "Httpd Shutdown".equals(e.getMessage())) && !(e instanceof SocketTimeoutException)) {
              LOG.log(Level.SEVERE, "Communication with the client broken, or an bug in the handler code", e);
          }
      } finally {
      	HTTPD.safeClose(outputStream);
      	HTTPD.safeClose(this.inputStream);
      	HTTPD.safeClose(this.acceptSocket);
      	httpd.asyncRunner.closed(this);
      }
  }

	/**
	 * @return the httpd
	 */
	public HTTPD getHttpd() {
		return httpd;
	}

	/**
	 * @param httpd the httpd to set
	 */
	public void setHttpd(HTTPD httpd) {
		this.httpd = httpd;
	}
}
