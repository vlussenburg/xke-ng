package com.xebia.xcoss.axcv.model;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import android.util.Log;

import com.xebia.xcoss.axcv.model.util.ConferenceComparator;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class ConferenceList {

	private static ConferenceList instance = null;
	
	private HashMap<String, Set<Conference>> conferences;
	private Comparator<Conference> comparator;
	
	public static ConferenceList getInstance() {
		if (instance == null) {
			instance = new ConferenceList();
		}
		return instance;
	}
	
	private ConferenceList() {
		conferences = new HashMap<String, Set<Conference>>();
		comparator = new ConferenceComparator();
	}
	
	public Conference[] getConferencesAsList(String year) {
		Set<Conference> set = getConferences(year);
		return set.toArray(new Conference[set.size()]);
	}
	
	public Set<Conference> getConferences(String year) {
		Set<Conference> set = conferences.get(year);
		if ( set == null ) {
			// TODO : Load conferences
			set = new TreeSet<Conference>(comparator);
			conferences.put(year, set);
			
			// TODO : Temp
			Conference conf = new Conference();
			conf.setDate(DateTime.forDateOnly(2011, 6, 5));
			Log.w(LOG.ALL, "Date June 5th = " + conf.getDate());
			Log.w(LOG.ALL, "Date June 5th = " + conf.getDate().getRawDateString());
			set.add(conf);
			
			Conference conf2 = new Conference();
			conf2.setDate(DateTime.forDateOnly(2011, 5, 14));
			Log.w(LOG.ALL, "Date May 14th = " + conf2.getDate());
			Log.w(LOG.ALL, "Date May 14th = " + conf2.getDate().getRawDateString());
			set.add(conf2);
			
			Conference conference = new Conference();
			Log.w(LOG.ALL, "Date Today = " + conference.getDate());
			Log.w(LOG.ALL, "Date Today = " + conference.getDate().getRawDateString());
			Location loc = new Location("Laap");
			Session session = new Session();
			session.setLocation(loc);
			session.setAuthor("M. van Leeuwen");
			session.setStartTime(DateTime.forTimeOnly(16,0,0,0));
			session.setEndTime(DateTime.forTimeOnly(17,0,0,0));
			session.setTitle("Android XKE");
			ArrayList<String> labels = new ArrayList<String>();
			labels.add("Android");
			labels.add("REST");
			session.setLabels(labels);
			conference.addSession(session);
			set.add(conference);
		}
		return set;
	}

	public Conference getUpcomingConference() {
		DateTime dt = DateTime.today(XCS.TZ);
		String year = String.valueOf(dt.getYear());
		Set<Conference> cfs = getConferences(year);

		for (Conference conference : cfs) {
			DateTime cdate = conference.getDate();
			if ( cdate.isInThePast(XCS.TZ) ) {
				Log.w(LOG.ALL, "Conference on " + cdate.toString() + ": In the past" );
				continue;
			}
			Log.w(LOG.ALL, "Conference on " + cdate.toString() + ": Ok" );
			// List is sorted on date
			return conference;
		}
		return null;
	}

}
