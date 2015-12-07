package xextension.operation.file_browser;

import javax.swing.JFileChooser;

import xextension.global.Configurations;
import xextension.global.IDGenerator;
import xextension.http.Request;
import xextension.http.Response;
import xextension.operation.OperationResult;
import xextension.operation.Processor;
import xextension.operation.TimeoutException;

/**
 * 
 * @author QiaoMingkui
 * 
 */
public class FileBrowser extends Processor {
	public static final int			FILES_ONLY					= JFileChooser.FILES_ONLY;
	public static final int			DIRECTORIES_ONLY		= JFileChooser.DIRECTORIES_ONLY;
	public static final int			FILES_DIRECTORIES		= JFileChooser.FILES_AND_DIRECTORIES;
	public static final String	FILE_SEPARATOR			= "|";

	private static final String	MULTI_SELECTION			= "multi";
	private static final String	SELECTION_MODE			= "mode";
	private static final String	DEFAULT_DIR					= "defaultDir";
	private static final String	FILTER							= "filter";
	private static final String	FILTER_DESC					= "filterDesc";
	private static final String SELECTED_FILES			= "selectedFiles";
	private static final String TYPE								= "type";
	private static final String CANCEL							= "cancel";
	private static final String	FILTER_SEPARATOR		= ",";

	private static LocalFileBrowser	fileBrowser;
	private static String currentId;

	public FileBrowser() {
	}

	public void doGet(Request request, Response response) throws Exception {
		this.doPost(request, response);
	}

	// need a synchronization?
	public void doPost(Request request, Response response) throws Exception {
		OperationResult result;

		String id = request.getParameter(Configurations.REQUEST_ID);
		// cancel or query a prior request's result 
		if (id != null && id.trim().length() > 0) {
			String type = request.getParameter(TYPE);
			if (CANCEL.equals(type)) {
				result = cancelSelection(request, id);
			} else {
				result = queryResult(request, id);
			}

		} else if (fileBrowser == null) { // new browser
			result = newBrowser(request);

		} else { // can only open one fileBrowser in time
			// 如果之前的某次请求打开了文件选择对话框，但随后请求主体却消失了（例如用户关闭了浏览器），
			// 此时用户必须先主动关闭之前的对话框才能打开新的对话框（客户端应该尽量在离开之前取消未完成的请求）
			result = new OperationResult(request);
			result.setReturnCode(Configurations.UNSUPPORT_OPERATION);
			result.setException("File Browser has been opening");
		}

		response.print(result.toJsonString());
		response.flush();
	}

	private OperationResult newBrowser(Request request) {
		currentId = IDGenerator.nextId(this.getClass());
		fileBrowser = new LocalFileBrowser();

		//---------------- setup attributes ---------------
		String multiSelection = request.getParameter(MULTI_SELECTION);
		fileBrowser.setMultiSelection("true".equals(multiSelection));

		try {
			String fileSelectionMode = request.getParameter(SELECTION_MODE);
			fileBrowser.setFileSelectionMode(Integer.parseInt(fileSelectionMode));
		} catch (NumberFormatException e) {
		}

		String defaultDir = request.getParameter(DEFAULT_DIR);
		fileBrowser.setDefaultDirectory(defaultDir);

		String filter = request.getParameter(FILTER);
		String filterDesc = request.getParameter(FILTER_DESC);
		if (filter != null && filter.trim().length() != 0) {
			fileBrowser.setFileFilter(filter.split(FILTER_SEPARATOR), filterDesc);
		}
		//-------------- end setup attributes --------------

		fileBrowser.chooseFile();

		OperationResult result = new OperationResult(request);
		result.setResponseId(currentId);
		result.setReturnCode(Configurations.OPERATION_UNCOMPLETED);

		return result;
	}

	private OperationResult queryResult(Request request, String id) {
		OperationResult result = new OperationResult(request);
		result.setResponseId(id);

		if (fileBrowser == null || !id.equals(currentId)) {
			result.setReturnCode(Configurations.UNKNOWN_ID);
			result.setException("[FileBrowser] no data");
		} else {
			try {
				String selectedFiles = tryToGetSelectedFiles();
				result.setReturnCode(Configurations.OPERATION_SUCCEED);
				result.setExtraData(SELECTED_FILES, selectedFiles);
				fileBrowser = null;
				currentId = null;
			} catch (TimeoutException e) {
				result.setReturnCode(Configurations.OPERATION_UNCOMPLETED);
			}
		}

		return result;
	}

	private OperationResult cancelSelection(Request request, String id) {
		OperationResult result = new OperationResult(request);
		result.setResponseId(id);

		if (fileBrowser == null || !id.equals(currentId)) {
			result.setReturnCode(Configurations.UNKNOWN_ID);
			result.setException("[FileBrowser] incorrect id");
		} else if (!fileBrowser.hasSelected()) {
			fileBrowser.closeFileChooser();
			result.setReturnCode(Configurations.OPERATION_SUCCEED);
			fileBrowser = null;
			currentId = null;
		}

		return result;
	}

	private String tryToGetSelectedFiles() throws TimeoutException  {
		long start = System.currentTimeMillis();
		do {
			if (fileBrowser == null) { // has been cancelled
				return "";
			}

			if (fileBrowser.hasSelected()) {
				return fileBrowser.getSelectedFiles();
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
		} while (System.currentTimeMillis() - start < Configurations.REQUEST_TIME_OUT);

		throw new TimeoutException("request timeout");
	}

}
