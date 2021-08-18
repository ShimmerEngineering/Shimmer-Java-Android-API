package com.shimmerresearch.driver.ble;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.util.encoders.Hex;

import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.driverUtilities.ByteUtils;

public class VerisenseProtocolByteCommunication {
	
	byte[] ReadStatusRequest = new byte[] { 0x11, 0x00, 0x00 };
    byte[] ReadDataRequest = new byte[] { 0x12, 0x00, 0x00 };
    byte[] StreamDataRequest = new byte[] { 0x2A, 0x01, 0x00, 0x01 };
    byte[] StopStreamRequest = new byte[] { 0x2A, 0x01, 0x00, 0x02 };
    byte[] ReadProdConfigRequest = new byte[] { 0x13, 0x00, 0x00 };
    byte[] ReadOpConfigRequest = new byte[] { 0x14, 0x00, 0x00 };
    byte[] ReadTimeRequest = new byte[] { 0x15, 0x00, 0x00 };
    byte[] ReadPendingEventsRequest = new byte[] { 0x17, 0x00, 0x00 };
    byte[] DFUCommand = new byte[] { 0x26, 0x00, 0x00 };
    byte[] DisconnectRequest = new byte[] { 0x2B, 0x00, 0x00 };
    
	StatusPayload mStatusPayload;
	boolean mNewStreamPayload;
	boolean WaitingForStopStreamingCommand = false;
	public transient List<RadioListener> mRadioListenerList = new ArrayList<RadioListener>();
	public DataChunkNew DataCommandBuffer;
	public DataChunkNew DataStreamingBuffer;
	
	public enum VerisenseProtocolState
    {
        None,
        Disconnected,
        Connecting,
        Connected,
        Streaming,
        StreamingLoggedData,
        Limited
    }
	
	VerisenseProtocolState mState = VerisenseProtocolState.None;
	boolean mNewCommandPayload = false;
	AbstractByteCommunication mByteCommunication;
	
	public void addRadioListener(RadioListener radioListener){
		mRadioListenerList.add(radioListener);
	}
	
	protected void stateChange(VerisenseProtocolState state) {
		System.out.println("State Change: " + state.toString());
		mState = state;
	}
	
	public void removeRadioListenerList(){
		mRadioListenerList.clear();
	}
	
	public VerisenseProtocolByteCommunication(AbstractByteCommunication byteComm) {
		mByteCommunication = byteComm;
		byteComm.setByteCommunicationListener(new ByteCommunicationListener() {
			
			@Override
			public void eventNewBytesReceived(byte[] rxBytes) {
				// TODO Auto-generated method stub
				System.out.println("PROTOCOL EVENT BYTES" + Hex.toHexString(rxBytes));
				
				 if (mState.equals(VerisenseProtocolState.StreamingLoggedData))
	             {
	                
	             } else if (mState.equals(VerisenseProtocolState.Streaming)) {
	            	 
	            	//System.Console.WriteLine("STREAMING DATA (" + bytes.Length + ") :" + String.Join(" ", bytes));
	                    if (rxBytes.length == 3 && rxBytes[0] == 74)//4A 00 00
	                    {
	                        if (WaitingForStopStreamingCommand)
	                        {
	                            handleCommonResponse(rxBytes);
	                            WaitingForStopStreamingCommand = false;
	                            return;
	                        }
	                        else
	                        {
	                            stateChange(VerisenseProtocolState.Streaming);
	                            return;
	                        }
	                    }

	                    if (mNewStreamPayload)
	                    {
	                        createNewStreamPayload(rxBytes);
	                    }
	                    else
	                    {
	                        handleStreamDataChunk(rxBytes);
	                    }

	                    return;
	            	 
	             } else {
	            	 if (rxBytes.length == 3 && rxBytes[0] >> 4 == 4)//if it is an ack
	                 {
	                        handleCommonResponse(rxBytes);
	                 }
	                 else
	                 {
	                	 if (mNewCommandPayload)
	                	 {
	                		 createNewCommandPayload(rxBytes);
	                	 }
	                	 else
	                     {
	                         handleCommandDataChunk(rxBytes);
	                     }
	                 }
	             }
				
			}
			
			@Override
			public void eventDisconnected() {
				// TODO Auto-generated method stub
				System.out.println("PROTOCOL DISCONNECTED");
				stateChange(VerisenseProtocolState.Disconnected);
			}
			
			@Override
			public void eventConnected() {
				// TODO Auto-generated method stub
				System.out.println("PROTOCOL CONNECTED");
				stateChange(VerisenseProtocolState.Connected);
			}
		});
	}
	
	
    void handleCommandDataChunk(byte[] payload)
    {
        try
        {
        	System.arraycopy(payload, 0, DataCommandBuffer.mPackets, DataCommandBuffer.mCurrentLength, payload.length);
            DataCommandBuffer.mCurrentLength += payload.length;

            //JC: This causes too many msgs in the logs we need a better implementation of this, maybe just logs last 10 msgs if a failure occurs
            //AutoSyncLogger.AddLog(LogObject, "PayloadChunk", string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}",
            //    payload.Length, DataBuffer.CurrentLength, DataBuffer.ExpectedLength), ASMName);
            //InternalWriteConsoleAndLog("Payload Chunk", string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}", payload.Length, DataBuffer.CurrentLength, DataBuffer.ExpectedLength));
            //FinalChunkLogMsgForNack = string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}", payload.Length, DataCommandBuffer.CurrentLength, DataCommandBuffer.ExpectedLength);
            if (DataCommandBuffer.mCurrentLength >= DataCommandBuffer.mExpectedLength) //Note: for readability it would be better for this to be == because > will cause the crc to fail anyway via a Buffer.BlockCopy exception e.g. "System.ArgumentException","Message":"Offset and length were out of bounds for the array or count is greater than the number of elements from index to the end of the source collection."
            {
                handleCompleteCommandPayload();
            }
        }
        catch (Exception ex)
        {
            //SendDataNACK(false);
            //AdvanceLog(LogObject, "HandleDataChunk", ex, ASMName);
        }
    }
	
