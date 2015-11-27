/**
 * 
 */
package xextension.operation;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONStringer;

import xextension.global.Configurations;
import xextension.http.Request;

/**
 * @author QiaoMingkui
 *
 */
public class OperationResult {
	private String				operator;
	private String				reqId;
	private int						returnCode;
	private String				respId;
	private String				exception;
	private Map<String, Object>	extraData	= new HashMap<String, Object>();

	public OperationResult() {

	}

	public OperationResult(Request request) {
		if (request != null) {
			operator = request.getParameter(Configurations.REQUEST_OPERATOR);
			reqId = request.getParameter(Configurations.REQUEST_ID);
		}
	}

	public String toJsonString() {
		JSONStringer json = new JSONStringer();
		json.object()
			.key(Configurations.REQUEST_OPERATOR).value(operator)
			.key(Configurations.REQUEST_ID).value(reqId)
			.key(Configurations.RESPONSE_RETURN_CODE).value(returnCode)
			.key(Configurations.RESPONSE_ID).value(respId)
			.key(Configurations.RESPONSE_EXCEPTION).value(exception);

		if (extraData.size() > 0) {
			json.key(Configurations.RESPONSE_EXTRA_DATA).value(extraData);
		}

		json.endObject();

		return json.toString();
	}

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * @param operator
	 *        the operator to set
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * @return the id
	 */
	public String getReqestId() {
		return reqId;
	}

	/**
	 * @param id
	 *        the id to set
	 */
	public void setRequestId(String id) {
		this.reqId = id;
	}

	/**
	 * @return the returnCode
	 */
	public int getReturnCode() {
		return returnCode;
	}

	/**
	 * @param returnCode
	 *        the returnCode to set
	 */
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	/**
	 * @return the respId
	 */
	public String getResponseId() {
		return respId;
	}

	/**
	 * @param respId
	 *        the respId to set
	 */
	public void setResponseId(String respId) {
		this.respId = respId;
	}

	/**
	 * @return the exception
	 */
	public String getException() {
		return exception;
	}

	/**
	 * @param exception
	 *        the exception to set
	 */
	public void setException(String exception) {
		this.exception = exception;
	}

	/**
	 * @param name
	 *        the data name
	 * @return the value of represented name
	 */
	public Object getExtraData(String name) {
		return extraData.get(name);
	}

	/**
	 * @return the extraData
	 */
	public Map<String, Object> getExtraDataAll() {
		return extraData;
	}

	/**
	 * @param name
	 *        the data name to set
	 * @param value
	 *        the data value to set
	 */
	public void setExtraData(String name, Object value) {
		if (name == null || name.length() == 0) {
			return;
		}

		this.extraData.put(name, value);
	}

	/**
	 * @param name
	 *        the data name to remove
	 */
	public void removeExtraData(String name) {
		if (name == null || name.length() == 0) {
			return;
		}

		this.extraData.remove(name);
	}
}
