package com.example.sorcer;

import java.rmi.Remote;
import java.rmi.RemoteException;

import sorcer.service.Context;

public interface SampleService extends Remote {
	
	Context sayHelloWorld(Context context) throws RemoteException;

}
