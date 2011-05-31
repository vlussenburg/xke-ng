package com.xebia.xcoss.axcv.logic;

import android.util.Log;

import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;


public class ConferenceServer {

	private static ConferenceServer instance;
	
	public static ConferenceServer getInstance() {
		if ( instance == null || instance.isLoggedIn() == false ) {
			return null;
		}
		return instance;
	}

	public static ConferenceServer createInstance(String user, String password, String url) {
		ConferenceServer server = new ConferenceServer(url);
		server.login(user,password);
		instance = server;
		return instance;
	}

	private String baseUrl;
	private String token;
	
	private ConferenceServer(String base) {
		this.baseUrl = base;
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

	public CharSequence getComments(Session session) {
		// TODO implement

		StringBuilder builder = new StringBuilder();
		builder.append("Erwin - I've found better sessions to join");
		builder.append(System.getProperty("line.separator"));
		builder.append("Guido - Cum on, positive feedback");
		builder.append(System.getProperty("line.separator"));
		builder.append("Marnix - No, it is kidda cool this stuf");
		return builder.toString();
	}

	public void registerRemark(String remark) {
		// TODO Auto-generated method stub
		Log.w(XCS.LOG.ALL, "Not implemented: " + remark);
	}
}
