package com.xebia.xcoss.axcv.model;

import hirondelle.date4j.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Session implements Serializable {

	private static final long serialVersionUID = 1L;

	private static int counter = 899;

	private Conference conference = null;

	private int id;

	private String title;
	private Location location;
	private DateTime date;
	private DateTime startTime;
	private DateTime endTime;
	private String description;
	private String intendedAudience;
	private String limit;
	private String preparation;

	private Set<String> authors;
	private Set<String> labels;
	private Set<String> languages;

	public Session() {
		labels = new TreeSet<String>();
		authors = new TreeSet<String>();
		languages = new HashSet<String>();
		location = new Location("TODO");
		this.id = ++counter;
	}

	public int getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Set<String> getAuthors() {
		return authors;
	}

	public void addAuthor(String author) {
		authors.add(author);
	}
	
	public void removeAuthor(String author) {
		authors.remove(author);
	}

	public Set<String> getLabels() {
		return labels;
	}

	public void addLabel(String label) {
		labels.add(label);
	}

	public void removeLabel(String label) {
		labels.remove(label);
	}
	
	public void setLabels(Set<String> labels) {
		this.labels = labels;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		if ( location != null ) {
			this.location = location;
		}
	}

	public DateTime getDate() {
		return date;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}

	public DateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}

	public DateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(DateTime endTime) {
		this.endTime = endTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIntendedAudience() {
		return intendedAudience;
	}

	public void setIntendedAudience(String intendedAudience) {
		this.intendedAudience = intendedAudience;
	}

	public Conference getConference() {
		return conference;
	}

	public void setConference(Conference conference) {
		this.conference = conference;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public String getPreparation() {
		return preparation;
	}

	public void setPreparation(String preparation) {
		this.preparation = preparation;
	}
	
	public Set<String> getLanguages() {
		return languages;
	}

	public int getDuration() {
		int duration = 0;
		if (startTime != null && endTime != null) {
			int start = startTime.getHour()*60 + startTime.getMinute();
			int end = endTime.getHour()*60 + endTime.getMinute();
			duration = end - start;
		}
		return duration;
	}
}
