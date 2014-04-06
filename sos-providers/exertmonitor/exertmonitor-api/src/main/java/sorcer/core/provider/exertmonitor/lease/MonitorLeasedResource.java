package sorcer.core.provider.exertmonitor.lease;

import com.sun.jini.landlord.LeasedResource;

public interface MonitorLeasedResource extends LeasedResource {

    public void leaseCancelled();

    public void setTimeout(long timeoutDuration);

    public long getTimeout();

    public void timedOut();

}
