package com.shimmerresearch.driver.ble;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.encoders.Hex;

public class StatusPayload {
	String Payload;
	String Header;
	int Length;
	public String ASMID ;
    public long StatusTimestamp ;
    public int BatteryLevel ;
    public int BatteryPercent ;
    public long TransferSuccessTimestamp ;
    public long TransferFailTimestamp ;
    public long BaseStationTimestamp ;
    public int FreeStorage ;
    public boolean IsSuccess ;
    public long VBattFallCounter ;
    public long StatusFlags;
    public boolean UsbPowered;
    public boolean RecordingPaused;
    public boolean FlashIsFull;
    public boolean PowerIsGood;
    public boolean AdaptiveScheduler;
    public boolean DfuServiceOn;
    public int SyncMode ;
    public long NextSyncAttemptTimestamp;
    public int StorageFull;
    public int StorageToDel;
    public int StorageBad;
    public final static long MaxFourByteUnsignedValue = 4294967295l; //2^32 -1
    public final static int SensorClockFrequency = 32768;
    private long ConvertMinuteToMS(long timestamp)
    {
        if (timestamp != MaxFourByteUnsignedValue) //special condition where the sensor/fw returns all FF values
        {
            timestamp = timestamp * 60 * 1000; //convert from minutes to milliseconds
        }
        else
        {
            timestamp = -1;
        }
        return timestamp;
    }
    private String reverse(String s)
    {
        char[] charArray = s.toCharArray();
        ArrayUtils.reverse(charArray);
        return new String(charArray);
    }
    
    private long ConvertTicksTomS(long ticks)
    {
        return (long)((ticks / (double)SensorClockFrequency) * 1000.0);
    }

    private long AppendToCurrentTimestamp(long timestamp, double durationToAppend)
    {
        return (long)(timestamp + durationToAppend);
    }

