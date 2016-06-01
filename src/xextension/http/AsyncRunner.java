package xextension.http;

/**
 * Pluggable strategy for asynchronously executing requests.
 */
public interface AsyncRunner {

  void closeAll();

  void closed(ClientHandler clientHandler);

  void exec(ClientHandler code);
}
