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

package sorcer.service;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import sorcer.co.tuple.Tuple2;
import sorcer.core.SorcerConstants;
import sorcer.core.context.ControlContext;
import sorcer.core.context.ControlContext.ThrowableTrace;
import sorcer.core.context.ServiceContext;
import sorcer.core.signature.NetSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.security.util.SorcerPrincipal;
import sorcer.service.Signature.ReturnPath;
import sorcer.service.Signature.Type;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.util.ExertManager;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("rawtypes")
public abstract class ServiceExertion implements Exertion, Revaluation, SorcerConstants, ExecState, Serializable {

	static final long serialVersionUID = -3907402419486719293L;

	protected final static Logger logger = Logger.getLogger(ServiceExertion.class.getName());

	protected Uuid exertionId;

	protected String runtimeId;
	
	protected Uuid parentId;

	protected Exertion parent;

	protected String ownerId;

	protected String subjectId;

	protected Subject subject;

	protected String domainId;

	protected String subdomainId;

	protected Long lsbId;

	protected Long msbId;

	protected Uuid sessionId;

	protected MonitoringSession monitorSession;
	
	/** position of Exertion in a job */
	protected Integer index;

	protected String name;

	protected String description;

	protected String project;

	protected String goodUntilDate;

	protected String accessClass;

	protected Boolean isExportControlled;
	
	protected Integer scopeCode;

	/** execution status: INITIAL|DONE|RUNNING|SUSPENDED|HALTED */
	protected Integer status = ExecState.INITIAL;

	protected Integer priority;

	protected List<Signature> signatures = new ArrayList<Signature>();
	//protected List<Signature> signatures;
	
	protected ServiceContext dataContext;
	
	public static boolean debug = false;
	
	private static String defaultName = "xrt-";
		
	// sequence number for unnamed Exertion instances
	public static int count = 0;
	
	/**
	 * A form of service dataContext that describes the control strategy of this
	 * exertion.
	 */
	protected ControlContext controlContext;

	protected SorcerPrincipal principal;
	
	protected boolean isRevaluable = false;
	
	public ServiceExertion() {
		this(defaultName + count++);
	}
	
	public ServiceExertion(String name) {
		init(name);
	}
	
	protected void init(String name) {
		if (name == null || name .length()==0)
			this.name = defaultName + count++;
		else
			this.name = name;
		exertionId = UuidFactory.generate();
		domainId = "0";
		subdomainId = "0";
		index = new Integer(-1);
		accessClass = SorcerConstants.PUBLIC;
		isExportControlled = Boolean.FALSE;
		scopeCode = new Integer(SorcerConstants.PRIVATE_SCOPE);
		status = new Integer(ExecState.INITIAL);
		dataContext = new ServiceContext(name);
		controlContext = new ControlContext(this);
		principal = new SorcerPrincipal(System.getProperty("user.name"));
		principal.setId(principal.getName());
		setSubject(principal);
		
		Calendar c = new GregorianCalendar();
		c.roll(Calendar.YEAR, true);
		goodUntilDate = Integer.toString(c.get(Calendar.MONTH)) + "/"
				+ Integer.toString(c.get(Calendar.DAY_OF_MONTH)) + "/"
				+ Integer.toString(c.get(Calendar.YEAR));
	}

