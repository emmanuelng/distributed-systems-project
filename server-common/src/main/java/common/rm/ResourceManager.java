package common.rm;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ResourceManager extends Remote {

	public boolean commit(int id) throws RemoteException;

	public boolean abort(int id) throws RemoteException;

}
