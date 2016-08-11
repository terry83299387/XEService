/**
 * 
 */
package xextension.operation.file_transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xextension.global.Configurations;
import xextension.global.IDGenerator;
import xextension.http.IHTTPSession;
import xextension.operation.OperationResult;
import xextension.operation.Processor;
import cn.net.xfinity.applet.ftp.client.AppletProviderFasade;
import cn.net.xfinity.applet.ftp.client.FtpClientApplet;
import cn.net.xfinity.applet.trans.client.beans.TransferBean;
import cn.net.xfinity.applet.trans.client.cache.TransferTaskCache;

/**
 * @author QiaoMingkui
 *
 */
public class FileTransfer extends Processor {
	private static final Logger	logger = LogManager.getLogger(FileTransfer.class);

	private static final String TYPE = "type";

	private static final String UPLOAD = "upload";
	private static final String DOWNLOAD = "download";
	private static final String TRANSFER_LIST = "transferList";
	private static final String REMOVE_COMPLETED = "removeCompleted";
	private static final String TRANSFER_PROGRESS = "transferProgress";

	private static FtpClientApplet client;
	private static Map<String, Map<String, String>> params = new HashMap<String, Map<String, String>>();

	public OperationResult doGet(IHTTPSession session) throws Exception {
		return this.doPost(session);
	}

	public OperationResult doPost(IHTTPSession session) throws Exception {
		OperationResult result = null;

		String type = session.getParameter(TYPE);
		if (UPLOAD.equals(type) || DOWNLOAD.equals(type)) {
			result = _doTransfer(session);
		} else if (TRANSFER_LIST.equals(type)) {
			result = _listTransferTasks(session);
		} else if (REMOVE_COMPLETED.equals(type)) {
			result = _removeCompleted(session);
		} else if (TRANSFER_PROGRESS.equals(type)) {
			result = _getProgress(session);
		}

		return result;
	}

	private OperationResult _removeCompleted(IHTTPSession session) {
		TransferTaskCache.removeCompletedCache();

		OperationResult result = new OperationResult(session);
		String respId = session.getParameter(Configurations.REQUEST_ID);
		if (respId == null) {
			respId = IDGenerator.nextId(this.getClass());
		}
		result.setResponseId(respId);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		return result;
	}

	private OperationResult _listTransferTasks(IHTTPSession session) {
		OperationResult result = new OperationResult(session);
		String respId = session.getParameter(Configurations.REQUEST_ID);
		if (respId == null) {
			respId = IDGenerator.nextId(this.getClass());
		}
		result.setResponseId(respId);

		if (client == null) {
			result.setReturnCode(Configurations.UNSUPPORT_OPERATION);
			result.setException("transfer has not initialized");

			return result;
		}

		TransferBean[] transferBeans = TransferTaskCache.getAllTransferTask();
		List<TransferFileBean> transferFileBeans;
		if (transferBeans != null) {
			transferFileBeans = new ArrayList<TransferFileBean>(transferBeans.length);
			TransferFileBean transferFileBean;
			for (TransferBean transferBean : transferBeans) {
				if (transferBean.getFileType() != TransferFileBean.FILE) {
					continue;
				}

				transferFileBean = new TransferFileBean();
				transferFileBean.setServerName(transferBean.getServerName());
				transferFileBean.setFileName(_getFileName(transferBean.getLocalFilePath()));
				transferFileBean.setLocalPath(_getFilePath(transferBean.getLocalFilePath()));
				transferFileBean.setRemotePath(_getFilePath(transferBean.getRemoteFilePath()));
				if (transferBean.isUpload()) {
					transferFileBean.setTransferType(TransferFileBean.UPLOAD);
				} else {
					transferFileBean.setTransferType(TransferFileBean.DOWNLOAD);
				}
				transferFileBean.setTransferedSize(transferBean.getHasTransferredSize());
				transferFileBean.setTotalSize(transferBean.getTotalSize());
				transferFileBean.setStatus(transferBean.getTransferStatus());

				transferFileBeans.add(transferFileBean);
			}
		} else {
			transferFileBeans = new ArrayList<TransferFileBean>(0);
		}

		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		result.setExtraData("transferList", transferFileBeans);

		return result;
	}

	private String _getFileName(String filePath) {
		if (filePath == null || filePath.trim().length() == 0) {
			return "(unknown_file)";
		}

		filePath = filePath.trim();

		if (filePath.endsWith("/") || filePath.endsWith("\\")) {
			return "(empty_name)";
		}

		int idx1 = filePath.lastIndexOf("/");
		int idx2 = filePath.lastIndexOf("\\");
		int idx = Math.max(idx1, idx2);
		if (idx == -1) {
			return filePath;
		}

		return filePath.substring(idx + 1);
	}

