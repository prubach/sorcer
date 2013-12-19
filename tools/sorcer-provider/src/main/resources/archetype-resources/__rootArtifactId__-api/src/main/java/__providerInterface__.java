package ${package};

import java.rmi.Remote;
import java.rmi.RemoteException;

import sorcer.schema.Schema;
import sorcer.service.Context;

public interface ${providerInterface} extends Remote {
	
    @Schema(${providerInterface}Context.class)
	Context sayHelloWorld(Context context) throws RemoteException;

}
