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

package sorcer.util.dbas;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import net.jini.core.lookup.ServiceID;
import net.jini.core.transaction.TransactionException;
import sorcer.core.SorcerConstants;
import sorcer.core.exertion.NetJob;
import sorcer.core.provider.jobber.ExertionJobber;
import sorcer.security.util.Auth;
import sorcer.security.util.SorcerPrincipal;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Job;
import sorcer.service.Jobber;
import sorcer.service.ServiceExertion;
import sorcer.service.SignatureException;
import sorcer.util.AccessorException;
import sorcer.util.Command;
import sorcer.util.Invoker;
import sorcer.util.Mandate;
import sorcer.util.Mandator;
import sorcer.util.ProviderAccessor;
import sorcer.util.Sorcer;

public class ServiceCmd implements Command, SorcerConstants {
	private static Logger logger = Logger.getLogger(ServiceCmd.class.getName());
	private String cmdName;
	private SorcerProtocolStatement fps;
	private Object[] args;

	private ServiceExertion inputEx;
	private Exertion resultEx;

	private Mandate resultMandate;

	private static ExertionJobber servletJobber;

	private static Mandator persister;

	public ServiceCmd(String cmdName) {
		this.cmdName = cmdName;
		if (servletJobber == null)
			try {
				servletJobber = new ExertionJobber();
			} catch (RemoteException re) {
				re.printStackTrace();
			}

		String location = Sorcer.getRmiUrl();
		String name;
	}

	public ServiceCmd(String cmdName, Object[] args) {
		this(cmdName);
		this.args = args;
	}

	public void setArgs(Object target, Object[] args) {
		fps = (SorcerProtocolStatement) target;
		this.args = args;
	}

