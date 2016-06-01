package xextension.http;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Factory to create ServerSocketFactories.
 */
public interface ServerSocketFactory {
	ServerSocket create() throws IOException;
}
