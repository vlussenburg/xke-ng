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
	private DateTime startTime = DateTime.forTimeOnly(16, 0, 0, 0);
	private DateTime endTime = DateTime.forTimeOnly(21, 0, 0, 0);

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

	public boolean addSession(Session session) {
		Log.w(XCS.LOG.ALL, "Adding " + session.getTitle() + " to " + this.getTitle());
		// TODO : Check whether it fits. Otherwise return false.
		sessions.add(session);
		session.setConference(this);
		session.setDate(date);
		ConferenceServer.getInstance().storeSession(session);
		return true;
	}

	public TimeSlot getNextAvailableTimeSlot(DateTime start, int duration) {
		int prefstart = Math.max(getTime(start), getTime(startTime));

		// TODO Allow for multiple locations
		
		for (Session session: sessions) {
			int sesstart = getTime(session.getStartTime());
			
			// Preferred start time is later than session time.
			if ( sesstart <= prefstart ) {
				Log.w(XCS.LOG.ALL, "On " + session.getTitle() + " session starting earlier.");
				int end = getTime(session.getEndTime());
				if ( end > prefstart) {
					// Move start time to the end of this session
					prefstart = end;
				}
			} else {
				// There is room before this session
				int available = prefstart - sesstart;
				if (available >= duration ) {
					return getTimeSlot(prefstart, duration);
				}
			}
		}

		// Check last session till end of conference.
		int endspace = getTime(endTime) - prefstart;
		if ( endspace >= duration ) {
			return getTimeSlot(prefstart, duration);			
		}
		return null;
	}

	
	public List<TimeSlot> getAvailableTimeSlots() {
		ArrayList<TimeSlot> list = new ArrayList<TimeSlot>();

		final int length = 30;
		DateTime start = startTime;
		TimeSlot t;
		while ((t = getNextAvailableTimeSlot(start, length)) != null ) {
			list.add(t);
			start = start.plus(0, 0, 0, 0, length, 0, DayOverflow.Spillover);
		}
		return list;
	}

	private int getTime(DateTime dt) {
		return 100*dt.getHour() + dt.getMinute();
	}

	private TimeSlot getTimeSlot(int value, int duration) {
		TimeSlot ts = new TimeSlot();
		ts.start = DateTime.forTimeOnly(value/100, value%100, 0, 0);
		ts.end = ts.start.plus(0, 0, 0, 0, duration, 0, DayOverflow.Spillover);
		return ts;
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
			if (ScreenTimeUtil.isNow(session.getStartTime(), session.getEndTime())) {
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
