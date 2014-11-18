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

package sorcer.core.provider.exerter;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.Callable;

import net.jini.core.lookup.ServiceID;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;

import org.dancres.blitz.jini.lockmgr.LockResult;
import org.dancres.blitz.jini.lockmgr.MutualExclusion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.ControlContext;
import sorcer.core.context.ThrowableTrace;
import sorcer.core.deploy.ServiceDeployment;
import sorcer.core.dispatch.*;
import sorcer.core.provider.*;
import sorcer.core.SorcerConstants;
import sorcer.core.context.model.par.Par;
import sorcer.core.signature.NetSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.ext.Provisioner;
import sorcer.ext.ProvisioningException;
import sorcer.jini.lookup.ProviderID;
import sorcer.service.*;
import sorcer.service.Strategy.Access;
import sorcer.service.txmgr.TransactionManagerAccessor;
import sorcer.util.ProviderLookup;
import sorcer.util.Sorcer;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings("rawtypes")
public class ExertionDispatcher implements Exerter, Callable {
    protected final static Logger logger = LoggerFactory.getLogger(ExertionDispatcher.class);

    private ServiceExertion exertion;
    private Transaction transaction;
    private static MutualExclusion locker;

    public ExertionDispatcher() {
    }

    public ExertionDispatcher(Exertion xrt) {
        exertion = (ServiceExertion) xrt;
    }

    public ExertionDispatcher(Exertion xrt, Transaction txn) {
        exertion = (ServiceExertion) xrt;
        transaction = txn;

    }

    public Exertion exert(Arg... entries) throws TransactionException,
            ExertionException, RemoteException {
        return exert(null, (String) null, entries);
    }

    /* (non-Javadoc)
     * @see sorcer.core.provider.Exerter#exert(sorcer.service.Exertion, sorcer.service.Parameter[])
     */
    @Override
    public Exertion exert(Exertion xrt, Arg... entries)
            throws TransactionException, ExertionException, RemoteException {
        try {
            xrt.substitute(entries);
        } catch (Exception e) {
            throw new ExertionException(e);
        }
        return exert(xrt, null, (String) null);
    }

