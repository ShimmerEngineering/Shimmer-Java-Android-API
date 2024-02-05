package com.shimmerresearch.grpc;

import javax.swing.JFrame;

import com.google.protobuf.ByteString;
import com.shimmerresearch.driverUtilities.BluetoothDeviceDetails;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.grpc.ShimmerBLEGRPC.BluetoothState;
import com.shimmerresearch.grpc.ShimmerBLEGRPC.ObjectClusterByteArray;
import com.shimmerresearch.grpc.ShimmerBLEGRPC.Reply;
import com.shimmerresearch.grpc.ShimmerBLEGRPC.Request;
import com.shimmerresearch.grpc.ShimmerBLEGRPC.StateStatus;
import com.shimmerresearch.grpc.ShimmerBLEGRPC.StreamRequest;
import com.shimmerresearch.grpc.ShimmerBLEGRPC.WriteBytes;
import com.shimmerresearch.verisense.communication.AbstractByteCommunication;

import bolts.TaskCompletionSource;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;
import java.awt.event.ActionEvent;

public class GrpcBLERadioByteCommunication extends AbstractByteCommunication {
	String mMacAddress;
	ShimmerBLEByteServerGrpc.ShimmerBLEByteServerBlockingStub blockingStub;
	ManagedChannel channel;;
	TaskCompletionSource<Boolean> mConnectTask = new TaskCompletionSource<>();
	static final byte[] ReadStatusRequest = new byte[] { 0x11, 0x00, 0x00 };
	long ct1 = System.currentTimeMillis();
	boolean debug = false;
    String mServerHost = "localhost";
    int mServerPort = 500052;
    
    
	/** Note that the server has to be started prior. See GrpcBLERadioByteTools
	 * @param macaddress the Shimmer3 BLE MAC address you want to connect to
	 * @param serverHost the GRPC server to be used for BLE communications
	 * @param serverPort the GRPC port to be used for BLE communications
	 */
	public GrpcBLERadioByteCommunication(String macaddress, String serverHost, int serverPort) {
		mServerHost = serverHost;
		mServerPort = serverPort;
		mMacAddress = macaddress.toUpperCase().replace(":", "");
		InitializeProcess();
	}

	public GrpcBLERadioByteCommunication(BluetoothDeviceDetails bdd, String serverHost, int serverPort) {
		mServerHost = serverHost;
		mServerPort = serverPort;
		mMacAddress = bdd.mComPort.toUpperCase().replace(":", "");
		InitializeProcess();
	}
	
	public void InitializeProcess() {
		// Define the server host and port


        // Create a channel to connect to the server
        channel = ManagedChannelBuilder.forAddress(mServerHost, mServerPort)
                .usePlaintext() // Use plaintext communication (insecure for testing)
                .build();

        // Create a gRPC client stub
    	blockingStub = ShimmerBLEByteServerGrpc.newBlockingStub(channel);
    	
    	
        
    	
	}
	
