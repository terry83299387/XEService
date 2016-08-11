/**
 * 
 */
package xextension.operation.remote_desktop;

import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import scc.net.cn.client.ClientApplet;
import scc.net.cn.client.ClientProxyDeamon;
import scc.net.cn.client.ShowInfo;

import xextension.global.Configurations;
import xextension.global.IDGenerator;
import xextension.http.IHTTPSession;
import xextension.operation.OperationResult;
import xextension.operation.Processor;

/**
 * 
 * @author QiaoMingkui
 *
 */
public class RemoteDesktop extends Processor {
	private static Logger logger = LogManager.getLogger(RemoteDesktop.class);
	private static ClientApplet client;

	@Override
	public OperationResult doGet(IHTTPSession session) throws Exception {
		return this.doPost(session);
	}

	@Override
	public OperationResult doPost(IHTTPSession session) throws Exception {
		StringBuilder cmds = new StringBuilder("java -cp libs\\xextension.jar")
			.append(";libs\\remotedesktop0.jar")
			.append(";libs\\remotedesktop.jar")
			.append(";libs\\commons-logging-1.1.1.jar")
			.append(";libs\\commons-net-ftp-3.0.jar")
			.append(";libs\\json.jar")
			.append(";libs\\jsch-0.1.50.jar")
			.append(";libs\\jzlib-1.1.3.jar")
			.append(";libs\\log4j-api-2.4.1.jar")
			.append(";libs\\")
			.append(";libs\\log4j-core-2.4.1.jar")
			.append(";libs\\plugin.jar")
			.append(";libs\\swing-layout-1.0.jar")
			.append(" xextension.operation.remote_desktop.RemoteDesktop")
			.append(" \"").append(session.getParameter("connectAddr")).append("\" ")
			.append("\"").append(session.getParameter("connectPort")).append("\" ")
			.append("\"").append(session.getParameter("clientkey")).append("\" ")
			.append("\"").append(session.getParameter("password")).append("\" ")
			.append("\"").append(session.getParameter("appName")).append("\" ")
			.append("\"").append(session.getParameter("appVersion")).append("\" ")
			.append("\"").append(session.getParameter("appInitParams")).append("\" ")
			.append("\"").append(session.getParameter("serverInitParams")).append("\" ")
			.append("\"").append(session.getParameter("acquirement")).append("\" ")
			.append("\"").append(session.getParameter("clusterName")).append("\" ")
			.append("\"").append(session.getParameter("hostUserName")).append("\" ")
			.append("\"").append(session.getParameter("workDir")).append("\" ")
			.append("\"").append(session.getParameter("displayUserName")).append("\"");

		try {
			logger.info("cmds: " + cmds.toString());
			Runtime.getRuntime().exec(cmds.toString());
		} catch (IOException e) {
			throw new IllegalAccessError("can not open remote app");
		}

//		if (client == null) {
//			client = new ClientApplet();
//			ClientApplet.applet = client;
//		}
//		initParams(request);
//
//		try {
//			Class<ClientApplet> clazz = ClientApplet.class;
//			Field proxy = clazz.getDeclaredField("proxy");
//			proxy.setAccessible(true);
//
////			if (proxy.get(client) == null) {
//				proxy.set(client, (new ClientProxyDeamon()).startDeamon());
////			}
////		logger.info("connection info: " + ClientApplet.connectAddr + ", " + ClientApplet.connectPort);
//			client.startVncViewer();
//		} catch (Exception e) {
//			logger.info("RemoteDesktop init error", e.getMessage());
//		}

		OperationResult result = new OperationResult(session);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		String respId = IDGenerator.nextId(this.getClass());
		result.setResponseId(respId);

		return result;
	}

	private void initParams(IHTTPSession session) {
		ClientApplet.connectAddr      = session.getParameter("connectAddr"); // "xfinity.net.cn"
		ClientApplet.connectPort      = Integer.parseInt(session.getParameter("connectPort")); // 6000
		ClientApplet.clientkey        = session.getParameter("clientkey"); // "fapv9fla"
		ClientApplet.password         = session.getParameter("password"); // "ed3aa70470840fa214573d83607687db"
		ClientApplet.appName          = session.getParameter("appName"); // "abaqus"
		ClientApplet.appVersion       = session.getParameter("appVersion"); // "6.10"
		ClientApplet.appInitParams    = session.getParameter("appInitParams"); // ""
		ClientApplet.serverInitParams = session.getParameter("serverInitParams"); // " -geometry 1600x900 -depth 24"
		ClientApplet.acquirement      = session.getParameter("acquirement"); // "A1"
		ClientApplet.clusterName      = session.getParameter("clusterName"); // "蜂鸟LinuxHPC"
		ClientApplet.hostUserName     = session.getParameter("hostUserName"); // "56947ebf485933a1"
		ClientApplet.workDir          = session.getParameter("workDir"); // "/home/linux/users/rdtest/jieliu"
		ClientApplet.displayUserName  = session.getParameter("displayUserName"); // "jieliu"
		ClientApplet.desktopName      = "";
		ClientApplet.number           = "";
		ClientApplet.monitorDesktop   = false;
		ClientApplet.directDesktop    = false;
		ClientApplet.encrypt          = true;
	}

