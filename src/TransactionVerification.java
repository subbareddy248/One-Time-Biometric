/**
 * 
 */
package bio;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author subba
 *
 */
public interface TransactionVerification extends Remote{
	public String sendDetails(String password) throws RemoteException,NotBoundException;
}
