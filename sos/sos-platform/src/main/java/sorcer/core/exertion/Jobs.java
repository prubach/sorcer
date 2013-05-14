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
package sorcer.core.exertion;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import sorcer.core.SorcerConstants;
import sorcer.core.context.Contexts;
import sorcer.core.context.ControlContext;
import sorcer.core.context.ServiceContext;
import sorcer.falcon.base.Conditional;
import sorcer.service.*;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;

import java.util.*;

public class Jobs implements SorcerConstants {

	private Jobs() {
		// Utility class
	}
	
	public static boolean isCatalogSingleton(Job job) {
		ControlContext cc = job.getControlContext();
		return job.size() == 1
				&& Access.PUSH.equals(cc.get(cc.EXERTION_ACCESS));
	}

	public static boolean isQosCatalogSequential(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.SEQ.equals(cc.get(cc.EXERTION_FLOW))
				&& Access.QOS_PUSH.equals(cc.get(cc.EXERTION_ACCESS));
	}

	public static boolean isQosCatalogParallel(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.PAR.equals(cc.get(cc.EXERTION_FLOW))
				&& Access.QOS_PUSH.equals(cc.get(cc.EXERTION_ACCESS));
	}

	public static boolean isCatalogParallel(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.PAR.equals(cc.get(cc.EXERTION_FLOW))
				&& Access.PUSH.equals(cc.get(cc.EXERTION_ACCESS));
	}

	public static boolean isCatalogSequential(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.SEQ.equals(cc.get(cc.EXERTION_FLOW))
				&& Access.PUSH.equals(cc.get(cc.EXERTION_ACCESS));
	}

	public static boolean isSWIFSequential(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.SEQ.equals(cc.get(cc.EXERTION_FLOW))
				&& Access.SWIF.equals(cc.get(cc.EXERTION_ACCESS));
	}

	public static boolean isSpaceSequential(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.SEQ.equals(cc.get(cc.EXERTION_FLOW))
				&& Access.PULL.equals(cc.get(cc.EXERTION_ACCESS));
	}

	public static boolean isQosSpaceSequential(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.SEQ.equals(cc.get(cc.EXERTION_FLOW))
				&& Access.QOS_PULL.equals(cc.get(cc.EXERTION_ACCESS));
	}

	public static boolean isSpaceParallel(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.PAR.equals(cc.get(cc.EXERTION_FLOW))
				&& Access.PULL.equals(cc.get(cc.EXERTION_ACCESS));
	}

	public static boolean isQosSpaceParallel(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.PAR.equals(cc.get(cc.EXERTION_FLOW))
				&& Access.QOS_PULL.equals(cc.get(cc.EXERTION_ACCESS));
	}
	
	public static boolean isSpaceSingleton(Job job) {
		ControlContext cc = job.getControlContext();
		return job.size() == 1
				&& Access.PULL.equals(cc.get(cc.EXERTION_ACCESS));
	}

	public static boolean isParallel(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.PAR.equals(cc.get(cc.EXERTION_FLOW));
	}

	public static boolean isSequential(Job job) {
		ControlContext cc = job.getControlContext();
		return Flow.SEQ.equals(cc.get(cc.EXERTION_FLOW));
	}

	public static boolean isMonitorable(Job job) {
		ControlContext cc = job.getControlContext();
		return cc.isMonitorable();
	}

	public static Vector<Exertion> getInputExertions(Job job) {
		if (job == null || job.size() == 0)
			return null;
		Vector<Exertion> exertions = new Vector<Exertion>();
		Exertion master = job.getMasterExertion();
		for (int i = 0; i < job.size(); i++)
			if (!(job.exertionAt(i).equals(master) || job
					.getControlContext().isSkipped(job.exertionAt(i))))
				exertions.addElement(job.exertionAt(i));
		return exertions;
	}

	public static ControlContext getCC(Context sc) {
		ControlContext cc = new ControlContext();
		for (Enumeration e = ((Hashtable) sc).keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			cc.put(key, ((Hashtable) sc).get(key));
		}
		cc.setName(sc.getName());
		cc.setId(sc.getId());
		cc.setParentPath(sc.getParentPath());
		cc.setParentID((sc.getParentID() == null) ? null : sc.getParentID());
		cc.setCreationDate(sc.getCreationDate());
		cc.setLastUpdateDate(sc.getLastUpdateDate());
		cc.setDescription(sc.getDescription());
		cc.setScopeCode(sc.getScope());
		cc.setOwnerID(sc.getOwnerID());
		cc.setSubjectID(sc.getSubjectID());
		cc.setProject(sc.getProject());
		cc.setAccessClass(sc.getAccessClass());
		cc.setExportControl(sc.getExportControl());
		cc.setGoodUntilDate(sc.getGoodUntilDate());
		cc.setDomainID(sc.getDomainID());
		cc.setSubdomainID(sc.getSubdomainID());
		// controlContext.setLinkCount(sc.getLinkCount());
		// controlContext.setExceptionCount(sc.getExceptionCount());
		cc.setDomainName(sc.getDomainName());
		// controlContext.setCP(sc.getCP());
		// controlContext.scratch = sc.scratch;
		cc.setMetacontext(sc.getMetacontext());
		cc.isPersistantTaskAssociated = ((ServiceContext) sc).isPersistantTaskAssociated;
		return cc;
	}