	private void initialize() {
		if (args == null || args.length == 0)
			return;
		else if (args[0] instanceof ServiceExertion)
			inputEx = (ServiceExertion) args[0];
		else if (args[0] instanceof Mandate) {
			Object margs[] = ((Mandate) args[0]).getArgs();
			SorcerPrincipal principal = ((Mandate) args[0]).getPrincipal();
			if (margs[0] instanceof Exertion)
				try {
					inputEx = ((ServiceExertion) margs[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			else
				try {
					inputEx = getJob((String) margs[0], principal);
				} catch (Exception e) {
					e.printStackTrace();
				}
		} else if (args.length >= 2)
			try {
				inputEx = getJob((String) args[0], (SorcerPrincipal) args[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public void doIt() {
		initialize();
		resultMandate = new Mandate(Integer.parseInt(cmdName));
		logger.info("doIt:cmdName: " + cmdName);
		try {
			switch (Integer.parseInt(cmdName)) {
			case DO_TASK:
				doTask();
				break;
			case DO_JOB:
				doJob();
				break;
			case SERVICE_EXERTION:
				service();
				break;
			case STOP_JOB:
				stopJob();
				break;
			case SUSPEND_JOB:
				suspendJob();
				break;
			case RESUME_JOB:
				resumeJob();
				break;
			case STEP_JOB:
				stepJob();
				break;

			default:
				fps.answer = "ERROR:Invalid cmd: " + cmdName;
			}
		} catch (ExertionException e) {
			// fps.answer = "ERROR:" + e.getMessage();
			resultMandate.getResult().addElement(
					"ERROR:ExertionException: " + e.getMessage());
		} catch (RemoteException e) {
			e.printStackTrace();
			// fps.answer = "ERROR:" + e.getMessage();
			resultMandate.getResult().addElement(
					"ERROR:RemoteException: " + e.getMessage());
		} catch (SignatureException tme) {
			// fps.answer = "ERROR:" + tme.getMessage();
			resultMandate.getResult().addElement(
					"ERROR:ExertionMethodException: " + tme.getMessage());
		}
		if (resultEx != null)
			resultMandate.getResult().addElement(resultEx);
		else
			resultMandate.getResult().addElement(
					"ERROR: Jobber returned Null Job after Processing!");
	}

	private void doTask() throws RemoteException, ExertionException {
		if (inputEx != null && ((ServiceExertion) inputEx).isTask())
			try {
				resultEx = getJobberByName().service(inputEx, null);
			} catch (TransactionException te) {
				throw new ExertionException("transaction failure", te);
			}
	}

	public void doJob() throws RemoteException, ExertionException {
		if (inputEx != null && ((ServiceExertion) inputEx).isJob())
			try {
				resultEx = getJobberByName().service(inputEx, null);
			} catch (TransactionException te) {
				throw new ExertionException("transaction failure", te);
			}
	}

	public void service() throws RemoteException, ExertionException {
		if (inputEx != null) {
			if (((ServiceExertion) inputEx).isJob())
				doJob();
			else if (((ServiceExertion) inputEx).isTask())
				doTask();
		}
	}

	public void stopJob() throws RemoteException, ExertionException,
			SignatureException {
		// Jobber jobber = getJobberByID();

		// if (inputEx!=null && inputEx.isJob() && jobber!=null)
		// resultEx = jobber.stopJob(inputEx.getID(),
		// ((RemoteServiceJob)inputEx).subject);
		// else
		// throw new ExertionException("Job currently not running!");
	}

	public void suspendJob() throws RemoteException, ExertionException,
			SignatureException {
		// Util.debug(this,"______________SUSPEND JOB_________________");
		// Jobber jobber = null;
		// jobber = getJobberByID();
		// if (jobber==null)
		// jobber = getJobberByName();
		// if (inputEx!=null && inputEx.isJob() && jobber != null)
		// resultEx = jobber.suspendJob(inputEx.getID(),
		// ((RemoteServiceJob)inputEx).subject);
		// else
		// throw new ExertionException("Job currently not running!");
	}

	public void stepJob() throws RemoteException, ExertionException,
			SignatureException {
		// Jobber jobber = getJobberByName();
		// if (inputEx!=null && inputEx.isJob() && jobber!=null )
		// resultEx = getJobberByName().stepJob(inputEx.getID(),
		// ((RemoteServiceJob)inputEx).subject);
		// else
		// throw new ExertionException("No jobber Available to execute Job!");
	}

	public void resumeJob() throws RemoteException, ExertionException,
			SignatureException {
		// Jobber jobber = getJobberByName();
		// if (inputEx!=null && inputEx.isJob() && jobber!=null)
		// resultEx = getJobberByName().resumeJob(inputEx.getID(),
		// ((RemoteServiceJob)inputEx).subject);
		// else
		// throw new ExertionException("No jobber Available to execute Job!");
	}

	public Mandate getResult() {
		resultMandate = new Mandate(Integer.parseInt(cmdName));
		resultMandate.getResult().addElement(resultEx);
		return resultMandate;
	}

	public void setInvoker(Invoker invoker) {
		fps = (SorcerProtocolStatement) invoker;
	}

	public Invoker getInvoker() {
		return fps;
	}

	public void undoIt() {
		// do nothing
	}

	private Jobber getJobberByID() {
		// If task, return the serlvletJobber which is a Jobber but Peer.
		if (((ServiceExertion) inputEx).isTask())
			return servletJobber;
		Jobber jobber = null;
		ServiceID sid = ((NetJob) inputEx).getServiceID();
		if (sid != null) {
			jobber = (Jobber) ProviderAccessor.getProvider(sid);
		}

		return jobber;
	}

	private Jobber getJobberByName() {
		// If task, return the serlvletJobber which is a Jobber but Peer.
		if (inputEx.isTask())
			return servletJobber;
		Jobber jobber;
		String jobberName = inputEx.getRendezvousName();
		if (jobberName != null && !jobberName.trim().equals("")) {
			jobber = ProviderAccessor.getJobber(jobberName.trim());
			inputEx.reportException(new ExertionException(
                    "Jobber " + jobberName + "not available!"));
			try {
				// inputEx.setStatus(FAILED);
				updateDB(inputEx, UPDATE_EXERTION);
			} catch (Exception e) {
				e.printStackTrace();
				// cannot do anything as updating exertion Failed.
			}
		} else
			jobber = servletJobber;

		return jobber;
	}

	private NetJob getJob(String jobID, SorcerPrincipal principal)
			throws RemoteException, ExertionException, SignatureException {
		Mandate mandate = new Mandate(GET_RUNTIME_JOB, principal);
		Serializable[] data = { jobID };
		mandate.setArgs(data);
		NetJob job = (NetJob) persister.execMandate(mandate)
				.getResult().elementAt(0);
		job.setSubject(Auth.createSubject(principal));
		return job;
	}

	private Exertion updateDB(Exertion exertion, int cmdName)
			throws ExertionException, SignatureException {
		// Mandator persister = null;
		// try {
		// // Now get the remote cache object
		// String location = Env.getRMILocation();
		// String name = location + Env.getProperty("sorcer.cacheServer");
		// //Util.debug(this, "persister name=" + name);
		// persister = (Mandator)Naming.lookup(name);
		// } catch (Exception e) {
		// e.printStackTrace();
		// return exertion;
		// }
		// if (exertion==null) return exertion;
		// Exertion result = null;
		// RemoteExertion remoteResult = null;

		// String sessionID =
		// (exertion.isJob())?((ServiceJob)exertion).exertionAt(0).getSessionID()
		// :exertion.getSessionID();

		// Mandate mandate = new Mandate(cmdName,exertion.getPrincipal());
		// Serializable[] data = { exertion };
		// mandate.setArgs(data);
		// try {
		// Util.debug(this,"Calling persister cmdName:"+cmdName+" exertion.ID="+exertion.getID()
		// +"Exertion ="+exertion);
		// mandate = persister.execMandate(mandate);
		// if (mandate.getResult().size()==0)
		// return exertion;
		// if (!(mandate.getResult().elementAt(0) instanceof Exertion) ||
		// (mandate.getResult().getStatus()== TRANSACTION_ERROR))
		// throw new
		// ExertionException("Could not save exertion id="+exertion.getID());
		// result = (Exertion)mandate.getResult().elementAt(0);
		// //Replace old jobid by new one in dispatcher.
		// remoteResult =
		// (result.isJob())?(RemoteExertion)RemoteServiceJob.getRemoteJob((ServiceJob)result)
		// :(RemoteExertion)RemoteServiceTask.getRemoteTask((ServiceTask)result);
		// }
		// catch (RemoteException re) {
		// re.printStackTrace();
		// throw new ExertionException("Failed to store a runtime exertion id="
		// + exertion.getID());
		// }
		// remoteResult.setSessionID(sessionID);
		// return remoteResult;
		return null;
	}

}
