package com.xebia.xcoss.axcv.model;

import java.util.List;

import com.xebia.xcoss.axcv.Messages;

import android.widget.RatingBar;

public class Rate {
	
	private int rate;
	// These values are not included in the JSON
	private transient String value;
	private static transient String[] values;
	private transient String sessionId;

	public Rate(RatingBar ratingBar, String sessionId) {
		if ( values == null ) throw new RuntimeException(Messages.getString("Exception.3"));
		this.rate = (int)(10 * ratingBar.getRating())/ratingBar.getNumStars();
		this.value = String.valueOf(ratingBar);
		this.sessionId = sessionId;
	}

	public Rate(List<Integer> list) {
		if ( values == null ) throw new RuntimeException(Messages.getString("Exception.3"));
		if (list == null || list.size() == 0) {
			value = "-";
		} else {
			int total = 0;
			for (Integer i : list) {
				total += i;
			}
			value = String.valueOf((total * 10 / list.size()) / 10.0);
		}
	}

	public static void init(String[] input) {
		if ( input == null || input.length != 11) {
			throw new RuntimeException(Messages.getString("Exception.4"));
		}
		values = input;
	}
	
	public String toString() {
		return value;
	}

	public String getMessage() {
		if ( rate >= 0 && rate < values.length ) 
			return values[rate];
		return "?";
	}
	
	public boolean isRated() {
		return rate > 0 && rate < values.length;
	}
	
	public String getSessionId() {
		return sessionId;
	}
}
