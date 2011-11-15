package com.xebia.xcoss.axcv.test;

import android.test.AndroidTestCase;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.logic.DataException;

public class UrlTest extends AndroidTestCase {

	private ConferenceServer server;
	private String username;
	private String password;

	public UrlTest() {
		server = ConferenceServer.createInstance(null, null, "http://10.0.2.2:8080", null);
		username = "mvanleeuwen";
		password = "geheim";
	}

	public void testUrls() {
		server.login(username, password);
		assertNotNull(server.getLocations());
	}
}
