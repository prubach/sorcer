/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
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
package sorcer.core.provider.jobber;

import java.rmi.RemoteException;
import java.util.HashSet;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import sorcer.core.dispatch.DispatcherException;
import sorcer.core.dispatch.DispatcherFactory;
import sorcer.core.provider.MonitoringControlFlowManager;
import sorcer.core.provider.Provider;
import sorcer.core.dispatch.ExertionDispatcherFactory;
import sorcer.core.dispatch.SpaceTaskDispatcher;
import sorcer.core.exertion.NetTask;
import sorcer.core.loki.member.LokiMemberUtil;
import sorcer.core.provider.ControlFlowManager;
import sorcer.service.*;
import sorcer.core.provider.Spacer;

import com.sun.jini.start.LifeCycle;

import static sorcer.util.StringUtils.tName;

/**
 * ServiceSpacer - The SORCER rendezvous service provider that provides
 * coordination for executing exertions using JavaSpace from which provides PULL
 * exertions to be executed.
 */
public class ServiceSpacer extends ServiceJobber implements Spacer, Executor {
    private LokiMemberUtil myMemberUtil;

    /**
     * ServiceSpacer - Default constructor
     *
     * @throws RemoteException
     */
    public ServiceSpacer() throws RemoteException {
        myMemberUtil = new LokiMemberUtil(ServiceSpacer.class.getName());
    }

    /**
     * Require ctor for Jini 2 NonActivatableServiceDescriptor
     *
     * @throws RemoteException
     */
    public ServiceSpacer(String[] args, LifeCycle lifeCycle) throws Exception {
        super(args, lifeCycle);
        myMemberUtil = new LokiMemberUtil(ServiceSpacer.class.getName());
    }

    public Exertion execute(Exertion exertion, Transaction txn)
            throws TransactionException, RemoteException, ExertionException {
        if (exertion.isJob())
            return super.execute(exertion, txn);
        else
            return doTask(exertion);
    }

    protected class TaskThread extends Thread {

        // doJob method calls this internally
        private Task task;

        private Task result;

        private Provider provider;

        public TaskThread(Task task, Provider provider) {
            super(tName("Task-" + task.getName()));
            this.task = task;
            this.provider = provider;
        }

        public void run() {
            logger.trace("*** TaskThread Started ***");
            try {
                SpaceTaskDispatcher dispatcher = (SpaceTaskDispatcher) getDispatcherFactory(task).createDispatcher(task,
                        new HashSet<Context>(), false, provider);
                try {
                    task.getControlContext().appendTrace(provider.getProviderName() + " dispatcher: "
                            + dispatcher.getClass().getName());
                } catch (RemoteException e) {
                    //ignore it, local call
                }
                while (dispatcher.getState() != Exec.DONE
                        && dispatcher.getState() != Exec.FAILED
                        && dispatcher.getState() != Exec.SUSPENDED) {
                    logger.debug("Dispatcher waiting for a space task... Sleeping for 250 milliseconds.");
                    Thread.sleep(250);
                }
                logger.debug("Dispatcher State: " + dispatcher.getState());
				result = (NetTask) dispatcher.getExertion();
            } catch (InterruptedException e) {
				logger.warn("Interrupted", e);
            } catch (DispatcherException e) {
                logger.warn("Error while executing space task {}", task.getName(), e);
                task.reportException(e);
            }
        }

        public Task getResult() throws ContextException {
            return result;
        }
    }

    public Exertion doTask(Exertion task) throws RemoteException {
        setServiceID(task);
        try {
            if (task.isMonitorable()
                    && !task.isWaitable()) {
                replaceNullExertionIDs(task);
                notifyViaEmail(task);
                new TaskThread((Task) task, this).start();
                return task;
            } else {
                TaskThread taskThread = new TaskThread((Task) task, this);
                taskThread.start();
                taskThread.join();
                Task result = taskThread.getResult();
                logger.trace("Spacer result: " + result);
                return result;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

	@Override
    protected DispatcherFactory getDispatcherFactory(Exertion exertion) {
        if (exertion.isSpacable())
            return ExertionDispatcherFactory.getFactory(myMemberUtil);
        else
            return super.getDispatcherFactory(exertion);
    }

    @Override
    protected ControlFlowManager getControlFlownManager(Exertion exertion) throws ExertionException {
        if (!exertion.isSpacable())
            throw new ExertionException(new IllegalArgumentException("Exertion not spacable: " + exertion));

        if (exertion.isMonitorable())
            return new MonitoringControlFlowManager(exertion, delegate, this);
        else
            return new ControlFlowManager(exertion, delegate, this);
    }
}
