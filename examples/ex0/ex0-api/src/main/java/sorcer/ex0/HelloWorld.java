package sorcer.ex0;

import java.rmi.Remote;
import java.rmi.RemoteException;

import sorcer.service.Context;

public interface HelloWorld extends Remote {
	
	Context sayHelloWorld(Context context) throws RemoteException;

}
