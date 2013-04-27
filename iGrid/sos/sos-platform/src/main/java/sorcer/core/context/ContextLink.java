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

package sorcer.core.context;

import net.jini.id.Uuid;
import sorcer.core.SorcerConstants;
import sorcer.security.util.SorcerPrincipal;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Link;
import sorcer.util.SorcerUtil;

// import sorcer.core.util.*;

/**
 * Provides for service dataContext linking. Context links are references to an
 * offset (path) in a dataContext, which allows the reuse of dataContext objects.
 * 
 * @version $Revision: 1.2 $, $Date: 2007/08/15 22:11:10 $
 */
public class ContextLink implements SorcerConstants, Link {

	private static final long serialVersionUID = -7115324059076651991L;

	protected String name, offset;

	protected Uuid contextId;
	
	protected float version;

	protected boolean fetched;

	// runtime variables:
	public Context context;

	private static ContextAccessor cntxtAccessor;

	public int status = -1;

	public String restoreName = null;

	public String rootName = null;

	/**
	 * Add a dataContext link given the dataContext id and offset. Note the dataContext must
	 * have already been persisted in database.
	 */
	public ContextLink(Uuid id, float version, String offset,
			SorcerPrincipal principal) throws ContextException {
		// fetch dataContext
		contextId = id;
		this.version = version;
		context = getContext(principal);
		if (context == null) {
			status = BROKEN_LINK;
			// restoreName = name;
			// name = "Broken Link";
			return;
			// throw new ContextException("Failed to create ContextLink: dataContext
			// to link is null");
		} else if (offset == null) {
			status = BROKEN_LINK;
			this.name = context.getName();
			fetched = true;
			// throw new ContextException("Failed to create ContextLink: offset
			// is null");
		} else {
			this.name = context.getName();
			setOffset(offset);
			fetched = true;
		}
	}

	/**
	 * Returns a dataContext liink identifier.
	 * 
	 * @return a liink identifier
	 */
	public Uuid getId() {
		return contextId;
	}

	/**
	 * Assigns a pesrsistent datastore identifier for this linked dataContext.
	 * 
	 * @param id a link identifier
	 */
	public void setId(Uuid id) {
		contextId = id;
	}
	
	/**
	 * Add a dataContext link given the dataContext and offset. Public access to this
	 * method is probably temporary, as the preferred constructor is
	 * {@link #contextLink(String, SorcerPrincipal, String)}, since that method
	 * requires the dataContext has already been persisted.
	 * 
	 */
	public ContextLink(Context ctxt, String offset) throws ContextException {
		if (ctxt == null) {
			status = BROKEN_LINK;
			restoreName = name;
			// name = "Broken Link";
			return;
		}
		// throw new ContextException("Failed to create ContextLink: dataContext to
		// link is null");}
		else if (offset == null) {
			status = BROKEN_LINK;
			this.name = ctxt.getName();
			context = ctxt;
			version = ctxt.getVersion();
			Uuid id = context.getId();
			if (id != null)
				contextId = id;
			fetched = true;
			return;
			// throw new ContextException("Failed to create ContextLink: offset
			// is null");}}
		} else {
			this.name = ctxt.getName();
			context = ctxt;
			version = ctxt.getVersion();
			setOffset(offset);
			Uuid id = context.getId();
			if (id != null)
				contextId = id;
			fetched = true;
		}
	}

	/*
	 * public String rootName() { return SorcerUtil.firstToken(offset, CPS); }
	 */
	public String getName() {
		String result = name == null ? SorcerUtil.firstToken(offset, CPS) : name;
		if (result.equals(""))
			result = context.getRootName(); // assuming we have the dataContext...
		return result;
	}

	public String getRootName() {
		return context.getRootName();
	}

	public String getOffset() {
		return offset;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the offset in this linked dataContext. If the offset itself is obtained
	 * by traversing a link (meaning there is a redundant link), the offset is
	 * recalculated and the link object is reset to point to the owning dataContext
	 * (removing the redundancy).
	 * <P>
	 * Note: when links are originally set in ServiceContext, checks are
	 * performed.
	 */
	public void setOffset(String offset) throws ContextException {
		// validate offset is in this dataContext
		Object[] result = context.getContextMap(offset);

		if ((((String) result[1]).trim()).equals("")) {
			if (!isSameContext(result[0], result[1])) {
				// the alternative is throwing an exception:
				// throw new ContextException("Failed in setOffset: offset=
				// \""+offset+"\" is not in this dataContext, but in the dataContext
				// with name=\""+dataContext.getName()+"\". Link and
				// offset=\""+result[1]+"\" should be set in this dataContext
				// instead");
				this.offset = offset;
				return;
			}
			// Check if some rootname has changed

			return;
		}

		if (((Context) result[0]).getValue((String) result[1]) == null) {
			if (Contexts.checkIfPathBeginsWith((ServiceContext) result[0],
					(String) result[1])) {
				if (!isSameContext(result[0], result[1])) {
					this.offset = offset;
				}
			} else {
				restoreName = name;
				// name = "Broken Link";
				status = BROKEN_LINK;
				this.offset = (String) result[1];
				return;
			}
			return;
		} else if (result[0] != context) {
			this.offset = (String) result[1];
			this.context = (Context) result[0];
			this.version = context.getVersion();
			this.contextId = context.getId();
			this.fetched = true;
			// status = BROKEN_LINK;

			// the alternative is throwing an exception:
			// throw new ContextException("Failed in setOffset: offset=
			// \""+offset+"\" is not in this dataContext, but in the dataContext with
			// name=\""+dataContext.getName()+"\". Link and offset=\""+result[1]+"\"
			// should be set in this dataContext instead");
		} else
			this.offset = offset;
	}

	public String toString() {
		String str = "Link:\"" + name + "\"";
		return str;
	}

	public boolean isSameContext(Object cntxt, Object offset) {
		if (cntxt != context) {
			this.offset = (String) offset;
			this.context = (Context) cntxt;
			this.version = context.getVersion();
			this.contextId = context.getId();
			this.fetched = true;
			return true;
		}
		return false;
	}

	/**
	 * Return the dataContext. The {@link SorcerPrincipal} is given for authorization.
	 */
	public Context getContext(SorcerPrincipal prin) throws ContextException {
		if (!fetched) {
			Context cntxt;
			cntxt = cntxtAccessor.getContext(contextId, version, prin);
			fetched = true;
		}
		return context;
	}

	public void setContext(Context ctxt) {
		context = ctxt;
	}

	public boolean isRemote() {
		return contextId != null;
	}

	public boolean isLocal() {
		return context != null;
	}

	public boolean isFetched() {
		return fetched;
	}

	public void isFetched(boolean state) {
		fetched = state;
	}
}
