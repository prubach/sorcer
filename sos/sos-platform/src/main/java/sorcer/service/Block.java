/*
 * Copyright 2013 the original author or authors.
 * Copyright 2013 SorcerSoft.org.
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sorcer.core.SorcerConstants;
import sorcer.util.bdb.sdb.DbpUtil;
import sorcer.util.bdb.sdb.SdbUtil;


/**
 * @author Mike Sobolewski
 */
public class Block extends ServiceExertion {

	private List<Exertion> exertions = new ArrayList<Exertion>();
	
	private URL contextURL;
	
	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#addExertion(sorcer.service.Exertion)
	 */
	@Override
	public Exertion addExertion(Exertion component) {
		exertions.add(component);
		return component;
	}

	public void setExertions(List<Exertion> exertions) {
		this.exertions = exertions;
	}

	public void setExertions(Exertion[] exertions) {
		this.exertions = Arrays.asList(exertions);
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getValue(java.lang.String, sorcer.service.Arg[])
	 */
	@Override
	public Object getValue(String path, Arg... args) throws ContextException {
		dataContext.getValue(path, args);
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getExertions()
	 */
	@Override
	public List<Exertion> getExertions() {
		return exertions;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.ServiceExertion#linkContext(sorcer.service.Context, java.lang.String)
	 */
	@Override
	public Context linkContext(Context context, String path)
			throws ContextException {
		dataContext.putLink(path + SorcerConstants.CPS + name, context);
		return context;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.ServiceExertion#linkControlContext(sorcer.service.Context, java.lang.String)
	 */
	@Override
	public Context linkControlContext(Context context, String path)
			throws ContextException {
		controlContext.putLink(path + SorcerConstants.CPS + name, context);
		return context;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.ServiceExertion#isTree(java.util.Set)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see sorcer.service.ServiceExertion#getExertions(java.util.List)
	 */
	@Override
	public List<Exertion> getExertions(List<Exertion> exs) {
		for (Exertion e : exertions)
			((ServiceExertion) e).getExertions(exs);
		exs.add(this);
		return exs;
	}
	
	public URL persistContext() throws ExertionException, SignatureException, ContextException {
        if (contextURL == null) {
			contextURL = DbpUtil.store(dataContext);
			dataContext = null;
		} else {
            DbpUtil.update(dataContext);
		}
		return contextURL;
	}
}
