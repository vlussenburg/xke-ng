package com.xebia.xcoss.axcv.logic.gson;

import hirondelle.date4j.DateTime;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class GsonDateTimeAdapter implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {

	// Used format is: "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
	// Used format is: "yyyy-MM-dd'T'HH:mm:ss.SSS'+'02:00"
	// Used format is: "yyyy-MM-dd'T'HH:mm:ss.SSS'-'02:00"

	private static final String REGEXP = "([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2})\\.([0-9]*)([Z+-]?)([0-9]{2})?:?([0-9]{2})?";
	private static final Pattern pattern = Pattern.compile(REGEXP);

	@Override
	public DateTime deserialize(JsonElement element, Type type, JsonDeserializationContext ctx)
			throws JsonParseException {
		return extractDate(element.getAsJsonPrimitive().getAsString());
	}
	
	public static DateTime extractDate(String value) {
		Matcher m = pattern.matcher(value);
		if (m.matches()) {
			Integer year = Integer.valueOf(m.group(1), 10);
			Integer month = Integer.valueOf(m.group(2), 10);
			Integer day = Integer.valueOf(m.group(3), 10);
			Integer hour = Integer.valueOf(m.group(4), 10);
			Integer minute = Integer.valueOf(m.group(5), 10);
			Integer seconds = Integer.valueOf(m.group(6), 10);
			if ( year + month + day == 0) {
				year = month = day = null;
			}
			if ( hour + minute + seconds == 0) {
				hour = minute = seconds = null;
			}
			if ( !StringUtil.isEmpty(m.group(8)) ) {
				switch (m.group(8).charAt(0)) {
					case 'Z': break;
					case '+': 
						hour = hour - Integer.valueOf(m.group(9), 10);
						minute = minute - Integer.valueOf(m.group(10), 10);
						break;
					case '-': 
						hour = hour + Integer.valueOf(m.group(9), 10);
						minute = minute + Integer.valueOf(m.group(10), 10);
						break;
				}
			}
			DateTime dt = new DateTime(year, month, day, hour, minute, seconds, null);
			Log.w(XCS.LOG.COMMUNICATE, "Date parsing: From "+value+" to " + dt);
			return dt;
		}
		return null;
	}

	@Override
	public JsonElement serialize(DateTime dt, Type type, JsonSerializationContext ctx) {
		String value = "";
		if (dt.hasHourMinuteSecond() && dt.hasYearMonthDay()) {
			value = dt.format("YYYY-MM-DD|T|hh:mm:ss.fff");
		} else if (dt.hasHourMinuteSecond()) {
			value = dt.format("|0000-00-00T|hh:mm:ss.fff");
		} else if (dt.hasYearMonthDay()) {
			value = dt.format("YYYY-MM-DD|T00:00:00.000|");
		}
		int offset = XCS.TZ.getRawOffset();
		Log.w(XCS.LOG.COMMUNICATE, "Timezone value = " + offset);
		StringBuilder tzid = new StringBuilder();
		if ( offset == 0 ) {
			tzid.append('Z');
		} else {
			if ( offset > 0 ) {
				tzid.append("+");
			} else {
				tzid.append("-");
			}
			int offsetInMinutes = offset/(1000*60);
			int hours = offsetInMinutes/60;
			int minutes = offsetInMinutes%60;
			if ( hours < 10 ) tzid.append('0');
			tzid.append(hours);
			if ( minutes < 10 ) tzid.append('0');
			tzid.append(minutes);
		}
		
		Log.w(XCS.LOG.COMMUNICATE, "Timezone value = " + tzid.toString());
		value += tzid.toString();
		return new JsonPrimitive(value);
	}
}
