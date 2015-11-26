/**
 * 
 */
package xextension.global;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author QiaoMingkui
 *
 */
public final class Configurations {
	private static final Logger logger = LogManager.getLogger(Configurations.class);

	// version info
	public static final String	NAME				= "XeXtension";
	public static final String	COPYRIGHT		= "XeXtension© is powered by Shanghai Supercomputer Center (SSC).\nAll right reserved.";
	// read version from config, version increases at each time update
	public static final String	VERSION;
	static {
		try {
			VERSION = ConfigHelper.getProperty("version");
		} catch(IOException e) {
			logger.error("can not get the value of current version, failed to read config: ", e);
			throw new IllegalStateException("failed to read config");
		}
	}

	// general constants
	public static final String	DEFAULT_ENCODING		= "UTF-8";
	public static final int[]	CANDIDATE_PORTS			= {20052, 26126, 22862};
	public static final long	REQUEST_TIME_OUT		= 30 * 1000; // 30 secs

	// request & response filed names
	public static final String	JSON_CALLBACK = "jsoncallback";
	public static final String	REQUEST_OPERATOR		= "op";
	public static final String	REQUEST_ID				= "reqId";
	public static final String	RESPONSE_RETURN_CODE	= "returnCode";
	public static final String	RESPONSE_ID				= "respId";
	public static final String	RESPONSE_EXCEPTION		= "exception";
	public static final String	RESPONSE_EXTRA_DATA		= "extraData";

	// operators
	public static final int		ECHO_BACK				= 1;
	public static final int		FILE_BROWSER			= 2;
	public static final int		FILE_TRANSFER			= 3;
	public static final int		RUN_APP					= 4;
	public static final int		VERSION_INFO			= 5;

	// return codes
	public static final int		OPERATION_SUCCEED		= 0;
	public static final int		OPERATION_UNCOMPLETED	= 1;
	public static final int		UNKNOWN_ERROR			= -1;
	public static final int		UNKNOWN_OPERATOR		= 101;
	public static final int		UNSUPPORTED_METHOD		= 102;
	public static final int		UNKNOWN_ID				= 103;
	public static final int		UNSUPPORT_OPERATION		= 104;

}
