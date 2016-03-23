package com.shimmerresearch.driver;

import java.util.Comparator;

public class ShimmerObjectComparator implements Comparator<ShimmerDevice> {
	
	public enum OrderShimmerObject {LOCATION, BT_RADIO_ID, EXPANSION, FIRMWARE, BATTERY, NAME}
	
	private OrderShimmerObject sortingBy = OrderShimmerObject.LOCATION;

	@Override
	public int compare(ShimmerDevice shimmerObject1, ShimmerDevice shimmerObject2) {
		switch(sortingBy) {
			case LOCATION: return shimmerObject1.mUniqueID.compareTo(shimmerObject2.mUniqueID);
			case BT_RADIO_ID: return shimmerObject1.getMacIdFromUartParsed().compareTo(shimmerObject2.getMacIdFromUartParsed());
			case EXPANSION: return shimmerObject1.getExpansionBoardParsed().compareTo(shimmerObject2.getExpansionBoardParsed());
			case FIRMWARE: return shimmerObject1.getFirmwareVersionParsed().compareTo(shimmerObject2.getFirmwareVersionParsed());
			case BATTERY: return shimmerObject1.getEstimatedChargePercentageParsed().compareTo(shimmerObject2.getEstimatedChargePercentageParsed());
			case NAME: return shimmerObject1.getShimmerUserAssignedName().compareTo(shimmerObject2.getShimmerUserAssignedName());
			default: return 0;
		}
	}
	
	public void setSortingBy(OrderShimmerObject sortBy) {
		this.sortingBy = sortBy;
	}
	
	public OrderShimmerObject getSortingBy(){
		return this.sortingBy;
	}

}