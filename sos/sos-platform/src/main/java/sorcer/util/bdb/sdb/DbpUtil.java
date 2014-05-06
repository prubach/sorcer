package sorcer.util.bdb.sdb;

import net.jini.id.Uuid;
import sorcer.core.SorcerEnv;
import sorcer.core.context.PositionalContext;
import sorcer.core.context.ServiceContext;
import sorcer.core.provider.StorageManagement;
import sorcer.core.signature.NetSignature;
import sorcer.service.*;
import sorcer.util.bdb.objects.Store;

import java.net.URL;
import java.util.List;

import static sorcer.util.bdb.sdb.SdbUtil.getProviderName;
import static sorcer.util.bdb.sdb.SdbUtil.getServiceType;
import static sorcer.util.bdb.sdb.SdbUtil.getStoreType;

/**
 * SORCER class
 * User: prubach
 * Date: 19.09.13
 */
public class DbpUtil {

    public static URL store(Object object) throws ExertionException,
            SignatureException, ContextException {
        String storageName = SorcerEnv.getActualName(SorcerEnv
                .getDatabaseStorerName());
        Context ctx = new PositionalContext("store");
        ((ServiceContext) ctx).setReturnPath(new ReturnPath("result, stored/object/url"));
        ctx.putInValue(StorageManagement.object_stored, object);
        Task objectStoreTask = new Task("store",
                new NetSignature("contextStore", DatabaseStorer.class, null, storageName),
                ctx);
        return (URL) execDbTask(objectStoreTask);
    }


    static public URL update(Object value) throws ExertionException,
            SignatureException, ContextException {
        if (!(value instanceof Identifiable)
                || !(((Identifiable) value).getId() instanceof Uuid)) {
            throw new ContextException("Object is not Uuid Identifiable: "
                    + value);
        }
        return update((Uuid) ((Identifiable) value).getId(), value);
    }

    static public URL update(URL storedURL, Object value)
            throws ExertionException, SignatureException, ContextException {
        return update(SdbUtil.getUuid(storedURL), value);
    }

    static public URL update(Uuid storeUuid, Object value)
            throws ExertionException, SignatureException, ContextException {

        String storageName = SorcerEnv.getActualName(SorcerEnv
                .getDatabaseStorerName());
        Exertion objectUpdateTask = new Task("update",
                new NetSignature("contextUpdate", DatabaseStorer.class, null, storageName),
                getUpdateContext(value, storeUuid));
        try {
            objectUpdateTask.exert(null);
        } catch (Exception e) {
            throw new ExertionException(e);
        }
        return (URL) objectUpdateTask.getValue(StorageManagement.object_url);
    }

    public static int clear(Store type) throws ExertionException,
            SignatureException, ContextException {
        String storageName = SorcerEnv.getActualName(SorcerEnv
                .getDatabaseStorerName());
        Context ctx = new PositionalContext("clear");
        ((ServiceContext) ctx).setReturnPath(new ReturnPath(StorageManagement.store_size));
        ctx.putInValue(StorageManagement.store_type, type);
        Task objectStoreTask = new Task("clear",
                new NetSignature("contextClear", DatabaseStorer.class, null, storageName),
                ctx);
        return (Integer) execDbTask(objectStoreTask);
/*
        String storageName = Sorcer.getActualName(Sorcer
                .getDatabaseStorerName());
        Task objectStoreTask = task(
                "clear",
                sig("contextClear", DatabaseStorer.class, storageName),
                context("clear", in(StorageManagement.store_type, type),
                        result(StorageManagement.store_size)));
        return (Integer) value(objectStoreTask);
*/
    }

    public static int size(Store type) throws ExertionException,
            SignatureException, ContextException {
        String storageName = SorcerEnv.getActualName(SorcerEnv
                .getDatabaseStorerName());
        Context ctx = new PositionalContext("size");
        ((ServiceContext) ctx).setReturnPath(new ReturnPath(StorageManagement.store_size));
        ctx.putInValue(StorageManagement.store_type, type);
        Task objectStoreTask = new Task("size",
                new NetSignature("contextSize", DatabaseStorer.class, null, storageName),
                ctx);
        return (Integer) execDbTask(objectStoreTask);
    }

