package com.xebia.xcoss.axcv.model;

import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.DayOverflow;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.util.Log;

import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class Session implements Serializable {

	public enum Type {
		STANDARD("Presentation"), WORKSHOP("Workshop"), BRAINSTORM("Brainstorm"), BOOK("Book review"), SUMMARY("Report"), STRATEGIC(
				"Strategy"), INCUBATOR("Incubator"),

		MANDATORY("Corporate (mandatory)"), BREAK("Break"); // This is also Mandatory

		private String text;

		private Type(String txt) {
			this.text = txt;
		}

		public String toString() {
			return text;
		}

		public boolean hasDetails() {
			return (this != MANDATORY && this != BREAK);
		}
	}

	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_DURATION = 60;

	// Auto mapped
	private Long id;
	private String title;
	private String description;
	private DateTime startTime;
	private DateTime endTime;
	private String limit = "Unlimited";
	private Type type = Type.STANDARD;
	private Set<Author> authors;
	private Location location;
	private Set<String> labels;
	
	private transient String conferenceId;
	
	// TODO : Not mapped at all at the moment...
	private String intendedAudience;
	private String preparation;
	private Set<String> languages;

	public Session() {
		labels = new TreeSet<String>();
		authors = new TreeSet<Author>();
		languages = new HashSet<String>();
	}

	public Session(Session original) {
		this();

		id = original.id;
		type = original.type;

		title = original.title;
		location = original.location;
		startTime = original.startTime;
		endTime = original.endTime;
		description = original.description;
		intendedAudience = original.intendedAudience;
		limit = original.limit;
		preparation = original.preparation;

		authors.addAll(original.authors);
		labels.addAll(original.labels);
		languages.addAll(original.languages);
	}

	public String getId() {
		return id.toString();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Set<Author> getAuthors() {
		return authors;
	}

	public void addAuthor(Author author) {
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
		if (location != null) {
			this.location = location;
		}
	}

	public DateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(DateTime time) {
		int duration = getDuration();
		startTime = updateTime(startTime, time);
		setEndTime(startTime.plus(0, 0, 0, 0, duration, 0, DayOverflow.Spillover));
	}

	public DateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(DateTime time) {
		endTime = updateTime(endTime, time);
	}

	private DateTime updateTime(final DateTime baseTime, final DateTime time) {
		DateTime result = baseTime;
		if ( result == null ) {
			result = DateTime.now(XCS.TZ);
		}
		if (time.hasYearMonthDay()) {
			result = new DateTime(time.getYear(), time.getMonth(), time.getDay(), result.getHour(), result.getMinute(), 0, 0);
		}
		if (time.hasHourMinuteSecond()) {
			result = new DateTime(result.getYear(), result.getMonth(), result.getDay(), time.getHour(), time.getMinute(), 0, 0);
		}
		return result;
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

	/**
	 * Only allow the type to be set if it has no id yet.
	 */
	public void setType(Type type) {
		if (id == null) {
			this.type = type;
		} else {
			Log.w(XCS.LOG.PROPERTIES, "Could not set type to " + type + ", id = " + id);
		}
	}

	public Type getType() {
		return type;
	}

	public int getDuration() {
		int duration = DEFAULT_DURATION;
		if (startTime != null && endTime != null) {
			int start = startTime.getHour() * 60 + startTime.getMinute();
			int end = endTime.getHour() * 60 + endTime.getMinute();
			duration = end - start;
		}
		return duration;
	}

	public boolean check(List<String> messages) {
		if (startTime == null) {
			messages.add("Start time");
		}
		if (StringUtil.isEmpty(title)) {
			messages.add("Title");
		}
		if (StringUtil.isEmpty(description)) {
			messages.add("Description");
		}
		if ( type != Type.BREAK ) {
			if (authors.isEmpty()) {
				messages.add("Author");
			}
			if (StringUtil.isEmpty(limit)) {
				messages.add("Number of people");
			}
		}
		if (location == null) {
			messages.add("Location");
		}
		return (messages.size() == 0);
	}

	public int calculateCompleteness(int maxInclusive) {
		int value = 0;
		// Title: Max 10
		if (!StringUtil.isEmpty(title)) {
			value += Math.min(10, title.length() / 2);
			Log.v(XCS.LOG.ALL, "Title boosted value to " + value);
		}
		// Description: Max 35
		if (!StringUtil.isEmpty(description)) {
			value += Math.min(35, description.replaceAll("\\w", "").length() * 1.4);
			Log.v(XCS.LOG.ALL, "Description boosted value to " + value);
		}
		// startTime: Max 5
		if (startTime != null) {
			value += 5;
			Log.v(XCS.LOG.ALL, "Start time boosted value to " + value);
		}
		// Location: Max 5
		if (location != null) {
			value += 5;
			Log.v(XCS.LOG.ALL, "Location boosted value to " + value);
		}
		// Author: Max 5
		if (authors != null && authors.size() > 0) {
			value += 5;
			Log.v(XCS.LOG.ALL, "Authors boosted value to " + value);
		}
		// Labels: Max 10
		if (labels != null) {
			value += Math.min(10, labels.size() * 3);
			Log.v(XCS.LOG.ALL, "Labels boosted value to " + value);
		}
		// Languages: Max 5
		if (languages != null && languages.size() > 0) {
			value += 5;
			Log.v(XCS.LOG.ALL, "Languages boosted value to " + value);
		}
		// Preparation: Max 5
		if (!StringUtil.isEmpty(preparation)) {
			value += Math.min(5, preparation.length() / 3);
			Log.v(XCS.LOG.ALL, "Preparation boosted value to " + value);
		}
		// intendedAudience: Max 20
		if (!StringUtil.isEmpty(intendedAudience)) {
			value += Math.min(20, intendedAudience.length());
			Log.v(XCS.LOG.ALL, "Audience boosted value to " + value);
		}
		Log.i(XCS.LOG.ALL, "Completeness (100) = " + value);

		return value == 0 ? 0 : Math.max(1, (value * maxInclusive) / 100);
	}

	@Override
	public String toString() {
		return "Session [title=" + title + ", id=" + id + ", description=" + description + ", location=" + location
				+ ", startTime=" + startTime + ", endTime=" + endTime + ", intendedAudience=" + intendedAudience
				+ ", limit=" + limit + ", preparation=" + preparation + ", authors=" + authors + ", labels=" + labels
				+ ", languages=" + languages + "]";
	}

	public boolean isMandatory() {
		return (type != null && (type == Type.MANDATORY || type == Type.BREAK));
	}

	public boolean isBreak() {
		return (type != null && type == Type.BREAK);
	}

	public boolean isExpired() {
		if (getEndTime() == null) {
			return false;
		}
		return getEndTime().isInThePast(XCS.TZ);
	}

	public boolean isRunning() {
		if (getStartTime() == null) {
			return false;
		}
		return DateTime.now(XCS.TZ).gteq(getStartTime()) && !isExpired();
	}

	public long getModificationHash() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void setConferenceId(String conferenceId) {
		this.conferenceId = conferenceId;
	}
	
	public String getConferenceId() {
		return conferenceId;
	}
}