	void createNewCommandPayload(byte[] payload)
    {
        try
        {
            byte header = payload[0];

            byte[] lengthBytes = new byte[2];
            System.arraycopy(payload, 1, lengthBytes, 0, 2);
            
            int length = (int)ByteUtils.bytesToShort(lengthBytes, ByteOrder.LITTLE_ENDIAN);
            int offset = 3;
            DataCommandBuffer = new DataChunkNew(length+offset);
            DataCommandBuffer.mExpectedLength = length+offset;
            //var sensorid = reader.ReadByte();
            //var tickBytes = reader.ReadBytes(3);
            //var bArray = addByteToArray(tickBytes, 0);
            //var tick = BitConverter.ToUInt32(bArray, 0);
            byte[] remainingBytes = new byte[payload.length-offset];
            System.arraycopy(payload, 3, remainingBytes, 0, payload.length-offset);
            byte[] offsetBytes = new byte[] { header, lengthBytes[0], lengthBytes[1]};
            byte[] remainingBytesWStartingOffset = new byte[remainingBytes.length+offset];
            remainingBytesWStartingOffset[0] = header;
            remainingBytesWStartingOffset[1] = lengthBytes[0];
            remainingBytesWStartingOffset[2] = lengthBytes[1];

            System.arraycopy(remainingBytes, 0, remainingBytesWStartingOffset, 3, remainingBytes.length);
            System.arraycopy(remainingBytesWStartingOffset, 0, DataCommandBuffer.mPackets, DataCommandBuffer.mCurrentLength, remainingBytesWStartingOffset.length);

            DataCommandBuffer.mCurrentLength += remainingBytesWStartingOffset.length;
            mNewCommandPayload = false;

            //AdvanceLog(LogObject, "PayloadIndex", string.Format("Payload Index = {0}; Expected Length = {1}", PayloadIndex, DataCommandBuffer.ExpectedLength), ASMName);

            if (DataCommandBuffer.mCurrentLength > DataCommandBuffer.mExpectedLength)
            {
                //AdvanceLog(LogObject, "CreateNewPayload", "Error Current Length: " + DataCommandBuffer.CurrentLength + " bigger than Expected Length: " + DataCommandBuffer.ExpectedLength, ASMName);
                //SendDataNACK(true);
            }
            else if (DataCommandBuffer.mCurrentLength == DataCommandBuffer.mExpectedLength) //this might occur if the payload length is very small
            {
                //AdvanceLog(LogObject, "CreateNewPayload", "HandleCompleteStreamingPayload", ASMName);
                handleCompleteCommandPayload();
            }
        }
        catch (Exception ex)
        {
            //AdvanceLog(LogObject, "CreateChunk Exception", ex, ASMName);
            //SendDataNACK(false);
            return;
        }

    }
	
	public void handleCompleteCommandPayload() {
        mNewCommandPayload = true;
        handleCommonResponse(DataCommandBuffer.mPackets);
        DataCommandBuffer = new DataChunkNew();
	}
	
