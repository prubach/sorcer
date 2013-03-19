package sorcer.core.provider.exertmonitor;

import net.jini.id.Uuid;
import sorcer.service.Exertion;

/**
 * @author Rafał Krupiński
 */
public interface IMonitorSession {

    void setExpiration(long expiration);

    void setTimeout(long timeoutDuration);

    long getTimeout();

    // If the object is in space, the lease
    // never expires
    long getExpiration();

    void leaseCancelled();

    void timedOut();

    Uuid getCookie();

    String toString();

    Exertion getInitialExertion();
}
