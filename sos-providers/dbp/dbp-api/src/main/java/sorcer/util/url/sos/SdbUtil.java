/*
 * Copyright 2012 the original author or authors.
 * Copyright 2012 SorcerSoft.org.
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

package sorcer.util.url.sos;


import java.net.URL;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import sorcer.core.SorcerEnv;
import sorcer.util.bdb.objects.Store;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings("rawtypes")
public class SdbUtil {

    public static boolean isSosURL(Object url) {
        if (url instanceof URL && ((URL)url).getProtocol().equals("sos"))
            return true;
        else
            return false;
    }

    public static boolean isSosIdURL(Object url) {
        if (isSosURL(url) && ((URL) url).getRef() != null)
            return true;
        else
            return false;
    }

    public static Uuid getUuid(URL url) {
        String urlRef = url.getRef();
        int index = urlRef.indexOf('=');
        // storeType = SorcerDatabaseViews.getStoreType(reference.substring(0,
        // index));
        return UuidFactory.create(urlRef.substring(index + 1));
    }

	public static Store getStoreType(URL url) {
		String urlRef = url.getRef();
		int index = urlRef.indexOf('=');
		return Store.getStoreType(urlRef.substring(0, index));
	}

    public static String getProviderName(URL url) {
        if (url == null)
            return null;
        else {
            String shortName = url.getPath().substring(1);
            if (shortName.endsWith(SorcerEnv.getNameSuffix())) return shortName;
            else return SorcerEnv.getActualName(shortName);
        }

    }

    public static String getServiceType(URL url) {
        return url.getHost();
    }

}
