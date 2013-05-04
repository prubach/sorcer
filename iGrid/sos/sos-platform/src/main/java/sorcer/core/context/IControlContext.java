package sorcer.core.context;

import sorcer.core.context.ControlContext.ThrowableTrace;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.Strategy;

import java.util.List;

/**
 * @author Rafał Krupiński
 */
public interface IControlContext<T> extends Context<T>, AssociativeContext {
    void addException(ThrowableTrace et);

    void addException(Throwable t);

    void addException(String message, Throwable t);

    Object getMutexId();

    boolean isNodeReferencePreserved();

    String getExecTime();

    void setAccessType(Strategy.Access access);

    Strategy.Access getAccessType();

    String getNotifyList();

    String getNotifyList(Exertion ex);

    List<String> getTrace();

    void setMutexId(Object mutexId);

    boolean isWait();

    boolean isMonitored();
    
    List<ThrowableTrace> getExceptions();
}
