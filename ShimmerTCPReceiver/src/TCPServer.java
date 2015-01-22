//TCPServer.java

import java.io.*;
import java.net.*;

import com.shimmerresearch.driver.ObjectCluster;

class TCPServer 
{
   public static void main(String argv[]) throws Exception
      {
         String fromclient;
         String toclient;
          
         InetAddress ip;
   	  try {
    
   		ip = InetAddress.getLocalHost();
   		System.out.println("Current IP address : " + ip.getHostAddress());
    
   	  } catch (UnknownHostException e) {
    
   		  e.printStackTrace();
    
   	  }
    
         
         ServerSocket Server = new ServerSocket (5000);
         
         System.out.println ("TCPServer Waiting for client on port 5000");

         while(true) 
         {
         	Socket connected = Server.accept();
            System.out.println( " THE CLIENT"+" "+
            connected.getInetAddress() +":"+connected.getPort()+" IS CONNECTED ");
            DataInputStream dIn = new DataInputStream(connected.getInputStream());  
           
            
            while ( true )
            {	     
            	int length = dIn.readInt();                    // read length of incoming message
            	if(length>0) {
            	    byte[] message = new byte[length];
            	    dIn.readFully(message, 0, message.length); // read the message
            	    ByteArrayInputStream bais = new ByteArrayInputStream(message);
        			ObjectInputStream ois = new ObjectInputStream(bais);
        			ObjectCluster rxojc =  (ObjectCluster) ois.readObject();
        			System.out.print(rxojc.mMyName + "\n");
            	}
            	
            	
            	
			}  
			
          }
      }
}