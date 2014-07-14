package sorcer.core.context.model.par;

import sorcer.core.context.ServiceContext;
import sorcer.service.modeling.Variability;
import sorcer.service.*;
import sorcer.util.bdb.sdb.DbpUtil;
import sorcer.util.bdb.sdb.SdbUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * SORCER class
 * User: prubach
 * Date: 01.03.14
 */
public class ParImpl<T> extends Par {

    private static Logger logger = Logger.getLogger(ParImpl.class.getName());

    public ParImpl(String parname) {
        super(parname);
    }

    public ParImpl(Identifiable identifiable) {
        super(identifiable);
    }

    public ParImpl(String parname, T argument) {
        super(parname, argument);
    }

    public ParImpl(String parname, Object argument, Context scope) throws RemoteException {
        super(parname, argument, scope);
    }

    public ParImpl(String name, String path, Mappable map) {
        super(name, path, map);
    }

    @Override
    public void setClosure(Context scope) {
        if (((ServiceContext)scope).containsKey(Condition._closure_))
            scope.remove(Condition._closure_);
    }

    @Override
    public void setValue(Object value) throws EvaluationException {
        if (isPersistent()) {
            try {
                if (SdbUtil.isSosURL(value)) {
                    if (((URL)value).getRef() == null) {
                        value = DbpUtil.store(value);
                    } else if (isPersistent()){
                        DbpUtil.update((URL)value, value);
                    }
                    return;
                }
            } catch (Exception e) {
                throw new EvaluationException(e);
            }
        }
        if (mappable != null) {
            try {
                Object val = mappable.asis((String)this.value);
                if (val instanceof Par) {
                    ((Par)val).setValue(value);
                } else if (isPersistent()) {
                    if (SdbUtil.isSosURL(val)) {
                        DbpUtil.update((URL)val, value);
                    } else {
                        URL url = DbpUtil.store(value);
                        Par p = new ParImpl((String)this.value, url);
                        p.setPersistent(true);
                        if (mappable instanceof ServiceContext)
                            ((ServiceContext)mappable).put(this.value, p);
                        else
                            mappable.putValue((String)this.value, p);
                    }
                } else {
                    mappable.putValue((String)this.value, value);
                }
            } catch (Exception e) {
                throw new EvaluationException(e);
            }
        }
        else
            this.value = value;
    }

    /* (non-Javadoc)
     * @see sorcer.service.Evaluation#getValue(sorcer.co.tuple.Parameter[])
     */
    @Override
    public T getValue(Arg... entries) throws EvaluationException,
            RemoteException {
        substitute(entries);
        T val = null;
        try {
            if (mappable != null) {
                val = (T) mappable.getValue((String) value);
            } else if (value == null && scope != null) {
                val = ((ServiceContext<T>) scope).get(name);
            } else {
                val = (T)value;
            }
            if (val instanceof Evaluation) {
                if (val instanceof Par && ((Par)val).asis() == null && value == null) {
                    logger.warning("undefined par: " + val);
                    return null;
                }

                if (val instanceof Scopable && ((Scopable)val).getScope() != null) {
                    ((Context)((Scopable)val).getScope()).append(scope);
                }

                if (val instanceof Exertion) {
                    // TODO context binding for all exertions, works for tasks only
                    Context cxt = ((Exertion)val).getDataContext();
                    List<String> paths = cxt.getPaths();
                    for (String an : ((Map<String, Object>)scope).keySet()) {
                        for (String p : paths) {
                            if (p.endsWith(an)) {
                                cxt.putValue(p, scope.getValue(an));
                                break;
                            }
                        }
                    }
                }
                val = ((Evaluation<T>) val).getValue(entries);
            }

            if (isPersistent()) {
                if (SdbUtil.isSosURL(val))
                    val = (T) ((URL) val).getContent();
                else {
                    if (mappable != null) {
                        URL url = DbpUtil.store(val);
                        Par p = new ParImpl((String)this.value, url);
                        p.setPersistent(true);
                        if (mappable instanceof ServiceContext)
                            ((ServiceContext)mappable).put(this.value, p);
                        else
                            mappable.putValue((String)this.value, p);
                    }
                    else {
                        value = DbpUtil.store(val);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e);
        }
        return val;
    }

    @Override
    public void setScope(Context scope) {
        if (((ServiceContext)scope).containsKey(Condition._closure_))
            scope.remove(Condition._closure_);
        this.scope = scope;
    }

    @Override
    public Variability getVariability(String name) throws ArgException {
        Object obj = scope.get(name);
        if (obj instanceof Par)
            return (Par)obj;
        else
            try {
                return new ParImpl(name, obj, scope);
            } catch (RemoteException e) {
                throw new ArgException(e);
            }
    }

    @Override
    public URL getDbURL() throws MalformedURLException {
        URL url = null;
        if (dbURL != null)
            url = dbURL;
        else if (((ServiceContext)scope).getDbUrl() != null)
            url = new URL(((ServiceContext)scope).getDbUrl());

        return url;
    }
}
