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
package sorcer.core.context.model.par;

import sorcer.core.SorcerConstants;
import sorcer.core.context.ApplicationDescription;
import sorcer.core.context.ServiceContext;
import sorcer.service.*;
import sorcer.util.bdb.sdb.DbpUtil;
import sorcer.util.bdb.sdb.SdbUtil;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.Principal;
//import sorcer.vfe.VarException;
//import sorcer.vfe.Variability;
//import sorcer.vfe.util.VarSet;

/**
 * In service-based modeling, a parameter (for short a par) is a special kind of
 * variable, used in a service context {@link ParModel} to refer to one of the
 * pieces of data provided as input to the invokers (subroutines of the
 * context). These pieces of data are called arguments.
 * 
 * @author Mike Sobolewski
 */
@SuppressWarnings({"unchecked", "rawtypes" })
public class Par<T> extends Identity implements Arg, Mappable<T>, Evaluation<T>, Invocation<T>, Setter, Comparable<T>, Serializable {
    //Variability<T>,

	private static final long serialVersionUID = 7495489980319169695L;
	 
	protected String name;
	
	private Principal principal;

	protected T value;

	protected Context<T> scope;
	
	boolean persistent = false;
				
	// data store URL for this par
	private URL dbURL;

	// A context returning value at the path
	// that is this par name
	// Sorcer Mappable: Context, Exertion, or Var args
	protected Mappable mappable;

	public Par(String parname) {
		name = parname;
		value = null;
	}
	
	public Par(String parname, T argument) {
		name = parname;
		value = argument;
	}
	
	public Par(String parname, Object argument, Context scope) {
		this(parname, (T)argument);
		this.scope = scope;
	}
	
