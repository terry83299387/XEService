package xextension.operation.file_operator;

import java.io.File;

import xextension.global.Configurations;
import xextension.global.IDGenerator;
import xextension.http.Request;
import xextension.http.Response;
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

	public void doGet(Request request, Response response) throws Exception {
		this.doPost(request, response);
	}

	public void doPost(Request request, Response response) throws Exception {
		String type = request.getParameter(TYPE);
		if (FILE_SIZE.equals(type)) {
			getFileSize(request, response);
		}
	}

	private void getFileSize(Request request, Response response) {
		String filePath = request.getParameter(FILE_PATH);
		if (filePath == null || filePath.trim().length() == 0) {
			throw new IllegalArgumentException("file not specified");
		}

		OperationResult result = new OperationResult(request);
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

		String ret = result.toJsonString();
		response.print(ret);
		response.flush();
	}

}
