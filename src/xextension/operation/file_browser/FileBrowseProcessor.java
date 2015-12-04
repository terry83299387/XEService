package xextension.operation.file_browser;

import javax.swing.JFileChooser;

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
public class FileBrowseProcessor extends Processor {
	public static final String	MULTI_SELECTION			= "multi";
	public static final String	SELECTION_MODE			= "mode";
	public static final String	DEFAULT_DIR					= "defaultDir";
	public static final String	FILTER							= "filter";
	public static final String	FILTER_DESC					= "filterDesc";
	public static final String	FILTER_SEPARATOR		= ",";

	public static final int			FILES_ONLY					= JFileChooser.FILES_ONLY;
	public static final int			DIRECTORIES_ONLY		= JFileChooser.DIRECTORIES_ONLY;
	public static final int			FILES_DIRECTORIES		= JFileChooser.FILES_AND_DIRECTORIES;
	public static final String	FILE_SEPARATOR			= "|";

	private static LocalFileBrowser	fileBrowser;
	private static String currentId;

	public FileBrowseProcessor() {
	}

	public void doGet(Request request, Response response) throws Exception {
		this.doPost(request, response);
	}

	// TODO need a synchronization?
	public void doPost(Request request, Response response) throws Exception {
		String id = request.getParameter(Configurations.REQUEST_ID);
		OperationResult result;

		if (id != null && id.trim().length() > 0) { // query result for a prior request
			result = queryResult(request, id);

		} else if (fileBrowser != null) { // can only open one fileBrowser in time
			// TODO 如果之前的某次请求打开了文件选择对话框，但随后请求主体却消失了（例如用户关闭了浏览器）
			// 如果发生这种情况，将会导致无法再接受新的请求。所以这里需要设置一个超时机制。从用户在文件选择框中选择了
			// 一个文件开始，请求主体必须在规定的时间内取走数据，否则请求将会失效
			// 另外，客户端应该尽量在离开之前（例如关闭了浏览器）清理或取消未完成的请求。当然，服务端需要提供相应接口。
			result = new OperationResult(request);
			result.setReturnCode(Configurations.UNSUPPORT_OPERATION);
			result.setException("File Browser has been opening");

		} else { // new browser
			result = newBrowser(request);
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

		long start = System.currentTimeMillis();
		fileBrowser.chooseFile();

		OperationResult result = new OperationResult(request);
		result.setResponseId(currentId);
		result.setReturnCode(Configurations.OPERATION_UNCOMPLETED);

		// it may take a long time to start a JFileChooser (especially at the very first time)
		// so we need count this period of time in
		long passedTime = System.currentTimeMillis() - start;
		tryToGetSelectedFiles(result, passedTime);

		return result;
	}

	private OperationResult queryResult(Request request, String id) {
		OperationResult result = new OperationResult(request);
		result.setResponseId(id);

		if (fileBrowser == null || !id.equals(currentId)) {
			result.setReturnCode(Configurations.UNKNOWN_ID);
			result.setException("[FileBrowser] no data");
		} else if (fileBrowser.hasSelected()) {
			result.setReturnCode(Configurations.OPERATION_SUCCEED);
			result.setExtraData("selectedFiles", fileBrowser.getSelectedFiles());
			fileBrowser = null;
			currentId = null;
		} else {
			result.setReturnCode(Configurations.OPERATION_UNCOMPLETED);
			tryToGetSelectedFiles(result, 0);
		}

		return result;
	}

	private void tryToGetSelectedFiles(OperationResult result, long passedTime) {
		long start = System.currentTimeMillis() - passedTime;
		do {
			if (fileBrowser.hasSelected()) {
				result.setReturnCode(Configurations.OPERATION_SUCCEED);
				result.setExtraData("selectedFiles", fileBrowser.getSelectedFiles());
				fileBrowser = null;
				break;
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		} while (System.currentTimeMillis() - start < Configurations.REQUEST_TIME_OUT);
	}

}
