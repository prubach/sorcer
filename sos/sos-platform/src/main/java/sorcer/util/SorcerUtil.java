/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

import sorcer.core.SorcerConstants;
import sorcer.service.Identifiable;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class SorcerUtil implements SorcerConstants {
	/**
	 * Makes an arry from the parameter enumeration <code>e</code>.
	 * 
	 * @param e
	 *            an enumeration
	 * @return an arry of objects in the underlying enumeration <code>e</code>
	 */
	static public Object[] makeArray(final Enumeration e) {
		List<Object> objs = new LinkedList<Object>();
		while (e.hasMoreElements()) {
			objs.add(e.nextElement());
		}
		return objs.toArray();
	}
}
