package sorcer.core.provider.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.lease.LeaseRenewalManager;
import org.slf4j.LoggerFactory;
import sorcer.core.RemoteLogger;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * SORCER class
 * User: prubach
 * Date: 05.06.14
 */
public class LoggerRemoteEventClient implements RemoteEventListener, Serializable, Runnable {

        private RemoteEventListener proxy = null;
        private Exporter exporter = null;
        private boolean running = true;

        private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LoggerRemoteEventClient.class);

		/*
		 * The arguments should be passed as proxies such that they
		 * can be used directly by client.
		 *
		 */

        public LoggerRemoteEventClient(RemoteLogger serverProxy, String hostAddress, Map<String,String> filterMap)
                throws RemoteException {
           try {
                //Make a proxy of myself to pass to the server/filter
                exporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(hostAddress, 0),
                        new BasicILFactory());
                proxy = (RemoteEventListener)exporter.export(this);

                //register as listener with server and passing the
                //event registration to the filter while registering there.
                logger.debug("Registering with event server: " + serverProxy);
                EventRegistration evReg = serverProxy.registerLogListener(proxy, null, Lease.FOREVER, filterMap);
                logger.debug("Got registration " + evReg.getID() + " " + evReg.getSource() + " " + evReg.toString());
                Lease providersEventLease = evReg.getLease();
                LeaseRenewalManager lrm = new LeaseRenewalManager();
                providersEventLease.renew(Lease.ANY);
                lrm.renewUntil(providersEventLease,Lease.FOREVER, 30000, null);

            } catch (Exception e){
                logger.error("Exception while initializing Listener client " + e);
            }
        }

        public void destroy() {
            running = false;
        }


        /*
         * The method given by the RemoteEventListener interface and thus
         * available remotely. Called by the logger when new logs appear
         */
        public void notify(RemoteEvent event)
                throws UnknownEventException, RemoteException {
            LoggerRemoteEvent logEvent = (LoggerRemoteEvent)event;
            ILoggingEvent le = logEvent.getLoggingEvent();
            // Print everything to console as if it was a local log
            // TODO: print nicely with a marker showing that it's a remote log
            ((ch.qos.logback.classic.Logger)logger).callAppenders(logEvent.getLoggingEvent());
        }

    @Override
    public void run() {
            try {
                while (running) {
                    Thread.sleep(500);
                }
                exporter.unexport(true);
            } catch (InterruptedException e) {
            }
    }
}
