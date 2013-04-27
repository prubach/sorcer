package sorcer.core.loki.exertion;

import java.rmi.RemoteException;
import java.security.Key;
import java.util.List;
import java.util.Map;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.id.Uuid;
import sorcer.co.tuple.Parameter;
import sorcer.core.context.ControlContext;
import sorcer.core.context.ControlContext.ThrowableTrace;
import sorcer.service.Context;
import sorcer.service.Evaluation;
import sorcer.service.EvaluationException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Servicer;
import sorcer.service.Signature;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;

public class CCKExertion implements Exertion {
	public Map<Uuid, Key> ccKeys;

	static public CCKExertion get(Map<Uuid, Key> keys) {
		CCKExertion CCK = new CCKExertion();
		CCK.ccKeys = keys;
		return CCK;
	}

	public Exertion exert(Transaction txn) throws TransactionException,
			ExertionException, RemoteException {
		return null;
	}

	public Context getDataContext() {
		return null;
	}

	public Flow getFlowType() {
		return null;
	}

	public String getName() {
		return "Complimentary Compound Key Exertion";
	}

	public Signature getProcessSignature() {
		return null;
	}

	public List<Signature> getSignatures() {
		return null;
	}

	public boolean isJob() {
		return false;
	}

	public boolean isTree() {
		return false;
	}

	public void setFlowType(Flow flowType) {
	}

	public void setServicer(Servicer provider) {
	}

	public boolean isQos() {
		return false;
	}

	public boolean isMonitored() {
		return getControlContext().isMonitorable();
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see sorcer.service.Exertion#getControlContext()
	 */
	@Override
	public ControlContext getControlContext() {
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#addExertion(sorcer.service.Exertion)
	 */
	@Override
	public Exertion addExertion(Exertion component) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getDataContext()
	 */
	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getExertions()
	 */
	@Override
	public List<Exertion> getExertions() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getExceptions()
	 */
	public List<ThrowableTrace> getExceptions() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getAllExertions()
	 */
	@Override
	public List<Exertion> getAllExertions() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getExertion(java.lang.String)
	 */
	@Override
	public Exertion getExertion(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getDataContext(java.lang.String)
	 */
	@Override
	public Context getContext(String componentExertionName) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Identifiable#getID()
	 */
	@Override
	public Uuid getId() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getTrace()
	 */
	@Override
	public List<String> getTrace() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getAccessType()
	 */
	@Override
	public Access getAccessType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#isWait()
	 */
	@Override
	public boolean isWaitable() {
		return getControlContext().isWaitable();
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getStatus()
	 */
	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Identifiable#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#exert(net.jini.core.transaction.Transaction, sorcer.core.dataContext.Path.Entry[])
	 */
	@Override
	public <T extends Exertion> T exert(Transaction txn, Parameter... entries)
			throws TransactionException, ExertionException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#exert(sorcer.core.dataContext.Path.Entry[])
	 */
	@Override
	public Exertion exert(Parameter... entries) throws TransactionException,
			ExertionException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getAsis()
	 */
	@Override
	public Object getAsis() throws EvaluationException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#getValue(sorcer.co.tuple.Parameter[])
	 */
	@Override
	public Object getValue(Parameter... entries) throws EvaluationException,
			RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#substitute(sorcer.co.tuple.Parameter[])
	 */
	@Override
	public Evaluation<Object> substitute(Parameter... entries)
			throws EvaluationException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

    public boolean isProvisionable() {
        return getControlContext().isProvisionable();
    }

}
