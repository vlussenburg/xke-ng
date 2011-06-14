package com.xebia.xcoss.axcv.model;

import java.io.Serializable;

public class Location implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private String description;
	private boolean standard;

	public Location(int id, String place, boolean isBase) {
		this.description = place;
		this.standard = isBase;
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return description;
	}

	public boolean isStandard() {
		return standard;
	}

	public int getId() {
		return id;
	}
}