    public boolean ProcessPayload(byte[] response)
    {
        try
        {
            Payload = Hex.toHexString(response);
            ByteArrayInputStream input = new ByteArrayInputStream(response);
            InputStreamShimmer reader = new InputStreamShimmer(input);
            Header = Hex.toHexString(reader.readBytes(1));

            byte[] lengthBytes = reader.readBytes(2);
            ArrayUtils.reverse(lengthBytes);

            Length = Integer.parseInt(Hex.toHexString(lengthBytes).replace("-", ""), 16);
            
            byte[] idBytes = reader.readBytes(6);
            ArrayUtils.reverse(idBytes);
            ASMID = Hex.toHexString(idBytes).replace("-", "");
            
            byte[] tsBytes = reader.readBytes(4);
            ArrayUtils.reverse(tsBytes);
            StatusTimestamp = Long.parseLong(Hex.toHexString(tsBytes).replace("-", ""), 16);
            StatusTimestamp = ConvertMinuteToMS(StatusTimestamp);
            byte[] batteryBytes = reader.readBytes(2);
            ArrayUtils.reverse(batteryBytes);
            BatteryLevel = Integer.parseInt(Hex.toHexString(batteryBytes).replace("-", ""), 16);

            BatteryPercent = Integer.parseInt(Hex.toHexString(reader.readBytes(1)).replace("-", ""), 16);

            byte[] successBytes = reader.readBytes(4);
            ArrayUtils.reverse(successBytes);
            TransferSuccessTimestamp = Long.parseLong(Hex.toHexString(successBytes).replace("-", ""), 16);
            TransferSuccessTimestamp = ConvertMinuteToMS(TransferSuccessTimestamp);
            byte[] failBytes = reader.readBytes(4);
            ArrayUtils.reverse(failBytes);
            TransferFailTimestamp = Long.parseLong(Hex.toHexString(failBytes).replace("-", ""), 16);
            TransferFailTimestamp = ConvertMinuteToMS(TransferFailTimestamp);

            byte[] storageBytes = reader.readBytes(3);
            ArrayUtils.reverse(storageBytes);
            FreeStorage = Integer.parseInt(Hex.toHexString(storageBytes).replace("-", ""), 16);
            /* I am moving this to the UI level, because this values might be meaningful in the web DB
            if (FreeStorage > App.MaxSensorStorageCapacityKB)
            {
                FreeStorage = App.MaxSensorStorageCapacityKB;
            }
            */
            if (Length <= 24)    //old fw, no VBattFallCounter bytes 
            {
                VBattFallCounter = -1; //set to null because 0 can be a valid value
            }
            else
            {
                byte[] battFallBytes = reader.readBytes(2);
                ArrayUtils.reverse(battFallBytes);
                VBattFallCounter = Long.parseLong(Hex.toHexString(battFallBytes).replace("-", ""), 16);
            }


            if (Length > 26)  //new fw support StatusFlags bytes 
            {   
               
                byte[] statusFlagsBytes = reader.readBytes(8);
                ArrayUtils.reverse(statusFlagsBytes);
                //eg 0000000000000009 where 09 is the LSB (byte 26) will result in a StatusFlags value of 9
                StatusFlags = Long.parseLong(Hex.toHexString(statusFlagsBytes).replace("-", ""), 16);
                //reverse so the value of 9 00001001 will be 10010000 which is easier to read via index/table provided in the document ASM-DES04
                UsbPowered = ((statusFlagsBytes[7] & 1) > 0)?true:false;
                RecordingPaused = ((statusFlagsBytes[7] & 0b10) > 0) ? true : false;
                FlashIsFull = ((statusFlagsBytes[7] & 0b100) > 0) ? true : false;
                PowerIsGood = ((statusFlagsBytes[7] & 0b1000) > 0) ? true : false;
                AdaptiveScheduler = ((statusFlagsBytes[7] & 0b10000) > 0) ? true : false;
                DfuServiceOn = ((statusFlagsBytes[7] & 0b100000) > 0) ? true : false;
            }

            if (Length > 34)  //supported fw for ASM-1329
            {
                byte[] statusTimestampTicksBytes = reader.readBytes(3);
                ArrayUtils.reverse(statusTimestampTicksBytes);
                long statusTimestampTicks = Long.parseLong(Hex.toHexString(statusTimestampTicksBytes).replace("-", ""), 16);
                StatusTimestamp = AppendToCurrentTimestamp(StatusTimestamp, ConvertTicksTomS(statusTimestampTicks));

                byte[] transferSuccessTimestampTicksBytes = reader.readBytes(3);
                if (TransferSuccessTimestamp != -1)
                {
                    ArrayUtils.reverse(transferSuccessTimestampTicksBytes);
                    long transferSuccessTimestampTicks = Long.parseLong(Hex.toHexString(transferSuccessTimestampTicksBytes).replace("-", ""), 16);
                    TransferSuccessTimestamp = AppendToCurrentTimestamp(TransferSuccessTimestamp, ConvertTicksTomS(transferSuccessTimestampTicks));
                }

                byte[] transferFailTimestampTicksBytes = reader.readBytes(3);
                if(TransferFailTimestamp != -1)
                {
                    ArrayUtils.reverse(transferFailTimestampTicksBytes);
                    long transferFailTimestampTicks = Long.parseLong(Hex.toHexString(transferFailTimestampTicksBytes).replace("-", ""), 16);
                    TransferFailTimestamp = AppendToCurrentTimestamp(TransferFailTimestamp, ConvertTicksTomS(transferFailTimestampTicks));
                }

                byte[] nextSyncAttemptTimeBytes = reader.readBytes(4);
                ArrayUtils.reverse(nextSyncAttemptTimeBytes);
                NextSyncAttemptTimestamp = Long.parseLong(Hex.toHexString(nextSyncAttemptTimeBytes).replace("-", ""), 16);
                NextSyncAttemptTimestamp = ConvertMinuteToMS((long)NextSyncAttemptTimestamp);

                byte[] storageFullBytes = reader.readBytes(3);
                ArrayUtils.reverse(storageFullBytes);
                StorageFull = Integer.parseInt(Hex.toHexString(storageFullBytes).replace("-", ""), 16);
                /* I am moving this to the UI level, because this values might be meaningful in the web DB
                if (StorageFull > App.MaxSensorStorageCapacityKB)
                {
                    StorageFull = App.MaxSensorStorageCapacityKB;
                }*/
                byte[] storageToDelBytes = reader.readBytes(3);
                ArrayUtils.reverse(storageToDelBytes);
                StorageToDel = Integer.parseInt(Hex.toHexString(storageToDelBytes).replace("-", ""), 16);

                byte[] storageBadBytes = reader.readBytes(3);
                ArrayUtils.reverse(storageBadBytes);
                StorageBad = Integer.parseInt(Hex.toHexString(storageBadBytes).replace("-", ""), 16);
            }

            //SyncMode = (int)syncMode;
            //BaseStationTimestamp = DateHelper.GetTimestamp(DateTime.Now);



            IsSuccess = true;
        }
        catch (Exception ex)
        {
            //System.Console.WriteLine(ex.ToString());
        }

        return IsSuccess;
    }
}
