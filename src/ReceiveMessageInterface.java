package bio;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public interface ReceiveMessageInterface extends Remote{
	public PublicKey receiveMessage() throws RemoteException, NoSuchAlgorithmException;
	public String sendDetails(String password,byte[] phash,String transactionID,byte[] spdata) throws RemoteException,NotBoundException,NoSuchAlgorithmException, NoSuchPaddingException,InvalidKeyException, IllegalBlockSizeException, BadPaddingException;
	//void generateKeys(PublicKey x) throws RemoteException;
}
