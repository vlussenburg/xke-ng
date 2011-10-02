package com.xebia.xcoss.axcv.logic;

import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.Unit;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;

public abstract class ConferenceCache {
	protected static final long CACHETIME = 30 * 60 * 1000;

	public <T> T getObject(String key, Class<T> type) {
		return checkValid(doGetCachedObject(key, type));
	}

	public Conference getConference(String id) {
		return checkValid(doGetCachedObject(id, Conference.class));
	}

	public List<Conference> getConferences(Integer year) {
		List<Conference> result = new ArrayList<Conference>();
		for (CachedObject<Conference> co : doGetCachedObjects(Conference.class)) {
			if (co.object.getDate().getYear().equals(year)) {
				// Not checking on validity...
				if (co.object != null) {
					result.add(co.object);
				}
			}
		}
		return result;
	}

	public Conference getConference(DateTime date) {
		for (CachedObject<Conference> co : doGetCachedObjects(Conference.class)) {
			if (co.object.getDate().equals(date)) {
				return checkValid(co);
			}
		}
		return null;
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

	public Session getSession(String id) {
		return checkValid(doGetCachedObject(id, Session.class));
	}

	public void add(Session result) {
		doPutCachedObject(result.getId(), new CachedObject<Session>(result));
	}

	public void add(Conference result) {
		CachedObject<Conference> cachedObject = new CachedObject<Conference>(result);
		doPutCachedObject(result.getId(), cachedObject);
	}

	public <T> void addObject(String key, T o) {
		doPutCachedObject(key, new CachedObject<T>(o));
	}

	public void remove(Session session) {
		doRemoveCachedObject(session.getId(), Session.class);
	}

	public void remove(Conference conference) {
		doRemoveCachedObject(conference.getId(), Session.class);
	}

	public void removeObject(String key, Class<?> type) {
		doRemoveCachedObject(key, type);
	}

	public void init() {}

	public void destroy() {}

	public abstract <T> CachedObject<T> doGetCachedObject(String key, Class<T> type);

	public abstract <T> List<CachedObject<T>> doGetCachedObjects(Class<T> type);

	public abstract <T> void doPutCachedObject(String key, CachedObject<T> cachedObject);

	public abstract <T> void doRemoveCachedObject(String key, Class<T> type);

	protected <T> List<T> checkValid(List<CachedObject<T>> co) {
		ArrayList<T> result = new ArrayList<T>();
		for (CachedObject<T> t : co) {
			T valid = checkValid(t);
			if (valid != null) {
				result.add(valid);
			}
		}
		return result;
	}

	protected <T> T checkValid(CachedObject<T> co) {
		if (co == null) {
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
