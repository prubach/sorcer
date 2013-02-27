package jini.intro;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Summer extends Remote {

	long sumString(String s) throws InvalidLongException, RemoteException;
}