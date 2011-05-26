package com.xebia.xcoss.axcv.model;

import hirondelle.date4j.DateTime;

import java.io.Serializable;
import java.util.ArrayList;

public class Session implements Serializable {

	private static final long serialVersionUID = 1L;

	private String title;
	private String author;
	private ArrayList<String> labels;
	private Location location;
	private DateTime date;
	private DateTime startTime;
	private DateTime endTime;

	public DateTime getDate() {
		return date;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}

	public Session() {
		labels = new ArrayList<String>();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public ArrayList<String> getLabels() {
		return labels;
	}

	public void setLabels(ArrayList<String> labels) {
		this.labels = labels;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public DateTime getStartTime() {
		return startTime;
	}

	public DateTime getEndTime() {
		return endTime;
	}

	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(DateTime endTime) {
		this.endTime = endTime;
	}
}
