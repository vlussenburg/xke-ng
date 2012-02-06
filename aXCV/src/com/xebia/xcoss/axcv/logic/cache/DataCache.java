package com.xebia.xcoss.axcv.logic.cache;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.xebia.xcoss.axcv.Messages;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;

public abstract class DataCache {
	protected static final long CACHETIME = 30 * 60 * 1000;
	
	public static final String CK_ALL_LOCATIONS = "CK-Location";
	public static final String CK_ALL_AUTHORS = "CK-Author";
	public static final String CK_ALL_LABELS = "CK-Label";

	public enum Type {
		Memory(MemoryCache.class), Database(DatabaseCache.class), None(NoCache.class);

		private Class<?> clazz;

		Type(Class<?> clz) {
			this.clazz = clz;
		}

		public DataCache newInstance(Context ctx) throws Exception {
			return (DataCache) clazz.getConstructor(Context.class).newInstance(ctx);
		}
	}

	public DataCache(Context ctx) {}

	public <T> T getObject(String key, Class<T> type) {
		return checkValid(doGetCachedObject(key, type));
	}

	public Conference getConference(String id) {
		return checkValid(doGetCachedObject(id, Conference.class));
	}

	public List<Conference> getConferences(Integer year) {
		List<Conference> result = new ArrayList<Conference>();
		for (CachedObject<Conference> co : doGetCachedObjects(Conference.class)) {
			if (co.object.getStartTime().getYear().equals(year)) {
				// Not checking on validity...
				if (co.object != null) {
					result.add(co.object);
				}
			}
		}
		return result;
	}

	public Conference getConference(Moment date) {
		for (CachedObject<Conference> co : doGetCachedObjects(Conference.class)) {
			if (co.object.getStartTime().compare(date) == 0) {
				return checkValid(co);
			}
		}
		return null;
	}

	public List<Conference> getConferences(Moment date) {
		List<Conference> list = getConferences(date.getYear());
		if (list == null) {
			return list;
		}
		List<Conference> result = new ArrayList<Conference>();
		for (Conference conference : list) {
			Moment m = conference.getStartTime();
			if (date.getMonth() == null) {
				result.add(conference);
			} else if (date.getMonth().equals(m.getMonth())) {
				if (date.getDay() == null || date.getDay().equals(m.getDay())) {
					result.add(conference);
				}
			}
		}
		return result;
	}

	public Session getSession(String id) {
		return checkValid(doGetCachedObject(id, Session.class));
	}

	public List<Session> getSessions(String id) {
		ArrayList<Session> sessions = new ArrayList<Session>();
		for (CachedObject<Session> co : doGetCachedObjects(Session.class)) {
			if (co.object != null && co.object.getConferenceId() != null && co.object.getConferenceId().equals(id)) {
				sessions.add(checkValid(co, false));
			}
		}
		return sessions;
	}

	/**
	 * The cached conference is now made invalid.
	 *  
	 * @param conferenceId
	 * @param result
	 */
	public void add(String conferenceId, Session result) {
		if ( conferenceId == null ) {
			throw new RuntimeException(Messages.getString("Exception.0"));
		}
		if (result != null && result.getId() != null) {
			result.setConferenceId(conferenceId);
			doPutCachedObject(result.getId().toString(), new CachedObject<Session>(result));
		}
	}

	public void add(Conference result) {
		CachedObject<Conference> cachedObject = new CachedObject<Conference>(result);
		if (result != null) {
			doPutCachedObject(result.getId(), cachedObject);
			for (Session session : result.getSessions()) {
				add(result.getId(), session);
			}
		}
	}

	public <T> void addObject(String key, T o) {
		doPutCachedObject(key, new CachedObject<T>(o));
	}

	/**
	 * The cached conference is now made invalid
	 * 
	 * @param session
	 */
	public void remove(Session session) {
		doRemoveCachedObject(session.getId().toString(), Session.class);
	}

	public void remove(Conference conference) {
		doRemoveCachedObject(conference.getId(), Conference.class);
		for (Session session : conference.getSessions()) {
			remove(session);
		}
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
		return checkValid(co, true);
	}

	protected <T> T checkValid(CachedObject<T> co, boolean discard) {
		if (co == null) {
			return null;
		}
		long now = System.currentTimeMillis();
		if (co.dirty || (co.moment + CACHETIME) < now) {
			Log.d(XCS.LOG.CACHE, "Cache hit, but dirty: " + co.object);
			co.dirty = true;
			return discard ? null : co.object;
		}
		Log.d(XCS.LOG.CACHE, "Cache hit: " + co.object);
		return co.object;
	}

	public static String getKey(String... objects) {
		StringBuilder sb = new StringBuilder("CK");
		for (String object : objects) {
			sb.append("-");
			sb.append(object);
		}
		return sb.toString();
	}
}
