package sorcer.core.provider.exertmonitor;

import sorcer.core.monitor.MonitoringSession;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Exec;
import sorcer.util.StringUtils;

/**
 * extracted from ServiceContext
 *
 * @author Rafał Krupiński
 */
public class MonitorClient {
    /**
     * Records this context in related monitoring session.
     */
    public void checkpoint(Context ctx) throws ContextException {
        MonitoringSession session = MonitorHelper.getMonitoringSession(ctx);
        if (session == null)
            return;

        try {
            ctx.putValue("context/checkpoint/time", StringUtils.getDateTime());
            session.changed(ctx, Exec.State.UPDATED);
        } catch (ContextException x) {
            throw x;
        } catch (Exception e) {
            throw new ContextException(e);
        }
    }
}
