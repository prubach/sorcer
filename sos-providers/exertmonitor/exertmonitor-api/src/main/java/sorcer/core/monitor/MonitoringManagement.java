/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
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
package sorcer.core.monitor;

import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.id.Uuid;
import sorcer.core.provider.exertmonitor.IMonitorSession;
import sorcer.core.provider.exertmonitor.lease.IMonitorLandlord;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.service.MonitorException;
import sorcer.service.Monitorable;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MonitoringManagement extends MonitorUIManagement {

    public Exertion register(RemoteEventListener lstnr, Exertion ex,
                             long duration) throws RemoteException;

    public boolean persist(IMonitorSession session) throws IOException;

    public void update(Uuid cookie, Context ctx, Object aspect)
            throws RemoteException, MonitorException;

    public int getState(Uuid cookie) throws RemoteException,
            MonitorException;

    /**
   	 * Makes this an active session. The jobber decides the lease duration and
   	 * the timeout after which the monitor will call on monitorables that the
   	 * job is failed and report back to the Listener that the exertion of this
   	 * session has failed.
   	 *
   	 * @param mntrbl
   	 *            The monitorable to which this task is dispatched.
   	 * @param duration
   	 *            Requested lease duration for the session.
   	 * @param timeout
   	 *            Timeout for execution of this task.
   	 *
   	 * @throws MonitorException
   	 *             1) If this session is already active 2) If there is no such
   	 *             session
   	 *
   	 * @throws RemoteException
   	 *             if there is a communication error
   	 *
   	 * @see net.jini.core.transaction.server.TransactionConstants
   	 */

   	public Lease init(Uuid cookie, Monitorable mntrbl, long duration,
   			long timeout) throws RemoteException, MonitorException;


}
