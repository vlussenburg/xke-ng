package com.xebia.xcoss.axcv.model;

import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.DayOverflow;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.CommException;
import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.model.util.SessionComparator;
import com.xebia.xcoss.axcv.model.util.TimeSlotComparator;
import com.xebia.xcoss.axcv.ui.FormatUtil;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class Conference implements Serializable {

	public class TimeSlot {
		public static final int LENGTH = 60;
		public static final int MIN_LENGTH = 5;
		public DateTime start;
		public DateTime end;
		public Location location;
	}

	private static final long serialVersionUID = 2L;

	private String id;
	private String title;
	private String description;
	private Author organiser;
	@SerializedName("begin")
	private DateTime startTime = DateTime.forTimeOnly(16, 0, 0, 0);
	@SerializedName("end")
	private DateTime endTime = DateTime.forTimeOnly(21, 0, 0, 0);
	private Set<Location> locations;
	private Set<Session> sessions;

	public Conference() {
		this.locations = new HashSet<Location>();
	}

	public Conference(Conference original) {
		this();

		this.id = original.id;
		this.title = original.title;
		this.description = original.description;
		// DateTime is immutable
		this.startTime = original.startTime;
		this.endTime = original.endTime;
		// Author is immutable
		this.organiser = original.organiser;
		// Location is immutable
		this.locations.addAll(original.locations);
	}

	// Getters and setters

	public String getId() {
		return id;
	}

	public DateTime getDate() {
		if (startTime.hasYearMonthDay()) {
			return startTime;
		}
		return null;
	}

	public Set<Session> getSessions() {
		if (sessions == null) {
			resetSessions();
		}
		if (sessions.isEmpty()) {
			try {
				List<Session> list = ConferenceServer.getInstance().getSessions(this);
				sessions.addAll(list);
			}
			catch (CommException e) {
				BaseActivity.handleException(null, "retrieving sessions", e);
			}
		}
		return sessions;
	}

	public Set<Session> getSessions(Location loc) {
		Set<Session> data = getSessions();
		if (loc == null) {
			return data;
		}

		TreeSet<Session> set = new TreeSet<Session>(new SessionComparator());
		for (Session session : data) {
			// If the session has a location not assigned to the conference, it never shows ...
			if (session.isMandatory() || loc.equals(session.getLocation())) {
				set.add(session);
			}
		}
		return set;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public DateTime getStartTime() {
		return startTime;
	}

	public void updateStartTime(final DateTime updated) {
		if (startTime == null || (updated.hasHourMinuteSecond() && updated.hasYearMonthDay())) {
			startTime = updated;
		} else {
			startTime = addToTime(startTime, updated);
		}
	}

	public DateTime getEndTime() {
		return endTime;
	}

	public void updateEndTime(final DateTime updated) {
		if (endTime == null || (updated.hasHourMinuteSecond() && updated.hasYearMonthDay())) {
			endTime = updated;
		} else {
			endTime = addToTime(endTime, updated);
		}
	}

	private DateTime addToTime(DateTime time, DateTime updated) {
		if (updated.hasHourMinuteSecond()) {
			return new DateTime(time.getYear(), time.getMonth(), time.getDay(), updated.getHour(), updated.getMinute(),
					updated.getSecond(), 0);
		}
		return new DateTime(updated.getYear(), updated.getMonth(), updated.getDay(), time.getHour(), time.getMinute(),
				time.getSecond(), 0);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Author getOrganiser() {
		return organiser;
	}

	public void setOrganiser(Author organiser) {
		this.organiser = organiser;
	}

	public Set<Location> getLocations() {
		return locations;
	}

	// Utilities

	public boolean check(List<String> messages) {
		if (startTime == null || endTime == null || !startTime.hasHourMinuteSecond()) {
			messages.add("Date, start/end time");
		}
		if (StringUtil.isEmpty(title)) {
			messages.add("Title");
		}
		if (locations == null || locations.isEmpty()) {
			messages.add("Locations");
		}
		return (messages.size() == 0);
	}

	private void resetSessions() {
		sessions = new TreeSet<Session>(new SessionComparator());
	}

	public Session getSessionById(String id) {
		for (Session session : getSessions()) {
			if (id.equals(session.getId())) {
				return session;
			}
		}
		return null;
	}

	/**
	 * Adds a session on the server and resets the local stored sessions.
	 * 
	 * @param session
	 * @return
	 */
	public boolean addSession(Session session, boolean create) {
		Log.w(XCS.LOG.ALL, "Adding " + session.getTitle() + " to " + getTitle());
		try {
			String id = ConferenceServer.getInstance().storeSession(session, getId(), create);
			resetSessions();
			return !StringUtil.isEmpty(id);
		}
		catch (CommException e) {
			BaseActivity.handleException(null, "adding session", e);
		}
		return false;
	}

	public void deleteSession(Session session) {
		if (sessions != null) {
			sessions.remove(session);
		}
		try {
			ConferenceServer.getInstance().deleteSession(session);
		}
		catch (CommException e) {
			BaseActivity.handleException(null, "deleting session", e);
		}
	}

	public static String create(Conference conference) {
		try {
			return ConferenceServer.getInstance().storeConference(conference, true);
		}
		catch (CommException e) {
			BaseActivity.handleException(null, "creating conference", e);
		}
		return null;
	}

	public boolean update() {
		try {
			ConferenceServer.getInstance().storeConference(this, false);
			return true;
		}
		catch (CommException e) {
			BaseActivity.handleException(null, "updating conference", e);
		}
		return false;
	}

	public void delete() {
		try {
			ConferenceServer.getInstance().deleteConference(this);
		}
		catch (CommException e) {
			BaseActivity.handleException(null, "deleting conference", e);
		}
	}

	// TODO : Allow current session to be overwritten
	public TimeSlot getNextAvailableTimeSlot(Session rescheduleSession, DateTime start, int duration, Location location) {

		int prefstart = Math.max(getTime(start), getTime(startTime));

		for (Session session : getSessions()) {
			if (!location.equals(session.getLocation()) || session.equals(rescheduleSession)) {
				continue;
			}
			int sesstart = getTime(session.getStartTime());

			// Preferred start time is later than session time.
			if (prefstart >= sesstart) {
				// Log.d(XCS.LOG.ALL, "On " + session.getTitle() + ": session starting earlier.");
				// If the session ends after the preferred start time, move start time.
				prefstart = Math.max(getTime(session.getEndTime()), prefstart);
			} else {
				// There is room before this session
				int available = sesstart - prefstart;
				if (available >= duration) {
					return getTimeSlot(prefstart, duration, location);
				}
				// Not enough time available. Try with a later time.
				prefstart = getTime(session.getEndTime());
			}
		}

		// Check last session till end of conference.
		int endspace = getTime(endTime) - prefstart;
		if (endspace >= duration) {
			return getTimeSlot(prefstart, duration, location);
		}
		return null;
	}

	public SortedSet<TimeSlot> getAvailableTimeSlots(int duration) {
		TreeSet<TimeSlot> list = new TreeSet<TimeSlot>(new TimeSlotComparator());
		for (Location loc : locations) {
			list.addAll(getAvailableTimeSlots(duration, loc));
		}
		return list;
	}

	public SortedSet<TimeSlot> getAvailableTimeSlots(int duration, Location loc) {
		TreeSet<TimeSlot> list = new TreeSet<TimeSlot>(new TimeSlotComparator());
		TimeSlot t;
		int length = duration < TimeSlot.MIN_LENGTH ? TimeSlot.LENGTH : duration;
		DateTime start = startTime;
		while ((t = getNextAvailableTimeSlot(null, start, length, loc)) != null) {
			list.add(t);
			start = start.plus(0, 0, 0, 0, length, 0, DayOverflow.Spillover);
		}
		return list;
	}

	private int getTime(DateTime dt) {
		if (dt == null) {
			return 0;
		}
		return 60 * dt.getHour() + dt.getMinute();
	}

	private TimeSlot getTimeSlot(int value, int duration, Location loc) {
		TimeSlot ts = new TimeSlot();
		ts.start = DateTime.forTimeOnly(value / 60, value % 60, 0, 0);
		ts.end = ts.start.plus(0, 0, 0, 0, duration, 0, DayOverflow.Spillover);
		ts.location = loc;
		return ts;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Conference other = (Conference) obj;
		if (startTime == null) {
			if (other.startTime != null) return false;
		} else if (!startTime.equals(other.startTime)) return false;
		if (title == null) {
			if (other.title != null) return false;
		} else if (!title.equals(other.title)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "Conference [id=" + id + ", title=" + title + ", description=" + description + ", organiser="
				+ organiser + ", startTime=" + startTime + ", endTime=" + endTime + ", locations="
				+ FormatUtil.getList(locations) + ", sessions=" + FormatUtil.getList(sessions) + "]";
	}

	public void setId(String i) {
		this.id = i;
	}
}
