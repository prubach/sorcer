package sorcer.caller;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

import sorcer.schema.Schema;
import sorcer.service.Context;

public interface Caller extends Remote {
	
    Context execute(Context context) throws RemoteException;

    String getCurrentOutput(Context context) throws RemoteException;

    String getCurrentError(Context context) throws RemoteException;

    List<Context> getCurrentContexts() throws RemoteException;

    Boolean getCurrentStatus(Context context) throws RemoteException;

}
