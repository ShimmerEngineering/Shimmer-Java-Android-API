package com.shimmerresearch.bluetooth;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Test;

import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.pcDriver.ShimmerPC;
import com.shimmerresearch.shimmer3.communication.ByteCommunicationSimulatorS3;
import com.shimmerresearch.shimmer3.communication.ByteCommunicationTestUtils;

/**
 * DEV-1310: ShimmerPC.deepClone() copies a device by serialising it, and Consensys's
 * Bluetooth manager configures a connected device through such a copy. Serialisation
 * takes no lock, so on a connected device the timers and the IO thread went on changing
 * its instruction list while it was copied: the status timer queues GET_STATUS every
 * 5 s, the battery timer queues a battery read, and the IO thread removes each
 * instruction it sends. A change mid-copy threw ConcurrentModificationException out of
 * deepClone(), and the configure failed. The Java HIL saw it about once in 30 configures.
 * <p>
 * A clone is a copy of the configuration, not of the connection, so it now starts with
 * none of the connection's queues.
 */
public class API_00034_DeepCloneWhileBusyTest {

	private static final int CLONES = 200;

	private ShimmerPC mDevice;

	@After
	public void tearDown() {
		if(mDevice!=null){
			mDevice.disconnectNoException();
		}
	}

	/**
	 * Another thread queues an instruction the way a timer does (readBattery() takes the
	 * device, then the list) and retires one the way the IO thread does, while this one
	 * copies the device. A long queue keeps the copy of the list busy for long enough that
	 * nearly every clone overlaps a change.
	 */
	@Test
	public void clonesWhileATimerQueuesInstructions() throws Exception {
		ShimmerPC device = new ShimmerPC("COM99");
		for(int i=0;i<2000;i++){
			device.readBattery();
		}
		AtomicBoolean stop = new AtomicBoolean();
		Thread timer = daemon("timer", () -> {
			while(!stop.get()){
				device.readBattery();
				device.removeInstruction(0);
			}
		});
		timer.start();
		try {
			for(int i=1;i<=CLONES;i++){
				ShimmerPC clone = device.deepClone();
				assertNotNull("clone " + i, clone);
				assertTrue("clone " + i + " carries no instructions", clone.getListofInstructions().isEmpty());
			}
		} finally {
			stop.set(true);
			timer.join(5000);
		}
		assertFalse("the queue was not copied away from the device", device.getListofInstructions().isEmpty());
	}

	/**
	 * The product as it runs: a device connected to the simulator with its status timer on,
	 * an app thread reading the battery and status as Consensys does, and clones taken all
	 * the while. Each clone must be usable to configure from.
	 */
	@Test
	public void clonesAConnectedDeviceWhileItWorksThroughItsQueue() throws Exception {
		mDevice = new ShimmerPC("COM99");
		mDevice.setTestRadio(new ByteCommunicationSimulatorS3("COM99"));
		ByteCommunicationTestUtils.connectAndWaitUntilInitialised(mDevice);
		assertEquals(BT_STATE.CONNECTED, mDevice.getBluetoothRadioState());

		AtomicBoolean stop = new AtomicBoolean();
		AtomicReference<Throwable> appFailure = new AtomicReference<>();
		Thread app = daemon("app", () -> {
			try {
				while(!stop.get()){
					if(mDevice.getListofInstructions().size()<8){
						mDevice.readBattery();
						mDevice.readStatusLogAndStream();
					}
					Thread.yield();
				}
			} catch (Throwable t) {
				appFailure.set(t);
			}
		});
		app.start();
		try {
			for(int i=1;i<=CLONES;i++){
				ShimmerPC clone = mDevice.deepClone();
				assertNotNull("clone " + i, clone);
				assertTrue("clone " + i + " carries no instructions", clone.getListofInstructions().isEmpty());
				assertEquals("clone " + i + " has the device's rate", mDevice.getSamplingRateShimmer(), clone.getSamplingRateShimmer(), 0);
			}
		} finally {
			stop.set(true);
			app.join(5000);
		}
		assertNull("the app thread failed", appFailure.get());
	}

	private Thread daemon(String name, Runnable runnable) {
		Thread thread = new Thread(runnable, name);
		thread.setDaemon(true);
		return thread;
	}
}
