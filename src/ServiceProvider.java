/**
 * 
 */
package bio;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * @author subba
 *
 */
public class ServiceProvider extends java.rmi.server.UnicastRemoteObject implements ServiceProviderInterface{
	static ReceiveMessageInterface rmiServer1;
	String address;
	static Registry spregistry;
	static String BioPassword;
	static byte[] BioHashvalue;
	static String BioTransactionID;
	static Registry registry;
	static byte[] cipherData;
	static String serverAddress;
	static String serverPort;
	static String serviceProviderId="d3abdabac2914a16896302327dc346d8";
	
	public String receiveMessage(String password,byte[] hashvalue,String transactionID) throws RemoteException, NotBoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		//System.out.println(x);
		BioPassword=password;
		BioHashvalue=hashvalue;
		BioTransactionID=transactionID;
		/**
		 * sending all service provider details along with transaction details
		 */
		rmiServer1=(ReceiveMessageInterface)(registry.lookup("rmiServer1"));
		String msg=rmiServer1.sendDetails(BioPassword,BioHashvalue,BioTransactionID,cipherData);
		return msg;
	}
	public ServiceProvider() throws RemoteException
	{
		try
		{  
			address = (InetAddress.getLocalHost()).toString();
		}
		catch(Exception e){
			System.out.println("can't get inet address.");
		}
		int port=3234;
		System.out.println("this address=" + address +  ",port=" + port);
		try
		{
			spregistry = LocateRegistry.createRegistry(port);
			spregistry.rebind("spServer", this);
		}
		catch(RemoteException e){
			System.out.println("remote exception"+ e);
		}
	}
	static public void main(String args[]) throws NotBoundException, NoSuchAlgorithmException, RemoteException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		serverAddress=args[0];
		serverPort=args[1];
		String text=args[2];
		
		System.out.println("sending " + text + " to " +serverAddress + ":" + serverPort);
		try
		{
			ServiceProvider sp=new ServiceProvider();
			
			registry=LocateRegistry.getRegistry(serverAddress,(new Integer(serverPort)).intValue());
			rmiServer1=(ReceiveMessageInterface)(registry.lookup("rmiServer1"));
			
			// call the remote method
			//Receiving Bank Public Key
			PublicKey key=rmiServer1.receiveMessage();
			
			/**
			 * Encryption
			 */
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			String serviceProviderKey="M35000AS";
			String TransactionInfo="item price=24,000";
			
			
			/*MessageDigest digest = MessageDigest.getInstance("SHA-256");
		    byte[] hashvalue=serviceProviderKey.getBytes();
		    digest.reset();
		    digest.update(hashvalue);
		    byte[] Phashdigest=digest.digest();
		    System.out.println(Phashdigest.toString());
		    */
			
			/**
			 * Hashing the service provider key
			 */
	        String securePassword = getSecurePassword(serviceProviderKey,"200");
		    System.out.println(securePassword);
			String req=serviceProviderId+"|"+securePassword.toString()+"|"+TransactionInfo;
			byte[] data=req.getBytes();
			cipherData = cipher.doFinal(data);
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
		catch(NotBoundException e){
			System.err.println(e);
		}
		
	}
	 private static String getSecurePassword(String passwordToHash, String salt)
	    {
	        String generatedPassword = null;
	        try {
	            // Create MessageDigest instance for MD5
	            MessageDigest md = MessageDigest.getInstance("MD5");
	            //Add password bytes to digest
	            md.update(salt.getBytes());
	            //Get the hash's bytes
	            byte[] bytes = md.digest(passwordToHash.getBytes());
	            //This bytes[] has bytes in decimal format;
	            //Convert it to hexadecimal format
	            StringBuilder sb = new StringBuilder();
	            for(int i=0; i< bytes.length ;i++)
	            {
	                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
	            }
	            //Get complete hashed password in hex format
	            generatedPassword = sb.toString();
	        }
	        catch (NoSuchAlgorithmException e) {
	            e.printStackTrace();
	        }
	        return generatedPassword;
	    }
	/*private static String getSalt() throws NoSuchAlgorithmException
	{
	    //Always use a SecureRandom generator
	    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
	    //Create array for salt
	    byte[] salt = new byte[16];
	    //Get a random salt
	    sr.nextBytes(salt);
	    //return salt
	    return salt.toString();
	}*/
}
