package xextension.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xextension.global.Configurations;
import xextension.operation.Processor;

/**
 * XEService stands for Xfinity Extension Service.
 * 
 * @author QiaoMingkui
 * 
 */
public class XEService {
	private static final Logger logger = LogManager.getLogger(XEService.class);

	private ServerSocket	server;

	public XEService() throws IllegalStateException {
		String msg = "";
		for (int p : Configurations.CANDIDATE_PORTS) {
			try {
				server = new ServerSocket(p);
				logger.info("server starts on port: " + p);
				break;
			} catch (IOException e) {
				msg = e.getMessage();
			}
		}
		if (server == null) {
			throw new IllegalStateException(msg);
		}
	}

	public void startService() {
		try {
			Socket connection = null;
			while (true) {
					connection = server.accept();
					Processor.dispatchService(connection);
			}
		} catch (Exception e) {
			logger.error("an error occurs: ", e);
		}
	}

}
