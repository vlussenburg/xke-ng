package com.xebia.xcoss.axcv.logic;

import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.Unit;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Remark;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.model.util.ConferenceComparator;
import com.xebia.xcoss.axcv.model.util.SessionComparator;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class ConferenceServer {

	private static final String AUTHOR_CACHE_KEY = "authcache--";
	private static final String LOCATION_CACHE_KEY = "locscache--";
	private static final String LABEL_CACHE_KEY = "labelcache--";
	private static final String AUTHOR_LABEL_CACHE_KEY = "aulblcache--";
	private static final String REMARK_CACHE_KEY = "remarkcache--";

	private String baseUrl;
	private String token;

	private static ConferenceServer instance;

	private ConferenceCache conferenceCache;

	public static ConferenceServer getInstance() {
		return instance;
	}

	public static ConferenceServer createInstance(String user, String password, String url, Context ctx) {
		ConferenceServer server = new ConferenceServerProxy(url, ctx);
		instance = server;
		instance.login(user, password);
		return instance;
	}

	protected ConferenceServer(String base, Context ctx) {
		this.baseUrl = base;
		this.conferenceCache = new PersistentConferenceCache(ctx);
		this.conferenceCache.init();
	}

	public void login(String user, String password) {
		// TODO : Not yet implemented on EC2
		this.token = "NoAuthenticationYet";
		// StringBuilder requestUrl = new StringBuilder();
		// requestUrl.append(baseUrl);
		// requestUrl.append("/login");
		// this.token = RestClient.postObject(requestUrl.toString(), new Credential(user, password), String.class,
		// null);
	}

	public boolean isLoggedIn() {
		return (this.token != null);
	}

	public Conference getConference(DateTime date) {
		List<Conference> conferences = getConferences(date);
		if (conferences == null || conferences.isEmpty()) {
			return null;
		}
		return conferences.get(0);
	}

	public Conference getConference(String id) {
		Conference result = conferenceCache.getConference(id);
		if (result == null) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/conference/");
			requestUrl.append(id);

			result = RestClient.loadObject(requestUrl.toString(), Conference.class, token);
			conferenceCache.add(result);
		}
		return result;
	}

	public List<Conference> getConferences(Integer year) {
		List<Conference> result = conferenceCache.getConferences(year);
		if (result == null || result.isEmpty()) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/conferences/");
			requestUrl.append(year);

			result = RestClient.loadObjects(requestUrl.toString(), Conference.class, token);
			if (result == null) {
				return new ArrayList<Conference>();
			}
			for (Conference conference : result) {
				conferenceCache.add(conference);
			}
		}
		Collections.sort(result, new ConferenceComparator());
		for (Conference conference : result) {
			Log.w(LOG.ALL, "[result] " + conference);
		}
		return result;
	}

	public List<Conference> getConferences(DateTime date) {
		List<Conference> result = conferenceCache.getConferences(date);
		if (result == null || result.isEmpty()) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/conferences/");
			requestUrl.append(date.getYear());
			if (date.unitsAllPresent(Unit.MONTH)) {
				requestUrl.append("/");
				requestUrl.append(date.getMonth());
				if (date.unitsAllPresent(Unit.DAY)) {
					requestUrl.append("/");
					requestUrl.append(date.getDay());
				}
			}
			result = RestClient.loadObjects(requestUrl.toString(), Conference.class, token);
			if (result == null) {
				return new ArrayList<Conference>();
			}
			for (Conference conference : result) {
				conferenceCache.add(conference);
			}
		}
		Collections.sort(result, new ConferenceComparator());
		return result;
	}

	public String storeConference(Conference conference, boolean create) {
		conferenceCache.remove(conference);
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/conference");

		if (create) {
			return RestClient.createObject(requestUrl.toString(), conference, String.class, token);
		}
		RestClient.updateObject(requestUrl.toString(), conference, token);
		return conference.getId();
	}

	public void deleteConference(Conference conference) {
		conferenceCache.remove(conference);
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/conference/");
		requestUrl.append(conference.getId());

		RestClient.deleteObject(requestUrl.toString(), token);
	}

	public List<Session> getSessions(Conference conference) {
		Class<?> type = Array.newInstance(Session.class, 0).getClass();
		Session[] sessions = (Session[]) conferenceCache.getObject(conference.getId(), type);
		if (sessions != null) {
			return Arrays.asList(sessions);
		}

		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/conference/");
		requestUrl.append(conference.getId());
		requestUrl.append("/sessions");

		List<Session> result = RestClient.loadObjects(requestUrl.toString(), Session.class, token);
		if (result == null) {
			return new ArrayList<Session>();
		}

		conferenceCache.addObject(conference.getId(), result.toArray(new Session[result.size()]));
		for (Session session : result) {
			conferenceCache.add(session);
		}

		Collections.sort(result, new SessionComparator());
		return result;
	}

	public Session getSession(String id) {
		Session result = conferenceCache.getSession(id);
		if (result == null) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/session/");
			requestUrl.append(id);

			result = RestClient.loadObject(requestUrl.toString(), Session.class, token);
			conferenceCache.add(result);
		}
		return result;
	}

	public String storeSession(Session session, String conferenceId, boolean create) {
		conferenceCache.remove(session);
		conferenceCache.removeConference(conferenceId);
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/conference/");
		requestUrl.append(conferenceId);
		requestUrl.append("/session");

		if (create) {
			return RestClient.createObject(requestUrl.toString(), session, String.class, token);
		}
		RestClient.updateObject(requestUrl.toString(), session, token);
		return session.getId();
	}

	public void deleteSession(Session session, String conferenceId) {
		conferenceCache.remove(session);
		conferenceCache.remove(conferenceCache.getConference(conferenceId));
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/session/");
		requestUrl.append(session.getId());

		RestClient.deleteObject(requestUrl.toString(), token);
	}

	public Author[] getAllAuthors() {
		List<Author> result = conferenceCache.getObject(AUTHOR_CACHE_KEY, new ArrayList<Author>().getClass());
		if (result == null || result.isEmpty()) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/authors");

			result = RestClient.loadObjects(requestUrl.toString(), Author.class, token);
			if (result == null) {
				return new Author[0];
			}
			conferenceCache.addObject(AUTHOR_CACHE_KEY, result);
		}
		return result.toArray(new Author[result.size()]);
	}

	public int createLocation(String location) {
		conferenceCache.removeObject(LOCATION_CACHE_KEY, new ArrayList<Location>().getClass());
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/location");

		return RestClient.createObject(requestUrl.toString(), location, int.class, token);
	}

	public Location[] getLocations() {
		List<Location> result = conferenceCache.getObject(LOCATION_CACHE_KEY, new ArrayList<Location>().getClass());
		if (result == null || result.isEmpty()) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/locations");

			result = RestClient.loadObjects(requestUrl.toString(), Location.class, token);
			if (result == null) {
				return new Location[0];
			}
			// TODO Server returns no unique list
			conferenceCache.addObject(LOCATION_CACHE_KEY, result);
		}
		return result.toArray(new Location[result.size()]);
	}

	public void createLabel(String name) {
		conferenceCache.removeObject(LABEL_CACHE_KEY, new ArrayList<String>().getClass());
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/label");
		// requestUrl.append(URLEncoder.encode(name));

		RestClient.createObject(requestUrl.toString(), name, void.class, token);
	}

	public String[] getLabels() {
		List<String> result = conferenceCache.getObject(LABEL_CACHE_KEY, new ArrayList<String>().getClass());
		if (result == null || result.isEmpty()) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/labels");

			Type collectionType = new TypeToken<List<String>>() {}.getType();
			result = RestClient.loadCollection(requestUrl.toString(), collectionType, token);
			if (result == null) {
				return new String[0];
			}
			conferenceCache.addObject(LABEL_CACHE_KEY, result);
		}
		return result.toArray(new String[result.size()]);
	}

	public List<String> getLabels(Author author) {
		List<String> result = conferenceCache.getObject(AUTHOR_LABEL_CACHE_KEY, new ArrayList<String>().getClass());
		if (result == null || result.isEmpty()) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/labels/author/");
			requestUrl.append(author.getUserId());

			Type collectionType = new TypeToken<List<String>>() {}.getType();
			result = RestClient.loadCollection(requestUrl.toString(), collectionType, token);
			if (result != null) {
				conferenceCache.addObject(LABEL_CACHE_KEY, result);
			}
		}
		return result;
	}

	public List<Author> searchAuthors(Search search) {
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/search/authors");

		List<Author> result = RestClient.searchObjects(requestUrl.toString(), "authors", Author.class, search, token);
		if (result == null) {
			return new ArrayList<Author>();
		}
		return result;
	}

	public List<Session> searchSessions(Search search) {
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/search/sessions");

		List<Session> result = RestClient
				.searchObjects(requestUrl.toString(), "sessions", Session.class, search, token);
		if (result == null) {
			return new ArrayList<Session>();
		}
		Collections.sort(result, new SessionComparator());
		return result;
	}

	public void registerRate(Session session, int rate) {
		// TODO Not server implemented.
		if (true) return;

		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/feedback/");
		requestUrl.append(session.getId());
		requestUrl.append("/rating");

		RestClient.createObject(requestUrl.toString(), rate, void.class, token);
	}

	public double getRate(Session session) {
		// TODO Not server implemented.
		if (true) return 0;

		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/feedback/");
		requestUrl.append(session.getId());
		requestUrl.append("/rating");

		return RestClient.loadObject(requestUrl.toString(), double.class, token);
	}

	public Remark[] getRemarks(Session session) {
		// TODO Not server implemented.
		if (true) return new Remark[0];

		String key = REMARK_CACHE_KEY + session.getId();
		List<Remark> result = (List<Remark>) conferenceCache.getObject(key, new ArrayList<Remark>().getClass());
		if (result == null) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/feedback/");
			requestUrl.append(session.getId());
			requestUrl.append("/comment");

			result = RestClient.loadObjects(requestUrl.toString(), Remark.class, token);
			if (result == null) {
				return new Remark[0];
			}
			conferenceCache.addObject(key, result);
		}
		return result.toArray(new Remark[result.size()]);
	}

	public void registerRemark(Session session, Remark remark) {
		// TODO Not server implemented.
		if (true) return;

		String key = REMARK_CACHE_KEY + session.getId();
		conferenceCache.removeObject(key, new ArrayList<Remark>().getClass());
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/feedback/");
		requestUrl.append(session.getId());
		requestUrl.append("/comment");

		RestClient.createObject(requestUrl.toString(), remark, void.class, token);
	}

	/* Utility functions */

	public Conference getNextConference(DateTime dt) {
		List<Conference> list = getConferences(dt.getYear());

		if (list.isEmpty()) {
			return null;
		}

		for (Conference conference : list) {
			DateTime cdate = conference.getDate();
			if (cdate.lt(dt) || cdate.isSameDayAs(dt)) {
				continue;
			}
			// List is sorted on date
			return conference;
		}
		// No conference in this year.
		return getNextConference(DateTime.forDateOnly(dt.getYear() + 1, 1, 1));
	}

	public Conference getPreviousConference(DateTime dt) {
		List<Conference> list = getConferences(dt.getYear());
		if (list.isEmpty()) {
			return null;
		}

		Collections.reverse(list);
		for (Conference conference : list) {
			DateTime cdate = conference.getDate();
			if (cdate.gt(dt) || cdate.isSameDayAs(dt)) {
				continue;
			}
			// List is reversed sorted on date
			return conference;
		}
		// No conference in this year.
		return getPreviousConference(DateTime.forDateOnly(dt.getYear() - 1, 1, 1));
	}

	public List<Conference> getUpcomingConferences(int size) {
		DateTime now = DateTime.today(XCS.TZ);
		Integer yearValue = now.getYear();

		List<Conference> list = findUpcomingConferences(yearValue, size);
		int delta = size - list.size();
		if (delta > 0) {
			list.addAll(findUpcomingConferences(yearValue + 1, delta));
		}
		return list;
	}

	public Conference getUpcomingConference() {
		return getUpcomingConference(DateTime.today(XCS.TZ));
	}

	public Conference getUpcomingConference(DateTime dt) {
		List<Conference> list = getConferences(dt.getYear());

		if (list.isEmpty()) {
			return null;
		}

		for (Conference conference : list) {
			DateTime cdate = conference.getDate();
			if (cdate.isInThePast(XCS.TZ) && !cdate.isSameDayAs(dt)) {
				continue;
			}
			// List is sorted on date
			return conference;
		}
		// No conference in this year.
		return getUpcomingConference(DateTime.forDateOnly(dt.getYear() + 1, 1, 1));
	}

	private List<Conference> findUpcomingConferences(int yearValue, int size) {
		ArrayList<Conference> list = new ArrayList<Conference>();

		List<Conference> cfs = getConferences(yearValue);
		for (Conference conference : cfs) {
			DateTime cdate = conference.getDate();
			if (cdate.plusDays(1).isInThePast(XCS.TZ)) {
				continue;
			}
			list.add(conference);
		}
		return list;
	}

	public static void close() {
		if (instance != null) {
			instance.token = null;
			instance.conferenceCache.destroy();
		}
	}
}
