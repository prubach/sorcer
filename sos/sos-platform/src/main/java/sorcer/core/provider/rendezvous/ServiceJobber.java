/*
 * Copyright 2013 the original author or authors.
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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
package sorcer.core.provider.rendezvous;

import com.sun.jini.start.LifeCycle;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.id.UuidFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerEnv;
import sorcer.core.context.Contexts;
import sorcer.core.dispatch.DispatcherFactory;
import sorcer.core.dispatch.ExertionDispatcherFactory;
import sorcer.core.dispatch.JobThread;
import sorcer.core.provider.ControlFlowManager;
import sorcer.core.provider.Jobber;
import sorcer.core.provider.MonitoringControlFlowManager;
import sorcer.core.provider.ServiceProvider;
import sorcer.service.*;

import sorcer.util.StringUtils;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static sorcer.core.SorcerConstants.MAIL_SEP;

/**
 * ServiceJobber - The SORCER rendezvous service provider that provides
 * coordination for executing exertions using directly (PUSH) service providers.
 * 
 */
public class ServiceJobber extends RendezvousBean implements Jobber, Executor {
    private Logger logger = LoggerFactory.getLogger(ServiceJobber.class.getName());

    public ServiceJobber() throws RemoteException {
		// do nothing
	}

	public Exertion execute(Exertion job, Transaction txn)
			throws TransactionException, ExertionException, RemoteException {

            setServiceID(job);
            try {
                JobThread jobThread = new JobThread((Job)job, provider, getDispatcherFactory(job));
                if (job.getControlContext().isMonitorable()
                        && !job.getControlContext().isWaitable()) {
                    replaceNullExertionIDs(job);
                    notifyViaEmail(job);
                    new Thread(jobThread, ((Job)job).getContextName()).start();
                    return job;
                } else {
                    jobThread.run();
                    Job result = jobThread.getResult();
                    logger.trace("<== Result: " + result);
                    return result;
                }
            } catch (Exception e) {
                ((Job)job).reportException(e);
                logger.warn("Error", e);
                return job;
            }
	}

    protected DispatcherFactory getDispatcherFactory(Exertion exertion) {
        return ExertionDispatcherFactory.getFactory();
    }

}
