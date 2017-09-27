from __future__ import print_function

import random
import time

import grpc

from time import sleep

from ShimmerGrpcPythonLib import ShimmerGrpcAndOJC_pb2
from ShimmerGrpcPythonLib import ShimmerGrpcAndOJC_pb2_grpc

def helloServer(stub):
    print('Saying Hello to the Server')
    response = stub.SayHello(ShimmerGrpcAndOJC_pb2.HelloRequest(name='Helbling'))
    print('\tResponse' + response.message)

def closeApplication(stub):
    print('Requesting to close Application...')
    response = stub.CloseApplication(ShimmerGrpcAndOJC_pb2.ShimmerRequest())

def getWorkspaceDirectory(stub):
    print('Getting Workspace Directory')
    response = stub.GetWorkspaceDirectory(ShimmerGrpcAndOJC_pb2.StringMsg(message=''))
    print('\tWorkspace Directory=' + response.message)

def setWorkspaceDirectory(stub):
    newWorkspace='C:/Users/Shimmer/Documents/StroKare_Workspace/'
    print('Setting Workspace Directory to: ' + newWorkspace)
    response = stub.SetWorkspaceDirectory(ShimmerGrpcAndOJC_pb2.StringMsg(message=newWorkspace))
#    print('\tPass? ' + str(response.isSuccess))
#    print('\tMessage=' + response.message)
    print(response)

def getDockedShimmerInfo(stub):
    print('Getting docked Shimmers')
    response = stub.GetDockedShimmerInfo(ShimmerGrpcAndOJC_pb2.StringMsg(message=''))
    print(response)
#    for key in response.shimmerMap:
#      shimmer = response.shimmerMap[key]
#      print(shimmer.name)
  
def getMadgewickBetaValue(stub):
    response = stub.GetMadgewickBetaValue(ShimmerGrpcAndOJC_pb2.StringMsg(message=''))
    print('Madgewick Beta value=')
    print(response)
  
def pairShimmers(stub):
    print('Pairing Shimmers')
    stringArrayMsg = ShimmerGrpcAndOJC_pb2.StringArrayMsg();
    #Specify particular slots or comment out to pair all docked Shimmers (i.e., has to be 2 devices docked and read)
#    stringArrayMsg.messageArray.extend(['Base6U.01.01', 'Base6U.01.02'])
    response = stub.PairShimmers(stringArrayMsg)
    print(response)
    sleep(2.0)
    if response.isSuccess:
		waitForOperationToFinish(stub)

def importSdDataFromShimmers(stub):
    print('Importing from Shimmers')
    stringArrayMsg = ShimmerGrpcAndOJC_pb2.StringArrayMsg();
    #Specify particular slots or comment out to import from all docked Shimmers
#    stringArrayMsg.messageArray.extend(['Base6U.01.01', 'Base6U.01.02'])
    response = stub.ImportSdDataFromShimmers(stringArrayMsg)
    print(response)
    sleep(2.0)
    if response.isSuccess:
		waitForOperationToFinish(stub)

def parseSdDataFromPath(stub, path):
    print('Parsing data from path')
    response = stub.ParseSdDataFromPath(ShimmerGrpcAndOJC_pb2.StringMsg(message=path))
    print(response)
    sleep(2.0)
    if response.isSuccess:
		waitForOperationToFinish(stub)

def getOperationProgress(stub):
    response = stub.GetOperationProgress(ShimmerGrpcAndOJC_pb2.StringMsg(message=''))
    return response

def isOperationInProgress(stub):
    response = getOperationProgress(stub)
    return isOperationInProgressCheck(response.message)

def isOperationInProgressCheck(message):
    if message == 'None':# or message == 'Finished':
        return False
    else :
        return True

def waitForOperationToFinish(stub):
    response = getOperationProgress(stub)
    while not response.isFinished or isOperationInProgressCheck(response.message):
        sleep(2.0)
        response = getOperationProgress(stub)
        print(response)

def run():
    channel = grpc.insecure_channel('localhost:50051')
    stub = ShimmerGrpcAndOJC_pb2_grpc.ShimmerServerStub(channel)
  
    helloServer(stub)
  
    getWorkspaceDirectory(stub)
    setWorkspaceDirectory(stub)
    getWorkspaceDirectory(stub)

    getMadgewickBetaValue(stub)
  
    getDockedShimmerInfo(stub)
  
    if isOperationInProgress(stub):
        print("API is busy")
        waitForOperationToFinish(stub)
		
#    pairShimmers(stub)
#    importSdDataFromShimmers(stub)

#    parseSdDataFromPath(stub, 'C:/Users/Shimmer/Documents/StroKare_Workspace/Backup/2017-09-27_12.03.19/0006668ca4cc')
#    parseSdDataFromPath(stub, 'C:/Users/Shimmer/Documents/StroKare_Workspace/Backup/2017-09-27_12.03.19')

#    closeApplication(stub)
    

if __name__ == '__main__':
    run()