	public static void main(String[] args) {
		GrpcBLERadioByteCommunication ble = new GrpcBLERadioByteCommunication("e7452c6d6f14","localhost",50052);
		JFrame frame = new JFrame();
		frame.setSize(300, 300);
		frame.setVisible(true);
		frame.getContentPane().setLayout(null);
		
		JButton btnNewButton = new JButton("connect");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ble.connect();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnNewButton.setBounds(10, 11, 89, 23);
		frame.getContentPane().add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("read status");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ble.writeBytes(ReadStatusRequest);
			}
		});
		btnNewButton_1.setBounds(10, 45, 89, 23);
		frame.getContentPane().add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("disconnect");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ble.disconnect();
				} catch (ShimmerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnNewButton_2.setBounds(10, 79, 89, 23);
		frame.getContentPane().add(btnNewButton_2);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// TODO Auto-generated method stub
		//BTHLE\Dev_e7452c6d6f14
	}

	@Override
	public void connect() throws ShimmerException {
		// TODO Auto-generated method stub
		// Create a request message
		//E8EB1B713E36
		//e7452c6d6f14
		ShimmerBLEByteServerGrpc.ShimmerBLEByteServerStub stub = ShimmerBLEByteServerGrpc.newStub(channel);
		Request request = Request.newBuilder().setName(mMacAddress).build();
		mConnectTask = new TaskCompletionSource<>();
		

		StreamObserver<StateStatus> responseObserverState = new StreamObserver<StateStatus>() {

			@Override
			public void onNext(StateStatus value) {
				// TODO Auto-generated method stub
				System.out.println(value.getMessage() + " " + value.getState().toString());
				if (value.getState().equals(BluetoothState.Connected)) {
					mConnectTask.setResult(true);
			        
			        if (mByteCommunicationListener!=null) {
			        	mByteCommunicationListener.eventConnected();
			        }
				} else {
					mConnectTask.setResult(false);
				}
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
        // Call the remote gRPC service method
        //Reply response = blockingStub.connectShimmer(request);

        // Process the response
        //System.out.println("Received: " + response.getMessage());
        
        
		try {
			boolean result = mConnectTask.getTask().waitForCompletion(10, TimeUnit.SECONDS);
			if (result) {
				StreamRequest sreq = StreamRequest.newBuilder().setMessage(mMacAddress).build();
		        
		        StreamObserver<ObjectClusterByteArray> responseObserver = new StreamObserver<ObjectClusterByteArray>() {
		        	long numberOfBytes = 0;
		        	long st = 0;
					@Override
					public void onNext(ObjectClusterByteArray value) {
						// TODO Auto-generated method stub
						byte[] bytesData = value.getBinaryData().toByteArray();
						if (debug) {
							long nt = System.currentTimeMillis();
							if (st==0) {
								st=nt;
							}
							numberOfBytes += bytesData.length;
							long totalElapsed = nt-st;
							//System.out.println(value.getBluetoothAddress() + "  elapsed time:" + (nt-ct1)+ " # Bytes: " + bytesData.length + " Throughput: " + (bytesData.length*1000.0/(nt-ct1))/1024 + "KB/s values : " + byteArrayToHexString(bytesData));
							if(totalElapsed!=0) {
								System.out.println(totalElapsed + "   "+ numberOfBytes + "   " + "  Throughput: " + (numberOfBytes*1000.0/(totalElapsed))/1024 + "KB/s values");
							}
							ct1=nt;
						}
						if (mByteCommunicationListener!=null) {
							mByteCommunicationListener.eventNewBytesReceived(bytesData);
						}
					}

					@Override
					public void onError(Throwable t) {
						// TODO Auto-generated method stub
						System.out.println("error 1");
					}

					@Override
					public void onCompleted() {
						// TODO Auto-generated method stub
						System.out.println("completed");
					}
		        };
		        
		        //ShimmerBLEByteServerGrpc.ShimmerBLEByteServerStub stub = ShimmerBLEByteServerGrpc.newStub(channel);
		        stub.getDataStream(sreq, responseObserver);
		        
			} else {
				throw new ShimmerException("Connect Failed");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new ShimmerException("InterruptedException");
		}
        

	}

	@Override
	public void disconnect() throws ShimmerException {
		// TODO Auto-generated method stub
		// Create a request message
		Request request = Request.newBuilder().setName(mMacAddress).build();

        // Call the remote gRPC service method
        Reply response = blockingStub.disconnectShimmer(request);

        // Process the response
        System.out.println("Received: " + response.getMessage());
        if (mByteCommunicationListener!=null) {
        	mByteCommunicationListener.eventDisconnected();
        }
	}

	@Override
	public void writeBytes(byte[] bytes) {
		// TODO Auto-generated method stub
		
		// Create a request message
		WriteBytes request = WriteBytes.newBuilder().setAddress(mMacAddress).setByteToWrite(ByteString.copyFrom(bytes)).build();

        // Call the remote gRPC service method
        Reply response = blockingStub.writeBytesShimmer(request);

        // Process the response
        System.out.println("Received: " + response.getMessage());
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return mMacAddress;
	}
	
	public static String byteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            // Convert each byte to a two-digit hexadecimal representation
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }
}
