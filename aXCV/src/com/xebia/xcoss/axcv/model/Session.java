package com.xebia.xcoss.axcv.model;

import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.DayOverflow;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class Session implements Serializable {

	public enum Type {
		STANDARD ("Standard session"), 
		MANDATORY ("Mandatory session"), 
		BREAK ("Break"); // This is also Mandatory
		
		private String text;
		private Type(String txt) { this.text = txt; }
		public String toString() { return text; } 
	}

	private static final long serialVersionUID = 1L;

	// Auto mapped
	private String title;
	private int id;
	private Type type = Type.STANDARD;

	private String description;

	private Location location;
	private DateTime date;
	private DateTime startTime;
	private DateTime endTime;

	@SerializedName("audience")
	private String intendedAudience;
	private String limit;
	private String preparation;

	private Set<Author> authors;
	private Set<String> labels;
	private Set<String> languages;

	public Session() {
		labels = new TreeSet<String>();
		authors = new TreeSet<Author>();
		languages = new HashSet<String>();
	}

	public Session(Session original) {
		this();

		id = original.id;

		title = original.title;
		location = original.location;
		date = original.date;
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

	public int getId() {
		return id;
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
		if (id == 0) {
			this.type = type;
		} else {
			Log.w(XCS.LOG.PROPERTIES, "Could not set type to "+type+", id = " + id);
		}
	}

	public Type getType() {
		return type;
	}

	public int getDuration() {
		int duration = 60;
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
		if (type != Type.BREAK && authors.isEmpty()) {
			messages.add("Author");
		}
		if (location == null) {
			messages.add("Location");
		}
		return (messages.size() == 0);
	}

	@Override
	public String toString() {
		return "Session [title=" + title + ", id=" + id + ", description=" + description + ", location=" + location
				+ ", date=" + date + ", startTime=" + startTime + ", endTime=" + endTime + ", intendedAudience="
				+ intendedAudience + ", limit=" + limit + ", preparation=" + preparation + ", authors=" + authors
				+ ", labels=" + labels + ", languages=" + languages + "]";
	}

	public boolean isMandatory() {
		return (type != null && (type == Type.MANDATORY || type == Type.BREAK));
	}

	public boolean isBreak() {
		return (type != null && type == Type.BREAK);
	}

	public boolean isExpired() {
		DateTime expired = getDate().plus(0, 0, 0, getEndTime().getHour(), getEndTime().getMinute(), 0, DayOverflow.Spillover);
		return expired.isInThePast(XCS.TZ);
	}

	public boolean isRunning() {
		DateTime started = getDate().plus(0, 0, 0, getStartTime().getHour(), getStartTime().getMinute(), 0, DayOverflow.Spillover);
		return DateTime.now(XCS.TZ).gteq(started) && !isExpired(); 
	}
}
