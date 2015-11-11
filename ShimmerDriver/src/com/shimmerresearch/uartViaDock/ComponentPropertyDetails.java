package com.shimmerresearch.uartViaDock;

import java.util.List;

import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/**Holds each Component and Property byte pairs for use during configuration 
 * through the Shimmer's UART. 
 * 
 * @author Mark Nolan
 *
 */
public class ComponentPropertyDetails {
	public int component = 0;
	public byte componentByte = 0;
	public int property = 0;
	public byte propertyByte = 0;
	public String propertyName = "";
	public List<ShimmerVerObject> listOfCompatibleVersionInfo;
	public PERMISSION permission = PERMISSION.UNKNOWN; 
	
	public byte[] compPropByteArray = null;
	
	public enum PERMISSION{
		UNKNOWN,
		READ_ONLY,
		WRITE_ONLY,
		READ_WRITE
	}

	/**Holds each Component and Property byte pairs for use during configuration 
	 * through the Shimmer's UART. 
	 * @param component the device number to configure on the Shimmer 
	 * @param property the property number of the component to configure
	 * @param readWrite 
	 * @param listOfCompatibleVersionInfo 
	 * @param descrpition 
	 */
	public ComponentPropertyDetails(int component, int property, PERMISSION readWrite, List<ShimmerVerObject> listOfCompatibleVersionInfo, String propertyName){
		this.component = component;
		this.componentByte = (byte)component;
		this.property = property;
		this.propertyByte = (byte)property;
		this.propertyName = propertyName;
		this.listOfCompatibleVersionInfo = listOfCompatibleVersionInfo;
		
		this.compPropByteArray = new byte[]{(byte)this.componentByte,(byte)this.propertyByte};
	}
}