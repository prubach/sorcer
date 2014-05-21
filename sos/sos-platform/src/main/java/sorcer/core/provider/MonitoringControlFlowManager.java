/*
 * Copyright 2014 Sorcersoft.com S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorcer.core.provider;

import net.jini.core.lease.Lease;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.lease.LeaseRenewalManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.exertion.NetTask;
import sorcer.core.monitor.MonitorSessionManagement;
import sorcer.core.monitor.MonitoringManagement;
import sorcer.core.monitor.MonitoringSession;
import sorcer.service.*;

import java.rmi.RemoteException;

import static sorcer.service.Exec.FAILED;
import static sorcer.service.monitor.MonitorUtil.getMonitoringSession;

/**
 * @author Rafał Krupiński
 */
public class MonitoringControlFlowManager extends ControlFlowManager {
    final private static Logger log = LoggerFactory.getLogger(MonitoringControlFlowManager.class);

    public static final long LEASE_RENEWAL_PERIOD = 1 * 1000 * 30L;
    public static final long DEFAULT_TIMEOUT_PERIOD = 1 * 1000 * 60L;

    private MonitorSessionManagement sessionMonitor;

    private LeaseRenewalManager lrm;

    public MonitoringControlFlowManager(Exertion exertion, ProviderDelegate delegate) {
        super(exertion, delegate);
        sessionMonitor = Accessor.getService(MonitoringManagement.class);
        lrm = new LeaseRenewalManager();
    }

    public MonitoringControlFlowManager(Exertion exertion, ProviderDelegate delegate, Jobber jobber) {
        super(exertion, delegate, jobber);
        sessionMonitor = Accessor.getService(MonitoringManagement.class);
        lrm = new LeaseRenewalManager();
    }

    public MonitoringControlFlowManager(Exertion exertion, ProviderDelegate delegate, Spacer spacer) {
        super(exertion, delegate, spacer);
        sessionMonitor = Accessor.getService(MonitoringManagement.class);
        lrm = new LeaseRenewalManager();
    }

    public MonitoringControlFlowManager(Exertion exertion, ProviderDelegate delegate, Concatenator concatenator) {
        super(exertion, delegate, concatenator);
        sessionMonitor = Accessor.getService(MonitoringManagement.class);
        lrm = new LeaseRenewalManager();
    }

    @Override
    public Exertion process() throws ExertionException {
        try {
            exertion = register(exertion);
        } catch (RemoteException e) {
            throw new ExertionException(e);
        }
        MonitoringSession monSession = getMonitoringSession(exertion);

        try {
            monSession.init((Monitorable) delegate.getProvider().getProxy(), LEASE_RENEWAL_PERIOD,
                    DEFAULT_TIMEOUT_PERIOD);
            lrm.renewUntil(monSession.getLease(), Lease.ANY, null);

            NetTask result = (NetTask) super.process();
            Exec.State resultState = result.getStatus() <= FAILED ? Exec.State.FAILED : Exec.State.DONE;
            try {
                monSession.changed(result.getContext(), resultState);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Error while executing monitorable exertion", e);
                result.reportException(e);
                throw new ExertionException(e);
            }
            return result;
        } catch (RemoteException e) {
            String msg = "RemoteException from local call";
            log.error(msg,e);
            throw new IllegalStateException(msg, e);
        } catch (MonitorException e) {
            String msg = "RemoteException from local call";
            log.error(msg,e);
            throw new IllegalStateException(msg, e);
        } finally {
            try {
                lrm.remove(monSession.getLease());
            } catch (UnknownLeaseException e) {
                log.debug("Error while removing lease for {}", exertion.getName(), e);
            }
        }
    }

    private Exertion register(Exertion exertion) throws RemoteException {

        ServiceExertion registeredExertion = (ServiceExertion) (sessionMonitor.register(null,
                exertion, LEASE_RENEWAL_PERIOD));

        MonitoringSession session = getMonitoringSession(registeredExertion);
        log.info("Session for the exertion = {}", session);
        log.info("Lease to be renewed for duration = {}",
                (session.getLease().getExpiration() - System
                        .currentTimeMillis())
        );
        lrm.renewUntil(session.getLease(), Lease.ANY, null);
        return registeredExertion;
    }
}
