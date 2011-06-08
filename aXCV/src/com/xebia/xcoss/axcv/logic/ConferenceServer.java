package com.xebia.xcoss.axcv.logic;

import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.Unit;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Remark;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;

public class ConferenceServer {

	private String baseUrl;
	private String token;
	
	private static ConferenceServer instance;
	
	private ConferenceCache conferenceCache;

	public static ConferenceServer getInstance() {
		if (instance == null || instance.isLoggedIn() == false) {
			return null;
		}
		return instance;
	}

	public static ConferenceServer createInstance(String user, String password, String url) {
		ConferenceServer server = new ConferenceServerWrapper(url);
		server.login(user, password);
		instance = server;
		return instance;
	}

	protected ConferenceServer(String base) {
		this.baseUrl = base;
		this.conferenceCache = new ConferenceCache();
	}

	public boolean login(String user, String password) {
		// TODO implement
		this.token = "test";
		return isLoggedIn();
	}

	public boolean isLoggedIn() {
		return (this.token != null);
	}

	public Conference getConference(DateTime date) {
		Conference result = conferenceCache.getConference(date);
		if ( result == null ) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/conference/on/");
			requestUrl.append(date.format("YYYYMMDD"));
			
			result = RestClient.loadObject(requestUrl.toString(), Conference.class);
			conferenceCache.add(result);
		}
		return result;
	}
	
	public Conference getConference(int id) {
		Conference result = conferenceCache.getConference(id);
		if ( result == null ) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/conference/");
			requestUrl.append(id);
			
			result = RestClient.loadObject(requestUrl.toString(), Conference.class);
			conferenceCache.add(result);
		}
		return result;
	}
	
	public List<Conference> getConferences(Integer year) {
		List<Conference> result = conferenceCache.getConferences(year);
		if ( result == null ) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/conferences/");
			requestUrl.append(year);
	
			result = RestClient.loadObjects(requestUrl.toString(), "conferences", Conference.class);
			conferenceCache.add(result);
		}
		return result;
	}

	public List<Conference> getConferences(DateTime date) {
		List<Conference> result = conferenceCache.getConferences(date);
		if ( result == null ) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/conferences/");
			requestUrl.append(date.getYear());
			if ( date.unitsAllPresent(Unit.MONTH)) {
				requestUrl.append("/");
				requestUrl.append(date.getMonth());
				if ( date.unitsAllPresent(Unit.DAY)) {
					requestUrl.append("/");
					requestUrl.append(date.getDay());
				}
			}	
			result = RestClient.loadObjects(requestUrl.toString(), "conferences", Conference.class);
			conferenceCache.add(result);
		}
		return result;
	}

	public int storeConference(Conference conference, boolean update) {
		conferenceCache.remove(conference);
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/conference/");
		requestUrl.append(conference.getId());
		
		if ( update ) {
			RestClient.updateObject(requestUrl.toString(), conference);
			return -1;
		}
		return RestClient.createObject(requestUrl.toString(), conference);
	}

	public void deleteConference(Conference conference) {
		conferenceCache.remove(conference);
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/conference/");
		requestUrl.append(conference.getId());
		
		RestClient.deleteObject(requestUrl.toString());
	}

	public List<Session> getSessions(Conference conference) {
		// Conference is cached, so no need to do it for the sessions
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/conference/");
		requestUrl.append(conference.getId());
		requestUrl.append("/sessions");

		List<Session> result = RestClient.loadObjects(requestUrl.toString(), "sessions", Session.class);
		for (Session session : result) {
//			conference.addSession(session);
		}
		conferenceCache.addSessions(result);
		return result;
	}

	public Session getSession(int id) {
		Session result = conferenceCache.getSession(id);
		if ( result == null ) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(baseUrl);
			requestUrl.append("/session/");
			requestUrl.append(id);
	
			result = RestClient.loadObject(requestUrl.toString(), Session.class);
			conferenceCache.add(result);
		}
		return result;
	}

	public int storeSession(Session session, int conferenceId, boolean update) {
		conferenceCache.remove(session);
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/conference/");
		requestUrl.append(conferenceId);
		requestUrl.append("/session");
		
		if ( update ) {
			RestClient.updateObject(requestUrl.toString(), session);
			return -1;
		}
		return RestClient.createObject(requestUrl.toString(), session);
	}

	public void deleteSession(Session session) {
		conferenceCache.remove(session);
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		requestUrl.append("/session/");
		requestUrl.append(session.getId());
		
		RestClient.deleteObject(requestUrl.toString());
	}

	public Author[] getAllAuthors() {
		// TODO implement

		Author[] authors = new Author[3];
		authors[0] = new Author("eembsen", "Erwin Embsen", "eembsen@xebia.com");
		authors[1] =  new Author("guido", "Guido Schoonheim", "xita@xebia.com");
		authors[2] =  new Author("marnix", "Marnix van Wendel de Joode", "info@xebia.com");
		return authors;
	}
	
	public void registerRate(Session session, int rate) {
		// TODO implement
	}

	public double getRate(Session session) {
		// TODO implement
		return 5.6;
	}

	public Remark[] getRemarks(Session session) {
		// TODO implement

		Remark[] remarks = new Remark[3];
		remarks[0] = new Remark("Erwin", "I've found better sessions to join");
		remarks[1] = new Remark("Guido","Cum on, positive feedback &copy;");
		remarks[2] = new Remark("Marnix","No, it is kidda cool this stuf");
		return remarks;
	}

	public void registerRemark(String remark) {
		// TODO Auto-generated method stub
		Log.w(XCS.LOG.ALL, "Not implemented: " + remark);
	}

	/* Utility functions */

	public List<Conference> getUpcomingConferences(int size) {
		DateTime now = DateTime.today(XCS.TZ);
		Integer yearValue = now.getYear();

		List<Conference> list = findUpcomingConferences(yearValue, size);
		int delta = size - list.size();
		if ( delta > 0 ) {
			list.addAll(findUpcomingConferences(yearValue+1, delta));
		}
		return list;
	}

	public Conference getUpcomingConference() {
		return getUpcomingConference(DateTime.today(XCS.TZ));
	}

	public Conference getUpcomingConference(DateTime dt) {
		List<Conference> list = getConferences(dt.getYear());

		if ( list.isEmpty() ) {
			return null;
		}
		
		for (Conference conference : list) {
			DateTime cdate = conference.getDate();
			if ( cdate.isInThePast(XCS.TZ) && !cdate.isSameDayAs(dt)) {
				continue;
			}
			// List is sorted on date
			return conference;
		}
		// No conference in this year.
		return getUpcomingConference(DateTime.forDateOnly(dt.getYear()+1, 1, 1));
	}

	private List<Conference> findUpcomingConferences(int yearValue, int size) {
		ArrayList<Conference> list = new ArrayList<Conference>();
		
		List<Conference> cfs = getConferences(yearValue);
		for (Conference conference : cfs) {
			DateTime cdate = conference.getDate();
			if ( cdate.plusDays(1).isInThePast(XCS.TZ)) {
				continue;
			}
			list.add(conference);
		}
		return list;
	}

}