    /* (non-Javadoc)
     * @see sorcer.core.provider.Exerter#exert(sorcer.service.Exertion, net.jini.core.transaction.Transaction, sorcer.service.Parameter[])
     */
    @Override
    public Exertion exert(Exertion xrt, Transaction txn, Arg... entries)
            throws TransactionException, ExertionException, RemoteException {
        try {
            xrt.substitute(entries);
        } catch (Exception e) {
            throw new ExertionException(e);
        }
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

    public Exertion exert(Transaction txn, String providerName, Arg... entries)
            throws TransactionException, ExertionException, RemoteException {
        try {
            exertion.selectFidelity(entries);
            Exertion xrt = postProcessExertion(exert0(txn, providerName,
                    entries));
            if (exertion.isProxy()) {
                exertion.setContext(xrt.getDataContext());
                exertion.setControlContext((ControlContext)xrt.getControlContext());
                if (exertion.isCompound()) {
                    ((CompoundExertion) exertion).setExertions(((CompoundExertion) xrt)
                                    .getExertions());
                }

                return exertion;
            } else {
                return xrt;
            }
        } catch (ContextException e) {
            throw new ExertionException(e);
        }
    }



    /*public Exertion exert(Transaction txn, String providerName, Arg... entries)
            throws TransactionException, ExertionException, RemoteException {
        try {
            return postProcessExertion(exert0(txn, providerName, entries));
        } catch (ContextException e) {
            throw new ExertionException(e);
        }
    } */

	private void initExecState() {
		Exec.State state = exertion.getControlContext().getExecState();
		if (state == Exec.State.INITIAL) {
			for (Exertion e : exertion.getAllExertions()) {
				if (((ControlContext)e.getControlContext()).getExecState() == Exec.State.INITIAL) {
					((ServiceExertion)e).setStatus(Exec.INITIAL);
				}
			}
		}
	}

    private void realizeDependencies(Arg... entries) throws RemoteException,
            ExertionException {
        List<Invocation> dependers = exertion.getDependers();
        if (dependers != null && dependers.size() > 0) {
            for (Invocation<Object> depender : dependers) {
                try {
                    depender.invoke(exertion.getScope(), entries);
                } catch (InvocationException e) {
                    throw new ExertionException(e);
                }
            }
        }
    }
	
    public Exertion exert0(Transaction txn, String providerName, Arg... entries)
            throws TransactionException, ExertionException, RemoteException {
		initExecState();
        realizeDependencies(entries);
        try {
            if (entries != null && entries.length > 0) {
                exertion.substitute(entries);
            }
            // PROVISIONING for TASKS using Deployment
            // TODO PROVISIONING
			if (exertion.isTask() && exertion.isProvisionable()) {
				try {
					List<ServiceDeployment> deployments = exertion
							.getDeployments();
					if (deployments.size() > 0) {
						ProvisionManager ProvisionManager = new ProvisionManager(exertion);
						ProvisionManager.deployServices();
					}
				} catch (DispatcherException e) {
					throw new ExertionException(
							"Unable to deploy services for: "
									+ exertion.getName(), e);
				}
			}
            // This is disabled due to bugs with a job containing only one job
            /* if (exertion instanceof Job && ((Job) exertion).size() == 1) {
                return processAsTask();
            }*/
			transaction = txn;
			Context<?> cxt = exertion.getDataContext();
            cxt.setExertion(exertion);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ExertionException(ex);
		}
        Signature signature = exertion.getProcessSignature();
        Service provider = null;
        try {
            // If the exertion is a job rearrange the inner exertions to make sure the
            // dependencies are not broken
            if (exertion.isJob()) {
                ExertionSorter es = new ExertionSorter(exertion);
                exertion = (ServiceExertion)es.getSortedJob();
            }
            //
            if (!(signature instanceof NetSignature)) {
                if (exertion instanceof Task) {
                    if (exertion.getFidelity().size() == 1) {
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
				else if (exertion instanceof Job) {
					return ((Job) exertion).doJob(txn);
				} else if (exertion instanceof Block) {
					return ((Block) exertion).doBlock(txn);
				}
            }
            // check for missing signature of inconsistent PULL/PUSH cases
            signature = correctProcessSignature();
            if (!((ServiceSignature) signature).isSelectable()) {
                exertion.reportException(new ExertionException(
                        "No such operation in the requested signature: "
                                + signature));
                logger.warn("Not selectable exertion operation: ", signature);
                return exertion;
            }

            if (providerName != null && providerName.length() > 0) {
                signature.setProviderName(providerName);
            }
            logger.debug("Exertion shell's servicer accessor: {}",
                    Accessor.getAccessorType());
            provider = ((NetSignature) signature).getService();
        } catch (SignatureException e) {
            logger.warn("Error", e);
            throw new ExertionException(e);
        } catch (ContextException se) {
            throw new ExertionException(se);
        } catch (SortingException se) {
            throw new ExertionException(se);
        }

        if (provider == null) {
            // handle signatures for PULL tasks
            if (!exertion.isJob()
                    && exertion.getControlContext().getAccessType() == Access.PULL) {
                signature = new NetSignature("service", Spacer.class, Sorcer.getActualSpacerName());
                provider = (Service) Accessor.getService(signature);
            } else {
                provider = (Service) Accessor.getService(signature);
                if (provider == null && exertion.isProvisionable() && signature instanceof NetSignature) {
                    try {
                        logger.debug("Provisioning {}", signature);
                        provider = ServiceDirectoryProvisioner.getProvisioner().provision(signature.getServiceType().getName(),
                                signature.getName(), ((NetSignature) signature).getVersion());
                    } catch (ProvisioningException pe) {
                        logger.warn("Provider not available and not provisioned", pe);
                        exertion.setStatus(Exec.FAILED);
                        exertion.reportException(new RuntimeException(
                                "Cannot find provider and provisioning returned error: " + pe.getMessage()));
                        return exertion;
                    }
                }
            }
        }
        // Provider tasker = ProviderLookup.getProvider(exertion.getProcessSignature());
        // provider = ProviderAccessor.getProvider(null, signature
        // .getServiceType());
        if (provider == null) {
            logger.warn("Provider not available for: {}", signature);
            exertion.setStatus(Exec.FAILED);
            exertion.reportException(new RuntimeException(
                    "Cannot find provider for: " + signature));
            return exertion;
        }
        exertion.getControlContext().appendTrace(
                "bootstrapping: " + ((Provider) provider).getProviderName()
                        + ":" + ((Provider) provider).getProviderID());
        ((NetSignature) signature).setProvider(provider);
        logger.info("Provider found for: " + signature + "\n\t" + provider);
        if (((Provider) provider).mutualExclusion()) {
            return serviceMutualExclusion((Provider) provider, exertion,
                    transaction);
        } else {
            // test exertion for serialization
//			 try {
//				 logger.info("ServiceExerter.exert0(): going to serialize exertion for testing!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//				 ObjectLogger.persistMarshalled("exertionfile", exertion);
//			 } catch (Exception e) {
//				 e.printStackTrace();
//			 }
			Exertion result = provider.service(exertion, transaction);
            if (result != null && result.getExceptions().size() > 0) {
                for (ThrowableTrace et : result.getExceptions()) {
                    Throwable t = et.getThrowable();
                    logger.error("Got exception running {} : , {}", exertion.getName(), t.getMessage());
                    logger.debug("Exception details: {}", t);
                    if (t instanceof Error)
                        ((ServiceExertion) result).setStatus(Exec.ERROR);
                }
				((ServiceExertion)result).setStatus(Exec.FAILED);
			} else if (result == null) {
				exertion.reportException(new ExertionException("ServiceExerter failed calling: " 
						+ exertion.getProcessSignature()));
				exertion.setStatus(Exec.FAILED);
				result = exertion;
            }
            return result;
        }
    }

/*    private Exertion processAsTask() throws RemoteException,
            TransactionException, ExertionException {
        Exertion xrt = exertion.getExertions().get(0);
        xrt.exert();
        exertion.getExertions().set(0, xrt);
        exertion.setStatus(xrt.getStatus());
*//*

        Task task = (Task) exertion.getExertions().get(0);
        task = (Task) task.exert();
        exertion.getExertions().set(0, task);
        exertion.setStatus(task.getStatus());
*//*
        return exertion;
    }*/

    private Exertion serviceMutualExclusion(Provider provider,
                                            Exertion exertion, Transaction transaction) throws RemoteException,
            TransactionException, ExertionException {
        ServiceID mutexId = provider.getProviderID();
        if (locker == null) {
			locker = (MutualExclusion) ProviderLookup
					.getService(MutualExclusion.class);
		}
		TransactionManagerAccessor.getTransactionManager();
        Transaction txn = null;

        LockResult lr = locker.getLock(""
                + exertion.getProcessSignature().getServiceType(),
                new ProviderID(mutexId), txn,
                exertion.getId());
        if (lr.didSucceed()) {
            ((ControlContext)exertion.getControlContext()).setMutexId(provider.getProviderID());
            Exertion xrt = provider.service(exertion, transaction);
            txn.commit();
            return xrt;
        } else {
            // try continue to get lock, if failed abort the transaction txn
            txn.abort();
        }
        ((ControlContext)exertion.getControlContext()).addException(
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
            if ((access.equals(Access.PULL) || access.equals(Access.QOS_PULL))
                    && !exertion.getProcessSignature().getServiceType()
                    .isAssignableFrom(Spacer.class)) {
                sig.setServiceType(Spacer.class);
                ((NetSignature) sig).setSelector("service");
                sig.setProviderName(SorcerConstants.ANY);
                sig.setType(Signature.Type.SRV);
                exertion.getControlContext().setAccessType(access);
            } else if (( access.equals(Access.PUSH) || access.equals(Access.QOS_PUSH))
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

    public static Exertion postProcessExertion(Exertion exertion)
            throws ContextException, RemoteException {
        List<Exertion> exertions = exertion.getAllExertions();
        for (Exertion xrt : exertions) {
            List<Setter> ps = ((ServiceExertion) xrt).getPersisters();
            if (ps != null) {

				for (Setter p : ps) {
					if (p != null && (p instanceof Par) && ((Par) p).isMappable()) {
						String from = ((Par) p).getName();
						Object obj = null;
						if (xrt instanceof Job)
							obj = ((Job) xrt).getJobContext().getValue(from);
						else {
							obj = xrt.getContext().getValue(from);
						}
						
						if (obj != null)
							p.setValue(obj);
					}
				}
			}
		}
		return exertion;
	}

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public String toString() {
        if (exertion == null)
            return "ExertionShell";
        else
            return "ExertionShell for: " + exertion.getName();
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
