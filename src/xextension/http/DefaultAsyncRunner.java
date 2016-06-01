package xextension.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Default threading strategy for HTTPD.
 * <p/>
 * <p>
 * By default, the server spawns a new Thread for every incoming request.
 * These are set to <i>daemon</i> status, and named according to the request
 * number. The name is useful when profiling the application.
 * </p>
 */
public class DefaultAsyncRunner implements AsyncRunner {

  private final List<ClientHandler> running = Collections.synchronizedList(new ArrayList<ClientHandler>());
  private final ExecutorService service;

  public DefaultAsyncRunner() {
  	service = Executors.newCachedThreadPool(new ThreadFactory() {
  	  private long requestCount;

  	  @Override
  		public Thread newThread(Runnable r) {
        ++this.requestCount;
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("Httpd Request Processor (#" + this.requestCount + ")");
        return t;
  		}
  	});
	}

  /**
   * @return a list with currently running clients.
   */
  public List<ClientHandler> getRunning() {
      return running;
  }

  @Override
  public void closeAll() {
      // copy of the list for concurrency
      for (ClientHandler clientHandler : new ArrayList<ClientHandler>(this.running)) {
          clientHandler.close();
      }
      service.shutdownNow();
  }

  @Override
  public void closed(ClientHandler clientHandler) {
      this.running.remove(clientHandler);
  }

  @Override
  public void exec(ClientHandler clientHandler) {
  	  this.running.add(clientHandler);
      service.execute(clientHandler);
  }
}
