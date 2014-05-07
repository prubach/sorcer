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

    public static <T extends Entry> T getFirstByType(Entry[] attributeSets, Class<T> type) {
        if (attributeSets != null && attributeSets.length > 0) {
            for (Entry attributeSet : attributeSets) {
                if (type.isInstance(attributeSet)) return (T) attributeSet;
            }
        }
        return null;
    }

    static public SorcerServiceInfo getSorcerServiceInfo(Entry[] attributeSets) {
        if (attributeSets != null && attributeSets.length > 0) {
            for (Entry entry : attributeSets) {
                if (entry instanceof SorcerServiceInfo) {
                    return ((SorcerServiceInfo) entry);
                }
            }
        }
        return null;
    }

	static public String getGroups(Entry[] attributeSets) {
        SorcerServiceInfo ssi = getSorcerServiceInfo(attributeSets);
        if (ssi!=null) return ssi.groups;
        else return null;
	}

	static public String getProviderName(Entry[] attributeSets) {
        SorcerServiceInfo ssi = getSorcerServiceInfo(attributeSets);
        if (ssi!=null) return ssi.providerName;
        else return null;
	}

	static public String getHostName(Entry[] attributeSets) {
        SorcerServiceInfo ssi = getSorcerServiceInfo(attributeSets);
        if (ssi!=null) return ssi.hostName;
        else return null;
	}

	static public String getHostAddress(Entry[] attributeSets) {
        SorcerServiceInfo ssi = getSorcerServiceInfo(attributeSets);
        if (ssi!=null) return ssi.hostAddress;
        else return null;
	}

	static public String getUserDir(Entry[] attributeSets) {
        SorcerServiceInfo ssi = getSorcerServiceInfo(attributeSets);
        if (ssi!=null) return ssi.serviceHome;
        else return null;
	}

	static public String[] getPublishedServices(Entry[] attributeSets) {
        SorcerServiceInfo ssi = getSorcerServiceInfo(attributeSets);
        if (ssi!=null) return ssi.publishedServices;
        else return null;
	}
}
