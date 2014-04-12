/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import net.jini.core.lookup.ServiceID;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.id.Uuid;
import sorcer.co.tuple.Entry;
import sorcer.core.SorcerConstants;
import sorcer.core.context.ControlContext;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.ThrowableTrace;
import sorcer.core.provider.Jobber;
import sorcer.core.signature.NetSignature;
import sorcer.security.util.Auth;
import sorcer.security.util.SorcerPrincipal;
import sorcer.util.StringUtils;

import static sorcer.core.SorcerConstants.CPS;

/**
 * A job is a composite service-oriented message comprised of {@link Exertion}
 * instances with its own service {@link Context} and a collection of service
 * {@link Signature}s. The job's signature is usually referring to a
 * {@link Jobber} and the job's context describes the composition
 * of component exertions as defined by the Interpreter programming pattern.
 * 
 * @see Exertion
 * @see Task
 * 
 * @author Mike Sobolewski
 */
@SuppressWarnings("rawtypes")
public abstract class Job extends ServiceExertion implements CompoundExertion {

	private static final long serialVersionUID = -6161435179772214884L;

	/* our logger */
	protected final static Logger logger = Logger.getLogger(Job.class.getName());
	
	/**
	 * Component exertions of this job (the Composite Design pattern)
	 */
	protected List<Exertion> exertions = new ArrayList<Exertion>();

	public Integer state = INITIAL;

	/**
	 * Constructs a job and sets all default values to it.
	 */
	public Job() {
		exertions = new ArrayList<Exertion>();
		// exertions = Collections.synchronizedList(new ArrayList<Exertion>());
		init();
	}

	/**
	 * Constructs a job and sets all default values to it.
	 * 
	 * @param name
	 *            The name of the job.
	 */
	public Job(String name) {
		super(name);
	}

	/**
	 * Constructs a job and sets all default values to it.
	 * 
	 * @param exertion
	 *            The first Exertion of the job.
	 */
	public Job(Exertion exertion) throws ExertionException {
		addExertion(exertion);
	}

	public Job(String name, String description) {
		this(name);
		this.description = description;
	}

	public Job(String name, String description, List<Signature> signatures) {
		this(name, description);
		this.signatures = signatures;
	}

	/**
	 * Initialize it with assigning it a new ControlContext and a defaultMethod
	 * with serviceType as "sorcer.core.provider.jobber.ServiceJobber" name as
	 * "service" and providerName "*"
	 */
	private void init() {
		NetSignature s = new NetSignature("service", Jobber.class);
		// Needs to be RemoteJobber for Cataloger to find it
		// s.setServiceType(Jobber.class.getName());
		s.setProviderName(null);
		s.setType(Signature.Type.SRV);
		signatures.add(s); // Add the signature
	}

	public List<Signature> getSignatures() {
//TODO check...
//		if (signatures != null)
//			for (int i = 0; i < signatures.size(); i++)
//				signatures.get(i).setProviderName(controlContext.getRendezvousName());
		return signatures;
	}

	@Override
	public boolean isJob() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#isCompound()
	 */
	@Override
	public boolean isCompound() {
		return true;
	}
	
	public boolean hasChild(String childName) {
		for (Exertion ext : exertions) {
			if (ext.getName().equals(childName))
				return true;
		}
		return false;
	}

	@Override
    public Exertion getChild(String childName) {
		for (Exertion ext : exertions) {
			if (ext.getName().equals(childName))
				return ext;
		}
		return null;
	}

	public long getLsbID() {
		return (lsbId == null) ? -1 : lsbId;
	}

	/**
	 * Returns the number of exertions in this Job.
	 * 
	 * @return the number of exertions in this Job.
	 */
	@Override
    public int size() {
		return exertions.size();
	}

    public int indexOf(Exertion ex) {
		return exertions.indexOf(ex);
	}

	/**
	 * Sets the component at the specified <code>index</code> of this vector to
	 * be the specified object. The previous component at that position is
	 * discarded.
	 * <p>
	 */
	@Override
    public void setExertionAt(Exertion ex, int i) {
		exertions.set(i, ex);
	}

