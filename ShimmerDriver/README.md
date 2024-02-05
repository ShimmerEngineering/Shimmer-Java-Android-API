# Shimmer Java API

The Shimmer Java API, is used for both PC and Android Shimmer APIs. 

The following git workflow is suggested for Shimmer team members
https://guides.github.com/introduction/flow/

For the following GRPC examples, you have to set the mExePath variable to the ShimmerBLEGRPC.exe server file in ShimmerDriver/src/main/java/com/shimmerresearch/grpc/GrpcBLERadioByteTools.java prior to use it.

1. ShimmerDriverPC/src/test/java/com/shimmerresearch/driver/ble/VerisenseProtocolByteGrpcCommunicationTest.java
2. ShimmerPCBasicExamples/src/main/java/com/shimmerresearch/simpleexamples/ShimmerGRPCExample.java

Make sure the ShimmerBLEGRPC.exe version (printed in the console when first running the examples) match the required version to use = v1.0.0 (can be found in https://github.com/ShimmerEngineering/Shimmer-C-API/releases/tag/BLE_GRPC_v1.0.0)






