/**
 * 
 */
package sorcer.core.context.model;

import java.rmi.RemoteException;

import sorcer.service.Parameter;
import sorcer.service.Evaluation;
import sorcer.service.EvaluationException;
import sorcer.service.Identity;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes","unchecked" })
public class Par<T> extends Identity implements Parameter, Evaluation<T>, Comparable<T> {

	String name;
	T value;

	ServiceModel scope;
	
	public Par(String parname) {
		name = parname;
		value = null;
	}
	
	public Par(String parname, T argument) {
		name = parname;
		value = argument;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getName()
	 */
	@Override
	public String getName() {
		return name;
	}
	
	public void setValue(T value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getAsis()
	 */
	@Override
	public T getAsis() throws EvaluationException, RemoteException {
		return value;
	}
	
	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getValue(sorcer.service.Parameter[])
	 */
	@Override
	public T getValue(Parameter... entries) throws EvaluationException,
			RemoteException {
		if (value instanceof Evaluation)
			return (T)((Evaluation)value).getValue();
		else 
			return value;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#substitute(sorcer.service.Parameter[])
	 */
	@Override
	public Evaluation<T> substitute(Parameter... parameters)
			throws EvaluationException, RemoteException {
		for (Parameter p : parameters) {
			if (p instanceof Par) {
				if (name.equals(((Par)p).name)) {
					value = (T) ((Par)p).value;
				if (((Par)p).getScope() != null)
					scope = ((Par)p).getScope();
				}
			}
		}
		return this;
	}
	
	public ServiceModel getScope() {
		return scope;
	}

	public void setScope(ServiceModel scope) {
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
		return name + ":" + value;
	}

}