	public Exertion getMasterExertion() {
        // Changes below fix master exertions 03.09.2013 PR
		Uuid contextId = null;
		try {
			//contextName = (String) controlContext.getValue(ControlContext.MASTER_EXERTION);
            contextId = (Uuid) controlContext.getValue(ControlContext.MASTER_EXERTION);
		} catch (ContextException ex) {
			ex.printStackTrace();
		}
		if (contextId == null
				&& (controlContext.getFlowType().equals(ControlContext.SEQUENTIAL)
                || controlContext.getFlowType().equals(ControlContext.AUTO))) {
			return (size() > 0) ? get(size() - 1) : null;
		} else {
			Exertion master = null;
			for (int i = 0; i < size(); i++) {
				if (get(i).getId().equals(contextId)) {
					master = get(i);
					break;
				}
			}
			return master;
		}
	}

	public void setRendezvousName(String jobberName) {
		controlContext.setRendezvousName(jobberName);
	}

	public Signature getProcessSignature() {
		Signature method = super.getProcessSignature();
		if (method != null)
			method.setProviderName(controlContext.getRendezvousName());
        // Required for space to work - added by PR, 19.10.2013
        if (method.getProviderName()==null)
            method.setProviderName(SorcerConstants.ANY);
		return method;
	}
	
	@Override
	public Exertion addExertion(Exertion ex) throws ExertionException {
		exertions.add(ex);
		ex.setIndex(exertions.indexOf(ex));
		try {
			controlContext.registerExertion(ex);
		} catch (ContextException e) {
			throw new ExertionException(e);
		}
		((ServiceExertion) ex).setParentId(getId());
		return this;
	}

    public void addExertions(List<Exertion> exertions) {
		if (this.exertions != null)
			this.exertions.addAll(exertions);
		else {
			this.exertions = new ArrayList<Exertion>();
			this.exertions.addAll(exertions);
		}
	}

    public void setExertions(List<Exertion> exertions) {
		this.exertions = exertions;

	}

	public Job addExertion(Exertion exertion, int priority) throws ExertionException {
		addExertion(exertion);
		controlContext.setPriority(exertion, priority);
		return this;
	}

	public Exertion removeExertion(Exertion exertion) throws ContextException {
		// int index = ((ExertionImpl)exertion).getIndex();
		exertions.remove(exertion);
		controlContext.deregisterExertion(this, exertion);
		return exertion;
	}

	public void remove(int index) throws ContextException {
		removeExertion(get(index));
	}

	/**
	 * Returns the exertion at the specified index.
	 * <p>
	 * 
	 * @param index
	 *            an index into this vector.
	 * @return the Exertion at the specified index.
	 * @exception ArrayIndexOutOfBoundsException
	 *                if the <tt>index</tt> is negative or not less than the
	 *                current size of this <tt>Job</tt> object.
	 */
	public Exertion get(int index) {
		return exertions.get(index);
	}

    @Override
    public Exertion doExert(Transaction tx) throws ExertionException, SignatureException, RemoteException, TransactionException {
        return doJob(tx);
	}

	public abstract Job doJob(Transaction txn) throws ExertionException,
			SignatureException, RemoteException, TransactionException;

	public void undoJob() throws ExertionException, SignatureException,
			RemoteException {
		throw new ExertionException("Not implemneted by this Job: " + this);
	}
	
	public void setState(int state) {
		this.state = new Integer(state);
	}

	public int getState() {
		return state.intValue();
	}

	public String getPrincipalID() {
		Set principals = subject.getPrincipals();
		Iterator iterator = principals.iterator();
		while (iterator.hasNext()) {
			Principal p = (Principal) iterator.next();
			if (p instanceof SorcerPrincipal)
				return ((SorcerPrincipal) p).getId();
		}
		return null;
	}

	public void setPrincipalID(String id) {
		Set principals = subject.getPrincipals();
		Iterator iterator = principals.iterator();
		while (iterator.hasNext()) {
			Principal p = (Principal) iterator.next();
			if (p instanceof SorcerPrincipal)
				((SorcerPrincipal) p).setId(id);
		}
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
		for (int i = 0; i < size(); i++) {
			((ServiceExertion) get(i)).setSubject(subject);
		}
	}

	public void setPrincipal(SorcerPrincipal principal) {
		setSubject(Auth.createSubject(principal));
		this.principal = principal;
	}

	public Subject getSubject() {
		return subject;
	}
	
	public ServiceID getServiceID() {
		if (lsbId == null || msbId == null)
			return null;
		else
			return new ServiceID(msbId.longValue(), lsbId.longValue());
	}
	
