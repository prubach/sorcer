package sorcer.core.dispatch;

import net.jini.core.lease.Lease;
import net.jini.id.Uuid;
import net.jini.lease.LeaseRenewalManager;
import sorcer.core.monitor.MonitoringSession;
import sorcer.core.provider.exertmonitor.MonitorHelper;
import sorcer.service.ServiceExertion;

/**
 * A nested class to hold the state information of the executing thread for
 * a served exertion.
 */
public class ExertionSessionInfo {

    static LeaseRenewalManager lrm = new LeaseRenewalManager();

    private static class ExertionSessionBundle {
        public Uuid exertionID;
        public MonitoringSession session;
    }

    private static final ThreadLocal<ExertionSessionBundle> tl = new ThreadLocal<ExertionSessionBundle>() {
        @Override
        protected ExertionSessionBundle initialValue() {
            return new ExertionSessionBundle();
        }
    };

    public static void add(ServiceExertion ex) {
        ExertionSessionBundle esb = tl.get();
        esb.exertionID = ex.getId();
        esb.session = MonitorHelper.getMonitoringSession(ex.getControlContext());
        if (esb.session != null)
            lrm.renewUntil(
                    esb.session.getLease(),
                    Lease.ANY, null);
    }

    public static MonitoringSession getSession() {
        ExertionSessionBundle esb = tl.get();
        return (esb != null) ? esb.session : null;
    }

    public static Uuid getID() {
        ExertionSessionBundle esb = tl.get();
        return (esb != null) ? esb.exertionID : null;
    }

    public static void removeLease() {
        ExertionSessionBundle esb = tl.get();
        try {
            lrm.remove(esb.session.getLease());
        } catch (Exception e) {
        }
    }
}