    public static URL delete(Object object) throws ExertionException,
            SignatureException, ContextException {
        if (object instanceof URL) {
            return deleteURL((URL) object);
        } else {
            return deleteObject(object);
        }
    }

    public static URL deleteObject(Object object) throws ExertionException,
            SignatureException, ContextException {
        String storageName = SorcerEnv.getActualName(SorcerEnv
                .getDatabaseStorerName());
        Context ctx = new PositionalContext("delete");
        ((ServiceContext) ctx).setReturnPath(new ReturnPath(StorageManagement.object_url));
        ctx.putInValue(StorageManagement.object_deleted, object);
        Task objectStoreTask = new Task("delete",
                new NetSignature("contextDelete", DatabaseStorer.class, null, storageName),
                ctx);
        return (URL) execDbTask(objectStoreTask);

        /*
        String storageName = Sorcer.getActualName(Sorcer
                .getDatabaseStorerName());
        Task objectStoreTask = task(
                "delete",
                sig("contextDelete", DatabaseStorer.class, storageName),
                context("delete", in(StorageManagement.object_deleted, object),
                        result(StorageManagement.object_url)));
        return (URL) value(objectStoreTask);*/
    }

    public static URL deleteURL(URL url) throws ExertionException,
            SignatureException, ContextException {
        String serviceTypeName = getServiceType(url);
        String storageName = getProviderName(url);
        Task objectStoreTask = null;

        try {
            Context ctx = new PositionalContext("delete");
            ((ServiceContext) ctx).setReturnPath(new ReturnPath(StorageManagement.object_url));
            ctx.putInValue(StorageManagement.object_deleted, url);
            objectStoreTask = new Task("delete",
                    new NetSignature("contextDelete", Class.forName(serviceTypeName), null, storageName),
                    ctx);
        } catch (ClassNotFoundException e) {
            throw new SignatureException("No such service type: "
                    + serviceTypeName, e);
        }
        return (URL) execDbTask(objectStoreTask);

        /*
        String serviceTypeName = getServiceType(url);
        String storageName = getProviderName(url);
        Task objectStoreTask = null;
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
        */
        //return null;
    }

    public static URL write(Object object) throws ExertionException,
            SignatureException, ContextException {
        String storageName = SorcerEnv.getActualName(SorcerEnv
                .getDatabaseStorerName());
        Context ctx = new PositionalContext("write");
        ((ServiceContext) ctx).setReturnPath(new ReturnPath("result, stored/object/url"));
        ctx.putInValue(StorageManagement.object_stored, object);
        Task objectStoreTask = new Task("write",
                new NetSignature("contextWrite", DatabaseStorer.class, null, storageName),
                ctx);
        // It was DataspaceStorer.class in Mike's version
        return (URL) execDbTask(objectStoreTask);
    }

    static public Object retrieve(URL url) throws ExertionException,
            SignatureException, ContextException {
        return retrieve(SdbUtil.getUuid(url), getStoreType(url));
    }

    static public Object retrieve(Uuid storeUuid, Store storeType)
            throws ExertionException, SignatureException, ContextException {
        String storageName = SorcerEnv.getActualName(SorcerEnv
                .getDatabaseStorerName());
        Context ctx = new PositionalContext("retrieve");
        ((ServiceContext) ctx).setReturnPath(new ReturnPath("result, stored/object/url"));
        //ctx.putInValue(StorageManagement.object_stored, object);
        Task objectStoreTask = new Task("retrieve",
                new NetSignature("contextRetrieve", DatabaseStorer.class, null, storageName),
                getRetrieveContext(storeUuid, storeType));
        // It was DataspaceStorer.class in Mike's version
        return execDbTask(objectStoreTask);

        /*
        Task objectRetrieveTask = task(
                "retrieve",
                sig("contextRetrieve", DatabaseStorer.class,
                        Sorcer.getActualDatabaseStorerName()),
                SdbUtil.getRetrieveContext(storeUuid, storeType));

        try {
            return get((Context) value(objectRetrieveTask));
        } catch (RemoteException e) {
            throw new ExertionException(e);
        } */
    }

