/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sorcer.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * SORCER generic utility class.
 */
public class GenericUtil {

	/**
	 * The method returns an Object of type fullClassName using a constructor
	 * from that type with arguments matching the types supplied in the
	 * constructorArgs.
	 */
	public static Object getInstance(String fullClassName,
			Object[] constructorArgs) throws ClassNotFoundException,
			NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Class<?> c = Class.forName(fullClassName);
		Class<?>[] constrArgClasses = new Class[constructorArgs.length];
		for (int i = 0; i < constructorArgs.length; i++) {
			constrArgClasses[i] = constructorArgs[i].getClass();
		}
		Constructor<?> constr = c.getConstructor(constrArgClasses);
		return constr.newInstance(constructorArgs);
	}

	public static String getPropertiesString(Properties myProps) {

		int maxKeySize = 0;
		int maxValueSize = 0;
		Enumeration<Object> keys = myProps.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = myProps.getProperty(key);
			if (key.length() > maxKeySize)
				maxKeySize = key.length();
			if (value.length() > maxValueSize)
				maxValueSize = value.length();
		}

		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		// String format = "%" + maxKeySize + "s = %" + maxValueSize + "s\n";
		String format = "%-" + maxKeySize + "s = %s\n";
		Enumeration<Object> keys2 = myProps.keys();
		if (!keys2.hasMoreElements())
			sb.append("*** no properties to print ***\n");
		while (keys2.hasMoreElements()) {
			String key = (String) keys2.nextElement();
			String value = myProps.getProperty(key);
			formatter.format(format, key, value);
		}
		return sb.toString();
	}

	/**
	 * The method returns a unique string based on time
	 * 
	 * @return String containing time represented as a long hexadecimal number
	 */
	public static String getUniqueString() {
		// String uniqueID = (new UID()).toString();
		return Long.toHexString(new Date().getTime());
	}

	/**
	 * Main method for the GenericUtil class
	 * 
	 * @param args
	 *            Argument array
	 */
	public static void main(String[] args) {
		if (hasArg("-h", args) || args.length == 0)
			printHelp();
		if (hasArg("-sp", args))
			printSystemProperties();
		if (hasArg("-ev", args))
			printEnvVars();
	}

    public static void printEnvVars() {
		Map<String, String> envMap = System.getenv();
		for (String key : envMap.keySet()) {
			System.out.println(key + " = " + envMap.get(key));
		}
	}

	public static void printProperties(Properties myProps) {
		System.out.println(getPropertiesString(myProps));
	}

	public static void printSystemProperties() {
		printProperties(System.getProperties());
	}

	private static boolean hasArg(String test, String[] args) {
		for (String arg : args) {
			if (arg.equals(test))
				return true;
		}
		return false;
	}

	private static void printHelp() {
		StringBuilder sb = new StringBuilder();

		sb.append("\nUsage: java sorcer.util.GenericUtil [-options]");
		sb.append("\n\nand options include:\n\n");
		sb.append("-h\tprint this message\n");
		sb.append("-ev\tprint environment variables\n");
		sb.append("-sp\tprint system properties\n");
		System.out.println(sb.toString());

	}
}
