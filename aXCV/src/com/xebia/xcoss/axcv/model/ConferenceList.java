package com.xebia.xcoss.axcv.model;

import hirondelle.date4j.DateTime;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

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
		DateTime date = conference.getDate();
		if ( date == null ) {
			DateTime now = DateTime.now(XCS.TZ);
			conference.setDate(now);
			date = now;
		}
		String year = String.valueOf(date.getYear());
		getConferences(year).add(conference);
	}
	
	public Set<Conference> getConferences(String year) {
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
				continue;
			}
			// List is sorted on date
			return conference;
		}
		return getConferences(String.valueOf(dt.getYear()+1)).iterator().next();
	}
}
