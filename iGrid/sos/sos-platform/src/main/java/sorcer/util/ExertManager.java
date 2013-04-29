/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.util;

import net.jini.core.lookup.ServiceID;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.server.TransactionManager;
import org.dancres.blitz.jini.lockmgr.LockResult;
import org.dancres.blitz.jini.lockmgr.MutualExclusion;
import sorcer.co.tuple.Parameter;
import sorcer.core.Provider;
import sorcer.core.SorcerConstants;
import sorcer.core.exertion.NetJob;
import sorcer.core.provider.ControlFlowManager;
import sorcer.core.signature.NetSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.jini.lookup.ProviderID;
import sorcer.service.*;
import sorcer.service.Strategy.Access;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import sorcer.core.context.ControlContext.ThrowableTrace;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings("rawtypes")
public class ExertManager implements Callable {
	protected final static Logger logger = Logger.getLogger(ExertManager.class
			.getName());

	private ServiceExertion exertion;
	private Transaction transaction;
	private static MutualExclusion locker;

	public ExertManager(Exertion xrt) {
		exertion = (ServiceExertion) xrt;
	}

	public ExertManager(Exertion xrt, Transaction txn) {
		exertion = (ServiceExertion) xrt;
		transaction = txn;

	}

	public Exertion exert(Parameter... entries) throws TransactionException, ExertionException,
			RemoteException {
		try {
			exertion.substitute(entries);
		} catch (EvaluationException e) {
			e.printStackTrace();
			throw new  ExertionException(e);
		}
		return exert((Transaction) null, (String) null);
	}

	public Exertion exert(Exertion xrt) throws TransactionException,
			ExertionException, RemoteException {
		return exert(xrt, (Transaction) null, (String) null);
	}

	public Exertion exert(Exertion xrt, Transaction txn)
			throws TransactionException, ExertionException, RemoteException {
		transaction = txn;
		return exert(xrt, txn, (String) null);
	}

	public Exertion exert(String providerName) throws TransactionException,
			ExertionException, RemoteException {
		return exert(null, providerName);
	}

	public Exertion exert(Exertion xrt, Transaction txn, String providerName)
			throws TransactionException, ExertionException, RemoteException {
		this.exertion = (ServiceExertion) xrt;
		transaction = txn;
		return exert(txn, providerName);
	}
	
	public Exertion exert(Transaction txn, String providerName, Parameter... entries)
			throws TransactionException, ExertionException, RemoteException {
		try {
			exertion.substitute(entries);
		} catch (EvaluationException ex) {
			ex.printStackTrace();
			throw new ExertionException();
		}
		if (exertion instanceof Job && ((Job) exertion).size() == 1) {
			return processAsTask();
		}
		transaction = txn;
		Context<?> cxt = exertion.getDataContext();
		cxt.setExertion(exertion);
		Signature signature = exertion.getProcessSignature();
		Servicer provider = null;
		try {
			if (!(signature instanceof NetSignature)) {
				if (exertion instanceof Task) {
					if (exertion.getSignatures().size() == 1) {
						return ((Task) exertion).doTask(txn);
					} else {
						try {
							return new ControlFlowManager().doTask((Task)exertion);
						} catch (ContextException e) {
							e.printStackTrace();
							throw new ExertionException(e);
						}
					}
				}
				else if (!(exertion instanceof NetJob))
					return ((Job) exertion).doJob(txn);
			}
			// check for missing signature of inconsistent PULL/PUSH cases
			signature = correctProcessSignature();
			if (!((ServiceSignature) signature).isSelectable()) {
				exertion.reportException(new ExertionException(
						"No such operation in the requested signature: "
								+ signature));
				logger.warning("Not selectable exertion operation: " + signature);
				return exertion;
			}

			if (providerName != null && providerName.length() > 0) {
				signature.setProviderName(providerName);
			}
			logger.finer("* Exertion shell's servicer accessor: "
					+ Accessor.getAccessor());
			provider = ((NetSignature) signature).getServicer();
		} catch (SignatureException e) {
			e.printStackTrace();
			new ExertionException(e);
		}
		if (provider == null) {
			// handle signatures for PULL tasks
			if (!exertion.isJob()
					&& exertion.getControlContext().getAccessType() == Access.PULL) {
				signature = new NetSignature("service", Spacer.class, Sorcer.getActualSpacerName());
			}
			try {
				provider = Accessor.getServicer(signature);
			} catch (SignatureException e) {
				exertion.getControlContext().addException(
						"no provider avaialable", e);
				exertion.getControlContext().appendTrace(
						"xrt shell:" + exertion.getName());
				exertion.setStatus(ExecState.FAILED);
				throw new ExertionException("exerting failed ", e);
			}
		}
		// provider = ProviderLookup.getService(signature);
		// provider = ProviderAccessor.getProvider(null, signature
		// .getServiceType());
		if (provider == null) {
			logger.warning("* Provider not available for: " + signature);
			exertion.setStatus(ExecState.FAILED);
			exertion.reportException(new RuntimeException(
					"Cannot find provider for: " + signature));
			return exertion;
		}
		exertion.getControlContext().appendTrace(
				"bootstrapping: " + ((Provider) provider).getProviderName()
						+ ":" + ((Provider) provider).getProviderID());
		((NetSignature) signature).setServicer(provider);
		logger.info("Provider found: " + provider + ", for: " + signature);
		Exertion result = null;
		if (((Provider) provider).mutualExclusion()) {
			 result =  serviceMutualExclusion((Provider) provider, exertion,
					transaction);
		} else {
			 result = provider.service(exertion, transaction);
		}
		if (result.getExceptions().size() > 0) {
			for (ThrowableTrace et : result.getExceptions()) {
				if (et.getThrowable() instanceof Error)
					((ServiceExertion)result).setStatus(ExecState.ERROR); 
			}
			((ServiceExertion)result).setStatus(ExecState.FAILED); 
		}		
		return result;
	}

