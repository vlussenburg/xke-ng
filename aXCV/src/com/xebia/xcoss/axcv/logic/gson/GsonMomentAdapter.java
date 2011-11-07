package com.xebia.xcoss.axcv.logic.gson;

import java.lang.reflect.Type;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.util.XCS;

public class GsonMomentAdapter implements JsonSerializer<Moment>, JsonDeserializer<Moment> {

	@Override
	public Moment deserialize(JsonElement element, Type type, JsonDeserializationContext ctx)
			throws JsonParseException {
		String timeValue = element.getAsJsonPrimitive().getAsString();
		return Moment.fromString(timeValue);
	}
	

	@Override
	public JsonElement serialize(Moment dt, Type type, JsonSerializationContext ctx) {
		Log.w(XCS.LOG.COMMUNICATE, "Date serialize: " + dt.toString());
		return new JsonPrimitive(dt.toString());
	}
}