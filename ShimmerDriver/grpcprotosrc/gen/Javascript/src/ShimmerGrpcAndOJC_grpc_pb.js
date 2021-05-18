// GENERATED CODE -- DO NOT EDIT!

// Original file comments:
// import "C:/Users/Lim/git/Shimmer-Java-Android-API/ShimmerDriver/grpcprotosrc/src/LiteProtocolInstructionSet.proto";
//
'use strict';
var grpc = require('grpc');
var src_ShimmerGrpcAndOJC_pb = require('../src/ShimmerGrpcAndOJC_pb.js');

function serialize_shimmerGRPC_CommandStatus(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.CommandStatus)) {
    throw new Error('Expected argument of type shimmerGRPC.CommandStatus');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_CommandStatus(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.CommandStatus.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_DoubleMsg(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.DoubleMsg)) {
    throw new Error('Expected argument of type shimmerGRPC.DoubleMsg');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_DoubleMsg(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.DoubleMsg.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_EmulatedDevices(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.EmulatedDevices)) {
    throw new Error('Expected argument of type shimmerGRPC.EmulatedDevices');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_EmulatedDevices(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.EmulatedDevices.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_FileByteTransfer(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.FileByteTransfer)) {
    throw new Error('Expected argument of type shimmerGRPC.FileByteTransfer');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_FileByteTransfer(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.FileByteTransfer.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_HelloReply(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.HelloReply)) {
    throw new Error('Expected argument of type shimmerGRPC.HelloReply');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_HelloReply(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.HelloReply.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_HelloRequest(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.HelloRequest)) {
    throw new Error('Expected argument of type shimmerGRPC.HelloRequest');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_HelloRequest(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.HelloRequest.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_InfoSpans(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.InfoSpans)) {
    throw new Error('Expected argument of type shimmerGRPC.InfoSpans');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_InfoSpans(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.InfoSpans.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_ObjectCluster2(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.ObjectCluster2)) {
    throw new Error('Expected argument of type shimmerGRPC.ObjectCluster2');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_ObjectCluster2(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.ObjectCluster2.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_OperationRequest(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.OperationRequest)) {
    throw new Error('Expected argument of type shimmerGRPC.OperationRequest');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_OperationRequest(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.OperationRequest.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_ShimmerRequest(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.ShimmerRequest)) {
    throw new Error('Expected argument of type shimmerGRPC.ShimmerRequest');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_ShimmerRequest(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.ShimmerRequest.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_ShimmersInfo(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.ShimmersInfo)) {
    throw new Error('Expected argument of type shimmerGRPC.ShimmersInfo');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_ShimmersInfo(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.ShimmersInfo.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_StreamRequest(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.StreamRequest)) {
    throw new Error('Expected argument of type shimmerGRPC.StreamRequest');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_StreamRequest(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.StreamRequest.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_StringArrayMsg(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.StringArrayMsg)) {
    throw new Error('Expected argument of type shimmerGRPC.StringArrayMsg');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_StringArrayMsg(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.StringArrayMsg.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_shimmerGRPC_StringMsg(arg) {
  if (!(arg instanceof src_ShimmerGrpcAndOJC_pb.StringMsg)) {
    throw new Error('Expected argument of type shimmerGRPC.StringMsg');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_shimmerGRPC_StringMsg(buffer_arg) {
  return src_ShimmerGrpcAndOJC_pb.StringMsg.deserializeBinary(new Uint8Array(buffer_arg));
}


// The greeter service definition.
var ShimmerServerService = exports.ShimmerServerService = {
  // Sends a greeting
sayHello: {
    path: '/shimmerGRPC.ShimmerServer/SayHello',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.HelloRequest,
    responseType: src_ShimmerGrpcAndOJC_pb.HelloReply,
    requestSerialize: serialize_shimmerGRPC_HelloRequest,
    requestDeserialize: deserialize_shimmerGRPC_HelloRequest,
    responseSerialize: serialize_shimmerGRPC_HelloReply,
    responseDeserialize: deserialize_shimmerGRPC_HelloReply,
  },
  // Client asking for data
getDataStream: {
    path: '/shimmerGRPC.ShimmerServer/GetDataStream',
    requestStream: false,
    responseStream: true,
    requestType: src_ShimmerGrpcAndOJC_pb.StreamRequest,
    responseType: src_ShimmerGrpcAndOJC_pb.ObjectCluster2,
    requestSerialize: serialize_shimmerGRPC_StreamRequest,
    requestDeserialize: deserialize_shimmerGRPC_StreamRequest,
    responseSerialize: serialize_shimmerGRPC_ObjectCluster2,
    responseDeserialize: deserialize_shimmerGRPC_ObjectCluster2,
  },
  // Client sending data
sendDataStream: {
    path: '/shimmerGRPC.ShimmerServer/SendDataStream',
    requestStream: true,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.ObjectCluster2,
    responseType: src_ShimmerGrpcAndOJC_pb.HelloReply,
    requestSerialize: serialize_shimmerGRPC_ObjectCluster2,
    requestDeserialize: deserialize_shimmerGRPC_ObjectCluster2,
    responseSerialize: serialize_shimmerGRPC_HelloReply,
    responseDeserialize: deserialize_shimmerGRPC_HelloReply,
  },
  sendFileStream: {
    path: '/shimmerGRPC.ShimmerServer/SendFileStream',
    requestStream: true,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.FileByteTransfer,
    responseType: src_ShimmerGrpcAndOJC_pb.HelloReply,
    requestSerialize: serialize_shimmerGRPC_FileByteTransfer,
    requestDeserialize: deserialize_shimmerGRPC_FileByteTransfer,
    responseSerialize: serialize_shimmerGRPC_HelloReply,
    responseDeserialize: deserialize_shimmerGRPC_HelloReply,
  },
  connectShimmer: {
    path: '/shimmerGRPC.ShimmerServer/ConnectShimmer',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.ShimmerRequest,
    responseType: src_ShimmerGrpcAndOJC_pb.CommandStatus,
    requestSerialize: serialize_shimmerGRPC_ShimmerRequest,
    requestDeserialize: deserialize_shimmerGRPC_ShimmerRequest,
    responseSerialize: serialize_shimmerGRPC_CommandStatus,
    responseDeserialize: deserialize_shimmerGRPC_CommandStatus,
  },
  disconnectShimmer: {
    path: '/shimmerGRPC.ShimmerServer/DisconnectShimmer',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.ShimmerRequest,
    responseType: src_ShimmerGrpcAndOJC_pb.CommandStatus,
    requestSerialize: serialize_shimmerGRPC_ShimmerRequest,
    requestDeserialize: deserialize_shimmerGRPC_ShimmerRequest,
    responseSerialize: serialize_shimmerGRPC_CommandStatus,
    responseDeserialize: deserialize_shimmerGRPC_CommandStatus,
  },
  startStreaming: {
    path: '/shimmerGRPC.ShimmerServer/StartStreaming',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.ShimmerRequest,
    responseType: src_ShimmerGrpcAndOJC_pb.CommandStatus,
    requestSerialize: serialize_shimmerGRPC_ShimmerRequest,
    requestDeserialize: deserialize_shimmerGRPC_ShimmerRequest,
    responseSerialize: serialize_shimmerGRPC_CommandStatus,
    responseDeserialize: deserialize_shimmerGRPC_CommandStatus,
  },
  stopStreaming: {
    path: '/shimmerGRPC.ShimmerServer/StopStreaming',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.ShimmerRequest,
    responseType: src_ShimmerGrpcAndOJC_pb.CommandStatus,
    requestSerialize: serialize_shimmerGRPC_ShimmerRequest,
    requestDeserialize: deserialize_shimmerGRPC_ShimmerRequest,
    responseSerialize: serialize_shimmerGRPC_CommandStatus,
    responseDeserialize: deserialize_shimmerGRPC_CommandStatus,
  },
  closeApplication: {
    path: '/shimmerGRPC.ShimmerServer/CloseApplication',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.ShimmerRequest,
    responseType: src_ShimmerGrpcAndOJC_pb.CommandStatus,
    requestSerialize: serialize_shimmerGRPC_ShimmerRequest,
    requestDeserialize: deserialize_shimmerGRPC_ShimmerRequest,
    responseSerialize: serialize_shimmerGRPC_CommandStatus,
    responseDeserialize: deserialize_shimmerGRPC_CommandStatus,
  },
  // ConsensysApi related
setWorkspaceDirectory: {
    path: '/shimmerGRPC.ShimmerServer/SetWorkspaceDirectory',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.OperationRequest,
    requestSerialize: serialize_shimmerGRPC_StringMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringMsg,
    responseSerialize: serialize_shimmerGRPC_OperationRequest,
    responseDeserialize: deserialize_shimmerGRPC_OperationRequest,
  },
  getWorkspaceDirectory: {
    path: '/shimmerGRPC.ShimmerServer/GetWorkspaceDirectory',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.StringMsg,
    requestSerialize: serialize_shimmerGRPC_StringMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringMsg,
    responseSerialize: serialize_shimmerGRPC_StringMsg,
    responseDeserialize: deserialize_shimmerGRPC_StringMsg,
  },
  getDockedShimmerInfo: {
    path: '/shimmerGRPC.ShimmerServer/GetDockedShimmerInfo',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.ShimmersInfo,
    requestSerialize: serialize_shimmerGRPC_StringMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringMsg,
    responseSerialize: serialize_shimmerGRPC_ShimmersInfo,
    responseDeserialize: deserialize_shimmerGRPC_ShimmersInfo,
  },
  getMadgewickBetaValue: {
    path: '/shimmerGRPC.ShimmerServer/GetMadgewickBetaValue',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.DoubleMsg,
    requestSerialize: serialize_shimmerGRPC_StringMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringMsg,
    responseSerialize: serialize_shimmerGRPC_DoubleMsg,
    responseDeserialize: deserialize_shimmerGRPC_DoubleMsg,
  },
  pairShimmers: {
    path: '/shimmerGRPC.ShimmerServer/PairShimmers',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringArrayMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.OperationRequest,
    requestSerialize: serialize_shimmerGRPC_StringArrayMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringArrayMsg,
    responseSerialize: serialize_shimmerGRPC_OperationRequest,
    responseDeserialize: deserialize_shimmerGRPC_OperationRequest,
  },
  getOperationProgress: {
    path: '/shimmerGRPC.ShimmerServer/GetOperationProgress',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.OperationRequest,
    requestSerialize: serialize_shimmerGRPC_StringMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringMsg,
    responseSerialize: serialize_shimmerGRPC_OperationRequest,
    responseDeserialize: deserialize_shimmerGRPC_OperationRequest,
  },
  importSdDataFromShimmers: {
    path: '/shimmerGRPC.ShimmerServer/ImportSdDataFromShimmers',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringArrayMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.OperationRequest,
    requestSerialize: serialize_shimmerGRPC_StringArrayMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringArrayMsg,
    responseSerialize: serialize_shimmerGRPC_OperationRequest,
    responseDeserialize: deserialize_shimmerGRPC_OperationRequest,
  },
  parseSdDataFromPath: {
    path: '/shimmerGRPC.ShimmerServer/ParseSdDataFromPath',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.OperationRequest,
    requestSerialize: serialize_shimmerGRPC_StringMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringMsg,
    responseSerialize: serialize_shimmerGRPC_OperationRequest,
    responseDeserialize: deserialize_shimmerGRPC_OperationRequest,
  },
  scanSdDataAndCopy: {
    path: '/shimmerGRPC.ShimmerServer/ScanSdDataAndCopy',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringArrayMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.OperationRequest,
    requestSerialize: serialize_shimmerGRPC_StringArrayMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringArrayMsg,
    responseSerialize: serialize_shimmerGRPC_OperationRequest,
    responseDeserialize: deserialize_shimmerGRPC_OperationRequest,
  },
  clearSdCardData: {
    path: '/shimmerGRPC.ShimmerServer/ClearSdCardData',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringArrayMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.OperationRequest,
    requestSerialize: serialize_shimmerGRPC_StringArrayMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringArrayMsg,
    responseSerialize: serialize_shimmerGRPC_OperationRequest,
    responseDeserialize: deserialize_shimmerGRPC_OperationRequest,
  },
  dockAccessSlotWithSdCard: {
    path: '/shimmerGRPC.ShimmerServer/DockAccessSlotWithSdCard',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringArrayMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.OperationRequest,
    requestSerialize: serialize_shimmerGRPC_StringArrayMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringArrayMsg,
    responseSerialize: serialize_shimmerGRPC_OperationRequest,
    responseDeserialize: deserialize_shimmerGRPC_OperationRequest,
  },
  dockRestoreAutoTasks: {
    path: '/shimmerGRPC.ShimmerServer/DockRestoreAutoTasks',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringArrayMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.OperationRequest,
    requestSerialize: serialize_shimmerGRPC_StringArrayMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringArrayMsg,
    responseSerialize: serialize_shimmerGRPC_OperationRequest,
    responseDeserialize: deserialize_shimmerGRPC_OperationRequest,
  },
  // Shimmer device emulation software related
getInfoSpans: {
    path: '/shimmerGRPC.ShimmerServer/GetInfoSpans',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.InfoSpans,
    requestSerialize: serialize_shimmerGRPC_StringMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringMsg,
    responseSerialize: serialize_shimmerGRPC_InfoSpans,
    responseDeserialize: deserialize_shimmerGRPC_InfoSpans,
  },
  getInfoAllShimmers: {
    path: '/shimmerGRPC.ShimmerServer/GetInfoAllShimmers',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.ShimmersInfo,
    requestSerialize: serialize_shimmerGRPC_StringMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringMsg,
    responseSerialize: serialize_shimmerGRPC_ShimmersInfo,
    responseDeserialize: deserialize_shimmerGRPC_ShimmersInfo,
  },
  getEmulatedDevices: {
    path: '/shimmerGRPC.ShimmerServer/GetEmulatedDevices',
    requestStream: false,
    responseStream: false,
    requestType: src_ShimmerGrpcAndOJC_pb.StringMsg,
    responseType: src_ShimmerGrpcAndOJC_pb.EmulatedDevices,
    requestSerialize: serialize_shimmerGRPC_StringMsg,
    requestDeserialize: deserialize_shimmerGRPC_StringMsg,
    responseSerialize: serialize_shimmerGRPC_EmulatedDevices,
    responseDeserialize: deserialize_shimmerGRPC_EmulatedDevices,
  },
};

exports.ShimmerServerClient = grpc.makeGenericClientConstructor(ShimmerServerService);
