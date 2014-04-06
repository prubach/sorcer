package sorcer.core.provider.exertmonitor;

import net.jini.core.event.RemoteEventListener;
import sorcer.core.monitor.MonitoringManagement;
import sorcer.core.monitor.MonitoringSession;
import sorcer.service.Accessor;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Exertion;

import java.rmi.RemoteException;

/**
 * Transitional class
 *
 * @author Rafał Krupiński
 */
public class MonitorHelper {
    private static MonitoringManagement sessionMonitor = Accessor.getService(MonitoringManagement.class);

    public static <T extends Exertion> T register(RemoteEventListener lstnr, T ex,
                                                  long duration) throws RemoteException {
        return (T) sessionMonitor.register(lstnr, ex, duration);
    }


    private static String PATH = "sorcer/meta/monitor/session";
    //path for MonitoringManagement "exertion/monitor/enabled"

    public static void setMonitoringSession(Context ctx, MonitoringSession monitorSession) {
        try {
            ctx.putValue(PATH, monitorSession);
        } catch (ContextException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static MonitoringSession getMonitoringSession(Context ctx) {
        try {
            Object value = ctx.getValue(PATH);
            if (value == Context.none)
                return null;
            return (MonitoringSession) value;
        } catch (ContextException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
