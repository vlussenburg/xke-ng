package com.xebia.xcoss.axcv.model;

import hirondelle.date4j.DateTime;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import android.util.Log;

import com.xebia.xcoss.axcv.TestUtil;
import com.xebia.xcoss.axcv.model.util.ConferenceComparator;
import com.xebia.xcoss.axcv.util.XCS;

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

			// TODO Temp, temp temp...
			TestUtil.createConferences(set);
		}
		return set;
	}

	public Conference findConferenceById(int id) {
		for (Set<Conference> result : conferences.values()) {
			for (Conference conference : result) {
				if ( id == conference.getId()) {
					return conference;
				}
			}
		}
		return null;
	}
	
	public Conference getUpcomingConference() {
		DateTime dt = DateTime.today(XCS.TZ);
		String year = String.valueOf(dt.getYear());
		Set<Conference> cfs = getConferences(year);

		for (Conference conference : cfs) {
			DateTime cdate = conference.getDate();
			if ( cdate.isInThePast(XCS.TZ) && !cdate.isSameDayAs(dt)) {
//				Log.i(XCS.LOG.ALL, "Past: " + cdate);
//				Log.i(XCS.LOG.ALL, "Future ? " + cdate.isInTheFuture(XCS.TZ));
//				Log.i(XCS.LOG.ALL, "Today ? " + cdate.isSameDayAs(dt));
				continue;
			}
			// List is sorted on date
			return conference;
		}
		return null;
	}

}
