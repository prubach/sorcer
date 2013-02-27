package sorcer.ex5.provider;

import java.rmi.Remote;
import java.rmi.RemoteException;

import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.EvaluationException;

public interface Worker1 extends Remote {

	Context sayHi(Context context) throws RemoteException, ContextException,
			EvaluationException;

	Context sayBye(Context context) throws RemoteException, ContextException,
			EvaluationException;

	Context doIt(Context context) throws InvalidWork, RemoteException,
			ContextException, EvaluationException;
}
