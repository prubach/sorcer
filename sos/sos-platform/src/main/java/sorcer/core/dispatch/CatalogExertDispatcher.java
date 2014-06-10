/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.core.dispatch;

import java.rmi.RemoteException;
import java.util.Set;

import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.TransactionException;
import sorcer.core.Dispatcher;
import sorcer.core.SorcerEnv;
import sorcer.core.provider.Concatenator;
import sorcer.core.provider.Provider;
import sorcer.core.exertion.NetTask;
import sorcer.core.provider.ServiceProvider;
import sorcer.core.signature.NetSignature;
import sorcer.ext.Provisioner;
import sorcer.ext.ProvisioningException;
import sorcer.service.*;

import static sorcer.service.Exec.*;

abstract public class CatalogExertDispatcher extends ExertDispatcher {
    public CatalogExertDispatcher(Exertion job,
                                  Set<Context> sharedContext,
                                  boolean isSpawned,
                                  Provider provider,
                                  ProvisionManager provisionManager,
                                  ProviderProvisionManager providerProvisionManager) {
        super(job, sharedContext, isSpawned, provider, provisionManager, providerProvisionManager);
    }

    protected Exertion execExertion(Exertion ex) throws SignatureException,
            ExertionException {
        beforeExec(ex);
        // set subject before task goes out.
        // ex.setSubject(subject);
        ServiceExertion result = null;
        try {
			if (ex.isTask()) {
				result = execTask((Task) ex);
			} else if (ex.isJob()) {
				result = execJob((Job) ex);
			} else if (ex.isBlock()) {
				result = execBlock((Block) ex);
			} else {
				logger.warn("Unknown ServiceExertion: {}", ex);
			}
            afterExec(ex, result);
		} catch (Exception e) {
            logger.warn("Error while executing exertion");
			// return original exertion with exception
			result = (ServiceExertion) ex;
            result.reportException(e);
			result.setStatus(FAILED);
			setState(Exec.FAILED);
			return result;
		}
		// set subject after result is received
		// result.setSubject(subject);
		return result;
    }

    protected void afterExec(Exertion ex, Exertion result)
            throws SignatureException, ExertionException, ContextException {
        afterExec(result);
        ServiceExertion ser = (ServiceExertion) result;
		((CompoundExertion)xrt).setExertionAt(result, result.getIndex());
//		((CompoundExertion)xrt).setExertionAt(result, ex.getIndex());
        if (ser.getStatus() > FAILED && ser.getStatus() != SUSPENDED) {
            ser.setStatus(DONE);
/*            if (xrt.getControlContext().isNodeReferencePreserved())
                try {
                    Jobs.preserveNodeReferences(ex, result);
                } catch (ContextException ce) {
                    ce.printStackTrace();
                    throw new ExertionException("ContextException caught: "
                            + ce.getMessage());
                }
*/
            // update all outputs from sharedcontext only for tasks. For jobs,
            // spawned dispatcher does it.
			try {
				if (result.isTask()) {
					collectOutputs(result);
				}
			} catch (ContextException e) {
				throw new ExertionException(e);
			}
        }
    }
    protected Task execTask(Task task) throws ExertionException,
            SignatureException, RemoteException {
        if (task instanceof NetTask) {
            return execServiceTask(task);
        } else {
            return task.doTask();
        }
    }

