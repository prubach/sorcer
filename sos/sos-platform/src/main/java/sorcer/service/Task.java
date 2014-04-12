/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
 * Copyright 2013, 2014 SorcerSoft.com S.A.
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

package sorcer.service;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.jini.core.transaction.Transaction;
import sorcer.core.context.ServiceContext;
import sorcer.core.provider.ControlFlowManager;
import sorcer.core.signature.NetSignature;

/**
 * A <code>Task</code> is an elementary service-oriented message
 * {@link sorcer.service.Exertion} (with its own service {@link sorcer.service.Context} and a collection of
 * service {@link Signature}s. Signatures of four
 * {@link sorcer.service.Signature.Type}s can be associated with each task:
 * <code>SERVICE</code>, <code>PREPROCESS</code>, <code>POSTROCESS</code>, and
 * <code>APPEND</code>. However, only a single <code>PROCESS</code> signature
 * can be associated with a task but multiple preprocessing, postprocessing, and
 * context appending methods can be added.
 * 
 * @see Exertion
 * @see Job
 * 
 * @author Mike Sobolewski
 */
@SuppressWarnings("rawtypes")
public class Task extends ServiceExertion {

	private static final long serialVersionUID = 5179772214884L;

	/** our logger */
	protected final static Logger logger = Logger.getLogger(Task.class
			.getName());

	public final static String argsPath = "method/args";

	// used for tasks with multiple signatures by CatalogSequentialDispatcher
	private boolean isContinous = false;

	protected Task innerTask;
	
	public Task() {
		super();
	}

	public Task(String name) {
		super(name);
	}

	public Task(String name, Signature signature) {
		this(name);
		addSignature(signature);
	}
	
	public Task(String name, Signature signature, Context context) {
		this(name);
		addSignature(signature);
		dataContext = (ServiceContext)context;
	}
	
	public Task(String name, String description) {
		this(name);
		this.description = description;
	}

	public Task(String name, List<Signature> signatures) {
		this(name, "", signatures);
	}

	public Task(String name, String description, List<Signature> signatures) {
		this(name, description);
		if (this.signatures != null) {
			this.signatures.addAll(signatures);
		}
	}

    @Override
    public Exertion doExert(Transaction tx) throws ExertionException, SignatureException, RemoteException {
        return doTask(tx);
	}

	public Task doTask() throws ExertionException, SignatureException,
			RemoteException {
		return doTask(null);
	}
	
	public Task doTask(Transaction txn) throws ExertionException,
			SignatureException, RemoteException {
		return innerTask.doTask(txn);
	}
	
	public void undoTask() throws ExertionException, SignatureException,
			RemoteException {
		throw new ExertionException("Not implemneted by this Task: " + this);
	}

	public void setIndex(int i) {
		index = new Integer(i);
	}

	@Override
	public boolean isTask()  {
		return true;
	}
	
	@Override
	public boolean isCmd()  {
		return (signatures.size() == 1);
	}
	
	public boolean hasChild(String childName) {
		return false;
	}

	/** {@inheritDoc} */
	public boolean isJob() {
		return false;
	}

	public void setOwnerId(String oid) {
		// Util.debug("Owner ID: " +oid);
		this.ownerId = oid;
		if (signatures != null)
			for (int i = 0; i < signatures.size(); i++)
				((NetSignature) signatures.get(i)).setOwnerId(oid);
		// Util.debug("Context : "+ context);
		if (dataContext != null)
			dataContext.setOwnerID(oid);
	}

	public ServiceContext doIt() throws ExertionException {
		throw new ExertionException("Not supported method in this class");
	}

	// Just to remove if at all the places.
	public boolean equals(Task task) throws Exception {
		return name.equals(task.name);
	}

