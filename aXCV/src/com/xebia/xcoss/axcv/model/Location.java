package com.xebia.xcoss.axcv.model;

import java.io.Serializable;

public class Location implements Serializable {

	private String location;

	public Location(String place) {
		this.location = place;
	}
	
	public String getLocation() {
		return location;
	}

}
