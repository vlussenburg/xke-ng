package com.xebia.xcoss.axcv.logic;

import hirondelle.date4j.DateTime;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.xebia.xcoss.axcv.logic.gson.GsonDateTimeAdapter;
import com.xebia.xcoss.axcv.util.StreamUtil;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class RestClient {

	private static GsonBuilder gsonBuilder = null;

	private static Gson getGson() {
		if (gsonBuilder == null) {
			gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(DateTime.class, new GsonDateTimeAdapter());
		}
		return gsonBuilder.create();
	}

	public static <T> T loadObject(String url, Class<T> rvClass, String token) {
		Log.d(LOG.COMMUNICATE, "Loading [" + rvClass.getSimpleName() + "] " + url);
		Reader reader = null;
		try {
			reader = getReader(new HttpGet(url), token);
			T result = getGson().fromJson(reader, rvClass);
			return result;
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	public static <T> T loadCollection(String url, Type rvClass, String token) {
		Log.d(LOG.COMMUNICATE, "Loading [" + rvClass.toString() + "] " + url);
		Reader reader = null;
		try {
			reader = getReader(new HttpGet(url), token);
			return getGson().fromJson(reader, rvClass);
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	public static <T> List<T> loadObjects(String url, Class<T> rvClass, String token) {
		Log.d(LOG.COMMUNICATE, "Loading [[" + rvClass.getSimpleName() + "]] " + url);
		Reader reader = null;
		try {
			reader = getReader(new HttpGet(url), token);
			JsonParser parser = new JsonParser();
			JsonElement parse = parser.parse(reader);

			ArrayList<T> results = new ArrayList<T>();
			if (parse.isJsonArray()) {
				JsonArray jsonArray = parse.getAsJsonArray();
				Iterator<JsonElement> iterator = jsonArray.iterator();

				Gson gson = getGson();
				while (iterator.hasNext()) {
					JsonElement element = iterator.next();
					results.add(gson.fromJson(element, rvClass));
				}
			} else {
				Log.d(LOG.COMMUNICATE, "Expecting JsonArray, was " + parse.getClass().getSimpleName());
			}
			return results;
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	public static <T,U> U createObject(String url, T object, Class<U> rvClass, String token)  {
		Log.d(LOG.COMMUNICATE, "Creating [" + object.getClass().getSimpleName() + "] " + url);
		Reader reader = null;
		try {
			Gson gson = getGson();
			String postData = gson.toJson(object);
			reader = getReader(new HttpPut(url), postData, token);
			return gson.fromJson(reader, rvClass);
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	public static <T> void updateObject(String url, T object, String token)  {
		Log.d(LOG.COMMUNICATE, "Updating [" + object.getClass().getSimpleName() + "] " + url);
		Reader reader = null;
		try {
			Gson gson = getGson();
			String postData = gson.toJson(object);
			reader = getReader(new HttpPost(url), postData, token);
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	public static void deleteObject(String url, String token)  {
		Log.d(LOG.COMMUNICATE, "Deleting [x] " + url);
		Reader reader = null;
		try {
			reader = getReader(new HttpDelete(url), token);
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	public static <T> List<T> searchObjects(String url, String key, Class<T> rvClass, Object searchParms, String token)
			 {
		Log.d(LOG.COMMUNICATE, "Searching [" + key + "[" + rvClass.getSimpleName() + "]] " + url);
		Reader reader = null;
		try {
			Gson gson = getGson();
			String postData = gson.toJson(searchParms);
			reader = getReader(new HttpPost(url), postData, token);
			JsonParser parser = new JsonParser();
			JsonElement parse = parser.parse(reader);

			ArrayList<T> results = new ArrayList<T>();
			if (parse.isJsonObject()) {
				JsonArray jsonArray = parse.getAsJsonObject().get(key).getAsJsonArray();
				Iterator<JsonElement> iterator = jsonArray.iterator();

				while (iterator.hasNext()) {
					JsonElement element = iterator.next();
					results.add(gson.fromJson(element, rvClass));
				}
			}
			return results;
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	public static <T, V> V postObject(String url, T object, Class<V> rvClass, String token)  {
		Log.d(LOG.COMMUNICATE, "Posting [" + object.getClass().getSimpleName() + "] " + url);
		Reader reader = null;
		try {
			Gson gson = getGson();
			String postData = gson.toJson(object);
			Log.d(LOG.COMMUNICATE, "Posting: " + postData);
			reader = getReader(new HttpPost(url), postData, token);
			return getGson().fromJson(reader, rvClass);
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	private static Reader getReader(HttpEntityEnclosingRequestBase request, String content, String token)  {
		try {
			if (!StringUtil.isEmpty(content)) {
				request.setEntity(new StringEntity(content));
			}
		}
		catch (UnsupportedEncodingException e) {
			throw new ServerException(request.getURI().toString(), e);
		}
		return getReader(request, token);
	}

	private static Reader getReader(HttpRequestBase request, String token)  {
		try {
			request.addHeader("Authorization", "Token " + token);
			HttpResponse response = getHttpClient().execute(request);

			handleResponse(request, response);

			StringBuilder result = new StringBuilder();
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStreamReader str = new InputStreamReader(entity.getContent());
				char[] buffer = new char[1024];
				int read;
				while ((read = str.read(buffer, 0, 1024)) >= 0) {
					result.append(buffer, 0, read);
				}
				StreamUtil.close(str);
				Log.i(XCS.LOG.COMMUNICATE, "Read: " + result.toString());
			}
			return new StringReader(result.toString());
		}
		catch (IOException e) {
			throw new ServerException(request.getURI().toString(), e);
		}
	}

	private static HttpClient getHttpClient() {
		HttpParams httpParams = new BasicHttpParams();
		// TODO Set timeout to 20 seconds (instead of 2) for EC2.
		HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
		HttpConnectionParams.setSoTimeout(httpParams, 20000);
		
		return new DefaultHttpClient(httpParams);
	}

	private static void handleResponse(HttpRequestBase request, HttpResponse response) {
		int responseCode = response.getStatusLine().getStatusCode();
		switch (responseCode) {
			case 200:
				// This is an ok status
			break;
			case 403:
				Log.w(XCS.LOG.COMMUNICATE, "Not authenticated for URL '"+request.getURI().toString()+"'.");
				throw new DataException(DataException.Code.NOT_ALLOWED, request.getURI());
			case 404:
				Log.w(XCS.LOG.COMMUNICATE, "The URL '"+request.toString()+"' was not found!");
				throw new DataException(DataException.Code.NOT_FOUND, request.getURI());
			case 500:
				Log.e(XCS.LOG.COMMUNICATE, "Server error on '"+request.getURI().toString()+"'.");
				throw new ServerException(request.getURI().getPath());
			default:
				if (responseCode >= 400) {
					Log.e(XCS.LOG.COMMUNICATE, "Error "+responseCode+" on '"+request.getURI().toString()+"'.");
					throw new CommException(request.getURI(), responseCode);
				}
			break;
		}
	}
}
