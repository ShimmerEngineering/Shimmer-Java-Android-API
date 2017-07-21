Java Shimmer Connect

February 2015
- Found a bottleneck in performance in the plotting function of ShimmerConnect and ShimmerCapture (1024Hz, 6 Channels)

September 2014
- Added support for LogandStream

July 2014
Added support for use with BlueCove http://bluecove.org/
- The blueooth address of the device should be specified as follows btspp://000666669686:1 , where 000666669686 is the Bluetooth address
- Note that BlueCove has other useful Bluetooth functions such as Bluetooth Scanning
- Also note that using BlueCove on linux will require downloading additional libraries bluecove-gpl and bluecove (need to replace the snapshot library) from their site http://sourceforge.net/projects/bluecove/files/BlueCove/2.1.0/
- Note that the Linux library is licensed under a GPL license

March 2014
Connects a single Shimmer, plots raw or calibrated data and logs data. 
- Uses ShimmerPCInstrument Driver - implements driver's call back method
- Fails to connect to Shimmer if not properly disconnected the previous time. Need to restart app in this case
	