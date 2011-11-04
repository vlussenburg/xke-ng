package com.xebia.xcoss.axcv.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.logic.cache.MemoryCache;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Credential;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.model.Rate;
import com.xebia.xcoss.axcv.model.Remark;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.model.util.ConferenceComparator;
import com.xebia.xcoss.axcv.model.util.SessionComparator;
import com.xebia.xcoss.axcv.util.XCS;

public class ConferenceServer {

	private static final String AUTHOR_CACHE_KEY = "authcache--";
	private static final String LOCATION_CACHE_KEY = "locscache--";
	private static final String LABEL_CACHE_KEY = "labelcache--";
	private static final String AUTHOR_LABEL_CACHE_KEY = "aulblcache--";
	private static final String REMARK_CACHE_KEY = "remarkcache--";

	private String baseUrl;
	private String token;

	private static ConferenceServer instance;

	private DataCache conferenceCache;

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
		// TODO Make configurable
		// this.conferenceCache = new DatabaseCache(ctx);
		// this.conferenceCache = new NoCache(ctx);
		this.conferenceCache = new MemoryCache(ctx);
		this.conferenceCache.init();
	}

	public void login(String user, String password) {
		 StringBuilder requestUrl = new StringBuilder();
		 requestUrl.append(baseUrl);
		 requestUrl.append("/login");
		 RestClient.postObject(requestUrl.toString(), new Credential(user, password), void.class, null);
		 // RestClients holds the authentication token.
		 this.token = "logged_in";
	}

	public boolean isLoggedIn() {
		return (this.token != null);
	}

	public Conference getConference(Moment date) {
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
		return result;
	}

	public List<Conference> getConferences(Moment date) {
		List<Conference> result = conferenceCache.getConferences(date);
		if (result == null || result.isEmpty()) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/conferences/");
			requestUrl.append(date.getYear());
			if (date.getMonth() != null) {
				requestUrl.append("/");
				requestUrl.append(date.getMonth());
				if (date.getDay() != null) {
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

	public Conference storeConference(Conference conference, boolean create) {
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/conference");

		Log.w(XCS.LOG.COMMUNICATE, "Conference starts at " + conference.getStartTime());
		Log.w(XCS.LOG.COMMUNICATE, "Conference ends   at " + conference.getEndTime());
		
		conferenceCache.remove(conference);
		if (create) {
			conference = RestClient.createObject(requestUrl.toString(), conference, Conference.class, token);
		} else {
			requestUrl.append('/');
			requestUrl.append(conference.getId());
			conference = RestClient.updateObject(requestUrl.toString(), conference, token);
		}
		conferenceCache.add(conference);
		return conference;
	}

	public void deleteConference(Conference conference) {
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/conference/");
		requestUrl.append(conference.getId());

		conferenceCache.remove(conference);
		RestClient.deleteObject(requestUrl.toString(), token);
	}

	public List<Session> getSessions(Conference conference) {
		List<Session> result = conferenceCache.getSessions(conference.getId());
		if (result == null) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/conference/");
			requestUrl.append(conference.getId());
			requestUrl.append("/sessions");

			result = RestClient.loadObjects(requestUrl.toString(), Session.class, token);
			if (result == null) {
				return new ArrayList<Session>();
			}

			conferenceCache.add(conference.getId(), result);
		}
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
			// Not added to the cache, since we do not know the conference ID
		}
		return result;
	}

	public String storeSession(Session session, String conferenceId, boolean create) {
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);

		String sessionId = null;
		if (create) {
			requestUrl.append("/conference/");
			requestUrl.append(conferenceId);
			requestUrl.append("/session");
			session = RestClient.createObject(requestUrl.toString(), session, Session.class, token);
			sessionId = session.getId();
		} else {
			// TODO : New variant still does not work
//			requestUrl.append("/session/");
//			requestUrl.append(session.getId());
			requestUrl.append("/conference/");
			requestUrl.append(conferenceId);
			requestUrl.append("/session");
			RestClient.updateObject(requestUrl.toString(), session, token);
			sessionId = session.getId();
		}
		conferenceCache.add(conferenceId, session);
		return sessionId;
	}

	public void deleteSession(Session session) {
		conferenceCache.remove(session);
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

	public Location createLocation(Location location) {
		conferenceCache.removeObject(LOCATION_CACHE_KEY, new ArrayList<Location>().getClass());
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/location");

		return RestClient.createObject(requestUrl.toString(), location, Location.class, token);
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
			conferenceCache.addObject(LOCATION_CACHE_KEY, result);
		}
		Collections.sort(result, new Comparator<Location>() {
			public int compare(Location object1, Location object2) {
				return object1.getDescription().compareTo(object2.getDescription());
			}
		});
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

	public void registerRate(Session session, Rate rate) {
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/feedback/");
		requestUrl.append(session.getId());
		requestUrl.append("/rating");

		RestClient.createObject(requestUrl.toString(), rate, int[].class, token);
	}

	public Rate getRate(Session session) {
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/feedback/");
		requestUrl.append(session.getId());
		requestUrl.append("/rating");

		List<Integer> list = RestClient.loadObjects(requestUrl.toString(), int.class, token);
		return new Rate(list);
	}

	public Remark[] getRemarks(Session session) {
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
		String key = REMARK_CACHE_KEY + session.getId();
		conferenceCache.removeObject(key, new ArrayList<Remark>().getClass());
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/feedback/");
		requestUrl.append(session.getId());
		requestUrl.append("/comment");

		RestClient.createObject(requestUrl.toString(), remark, Remark[].class, token);
	}

	/* Utility functions */

	public Conference getNextConference(Moment dt) {
		List<Conference> list = getConferences(dt.getYear());

		if (list.isEmpty()) {
			return null;
		}

		for (Conference conference : list) {
			Moment cdate = conference.getStartTime();
			if (!cdate.isAfter(dt)) {
				continue;
			}
			// List is sorted on date
			return conference;
		}
		// No conference in this year.
		Moment nextYear = new Moment(dt);
		nextYear.setDate(dt.getYear()+1, 1, 1);
		return getNextConference(nextYear);
	}

	public Conference getPreviousConference(Moment dt) {
		List<Conference> list = getConferences(dt.getYear());
		if (list.isEmpty()) {
			return null;
		}

		Collections.reverse(list);
		for (Conference conference : list) {
			Moment cdate = conference.getStartTime();
			if (!cdate.isBefore(dt)) {
				continue;
			}
			// List is reversed sorted on date
			return conference;
		}
		// No conference in this year.
		Moment prevYear = new Moment(dt);
		prevYear.setDate(dt.getYear()-1, 1, 1);
		return getPreviousConference(prevYear);
	}

	public List<Conference> getUpcomingConferences(int size) {
		Integer yearValue = new Moment().getYear();
		List<Conference> list = findUpcomingConferences(yearValue, size);
		int delta = size - list.size();
		if (delta > 0) {
			list.addAll(findUpcomingConferences(yearValue + 1, delta));
		}
		return list;
	}

	public Conference getUpcomingConference() {
		return getUpcomingConference(new Moment());
	}

	public Conference getUpcomingConference(Moment dt) {
		List<Conference> list = getConferences(dt.getYear());

		if (list.isEmpty()) {
			return null;
		}

		for (Conference conference : list) {
			Moment cdate = conference.getStartTime();
			if (cdate.isBeforeToday()) {
				continue;
			}
			// List is sorted on date
			return conference;
		}
		// No conference in this year.
		Moment nextYear = new Moment(dt);
		nextYear.setDate(dt.getYear()+1, 1, 1);
		return getUpcomingConference(nextYear);
	}

	private List<Conference> findUpcomingConferences(int yearValue, int size) {
		ArrayList<Conference> list = new ArrayList<Conference>();

		List<Conference> cfs = getConferences(yearValue);
		for (Conference conference : cfs) {
			Moment cdate = conference.getStartTime();
			if (cdate.isBeforeNow()) {
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
