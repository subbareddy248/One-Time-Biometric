package bio;
import java.rmi.*;
import java.rmi.registry.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;

public class RmiClient
{
	static public void main(String args[]) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		ReceiveMessageInterface rmiServer;
		UserIdVerification rmiServer1;
		ServiceProviderInterface spServer;
		Registry registry;
		Registry registry1;
		String serverAddress=args[0];
		String serverPort=args[1];
		System.out.println(serverAddress+" "+serverPort);
		System.out.println("sending " + " to " +serverAddress + ":" + serverPort);
		try{
			registry=LocateRegistry.getRegistry(serverAddress,(new Integer(serverPort)).intValue());
			rmiServer=(ReceiveMessageInterface)(registry.lookup("rmiServer1"));
			
			// call the remote method
			//Receiving Bank Public Key
			PublicKey key=rmiServer.receiveMessage();
			System.out.println(key);
			
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			String userid="e3050620f38846eeab342d293f13e043";
			byte[] data=userid.getBytes();
			byte[] cipherData = cipher.doFinal(data);
			
			rmiServer1=(UserIdVerification)(registry.lookup("rmiServer1"));
			
			//Sending userid to bank for verification
			String deta[]=rmiServer1.receiveTransactionId(cipherData);
			//rmiServer.receiveMessage();
			System.out.println(deta[0]);
			
			ArrayList<String> list = new ArrayList<String>();
		    // read "any" type of image (in this case a png file)
		    BufferedImage image = ImageIO.read(new File("/home/subba/Desktop/test/iris1b.bmp"));
		    
		    
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
		    sb.append("0000000000000000000000000000000000000000000000");
		    
		    /**
		     * Token K
		     */
		    int token = 170;
		    StringBuilder sb1=new StringBuilder();
		    sb1.append(Integer.toBinaryString(token));
		    for(int j=0;j<102;j++)
		    {
		    	sb1.append("00000000000000000000");
		    }
		    
		    /**
		     * Biometric Template B xor K
		     */
		    StringBuilder template=new StringBuilder();
		    for(int j = 0; j < sb.length(); j++)
		        template.append((sb.charAt(j) ^ sb1.charAt(j)));
		    String result = template.toString();
		    
		    System.out.println("template"+result.length());
		    
		    /**
		     * create Phash B xor K xor R(random Binary R )
		     */
		    //create Hash value
		    template=new StringBuilder();
		    String randomBinary=deta[1];
		    for(int j=0;j<sb.length();j++)
		    {
		    	template.append((result.charAt(j) ^ randomBinary.charAt(j)));
		    }
		    String Phash=template.toString();
		    
		    Phash=Phash+"|"+key.toString()+"|"+deta[0];
		    
		    MessageDigest digest = MessageDigest.getInstance("SHA-256");
		    byte[] hashvalue=Phash.getBytes();
		    digest.update(hashvalue);
		    byte[] Phashdigest=digest.digest();
		    
		    //Reed Solomon Encoding
		    Random r = new Random();
		    int Low = 7;
		    int High = 9;
		    int R = r.nextInt(High-Low) + Low;
		    int blocksize=2048/(int)(Math.pow(2, R-1));
		    
		    int randomNum = r.nextInt(blocksize - 9) + 1;  
		    int leftOver = randomNum % R;
		    int newVal = randomNum + leftOver;  
		    if(newVal%2!=0)
		    	newVal=newVal+1;
		    R=8;
		    blocksize=16;
		    newVal=12;
		    
		    //keylength
		    int keylength=R*newVal;
		    System.out.println(keylength);
					    
		    //correction bits
		    int correctbits=(blocksize*R-keylength);
		    System.out.println(correctbits);
		    
		    int inputdata[]=new int[newVal+1];
		    int parity[]=new int[correctbits/2];
		   
		    for(i=0;i<newVal;i++)
		    {
		    	inputdata[i]=r.nextInt((int)Math.pow(2, R-1)-Low)+1;
		    }
		    RsEncode enc = new RsEncode(blocksize);
		    enc.encode(inputdata, parity);
		    System.out.println(java.util.Arrays.toString(parity));
		    
		    String s3=new String();
		    for(i = 0; i <blocksize; i++) 
			{
				String s1=Integer.toString(parity[i], 2);
				String s2=new String();
				int l=s1.length();
				while(l<Math.pow(2,R-1))
				{
					s2+="0";
					l++;
				}
				s3+=s2+s1;
			}
		    
		    String s11=new String();
		    
		    
		    
		    /*for(i=0; i<newVal*(int)Math.pow(2,R-1); i++)
		    {
		    	int x = 0;
			    if(r.nextBoolean()) x=1;
			    	s11 += x;
			}
		    System.out.println(R+"subba"+ s11.length());
		    *//**
		     * create bad words(erasers)
		     *//*
		    int[] bads = {};
			
			HashSet<Integer> bad = createSet(bads);
			String s3=new String();
			for(i = 0; i < (correctbits/R); i++) 
			{
				int b1 = r.nextInt(keylength + correctbits/R);
				bad.add(b1);
				String s1=Integer.toString(b1, 2);
				String s2=new String();
				int l=s1.length();
				while(l<Math.pow(2,R-1))
				{
					s2+="0";
					l++;
				}
				s3+=s1+s2;
			}*/
		    
		    
		    
			s11=s3;
			//Hadmard Matrix
		    int[][] H=new int[blocksize][(int)Math.pow(2,R-1)];
		    int k=0;
		    for(i=0;i<blocksize;i++)
		    {
		    	for(int j=0;j<(int)Math.pow(2, R-1);j++)
		    	{
		    		if(s11.charAt(k)=='0')
		    		{
		    			H[i][j]=1;
		    		}
		    		else
		    		{
		    			H[i][j]=0;
		    		}	
		    	}
		    }
		    //Password Creation
		    StringBuilder password=new StringBuilder();
		    k=0;
		    for(i=0;i<blocksize;i++)
		    {
		    	for(int j=0;j<(int)Math.pow(2, R-1);j++)
		    	{
		    			if(H[i][j]==1)
		    				password.append((result.charAt(k) ^ '1'));
		    			else
		    				password.append((result.charAt(k) ^ '0'));
		    			k++;
		    	}
		    }
		    System.out.println(password.toString());
		    /*//Encoding 
		    GF28.init();
			GF257.init();
			
			Random rnd = new Random();
			ArrayList<Integer> gens28 = GF28.findGenerators();
			System.out.println("# of Generators of GF(2^8): " + gens28.size());
			HashSet<Integer> gens257 = GF257.findGenerators();
			System.out.println("# of Generators of GF(257): " + gens257.size());
			char gen = 255;
			while(gen == 255) {
				int index = rnd.nextInt(gens28.size());
				int temp = gens28.get(index);
				if(gens257.contains(temp)) gen = (char)temp;
			}
			
			//String input = "Hello, my name is Alex Beutel."; //The message to send
			//gen = 3; //If you'd prefer to hardcode the  generator (just make sure its in both GF(2^8) and GF(257)
			int s = 5;
			
			Encoder e = new Encoder(result, s, gen);
			System.out.println("Generator: " + (int)gen);
			System.out.println("Encoded messaes"+ e.n);
			
			//char[] c28 = e.slow(); // O(nk) with GF(2^8)
			int[] cFFT = e.fast(); // O(nk) with GF(257)
			int[] c257 = e.slow257(); // FFT O(nlogn) with GF(257)
			
			System.out.println(cFFT);
			*/
			serverPort="3234";
			registry1=LocateRegistry.getRegistry(serverAddress,(new Integer(serverPort)).intValue());
			spServer=(ServiceProviderInterface)(registry1.lookup("spServer"));
			String x=spServer.receiveMessage(password.toString(),hashvalue,deta[0]);
			System.out.println(x);
			
			
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
		catch(NotBoundException e){
			System.err.println(e);
		}
	}
	public static HashSet<Integer> createSet(int[] a) {
		HashSet<Integer> h = new HashSet<Integer>();
		for(int a1 : a) h.add(a1);
		return h;
	}
}
 
