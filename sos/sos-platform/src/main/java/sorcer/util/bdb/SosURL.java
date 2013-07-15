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
package sorcer.util.bdb;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Mike Sobolewski
 */
public class SosURL {
	final protected static Logger logger = Logger.getLogger(SosURL.class
			.getName());

	private URL target;

    public SosURL(URL url) {
		target = url;
	}

	public Object getContent() throws IOException {
		return target.getContent();
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

}
