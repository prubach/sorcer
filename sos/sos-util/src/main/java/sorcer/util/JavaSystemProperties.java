package sorcer.util;

import java.util.Map;
import java.util.Properties;

/**
 * @author Rafał Krupiński
 */
public class JavaSystemProperties {
	public static final String JAVA_RMI_SERVER_CODEBASE = "java.rmi.server.codebase";
	public static final String JAVA_RMI_SERVER_USE_CODEBASE_ONLY = "java.rmi.server.useCodebaseOnly";
	public static final String JAVA_SECURITY_POLICY = "java.security.policy";
	public static final String JAVA_UTIL_LOGGING_CONFIG_FILE = "java.util.logging.config.file";
	public static final String JAVA_NET_PREFER_IPV4_STACK = "java.net.preferIPv4Stack";
	public static final String JAVA_PROTOCOL_HANDLER_PKGS = "java.protocol.handler.pkgs";

	public static String getProperty(String key, Map<String, String> properties) {
		return getProperty(key, null, properties);
	}

	public static String getProperty(String key, String defaultValue, Properties properties) {
		return getProperty(key, defaultValue, (Map)properties);
	}

	public static String getProperty(String key, String defaultValue, Map<String, String> properties) {
		if (properties == null) {
			return System.getProperty(key, defaultValue);
		} else {
			String value = properties.get(key);
			return value != null ? value : defaultValue;
		}
	}


}
