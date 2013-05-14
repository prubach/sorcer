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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

public class StringUtils {
	private static Calendar calendar = null;

	/**
	 * Returns a string representation of recursive arrays of any component
	 * type. in the form [e1,...,ek]
	 */
	public static String arrayToString(Object array) {
		if (array == null)
			return "null";
		else if (!array.getClass().isArray()) {
			return array.toString();
		}
		int length = Array.getLength(array);
		if (length == 0)
			return "[no elements]";

		StringBuffer buffer = new StringBuffer("[");
		int last = length - 1;
		Object obj;
		for (int i = 0; i < length; i++) {
			obj = Array.get(array, i);
			if (obj == null)
				buffer.append("null");
			else if (obj.getClass().isArray())
				buffer.append(arrayToString(obj));
			else
				buffer.append(obj);

			if (i == last)
				buffer.append("]");
			else
				buffer.append(",");
		}
		return buffer.toString();
	}

	/**
	 * Break string into an array of CVS tokens. The delimiter is passed to
	 * StringTokenizer for tokenizing the string.
	 *
	 * @param str
	 *            string to break up.
	 * @param delim
	 *            delimiter string.
	 * @return token array.
	 */
	public static String[] tokenize(String str, String delim) {
		Vector<String> tokens = new Vector<String>();
		String token = "";

		try {
			CSVStringTokenizer tokenizer = new CSVStringTokenizer(str, delim);
			while (tokenizer.hasMoreTokens()) {
				tokens.addElement((token = tokenizer.nextToken())
						.equals("null") ? "" : token);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] returnTokens = new String[tokens.size()];
		tokens.copyInto(returnTokens);
		return returnTokens;
	}

	public static String[] firstTwoTokens(String str, String delim) {
		String out[] = new String[2];

		try {
			CSVStringTokenizer token = new CSVStringTokenizer(str, delim);
			out[0] = token.nextToken();
			out[1] = token.nextToken();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	public static String firstToken(String str, String delim) {
		String out = new String();

		try {
			CSVStringTokenizer token = new CSVStringTokenizer(str, delim);
			out = token.nextToken();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	public static String secondToken(String str, String delim) {
		String out = new String();

		try {
			CSVStringTokenizer token = new CSVStringTokenizer(str, delim);
			token.nextToken();
			out = token.nextToken();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	public static String thirdToken(String str, String delim) {
		String out = new String();

		try {
			CSVStringTokenizer token = new CSVStringTokenizer(str, delim);
			token.nextToken();
			token.nextToken();
			out = token.nextToken();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	public static void bubbleSort(List coll) {
		int i = coll.size();
		while (--i >= 0) {
			for (int j = 0; j < i; j++) {
				if (((String) coll.get(j)).compareTo((String) coll
						.get(j + 1)) > 0) {
					/* swap objects */
					Object temp = coll.get(j);
					coll.set(j, coll.get(j + 1));
					coll.set(j + 1, temp);
				}
			}
		}
	}

	/**
	 * Replaces newline characters in the passed text with "\\n"
	 *
	 * @param origString
	 *            The text to replace return characters from
	 */
	public static String escapeReturns(String origString) {
		StringBuffer sb = new StringBuffer();
		int len = origString.length();
		for (int i = 0; i < len; i++) {
			char c = origString.charAt(i);
			if (c == '\n')
				sb.append("\\n");
			else
				sb.append(c);
		}
		return sb.toString();
	}

	public static String urlEncode(String origString) {
		StringBuffer sb = new StringBuffer();
		int len = origString.length();
		for (int i = 0; i < len; i++) {
			char c = origString.charAt(i);
			if (c == ' ')
				sb.append("%20");
			else
				sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Gets an exception's stack trace as a String
	 *
	 * @param e
	 *            the exception
	 * @return the stack trace of the exception
	 */
	public static String stackTraceToString(Throwable e) {
		if (e == null) {
			return "";
		}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(stream);
		e.printStackTrace(writer);
		writer.flush();
		return stream.toString();
	}

	public static String[] stackTraceToArray(Throwable e) {
		String str = stackTraceToString(e);
		return tokenize(str, "\t\n\r");
	}

	/**
	 * Returns a CSV string representation of recursive arrays of any component
	 * type.
	 *
	 * @param array
	 *            - an arry of object
	 * @return
	 */
	public static String arrayToCSV(Object array) {
		if (array == null)
			return "";
		else if (!array.getClass().isArray())
			return array.toString();

		int length = Array.getLength(array);
		if (length == 0)
			return "";

		StringBuffer buffer = new StringBuffer("" + Array.get(array, 0));
		int last = length - 1;
		Object obj;
		for (int i = 1; i < length; i++) {
			obj = Array.get(array, i);
			if (obj != null && obj.getClass().isArray())
				buffer.append(arrayToCSV(obj));
			else
				buffer.append(obj);

			if (i != last)
				buffer.append(",");
		}
		return buffer.toString();
	}

	/**
	 * Returns a date in the format "yyyyMMdd-HHmmss" format using this class calendar
	 */
	public static String getDateTime() {
		if (calendar == null) {
			calendar = Calendar.getInstance();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		long time = calendar.getTime().getTime();
		return sdf.format(time);
	}
}
