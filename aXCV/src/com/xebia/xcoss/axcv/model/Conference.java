package com.xebia.xcoss.axcv.model;

import java.util.ArrayList;
import java.util.Date;

import android.location.Location;

public class Conference {

	private String title = "Not specified";
	private Date date = new Date();

	private ArrayList<Session> sessions;
	private ArrayList<Location> locations;

	private int sessionsPerLocation = 4;

	public Conference() {
		locations = new ArrayList<Location>();
		sessions = new ArrayList<Session>();
	}

	public int getOpenSlots() {
		return Math.max(0, getExpectedSlots() - sessions.size());
	}

	public int getExpectedSlots() {
		return locations.size() * sessionsPerLocation;
	}
	
	public ArrayList<Session> getSessions() {
		return sessions;
	}

	public String getTitle() {
		return title;
	}

	public Date getDate() {
		return date;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setSessionsPerLocation(int sessionsPerLocation) {
		this.sessionsPerLocation = sessionsPerLocation;
	}

	public int getSessionsPerLocation() {
		return sessionsPerLocation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Conference other = (Conference) obj;
		if (date == null) {
			if (other.date != null) return false;
		} else if (!date.equals(other.date)) return false;
		if (title == null) {
			if (other.title != null) return false;
		} else if (!title.equals(other.title)) return false;
		return true;
	}
}
