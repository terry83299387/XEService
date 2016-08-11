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

	// program info
	public static final String	EXE_FILE_NAME 			= "XEService.exe";
	public static final String	NAME					= "XeXtension";
	public static final String	COPYRIGHT				= "XeXtension is powered by Shanghai Supercomputer Center (SSC).\nAll right reserved.";
	// read version from config, version increases at each time update
	public static final String	VERSION;

	// general constants
	public static final String	DEFAULT_ENCODING		= "UTF-8";
	// 2016.8.10 QiaoMingkui: use 1 port only
	public static final int[]	CANDIDATE_PORTS			= {20052/*, 26126, 22862*/};
	public static final long	REQUEST_TIME_OUT		= 30 * 1000; // 30 secs

	// request & response filed names
	public static final String	JSON_CALLBACK			= "jsoncallback";
	public static final String	REQUEST_OPERATOR		= "op";
	public static final String	REQUEST_ID				= "reqId";
	public static final String	RESPONSE_RETURN_CODE	= "returnCode";
	public static final String	RESPONSE_ID				= "respId";
	public static final String	RESPONSE_EXCEPTION		= "exception";
	public static final String	RESPONSE_EXTRA_DATA		= "extraData";
	public static final String	ACCESS_CONTROL_ALLOW_ORIGIN
														= "Access-Control-Allow-Origin";
//	public static final String	CONTENT_TYPE			= "Content-Type";
	public static final String	DEFAULT_CONTENT_TYPE	= "application/json;charset=UTF-8";

	// operators
	public static final int		ECHO_BACK				= 1;
	public static final int		FILE_BROWSER			= 2;
	public static final int		FILE_TRANSFER			= 3;
	public static final int		RUN_APP					= 4;
	public static final int		VERSION_INFO			= 5;
	public static final int		REMOTE_DESKTOP			= 6;
	public static final int		FILE_OPERATOR			= 7;

	// return codes
	public static final int		OPERATION_SUCCEED		= 0;
	public static final int		OPERATION_UNCOMPLETED	= 1;
	public static final int		UNKNOWN_ERROR			= -1;
	public static final int		UNKNOWN_OPERATOR		= 101;
	public static final int		UNSUPPORTED_METHOD		= 102;
	public static final int		UNKNOWN_ID				= 103;
	public static final int		UNSUPPORT_OPERATION		= 104;

	// config file related
	public static final String UPDATE_LOG_PATH					= "/xexupdate/update.log";
	public static final String AUTOUPDATE_DEFAULT_SERVERS		= "autoupdate.default_servers";
	public static final String VERSION_NAME_SEPARATOR			= "\\.";
	public static final String UPDATE_PATCHES_DIR				= "/xexupdate/patches/";
	public static final String UPDATE_LOG_FILE_SEPARATOR		= "/";
	public static final String UPDATE_LOG_VERSION_SUFFIX		= "]";
	public static final String UPDATE_LOG_VERSION_PREFIX		= "[";
	public static final String CUSTOMER_AUTOUPDATE				= "customer.autoupdate";
	public static final String CUSTOMER_VERSION					= "customer.version";
	public static final String CUSTOMER_AUTOUPDATE_SERVER		= "customer.autoupdate.server";
	public static final String CUSTOMER_PREFIX					= "customer";

	static {
		try {
			VERSION = ConfigHelper.getProperty(CUSTOMER_VERSION);
		} catch(IOException e) {
			logger.error("can not get the value of current version, failed to read config: ", e);
			throw new IllegalStateException("failed to read config");
		}
	}
}
