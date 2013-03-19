package sorcer.service;

/**
 * @author Rafał Krupiński
 */
public interface MonitoredExertion extends Exertion{
    MonitoringSession getMonitorSession();
}
