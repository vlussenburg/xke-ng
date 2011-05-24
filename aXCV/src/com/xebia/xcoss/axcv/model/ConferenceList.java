package com.xebia.xcoss.axcv.model;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import android.util.Log;

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
			
			// TODO : Temp
			Conference conf = new Conference();
			conf.setDate(new Date(111, 5, 5));
			set.add(conf);
			Conference conf2 = new Conference();
			conf2.setDate(new Date(111, 4, 14));
			set.add(conf2);
			set.add(new Conference());
		}
		return set;
	}

}
