package sorcer.core.loki.exertion;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.id.Uuid;
import sorcer.core.context.ControlContext;
import sorcer.core.context.ControlContext.ThrowableTrace;
import sorcer.service.*;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;

import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.List;

@SuppressWarnings("serial")
public class KPExertion implements Exertion {
	public Boolean isCreator;

	public byte[] keyPair;

	public PublicKey publicKey;

	public String GroupSeqId;

	static public KPExertion get(Boolean iscreator, byte[] keypair,
			PublicKey pk, String GSUID) {
		KPExertion KP = new KPExertion();
		KP.isCreator = iscreator;
		KP.keyPair = keypair;
		KP.publicKey = pk;
		KP.GroupSeqId = GSUID;
		return KP;
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
		return "KeyPair and KeyAgreement Exertion";
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
	 * @see sorcer.service.Evaluation#getValue(sorcer.service.Parameter[])
	 */
	@Override
	public Object getValue(Parameter... entries) throws EvaluationException,
			RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Evaluation#substitute(sorcer.service.Parameter[])
	 */
	@Override
	public Evaluation<Object> substitute(Parameter... entries)
			throws EvaluationException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

    /* (non-Javadoc)
 * @see sorcer.service.Exertion#isProvisionable()
 */
    @Override
    public boolean isProvisionable() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sorcer.service.Exertion#isTask()
     */
    @Override
    public boolean isTask() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sorcer.service.Exertion#isCmd()
     */
    @Override
    public boolean isCmd() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sorcer.service.Invoker#invoke(sorcer.service.Parameter[])
     */
    @Override
    public Object invoke(Parameter... entries) throws RemoteException,
            EvaluationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see sorcer.service.Invoker#invoke(sorcer.service.Context, sorcer.service.Parameter[])
     */
    @Override
    public Object invoke(Context context, Parameter... entries)
            throws RemoteException, EvaluationException {
        // TODO Auto-generated method stub
        return null;
    }


}
