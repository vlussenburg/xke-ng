package com.xebia.xcoss.axcv.model;

import hirondelle.date4j.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import android.util.Log;

import com.xebia.xcoss.axcv.model.util.SessionComparator;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.util.XCS;


public class Conference implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static int counter = 1;
	private int id;
	private String title = "Not specified";
	private DateTime date = DateTime.today(XCS.TZ);

	private Set<Session> sessions;
	private ArrayList<Location> locations;

	private int sessionsPerLocation = 4;

	public Conference() {
		locations = new ArrayList<Location>();
		sessions = new TreeSet<Session>(new SessionComparator());
		this.id = counter++;
	}

	public int getId() {
		return id;
	}
	
	public int getOpenSlots() {
		return Math.max(0, getExpectedSlots() - sessions.size());
	}

	public int getExpectedSlots() {
		return locations.size() * sessionsPerLocation;
	}
	
	public ArrayList<Session> getSessions() {
		return new ArrayList<Session>(sessions);
	}
	
	public Session getSessionById(int id) {
		for (Session session : sessions) {
			if (id == session.getId()) {
				return session;
			}
		}
		return null;
	}

	public String getTitle() {
		return title;
	}

	public DateTime getDate() {
		return date;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}

	public void setSessionsPerLocation(int sessionsPerLocation) {
		this.sessionsPerLocation = sessionsPerLocation;
	}

	public int getSessionsPerLocation() {
		return sessionsPerLocation;
	}

	public void addSession(Session session) {
		sessions.add(session);
		session.setConference(this);
		Log.w(XCS.LOG.ALL, "Adding " + session.getTitle() + " to " + this.getTitle());
		session.setDate(date);
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

	public Session getUpcomingSession() {
		for (Session session : sessions) {
			if ( ScreenTimeUtil.isNow(session.getStartTime(), session.getEndTime()) ) {
				return session;
			}
		}
		return null;
	}
}
