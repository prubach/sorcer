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
package sorcer.core.provider.jobber;

import com.sun.jini.start.LifeCycle;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.id.UuidFactory;
import sorcer.core.SorcerEnv;
import sorcer.core.context.Contexts;
import sorcer.core.dispatch.DispatcherFactory;
import sorcer.core.dispatch.ExertionDispatcherFactory;
import sorcer.core.dispatch.JobThread;
import sorcer.core.provider.ControlFlowManager;
import sorcer.core.provider.Jobber;
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
public class ServiceJobber extends ServiceProvider implements Jobber, Executor {
	public ServiceJobber() throws RemoteException {
		// do nothing
	}

	// require constructor for Jini 2 NonActivatableServiceDescriptor
	public ServiceJobber(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle);
	}
	
	public void setServiceID(Exertion ex) {
		// By default it's ServiceJobber associated with servlet.
		// Not service.
		try {
			if (getProviderID() != null) {
				logger.trace(getProviderID().getLeastSignificantBits() + ":"
						+ getProviderID().getMostSignificantBits());
				((ServiceExertion) ex).setLsbId(getProviderID()
						.getLeastSignificantBits());
				((ServiceExertion) ex).setMsbId(getProviderID()
						.getMostSignificantBits());
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public Exertion service(Exertion exertion) throws RemoteException, ExertionException {
		logger.trace("service: " + exertion.getName());
		try {
			// Jobber overrides SorcerProvider.service method here
			setServiceID(exertion);
			// Create an instance of the ExertionProcessor and call on the
			// process method, returns an Exertion
			return new ControlFlowManager(exertion, delegate, this).process(threadManager);

		} 
		catch (Exception e) {
			throw new ExertionException(e);
		}
	}

	public Exertion execute(Exertion exertion) throws RemoteException,
			TransactionException, ExertionException {
		return execute(exertion, null);
	}
	
	public Exertion execute(Exertion exertion, Transaction txn)
			throws TransactionException, ExertionException, RemoteException {
		return doJob(exertion, txn);
	}

	public Exertion doJob(Exertion job) {
		return doJob(job, null);
	}
	
	public Exertion doJob(Exertion job, Transaction txn) {
		//logger.info("*********************************************ServiceJobber.doJob(), job = " + job);

		setServiceID(job);
		try {
            Job _job = (Job) job;
            JobThread jobThread = new JobThread(_job, this, getDispatcherFactory(job));
			if (job.getControlContext().isMonitorable()
					&& !job.getControlContext().isWaitable()) {
				replaceNullExertionIDs(job);
				notifyViaEmail(job);
                new Thread(jobThread, _job.getContextName()).start();
                return job;
			} else {
				jobThread.run();
				Job result = jobThread.getResult();
				logger.trace("<== Result: " + result);
				return result;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

    protected DispatcherFactory getDispatcherFactory(Exertion exertion) {
        return ExertionDispatcherFactory.getFactory();
    }

    protected void replaceNullExertionIDs(Exertion ex) {
		if (ex != null && ex.getId() == null) {
			((ServiceExertion) ex)
					.setId(UuidFactory.generate());
			if (ex.isJob()) {
				for (int i = 0; i < ((Job) ex).size(); i++)
					replaceNullExertionIDs(((Job) ex).get(i));
			}
		}
	}

	protected void notifyViaEmail(Exertion ex) throws ContextException {
		if (ex == null || ex.isTask())
			return;
		Job job = (Job) ex;
		List<String> recipents = null;
		String notifyees = job.getControlContext()
				.getNotifyList();
		if (notifyees != null) {
			String[] list = StringUtils.tokenize(notifyees, MAIL_SEP);
			recipents = new ArrayList<String>(list.length);
			Collections.addAll(recipents, list);
		}
		String to = "", admin = SorcerEnv.getProperty("sorcer.admin");
		if (recipents == null) {
			if (admin != null) {
				recipents = new ArrayList<String>();
				recipents.add(admin);
			}
		} else if (admin != null && !recipents.contains(admin))
			recipents.add(admin);

		if (recipents == null)
			to = to + "No e-mail notifications will be sent for this job.";
		else {
			to = to + "e-mail notification will be sent to\n";
			for (String recipent : recipents) to = to + "  " + recipent + "\n";
		}
		String comment = "Your job '" + job.getName()
				+ "' has been submitted.\n" + to;
		job.getControlContext().setFeedback(comment);
		if (job.getMasterExertion() != null
				&& job.getMasterExertion().isTask()) {
			job.getMasterExertion().getDataContext()
					.putValue(Context.JOB_COMMENTS, comment);

			Contexts.markOut(job.getMasterExertion()
					.getDataContext(), Context.JOB_COMMENTS);

		}
	}

	@Override
	public Exertion doExertion(Exertion exertion, Transaction txn) throws ExertionException {
		return new ControlFlowManager(exertion, delegate, this).process(threadManager);
	}
}
