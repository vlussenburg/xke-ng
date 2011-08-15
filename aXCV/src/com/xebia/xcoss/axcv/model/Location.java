package com.xebia.xcoss.axcv.model;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class Location implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	@SerializedName("name")
	private String description;
	private int capacity;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Location other = (Location) obj;
		if (description == null) {
			if (other.description != null) return false;
		} else if (!description.equals(other.description)) return false;
		return true;
	}
}
