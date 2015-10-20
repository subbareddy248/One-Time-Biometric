package bio;
import java.rmi.*;
import java.rmi.registry.*;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;

public class RmiServer extends java.rmi.server.UnicastRemoteObject implements ReceiveMessageInterface,UserIdVerification{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String address;
	static Registry registry;
	static Registry registry1;
	static PublicKey publicKey;
	static PrivateKey privateKey;
	static StringBuilder bioTemplate=new StringBuilder();
	static String serviceProviderKey="M35000AS";
	static String serviceProviderId="d3abdabac2914a16896302327dc346d8";
	String[] transactionDetails = new String[20];
	static String securePassword;
	
	public PublicKey receiveMessage() throws RemoteException, NoSuchAlgorithmException
	{
		return publicKey;
	}
	public String[] receiveTransactionId(byte[] data) throws RemoteException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] cipherData = cipher.doFinal(data);
		String x=new String(cipherData);
		System.out.println(x);
		if(x.equals("e3050620f38846eeab342d293f13e043"))
		{
			String val= UUID.randomUUID().toString().replaceAll("-", "");
			transactionDetails[0]=val;
			System.out.println(transactionDetails[0]);
			String bits = "";
			Random r = new Random();
			for(int l=0; l<2048; l++){
				int y = 0;
				if(r.nextBoolean()) y=1;
				bits += y;
			}
			transactionDetails[1]=bits;
			System.out.println(transactionDetails[1]);
			System.out.println(transactionDetails);
		}
		return transactionDetails;
	}
	public String sendDetails(String password,byte[] phash,String transactionID,byte[] spdata) throws RemoteException,NotBoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] cipherData = cipher.doFinal(spdata);
		String x=new String(cipherData);
		String sp[]=new String[10];
		StringTokenizer st = new StringTokenizer(x.toString(),"|");
		int count=0;
		StringBuilder message=new StringBuilder();
		while(st.hasMoreTokens())
		{
			sp[count]=st.nextToken();
			count++;
		}
		
		String decodemessgae=new String();
		if(securePassword.equals(sp[1]) && serviceProviderId.equals(sp[0].toString().trim()))
		{
			System.out.println("hellosubba");
			if(transactionID.equals(transactionDetails[0].toString()))
			{
				int qrData[]=new int[16];
				
				for(int i=0;i<password.length();i++)
				{
					message.append(password.charAt(i)^bioTemplate.charAt(i));
				}
				for(int i=0;i<message.length();i++)
				{
					if(message.charAt(i)=='1')
					{
						decodemessgae+='0';
					}
					else
						decodemessgae+='1';
				}
				int start=56,end=63;
				for(int i=0;i<16;i++)
				{
					qrData[i]=Integer.parseInt(decodemessgae.substring(start, end),2);
					start+=64;
					end+=64;
				}	
				RsDecode dec = new RsDecode(16);
				int r = dec.decode(qrData);
				System.out.println("r=" + r);
				System.out.println("qrData=" + java.util.Arrays.toString(qrData));
				
				int[] MM = new int[qrData.length + 16];
				System.arraycopy(qrData, 0, MM, 0, qrData.length);
				RsEncode enc = new RsEncode(16);
				enc.encode(MM);
				System.out.println("qrData=" + java.util.Arrays.toString(MM));
			}
		}
		else
		{
			message.append("unkonw third party user");
		}
		return message.toString();
	}
	public RmiServer() throws RemoteException{
		try
		{  
			address = (InetAddress.getLocalHost()).toString();
		}
		catch(Exception e){
			System.out.println("can't get inet address.");
		}
		int port1=3232;
		System.out.println("this address=" + address +  ",port=" + port1);
		try
		{
			registry1=LocateRegistry.createRegistry(port1);
			registry1.rebind("rmiServer1", this);
		}
		catch(RemoteException e){
			System.out.println("remote exception"+ e);
		}
	}
	static public void main(String args[]){
		try{
			//ReceiveMessageInterface rmiclient;
			RmiServer server = new RmiServer();
			//rmiclient=(ReceiveMessageInterface)(registry.lookup("rmiclient"));
			//rmiclient.generateKeys(publicKey);
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			KeyPair keypair = keyGen.genKeyPair();
			publicKey = keypair.getPublic();
			privateKey = keypair.getPrivate();
			
			
			BufferedImage image = ImageIO.read(new File("/home/subba/Desktop/test/iris1.bmp"));

		    // write it to byte array in-memory (jpg format)
		    ByteArrayOutputStream b = new ByteArrayOutputStream();
		    ImageIO.write(image, "bmp", b);

		    // do whatever with the array...
		    byte[] jpgByteArray = b.toByteArray();

		    // convert it to a String with 0s and 1s        
		    StringBuilder sb = new StringBuilder();
		    int i=0;
		    for (byte by : jpgByteArray)
		    {
		    	i++;
		    	if(i>366)
		    		break;
		        sb.append(Integer.toBinaryString(by & 0xFF));
		    }
		    sb.append("0000000000000000000000000000000000000000000");
		    System.out.println(sb.toString().length());
		    System.out.println(sb.toString());
		    int token = 170;
		    StringBuilder sb1=new StringBuilder();
		    sb1.append(Integer.toBinaryString(token));
		    for(int j=0;j<102;j++)
		    {
		    	sb1.append("00000000000000000000");
		    }
		    System.out.println("Binary is " + sb1.length());
		    for(i=0;i<sb.length();i++)
		    {
		    	bioTemplate.append(sb.charAt(i)^sb1.charAt(i));
		    }
		    
		    /*MessageDigest digest = MessageDigest.getInstance("SHA-256");
		    byte[] hashvalue=serviceProviderKey.getBytes();
		    digest.update(hashvalue);
		    Phashdigest=digest.digest();
		    */
		    
		    securePassword = getSecurePassword(serviceProviderKey,"200");
		    System.out.println(securePassword);
		}
		catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	private static String getSecurePassword(String passwordToHash, String salt)
    {
        String generatedPassword = null;
        try 
        {
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