    protected Task execServiceTask(Task task) throws ExertionException {
        Task result = null;
        int maxTries = 3;
        int tried=0;
        try {
            if (((NetSignature) task.getProcessSignature())
                    .getService()!=null && ((NetSignature) task.getProcessSignature())
                    .getService().equals(provider)) {
                logger.debug("\n*** getting result from delegate of "
                        + provider.getProviderName() + "... ***\n");
                result = ((ServiceProvider) provider).getDelegate().doTask(
                        task, null);
                result.getControlContext().appendTrace(
                        "delegate of: " + this.provider.getProviderName()
                                + "=>" + this.getClass().getName());
            } else {
                NetSignature sig = (NetSignature) task.getProcessSignature();
                // Catalog lookup or use Lookup Service for the particular
                // service
                Service service = (Service) Accessor.getService(sig);
                if (service == null && task.isProvisionable()) {
                    Provisioner provisioner = Accessor.getService(Provisioner.class);
                    if (provisioner != null) {
                        try {
                            logger.info("Provisioning "+sig);
                            service = provisioner.provision(sig.getServiceType().getName(), sig.getProviderName(), sig.getVersion());
                        } catch (ProvisioningException pe) {
                            String msg = "Problem provisioning " + sig + " in task " + task.getName() + ": " +pe.getMessage();
                            logger.warn(msg, pe);
                            throw new ExertionException(msg, task);
                        } catch (RemoteException re) {
                            String msg = "Problem provisioning " + sig + " in task " + task.getName() + ": " +re.getMessage();
                            logger.warn(msg, re);
                            throw new ExertionException(msg, task);
                        }
                    }
                }
                if (service == null) {
                    String msg;
                    // get the PROCESS Method and grab provider name + interface
                    msg = "No Provider Available\n" + "Provider Name:      "
                            + sig.getProviderName() + "\n"
                            + "Provider Interface: " + sig.getServiceType();

                    logger.info(msg);
                    throw new ExertionException(msg, task);
                } else {
                    tried=0;
                    while (result==null && tried < maxTries) {
                        tried++;
                        try {
                            // setTaskProvider(task, provider.getProviderName());
                            task.setService(service);
                            // client security
                            /*
                             * ClientSubject cs = null;
                             * try{ // //cs =
                             * (ClientSubject)ServerContext.getServerContextElement
                             * (ClientSubject.class); }catch (Exception ex){
                             * Util.debug(this, ">>>No Subject in the server call");
                             * cs=null; } Subject client = null; if(cs!=null){
                             * client=cs.getClientSubject(); Util.debug(this,
                             * "Abhijit::>>>>> CS was not null"); if(client!=null){
                             * Util.debug(this,"Abhijit::>>>>> Client Subject was not
                             * null"+client); }else{ Util.debug(this,"Abhijit::>>>>>>
                             * CLIENT SUBJECT WAS
                             * NULL!!"); } }else{ Util.debug(this, "OOPS! NULL CS"); }
                             * if(client!=null&&task.getPrincipal()!=null){
                             * Util.debug(this,"Abhijit:: >>>>>--------------Inside
                             * Client!=null, PRINCIPAL != NULL, subject="+client);
                             * result = (RemoteServiceTask)provider.service(task);
                             * }else{ Util.debug(this,"Abhijit::
                             * >>>>>--------------Inside null Subject"); result =
                             * (RemoteServiceTask)provider.service(task); }
                             */
                            logger.debug("getting result from provider...");
                            result = (Task) service.service(task, null);

                        } catch (Exception re) {
                            if (tried >= maxTries) throw re;
                            else {
                                logger.warn("Problem exerting task, retrying " + tried + " time: " + xrt.getName() + " " + re.getMessage());
                                service = (Service) Accessor.getService(sig);
                                logger.warn("Got service: {}", service);
                            }
                        }
                    }
                    if (result!=null)
                        result.getControlContext().appendTrace(
                                   ((Provider)service).getProviderName() + " dispatcher: "
                                            + getClass().getName());
                }
            }
            logger.debug("got result: {}", result);
        //}
        //catch (ExertionException ee) {
        //    task.reportException(ee);
        //    throw ee;
        } catch (Exception re) {
            task.reportException(re);
            throw new ExertionException("Dispatcher failed for task, tried: " + tried + " : "
                    + xrt.getName(), re);
        }
        return result;
    }

    protected Job execJob(Job job)
            throws DispatcherException, InterruptedException,
            RemoteException {

            runningExertionIDs.add(job.getId());

            // create a new instance of a dispatcher
            Dispatcher dispatcher = ExertionDispatcherFactory.getFactory()
                    .createDispatcher(job, sharedContexts, true, provider);
        dispatcher.exec();
            // wait until serviceJob is done by dispatcher
        Job out = (Job) dispatcher.getResult().exertion;
            out.getControlContext().appendTrace(provider.getProviderName()
                    + " dispatcher: " + getClass().getName());
            return out;
    }

	private Block execBlock(Block block)
			throws DispatcherException, InterruptedException,
			ExertionException, RemoteException {

		try {
			ServiceTemplate st = Accessor.getServiceTemplate(null,
					null, new Class[] { Concatenator.class }, null);
			ServiceItem[] concatenators = Accessor.getServiceItems(st, null,
					SorcerEnv.getLookupGroups());
			/*
			 * check if there is any available concatenator in the network and
			 * delegate the inner block to the available Concatenator. In the future, a
			 * efficient load balancing algorithm should be implemented for
			 * dispatching inner jobs. Currently, it only does round robin.
			 */
			for (int i = 0; i < concatenators.length; i++) {
				if (concatenators[i] != null) {
					if (!provider.getProviderID().equals(
							concatenators[i].serviceID)) {
						logger.trace("Concatenator: " + i + " ServiceID: "
								+ concatenators[i].serviceID);
						Provider rconcatenator = (Provider) concatenators[i].service;

						return (Block) rconcatenator.service(block, null);
					}
				}
			}

			/*
			 * Create a new dispatcher thread for the inner job, if no available
			 * Jobber is found in the network
			 */
			Dispatcher dispatcher;
			runningExertionIDs.add(block.getId());

			// create a new instance of a dispatcher
			dispatcher = ExertionDispatcherFactory.getFactory()
					.createDispatcher(block, sharedContexts, true, provider);
            dispatcher.exec();
			// wait until serviceJob is done by dispatcher
			Block out = (Block) dispatcher.getResult().exertion;
			out.getControlContext().appendTrace(provider.getProviderName() 
					+ " dispatcher: " + getClass().getName());
			return out;
		} catch (RemoteException re) {
			re.printStackTrace();
			throw re;
		} catch (ExertionException ee) {
			ee.printStackTrace();
			throw ee;
		} catch (DispatcherException de) {
			de.printStackTrace();
			throw de;
		} catch (TransactionException te) {
			te.printStackTrace();
			throw new ExertionException("transaction failure", te);
		}
	}
}
