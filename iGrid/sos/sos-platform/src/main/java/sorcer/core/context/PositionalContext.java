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

package sorcer.core.context;

import java.util.Arrays;
import java.util.List;

import sorcer.service.Context;
import sorcer.service.ContextException;

/**
 * @author Mike Sobolewski
 *
 */
public class PositionalContext extends ServiceContext implements Positioning {
	private int tally = 0;
	
	public PositionalContext() {
		super();
	}
	
	public PositionalContext(String name) {
		super(name);
	}
	
	public PositionalContext(String name, String subjectPath, Object subjectValue) {
		super(name, subjectPath, subjectValue);
	}
	
	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.Positioning#getInValueAt(sorcer.service.Context, int)
	 */
	@Override
	public Object getInValueAt(int index) throws ContextException {
		return Contexts.getInValueAt(this, index);
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.Positioning#getInoutValueAt(sorcer.service.Context, int)
	 */
	@Override
	public Object getInoutValueAt(int index) throws ContextException {
		return Contexts.getInoutValueAt(this, index);
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.Positioning#getOutValueAt(sorcer.service.Context, int)
	 */
	@Override
	public Object getOutValueAt(int index) throws ContextException {
		return Contexts.getOutValueAt(this, index);

	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.Positioning#getValueAt(sorcer.service.Context, int)
	 */
	@Override
	public Object getValueAt(int index) throws ContextException {
		return Contexts.getValueAt(this, index);

	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.Positioning#getValuesAt(sorcer.service.Context, int)
	 */
	@Override
	public List<Object> getValuesAt(int index) throws ContextException {
		Object[] objs = Contexts.getValuesAt(this, index);
		return Arrays.asList(objs);
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.Positioning#putInValueAt(sorcer.service.Context, java.lang.String, java.lang.Object, int)
	 */
	@Override
	public Object putInValueAt(String path, Object value,
			int index) throws ContextException {
		if (index >= tally) tally++;
		super.putValue(path, value);
		mark(path, Context.CONTEXT_PARAMETER + APS + Context.DA_IN + APS + APS);
		mark(path, Context.OPP + APS + Context.DA_IN + APS + index);
		return value;
			}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.Positioning#putInoutValueAt(sorcer.service.Context, java.lang.String, java.lang.Object, int)
	 */
	@Override
	public Object putInoutValueAt(String path, Object value,
			int index) throws ContextException {
		if (index >= tally) tally++;
		super.putValue(path, value);
		mark(path, Context.CONTEXT_PARAMETER + APS + Context.DA_INOUT + APS + APS);
		mark(path, Context.OPP + APS + Context.DA_INOUT + APS + index + APS);
		return value;
	}

	/* (non-Javadoc)
	 * @see sorcer.core.dataContext.Positioning#putOutValueAt(sorcer.service.Context, java.lang.String, java.lang.Object, int)
	 */
	@Override
	public Object putOutValueAt(String path, Object value, int index)
			throws ContextException {
		if (index >= tally) tally++;
		super.putValue(path, value);
		mark(path, Context.CONTEXT_PARAMETER + APS + Context.DA_OUT + APS + APS);
		mark(path, Context.OPP + APS + Context.DA_OUT + APS + index);
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.core.dataContext.Positioning#putValueAt(sorcer.service.Context,
	 * java.lang.String, java.lang.Object, int)
	 */
	@Override
	public Object putValueAt(String path, Object value, int index)
			throws ContextException {
		super.putValue(path, value);
		if (index >= tally) tally++;
		mark(path, Context.INDEX + APS + index);
		return value;
	}

	@Override
	public Object putValue(String path, Object value) throws ContextException {
		if (this.containsPath(path)) {
			return super.put(path, value);
		}
		else {
			int index = tally++;
			mark(path, Context.INDEX + APS + index);
			return putValueAt(path, value, index);
		}
	}
	
	public int getTally() {
		return tally;
	}
}
