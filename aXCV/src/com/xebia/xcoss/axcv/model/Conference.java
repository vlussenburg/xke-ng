package com.xebia.xcoss.axcv.model;

import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.DayOverflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.util.Log;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.model.util.SessionComparator;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.util.XCS;


public class Conference implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static int counter = 1;
	private int id;
	private String title = "Not specified";
	private DateTime date = DateTime.today(XCS.TZ);
	private DateTime startTime = DateTime.forTimeOnly(16, 0, 0,0);
	private DateTime endTime = DateTime.forTimeOnly(21, 0, 0,0);

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
		return new ArrayList<Session>(loadSessions());
	}
	
	public Session getSessionById(int id) {
		for (Session session : loadSessions()) {
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
		Log.w(XCS.LOG.ALL, "Adding " + session.getTitle() + " to " + this.getTitle());
		sessions.add(session);
		session.setConference(this);
		session.setDate(date);
		ConferenceServer.getInstance().storeSession(session);
	}

	public List<TimeSlot> getAvailableTimeSlots() {
		ArrayList<TimeSlot> list = new ArrayList<TimeSlot>();
		
		int end = (endTime.getHour()-startTime.getHour())*2;
		for (int i = 0; i < end; i++) {
			TimeSlot ts = new TimeSlot();
			ts.start = startTime.plus(0, 0, 0, 0, i*30, 0, DayOverflow.Spillover);
			ts.end = startTime.plus(0, 0, 0, 0, (i+1)*30, 0, DayOverflow.Spillover);
			list.add(ts);
		}
		return list;
	}
	
	private Set<Session> loadSessions() {
		ConferenceServer.getInstance().loadSessions(this, sessions);
		return sessions;
	}
	
	public DateTime getStartTime() {
		return startTime;
	}
	
	public DateTime getEndTime() {
		return endTime;
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
	
	public class TimeSlot {
		public DateTime start;
		public DateTime end;
	}
}
