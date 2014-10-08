/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
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
package sorcer.core.provider.rendezvous;

import java.rmi.RemoteException;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.id.UuidFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerConstants;
import sorcer.core.dispatch.BlockThread;
import sorcer.core.provider.Concatenator;
import sorcer.core.provider.ControlFlowManager;
import sorcer.core.provider.MonitoringControlFlowManager;
import sorcer.core.provider.ServiceProvider;
import sorcer.service.Block;
import sorcer.service.Condition;
import sorcer.service.Executor;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Job;
import sorcer.service.ServiceExertion;

import com.sun.jini.start.LifeCycle;

/**
 * ServiceJobber - The SORCER rendezvous service provider that provides
 * coordination for executing exertions using directly (PUSH) service providers.
 * 
 */
public class ServiceConcatenator extends RendezvousBean implements Concatenator, Executor, SorcerConstants {
	private Logger logger = LoggerFactory.getLogger(ServiceConcatenator.class.getName());

    public ServiceConcatenator() throws RemoteException {
    }

    public Exertion execute(Exertion exertion) throws RemoteException,
			TransactionException, ExertionException {
		return execute(exertion, null);
	}
	
	public Exertion execute(Exertion exertion, Transaction txn)
			throws TransactionException, ExertionException, RemoteException {
        setServiceID(exertion);
        try {
            if ((exertion).getControlContext().isMonitorable()
                    && !((exertion).getControlContext()).isWaitable()) {
                replaceNullExertionIDs(exertion);
                new BlockThread((Block) exertion, provider).start();
                return exertion;
            } else {
                BlockThread blockThread = new BlockThread((Block) exertion, provider);
                blockThread.start();
                blockThread.join();
                Block result = blockThread.getResult();
                Condition.cleanupScripts(result);
                logger.trace("<== Result: " + result);
                return result;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
	}

}
