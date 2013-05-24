/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
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

import com.sun.jini.thread.TaskManager;
import net.jini.core.transaction.TransactionException;
import sorcer.core.SorcerConstants;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetJob;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.falcon.base.Conditional;
import sorcer.falcon.core.exertion.IfExertion;
import sorcer.falcon.core.exertion.WhileExertion;
import sorcer.service.*;
import sorcer.service.Strategy.Access;
import sorcer.util.AccessorException;
import sorcer.util.ProviderAccessor;

import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;

import static sorcer.eo.operator.task;

/**
 * @author Mike Sobolewski
 */

/**
 * The ControlFlowManager class is responsible for handling control floe of conditional exertions
 * ({@link Conditional}, {@link IfExertion}, {@link WhileExertion}).
 * 
 * This class is used by the {@link ServiceProvider} class and
 * {@link ProviderDelegate} for executing {@link Exertion}.
 */
public class ControlFlowManager {

	/**
	 * Logger for this ExerterController logging.
	 */
	protected static final Logger logger = Logger
			.getLogger(ControlFlowManager.class.getName());

	/**
	 * ExertionDelegate reference needed for handling exertions.
	 */
	protected ProviderDelegate delegate;

	/**
	 * The Exertion that is going to be executed.
	 */
	protected Exertion exertion;

	/**
	 * Reference to a jobber proxy if available.
	 */
	protected Jobber jobber;

	/**
	 * Reference to a spacer proxy if available.
	 */
	protected Spacer spacer;

	static int WAIT_INCREMENT = 50;

	/**
	 * Default Constructor.
	 */
	public ControlFlowManager() {
		// do nothng
	}

	/**
	 * Overloaded constructor which takes in an Exertion and an ExerterDelegate.
	 * 
	 * @param exertion
	 *            Exertion
	 * @param delegate
	 *            ExerterDelegate
	 */
	public ControlFlowManager(Exertion exertion, ProviderDelegate delegate) {
		this.delegate = delegate;
		this.exertion = exertion;
	}

	/**
	 * Overloaded constructor which takes in an Exertion, ExerterDelegate, and
	 * Jobber. This constructor is used when handling {@link Job}.
	 * 
	 * @param exertion
	 *            Exertion
	 * @param delegate
	 *            ExerterDelegate
	 * @param jobber
	 *            Jobber
	 */
	public ControlFlowManager(Exertion exertion, ProviderDelegate delegate,
                              Jobber jobber) {
		this(exertion, delegate);
		this.jobber = jobber;
	}