	private Exertion processAsTask() throws RemoteException,
			TransactionException, ExertionException {
		Task task = (Task) exertion.getExertions().get(0);
		task = (Task) task.exert();
		exertion.getExertions().set(0, task);
		exertion.setStatus(task.getStatus());
		return exertion;
	}

	private Exertion serviceMutualExclusion(Provider provider,
			Exertion exertion, Transaction transaction) throws RemoteException,
			TransactionException, ExertionException {
		ServiceID mutexId = provider.getProviderID();
		if (locker == null) {
			locker = (MutualExclusion) ProviderLookup
					.getService(MutualExclusion.class);
		}
		TransactionManager transactionManager = ProviderAccessor
				.getTransactionManager();
		Transaction txn = null;

		LockResult lr = locker.getLock(""
				+ exertion.getProcessSignature().getServiceType(),
				new ProviderID(mutexId), txn,
				((ServiceExertion) exertion).getId());
		if (lr.didSucceed()) {
			exertion.getControlContext().setMutexId(provider.getProviderID());
			Exertion xrt = provider.service(exertion, transaction);
			txn.commit();
			return xrt;
		} else {
			// try continue to get lock, if failed abort the transaction txn
			txn.abort();
		}
		exertion.getControlContext().addException(
				new ExertionException("no lock avaialable for: "
						+ provider.getProviderName() + ":"
						+ provider.getProviderID()));
		return exertion;
	}

	/**
	 * Depending on provider access type correct inconsistent signatures for
	 * composite exertions only. Tasks go either to its provider directly or
	 * Spacer depending on their provider access type (PUSH or PULL).
	 * 
	 * @return the corrected signature
	 */
	public Signature correctProcessSignature() {
		if (!exertion.isJob())
			return exertion.getProcessSignature();
		Signature sig = exertion.getProcessSignature();
		if (sig != null) {
			Access access = exertion.getControlContext().getAccessType();
			if ((Access.PULL == access || Access.QOS_PULL == access)
					&& !exertion.getProcessSignature().getServiceType()
							.isAssignableFrom(Spacer.class)) {
				sig.setServiceType(Spacer.class);
				((NetSignature) sig).setSelector("service");
				sig.setProviderName(SorcerConstants.ANY);
				sig.setType(Signature.Type.SRV);
				exertion.getControlContext().setAccessType(access);
			} else if ((Access.PUSH == access || Access.QOS_PUSH == access)
					&& !exertion.getProcessSignature().getServiceType()
							.isAssignableFrom(Jobber.class)) {
				if (sig.getServiceType().isAssignableFrom(Spacer.class)) {
					sig.setServiceType(Jobber.class);
					((NetSignature) sig).setSelector("service");
					sig.setProviderName(SorcerConstants.ANY);
					sig.setType(Signature.Type.SRV);
					exertion.getControlContext().setAccessType(access);
				}
			}
		} else {
			sig = new NetSignature("service", Jobber.class);
		}
		return sig;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	@Override
	public String toString() {
		return "ExertManager for: " + exertion.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Object call() throws Exception {
		return exertion.exert(transaction);
	}

}
