/**
 * 
 */
package xextension.operation.remote_desktop;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import scc.net.cn.client.ClientApplet;
import scc.net.cn.client.ClientProxyDeamon;

import xextension.global.Configurations;
import xextension.global.IDGenerator;
import xextension.http.Request;
import xextension.http.Response;
import xextension.operation.OperationResult;
import xextension.operation.Processor;

/**
 * 
 * @author QiaoMingkui
 *
 */
public class RemoteDesktop extends Processor {
	private static Logger logger = LogManager.getLogger(RemoteDesktop.class);

	@Override
	public void doGet(Request request, Response response) throws Exception {
		this.doPost(request, response);
	}

	@Override
	public void doPost(Request request, Response response) throws Exception {
		ClientApplet client = new ClientApplet();
		ClientApplet.applet = client;
		initParams();

		try {
			Class<ClientApplet> clazz = ClientApplet.class;
			Field proxy = clazz.getDeclaredField("proxy");
			proxy.setAccessible(true);

			if (proxy.get(client) == null) {
				proxy.set(client, (new ClientProxyDeamon()).startDeamon());
				client.startVncViewer();
			}
		} catch (Exception e) {
			logger.info("RemoteDesktop init error", e.getMessage());
		}

		OperationResult result = new OperationResult(request);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		String respId = IDGenerator.nextId(this.getClass());
		result.setResponseId(respId);
		response.print(result.toJsonString());
		response.flush();
	}

	private void initParams() {
		ClientApplet.connectAddr      = "xfinity.net.cn";
		ClientApplet.connectPort      = 6000;
		ClientApplet.clientkey        = "fapv9fla";
		ClientApplet.password         = "ed3aa70470840fa214573d83607687db";
		ClientApplet.appName          = "abaqus";
		ClientApplet.appVersion       = "6.10";
		ClientApplet.appInitParams    = "";
		ClientApplet.serverInitParams = " -geometry 1600x900 -depth 24";
		ClientApplet.acquirement      = "A1";
		ClientApplet.clusterName      = "蜂鸟LinuxHPC";
		ClientApplet.hostUserName     = "56947ebf485933a1";
		ClientApplet.desktopName      = "";
		ClientApplet.number           = "";
		ClientApplet.workDir          = "/home/linux/users/rdtest/jieliu";
		ClientApplet.displayUserName  = "jieliu";
		ClientApplet.monitorDesktop   = false;
		ClientApplet.directDesktop    = false;
		ClientApplet.encrypt          = true;
	}

	public static void main(String[] args) throws Exception {
		RemoteDesktop desktop = new RemoteDesktop();
		desktop.doPost(null, null);
	}
}