	void handleCommonResponse(byte [] ResponseBuffer)
    {
        try
        {
            switch (ResponseBuffer[0])
            {
                case 0x31:
                    StatusPayload statusData = new StatusPayload();
                    boolean statusResult = statusData.ProcessPayload(ResponseBuffer);
                    if (statusResult)
                    {
                    	mStatusPayload = statusData;
                    }
                    else
                    {
                        
                    }

                    break;
                case 0x4A:
                    //var baseDataSS = new BasePayload();
                    //var baseResultSS = baseDataSS.ProcessPayload(ResponseBuffer);
                    if (mState.equals(VerisenseProtocolState.Streaming))
                    {
                        stateChange(VerisenseProtocolState.Connected);
                    } else {
                    	mNewStreamPayload = true;
                    	stateChange(VerisenseProtocolState.Streaming);
                    }
                    break;
                default:
                    //AdvanceLog(LogObject, "NonDataResponse", BitConverter.ToString(ResponseBuffer), ASMName);
                    throw new Exception();
            };
        }
        catch (Exception ex)
        {
            
        }
    }
	
	
	void createNewStreamPayload(byte[] payload)
    {
        try
        {
            //AutoSyncLogger.AddLog(LogObject, "NewPayloadHead", BitConverter.ToString(payload), ASMName);

        	byte header = payload[0];

            byte[] lengthBytes = new byte[2];
            System.arraycopy(payload, 1, lengthBytes, 0, 2);
            
            int length = (int)ByteUtils.bytesToShort(lengthBytes, ByteOrder.LITTLE_ENDIAN);
            int offset = 3;
        	
            DataStreamingBuffer = new DataChunkNew(length);
            DataStreamingBuffer.mExpectedLength = length;
            //var sensorid = reader.ReadByte();
            //var tickBytes = reader.ReadBytes(3);
            //var bArray = addByteToArray(tickBytes, 0);
            //var tick = BitConverter.ToUInt32(bArray, 0);
            byte[] remainingBytes = new byte[payload.length-offset];
            System.arraycopy(payload, 3, remainingBytes, 0, payload.length-offset);
            System.arraycopy(remainingBytes, 0, DataStreamingBuffer.mPackets, DataStreamingBuffer.mCurrentLength, remainingBytes.length);

            DataStreamingBuffer.mCurrentLength += remainingBytes.length;
            mNewStreamPayload = false;

            //AdvanceLog(LogObject, "PayloadIndex", string.Format("Payload Index = {0}; Expected Length = {1}", PayloadIndex, DataStreamingBuffer.ExpectedLength), ASMName);

            if (DataStreamingBuffer.mCurrentLength > DataStreamingBuffer.mExpectedLength)
            {
                //AdvanceLog(LogObject, "CreateNewPayload", "Error Current Length: " + DataStreamingBuffer.mCurrentLength + " bigger than Expected Length: " + DataStreamingBuffer.ExpectedLength, ASMName);
                //SendDataNACK(true);
            }
            else if (DataStreamingBuffer.mCurrentLength == DataStreamingBuffer.mExpectedLength) //this might occur if the payload length is very small
            {
                //AdvanceLog(LogObject, "CreateNewPayload", "HandleCompleteStreamingPayload", ASMName);
                handleCompleteStreamingPayload();
            }
        }
        catch (Exception ex)
        {
            //AdvanceLog(LogObject, "CreateChunk Exception", ex, ASMName);

            //SendDataNACK(false);
            return;
        }

    }
	
	void handleStreamDataChunk(byte[] payload)
    {
        try
        {
            System.arraycopy(payload, 0, DataStreamingBuffer.mPackets, DataStreamingBuffer.mCurrentLength, payload.length);
            DataStreamingBuffer.mCurrentLength += payload.length;

            //JC: This causes too many msgs in the logs we need a better implementation of this, maybe just logs last 10 msgs if a failure occurs
            //AutoSyncLogger.AddLog(LogObject, "PayloadChunk", string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}",
            //    payload.Length, DataBuffer.CurrentLength, DataBuffer.ExpectedLength), ASMName);
            //InternalWriteConsoleAndLog("Payload Chunk", string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}", payload.Length, DataBuffer.CurrentLength, DataBuffer.ExpectedLength));
            //FinalChunkLogMsgForNack = string.Format("Chunk length = {0}; Current Length = {1}; Expected Length={2}", payload.Length, DataStreamingBuffer.CurrentLength, DataStreamingBuffer.ExpectedLength);
            if (DataStreamingBuffer.mCurrentLength >= DataStreamingBuffer.mExpectedLength) //Note: for readability it would be better for this to be == because > will cause the crc to fail anyway via a Buffer.BlockCopy exception e.g. "System.ArgumentException","Message":"Offset and length were out of bounds for the array or count is greater than the number of elements from index to the end of the source collection."
            {
                handleCompleteStreamingPayload();
            }
        }
        catch (Exception ex)
        {
            //SendDataNACK(false);
            //AdvanceLog(LogObject, "HandleDataChunk", ex, ASMName);
        }
    }
	
    void handleCompleteStreamingPayload()
    {
        mNewStreamPayload = true;
        //parseStreamingPayload();
        System.out.println("New Streaming Payload: " + System.currentTimeMillis());
        for (RadioListener rl:mRadioListenerList){
			rl.eventNewPacket(DataStreamingBuffer.mPackets, System.currentTimeMillis());
		}

        DataStreamingBuffer = new DataChunkNew();
    }
    
	public void connect() {
		mByteCommunication.connect();
	}
	
	public void disconnect() {
		mByteCommunication.disconnect();
	}
	
	public void readStatus() {
		mNewCommandPayload = true;
		mByteCommunication.writeBytes(ReadStatusRequest);
	}
	
	public void startStreaming() {
		mByteCommunication.writeBytes(StreamDataRequest);
	}
	
	public void readOpConfig() {
		
	}
	
	public void stopStreaming() {
		WaitingForStopStreamingCommand = true;
		mByteCommunication.writeBytes(StopStreamRequest);
	}
}