	public static void removeExceptions(Job job) {
		removeExceptions(job.getDataContext());
		for (int i = 0; i < job.size(); i++) {
			if (job.exertionAt(i).isJob())
				removeExceptions((Job) job.exertionAt(i));
			else
				removeExceptions(job.exertionAt(i)
						.getDataContext());
		}
	}

	public static void removeExceptions(Context sc) {
		for (Enumeration e = ((ServiceContext) sc).keys(); e.hasMoreElements();) {
			String path = (String) e.nextElement();
			if (path.startsWith(SorcerConstants.EXCEPTIONS))
				try {
					sc.removePath(path);
				} catch (Exception ex) {
					// do nothing
				}
		}
		// sc.removeAttribute(SORCER.EXCEPTIONS);
	}

	public static void preserveNodeReferences(Exertion ref, Exertion res)
			throws ContextException {
		if (ref.isJob() && res.isJob())
			preserveNodeReferences((Job) ref, (Job) res);
		else
			preserveNodeReferences(ref, res);
	}

	public static void preserveNodeReferences(Job refJob,
			Job resJob) throws ContextException {
		int size = (refJob.size() < resJob.size()) ? refJob.size() : resJob
				.size();
		for (int i = 0; i < size; i++) {
			if (refJob.exertionAt(i).isJob()
					&& resJob.exertionAt(i).isJob())
				preserveNodeReferences((Job) refJob.exertionAt(i),
						(Job) resJob.exertionAt(i));
			else if (refJob.exertionAt(i).isTask()
					&& resJob.exertionAt(i).isTask())
				preserveNodeReferences(refJob.exertionAt(i)
						.getDataContext(), resJob.exertionAt(i)
						.getDataContext());
		}
	}

	public static void preserveNodeReferences(Context refContext,
			Context resContext) throws ContextException {
		Contexts.copyContextNodesFrom(resContext, refContext);
	}

	public static void replaceNullIDs(Exertion ex) {
		if (ex == null)
			return;
		if (ex.isJob()) {
			Job job = (Job) ex;
			if (job.getId() == null)
				job.setId(getId());
			if (job.getDataContext().getId() == null)
				job.getDataContext().setId(getId());
			for (int i = 0; i < job.size(); i++)
				replaceNullIDs(job.exertionAt(i));
		} else
			replaceNullIDs((ServiceExertion) ex);
	}

	public static void replaceNullIDs(ServiceExertion task) {
		if (task.getId() == null)
			task.setId(getId());
		if (task.getDataContext() != null) {
			if (task.getDataContext().getId() == null)
				task.getDataContext().setId(getId());
		}
	}

	public static synchronized Uuid getId() {
		 return UuidFactory.generate();
	}
	
	public static synchronized String getID() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException ie) {
			// do Nothing
		}
		return Long.toHexString(new Date().getTime());
	}

	public static ExertionEnvelop getEntryEnvelop(Exertion ex)
			throws ExertionException {
		if (ex == null)
			return null;
		else if (ex.getProcessSignature() != null)
			throw new ExertionException("No Method For Exertion e=" + ex);

		ExertionEnvelop eenv = ExertionEnvelop.getTemplate();
		eenv.serviceType = ex.getProcessSignature().getServiceType();
		eenv.providerName = ex.getProcessSignature().getProviderName();
		eenv.exertion = ex;
		eenv.exertionID = ex.getId();
		eenv.isJob = new Boolean(ex.isJob());
		eenv.state = new Integer(ExecState.INITIAL);
		return eenv;
	}

	public static List<Context> getTaskContexts(Exertion ex) {
		List<Context> v = new ArrayList<Context>();
		collectTaskContexts(ex, v);
		return v;
	}

	// For Recursion
	private static void collectTaskContexts(Exertion exertion, List<Context> contexts) {
		if (exertion instanceof Conditional)
			contexts.add(exertion.getDataContext());
		else if (exertion instanceof Job) {
			for (int i = 0; i < exertion.getExertions().size(); i++)
				collectTaskContexts(((Job) exertion).exertionAt(i),
						contexts);
		}
		else if (exertion instanceof Task) {
			contexts.add(exertion.getDataContext());
		}
	}
}
