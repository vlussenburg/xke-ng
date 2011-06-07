package com.xebia.xcoss.axcv.logic;

import java.util.Set;

import android.util.Log;

import com.xebia.xcoss.axcv.TestUtil;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.ConferenceList;
import com.xebia.xcoss.axcv.model.Remark;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;

public class ConferenceServer {

	private String baseUrl;
	private String token;
	private ConferenceList conferenceList;
	
	private static ConferenceServer instance;

	public static ConferenceServer getInstance() {
		if (instance == null || instance.isLoggedIn() == false) {
			return null;
		}
		return instance;
	}

	public static ConferenceServer createInstance(String user, String password, String url) {
		ConferenceServer server = new ConferenceServer(url);
		server.login(user, password);
		instance = server;
		return instance;
	}

	private ConferenceServer(String base) {
		this.baseUrl = base;
		this.conferenceList = new ConferenceList();
	}

	public ConferenceList getConferences() {
		return conferenceList;
	}

	public boolean login(String user, String password) {
		// TODO implement
		this.token = "test";
		return isLoggedIn();
	}

	public boolean isLoggedIn() {
		return (this.token != null);
	}

	public void registerRate(int rate) {
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

	public void loadConferences(String year, Set<Conference> set) {
		Set<Conference> conferences = getConferences().getConferences(year);
		if ( conferences.isEmpty() ) {
			// TODO Temp, temp temp...
			TestUtil.createConferences(conferences);
		}
	}

	public void loadSessions(Conference conference, Set<Session> set) {
		// TODO Auto-generated method stub
	}

	public void storeSession(Session session) {
		// TODO Auto-generated method stub
	}

	public String[] getAllAuthors() {
		// TODO implement

		String[] authors = new String[3];
		authors[0] = "Erwin Embsen";
		authors[1] = "Guido Schoonheim";
		authors[2] = "Marnix van Wendel de Joode";
		return authors;
	}
}
