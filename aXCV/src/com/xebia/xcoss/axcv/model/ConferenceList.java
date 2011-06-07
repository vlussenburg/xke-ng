package com.xebia.xcoss.axcv.model;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.util.Log;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.model.util.ConferenceComparator;
import com.xebia.xcoss.axcv.util.XCS;

public class ConferenceList {

	private HashMap<String, Set<Conference>> conferences;
	
	public ConferenceList() {
		conferences = new HashMap<String, Set<Conference>>();
	}
	
	public void addConference(Conference conference) {
		if ( conference == null ) {
			return;
		}
		String year = null;
		DateTime date = conference.getDate();
		if ( date != null ) {
			year = String.valueOf(date.getYear());
		}
		getConferences(year).add(conference);
	}
	
	public Set<Conference> getConferences(String year) {
		if ( year == null || year.trim().length() == 0 ) {
			DateTime now = DateTime.now(XCS.TZ);
			year = String.valueOf(now.getYear());
		}
		Set<Conference> set = conferences.get(year);
		if ( set == null ) {
			set = new TreeSet<Conference>(new ConferenceComparator());
			conferences.put(year, set);
			ConferenceServer.getInstance().loadConferences(year, set);
		}
		return set;
	}

	public Conference[] getConferencesAsList(String year) {
		Set<Conference> collection = getConferences(year);
		return collection.toArray(new Conference[collection.size()]);
	}

	public Conference findConferenceById(int id) {
		for (Set<Conference> result : conferences.values()) {
			for (Conference conference : result) {
				if ( id == conference.getId()) {
					Log.e("XCS", "Conference is " + conference);
					return conference;
				}
			}
		}
		return null;
	}
	
	public Conference getUpcomingConference() {
		return getFirstConference(DateTime.today(XCS.TZ));
	}

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
	
	private List<Conference> findUpcomingConferences(int yearValue, int size) {
		String year = String.valueOf(yearValue);
		ArrayList<Conference> list = new ArrayList<Conference>();
		
		Set<Conference> cfs = getConferences(year);
		for (Conference conference : cfs) {
			DateTime cdate = conference.getDate();
			if ( cdate.plusDays(1).isInThePast(XCS.TZ)) {
				continue;
			}
			list.add(conference);
		}
		return list;
	}

	public Conference findConferenceByName(String name) {
		if ( name != null ) {
			for (Set<Conference> result : conferences.values()) {
				for (Conference conference : result) {
					if ( name.equals(conference.getTitle()) ) {
						return conference;
					}
				}
			}
		}
		return null;
	}

	public Conference getFirstConference(DateTime dt) {
		String year = String.valueOf(dt.getYear());
		Set<Conference> cfs = getConferences(year);

		if ( cfs.isEmpty() ) {
			return null;
		}
		
		for (Conference conference : cfs) {
			DateTime cdate = conference.getDate();
			if ( cdate.isInThePast(XCS.TZ) && !cdate.isSameDayAs(dt)) {
				continue;
			}
			// List is sorted on date
			return conference;
		}
		// No conference in this year.
		return getFirstConference(DateTime.forDateOnly(dt.getYear()+1, 1, 1));
	}
}
