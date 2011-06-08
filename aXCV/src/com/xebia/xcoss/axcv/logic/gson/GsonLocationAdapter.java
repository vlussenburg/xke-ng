package com.xebia.xcoss.axcv.logic.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xebia.xcoss.axcv.model.Location;

public class GsonLocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {
	
	@Override
	public Location deserialize(JsonElement element, Type type, JsonDeserializationContext ctx)
			throws JsonParseException {
		String value = element.getAsJsonPrimitive().getAsString();
		return new Location(value);
	}

	@Override
	public JsonElement serialize(Location loc, Type type, JsonSerializationContext ctx) {
		String value = loc.getLocation();
		return new JsonPrimitive(value);
	}

}
