package com.shimmerresearch.driver;

import java.util.Comparator;

public class ShimmerObjectComparator implements Comparator<ShimmerObject> {
	
	public enum Order {LOCATION, BT_RADIO_ID, EXPANSION, NAME}
	
	private Order sortingBy = Order.LOCATION;

	@Override
	public int compare(ShimmerObject shimmerObject1, ShimmerObject shimmerObject2) {
		switch(sortingBy) {
			case LOCATION: return shimmerObject1.mUniqueID.compareTo(shimmerObject2.mUniqueID);
			case BT_RADIO_ID: return shimmerObject1.getMacIdFromUartParsed().compareTo(shimmerObject2.getMacIdFromUartParsed());
			case EXPANSION: return shimmerObject1.mExpansionBoardParsed.compareTo(shimmerObject2.mExpansionBoardParsed);
			case NAME: return shimmerObject1.getShimmerUserAssignedName().compareTo(shimmerObject2.getShimmerUserAssignedName());
			default: return 0;
		}
	}
	
	public void setSortingBy(Order sortBy) {
		this.sortingBy = sortBy;
	}

}