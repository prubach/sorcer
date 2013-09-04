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

package sorcer.util.bdb.sdb;


import java.net.URL;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import sorcer.core.StorageManagement;
import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
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
        else
            return url.getPath().substring(1);
    }

    public static String getServiceType(URL url) {
        return url.getHost();
    }

    /**
     * Returns a context to be used with
     * {@link StorageManagement#contextStore(Context)}
     *
     * @param uuid
     *            {@link Uuid}
     * @param type
     *            one of: exertion, context, var, table, varModel, object
     * @return retrieval {@link Context}
     * @throws ContextException
     */
    static public Context getRetrieveContext(Uuid uuid, Store type)
            throws ContextException {
		ServiceContext cxt = new ServiceContext("retrieve dataContext");
        cxt.putInValue(StorageManagement.object_type, type);
        cxt.putInValue(StorageManagement.object_uuid, uuid);
        cxt.setReturnPath(StorageManagement.object_retrieved);
        return cxt;
    }

    static public Context getUpdateContext(Object object, URL url)
            throws ContextException {
        return getUpdateContext(object, getUuid(url));
    }

    /**
	 * Returns a dataContext to be used with
     * {@link StorageManagement#contextUpdate(Context)}
     *
     * @param object
     *            to be updated
     * @param uuid
     *            {@link Uuid} og the updated object
     * @return update {@link Context}
     * @throws ContextException
     */
    static public Context getUpdateContext(Object object, Uuid uuid)
            throws ContextException {
        ServiceContext cxt = new ServiceContext("update context");
        cxt.putInValue(StorageManagement.object_uuid, uuid);
        cxt.putInValue(StorageManagement.object_updated, object);
        cxt.setReturnPath(StorageManagement.object_url);
        return cxt;
    }

    static public Context getListContext(Store storeType)
            throws ContextException {
        ServiceContext cxt = new ServiceContext("storage list context");
        cxt.putInValue(StorageManagement.store_type, storeType);
        cxt.setReturnPath(StorageManagement.store_content_list);
        return cxt;
    }

}
