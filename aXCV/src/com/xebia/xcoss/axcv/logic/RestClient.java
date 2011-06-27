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
import com.xebia.xcoss.axcv.ui.StringUtil;
import com.xebia.xcoss.axcv.util.StreamUtil;
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

	public static <T> T loadObject(String url, Class<T> rvClass) {
		Log.d(LOG.COMMUNICATE, "Loading [" + rvClass.getSimpleName() + "] " + url);
		Reader reader = null;
		try {
			reader = getReader(new HttpGet(url));
			T result = getGson().fromJson(reader, rvClass);
			Log.e(LOG.ALL, "Loaded: " + result);
			return result;
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	public static <T> T loadCollection(String url, Type rvClass) {
		Log.d(LOG.COMMUNICATE, "Loading [" + rvClass.toString() + "] " + url);
		Reader reader = null;
		try {
			reader = getReader(new HttpGet(url));
			return getGson().fromJson(reader, rvClass);
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	public static <T> List<T> loadObjects(String url, String key, Class<T> rvClass) {
		Log.d(LOG.COMMUNICATE, "Loading [" + key + "[" + rvClass.getSimpleName() + "]] " + url);
		Reader reader = null;
		try {
			reader = getReader(new HttpGet(url));
			JsonParser parser = new JsonParser();
			JsonElement parse = parser.parse(reader);

			ArrayList<T> results = new ArrayList<T>();
			if (parse.isJsonObject()) {
				JsonArray jsonArray = parse.getAsJsonObject().get(key).getAsJsonArray();
				Iterator<JsonElement> iterator = jsonArray.iterator();

				Gson gson = getGson();
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

	public static <T> int createObject(String url, T object)  {
		Log.d(LOG.COMMUNICATE, "Creating [" + object.getClass().getSimpleName() + "] " + url);
		Reader reader = null;
		try {
			Gson gson = getGson();
			String postData = gson.toJson(object);
			reader = getReader(new HttpPut(url), postData);
			return gson.fromJson(reader, int.class);
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	public static <T> void updateObject(String url, T object)  {
		Log.d(LOG.COMMUNICATE, "Updating [" + object.getClass().getSimpleName() + "] " + url);
		Reader reader = null;
		try {
			Gson gson = getGson();
			String postData = gson.toJson(object);
			reader = getReader(new HttpPost(url), postData);
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	public static void deleteObject(String url)  {
		Log.d(LOG.COMMUNICATE, "Deleting [x] " + url);
		Reader reader = null;
		try {
			reader = getReader(new HttpDelete(url));
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	public static <T> List<T> searchObjects(String url, String key, Class<T> rvClass, Object searchParms)
			 {
		Log.d(LOG.COMMUNICATE, "Searching [" + key + "[" + rvClass.getSimpleName() + "]] " + url);
		Reader reader = null;
		try {
			Gson gson = getGson();
			String postData = gson.toJson(searchParms);
			reader = getReader(new HttpPost(url), postData);
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

	public static <T, V> V postObject(String url, T object, Class<V> rvClass)  {
		Log.d(LOG.COMMUNICATE, "Posting [" + object.getClass().getSimpleName() + "] " + url);
		Reader reader = null;
		try {
			Gson gson = getGson();
			String postData = gson.toJson(object);
			reader = getReader(new HttpPost(url), postData);
			return getGson().fromJson(reader, rvClass);
		}
		finally {
			StreamUtil.close(reader);
		}
	}

	private static Reader getReader(HttpEntityEnclosingRequestBase request, String content)  {
		try {
			if (!StringUtil.isEmpty(content)) {
				request.setEntity(new StringEntity(content));
			}
		}
		catch (UnsupportedEncodingException e) {
			throw new ServerException(request.getURI().toString(), e);
		}
		return getReader(request);
	}

	private static Reader getReader(HttpRequestBase request)  {
		try {
			HttpResponse response = getHttpClient().execute(request);

			try {
				handleResponse(request.getURI(), response.getStatusLine().getStatusCode());
			}
			catch (DataException e) {
				return new StringReader("");
			}

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				StringBuilder result = new StringBuilder();
				InputStreamReader str = new InputStreamReader(entity.getContent());
				char[] buffer = new char[1024];
				int read;
				while ((read = str.read(buffer, 0, 1024)) >= 0) {
					result.append(buffer, 0, read);
				}
				StreamUtil.close(str);
				Log.i(XCS.LOG.COMMUNICATE, "Read: " + result.toString());
				return new StringReader(result.toString());
			}
		}
		catch (IOException e) {
			throw new ServerException(request.getURI().toString(), e);
		}
		return null;
	}

	private static HttpClient getHttpClient() {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 2000);
		HttpConnectionParams.setSoTimeout(httpParams, 2000);
		return new DefaultHttpClient(httpParams);
	}

	private static void handleResponse(URI uri, int code) throws DataException {
		switch (code) {
			case 200:
				// This is an ok status
			break;
			case 403:
				Log.w(XCS.LOG.COMMUNICATE, "Not authenticated for URL '"+uri.toString()+"'.");
				throw new DataException(DataException.Code.NOT_ALLOWED, uri);
			case 404:
				Log.w(XCS.LOG.COMMUNICATE, "The URL '"+uri.toString()+"' was not found!");
				throw new DataException(DataException.Code.NOT_FOUND, uri);
			case 500:
				Log.e(XCS.LOG.COMMUNICATE, "Server error on '"+uri.toString()+"'.");
				throw new ServerException(uri.getPath());
			default:
				if (code >= 400) {
					Log.e(XCS.LOG.COMMUNICATE, "Error "+code+" on '"+uri.toString()+"'.");
					throw new CommException(uri, code);
				}
			break;
		}
	}
}