	/**
	 * Overloaded constructor which takes in an Exertion, ExerterDelegate, and
	 * Spacer. This constructor is used when handling {@link Job}.
	 * 
	 * @param exertion
	 *            Exertion
	 * @param delegate
	 *            ExerterDelegate
	 * @param spacer
	 *            Spacer
	 */
	public ControlFlowManager(Exertion exertion, ProviderDelegate delegate,
                              Spacer spacer) {
		this(exertion, delegate);
		this.jobber = null;
		this.spacer = spacer;
	}
	/**
	 * Process the Exertion accordingly if it is a job, task, or a Conditional
	 * Exertion.
	 * 
	 * @return Exertion the result
	 * @see NetJob
	 * @see NetTask
	 * @see Conditional
	 * @throws RemoteException
	 *             exception from other methods
	 * @throws ExertionException
	 *             exception from other methods
	 */
	public Exertion process(TaskManager exertionManager) throws ExertionException {
		logger.info("********************************************* process exertion: " + exertion.getName());
		Exertion result = null;
		if (exertionManager == null) {
			logger.info("********************************************* exertionManager is NULL");

			try {
				if (exertion instanceof Conditional) {
					logger.info("********************************************* exertion Conditional");
					result = doConditional(exertion);
					logger.info("********************************************* exertion Conditional; result: " + result);
                } else if (exertion.isJob()) {
					logger.info("********************************************* exertion isJob()");
					result = doRendezvousExertion((Job) exertion);
					logger.info("********************************************* exertion isJob(); result: " + result);
                } else if (exertion.isTask()) {
					logger.info("********************************************* exertion isTask()");
					result = doTask((Task) exertion);
					logger.info("********************************************* exertion isTask(); result: " + result);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ExertionException(e.getMessage(), e);
			}
			return result;
		} else {
			logger.info("********************************************* exertionManager is *NOT* null");
			ExertionRunnable ethread = new ExertionRunnable(exertion);
			exertionManager.add(ethread);
			while (!ethread.stopped && ethread.result == null) {
				try {
					Thread.sleep(WAIT_INCREMENT);
				} catch (InterruptedException e) {
					e.printStackTrace();
					((ServiceExertion)exertion).setStatus(ExecState.FAILED);
					((ServiceExertion)exertion).reportException(e);
					return exertion;
				}
			}
			return ethread.result;
		}
	}

	/**
	 * This method delegates the doTask method to the ExertionDelegate.
	 * 
	 * @param task
	 *            ServiceTask
	 * @return ServiceTask
	 * @throws RemoteException
	 *             exception from ExertionDelegate
	 * @throws ExertionException
	 *             exception from ExertionDelegate
	 * @throws SignatureException
	 *             exception from ExertionDelegate
	 * @throws TransactionException 
	 * @throws ContextException 
	 */
	public Task doTask(Task task) throws RemoteException, ExertionException,
			SignatureException, TransactionException, ContextException {
		if (task.getControlContext().getAccessType() == Access.PULL) {
			return (Task)doRendezvousExertion(task);
		} else if (delegate != null) {
			return delegate.doTask(task, null);
		}
		else {
			return doIntraTask(task);
		}
	}
	
	/**
	 * Selects a Jobber or Spacer for exertion processing. If own Jobber or
	 * Spacer is not available then fetches one and forwards the exertion for
	 * processing.
	 * 
	 * @param xrt
	 * 			the exertion to be processed
	 * @return
	 * @throws RemoteException
	 * @throws ExertionException
	 */
	public Exertion doRendezvousExertion(ServiceExertion xrt) throws RemoteException, ExertionException {
		try {
			if (xrt.isSpacable()) {
				logger.info("********************************************* exertion isSpacable");

				if (spacer == null) {
					String spacerName = xrt.getRendezvousName();
					Spacer spacerService = null;
					try {
						if (spacerName != null) {
							spacerService = ProviderAccessor.getSpacer(spacerName);
						}
						else {
							spacerService = ProviderAccessor.getSpacer();
						}
						logger.info("Got Spacer: " + spacerService);
						return spacerService.service(xrt, null);
					} catch (AccessorException ae) {
						ae.printStackTrace();
						throw new ExertionException("Could not find Spacer: "
								+ spacerName);
					}
				}
				Exertion job = ((Executor)spacer).execute(xrt, null);
				logger.info("********************************************* spacable exerted = " + job);
				return job;
			}
			else {
				logger.info("********************************************* exertion NOT Spacable");
				if (jobber == null) {
					// return delegate.doJob(job);
					String jobberName = xrt.getRendezvousName();
					Jobber jobberService = null;
					try {
						if (jobberName != null)
							jobberService = ProviderAccessor.getJobber(jobberName);
						else
							jobberService = ProviderAccessor.getJobber();
						logger.info("Got Jobber: " + jobber);
						return jobberService.service(xrt, null);
					} catch (AccessorException ae) {
						ae.printStackTrace();
						throw new ExertionException("Could not find Jobber: "
								+ jobberName);
					}
				}
				Exertion job = ((Executor)jobber).execute(xrt, null);
				logger.info("********************************************* job exerted = " + job);

				return job;
			}
		} catch (TransactionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * This method handles the {@link Conditional} Exertions. It determines if
	 * the Conditional Exertion is a WhileExertion or a IfExertion.
	 * 
	 * @param exertion
	 *            Conditional type Exertion
	 * @return Exertion
	 * @see WhileExertion
	 * @see IfExertion
	 */
	public Exertion doConditional(Exertion exertion) {
		Exertion result = null;

		if (exertion.getClass().getName().equals(WhileExertion.class.getName())) {
			result = doWhileExertion(exertion);
		}

		else if (exertion.getClass().getName().equals(
				IfExertion.class.getName())) {
			result = doIfExertion(exertion);
		}

		return result;
	}

	/**
	 * DoWhileExertion handles the execution of an WhileExertion. Checks if the
	 * inner Exertion is another type of WhileExertion, IfExertion, ServiceJob,
	 * or a ServiceTask.
	 * 
	 * @param exertion
	 *            The WhileExertion container
	 * @return Exertion the result is also a WhileExertion
	 */
	protected Exertion doWhileExertion(Exertion exertion) {
		Exertion inner = ((WhileExertion) exertion).getDoExertion();
		Exertion tmp = null;
		Map<String, Object> whileState = new HashMap<String, Object>();
		Map<String, Object> ifState = new HashMap<String, Object>();
		long firstWhileIteration = 0;
		long firstIfIteration = 0;

		if (inner instanceof WhileExertion) {
			try {
				while (((WhileExertion) exertion).isTrue()) {
					saveState(whileState, inner.getDataContext());

					if (firstWhileIteration > 0) {
						((WhileExertion) exertion).adjustConditionVariables();
					}

					tmp = doWhileExertion((WhileExertion) inner);
					((WhileExertion) exertion).setDoExertion(tmp);
					firstWhileIteration++;
				}

				restoreState(whileState, inner.getDataContext());
				return exertion;
			} catch (Exception e) {
				logger
						.info("problem with doWhileExertion, inner exertion is a WhileExertion. "
								+ e);
				e.printStackTrace();
				return exertion;
			}
		}

		else if (inner instanceof IfExertion) {
			try {
				while (((WhileExertion) exertion).isTrue()) {
					saveState(ifState, inner.getDataContext());

					if (firstIfIteration > 0) {
						((WhileExertion) exertion).adjustConditionVariables();
					}

					tmp = doIfExertion(inner);
					((WhileExertion) exertion).setDoExertion(tmp);
					firstIfIteration++;
				}

				restoreState(ifState, inner.getDataContext());
				return exertion;
			} catch (Exception e) {
				logger
						.info("problem with doWhileExertion, inner exertion is an IfExertion. "
								+ e);
				e.printStackTrace();
				return exertion;
			}
		}

		else if (inner instanceof Task)
			return doWhileTask(exertion);

		else if (inner instanceof Job)
			return doWhileJob(exertion);

		else
			return exertion;
	}

	/**
	 * This method handles the elemental exertion as it's base exertion, a
	 * ServiceTask. It will loop the base Exertion untill the specified
	 * condition is satisfied.
	 * 
	 * @param exertion
	 *            a WhileExertion where the inner exertion is a task
	 * @return Exertion type of WhileExertion
	 */
	protected Exertion doWhileTask(Exertion exertion) {
		Task task = (Task) ((WhileExertion) exertion).getDoExertion();
		Map<String, Object> state = new HashMap<String, Object>();
		long firstIteration = 0;

		try {
			if (isValidExertion(task)) {
				while (((WhileExertion) exertion).isTrue()) {
					saveState(state, task.getDataContext());

					if (firstIteration > 0) {
						((WhileExertion) exertion).adjustConditionVariables();
					}

					task = this.doTask(task);
					((WhileExertion) exertion).setDoExertion(task);
					firstIteration++;
				}
				restoreState(state, task.getDataContext());
				return exertion;
			} else {
				logger
						.info("Task is not valid for this provider, forwarding task.");
				Signature method = exertion.getProcessSignature();
				Class providerType = ((NetSignature) method)
						.getServiceType();
				String codebase = ((NetSignature) method).getCodebase();
                Service provider = ProviderAccessor.getProvider(null,
						providerType, codebase);
				return provider.service(exertion, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Execute the job in loop using the WhileExertion, until the specified
	 * condition is satisfied.
	 * 
	 * @param exertion
	 *            Exertion
	 * @return Exertion
	 */
	protected Exertion doWhileJob(Exertion exertion) {
		Map<String, Object> state = new HashMap<String, Object>();
		Job temp = null;
		long jobIteration = 0;

		try {
			if (isValidExertion(((WhileExertion) exertion).getDoExertion())) {
				// initialize
				resetExertionStatus(((WhileExertion) exertion).getDoExertion());

				// order of the statements is the loop is very important
				while (((WhileExertion) exertion).isTrue()) {
					saveState(state, exertion.getDataContext());

					if (jobIteration > 0) {
						((WhileExertion) exertion).adjustConditionVariables();
						resetExertionStatus(((WhileExertion) exertion)
								.getDoExertion());
					}

					temp = (Job)this.doRendezvousExertion((Job) ((WhileExertion) exertion)
							.getDoExertion());
					((WhileExertion) exertion).setDoExertion(temp);
					jobIteration++;
				}
				// restore the (i-1) result, which is the correct answer
				restoreState(state, exertion.getDataContext());
				return exertion;

			} else {
				logger.info("Forwarding Job to SORCER-Jobber.");
				Signature method = exertion.getProcessSignature();
				Class providerType = ((NetSignature) method)
						.getServiceType();
				String codebase = ((NetSignature) method).getCodebase();
				Service provider = ProviderAccessor.getProvider(null,
						providerType, codebase);
				return provider.service(exertion, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * The doIfExertion handles the IfExertion. Checkes the condition and
	 * determines based on the isTrue() method wether to call on the
	 * thenExertion component or elseExertion component.
	 * 
	 * @param exertion
	 *            The WhileExertion container
	 * @return Exertion
	 */
	protected Exertion doIfExertion(Exertion exertion) {
		Exertion innerIf = ((IfExertion) exertion).getThenExertion();
		Exertion innerElse = ((IfExertion) exertion).getElseExertion();

		if (innerIf instanceof WhileExertion) {
			try {
				if (((IfExertion) exertion).isTrue()) {
					this.doWhileExertion(((IfExertion) exertion)
							.getThenExertion());
					exertion.getDataContext().putValue("exertion/done",
							((IfExertion) exertion).getThenExertion());
					copyContext(((IfExertion) exertion).getThenExertion()
							.getDataContext(), exertion.getDataContext());
				} else {
					if (innerElse instanceof WhileExertion) {
						Exertion remoteResult = this
								.doWhileExertion(((IfExertion) exertion)
										.getElseExertion());
						((IfExertion) exertion).setElseExertion(remoteResult);
						exertion.getDataContext().putValue("exertion/done",
								((IfExertion) exertion).getElseExertion());
						copyContext(((IfExertion) exertion).getElseExertion()
								.getDataContext(), exertion.getDataContext());
					} else if (innerElse instanceof IfExertion) {
						Exertion remoteResult = this
								.doIfExertion(((IfExertion) exertion)
										.getElseExertion());
						((IfExertion) exertion).setElseExertion(remoteResult);
						exertion.getDataContext().putValue("exertion/done",
								((IfExertion) exertion).getElseExertion());
						copyContext(((IfExertion) exertion).getElseExertion()
								.getDataContext(), exertion.getDataContext());
					} else if (innerElse instanceof Task) {
						Exertion remoteResult = this.doIfTask(exertion);
						((IfExertion) exertion)
								.setElseExertion(((IfExertion) remoteResult)
										.getElseExertion());
					} else if (innerElse instanceof Job) {
						Exertion remoteResult = this.doIfJob(exertion);
						((IfExertion) exertion)
								.setElseExertion(((IfExertion) remoteResult)
										.getElseExertion());
					}
				}
				return exertion;
			} catch (Exception e) {
				logger.severe("exception in doIfExertion: " + e);
				e.printStackTrace();
				return null;
			}
		}
		else if (innerIf instanceof IfExertion) {
			try {
				if (((IfExertion) exertion).isTrue()) {
					this
							.doIfExertion(((IfExertion) exertion)
									.getThenExertion());
					exertion.getDataContext().putValue("exertion/done",
							((IfExertion) exertion).getThenExertion());
					copyContext(((IfExertion) exertion).getThenExertion()
							.getDataContext(), exertion.getDataContext());
				} else {
					if (innerElse instanceof WhileExertion) {
						Exertion remoteResult = this
								.doWhileExertion(((IfExertion) exertion)
										.getElseExertion());
						((IfExertion) exertion).setElseExertion(remoteResult);
						exertion.getDataContext().putValue("exertion/done",
								((IfExertion) exertion).getElseExertion());
						copyContext(((IfExertion) exertion).getElseExertion()
								.getDataContext(), exertion.getDataContext());
					} else if (innerElse instanceof IfExertion) {
						Exertion remoteResult = this
								.doIfExertion(((IfExertion) exertion)
										.getElseExertion());
						((IfExertion) exertion).setElseExertion(remoteResult);
						exertion.getDataContext().putValue("exertion/done",
								((IfExertion) exertion).getElseExertion());
						copyContext(((IfExertion) exertion).getElseExertion()
								.getDataContext(), exertion.getDataContext());
					} else if (innerElse instanceof Task) {
						Exertion remoteResult = this.doIfTask(exertion);
						((IfExertion) exertion)
								.setElseExertion(((IfExertion) remoteResult)
										.getElseExertion());
					} else if (innerElse instanceof Job) {
						Exertion remoteResult = this.doIfJob(exertion);
						((IfExertion) exertion)
								.setElseExertion(((IfExertion) remoteResult)
										.getElseExertion());
					}
				}
				return exertion;
			} catch (Exception e) {
				logger.severe("exception in doIfExertion: " + e);
				e.printStackTrace();
				return null;
			}
		}

		else if (innerIf instanceof Task) {
			Exertion remoteResult = this.doIfTask(exertion);
			return remoteResult;
		}

		else if (innerIf instanceof Job) {
			Exertion remoteResult = this.doIfJob(exertion);
			return remoteResult;
		}

		else {
			return exertion;
		}
	}

	/**
	 * The doIfTask method handles the ServiceTask component.
	 * 
	 * @param exertion
	 *            The IfExertion container
	 * @return Exertion
	 */
	protected Exertion doIfTask(Exertion exertion) {
		try {
			if (isValidExertion(((IfExertion) exertion).getThenExertion())) {
				if (((IfExertion) exertion).isTrue()) {
					this.doTask((Task) ((IfExertion) exertion)
							.getThenExertion());
					exertion.getDataContext().putValue("exertion/done",
							((IfExertion) exertion).getThenExertion());
					copyContext(((IfExertion) exertion).getThenExertion()
							.getDataContext(), exertion.getDataContext());
				} else {
					this.doTask((Task) ((IfExertion) exertion)
							.getElseExertion());
					exertion.getDataContext().putValue("exertion/done",
							((IfExertion) exertion).getElseExertion());
					copyContext(((IfExertion) exertion).getElseExertion()
							.getDataContext(), exertion.getDataContext());
				}
				return exertion;
			} else {
				Signature method = exertion.getProcessSignature();
				Class providerType = ((NetSignature) method)
						.getServiceType();
				String codebase = ((NetSignature) method).getCodebase();
				Service provider = ProviderAccessor.getProvider(null,
						providerType, codebase);

				return provider.service(exertion, null);
			}
		} catch (Exception e) {
			logger.severe("exception in doWhileExertion: " + e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * The doIfJob handles the ServiceJob component.
	 * 
	 * @param exertion
	 *            The IfExertion container
	 * @return Exertion
	 */
	protected Exertion doIfJob(Exertion exertion) {
		try {
			if (isValidExertion(((IfExertion) exertion).getThenExertion())) {
				if (((IfExertion) exertion).isTrue()) {
					this.doRendezvousExertion((Job) ((IfExertion) exertion).getThenExertion());
					exertion.getDataContext().putValue("exertion/done",
							((IfExertion) exertion).getThenExertion());
					copyContext(((IfExertion) exertion).getThenExertion()
							.getDataContext(), exertion.getDataContext());
				} else {
					this.doRendezvousExertion((Job) ((IfExertion) exertion).getElseExertion());
					exertion.getDataContext().putValue("exertion/done",
							((IfExertion) exertion).getElseExertion());
					copyContext(((IfExertion) exertion).getElseExertion()
							.getDataContext(), exertion.getDataContext());
				}
				return exertion;
			} else {
				Signature method = exertion.getProcessSignature();
				Class providerType = ((NetSignature) method)
						.getServiceType();
				String codebase = ((NetSignature) method).getCodebase();
				Service provider = ProviderAccessor.getProvider(null,
						providerType, codebase);
				return provider.service(exertion, null);
			}
		} catch (Exception e) {
			logger.severe("exception in doWhileExertion: " + e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This mehtod saves all the data nodes of a dataContext and put it on a Map.
	 * 
	 * @param mapBackUp
	 *            HashMap where the ServiceContext data nodes are saved
	 * @param context
	 *            ServiceContext to be saved into the HashMap
	 */
	public static void saveState(Map<String, Object> mapBackUp, Context context) {
		try {
			Enumeration e = context.contextPaths();
			String path = null;

			while (e.hasMoreElements()) {
				path = new String((String) e.nextElement());
				mapBackUp.put(path, ((ServiceContext) context).get(path));
			}
		} catch (ContextException ce) {
			logger.info("problem saving state of the ServiceContext " + ce);
			ce.printStackTrace();
		}
	}

	/**
	 * Copies the backup map of the dataContext to the passed dataContext.
	 * 
	 * @param mapBackUp
	 *            Saved HashMap which is used to restore from
	 * @param context
	 *            ServiceContext that gets restored from the saved HashMap
	 */
	public static void restoreState(Map<String, Object> mapBackUp,
			Context context) {
		Iterator iter = mapBackUp.entrySet().iterator();

		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String path = (String) entry.getKey();
			Object value = (Object) entry.getValue();

			try {
				context.putValue(path, value);
			} catch (ContextException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Copies the data nodes from one dataContext to another (shallow copy).
	 * 
	 * @param fromContext
	 *            ServiceContext
	 * @param toContext
	 *            ServiceContext
	 */
	public static void copyContext(Context fromContext, Context toContext) {
		try {
			Enumeration e = fromContext.contextPaths();
			String path = null;

			while (e.hasMoreElements()) {
				path = new String((String) e.nextElement());
				toContext.putValue(path, fromContext.getValue(path));
			}
		} catch (ContextException ce) {
			ce.printStackTrace();
		}
	}

	/**
	 * Checks if the Exertion is valid for this provider. Returns true if it is
	 * valid otherwise returns false.
	 * 
	 * @param exertion
	 *            Exertion interface
	 * @return boolean
	 */
	public boolean isValidExertion(Exertion exertion) {
		String pn = exertion.getProcessSignature().getProviderName();

		if (!(pn == null || pn.equals(SorcerConstants.NULL) || SorcerConstants.ANY
				.equals(pn.trim()))) {
			if (!pn.equals(delegate.config.getProviderName()))
				return false;
		}

		for (int i = 0; i < delegate.publishedServiceTypes.length; i++) {
			if (delegate.publishedServiceTypes[i].equals(exertion
					.getProcessSignature().getServiceType()))
				return true;
		}

		return false;
	}

	public void setSpacer(Spacer spacer) {
		this.spacer = spacer;
	} 
	
	/**
	 * Traverses the Job hierarchy and reset the task status to INITIAL.
	 * 
	 * @param exertion
	 *            Either a task or job
	 */
	public void resetExertionStatus(Exertion exertion) {
		if (((ServiceExertion) exertion).isTask()) {
			((Task) exertion).setStatus(ExecState.INITIAL);
		} else if (((ServiceExertion) exertion).isJob()) {
			for (int i = 0; i < ((Job) exertion).size(); i++) {
				this.resetExertionStatus(((Job) exertion).exertionAt(i));
			}
		}
	}

	//com.sun.jini.thread.TaskManager.Task
	private class ExertionRunnable implements Runnable, TaskManager.Task {
		volatile boolean stopped = false;
		private Exertion xrt;
		private Exertion result;

		ExertionRunnable(Exertion exertion) {
			xrt = exertion;
		}

		public void run() {
			try {
				if (xrt instanceof Conditional) {
					result = doConditional(xrt);
				} else if (((ServiceExertion) xrt).isJob()) {
					result = doRendezvousExertion((Job) xrt);
				} else if (((ServiceExertion) xrt).isTask()) {
					result = doTask((Task) xrt);
				}
				stopped = true;
			} catch (Exception e) {
				stopped = true;
				logger.finer("Exertion thread killed by exception: "
						+ e.getMessage());
				// e.printStackTrace();
			}
		}

		@Override
		public boolean runAfter(List tasks, int size) {
			return false;
		}
	}
	
	public Task doIntraTask(Task task) throws ExertionException,
			SignatureException, RemoteException {
		List<Signature> alls = task.getSignatures();

        Signature lastSig = alls.get(alls.size()-1);
		if (alls.size() > 1 &&  task.isBatch() && !(lastSig instanceof NetSignature)) {
			for (int i = 0; i < alls.size()-1; i++) {
				alls.get(i).setType(Signature.PRE);
            }
		}
		
		task.startExecTime();
		if (task.getPreprocessSignatures().size() > 0) {
			Context cxt = preprocess(task);
			cxt.setExertion(task);
            task.setContext(cxt);
		}
		// execute service task
		List<Signature> ts = new ArrayList<Signature>(1);
		Signature tsig = task.getProcessSignature();
        ((ServiceContext)task.getDataContext()).setCurrentSelector(tsig.getSelector());
		((ServiceContext)task.getDataContext()).setCurrentPrefix(((ServiceSignature)tsig).getPrefix());

		ts.add(tsig);
		task.setSignatures(ts);
		if (tsig.getReturnPath() != null)
			((ServiceContext)task.getDataContext()).setReturnPath(tsig.getReturnPath());

		task = task.doTask();
		if (task.getStatus() <= ExecState.FAILED) {
			task.stopExecTime();
			ExertionException ex = new ExertionException("Batch service task failed: "
					+ task.getName());
			task.reportException(ex);
			task.setStatus(ExecState.FAILED);
			task.setSignatures(alls);
			return task;
		}
		task.setSignatures(alls);
		if (task.getPostprocessSignatures().size() > 0) {
			Context cxt = postprocess(task);
			cxt.setExertion(task);
			task.setContext(cxt);
		}
		if (task.getStatus() <= ExecState.FAILED) {
			task.stopExecTime();
			ExertionException ex = new ExertionException("Batch service task failed: "
					+ task.getName());
			task.reportException(ex);
			task.setStatus(ExecState.FAILED);
			task.setSignatures(alls);
			return task;
		}
		task.setSignatures(alls);
		task.stopExecTime();
		return task;
	}
	
	private Context preprocess(Task task) throws ExertionException {
		return processContinousely(task, task.getPreprocessSignatures());
	}

	private Context postprocess(Task task) throws ExertionException {
		return processContinousely(task, task.getPostprocessSignatures());
	}

	private Context processContinousely(Task task, List<Signature> signatures)
			throws ExertionException {
		Signature.Type type = signatures.get(0).getType();
		Task t = null;
		Context shared = task.getDataContext();
		for (int i = 0; i < signatures.size(); i++) {
            try {
                t = task(task.getName() + "-" + i, signatures.get(i), shared);
            } catch (SignatureException e) {
                throw new  ExertionException(e);
            }
            signatures.get(i).setType(Signature.SRV);
			((ServiceContext)task.getDataContext()).setCurrentSelector(signatures.get(i).getSelector());
			((ServiceContext)task.getDataContext()).setCurrentPrefix(((ServiceSignature)signatures.get(i)).getPrefix());

			List<Signature> tmp = new ArrayList<Signature>(1);
			tmp.add(signatures.get(i));
			t.setSignatures(tmp);
			t.setContinous(true);
			try {
				t = t.doTask();
				signatures.get(i).setType(type);
				shared = t.getDataContext();
				if (t.getStatus() <= ExecState.FAILED) {
					task.setStatus(ExecState.FAILED);
					ExertionException ne = new ExertionException(
							"Batch signature failed: " + signatures.get(i));
					task.reportException(ne);
					task.setContext(shared);
					return shared;
				}
			} catch (Exception e) {
				e.printStackTrace();
				task.setStatus(ExecState.FAILED);
				task.reportException(e);
				task.setContext(shared);
				return shared;
			}
		}
		// return the service dataContext of the last exertion
		return shared;
	}
}
