package com.xebia.xcoss.axcv;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.Set;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.MandatorySession;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;

public class TestUtil {

	public static void createConferences(Set<Conference> set) {
		Conference createConference = createConference(0);
		set.add(createConference);
		set.add(createConference(6,5));
		set.add(createConference(5,14));
		set.add(createConference(1));
		set.add(createConference(2));
		set.add(createConference(-7));

		createConference.addSession(createSession("Dinner break", 18, true));
		createConference.addSession(createSession("Moderate Geocaching", 19));
		createConference.addSession(createSession("Android XKE", 16));
		createConference.addSession(createSession("Autobandventieldopjesfabriek", 20));
	createConference.addSession(createSession("This is a very long title for a presentation on the XKE", 17));
	}
	private static Session createSession(String title, int start) {
		return createSession(title, start, false);
	}
	
	private static Session createSession(String title, int start, boolean mandatory) {
		Location loc = new Location("Laap");
		Session session = mandatory ? new MandatorySession() : new Session();
		session.setLocation(loc);
		session.setAuthor("M. van Leeuwen");
		session.setStartTime(DateTime.forTimeOnly(start,0,0,0));
		session.setEndTime(DateTime.forTimeOnly(start+1,0,0,0));
		session.setTitle(title);
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("Android");
		labels.add("REST");
		if ( start != 19 ) {
			session.setLabels(labels);
		}
		return session;
	}

	private static Conference createConference(int i) {
		DateTime now = DateTime.now(XCS.TZ);
		DateTime corrected = (i >= 0)? now.plusDays(i) : now.minusDays(i);
		Conference conf = new Conference();
		conf.setDate(corrected);
		conf.setTitle("Delta " + i + ":" + corrected.getDay()+ "-" + corrected.getMonth());
		
		return conf;
	}

	private static Conference createConference(int i, int j) {
		Conference conf = new Conference();
		conf.setDate(DateTime.forDateOnly(2011, i, j));
		conf.setTitle("Date " + i + ":" + j);
		return conf;
	}

}