	public void doPost() throws Exception {
		if (client == null) {
			client = new ClientApplet();
			ClientApplet.applet = client;
		}
		initParams();

		try {
			Class<ClientApplet> clazz = ClientApplet.class;
			Field proxy = clazz.getDeclaredField("proxy");
			proxy.setAccessible(true);

			if (proxy.get(client) == null) {
				proxy.set(client, (new ClientProxyDeamon()).startDeamon());
			}
			client.startVncViewer();
		} catch (Exception e) {
			logger.info("RemoteDesktop init error", e.getMessage());
		}
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
		ClientApplet.workDir          = "/home/linux/users/rdtest/jieliu";
		ClientApplet.displayUserName  = "jieliu";
		ClientApplet.desktopName      = "";
		ClientApplet.number           = "";
		ClientApplet.monitorDesktop   = false;
		ClientApplet.directDesktop    = false;
		ClientApplet.encrypt          = true;
	}
	private static void openApp(String[] args) {
		ClientApplet client = new ClientApplet();
		ClientApplet.applet = client;

		ClientApplet.connectAddr      = args[0];
		ClientApplet.connectPort      = Integer.parseInt(args[1]);
		ClientApplet.clientkey        = args[2];
		ClientApplet.password         = args[3];
		ClientApplet.appName          = args[4];
		ClientApplet.appVersion       = args[5];
		ClientApplet.appInitParams    = args[6];
		ClientApplet.serverInitParams = args[7];
		ClientApplet.acquirement      = args[8];
		ClientApplet.clusterName      = args[9];
		ClientApplet.hostUserName     = args[10];
		ClientApplet.workDir          = args[11];
		ClientApplet.displayUserName  = args[12];

		ClientApplet.desktopName      = "";
		ClientApplet.number           = "";
		ClientApplet.monitorDesktop   = false;
		ClientApplet.directDesktop    = false;
		ClientApplet.encrypt          = true;
//		ShowInfo.showMessage(null, "connectAddr=" + ClientApplet.connectAddr
//				+ "\nconnectPort=" + ClientApplet.connectPort
//				+ "\nclientkey=" + ClientApplet.clientkey
//				+ "\npassword=" + ClientApplet.password
//				+ "\nappName=" + ClientApplet.appName
//				+ "\nappVersion=" + ClientApplet.appVersion
//				+ "\nappInitParams=" + ClientApplet.appInitParams
//				+ "\nserverInitParams=" + ClientApplet.serverInitParams
//				+ "\nacquirement=" + ClientApplet.acquirement
//				+ "\nclusterName=" + ClientApplet.clusterName
//				+ "\nhostUserName=" + ClientApplet.hostUserName
//				+ "\nworkDir=" + ClientApplet.workDir
//				+ "\ndisplayUserName=" + ClientApplet.displayUserName);

		try {
			Class<ClientApplet> clazz = ClientApplet.class;
			Field proxy = clazz.getDeclaredField("proxy");
			proxy.setAccessible(true);
			proxy.set(client, (new ClientProxyDeamon()).startDeamon());
			client.startVncViewer();
		} catch (Exception e) {
			logger.info("RemoteDesktop init error", e.getMessage());
			ShowInfo.showMessage(null, "RemoteDesktop init error:" + e.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {
		openApp(args);

		// test1
//		args = new String[] {
//				"xfinity.net.cn",
//				"6000",
//				"zluqu5wg",
//				"445c3903016b0e15deca300c5cdb2457",
//				"CFX",
//				"11.0",
//				"",
//				" -depth 24 -geometry 1680x1050",
//				"A1",
//				"蜂鸟LinuxHPC", // "SSC_HBird"不行，集群名称与router上的权限配置不符
//				"8ad8bf2f5159894e",
//				"/home/linux/users/rdtest/jhchen",
//				"jhchen"
//		};
//		openApp(args);

		// test2
//		RemoteDesktop desktop = new RemoteDesktop();
//		desktop.doPost();

		// test3(需要与test2一起运行)
//		Thread.sleep(60000);
//		System.out.println("111111111111111");
//		desktop.doPost();
	}
}
