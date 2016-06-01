package xextension.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The runnable that will be used for the main listening thread.
 */
public class ServerRunnable implements Runnable {
  /**
   * logger to log to.
   */
  private static final Logger LOG = Logger.getLogger(ServerRunnable.class.getName());

  private final int timeout;

  private IOException bindException;

  private boolean hasBinded;

  private HTTPD httpd;

  public ServerRunnable(int timeout) {
      this.timeout = timeout;
  }

  @Override
  public void run() {
      try {
      	  httpd.getMyServerSocket().bind(httpd.getHostname() != null
      	  		? new InetSocketAddress(httpd.getHostname(), httpd.getMyPort())
      	  		: new InetSocketAddress(httpd.getMyPort()));
          hasBinded = true;
      } catch (IOException e) {
          this.bindException = e;
          return;
      }
      do {
          try {
              final Socket finalAccept = httpd.getMyServerSocket().accept();
              if (this.timeout > 0) {
                  finalAccept.setSoTimeout(this.timeout);
              }
              final InputStream inputStream = finalAccept.getInputStream();
              httpd.asyncRunner.exec(httpd.createClientHandler(finalAccept, inputStream));
          } catch (IOException e) {
              LOG.log(Level.FINE, "Communication with the client broken", e);
          }
      } while (!httpd.getMyServerSocket().isClosed());
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

	/**
	 * @return the bindException
	 */
	public IOException getBindException() {
		return bindException;
	}

	/**
	 * @param bindException the bindException to set
	 */
	public void setBindException(IOException bindException) {
		this.bindException = bindException;
	}

	/**
	 * @return the hasBinded
	 */
	public boolean hasBinded() {
		return hasBinded;
	}

	/**
	 * @param hasBinded the hasBinded to set
	 */
	public void setHasBinded(boolean hasBinded) {
		this.hasBinded = hasBinded;
	}
}
