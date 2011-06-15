package com.xebia.xcoss.axcv.logic;

import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;

public class ConferenceCache {

	private static final long CACHETIME = 30 * 60 * 1000;

	class CachedObject<T> {
		public T object;
		public long moment;
		public boolean dirty;

		public CachedObject(T data) {
			this.object = data;
			this.dirty = false;
			this.moment = System.currentTimeMillis();
		}

		@Override
		public int hashCode() {
			return object == null ? 0 : object.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CachedObject<T> other = (CachedObject<T>) obj;
			return object == null ? other.object == null : object.equals(other.object);
		}
		
	};

	private HashMap<Integer, CachedObject<Conference>> conferencesById;
	private HashMap<Integer, CachedObject<Session>> sessionsById;
	private HashMap<Integer, CachedObject<List<Conference>>> conferencesByYear;

	protected ConferenceCache() {
		this.conferencesById = new HashMap<Integer, CachedObject<Conference>>();
		this.conferencesByYear = new HashMap<Integer, CachedObject<List<Conference>>>();
		this.sessionsById = new HashMap<Integer, CachedObject<Session>>();
	}

	public Conference getConference(DateTime date) {
		for (CachedObject<Conference> co : conferencesById.values()) {
			if (co.object.getDate().equals(date)) {
				return checkValid(co);
			}
		}
		return null;
	}

	public Conference getConference(int id) {
		return checkValid(conferencesById.get(id));
	}

	public List<Conference> getConferences(Integer year) {
		return checkValid(conferencesByYear.get(year));
	}

	public List<Conference> getConferences(DateTime date) {
		List<Conference> list = getConferences(date.getYear());
		if (list == null || !date.unitsAllPresent(Unit.MONTH)) {
			return list;
		}
		List<Conference> result = new ArrayList<Conference>();
		boolean hasDay = date.unitsAllPresent(Unit.DAY);
		for (Conference conference : list) {
			if (conference.getDate().getMonth() == date.getMonth()) {
				if (hasDay && conference.getDate().getDay() != date.getDay()) {
					continue;
				}
				result.add(conference);
			}
		}
		return result;
	}

	public Session getSession(int id) {
		return checkValid(sessionsById.get(id));
	}

	public void add(Conference result) {
		conferencesById.put(result.getId(), new CachedObject<Conference>(result));
		try {
			Integer key = result.getDate().getYear();
			CachedObject<List<Conference>> cachedObject = conferencesByYear.get(key);
			if ( cachedObject == null ) {
				cachedObject = new CachedObject<List<Conference>>(new ArrayList<Conference>());
				conferencesByYear.put(key, cachedObject);
			}
			cachedObject.object.add(result);
		} catch (Exception e) {
			// ignore
		}
	}

	public void add(List<Conference> result) {
		for (Conference conference : result) {
			add(conference);
		}
	}

	public void add(Session result) {
		// Assume conferences are updated via server.
		sessionsById.put(result.getId(), new CachedObject<Session>(result));
	}

	public void addSessions(List<Session> result) {
		for (Session session : result) {
			add(session);
		}
	}

	public void remove(Conference conference) {
		conferencesById.remove(conference.getId());
		try {
			conferencesByYear.get(conference.getDate().getYear()).object.remove(conference);
		} catch (Exception e) {
			// ignore
		}
	}

	public void remove(Session session) {
		// Assume conferences are updated via server.
		sessionsById.remove(session.getId());
	}

	private <T> T checkValid(CachedObject<T> co) {
		if ( co == null ) {
			return null;
		}
		long now = System.currentTimeMillis();
		if (co.dirty || (co.moment + CACHETIME) < now) {
			Log.d(XCS.LOG.CACHE, "Cache hit, but dirty: " + co.object);
			co.dirty = true;
			return null;
		}
		Log.d(XCS.LOG.CACHE, "Cache hit: " + co.object);
		return co.object;
	}
}
