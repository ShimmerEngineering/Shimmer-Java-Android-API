package com.example.simmerradiotest;

import com.shimmerresearch.androidradiodriver.ShimmerSerialPortAndroid;
import com.shimmerresearch.bluetooth.ShimmerRadioProtocol;
import com.shimmerresearch.comms.radioProtocol.LiteProtocol;
import com.shimmerresearch.comms.radioProtocol.RadioListener;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet;
import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.driver.Shimmer4Test;
import com.shimmerresearch.driver.UtilShimmer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	ShimmerRadioProtocol mShimmerRadioProtocol;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mShimmerRadioProtocol = new ShimmerRadioProtocol(new ShimmerSerialPortAndroid("00:06:66:66:96:86"),new LiteProtocol());
		try {
			mShimmerRadioProtocol.connect();
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Button btnAccelRange = (Button) this.findViewById(R.id.btnReadAccelRange);
		
		btnAccelRange.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte[] ins = new byte[1];
				ins[0] = LiteProtocolInstructionSet.InstructionsGet.GET_ACCEL_SENSITIVITY_COMMAND_VALUE;
				mShimmerRadioProtocol.mRadioProtocol.writeInstruction(ins);
			}});
		mShimmerRadioProtocol.setRadioListener(new RadioListener(){

    		@Override
    		public void connected() {
    			// TODO Auto-generated method stub

    			mShimmerRadioProtocol.mRadioProtocol.initialize();

    			//Read infomem, and set packetsize
    			//mSRP.mRadioProtocol.writeInstruction(new byte[]{(byte) LiteProtocolInstructionSet.Instructions.GET_INFOMEM_COMMAND_VALUE});

    			mShimmerRadioProtocol.mRadioProtocol.setPacketSize(41);
    		}

    		@Override
    		public void disconnected() {
    			// TODO Auto-generated method stub

    		}

    		@Override
    		public void eventNewPacket(byte[] pbA) {
    			// TODO Auto-generated method stub
    			System.out.println("New Packet: " + UtilShimmer.bytesToHexString(pbA));
    			
    		}

    		@Override
    		public void eventResponseReceived(byte[] responseBytes) {
    			// TODO Auto-generated method stub
    			// TODO Auto-generated method stub
    			System.out.println("Response Received: " + UtilShimmer.bytesToHexString(responseBytes));
    			
    		}

    		@Override
    		public void eventAckReceived(byte[] instructionSent) {
    			// TODO Auto-generated method stub
    			// TODO Auto-generated method stub
    			System.out.println("Ack Received: " + UtilShimmer.bytesToHexString(instructionSent));
    			
    		}
    	});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
