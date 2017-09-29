from __future__ import print_function

import random
import time

import grpc

from time import sleep

from ShimmerGrpcPythonLib import ShimmerGrpcAndOJC_pb2
from ShimmerGrpcPythonLib import ShimmerGrpcAndOJC_pb2_grpc

def connectToServer():
    address = 'localhost:50051'
    channel = grpc.insecure_channel(address)
    try:
        print("Connecting to server at..." + address)
        stub = ShimmerGrpcAndOJC_pb2_grpc.ShimmerServerStub(channel)
        helloServer(stub)
        print("Connected...\n")
        return stub
    except:
        print("Could not connect")
        exit()


def helloServer(stub):
    print('Saying Hello to the Server...')
    response = stub.SayHello(ShimmerGrpcAndOJC_pb2.HelloRequest(name='Helbling'))
    print('\tResponse: ' + response.message)

def closeApplication(stub):
    print('Requesting to close Application...')
    response = stub.CloseApplication(ShimmerGrpcAndOJC_pb2.ShimmerRequest())
#    print('\tResponse: ' + response.message)

def getWorkspaceDirectory(stub):
    print('Getting Workspace Directory...')
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
    print('Getting docked Shimmers...\n')
    response = stub.GetDockedShimmerInfo(ShimmerGrpcAndOJC_pb2.StringMsg(message=''))
    print(response)
#    for key in response.shimmerMap:
#      shimmer = response.shimmerMap[key]
#      print(shimmer.name)

def printDockedShimmerUniqueIds(stub):
    uniqueIds = getDockedShimmerUniqueIds(stub)
    print(', '.join(uniqueIds))

def getDockedShimmerUniqueIds(stub):
    response = stub.GetDockedShimmerInfo(ShimmerGrpcAndOJC_pb2.StringMsg(message=''))
    uniqueIds = ['']*len(response.shimmerMap)
    i = 0
    for key in response.shimmerMap:
        uniqueIds[i] = key
        i += 1
    return uniqueIds
    
def getMadgewickBetaValue(stub):
    response = stub.GetMadgewickBetaValue(ShimmerGrpcAndOJC_pb2.StringMsg(message=''))
    print('Madgewick Beta value=')
    print(response)
  
def pairShimmers(stub):
    print('Pairing Shimmers...')
    stringArrayMsg = ShimmerGrpcAndOJC_pb2.StringArrayMsg();
    #Specify particular slots or comment out to pair all docked Shimmers (i.e., has to be 2 devices docked and read)
#    stringArrayMsg.messageArray.extend(['Base6U.01.01', 'Base6U.01.02'])
    response = stub.PairShimmers(stringArrayMsg)
    print('Started?')
    print(response)
    sleep(2.0)
    if response.isSuccess:
        waitForOperationToFinish(stub)

def importSdDataFromShimmers(stub):
    print('Importing from Shimmers...')
    stringArrayMsg = ShimmerGrpcAndOJC_pb2.StringArrayMsg();
    #Specify particular slots or comment out to import from all docked Shimmers
#    stringArrayMsg.messageArray.extend(['Base6U.01.01', 'Base6U.01.02'])
    response = stub.ImportSdDataFromShimmers(stringArrayMsg)
    print('Started?')
    print(response)
    sleep(1.0)
    if response.isSuccess:
        waitForOperationToFinish(stub)

def parseSdDataFromPath(stub, path):
    print('Parsing data from path: ' + path)
#    print(path)
    response = stub.ParseSdDataFromPath(ShimmerGrpcAndOJC_pb2.StringMsg(message=path))
    print('Started?')
    print(response)
    sleep(2.0)
    if response.isSuccess:
        waitForOperationToFinish(stub)

def copySdDataFromShimmers(stub):
    print('Copying data from Shimmers...')
    stringArrayMsg = ShimmerGrpcAndOJC_pb2.StringArrayMsg();
    #Specify particular slots or comment out to import from all docked Shimmers
#    stringArrayMsg.messageArray.extend(['Base6U.01.01', 'Base6U.01.02'])
    response = stub.ScanSdDataAndCopy(stringArrayMsg)
    print('Started?')
    print(response)
    sleep(2.0)
    if response.isSuccess:
        waitForOperationToFinish(stub)

