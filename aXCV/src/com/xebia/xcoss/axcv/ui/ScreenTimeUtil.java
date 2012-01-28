package com.xebia.xcoss.axcv.ui;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.util.XCS;

public class ScreenTimeUtil {

	private DateTimeFormatter dateShortFormat;
	private DateTimeFormatter dateFormat;
	private DateTimeFormatter timeFormat;

	public ScreenTimeUtil(Context ctx) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		this.dateFormat = DateTimeFormat.forPattern(sp.getString(XCS.PREF.DATEFORMAT, "d MMMM yyyy"));
		this.dateShortFormat = DateTimeFormat.forPattern("d/MM/yy");
		this.timeFormat = DateTimeFormat.forPattern(sp.getString(XCS.PREF.TIMEFORMAT, "HH:mm'u'"));
	}

	public String getRelativeDate(Moment date) {
		int year = date.getYearOffset();
		if ( year == 1 ) {
			return "Next year";
		}
		if ( year != 0 ) {
			return "";
		}
		
		int days = date.getDaysFromNow();
		switch (days) {
			case -7:
				return "Last week";
			case -1:
				return "Yesterday";
			case 0:
				return "Today";
			case 1:
				return "Tomorrow";
			case 7:
				return "Next week";
		}
		if (days > 100) return "Later this year";
		if (days <= -14) return "Earlier this year";

		if (days > 1 && days < 14) {
			return "In " + days + " days";
		}
		if (days < -1 && days > -14) {
			return "" + -1 * days + " days ago";
		}
		return "In " + days / 7 + " weeks";
	}

	public String getAbsoluteShortDate(Moment date) {
		return dateShortFormat.print(new DateTime(date.getYear(), date.getMonth(), date.getDay(), 0, 0));
	}

	public String getAbsoluteDate(Moment date) {
		return dateFormat.print(new DateTime(date.getYear(), date.getMonth(), date.getDay(), 0, 0));
	}

	public String getAbsoluteTime(Moment time) {
		return timeFormat.print(new DateTime(0, 1, 1, time.getHour(), time.getMinute()));
	}

	public Moment getAbsoluteTime(String value) {
		DateTime date = timeFormat.parseDateTime(value);
		return new Moment(date.getHourOfDay(), date.getMinuteOfHour());
	}

}
