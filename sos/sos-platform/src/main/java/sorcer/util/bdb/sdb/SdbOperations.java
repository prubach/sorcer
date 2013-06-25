package sorcer.util.bdb.sdb;
/**
 *
 * Copyright 2013 Rafał Krupiński.
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


import net.jini.id.Uuid;
import sorcer.core.StorageManagement;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.DatabaseStorer;
import sorcer.service.DataspaceStorer;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.service.Task;
import sorcer.util.Sorcer;
import sorcer.util.bdb.objects.SorcerDatabaseViews;

import java.net.URL;
import java.util.List;

import static sorcer.eo.operator.*;
import static sorcer.service.ContextFactory.context;

/**
 * SdbOperations are methods extracted from SdbUtil and use sorcer.oe.operator methods. Some other methods from SdbUtil
 * were moved directly to operator due to circular dependency.
 *
 * @author Rafał Krupiński
 */
public class SdbOperations {
    static public URL update(Uuid storeUuid, Object value)
            throws ExertionException, SignatureException, ContextException {
        Task objectUpdateTask = task(
                "update",
                sig("contextUpdate", DatabaseStorer.class,
                        Sorcer.getActualDatabaseStorerName()),
                SdbUtil.getUpdateContext(value, storeUuid));

        objectUpdateTask = exert(objectUpdateTask);
        return (URL) get(context(objectUpdateTask),
                StorageManagement.object_url);
    }

    public static URL deleteObject(Object object) throws ExertionException,
            SignatureException, ContextException {
        String storageName = Sorcer.getActualName(Sorcer
                .getDatabaseStorerName());
        Task objectStoreTask = task(
                "delete",
                sig("contextDelete", DatabaseStorer.class, storageName),
                context("delete", in(StorageManagement.object_deleted, object),
                        result(StorageManagement.object_url)));
        return (URL) value(objectStoreTask);
    }

    public static URL deleteURL(URL url) throws ExertionException,
			SignatureException, ContextException {
		String serviceTypeName = SdbUtil.getServiceType(url);
		String storageName = SdbUtil.getProviderName(url);
		Task objectStoreTask;
		try {
			objectStoreTask = task(
					"delete",
					sig("contextDelete", Class.forName(serviceTypeName),
							storageName),
					context("delete",
							in(StorageManagement.object_deleted, url),
							result(StorageManagement.object_url)));
		} catch (ClassNotFoundException e) {
			throw new SignatureException("No such service type: "
					+ serviceTypeName, e);
		}
		return (URL) value(objectStoreTask);
	}

    public static URL store(Object object) throws ExertionException,
			SignatureException, ContextException {
		String storageName = Sorcer.getActualName(Sorcer
				.getDatabaseStorerName());
		Task objectStoreTask = task(
				"store",
				sig("contextStore", DatabaseStorer.class, storageName),
				context("store", in(StorageManagement.object_stored, object),
						result("result, stored/object/url")));
		return (URL) value(objectStoreTask);
	}

    public static URL write(Object object) throws ExertionException,
            SignatureException, ContextException {
        String storageName = Sorcer.getActualName(Sorcer.getSpacerName());
        Task objectStoreTask = task(
                "write",
                sig("contextWrite", DataspaceStorer.class, storageName),
                context("stored", in(StorageManagement.object_stored, object),
                        result("result, stored/object/url")));
        return (URL) value(objectStoreTask);
    }

    static public Object retrieve(Uuid storeUuid, SorcerDatabaseViews.Store storeType)
            throws ExertionException, SignatureException, ContextException {
        Task objectRetrieveTask = task(
                "retrieve",
                sig("contextRetrieve", DatabaseStorer.class,
                        Sorcer.getActualDatabaseStorerName()),
                SdbUtil.getRetrieveContext(storeUuid, storeType));

        return get((Context) value(objectRetrieveTask));
    }

    @SuppressWarnings("unchecked")
    static public List<String> list(URL url, SorcerDatabaseViews.Store storeType)
            throws ExertionException, SignatureException, ContextException {
        SorcerDatabaseViews.Store type = storeType;
        String providerName = SdbUtil.getProviderName(url);
        if (providerName == null)
            providerName = Sorcer.getActualDatabaseStorerName();

        if (type == null) {
            type = SdbUtil.getStoreType(url);
            if (type == null) {
                type = SorcerDatabaseViews.Store.object;
            }
        }
        Task listTask = task("list",
                sig("contextList", DatabaseStorer.class, providerName),
                SdbUtil.getListContext(type));

        return (List<String>) value(listTask);
    }

}