def clearSdDataFromShimmers(stub):
    print('Clearing data from Shimmers...')
    stringArrayMsg = ShimmerGrpcAndOJC_pb2.StringArrayMsg();
    #Specify particular slots or comment out to import from all docked Shimmers
#    stringArrayMsg.messageArray.extend(['Base6U.01.01', 'Base6U.01.02'])
    response = stub.ClearSdCardData(stringArrayMsg)
    print('Started?')
    print(response)
    sleep(2.0)
    if response.isSuccess:
        waitForOperationToFinish(stub)

def dockAccessSlotWithSdCard(stub):
    slot = 'Base6U.01.01'
    print('Accessing the SD card for...' + slot)
    stringArrayMsg = ShimmerGrpcAndOJC_pb2.StringArrayMsg();
    # This operation can only handle 1 Shimmer per Base at a time
    # 'Base6U' for 6 slot base, 'Base15U' for 15 slot
    # The first '01' is the base number connected to the computer
    # The second '01' is the slot number on the base
    stringArrayMsg.messageArray.extend([slot])
    response = stub.DockAccessSlotWithSdCard(stringArrayMsg)
    print('Started?')
    print(response)
    sleep(2.0)
    if response.isSuccess:
        waitForOperationToFinish(stub)

def dockRestoreAutoTasks(stub):
    print('Restoring Dock automated tasks...')
    stringArrayMsg = ShimmerGrpcAndOJC_pb2.StringArrayMsg();
    #Specify particular docks or comment out to import from all docked Shimmers
#    stringArrayMsg.messageArray.extend(['Base6U.01', 'Base15U.01'])
    response = stub.DockRestoreAutoTasks(stringArrayMsg)
    print('Started?')
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
        print(time.ctime())
        print(response)


def showMenu(stub):
    print("Menu:")
    print("    0 = Exit script")
    print("    1 = Ping server")
    print("    2 = Close application")
    print("    3 = Get Docked Shimmer Information")
    print("    4 = Get Madgewick Beta Value")
    print("    5 = Change Workspace Path")
    print("    6 = pairShimmers")
    print("    7 = Parse Data from path to CSV")
    print("    8 = Full Import Data from Shimmers")
    print("    9 = Copy Data from Shimmers")
    print("    10 = Clear Data from Shimmers")
    print("    11 = Access a slot with SD card access")
    print("    12 = Restore Dock's Auto tasks after SD card access")
    print("")
    variable = raw_input('Input option: ')

    if variable == '0':
        exit()
    elif variable == '1':
        helloServer(stub)
    elif variable == '2':
        closeApplication(stub)
    elif variable == '3':
        getDockedShimmerInfo(stub)
    elif variable == '4':
        getMadgewickBetaValue(stub)
    elif variable == '5':
        getWorkspaceDirectory(stub)
        setWorkspaceDirectory(stub)
        getWorkspaceDirectory(stub)
    elif variable == '6':
        pairShimmers(stub)
    elif variable == '7':
        #parseSdDataFromPath(stub, 'C:/Users/Shimmer/Documents/StroKare_Workspace/Backup/2017-09-27_12.03.19/0006668ca4cc')
        parseSdDataFromPath(stub, 'C:/Users/Shimmer/Documents/StroKare_Workspace/Backup/2017-09-27_12.03.19')
    elif variable == '8':
        importSdDataFromShimmers(stub)
    elif variable == '9':
        copySdDataFromShimmers(stub)
    elif variable == '10':
        clearSdDataFromShimmers(stub)
    elif variable == '11':
        dockAccessSlotWithSdCard(stub)
    elif variable == '12':
        dockRestoreAutoTasks(stub)

    showMenu(stub)

    
def run():
    print("/**********************************/")
    print("/* StroKare Python/Java Interface */")
    print("/**********************************/")
    stub = connectToServer();

    if isOperationInProgress(stub):
        print("API is busy")
        variable = raw_input('Wait? y/n: ')
        if variable is 'n':
            closeApplication(stub)
            exit()
        elif variable is 'y':
            waitForOperationToFinish(stub)

    printDockedShimmerUniqueIds(stub)
    getDockedShimmerInfo(stub)
    showMenu(stub)
   
 
if __name__ == '__main__':
    run()