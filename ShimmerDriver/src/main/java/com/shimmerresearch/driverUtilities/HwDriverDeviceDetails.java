package com.shimmerresearch.driverUtilities;

import java.util.ArrayList;
import java.util.List;

/** Holds system HW information
 * 
 * @author Mark Nolan
 *
 */
public class HwDriverDeviceDetails {
	
	//SPDRP
	public String classGuid = "";
    public String serviceDescription = "";
    public String friendlyName = "";
    public String hardwareId = "";
    public String devInterfacePath = "";

    public String busTypeGuid = "";
	public String devType = "";
	public String enumeratorName = "";
	public String legacyBusType = "";
	public String locationInformation = "";
	public List<String> locationPaths = new ArrayList<String>();
	public String manufacturer = "";
	public String physicalDeviceObjectName = "";

    //DEVPROPKEY
	public String deviceParent = "";
    public String deviceChildren = "";
    public String deviceBusReportedDeviceDesc = "";
    public String deviceContainerId = "";
    public String deviceFriendlyName = "";
    public String deviceDisplayCategory = "";
    public String deviceLocationInfo = "";
    public String deviceManufacturer = "";
    public String deviceSecuritySDS = "";
	public String deviceDriverProvider = "";
	public String deviceDriverVersion = "";
	
	public String devInterfaceGuid = "";
    
}
