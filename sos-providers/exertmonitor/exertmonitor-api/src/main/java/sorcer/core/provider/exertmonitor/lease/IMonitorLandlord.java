package sorcer.core.provider.exertmonitor.lease;

import com.sun.jini.landlord.Landlord;
import com.sun.jini.landlord.LeasedResource;
import net.jini.core.lease.Lease;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Rafał Krupiński
 */
public interface IMonitorLandlord extends Landlord {
    Lease newLease(MonitorLeasedResource resource) throws RemoteException;

    // Apply the policy to a requested duration
    // to get an actual expiration time.
    long getExpiration(long request) throws RemoteException;

    void remove(LeasedResource lr) throws RemoteException;
}
