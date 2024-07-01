package com.shimmerresearch.grpc;

import javax.swing.JFrame;

import com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply;
import com.shimmerresearch.grpc.ShimmerBLEGRPC.Request;
import com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus;
import com.google.protobuf.ByteString;
import com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray;
import com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest;
import com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes;

import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class grpctest {
	static long ct1 = System.currentTimeMillis();
	static long ct2 = System.currentTimeMillis();
	static long ct3 = System.currentTimeMillis();
	static long ct4 = System.currentTimeMillis();
	
	public static String byteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            // Convert each byte to a two-digit hexadecimal representation
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }
	
	static ShimmerBLEByteServerGrpc.ShimmerBLEByteServerBlockingStub blockingStub;
	static ShimmerBLEByteServerGrpc.ShimmerBLEByteServerBlockingStub blockingStub1;
	static ShimmerBLEByteServerGrpc.ShimmerBLEByteServerBlockingStub blockingStub2;
	static ShimmerBLEByteServerGrpc.ShimmerBLEByteServerBlockingStub blockingStub3;
	static ShimmerBLEByteServerGrpc.ShimmerBLEByteServerBlockingStub blockingStub4;
	static ManagedChannel channel;
	static ManagedChannel channel1;
	static ManagedChannel channel2;
	static ManagedChannel channel3;
	static ManagedChannel channel4;
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(300, 300);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton btnNewButton = new JButton("Hello");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Create a request message
	            Request request = Request.newBuilder().setName("John").build();

	            // Call the remote gRPC service method
	            Reply response = blockingStub.sayHello(request);

	            // Process the response
	            System.out.println("Received: " + response.getMessage());
			}
		});
		btnNewButton.setBounds(10, 11, 89, 23);
		frame.getContentPane().add(btnNewButton);
		
		JButton btnTestStream = new JButton("Test Stream");
		btnTestStream.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 try {
			            
			            
			            ShimmerBLEByteServerGrpc.ShimmerBLEByteServerStub stub = ShimmerBLEByteServerGrpc.newStub(channel1);
			            StreamRequest sreq = StreamRequest.newBuilder().setMessage("1").build();
			            
			            StreamObserver<ObjectClusterByteArray> responseObserver = new StreamObserver<ObjectClusterByteArray>() {

							@Override
							public void onNext(ObjectClusterByteArray value) {
								// TODO Auto-generated method stub
								if (value.getSystemTime()%1000==0) {
									long nt = System.currentTimeMillis();
									System.out.println(value.getBluetoothAddress() + "  elapsed time:" + (nt-ct1)+ " Packet Number: " + value.getSystemTime() + " values : " + byteArrayToHexString(value.getBinaryData().toByteArray()));
									ct1=nt;
								}
							}

							@Override
							public void onError(Throwable t) {
								// TODO Auto-generated method stub
								System.out.println("error");
							}

							@Override
							public void onCompleted() {
								// TODO Auto-generated method stub
								System.out.println("completed");
							}
			            };
			            stub.getTestDataStream(sreq, responseObserver);
			            
			            StreamRequest sreq2 = StreamRequest.newBuilder().setMessage("2").build();
			            
			            StreamObserver<ObjectClusterByteArray> responseObserver2 = new StreamObserver<ObjectClusterByteArray>() {

							@Override
							public void onNext(ObjectClusterByteArray value) {
								// TODO Auto-generated method stub
								if (value.getSystemTime()%1000==0) {
									long nt = System.currentTimeMillis();
									System.out.println(value.getBluetoothAddress() + "  elapsed time:" + (nt-ct2)+ " Packet Number: " + value.getSystemTime() + " values : " + byteArrayToHexString(value.getBinaryData().toByteArray()));
									ct2=nt;
								}
							}

							@Override
							public void onError(Throwable t) {
								// TODO Auto-generated method stub
								System.out.println("error");
							}

							@Override
							public void onCompleted() {
								// TODO Auto-generated method stub
								System.out.println("completed");
							}
			            };
			            ShimmerBLEByteServerGrpc.ShimmerBLEByteServerStub stub2 = ShimmerBLEByteServerGrpc.newStub(channel2);
			            stub2.getTestDataStream(sreq2, responseObserver2);
			            
			StreamRequest sreq3 = StreamRequest.newBuilder().setMessage("3").build();
			            
			            StreamObserver<ObjectClusterByteArray> responseObserver3 = new StreamObserver<ObjectClusterByteArray>() {

							@Override
							public void onNext(ObjectClusterByteArray value) {
								// TODO Auto-generated method stub
								if (value.getSystemTime()%1000==0) {
									long nt = System.currentTimeMillis();
									System.out.println(value.getBluetoothAddress() + "  elapsed time:" + (nt-ct3)+ " Packet Number: " + value.getSystemTime() + " values : " + byteArrayToHexString(value.getBinaryData().toByteArray()));
									ct3=nt;
								}
							}

							@Override
							public void onError(Throwable t) {
								// TODO Auto-generated method stub
								System.out.println("error");
							}

							@Override
							public void onCompleted() {
								// TODO Auto-generated method stub
								System.out.println("completed");
							}
			            };
			            ShimmerBLEByteServerGrpc.ShimmerBLEByteServerStub stub3 = ShimmerBLEByteServerGrpc.newStub(channel3);
			            stub3.getTestDataStream(sreq3, responseObserver3);
			            
			StreamRequest sreq4 = StreamRequest.newBuilder().setMessage("4").build();
			            
			            StreamObserver<ObjectClusterByteArray> responseObserver4 = new StreamObserver<ObjectClusterByteArray>() {

							@Override
							public void onNext(ObjectClusterByteArray value) {
								// TODO Auto-generated method stub
								if (value.getSystemTime()%1000==0) {
									long nt = System.currentTimeMillis();
									System.out.println(value.getBluetoothAddress() + "  elapsed time:" + (nt-ct4)+ " Packet Number: " + value.getSystemTime() + " values : " + byteArrayToHexString(value.getBinaryData().toByteArray()));
									ct4=nt;
								}
							}

							@Override
							public void onError(Throwable t) {
								// TODO Auto-generated method stub
								System.out.println("error");
							}

							@Override
							public void onCompleted() {
								// TODO Auto-generated method stub
								System.out.println("completed");
							}
			            };
			            ShimmerBLEByteServerGrpc.ShimmerBLEByteServerStub stub4 = ShimmerBLEByteServerGrpc.newStub(channel4);
			            stub4.getTestDataStream(sreq4, responseObserver4);
			        } catch (StatusRuntimeException se) {
			            System.err.println("RPC failed: " + se.getStatus());
			        } finally {
			            // Shutdown the channel when done
//			            channel.shutdown();
			        }
			}
		});
		btnTestStream.setBounds(10, 227, 89, 23);
		frame.getContentPane().add(btnTestStream);
		
		JButton button_1 = new JButton("Connect");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Create a request message
				//E8EB1B713E36
				//e7452c6d6f14
				Request request = Request.newBuilder().setName("E8EB1B713E36").build();

	            // Call the remote gRPC service method
	            //Reply response = blockingStub.connectShimmer(request);

	            // Process the response
	            //System.out.println("Received: " + response.getMessage());
	            
	            ShimmerBLEByteServerGrpc.ShimmerBLEByteServerStub stub = ShimmerBLEByteServerGrpc.newStub(channel);
	    		//Request request = Request.newBuilder().setName(mMacAddress).build();

	    		StreamObserver<StateStatus> responseObserverState = new StreamObserver<StateStatus>() {

	    			@Override
	    			public void onNext(StateStatus value) {
	    				// TODO Auto-generated method stub
	    				
	    			}

	    			@Override
	    			public void onError(Throwable t) {
	    				// TODO Auto-generated method stub
	    				
	    			}

	    			@Override
	    			public void onCompleted() {
	    				// TODO Auto-generated method stub
	    				
	    			}
	    			
	    		};
	    		stub.connectShimmer(request, responseObserverState);
			}
		});
		

		
		button_1.setBounds(10, 45, 89, 23);
		frame.getContentPane().add(button_1);
		
		JButton btnNewButton_1 = new JButton("Disconnect");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Create a request message
				Request request = Request.newBuilder().setName("E8EB1B713E36").build();

	            // Call the remote gRPC service method
	            Reply response = blockingStub.disconnectShimmer(request);

	            // Process the response
	            System.out.println("Received: " + response.getMessage());
			}
		});
		btnNewButton_1.setBounds(10, 74, 89, 23);
		frame.getContentPane().add(btnNewButton_1);
		
		JButton button = new JButton("Connect2");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// Create a request message
				//E8EB1B713E36
				//e7452c6d6f14
				Request request = Request.newBuilder().setName("e7452c6d6f14").build();

				// Call the remote gRPC service method
	            //Reply response = blockingStub.connectShimmer(request);

	            // Process the response
	            //System.out.println("Received: " + response.getMessage());
	            
	            ShimmerBLEByteServerGrpc.ShimmerBLEByteServerStub stub = ShimmerBLEByteServerGrpc.newStub(channel);
	    		//Request request = Request.newBuilder().setName(mMacAddress).build();

	    		StreamObserver<StateStatus> responseObserverState = new StreamObserver<StateStatus>() {

	    			@Override
	    			public void onNext(StateStatus value) {
	    				// TODO Auto-generated method stub
	    				
	    			}

	    			@Override
	    			public void onError(Throwable t) {
	    				// TODO Auto-generated method stub
	    				
	    			}

	    			@Override
	    			public void onCompleted() {
	    				// TODO Auto-generated method stub
	    				
	    			}
	    			
	    		};
	    		stub.connectShimmer(request, responseObserverState);
			}
		});
		button.setBounds(110, 45, 89, 23);
		frame.getContentPane().add(button);
		
		JButton button_2 = new JButton("Disconnect2");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Create a request message
				Request request = Request.newBuilder().setName("e7452c6d6f14").build();

	            // Call the remote gRPC service method
	            Reply response = blockingStub.disconnectShimmer(request);

	            // Process the response
	            System.out.println("Received: " + response.getMessage());
			}
		});
		button_2.setBounds(109, 74, 90, 23);
		frame.getContentPane().add(button_2);
		
		JButton btnNewButton_2 = new JButton("Start Streaming");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				byte[] start = new byte[] {0x07};
				
				
				// Create a request message
				WriteBytes request = WriteBytes.newBuilder().setAddress("E8EB1B713E36").setByteToWrite(ByteString.copyFrom(start)).build();

	            // Call the remote gRPC service method
	            Reply response = blockingStub.writeBytesShimmer(request);

	            // Process the response
	            System.out.println("Received: " + response.getMessage());
			}
		});
		btnNewButton_2.setBounds(10, 108, 89, 23);
		frame.getContentPane().add(btnNewButton_2);
		
		JButton btnNewButton_3 = new JButton("Get Stream");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ShimmerBLEByteServerGrpc.ShimmerBLEByteServerStub stub = ShimmerBLEByteServerGrpc.newStub(channel);
	            StreamRequest sreq = StreamRequest.newBuilder().setMessage("E8EB1B713E36").build();
	            
	            StreamObserver<ObjectClusterByteArray> responseObserver = new StreamObserver<ObjectClusterByteArray>() {

					@Override
					public void onNext(ObjectClusterByteArray value) {
						// TODO Auto-generated method stub
						long nt = System.currentTimeMillis();
						System.out.println(value.getBluetoothAddress() + "  elapsed time:" + (nt-ct1)+ " Time Stamp: " + value.getSystemTime() + " values : " + byteArrayToHexString(value.getBinaryData().toByteArray()));
						ct1=nt;
					}

					@Override
					public void onError(Throwable t) {
						// TODO Auto-generated method stub
						System.out.println("error");
					}

					@Override
					public void onCompleted() {
						// TODO Auto-generated method stub
						System.out.println("completed");
					}
	            };
	            stub.getDataStream(sreq, responseObserver);
	            
			}
		});
		btnNewButton_3.setBounds(10, 142, 89, 23);
		frame.getContentPane().add(btnNewButton_3);
		// Define the server host and port
        String serverHost = "localhost";
        int serverPort = 50052;

        // Create a channel to connect to the server
        channel = ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext() // Use plaintext communication (insecure for testing)
                .build();
        // Create a gRPC client stub
    	blockingStub = ShimmerBLEByteServerGrpc.newBlockingStub(channel);
    	channel1= ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext() // Use plaintext communication (insecure for testing)
                .build();;
    	channel2= ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext() // Use plaintext communication (insecure for testing)
                .build();;
    	channel3= ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext() // Use plaintext communication (insecure for testing)
                .build();;
    	channel4= ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext() // Use plaintext communication (insecure for testing)
                .build();;
        
       
    }
}
