package com.xebia.xcoss.axcv.logic.gson;

import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.DayOverflow;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GsonDateTimeAdapter implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {

	// Used format is: "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

	@Override
	public DateTime deserialize(JsonElement element, Type type, JsonDeserializationContext ctx)
			throws JsonParseException {
		String value = element.getAsJsonPrimitive().getAsString();
		String[] parts = value.split("[TZ]");
		DateTime datePart = null;
		DateTime timePart = null;
		try {
			DateTime tmp = new DateTime(parts[0]);
			tmp.getDayOfYear();
			datePart = tmp;
		} catch (Exception e) {
			// Ignore
		}
		DateTime tmp = new DateTime(parts[1]);
		if ( tmp.getHour() + tmp.getMinute() + tmp.getSecond() != 0 ) {
			timePart = tmp;
		}
		if ( timePart == null ) {
			// Don't care if it is null
			return datePart;
		}
		if ( datePart == null ) {
			return timePart;
		}
		return datePart.plus(0, 0, 0, timePart.getHour(), timePart.getMinute(), timePart.getSecond(), DayOverflow.Spillover);
	}

	@Override
	public JsonElement serialize(DateTime dt, Type type, JsonSerializationContext ctx) {
		String value = dt.format("YYYY-MM-DD|T|hh:mm:ss.fff|Z|");
		return new JsonPrimitive(value);
	}
}
