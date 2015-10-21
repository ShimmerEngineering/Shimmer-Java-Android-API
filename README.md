# Shimmer-Java-Android-API

21 October 2015
- Major updates to get API working with LogandStream 0.6 and BTStream 0.8 which uses a 3 byte time stamp
- New algorithm ecgtohradaptive added to ShimmerBiophysicalProcessingLibrary_Rev_0_10.jar, new algorithm is more accurate and efficient

4 June 2015 (Beta 2.10)
 - fixes to MultiShimmerTemaplate and ShimmerGraphandLogService (A0 and A7)
 - update to filter (BSF) --> coefficients[(nTaps/2)] = coefficients[(nTaps/2)] +1;

25 May 2015 (Beta 2.9)
 - fix to shimmersetsamplingrate method
 - clean up to tcpip example (ShimmerTCPExample/ShimmerPCTCPExample/ShimmerTCPReceiver)

20 May 2015 (Beta 2.8)
  - updated filter/ecgtohr/ppgtohr algorithms
  - ecgtohr algorithm examples added to MultiShimmerTemplate (Android) and ShimmerCapture (PC)
  - various bug fixes to ShimmerCapture and ShimmerConnect
  - updates to GSR coefficients
  
20 October 2014 (Beta 2.7)
- support for LogAndStream Firmware
- support for Baud Rate modification
- support for reading the expansion board
- minor bugs fixed
- moved logging and plotting functionality to the Android Instrument Driver.
- added functionalities for MSS API
04 July 2014 (Beta 2.6)
- Support for Shimmer3 bridge amplifier
- added getExGConfiguration methods
- New Shimmer initialize. Now: get HW Version -> get FW Version -> Initialization
- Firmware Version divide into Major Firmware Version and Minor Firmware Version
- Firmware code added in order to identify the features in the different firmwares
- Change Firmware Version checks, now is Firmware Code checks
- Configurable filter, see Filter.java : 1.Low Pass, 2.High Pass, 3.Band Pass, 4.Band Stop.
- Sensor conflict handling for Shimmer3
- Shimmer PPG->HR jar v0.4 
- Updated 3dorientation to work with wide range accel, defaults to low noise if both are enabled
- Added DataProcessing interface
- Moved response timer to ShimmerBluetooth
- Bug fix to allow continual use of gerdavax library if needed
- Added support for BlueCove, see ShimmerConnectBCove

23 Jan 2014 (Beta 2.2)
- Separate the drivers into Shimmer Android Instrument Driver and Shimmer Driver, please read user guide for further details.

15 March 2013 (Beta 0.7)
- a more accurate GSR calibration method is implemented. the polynomial equation is replaced with a linear one.
- the state STATE_CONNECTED is now deprecated. users should use MSG_STATE_FULLY_INITIALIZED as a replacement.
- note that since the Android 4.2 update, Google has changed the Bluetooth Stack. It has been well documented that the reliability of the stack is less than desirable.
- Battery voltage monitoring is now enabled, see manual guideline for further information.
- Definition of the properties, formats and units, used, have been updated to be more concise and consistent across the various instrument drivers
- For further changes please refer to the Shimmer.java file, which can be found as part of the instrument driver

27 September 2012 (Beta 0.6)
- Additional handler msgs for stop streaming has been added.
- An optional AndroidBluetoothLibrary has been added. Please see license. The reason for this is because some stock firmware were not providing the full bluetooth stack. If bluetooth problems persist please consider using an aftermarket firmware.
- The connect method has been modified to accomodate the new library. Examples have been updated as well.
- Object Cluster now accepts the bluetooth id of the device
- Stop Streaming Command which was failing to receive an ACK intermittently has been fixed.
- createInsecureRfcommSocketToServiceRecord can be used for insecure connections (only for the default library). Benefit is that you wont have to key in the pin everytime you connect to a Shimmer device, which is a requirment for some Android devices. 
- the 'Nan' bug has been fixed, this bug occurs when the Shimmer device attempts to use the default calibration parameters

18 July 2012 (Beta 0.5)
- ShimmerGraph has been updated to deal with the following warning message 'the following handler class should be static or leaks might occur'

18 July 2012 (Beta 0.4)
- Added com.shimmerresearch.tools which has a Logging class (logs data onto the Android device)
- Fixed a bug with function retrievecalibrationparametersfrompacket

10 July 2012 (Beta 0.3b)
- Added locks to ensure commands can only be transmitted after the previous one is finished
- Updated the command transmission timeout

4 July 2012 (Beta 0.3)
- Fix a bug with the start streaming command which was failing intermittently 
- Rename ShimmerHandleClass to Shimmer

26 June 2012 (Beta 0.2)
-Added additional log messages to clearly show the communication transactions between the Shimmer and Android device
-Added an addtional constructor, which allows the android device to set Shimmer settings as soon as connection is made
-Manual updated explaining communication transactions and the use of the constructors

 
