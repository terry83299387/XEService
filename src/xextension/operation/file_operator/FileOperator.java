package xextension.operation.file_operator;

import java.io.File;

import xextension.global.Configurations;
import xextension.global.IDGenerator;
import xextension.http.IHTTPSession;
import xextension.operation.OperationResult;
import xextension.operation.Processor;

/**
 * Provide a set of operation of local files, such as get file size.
 * 
 * @author QiaoMingkui
 * 
 */
public class FileOperator extends Processor {

	private static final String TYPE = "type";
	private static final String FILE_SIZE = "fileSize";
	private static final String FILE_PATH = "file";
	private static final String SIZE = "size";

	public FileOperator() {
	}

	public OperationResult doGet(IHTTPSession session) throws Exception {
		return this.doPost(session);
	}

	public OperationResult doPost(IHTTPSession session) throws Exception {
		String type = session.getParameter(TYPE);
		if (FILE_SIZE.equals(type)) {
			return getFileSize(session);
		}
		return null; // TODO should not return null
	}

	private OperationResult getFileSize(IHTTPSession session) {
		String filePath = session.getParameter(FILE_PATH);
		if (filePath == null || filePath.trim().length() == 0) {
			throw new IllegalArgumentException("file not specified");
		}

		OperationResult result = new OperationResult(session);
		String respId = IDGenerator.nextId(this.getClass());
		result.setResponseId(respId);	

		File file = new File(filePath);
		if (!file.exists() || !file.canRead()) {
			result.setReturnCode(Configurations.UNSUPPORT_OPERATION);
			result.setException("operation failed, file does not exist or is not readable: " + filePath);
		} else if (file.isDirectory()) {
			result.setReturnCode(Configurations.UNSUPPORT_OPERATION);
			result.setException("operation failed, the specified file path is not a file but a folder: " + filePath);
		} else {
			result.setExtraData(SIZE, file.length());
			result.setReturnCode(Configurations.OPERATION_SUCCEED);
		}

		return result;
	}

}
