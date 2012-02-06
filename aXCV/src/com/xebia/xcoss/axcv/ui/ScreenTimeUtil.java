package com.xebia.xcoss.axcv.ui;

import java.text.MessageFormat;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.xebia.xcoss.axcv.Messages;
import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.util.XCS;

public class ScreenTimeUtil {

	private DateTimeFormatter dateShortFormat;
	private DateTimeFormatter dateFormat;
	private DateTimeFormatter timeFormat;
	private String[] dateIndicators;

	public ScreenTimeUtil(Context ctx) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		this.dateFormat = DateTimeFormat.forPattern(sp.getString(XCS.PREF.DATEFORMAT, "d MMMM yyyy"));
		this.dateShortFormat = DateTimeFormat.forPattern("d/MM/yy");
		this.timeFormat = DateTimeFormat.forPattern(sp.getString(XCS.PREF.TIMEFORMAT, "HH:mm'u'"));
		dateIndicators = ctx.getResources().getStringArray(R.array.dateIndicators);
		if ( dateIndicators.length != 13) {
			throw new RuntimeException(Messages.getString("Exception.2"));
		}
	}

	public String getRelativeDate(Moment date) {
		int year = date.getYearOffset();
		switch (year) {
			case -1: return dateIndicators[0];
			case 1: return dateIndicators[1];
			case 0: break;
			default: return dateIndicators[2];
		}
	
		int days = date.getDaysFromNow();
		switch (days) {
			case -7:
				return dateIndicators[3];
			case -1:
				return dateIndicators[4];
			case 0:
				return dateIndicators[5];
			case 1:
				return dateIndicators[6];
			case 7:
				return dateIndicators[7];
		}
		if (days > 1 && days < 14) {
			return MessageFormat.format(dateIndicators[8], days);
		}
		if (days < -1 && days > -14) {
			return MessageFormat.format(dateIndicators[9], -1*days);
		}
		if (days <= -14) return dateIndicators[10];
		if (days > 100) return dateIndicators[11];

		return MessageFormat.format(dateIndicators[12], days/7);
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
