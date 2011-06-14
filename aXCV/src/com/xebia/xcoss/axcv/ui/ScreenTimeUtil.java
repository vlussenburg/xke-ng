package com.xebia.xcoss.axcv.ui;


import hirondelle.date4j.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.xebia.xcoss.axcv.util.XCS;

public class ScreenTimeUtil {

	private SimpleDateFormat dateFormat;
	private SimpleDateFormat timeFormat;

	public ScreenTimeUtil(Activity ctx) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		String datePref = sp.getString(XCS.PREF.DATEFORMAT, "d MMMM yyyy");
		String timePref = sp.getString(XCS.PREF.TIMEFORMAT, "HH:mm'u'");
		this.dateFormat = new SimpleDateFormat(datePref);
		this.timeFormat = new SimpleDateFormat(timePref);
	}

	public String getRelativeDate(DateTime date) {
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

	private int getDayOffset(DateTime date) {
		return DateTime.today(XCS.TZ).numDaysFrom(date);
	}

	public String getAbsoluteDate(DateTime date) {
		Calendar cal = Calendar.getInstance(XCS.TZ);
		cal.set(Calendar.YEAR, date.getYear());
		cal.set(Calendar.MONTH, date.getMonth()-1);
		cal.set(Calendar.DAY_OF_MONTH, date.getDay());
		return dateFormat.format(cal.getTime());
	}

	public String getAbsoluteTime(DateTime time) {
		Calendar cal = Calendar.getInstance(XCS.TZ);
		cal.set(Calendar.HOUR_OF_DAY, time.getHour());
		cal.set(Calendar.MINUTE, time.getMinute());
		return timeFormat.format(cal.getTime());
	}

	public DateTime getAbsoluteTime(String value) {
		try {
			Date date = timeFormat.parse(value);
			return DateTime.forTimeOnly(date.getHours(), date.getMinutes(), 0,0);
		}
		catch (ParseException e) {
			Log.w(XCS.LOG.ALL, "The value '"+value+"' is not a valid time!");
		}
		return null;
	}
	
	public static boolean isNow(DateTime startTime, DateTime endTime) {
		DateTime now = DateTime.now(XCS.TZ);
		if (startTime.hasYearMonthDay() && !startTime.isSameDayAs(now)) {
			return false;
		}
		if ( startTime.lteq(now) && endTime.gteq(now)) {
			return true;
		}
		return false;
	}

}
