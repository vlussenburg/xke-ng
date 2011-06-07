package com.xebia.xcoss.axcv.logic;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class RestClient {

	public static <T> T loadObject(String url, Class<T> rvClass) {

		InputStream istream = null;
		try {
			HttpGet httpget = new HttpGet(url);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(httpget);
			Log.i("XCS", "Status = " + response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				istream = entity.getContent();
				InputStreamReader reader = new InputStreamReader(istream);
				Gson gson = new Gson();
				return gson.fromJson(reader, rvClass);
			}
		}
		catch (Exception e) {
			Log.e(LOG.ALL, "Fail to load stream: " + e.getMessage());
			Log.e(LOG.ALL, "Call was: " + url);
			e.printStackTrace();
		}
		finally {
			try {
				istream.close();
			}
			catch (Exception e) {
			}
		}
		return null;
	}

	public static <T> ArrayList<T> loadObjects(String url, String key, Class<T> rvClass) {

		ArrayList<T> results = new ArrayList<T>();
		InputStream istream = null;
		try {
			HttpGet httpget = new HttpGet(url);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(httpget);
			Log.i("XCS", "Status = " + response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				istream = entity.getContent();
				InputStreamReader reader = new InputStreamReader(istream);
				
				Gson gson = new Gson();
				JsonParser parser = new JsonParser();
				JsonElement parse = parser.parse(reader);
				JsonArray jsonArray = parse.getAsJsonObject().get(key).getAsJsonArray();
				Iterator<JsonElement> iterator = jsonArray.iterator();
				while ( iterator.hasNext() ) {
					JsonElement element = iterator.next();
					results.add(gson.fromJson(element, rvClass));
				}
			}
		}
		catch (Exception e) {
			Log.e(LOG.ALL, "Fail to load stream: " + e.getMessage());
			Log.e(LOG.ALL, "Call was: " + url);
			e.printStackTrace();
		}
		finally {
			try {
				istream.close();
			}
			catch (Exception e) {
			}
		}
		return results;
	}

}
