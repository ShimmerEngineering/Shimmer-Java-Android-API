This has been updated just to test the feasibility of using the Shimmer API. Test looks promising, but no validation was done.

Steps to run the examples
1. Right click project folder > configure > add gradle nature
2. Include liblsl64.dll into your application's root directory, or a system folder
3. Include jna-{version}.jar in the build path
4. Right click src folder > build path > use as source folder
5. Make sure the jna-{version}.jar is included in the build path
6. Make sure sensor Accel is enabled on your shimmer device (it can be enabled using Consensys)
7. Update the btComport variable in SendData.java (You can find the Comport information in device manager or using Consensys)
8. Run SendData to create a stream outlet and make the streaming data available for other platforms
9. Make sure the stream type in both SendData and ReceiveData are the same
10. Run ReceiveData to retrieve the streaming data