	public String toString() {
		if (innerTask != null) {
			return innerTask.toString();
		}
		StringBuilder sb = new StringBuilder(
				"\n=== START PRINTNIG TASK ===\nExertion Description: "
						+ getClass().getName() + ":" + name);
		sb.append("\n\tstatus: ").append(getStatus());
		sb.append(", task ID=");
		sb.append(getId());
		sb.append(", description: ");
		sb.append(description);
		sb.append(", priority: ");
		sb.append(priority);
		// .append( ", Index=" + getIndex())
		sb.append(", AccessClass=");
		sb.append(getAccessClass()).append(
				// ", isExportControlled=" + isExportControlled()).append(
				", providerName: ");
		if (getProcessSignature() != null)
			sb.append(getProcessSignature().getProviderName());
		sb.append(", principal: ").append(getPrincipal());
		sb.append(", serviceType: ").append(getServiceType());
		sb.append(", selector: ").append(getSelector());
		sb.append(", parent ID: ").append(parentId);

		if (signatures.size() == 1) {
            ///
            if (getProcessSignature()!=null)
			    sb.append(getProcessSignature().getProviderName());
		} else {
			for (Signature s : signatures) {
				sb.append("\n  ").append(s);
			}
		}
		String time = getControlContext().getExecTime();
		if (time != null && time.length() > 0)
			sb.append("\n\texec time=").append(time);
		sb.append(controlContext).append("\n");
		sb.append(dataContext);
		sb.append("\n=== DONE PRINTING TASK ===\n");

		return sb.toString();
	}

	public String describe() {
		StringBuilder sb = new StringBuilder(this.getClass().getName() + ": "
				+ name);
		sb.append(" task ID: ").append(getId()).append("\n  process sig: ")
				.append(getProcessSignature());
		sb.append("\n  status: ").append(getStatus());
		String time = getControlContext().getExecTime();
		if (time != null && time.length() > 0)
			sb.append("\n  exec time: ").append(time);
		return sb.toString();
	}

	/**
	 * Returns true; elementary exertions are always "trees."
	 * 
	 * @param visited
	 *            ignored
	 * @return true; elementary exertions are always "trees"
	 * @see Exertion#isTree()
	 */
	public boolean isTree(Set visited) {
		visited.add(this);
		return true;
	}

	/**
	 * Returns a service task in the specified format. Some tasks can be defined
	 * for thin clients that do not use RMI or Jini.
	 * 
	 * @param type
	 *            the type of needed task format
	 * @return
	 */
	public Exertion getUpdatedExertion(int type) {
		// the previous implementation of ServiceTask (thin) and
		// RemoteServiceTask (thick) abandoned for a while.
		return this;
	}

	@Override
	public Context linkContext(Context context, String path) {
		try {
			((ServiceContext) context).putLink(path, getContext());
		} catch (ContextException e) {
			e.printStackTrace();
		}
		return context;
	}

	@Override
	public Context linkControlContext(Context context, String path) {
		try {
			((ServiceContext) context).putLink(path, getControlContext());
		} catch (ContextException e) {
			e.printStackTrace();
		}
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Exertion#getExertions()
	 */
	@Override
	public List<Exertion> getExertions() {
		ArrayList<Exertion> list = new ArrayList<Exertion>(1);
		list.add(this);
		return list;
	}

	public List<Exertion> getExertions(List<Exertion> exs) {
		exs.add(this);
		return exs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Exertion#addExertion(sorcer.service.Exertion)
	 */
	@Override
	public Exertion addExertion(Exertion component) {
		throw new RuntimeException("Tasks do not contain component exertions!");
	}

	public Task getInnerTask() {
		return innerTask;
	}

	public void setInnerTask(Task innerTask) {
		this.innerTask = innerTask;
	}
	
	/**
	 * <p>
	 * Returns <code>true</code> if this task takes its service context from the
	 * previously executed task in sequence, otherwise <code>false</code>.
	 * </p>
	 * 
	 * @return the isContinous
	 */
	public boolean isContinous() {
		return isContinous;
	}

	/**
	 * <p>
	 * Assigns <code>isContinous</code> <code>true</code> to if this task takes
	 * its service context from the previously executed task in sequence.
	 * </p>
	 * 
	 * @param isContinous
	 *            the isContinous to set
	 */
	public void setContinous(boolean isContinous) {
		this.isContinous = isContinous;
	}

	protected Task doBatchTask(Transaction txn) throws RemoteException,
			ExertionException, SignatureException, ContextException {
		ControlFlowManager ep = new ControlFlowManager();
		return ep.doBatchTask(this);
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Mappable#getValue(java.lang.String, sorcer.service.Arg[])
	 */
	@Override
	public Object getValue(String path, Arg... args) throws ContextException {
		if (path.startsWith("super")) {
			return parent.getContext().getValue(path.substring(6));
		} else {
			return dataContext.getValue(path, args);
		}
	}
	
	@Override
	public void reset(int state) {
		status = state;
	}

    public boolean isNet() {
        return true;
    }
}
