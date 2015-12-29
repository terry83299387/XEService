/**
 * 
 */
package xextension.operation.file_transfer;

/**
 * @author QiaoMingkui
 *
 */
public class TransferFileBean {
	// file type (from FileType)
	public static final int FILE = 1;
	public static final int DIRECTORY = 2;

	// status (from TransferStatus)
	public static final int WAITING = 500;
	public static final int PAUSE = 501;
	public static final int RESUME = 502;
	public static final int CANCELLING = 503;
	public static final int TRANSFERRING = 504;
	public static final int COMPLETED = 505;
	public static final int FAILED = 506;
	public static final int RETRANSFER = 507;
	public static final int UNKNOWN = 508;
	public static final int CANCEL_FAILED = 509;
	public static final int CANCELLED = 510;
	public static final int CONNECT_ERROR = 511;
	public static final int CONNECT_FAILED = 512;
	public static final int CONNECT_TIMEOUT = 513;
	public static final int PAUSING = 514;
	public static final int TASK_UNSUPPORT = 520;

	// transfer type (self-defined)
	public static final int UPLOAD = 1;
	public static final int DOWNLOAD = 2;

	private String serverName;
	private String fileName;
	private String localPath;
	private String remotePath;
	private int status;
	private int transferType; // upload or download
	private int fileType; // file or directory
	private long transferedSize;
	private long totalSize;

	/**
	 * @return the serverName
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * @param serverName the serverName to set
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the localPath
	 */
	public String getLocalPath() {
		return localPath;
	}

	/**
	 * @param localPath the localPath to set
	 */
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	/**
	 * @return the remotePath
	 */
	public String getRemotePath() {
		return remotePath;
	}

	/**
	 * @param remotePath the remotePath to set
	 */
	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the transferType
	 */
	public int getTransferType() {
		return transferType;
	}

	/**
	 * @param transferType the transferType to set
	 */
	public void setTransferType(int transferType) {
		this.transferType = transferType;
	}

	/**
	 * @return the fileType
	 */
	public int getFileType() {
		return fileType;
	}

	/**
	 * @param fileType the fileType to set
	 */
	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	/**
	 * @return the transferedSize
	 */
	public long getTransferedSize() {
		return transferedSize;
	}

	/**
	 * @param transferedSize the transferedSize to set
	 */
	public void setTransferedSize(long transferedSize) {
		this.transferedSize = transferedSize;
	}

	/**
	 * @return the totalSize
	 */
	public long getTotalSize() {
		return totalSize;
	}

	/**
	 * @param totalSize the totalSize to set
	 */
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

}