    static public List<String> list(URL url) throws ExertionException,
            SignatureException, ContextException {
        return list(url, null);

    }

    @SuppressWarnings("unchecked")
    static public List<String> list(URL url, Store storeType)
            throws ExertionException, SignatureException, ContextException {
        Store type = storeType;
        String providerName = getProviderName(url);
        if (providerName == null)
            providerName = SorcerEnv.getActualDatabaseStorerName();

        if (type == null) {
            type = getStoreType(url);
            if (type == null) {
                type = Store.object;
            }
        }
        Context ctx = new PositionalContext("list");
        ((ServiceContext) ctx).setReturnPath(new ReturnPath("result, stored/object/url"));
        //ctx.putInValue(StorageManagement.object_stored, object);
        Task objectStoreTask = new Task("list",
                new NetSignature("contextList", DatabaseStorer.class, null, providerName),
                getListContext(type));
        // It was DataspaceStorer.class in Mike's version
        return (List<String>) execDbTask(objectStoreTask);

/*        Task listTask = task("list",
                sig("contextList", DatabaseStorer.class, providerName),
                SdbUtil.getListContext(type));

        return (List<String>) value(listTask);*/
        //return null;
    }

    @SuppressWarnings("unchecked")
    static public List<String> list(Store storeType) throws ExertionException,
            SignatureException, ContextException {
        String storageName = SorcerEnv.getActualName(SorcerEnv
                .getDatabaseStorerName());
        Context ctx = new PositionalContext("list");
        ((ServiceContext) ctx).setReturnPath(new ReturnPath("result, stored/object/url"));
        //ctx.putInValue(StorageManagement.object_stored, object);
        Task objectStoreTask = new Task("contextList",
                new NetSignature("contextList", DatabaseStorer.class, null, storageName),
                getListContext(storeType));
        // It was DataspaceStorer.class in Mike's version
        return (List<String>) execDbTask(objectStoreTask);

        /* String storageName = Sorcer.getActualName(Sorcer
                .getDatabaseStorerName());

        Task listTask = task("contextList",
                sig("contextList", DatabaseStorer.class, storageName),
                SdbUtil.getListContext(storeType));

        return (List<String>) value(listTask);*/
    }


    public static Exertion execTask(Task task) throws ExertionException, ContextException {
        Exertion xrt;
        try {
            xrt = task.exert(null);
            return xrt;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExertionException(e);
        }

    }

    public static Object execDbTask(Task task) throws ExertionException, ContextException {
        Exertion xrt;
        Object obj = null;
        try {
            xrt = task.exert(null);
            obj = xrt.getContext().getReturnValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExertionException(e);
        }
        return obj;
    }

    /**
     * Returns a context to be used with
     * {@link sorcer.core.provider.StorageManagement#contextStore(sorcer.service.Context)}
     *
     * @param uuid
     *            {@link net.jini.id.Uuid}
     * @param object
     *            to be stored
     * @return storage {@link sorcer.service.Context}
     * @throws sorcer.service.ContextException
     */
    static public Context getStoreContext(Object object)
            throws ContextException {
        ServiceContext cxt = new ServiceContext("store context");
        cxt.putInValue(StorageManagement.object_stored, object);
        cxt.putInValue(StorageManagement.object_uuid,
                ((Identifiable) object).getId());
        cxt.setReturnPath(StorageManagement.object_url);
        return cxt;
    }

    /**
     * Returns a context to be used with
     * {@link sorcer.core.provider.StorageManagement#contextStore(sorcer.service.Context)}
     *
     * @param uuid
     *            {@link net.jini.id.Uuid}
     * @param type
     *            one of: exertion, context, var, table, varModel, object
     * @return retrieval {@link sorcer.service.Context}
     * @throws sorcer.service.ContextException
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
        return getUpdateContext(object, SdbUtil.getUuid(url));
    }

    /**
	 * Returns a dataContext to be used with
     * {@link sorcer.core.provider.StorageManagement#contextUpdate(sorcer.service.Context)}
     *
     * @param object
     *            to be updated
     * @param uuid
     *            {@link net.jini.id.Uuid} og the updated object
     * @return update {@link sorcer.service.Context}
     * @throws sorcer.service.ContextException
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
