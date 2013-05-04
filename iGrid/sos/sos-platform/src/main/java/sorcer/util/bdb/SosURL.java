/*
 * Copyright 2012 the original author or authors.
 * Copyright 2012 SorcerSoft.org.
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

package sorcer.util.bdb;

import sorcer.service.EvaluationException;
import sorcer.util.bdb.sdb.SdbUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 * @author Mike Sobolewski
 */
public class SosURL {
	final protected static Logger logger = Logger.getLogger(SosURL.class
			.getName());

	private URL target;

	public SosURL(String url) throws MalformedURLException {
		target = new URL(url);
	}

	public SosURL(URL url) {
		target = url;
	}

	public Object getContent() throws IOException {
		return ((URL) target).getContent();
	}

	public URL getTarget() {
		return target;
	}

	public void setTarget(URL target) {
		this.target = target;
	}
	
	public String toString() {
		return target.toString();
	}

	public void setValue(Object value) throws EvaluationException,
			RemoteException {
		if (target != null && value != null) {
			try {
				if (target.getRef() == null) {
					target = SdbUtil.store(value);
				} else {
					SdbUtil.update((URL) target, value);
				}
			} catch (Exception e) {
				throw new EvaluationException(e);
			}
		}
	}
}