	private String _getFilePath(String filePath) {
		if (filePath == null || filePath.trim().length() == 0) {
			return "(unknown_path)";
		}

		filePath = filePath.trim();

		if (filePath.endsWith("/") || filePath.endsWith("\\")) {
			return filePath;
		}

		int idx1 = filePath.lastIndexOf("/");
		int idx2 = filePath.lastIndexOf("\\");
		int idx = Math.max(idx1, idx2);
		if (idx == -1) {
			return "";
		}

		return filePath.substring(0, idx);
	}

	private OperationResult _doTransfer(IHTTPSession session) throws Exception {
		Map<String, String> args = session.getParms();
		if (client == null) {
			try {
				initClient(args);
			} catch (Exception e) {
				if (client != null) {
					client.destroy();
					client = null;
				}
				throw e;
			}
		} else {
			setParams(args);
		}

		String targetPath = null;
		String module = session.getParameter("module");
		if ("job".equals(module)) {
			client.initDirectTranfser();
			targetPath = args.get("home");
		} else {
			client.initTransfer();
//			targetPath = client.getDownloadDir(); // TODO 下载时会在另一个线程中弹出选择文件对话框，所以这里是获取不到存储目录的
												  // 并且用户还可能会点击文件选择框中的取消按钮取消下载，实现时需要全面考虑这些因素。
												  // 不过目前不需要保存下载参数，所以这里暂时不做处理
		}

		OperationResult result = new OperationResult(session);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		String respId = IDGenerator.nextId(this.getClass());
		result.setResponseId(respId);

		// store params used to get transfer progress
		if ("job".equals(module)) { // TODO 目前只储存提交作业时的文件上传参数，因为下载进度的获取有问题
			Map<String, String> params = new HashMap<String, String>();
			params.put("serverName", args.get("serverName"));
			params.put("type",       args.get("type"));
			params.put("files",      args.get("files"));
			params.put("targetPath", targetPath);
			FileTransfer.params.put(respId, params);
		}

		return result;
	}

	private OperationResult _getProgress(IHTTPSession session) {
		if (client == null) {
			throw new IllegalStateException("transfer has not been initialized");
		}

		String reqId = session.getParameter(Configurations.REQUEST_ID);
		Map<String, String> params = FileTransfer.params.get(reqId);
		if (params == null) {
			throw new IllegalStateException("task not found");
		}

		String serverName = params.get("serverName");
		String type       = params.get("type");
		String files      = params.get("files");
		String targetPath = params.get("targetPath");

		String progress = client.getTaskPercentage(serverName, type, files, targetPath);
		int percentage = -1;
		String exception = null;
		try {
			percentage = Integer.parseInt(progress);
			if (percentage >= 100) {
				percentage = 100;
				// remove stored parameters while transfer completes
				FileTransfer.params.remove(reqId);
			}
		} catch (NumberFormatException e) {
			exception = progress;
		}

		OperationResult result = new OperationResult(session);
		result.setResponseId(reqId);
		result.setExtraData("percentage", percentage);
		if (exception != null) {
			result.setReturnCode(Configurations.UNKNOWN_ERROR);
			result.setException(exception);
		} else {
			result.setReturnCode(Configurations.OPERATION_SUCCEED);
		}

		return result;
	}

	private void initClient(Map<String, String> args) {
		synchronized (FileTransfer.class) {
			if (client != null) {
				setParams(args);
				return;
			}

			client = new FtpClientApplet();
			client.isAppRun = true;
			AppletProviderFasade.registerApplet(client);
			setParams(args);
			client.initComponent(client);
			client.initService();
			JFrame frame = new JFrame();
			client.startWindowFrame(frame);
			// do not show window at first
//			frame.setVisible(true);
		}
	}

	private void setParams(Map<String, String> params) {
		client.clearParameters();

		try {
			client.setParameter("host",                 params.get("host"));
			client.setParameter("user",                 params.get("user"));
			client.setParameter("passwd",               params.get("passwd"));
			client.setParameter("port",                 params.get("port"));
			client.setParameter("fileTransferProtocol", params.get("protocol"));
			client.setParameter("servername",           params.get("serverName"));
			client.setParameter("clientkey",            params.get("clientKey"));
			client.setParameter("home",                 params.get("home"));
			client.setParameter("files",                params.get("files"));
			client.setParameter("dlgtype",              params.get("type"));
			client.setParameter("rootpath",             params.get("rootPath"));
			client.setParameter("defaultpath",          params.get("defaultPath"));
			client.setParameter("portaluser",           params.get("portalUser"));
			client.setParameter("enableextend",         params.get("enableExtend"));
			client.setParameter("serverclass",          params.get("serverClass"));
			client.setParameter("language",             params.get("language"));
		} catch (Exception e) {
			logger.error("file transfer error", e);
		}

		client.initParameters();
	}

	/**
	 * test
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
//		FileTransfer trans = new FileTransfer();
//		trans.doPost(null, null);
	}
}
