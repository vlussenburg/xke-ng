package com.xebia.xcoss.axcv.ui;


import hirondelle.date4j.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.xebia.xcoss.axcv.util.XCS;

public class ConferenceTime {

	private SimpleDateFormat formatter;
	private TimeZone tz;

	public ConferenceTime(Activity ctx) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		String format = sp.getString(XCS.PREF.DATEFORMAT, "d MMMM yyyy");
		this.formatter = new SimpleDateFormat(format);
		this.tz = TimeZone.getDefault();
	}

	public String getRelativeDate(Date date) {
		int days = getDayOffset(date);
		switch (days) {
			case -7: return "Last week";
			case -1: return "Yesterday";
			case  0: return "Today";
			case  1: return "Tomorrow";
			case  7: return "Next week";
		}
		if ( days > 100 ) return "Later this year";
		if ( days <= -14 ) return "Earlier this year";

		if ( days > 1 && days < 14 ) {
			return "In " + days + " days";
		}
		if ( days < -1 && days > -14 ) {
			return "" + -1*days + " days ago";
		}
		return "In " + days/7 + " weeks";
	}

	private int getDayOffset(Date date) {
		DateTime dt = DateTime.today(tz);
		return dt.numDaysFrom(DateTime.forInstant(date.getTime(), tz));
	}

	public String getAbsoluteDate(Date date) {
		return formatter.format(date);
	}

	public boolean isHistory(Date date) {
		return getDayOffset(date) < 0;
	}
	
	public boolean isToday(Date date) {
		return getDayOffset(date) == 0;
	}

}
