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
package sorcer.jini.lookup;

import net.jini.core.entry.Entry;
import sorcer.jini.lookup.entry.SorcerServiceInfo;

public class AttributesUtil {

	static public String getGroups(Entry[] attributeSets) {
		if (attributeSets != null) {
			if (attributeSets.length > 0) {
				for (int i = 0; i < attributeSets.length - 1; i++) {
					if (attributeSets[i] instanceof SorcerServiceInfo) {
						return ((SorcerServiceInfo) attributeSets[i]).groups;
					}
				}
			}
		}
		return null;
	}

	static public String getProviderName(Entry[] attributeSets) {
		if (attributeSets != null) {
			if (attributeSets.length > 0) {
				for (int i = 0; i < attributeSets.length - 1; i++) {
					if (attributeSets[i] instanceof SorcerServiceInfo) {
						return ((SorcerServiceInfo) attributeSets[i]).providerName;
					}
				}
			}
		}
		return null;
	}

	static public String getHostName(Entry[] attributeSets) {
		if (attributeSets != null) {
			if (attributeSets.length > 0) {
				for (int i = 0; i < attributeSets.length - 1; i++) {
					if (attributeSets[i] instanceof SorcerServiceInfo) {
						return ((SorcerServiceInfo) attributeSets[i]).hostName;
					}
				}
			}
		}
		return null;
	}

	static public String getHostAddress(Entry[] attributeSets) {
		if (attributeSets != null) {
			if (attributeSets.length > 0) {
				for (int i = 0; i < attributeSets.length - 1; i++) {
					if (attributeSets[i] instanceof SorcerServiceInfo) {
						return ((SorcerServiceInfo) attributeSets[i]).hostAddress;
					}
				}
			}
		}
		return null;
	}

	static public String getUserDir(Entry[] attributeSets) {
		if (attributeSets != null) {
			if (attributeSets.length > 0) {
				for (int i = 0; i < attributeSets.length - 1; i++) {
					if (attributeSets[i] instanceof SorcerServiceInfo) {
						return ((SorcerServiceInfo) attributeSets[i]).userDir;
					}
				}
			}
		}
		return null;
	}

	static public String[] getPublishedServices(Entry[] attributeSets) {
		if (attributeSets != null) {
			if (attributeSets.length > 0) {
				for (int i = 0; i < attributeSets.length - 1; i++) {
					if (attributeSets[i] instanceof SorcerServiceInfo) {
						return ((SorcerServiceInfo) attributeSets[i]).publishedServices;
					}
				}
			}
		}
		return null;
	}
}