	public Par(String name, String path, Mappable map) {
		this(name);
		value =  (T)path;
		mappable = map;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getName()
	 */
	@Override
	public String getName() {
		return name;
	}
	
	public void setValue(Object value) throws EvaluationException {
		if (persistent) {
			try {
				if (SdbUtil.isSosURL(value)) {
					if (((URL)value).getRef() == null) {
						value = DbpUtil.store(value);
					} else if (persistent){
                        DbpUtil.update((URL)value, value);
					}
					return;
				}	
			} catch (Exception e) {
				throw new EvaluationException(e);
			} 
		}
		if (mappable != null) {
			try {
				Object val = mappable.asis((String)this.value);
				if (val instanceof Par) {
					((Par)val).setValue(value);
				} else if (persistent) {
					if (SdbUtil.isSosURL(val)) {
                        DbpUtil.update((URL)val, value);
					} else {
						URL url = DbpUtil.store(value);
						Par p = new Par((String)this.value, url);
						p.setPersistent(true);
						if (mappable instanceof ServiceContext)
							((ServiceContext)mappable).put((String)this.value, p);
						else
							mappable.putValue((String)this.value, p);
					} 
				} else {
					mappable.putValue((String)this.value, value);
				}
			} catch (Exception e) {
				throw new EvaluationException(e);
			}
		} 
		else
			this.value = (T)value;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getAsis()
	 */
	@Override
	public T asis() throws EvaluationException, RemoteException {
		return value;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getValue(sorcer.co.tuple.Parameter[])
	 */
	@Override
	public T getValue(Arg... entries) throws EvaluationException,
			RemoteException {
		substitute(entries);
		T val = null;
		try {
			if (mappable != null) {
				val = (T) mappable.getValue((String) value);
			} else if (value == null && scope != null) {
				val = (T) ((ServiceContext<T>) scope).get(name);
			} else {
				val = value;
			}

//			if (val instanceof Invoker)
//				val = ((Invoker<T>) val).getValue(entries);
			if (val instanceof Evaluation)
				val = ((Evaluation<T>) val).getValue(entries);

			if (persistent) {
				if (SdbUtil.isSosURL(val))
					val = (T) ((URL) val).getContent();
				else {
					if (mappable != null) {
						URL url = DbpUtil.store(val);
						Par p = new Par((String)this.value, url);
						p.setPersistent(true);
						if (mappable instanceof ServiceContext)
							((ServiceContext)mappable).put((String)this.value, p);
						else
							mappable.putValue((String)this.value, p);
					}
					else {
						value = (T) DbpUtil.store(val);
					}
				}
			}
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
		return val;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#substitute(sorcer.co.tuple.Parameter[])
	 */
	@Override
	public Evaluation<T> substitute(Arg... parameters)
			throws EvaluationException, RemoteException {
		if (parameters == null)
			return this;
		for (Arg p : parameters) {
			if (p instanceof Par) {
				if (name.equals(((Par<T>)p).name)) {
					value = ((Par<T>)p).value;
				if (((Par<T>)p).getScope() != null)
					try {
						scope.append(((Par<T>)p).getScope());
					} catch (ContextException e) {
						throw new EvaluationException(e);
					}
				}
			}
		}
		return this;
	}
	
	public Context getScope() {
		return scope;
	}

	public void setScope(Context scope) {
		this.scope = scope;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(T o) {
		if (o == null)
			throw new NullPointerException();
		if (o instanceof Par<?>)
			return name.compareTo(((Par<?>) o).getName());
		else
			return -1;
	}
	
	@Override
	public String toString() {
		return "par [" + name + ":" + value + "]";
	}


	/* (non-Javadoc)
	 * @see sorcer.vfe.Variability#getArg(java.lang.String)
	 */
	//@Override
	public T getArg(String varName) throws VarException {
		try {
			return (T) scope.getValue(varName, null);
		} catch (ContextException e) {
			throw new VarException(e);
		}
	}

	/**
	 * <p>
	 * Returns a Contextable (Context or Exertion) of this Par that by a its
	 * name provides values of this Par.
	 * </p>
	 * 
	 * @return the contextable
	 */
	public Mappable getContextable() {
		return mappable;
	}

	public Principal getPrincipal() {
		return principal;
	}
	
// TODO VFE related
	/* (non-Javadoc)
	 * @see sorcer.vfe.Variability#getArgVar(java.lang.String)
	 */
	/*@Override
	public Variability<?> getArgVar(String varName) throws VarException {
		Object obj = scope.get(varName);
		if (obj instanceof Variability)
			return (Variability) obj;
		else
			return new Par(varName, obj, scope);
	}  */

	public URL getDbURL() throws MalformedURLException {
		URL url = null;
		if (dbURL != null)
			url = dbURL;
		else if (((ServiceContext)scope).getDbUrl() != null)
			url = new URL(((ServiceContext)scope).getDbUrl());
		
		return url;
	}

	public URL getURL() throws ContextException {
		if (persistent) {
			if (mappable != null)
				return (URL)mappable.asis((String)value);
			else
				return (URL)value;
		}
		return null;
	}
	
	public void setDbURL(URL dbURL) {
		this.dbURL = dbURL;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.vfe.Persister#isPersistable()
	 */
	@Override
	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean state) {
		persistent = state;
	}
	
	public Mappable getMappable() {
		return mappable;
	}

	public void setMappable(Mappable mappable) {
		this.mappable = mappable;
	}
	
	public boolean isMappable() {
		return (mappable != null);
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Invocation#invoke(sorcer.service.Arg[])
	 */
	@Override
	public T invoke(Arg... entries) throws RemoteException, InvocationException {
		try {
			if (value instanceof Invocation)
				return ((Invocation<T>) value).invoke(entries);
			else
				return getValue(entries);
		} catch (EvaluationException e) {
			throw new InvocationException(e);
		}
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Invocation#invoke(sorcer.service.Context, sorcer.service.Arg[])
	 */
	@Override
	public T invoke(Context context, Arg... entries) throws RemoteException,
			InvocationException {
		try {
			scope.append(context);
			return invoke(entries);
		} catch (Exception e) {
			throw new InvocationException(e);
		}
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Mappable#getValue(java.lang.String, sorcer.service.Arg[])
	 */
	@Override
	public T getValue(String path, Arg... args) throws ContextException {
		String[] attributes = path.split(SorcerConstants.CPS);
		if (attributes[0].equals(name)) {
			if (attributes.length == 1)
				try {
					return (T)getValue(args);
				} catch (RemoteException e) {
					throw new ContextException(e);
				}
			else if (mappable != null)
				return (T)mappable.getValue(path.substring(name.length()), args);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Mappable#asis(java.lang.String)
	 */
	@Override
	public T asis(String path) throws ContextException {
		String[] attributes = path.split(SorcerConstants.CPS);
		if (attributes[0].equals(name)) {
			if (attributes.length == 1)
				return value;
			else if (mappable != null)
				return (T)mappable.asis(path.substring(name.length()));
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Mappable#putValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public T putValue(String path, Object value) throws ContextException {
		String[] attributes = path.split(SorcerConstants.CPS);
		if (attributes[0].equals(name)) {
			if (attributes.length == 1)
				this.value = (T)value;
			else if (mappable != null)
				mappable.putValue(path.substring(name.length()), value);
		}
		return (T)value;	
	}
}
