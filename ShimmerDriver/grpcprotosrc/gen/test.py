from __future__ import print_function

import random
import time

import grpc

import ShimmerGrpcAndOJC_pb2
import ShimmerGrpcAndOJC_pb2_grpc

def testfunction(stub):
  response = stub.SayHello(ShimmerGrpcAndOJC_pb2.HelloRequest(name='you'))
  print(response.message)

def run():
  channel = grpc.insecure_channel('localhost:50051')
  stub = ShimmerGrpcAndOJC_pb2_grpc.ShimmerServerStub(channel)
  testfunction(stub)

if __name__ == '__main__':
  run()