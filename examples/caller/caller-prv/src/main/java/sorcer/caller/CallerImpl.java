package sorcer.caller;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.*;

import net.jini.id.Uuid;
import sorcer.service.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallerImpl implements Caller {

	private static Logger logger = LoggerFactory.getLogger(CallerImpl.class);

    private Map<Uuid, Context> jcontexts = new HashMap<Uuid, Context>();
    private Map<Uuid, JavaSystemCaller> jscs = new HashMap<Uuid,JavaSystemCaller>();
    private Map<Uuid, Boolean> jscs_status = new HashMap<Uuid, Boolean>();

    public Context execute(Context context) throws RemoteException {
        JavaSystemCaller jsc = new JavaSystemCaller();
        Uuid id = (context.getExertion()!=null ? context.getExertion().getId() : context.getId());
        jcontexts.put(id, context);
        jscs.put(id, jsc);
        jscs_status.put(id, new Boolean(true));
        Context result = jsc.execute(context);
        jscs_status.remove(context);
        jscs_status.put(id, new Boolean(false));
        return result;
    }

    private Uuid getCtxId(Context context) {
        return (context.getExertion()!=null ? context.getExertion().getId() : context.getId());
    }

    public String getCurrentOutput(Context context) throws RemoteException {
        if (jscs.get(getCtxId(context))!=null)
            return jscs.get(getCtxId(context)).outputGobbler.getOutput();
        else
            return null;
    }

    public String getCurrentError(Context context) throws RemoteException {
        if (jscs.get(getCtxId(context))!=null)
            return jscs.get(getCtxId(context)).errorGobbler.getOutput();
        else
            return null;
    }

    public Boolean getCurrentStatus(Context context) throws RemoteException {
        logger.debug("Statuses:" + jscs_status.values());
        logger.debug("Current status: " + jscs_status.get(getCtxId(context)));
        if (jscs_status.get(context)!=null) return jscs_status.get(getCtxId(context));
        else return new Boolean(false);
    }

    public List<Context> getCurrentContexts() throws RemoteException {
        return new ArrayList<Context>(jcontexts.values());
    }
}