	/**
	 * Returns a string representation of Contexts of this Job, containing the
	 * String representation of each context in it's exertion.
	 * @throws sorcer.service.ExertionException
	 */
	public String jobContextToString() throws ExertionException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < exertions.size(); i++) {
			if (get(i).isJob())
				sb.append(((Job) get(i)).jobContextToString());
			else
				sb.append(((ServiceExertion) get(i)).contextToString());
		}
		return sb.toString();
	}

	public void setMasterExertion(Exertion exertion) {
		controlContext.setMasterExertion(exertion);
	}

	public void setOwnerId(String id) {
		ownerId = id;
		if (controlContext != null)
			controlContext.setOwnerID(id);
		for (int i = 0; i < exertions.size(); i++)
			(((ServiceExertion) get(i))).setOwnerId(id);
	}

	public String getContextName() {
		return Context.JOB_ + name + "[" + index + "]" + Context.ID;
	}

	public String toString() {
		StringBuffer desc = new StringBuffer(super.toString());
		desc.append("\n=== START PRINTING JOB ===\n");	
		desc
				.append("\n=============================\nListing Component Exertions\n=============================\n");
		for (int i = 0; i < size(); i++) {
			desc.append("\n===========\n Exertion ").append(i).append(
					"\n===========\n").append(
					((ServiceExertion) get(i)).describe());
		}
		desc.append("\n=== DONE PRINTING JOB ===\n");
		return desc.toString();
	}

	/**
	 * Returns all component <code>Exertion</code>s of this composite exertion.
	 * 
	 * @return all component exertions
	 */
	@Override
    public List<Exertion> getExertions() {
		return exertions;
	}

	@Override
    public List<Exertion> getExertions(List<Exertion> exs) {
		for (Exertion e : exertions)
			((ServiceExertion) e).getExertions(exs);
		exs.add(this);
		return exs;
	}
	
	@Override
	public List<ThrowableTrace> getExceptions() {
		List<ThrowableTrace> exceptions = new ArrayList<ThrowableTrace>();
		for (Exertion ext : exertions) {
			exceptions.addAll(ext.getExceptions());
		}
		return exceptions;
	}
	
	/**
	 * Return true if this composite <code>Job</code> is a tree.
	 * 
	 * @param visited
	 *            a set of visited exertions
	 * @return true if this <code>Job</code> composite is a tree
	 * @see Exertion#isTree()
	 */
	public boolean isTree(Set visited) {
		visited.add(this);
		Iterator i = exertions.iterator();
		while (i.hasNext()) {
			ServiceExertion e = (ServiceExertion) i.next();
			if (visited.contains(e) || !e.isTree(visited)) {
				return false;
			}
		}
		return true;
	}

	public Exertion getExertion(int index) {
		return exertions.get(index);
	}
	
	public Context getContext() {
		 return getJobContext();
	}
	
	public Context getJobContext() {
		ServiceContext cxt = new ServiceContext(name);
		cxt.setSubject("job/data/context", name);
		
		return linkContext(cxt, getName());
	}

	public Context getControlInfo() {
		ServiceContext cxt = new ServiceContext(name);
		cxt.setSubject("job/control/context", name);
		
		return linkControlContext(cxt, getName());
	}

	@Override
	public Context linkContext(Context context, String path) {
		Exertion ext;
		for (int i = 0; i < size(); i++) {
			ext = exertions.get(i);
			try {
				((ServiceExertion) ext).linkContext(context, path + CPS + ext.getName());
			} catch (ContextException e) {
				e.printStackTrace();
			}
		}
		return context;
	}
	
	@Override
	public Context linkControlContext(Context context, String path) {
		Exertion ext;
		for (int i = 0; i < size(); i++) {
			ext = exertions.get(i);
			try {
				((ServiceExertion) ext).linkControlContext(context, path + CPS
						+ ext.getName());
			} catch (ContextException e) {
				e.printStackTrace();
			}
		}
		return context;
	}

	public Object getJobValue(String path) throws ContextException {
        String[] attributes = StringUtils.tokenizerSplit(path, CPS);
		// remove the leading attribute of the current exertion
		if (attributes[0].equals(getName())) {
			String[] attributes1 = new String[attributes.length - 1];
			System.arraycopy(attributes, 1, attributes1, 0,
					attributes.length - 1);
			attributes = attributes1;
		}
		String last = attributes[0];
		Exertion exti = this;
		for (String attribute : attributes) {
			if (((ServiceExertion) exti).hasChild(attribute)) {
				exti = ((Job) exti).getChild(attribute);
				if (exti instanceof Task) {
					last = attribute;
					break;
				}
			} else {
				break;
			}
		}
		int index = path.indexOf(last);
		String contextPath = path.substring(index + last.length() + 1);

		return exti.getContext().getValue(contextPath);
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Mappable#getValue(java.lang.String, sorcer.service.Arg[])
	 */
	@Override
	public Object getValue(String path, Arg... args) throws ContextException {
		if (path.startsWith("super")) {
			return parent.getContext().getValue(path.substring(6), args);
		} else {
			if (path.indexOf(name) >= 0)
				return getJobValue(path);
			else
				return dataContext.getValue(path, args);
		}
	}
	
	public Object putValue(String path, Object value) throws ContextException {
		if (path.indexOf(name) >= 0)
			putJobValue(path, value);
		else
			super.putValue(path, value);
		return value;
	}
	
	public Object putJobValue(String path, Object value) throws ContextException {
        String[] attributes = StringUtils.tokenizerSplit(path, CPS);
		// remove the leading attribute of the current exertion
		if (attributes[0].equals(getName())) {
			String[] attributes1 = new String[attributes.length - 1];
			System.arraycopy(attributes, 1, attributes1, 0,
					attributes.length - 1);
			attributes = attributes1;
		}
		String last = attributes[0];
		Exertion exti = this;
		for (String attribute : attributes) {
			if (((ServiceExertion) exti).hasChild(attribute)) {
				exti = ((Job) exti).getChild(attribute);
				if (exti instanceof Task) {
					last = attribute;
					break;
				}
			} else {
				break;
			}
		}
		int index = path.indexOf(last);
		String contextPath = path.substring(index + last.length() + 1);
		exti.getContext().putValue(contextPath, value);
		return value;
	}
	
	public ReturnPath getReturnPath() {
		return dataContext.getReturnPath();
	}
	
	@Override
	public Object getReturnValue(Arg... entries) throws ContextException,
			RemoteException {
		//ReturnPath rp = ((ServiceContext) dataContext).getReturnJobPath();
        ReturnPath rp = dataContext.getReturnPath();
		Object obj = null;
		if (rp != null) {
			if (rp.path == null || rp.path.equals("self")) {
				return this;
			} else if (rp.type != null) {
				obj = rp.type.cast(getValue(rp.path));
			} else {
				obj = getValue(rp.path);
			}
		} else {
			obj = getJobContext();
		}
		return obj;
	}
	
	public Context getComponentContext(String path) throws ContextException {
		Exertion xrt = getComponentExertion(path);
		return xrt.getContext();
	}
	
	public Context getComponentControlContext(String path) {
		Exertion xrt = getComponentExertion(path);
		return xrt.getControlContext();
	}
	
	public Exertion getComponentExertion(String path) {
        String[] attributes = StringUtils.tokenizerSplit(path, CPS);
		// remove the leading attribute of the current exertion
		if (attributes[0].equals(getName())) {
			String[] attributes1 = new String[attributes.length - 1];
			System.arraycopy(attributes, 1, attributes1, 0,
					attributes.length - 1);
			attributes = attributes1;
		}
		Exertion exti = this;
		for (String attribute : attributes) {
			if (((ServiceExertion) exti).hasChild(attribute)) {
				exti = ((CompoundExertion) exti).getChild(attribute);
				if (exti instanceof Task) {
					break;
				}
			} else {
				break;
			}
		}
		return exti;
	}
	
	public void reset(int state) {
		for(Exertion e : exertions)
			((ServiceExertion)e).reset(state);
		
		this.setStatus(state);
	}
	
	@Override
	public ServiceExertion substitute(Arg... entries)
			throws EvaluationException {
		try {
			if (entries != null) {
				for (Arg e : entries) {
					if (e instanceof Entry)
						if (((Entry) e).path().indexOf(name) >= 0)
							putJobValue(((Entry) e).path(), ((Entry) e).value());

						else
							super.putValue(((Entry) e).path(),
									((Entry) e).value());
				}
			}
		} catch (ContextException ex) {
			ex.printStackTrace();
			throw new EvaluationException(ex);
		}
		return this;
	}
}
