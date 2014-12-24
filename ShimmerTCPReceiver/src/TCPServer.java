//TCPServer.java

import java.io.*;
import java.net.*;

class TCPServer 
{
   public static void main(String argv[]) throws Exception
      {
         String fromclient;
         String toclient;
          
         ServerSocket Server = new ServerSocket (5000);
         
         System.out.println ("TCPServer Waiting for client on port 5000");

         while(true) 
         {
         	Socket connected = Server.accept();
            System.out.println( " THE CLIENT"+" "+
            connected.getInetAddress() +":"+connected.getPort()+" IS CONNECTED ");
            
               
            BufferedReader inFromClient =
               new BufferedReader(new InputStreamReader (connected.getInputStream()));
                  
           
            
            while ( true )
            {	           	
            	fromclient = inFromClient.readLine();
            	System.out.println( "RECIEVED:" + fromclient );
			}  
			
          }
      }
}