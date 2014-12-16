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
package sorcer.core.provider.rendezvous;

import java.rmi.RemoteException;
import java.util.Vector;

import javax.inject.Inject;
import javax.security.auth.Subject;

import net.jini.core.lookup.ServiceID;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.id.UuidFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerConstants;
import sorcer.core.context.Contexts;
import sorcer.core.context.ControlContext;
import sorcer.core.exertion.NetJob;
import sorcer.core.exertion.ObjectBlock;
import sorcer.core.exertion.ObjectJob;
import sorcer.core.provider.*;
import sorcer.service.*;
import sorcer.util.Sorcer;
import sorcer.util.SorcerUtil;

/**
 * 
 * @author Mike Sobolewski
 */
abstract public class RendezvousBean implements Service, Executor, SorcerConstants {
	private Logger logger = LoggerFactory.getLogger(RendezvousBean.class.getName());


    protected ServiceProvider provider;

    protected ProviderDelegate delegate;

	public RendezvousBean() throws RemoteException {
		// do nothing
	}
	
	public void init(Provider provider) {
		this.provider = (ServiceProvider)provider;
		this.delegate = ((ServiceProvider)provider).getDelegate();
		//this.threadManager = ((ServiceProvider)provider).getThreadManager();
		try {
			logger = provider.getLogger();
		} catch (RemoteException e) {
			// ignore it, local call
		}
	}

	public String getProviderName()  {
		try {
			return provider.getProviderName();
		} catch (RemoteException e) {
			// ignore local call
			return null;
		}
	}
	
	/** {@inheritDoc} */
	public boolean isAuthorized(Subject subject, Signature signature) {
		return true;
	}
	
	protected void replaceNullExertionIDs(Exertion ex) {
		if (ex != null && ((ServiceExertion) ex).getId() == null) {
			((ServiceExertion) ex)
					.setId(UuidFactory.generate());
			if (((ServiceExertion) ex).isJob()) {
				for (int i = 0; i < ((Job) ex).size(); i++)
					replaceNullExertionIDs(((Job) ex).get(i));
			}
		}
	}

	protected void notifyViaEmail(Exertion ex) throws ContextException {
		if (ex == null || ((ServiceExertion) ex).isTask())
			return;
		Job job = (Job) ex;
		Vector recipents = null;
		String notifyees = ((ControlContext) ((NetJob)job).getControlContext())
				.getNotifyList();
		if (notifyees != null) {
			String[] list = SorcerUtil.tokenize(notifyees, MAIL_SEP);
			recipents = new Vector(list.length);
			for (int i = 0; i < list.length; i++)
				recipents.addElement(list[i]);
		}
		String to = "", admin = Sorcer.getProperty("sorcer.admin");
		if (recipents == null) {
			if (admin != null) {
				recipents = new Vector();
				recipents.addElement(admin);
			}
		} else if (admin != null && !recipents.contains(admin))
			recipents.addElement(admin);

		if (recipents == null)
			to = to + "No e-mail notifications will be sent for this job.";
		else {
			to = to + "e-mail notification will be sent to\n";
			for (int i = 0; i < recipents.size(); i++)
				to = to + "  " + recipents.elementAt(i) + "\n";
		}
		String comment = "Your job '" + job.getName()
				+ "' has been submitted.\n" + to;
		((ControlContext) ((NetJob)job).getControlContext()).setFeedback(comment);
		if (job.getMasterExertion() != null
				&& ((ServiceExertion) job.getMasterExertion()).isTask()) {
			((ServiceExertion) (job.getMasterExertion())).getContext()
					.putValue(Context.JOB_COMMENTS, comment);

			Contexts.markOut(((ServiceExertion) (job.getMasterExertion()))
					.getContext(), Context.JOB_COMMENTS);

		}
	}
	
	public void setServiceID(Exertion ex) {
		if (provider == null) {
                logger.info("Running locally as object - no provider!!!!!!!!!!");
				provider = new ServiceProvider();
				init (provider);
		}
		try {
			ServiceID id = provider.getProviderID();
			if (id != null) {
				logger.trace(id.getLeastSignificantBits() + ":"
                        + id.getMostSignificantBits());
				((ServiceExertion) ex).setLsbId(id.getLeastSignificantBits());
				((ServiceExertion) ex).setMsbId(id.getMostSignificantBits());
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private String getDataURL(String filename) {
		return delegate.getProviderConfig().getProperty(
				"provider.dataURL")
				+ filename;
	}

	private String getDataFilename(String filename) {
		return delegate.getProviderConfig().getDataDir() + "/"
				+ filename;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.provider.ServiceBean#service(sorcer.service.Exertion, net.jini.core.transaction.Transaction)
	 */
	@Override
	public Exertion service(Exertion exertion, Transaction transaction) throws RemoteException, ExertionException {
		try {
            logger.info("Got exertion to process: " + exertion.toString());
			setServiceID(exertion);
            if (exertion instanceof ObjectJob || exertion instanceof ObjectBlock)
                return execute(exertion, transaction);
            else
                return getControlFlownManager(exertion).process();
            //exrt.getDataContext().setExertion(null); ???
        }
		catch (Exception e) {
			e.printStackTrace();
			throw new ExertionException();
		}
	}

    protected ControlFlowManager getControlFlownManager(Exertion exertion) throws ExertionException {
        try {
            if (exertion.isMonitorable())
                return new MonitoringControlFlowManager(exertion, delegate, this);
            else
                return new ControlFlowManager(exertion, delegate, this);
        } catch (Exception e) {
            ((Task) exertion).reportException(e);
            throw new ExertionException(e);
        }
    }

	public Exertion service(Exertion exertion) throws RemoteException, ExertionException, TransactionException {
		return service(exertion, null);
	}
		
	abstract public Exertion execute(Exertion exertion, Transaction txn)
			throws TransactionException, ExertionException, RemoteException;
	
	public Exertion execute(Exertion exertion)
			throws TransactionException, ExertionException, RemoteException {
		return execute(exertion, null);
	}

	/* (non-Javadoc)
 * @see sorcer.service.Evaluation#asis()
 */
	@Override
	public Object asis() throws EvaluationException, RemoteException {
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getValue(sorcer.service.Arg[])
	 */
	@Override
	public Object getValue(Arg... entries) throws EvaluationException,
			RemoteException {
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#substitute(sorcer.service.Arg[])
	 */
	@Override
	public Evaluation substitute(Arg... entries) throws SetterException,
			RemoteException {
		return null;
	}

}
