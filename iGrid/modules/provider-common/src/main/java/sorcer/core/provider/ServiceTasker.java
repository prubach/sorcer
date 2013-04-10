/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
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

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.security.auth.Subject;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import sorcer.service.Executor;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.ServiceExertion;
import sorcer.service.Signature;
import sorcer.service.Task;
import sorcer.service.Tasker;

import com.sun.jini.start.LifeCycle;

/**
 * A <code>ServiceTasker</code> is a service coordinating execution of all
 * methods as specified by signatures associated with the instance of service
 * {@link sorcer.service.Task} and control flow exertion of task type.
 */
public class ServiceTasker extends ServiceProvider implements Tasker, Executor, Remote {

	public ServiceTasker() throws RemoteException {
		super();
	}

	/**
	 * Required constructor for Jini 2 NonActivatableServiceDescriptors
	 * 
	 * @param args
	 * @param lifeCycle
	 * @throws Exception
	 */
	public ServiceTasker(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle);
	}

	/** {@inheritDoc} */
	public ServiceExertion execute(Exertion task) throws TransactionException,
			ExertionException {
		return execute(task, null);
	}

	/** {@inheritDoc} */
	public ServiceExertion execute(Exertion task, Transaction transaction)
			throws TransactionException, ExertionException {
		return (Task) new ExertProcessor(task, delegate)
				.process(threadManager);
	}

	/** {@inheritDoc} */
	public boolean isAuthorized(Subject subject, Signature signature) {
		return true;
	}
	
}