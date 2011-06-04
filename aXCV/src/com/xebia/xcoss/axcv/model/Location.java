package com.xebia.xcoss.axcv.model;

import java.io.Serializable;

public class Location implements Serializable {

	private static final long serialVersionUID = 1L;

	private String location;

	public Location(String place) {
		this.location = place;
	}
	
	public String getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return location;
	}
}
