/**
 * 
 */
package xextension.operation;

import xextension.global.Configurations;
import xextension.http.Request;
import xextension.http.Response;

/**
 * @author QiaoMingkui
 *
 */
public class VersionInfo extends Processor {

	public void doGet(Request request, Response response) throws UnsupportedMethodException {
		this.doPost(request, response);
	}

	public void doPost(Request request, Response response) throws UnsupportedMethodException {
		OperationResult result = new OperationResult(request);
		result.setReturnCode(Configurations.OPERATION_SUCCEED);
		result.setExtraData("name", Configurations.NAME);
		result.setExtraData("version", Configurations.VERSION);
		result.setExtraData("copyright", Configurations.COPYRIGHT);

		response.print(result.toJsonString());
		response.flush();
	}

}
