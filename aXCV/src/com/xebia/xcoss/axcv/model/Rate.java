package com.xebia.xcoss.axcv.model;

import java.util.List;

import android.widget.RatingBar;

public class Rate {
	
	private static final String[] VALUES = {
		/*0*/ "No rating",
		/*1*/ "Very poor",
		/*2*/ "Poor",
		/*3*/ "Less than moderate",
		/*4*/ "Moderate",
		/*5*/ "Fair",
		/*6*/ "Adequate",
		/*7*/ "Good",
		/*8*/ "Very good",
		/*9*/ "Excellent",
		/*10*/ "Outstanding",
	};
	
	private int rate;
	// This value is not included in the JSON
	private transient String value;
	private transient String sessionId;

	public Rate(RatingBar ratingBar, String sessionId) {
		this.rate = (int)(10 * ratingBar.getRating())/ratingBar.getNumStars();
		this.value = String.valueOf(ratingBar);
		this.sessionId = sessionId;
	}

	public Rate(List<Integer> list) {
		if (list == null || list.size() == 0) {
			value = "n/a";
		} else {
			int total = 0;
			for (Integer i : list) {
				total += i;
			}
			value = String.valueOf((total * 10 / list.size()) / 10.0);
		}
	}

	public String toString() {
		return value;
	}

	public String getMessage() {
		if ( rate >= 0 && rate < VALUES.length ) 
			return VALUES[rate];
		return "?";
	}
	
	public boolean isRated() {
		return rate > 0 && rate < VALUES.length;
	}
	
	public String getSessionId() {
		return sessionId;
	}
}