    /* (non-Javadoc)
	 * @see sorcer.service.Invoker#invoke(sorcer.service.Parameter[])
	 */
    @Override
    public Object invoke(Parameter... entries) throws RemoteException,
            EvaluationException {
        try {
            return exert(entries);
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
    }

    /* (non-Javadoc)
     * @see sorcer.service.Invoker#invoke(sorcer.service.Context, sorcer.service.Parameter[])
     */
    @Override
    public Object invoke(Context context, Parameter... entries)
            throws RemoteException, EvaluationException {
        substitute(entries);
        try {
            dataContext.append(context);
            return exert();
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
    }

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#exert(net.jini.core.transaction.Transaction, sorcer.core.dataContext.Path.Entry[])
	 */
	@Override
	public <T extends Exertion> T exert(Transaction txn, Parameter... entries)
			throws TransactionException, ExertionException, RemoteException {
		ExertManager esh = new ExertManager(this);
		Exertion result = null;
		try {
			result = esh.exert(txn, null, entries);
		} catch (Exception e) {
			e.printStackTrace();
			if (result != null)
				((ServiceExertion) result).reportException(e);
		}
		return (T)result;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#exert(sorcer.core.dataContext.Path.Entry[])
	 */
	@Override
	public Exertion exert(Parameter... entries) throws TransactionException,
			ExertionException, RemoteException {
		try {
			substitute(entries);
		} catch (EvaluationException e) {
			e.printStackTrace();
			throw new ExertionException(e);
		}
		ExertManager esh = new ExertManager(this);
		return esh.exert(entries);
	}

	public Exertion exert(Transaction txn, String providerName, Parameter... entries)
			throws TransactionException, ExertionException, RemoteException {
		try {
			substitute(entries);
		} catch (EvaluationException e) {
			e.printStackTrace();
			throw new ExertionException(e);
		}
		ExertManager esh = new ExertManager(this);
		return esh.exert(txn, providerName);
	}
	
	private void setSubject(Principal principal) {
		if (principal == null)
			return;
		Set<Principal> principals = new HashSet<Principal>();
		principals.add(principal);
		subject = new Subject(true, principals, new HashSet(), new HashSet());
	}
	
	public SorcerPrincipal getSorcerPrincipal() {
		if (subject == null)
			return null;
		Set<Principal> principals = subject.getPrincipals();
		Iterator<Principal> iterator = principals.iterator();
		while (iterator.hasNext()) {
			Principal p = iterator.next();
			if (p instanceof SorcerPrincipal)
				return (SorcerPrincipal) p;
		}
		return null;
	}

	public String getPrincipalID() {
		SorcerPrincipal p = getSorcerPrincipal();
		if (p != null)
			return getSorcerPrincipal().getId();
		else
			return null;
	}

	public void setPrincipalID(String id) {
		SorcerPrincipal p = getSorcerPrincipal();
		if (p != null)
			p.setId(id);
	}
	
	public void removeSignature(int index) {
		signatures.remove(index);
	}

	public void setAccess(Access access) {
		controlContext.setAccessType(access);
	}

	public void setFlow(Flow type) {
		controlContext.setFlowType(type);
	}

    public List<Signature> getSignatures() {
		return signatures;
	}

	public void addSignatures(List<Signature> signatures) {
		if (this.signatures != null)
			this.signatures.addAll(signatures);
		else {
			this.signatures = new ArrayList<Signature>();
			this.signatures.addAll(signatures);
		}
	}

	public boolean isConcatenated() {
		for (Signature s : signatures) {
			if (s.getType() != Signature.Type.SRV)
				return false;
		}
		return true;
	}
	
	public void setSignatures(List<Signature> signatures) {
		this.signatures = signatures;
	}

	public void setProcessSignature(Signature signature) {
		for (Signature sig : this.signatures) {
			if (sig.getType() != Type.SRV) {
				this.signatures.remove(sig);
			}
		}
		this.signatures.add(signature);
	}
	
	public void setServicer(Servicer provider) {
		NetSignature ps = (NetSignature)getProcessSignature();
		ps.setServicer(provider);
	}

	public Servicer getServicer() {
		NetSignature ps = (NetSignature)getProcessSignature();
		return ps.getServicer();
	}

	public Flow getFlowType() {
		return controlContext.getFlowType();
	}

	public void setFlowType(Flow flowType) {
		controlContext.setFlowType(flowType);
	}

	public Access getAccessType() {
		return controlContext.getAccessType();
	}

	public void setAccessType(Access accessType) {
		controlContext.setAccessType(accessType);
	}

	public int getScopeCode() {
		return (scopeCode == null) ? -1 : scopeCode.intValue();
	}

	public void setScopeCode(int value) {
		scopeCode = new Integer(value);
	}

	public SorcerPrincipal getPrincipal() {
		return principal;
	}

	public void setPrincipal(SorcerPrincipal principal) {
		this.principal = principal;
	}

	public Uuid getParentId() {
		return parentId;
	}

	public void setParentId(Uuid parentId) {
		this.parentId = parentId;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String id) {
		ownerId = id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int value) {
		status = value;
	}

	public void setSubjectId(String id) {
		subjectId = id;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public void setProject(String projectName) {
		project = projectName;
	}

	public String getProject() {
		return project;
	}

	public void setAccessClass(String s) {
		if (SorcerConstants.SENSITIVE.equals(s) || SorcerConstants.CONFIDENTIAL.equals(s) || SorcerConstants.SECRET.equals(s))
			accessClass = s;
		else
			accessClass = SorcerConstants.PUBLIC;
	}

	public String getAccessClass() {
		return (accessClass == null) ? SorcerConstants.PUBLIC : accessClass;
	}

	public void isExportControlled(boolean b) {
		isExportControlled = new Boolean(b);
	}

	public boolean isExportControlled() {
		return isExportControlled.booleanValue();
	}

	public String getGoodUntilDate() {
		return goodUntilDate;
	}

	public void setGoodUntilDate(String date) {
		goodUntilDate = date;
	}

	public Uuid getId() {
		return exertionId;
	}

	public String getRuntimeId() {
		return runtimeId;
	}

	public void setRuntimeId(String id) {
		runtimeId = id;
	}

	public void setId(Uuid id) {
		exertionId = id;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public void setSubdomainId(String subdomaindId) {
		this.subdomainId = subdomaindId;
	}

	public String getSubdomainId() {
		return subdomainId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRendezvousName() {
		return controlContext.getRendezvousName();
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getIndex() {
		return (index == null) ? -1 : index.intValue();
	}

	public void setIndex(int i) {
		index = new Integer(i);
	}

	public boolean isMonitored() {
		return controlContext.isMonitorable();
	}
	
	public boolean isProvisionable() {
		return controlContext.isProvisionable();
	}
	
	public void setMonitored(boolean state) {
		controlContext.setMonitorable(state);
	}
	
	public boolean isWaitable() {
		return controlContext.isWaitable();
	}
	
	public void setWaitable(boolean state) {
		controlContext.setWaitable(state);
	}


	// should be implemented in subclasses accordingly
	public boolean hasChild(String childName) {
		return false;
	}

	public long getMsbId() {
		return (msbId == null) ? -1 : msbId.longValue();
	}

	public void setLsbId(long leastSig) {
		if (leastSig != -1) {
			lsbId = new Long(leastSig);
		}
	}

	public void setMsbId(long mostSig) {
		if (mostSig != -1) {
			msbId = new Long(mostSig);
		}
	}

	public void setSessionId(Uuid id) {
		sessionId = id;
		if (this instanceof Job) {
			System.out
					.println("sorcer.core.ExertionImpl::setSessionID this instanceof ServiceJob");
			List<Exertion> v = ((Job) this).getExertions();
			System.out
					.println("sorcer.core.ExertionImpl::setSessionID this instanceof ServiceJob2");
			for (int i = 0; i < v.size(); i++) {
				System.out
						.println("sorcer.core.ExertionImpl::setSessionID this instanceof ServiceJob3");
				((ServiceExertion) v.get(i)).setSessionId(id);
			}

		}
	}

	public Uuid getSessionId() {
		return sessionId;
	}

	public ServiceExertion setContext(Context dataContext) {
		this.dataContext = (ServiceContext) dataContext;
		((ServiceContext) dataContext).setExertion(this);
		return this;
	}
	
	public ServiceExertion setControlContext(ControlContext context) {
		controlContext = context;
		return this;
	}
	
	public ServiceExertion updateStrategy(ControlContext context) {
		controlContext.setAccessType(context.getAccessType());
		controlContext.setFlowType(context.getFlowType());
		controlContext.setProvisionable(context.isProvisionable());
		controlContext.setMonitorable(context.isMonitorable());
		controlContext.setWaitable(context.isWaitable());
		controlContext.setSignatures(context.getSignatures());
		return this;
	}
	
	public void setPriority(int p) {
		priority = new Integer(p);
	}

	public int getPriority() {
		return (priority == null) ? SorcerConstants.MIN_PRIORITY : priority.intValue();
	}

	public Signature getProcessSignature() {
		for (Signature s : signatures) {
			if (s.getType() == Signature.Type.SRV)
				return s;
		}
		return null;
	}

	public List<Signature> getPreprocessSignatures() {
		List<Signature> sl = new ArrayList<Signature>();
		for (Signature s : signatures) {
			if (s.getType() == Signature.Type.PRE)
				sl.add(s);
		}
		return sl;
	}

	public List<Signature> getPostprocessSignatures() {
		List<Signature> sl = new ArrayList<Signature>();
		for (Signature s : signatures) {
			if (s.getType() == Signature.Type.POST)
				sl.add(s);
		}
		return sl;
	}
	
	/**
	 * Appends a signature <code>signature</code> for this exertion.
	 * 
	 * @see #getSignatures
	 */
	public void addSignature(Signature signature) {
		if (signature == null)
			return;
		((ServiceSignature) signature).setOwnerId(getOwnerId());
		signatures.add(signature);
	}

	/**
	 * Removes a signature <code>signature</code> for this exertion.
	 * 
	 * @see #addSignature
	 */
	public void removeSignature(Signature signature) {
		signatures.remove(signature);
	}

	public Class getServiceType() {
		Signature signature = getProcessSignature();
		return (signature == null) ? null : signature.getServiceType();
	}

	public String getSelector() {
		Signature method = getProcessSignature();
		return (method == null) ? null : method.getSelector();
	}

	public int compareByIndex(Exertion e) {
		if (this.getIndex() > ((ServiceExertion) e).getIndex())
			return 1;
		else if (this.getIndex() < ((ServiceExertion) e).getIndex())
			return -1;
		else
			return 0;
	}

	public boolean isExecutable() {
		if (getServiceType() != null)
			return true;
		else
			return false;
	}

	public Exertion getParent() {
		return parent;
	}

	public void setParent(Exertion parent) {
		this.parent = parent;
	}
	
	public String contextToString() {
		return "";
	}

	public int getExceptionCount() {
		return controlContext.getExceptions().size();
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getTrace()
	 */
	@Override
	public List<String> getTrace() {
		return controlContext.getTrace();
	}
	
	/** {@inheritDoc} */
	public boolean isTree() {
		return isTree(new HashSet());
	}

	public Context getContext() {
		return dataContext;
	}

    public Context getDataContext() {
        return dataContext;
    }
	
	public Context getContext(String componentExertionName) {
		Exertion component = getExertion(componentExertionName);
		if (component != null)
			return getExertion(componentExertionName).getDataContext();
		else
			return null;
	}
	
	public Context getControlContext(String componentExertionName) {
		Exertion component = getExertion(componentExertionName);
		if (component != null)
			return getExertion(componentExertionName).getControlContext();
		else
			return null;
	}
	
	public ControlContext getControlContext() {
		return controlContext;
	}
	
	public Context getControlInfo() {
		return controlContext;
	}

	public void startExecTime() {
		if (controlContext.isExecTimeRequested())
			controlContext.startExecTime();
	}

	public void stopExecTime() {
		if (controlContext.isExecTimeRequested())
			controlContext.stopExecTime();
	}

	public String getExecTime() {
		if (controlContext.isExecTimeRequested() && controlContext.getStopwatch() != null)
			return controlContext.getExecTime();
		else
			return "";
	}
		
	public void setExecTimeRequested(boolean state) {
		controlContext.setExecTimeRequested(state);
	}

	public boolean isExecTimeRequested() {
		return controlContext.isExecTimeRequested();
	}

	abstract public Context linkContext(Context context, String path) throws ContextException;
	
	abstract public Context linkControlContext(Context context, String path) throws ContextException;
	
	/*
	 * Subclasses implement this to support the isTree() algorithm.
	 */
	public abstract boolean isTree(Set visited);

	public void reportException(Throwable t) {
		controlContext.addException(t);
	}
	
	public void addException(ThrowableTrace et) {
		controlContext.addException(et);
	}
	
	public ServiceExertion substitute(Parameter... entries) throws EvaluationException {
		if (entries != null && entries.length > 0) {
			for (Parameter e : entries) {
				if (e instanceof Tuple2) {
					try {
						putValue((String) ((Tuple2) e)._1, ((Tuple2) e)._2);
					} catch (ContextException ex) {
						ex.printStackTrace();
						throw new EvaluationException(ex);
					}
				}
			}
		}
		return this;
	}
	
	public Object getReturnValue() throws ContextException {
		ReturnPath returnPath = getDataContext().getReturnPath();
		if (returnPath != null) {
			if (returnPath.path == null || returnPath.path.equals("self")) {
				return getDataContext();
			} else {
				if (this instanceof Task) {
					return getDataContext().getValue(returnPath.path);
				} else if (this instanceof Job) {
					return ((Job) this).getValue(returnPath.path);
				}
			}
		}
		return this;
	}
	
	protected Object getArgs() throws ContextException {
		return dataContext.getArgs();
	}
	
	// no control dataContext
	public String info() {
		StringBuffer info = new StringBuffer().append(
				this.getClass().getName()).append(": " + name);
		info.append("\n  process sig=").append(getProcessSignature());
		info.append("\n  status=").append(status);
		info.append(", exertion ID=").append(exertionId);
		String time = getControlContext().getExecTime();
		if (time != null && time.length() > 0) {
			info.append("\n  Execution Time = " + time);
		}
		return info.toString();
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#isEvaluable()
	 */
	@Override
	public boolean isRevaluable() {
		return isRevaluable;
	}

	public void setRevaluable(boolean isRevaluable) {
		this.isRevaluable = isRevaluable;
	}
	
	public String toString() {
		if (debug)
			return describe();
		
		StringBuffer info = new StringBuffer().append(
				this.getClass().getName()).append(": " + name);
		info.append("\n  status=").append(status);
		info.append(", exertion ID=").append(exertionId);
		String time = getControlContext().getExecTime();
		if (time != null && time.length() > 0) {
			info.append("\n  Execution Time = " + time);
		}
		info.append("\n  [Control Context]\n");
		info.append(getControlContext() + "\n");
		return info.toString();
	}

	public List<Exertion> getAllExertions() {
		List<Exertion> exs = new ArrayList<Exertion>();
		getExertions(exs);
		return exs;
	}
	
	abstract public List<Exertion> getExertions(List<Exertion> exs);
	
	public void updateValue(Object value) throws ContextException {
		List<Exertion> exertions = getAllExertions();
//		logger.info(" value = " + value);
//		logger.info(" this exertion = " + this);
//		logger.info(" exertions = " + exertions);
		for (Exertion e : exertions) {
			if (!e.isJob()) {
				//logger.info(" exertion i = "+ e.getName());
				Context cxt = e.getDataContext();
				((ServiceContext) cxt).updateValue(value);
			}
		}
	}
	
	public Exertion getExertion(String componentExertionName) {
		if (name.equals(componentExertionName)) {
			return this;
		}
		else {
			List<Exertion> exertions = getAllExertions();
			for (Exertion e : exertions) {
				if (e.getName().equals(componentExertionName)) {
					return e;
				}
			}
			return null;
		}
	}
	
	public String state() {
		return controlContext.getRendezvousName();
	}

	// Check if this is a Job that will be performed by Spacer
	public boolean isSpacable() {
		if (controlContext.getAccessType().equals(Access.PULL)
				|| controlContext.getAccessType().equals(Access.QOS_PULL))
			return true;
		else
			return false;
	}

	public Signature correctProcessSignature() {
		Signature sig = getProcessSignature();
		if (sig != null) {
			Access access = getControlContext().getAccessType();

			if ((Access.PULL == access || Access.QOS_PULL == access)
					&& !getProcessSignature().getServiceType()
							.isAssignableFrom(Spacer.class)) {
				sig.setServiceType(Spacer.class);
				((NetSignature)sig).setSelector("service");
				sig.setProviderName(SorcerConstants.ANY);
				sig.setType(Signature.Type.SRV);
				getControlContext().setAccessType(access);
			} else if ((Access.PUSH == access || Access.QOS_PUSH == access)
					&& !getProcessSignature().getServiceType()
							.isAssignableFrom(Jobber.class)) {
				if (sig.getServiceType().isAssignableFrom(Spacer.class)) {
					sig.setServiceType(Jobber.class);
					((NetSignature)sig).setSelector("service");
					sig.setProviderName(SorcerConstants.ANY);
					sig.setType(Signature.Type.SRV);
					getControlContext().setAccessType(access);
				}
			}
		}
		return sig;
	}
	
	/**
	 * <p>
	 * Returns the monitor session of this exertion.
	 * </p>
	 * 
	 * @return the monitorSession
	 */
	public MonitoringSession getMonitorSession() {
		return monitorSession;
	}

	/**
	 * <p>
	 * Assigns a monitor session for this exertions.
	 * </p>
	 * 
	 * @param monitorSession
	 *            the monitorSession to set
	 */
	public void setMonitorSession(MonitoringSession monitorSession) {
		this.monitorSession = monitorSession;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getValue()
	 */
	@Override
	public Object getValue(Parameter... entries) throws EvaluationException, RemoteException {
		try {
			substitute(entries);
			return dataContext.getValue();
		} catch (ContextException e) {
			e.printStackTrace();
			throw new EvaluationException(e);
		}
	}

    /* (non-Javadoc)
     * @see sorcer.service.Evaluation#getAsIs()
     */
    @Override
    public Object getAsis() throws EvaluationException, RemoteException {
        return getValue();
    }

    public Object getValue(String path) throws ContextException {
        if (path.startsWith("super")) {
            return parent.getContext().getValue(path.substring(6));
        } else
            return dataContext.getValue(path);
    }

	public Object putValue(String path, Object value) throws ContextException {
		return dataContext.putValue(path, value);
	}

    /* (non-Javadoc)
 * @see sorcer.service.Exertion#getExceptions()
 */
    @Override
    public List<ThrowableTrace> getExceptions() {
        List<ThrowableTrace> exceptions = new ArrayList<ThrowableTrace>();
        if (controlContext != null)
            return controlContext.getExceptions();
        else
            return exceptions;
    }

    @Override
    public boolean isJob() {
        return false;
    }

    @Override
    public boolean isTask()  {
        return false;
    }

    @Override
    public boolean isCmd()  {
        return false;
    }

	
	public String describe() {
		if (!debug)
			return info();
		
		String stdoutSep = "================================================================================\n";
		StringBuffer info = new StringBuffer();
		info.append("\n" + stdoutSep)
		    .append("[SORCER Service Exertion]\n")
		    .append("\tExertion Type:        " + getClass().getName() + "\n")
		    .append("\tExertion Name:        " + name + "\n")
		    .append("\tExertion Status:      " + status + "\n")
		    .append("\tExertion ID:          " + exertionId + "\n")
		    .append("\tRuntime ID:           " + runtimeId + "\n")
		    .append("\tParent ID:            " + parentId  + "\n")
		    .append("\tOwner ID:             " + ownerId + "\n")
		    .append("\tSubject ID:           " + subjectId + "\n")
		    .append("\tDomain ID:            " + domainId  +  "\n")
		    .append("\tSubdomain ID:         "  + subdomainId + "\n")
		    .append("\tlsb ID:               " + lsbId + "\n")
		    .append("\tmsb ID:               " + msbId + "\n")
		    .append("\tSession ID:           " + sessionId + "\n")
		    .append("\tIndex:                " + index + "\n")
		    .append("\tDescription:          " + description + "\n")
		    .append("\tProject:              " + project + "\n")
		    .append("\tGood Until Date:      " + goodUntilDate + "\n")
		    .append("\tAccess Class:         " + accessClass + "\n")
		    .append("\tIs Export Controlled: " + isExportControlled + "\n")
		    .append("\tScope Code:           " + scopeCode + "\n")
		    .append("\tPriority:             " + priority + "\n")
		    .append("\tProvider Name:        " + getProcessSignature().getProviderName() + "\n")
		    .append("\tService Type:         " + getProcessSignature().getServiceType() + "\n")
		    .append("\tException Count:      " + getExceptionCount() + "\n")
		    .append("\tPrincipal:            " + principal + "\n")
		    .append(stdoutSep)
		    .append("[Control Context]\n")
		    .append(getControlContext() + "\n")
	        .append(stdoutSep);
		String time = getControlContext().getExecTime();
		if (time != null && time.length() > 0 ) {
			info.append("\nExecution Time = " + time + "\n"+ stdoutSep);
		}
		return info.toString();
	}
}
