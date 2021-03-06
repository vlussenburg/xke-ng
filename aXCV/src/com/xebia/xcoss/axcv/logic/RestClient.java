package com.xebia.xcoss.axcv.logic;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.xebia.xcoss.axcv.logic.DataException.Code;
import com.xebia.xcoss.axcv.logic.gson.GsonMomentAdapter;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.util.StreamUtil;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class RestClient {

	private static final String JSESSIONID = "JSESSIONID";
	private static final int HTTP_TIMEOUT = 15 * 1000;
	private static GsonBuilder gsonBuilder = null;
	private static Cookie sessionCookie = null;
	private static boolean showDataStreams = false;

	// static {
	// // In the adb shell, setprop log.tag.org.apache.http.headers and
	// log.tag.org.apache.http.wire to VERBOSE
	// java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
	// java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);
	// }

	private static Gson getGson() {
		if (gsonBuilder == null) {
			gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(Moment.class,
					new GsonMomentAdapter());
		}
		return gsonBuilder.create();
	}

	public static boolean isAuthenticated() {
		return sessionCookie != null;
	}

	public static void logout() {
		sessionCookie = null;
	}

	public static <T> T loadObject(String url, Class<T> rvClass) {
		Log.d(LOG.COMMUNICATE, "Loading [" + rvClass.getSimpleName() + "] "
				+ url);
		Reader reader = null;
		try {
			reader = getReader(new HttpGet(url));
			T result = getGson().fromJson(reader, rvClass);
			return result;
		} finally {
			StreamUtil.close(reader);
		}
	}

	public static <T> T loadCollection(String url, Type rvClass) {
		Log.d(LOG.COMMUNICATE, "Loading [" + rvClass.toString() + "] " + url);
		Reader reader = null;
		try {
			reader = getReader(new HttpGet(url));
			return getGson().fromJson(reader, rvClass);
		} finally {
			StreamUtil.close(reader);
		}
	}

	public static <T> List<T> loadObjects(String url, Class<T> rvClass) {
		Log.d(LOG.COMMUNICATE, "Loading [[" + rvClass.getSimpleName() + "]] "
				+ url);
		Reader reader = null;
		try {
			reader = getReader(new HttpGet(url));
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
				Log.d(LOG.COMMUNICATE, "Expecting JsonArray, was "
						+ parse.getClass().getSimpleName());
			}
			return results;
		} finally {
			StreamUtil.close(reader);
		}
	}

	public static <T, U> U createObject(String url, T object, Class<U> rvClass) {
		Log.d(LOG.COMMUNICATE, "Creating [" + object.getClass().getSimpleName()
				+ "] " + url);
		Reader reader = null;
		try {
			Gson gson = getGson();
			String postData = gson.toJson(object);
			Log.d(LOG.COMMUNICATE, "POST (create) to '" + url + "': ");
			logCommunicationData(postData);
			reader = getReader(new HttpPost(url), postData);
			return gson.fromJson(reader, rvClass);
		} finally {
			StreamUtil.close(reader);
		}
	}

	public static <T> T updateObject(String url, T object) {
		Log.d(LOG.COMMUNICATE, "Updating [" + object.getClass().getSimpleName()
				+ "] " + url);
		Reader reader = null;
		try {
			Gson gson = getGson();
			String postData = gson.toJson(object);
			Log.d(LOG.COMMUNICATE, "PUT (update) to '" + url + "':");
			logCommunicationData(postData);
			reader = getReader(new HttpPut(url), postData);
			T result = getGson().fromJson(reader, (Class<T>) object.getClass());
			return result;
		} finally {
			StreamUtil.close(reader);
		}
	}

	public static void deleteObject(String url) {
		Log.d(LOG.COMMUNICATE, "Deleting [x] " + url);
		Reader reader = null;
		try {
			reader = getReader(new HttpDelete(url));
		} finally {
			StreamUtil.close(reader);
		}
	}

	public static <T> List<T> searchObjects(String url, String key,
			Class<T> rvClass, Object searchParms) {
		Log.d(LOG.COMMUNICATE,
				"Searching [" + key + "[" + rvClass.getSimpleName() + "]] "
						+ url);
		Reader reader = null;
		try {
			Gson gson = getGson();
			String postData = gson.toJson(searchParms);
			logCommunicationData(postData);
			reader = getReader(new HttpPost(url), postData);
			JsonParser parser = new JsonParser();
			JsonElement parse = parser.parse(reader);

			ArrayList<T> results = new ArrayList<T>();
			if (parse.isJsonObject()) {
				JsonArray jsonArray = parse.getAsJsonObject().get(key)
						.getAsJsonArray();
				Iterator<JsonElement> iterator = jsonArray.iterator();

				while (iterator.hasNext()) {
					JsonElement element = iterator.next();
					results.add(gson.fromJson(element, rvClass));
				}
			}
			return results;
		} finally {
			StreamUtil.close(reader);
		}
	}

	private static void logCommunicationData(String postData) {
		if (showDataStreams) {
			String[] split = postData.split(",");
			for (int i = 0; i < split.length; i++) {
				Log.d(LOG.COMMUNICATE, "  " + split[i] + ",");
			}
		}
	}

	public static <T, V> V postObject(String url, T object, Class<V> rvClass) {
		Log.d(LOG.COMMUNICATE, "Posting [" + object.getClass().getSimpleName()
				+ "] " + url);
		Reader reader = null;
		try {
			Gson gson = getGson();
			String postData = gson.toJson(object);
			logCommunicationData(postData);
			reader = getReader(new HttpPost(url), postData);
			return getGson().fromJson(reader, rvClass);
		} finally {
			StreamUtil.close(reader);
		}
	}

	private static Reader getReader(HttpEntityEnclosingRequestBase request,
			String content) {
		try {
			if (!StringUtil.isEmpty(content)) {
				request.setEntity(new StringEntity(content, HTTP.UTF_8));

			}
		} catch (UnsupportedEncodingException e) {
			throw new ServerException(request.getURI().toString(), e);
		}
		return getReader(request);
	}

	private static Reader getReader(HttpRequestBase request) {
		DefaultHttpClient httpClient = null;
		try {
			httpClient = getHttpClient();
			if (sessionCookie != null) {
				// Log.e(XCS.LOG.COMMUNICATE, "Sending cookie " +
				// sessionCookie);
				httpClient.getCookieStore().addCookie(sessionCookie);
			}
			HttpResponse response = httpClient.execute(request);
			List<Cookie> cookies = httpClient.getCookieStore().getCookies();
			for (Cookie cookie : cookies) {
				if (JSESSIONID.equals(cookie.getName())) {
					// Log.e(XCS.LOG.COMMUNICATE, "Retrieving cookie " +
					// sessionCookie);
					sessionCookie = cookie;
					break;
				}
			}
			handleResponse(request, response);

			String result = readResponse(response);
			if (showDataStreams) {
				Log.i(XCS.LOG.COMMUNICATE, "Read: " + result);
			}
			return new StringReader(result);
		} catch (InterruptedIOException e) {
			throw new DataException(Code.TIME_OUT, request.getURI());
		} catch (SocketException e) {
			// Connection refused
			throw new DataException(Code.NO_NETWORK, request.getURI());
		} catch (UnknownHostException e) {
			throw new DataException(Code.NO_NETWORK, request.getURI());
		} catch (IOException e) {
			throw new ServerException(e.getClass().getSimpleName() + ": "
					+ request.getURI().toString(), e);
		} finally {
			try {
				httpClient.getConnectionManager().closeExpiredConnections();
			} catch (Exception e) {
				Log.i(XCS.LOG.COMMUNICATE, "Close expired failed: "
						+ StringUtil.getExceptionMessage(e));
			}
		}
	}

	private static String readResponse(HttpResponse response)
			throws IOException {
		// InputStreamReader str = null;
		// StringBuilder result = new StringBuilder();
		// try {
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			return EntityUtils.toString(entity, HTTP.UTF_8);
			// str = new InputStreamReader(entity.getContent());
			// char[] buffer = new char[1024];
			// int read;
			// while ((read = str.read(buffer, 0, 1024)) >= 0) {
			// result.append(buffer, 0, read);
			// }
			// }
		}
		return "";
		// finally {
		// StreamUtil.close(str);
		// }
		// return result.toString();
	}

	private static DefaultHttpClient getHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
		params.setBooleanParameter("http.protocol.expect-continue", false);
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 8080));
		try {
			SSLSocketFactory sslSocketFactory = new EC2TrustedSocketFactory();
			// Comodo is not a trusted provider (yet?)
			// HostnameVerifier hostnameVerifier =
			// org.apache.http.conn.ssl.SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
			// sslSocketFactory.setHostnameVerifier((X509HostnameVerifier)
			// hostnameVerifier);
			// Define only one port for the scheme, otherwise it will be
			// overwritten
			registry.register(new Scheme("https", sslSocketFactory, 443));
		} catch (Exception e) {
			Log.e(XCS.LOG.COMMUNICATE, "Could not use secure connection: "
					+ StringUtil.getExceptionMessage(e));
			throw new RuntimeException(e);
		}
		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
				params, registry);
		return new DefaultHttpClient(manager, params);
	}

	private static void handleResponse(HttpRequestBase request,
			HttpResponse response) {
		int responseCode = response.getStatusLine().getStatusCode();
		String message = "?";
		switch (responseCode) {
		case 200:
			// This is an ok status
			break;
		case 400:
			try {
				message = readResponse(response);
			} catch (IOException e) {
			}
			Log.w(XCS.LOG.COMMUNICATE, "Server said warning '"
					+ request.getURI().toString() + "': " + message + ".");
			throw new DataException(DataException.Code.NOT_HANDLED,
					request.getURI());
		case 403:
			Log.w(XCS.LOG.COMMUNICATE, "Not authenticated for URL '"
					+ request.getURI().toString() + "'.");
			throw new DataException(DataException.Code.NOT_ALLOWED,
					request.getURI());
		case 404:
			Log.w(XCS.LOG.COMMUNICATE, "The URL '"
					+ request.getURI().toString() + "' (" + request.getMethod()
					+ ") was not found!");
			throw new DataException(DataException.Code.NOT_FOUND,
					request.getURI());
		case 500:
			try {
				message = readResponse(response);
			} catch (IOException e) {
			}
			Log.e(XCS.LOG.COMMUNICATE, "Server error on '"
					+ request.getURI().toString() + "': " + message);
			throw new ServerException(request.getURI().getPath());
		default:
			if (responseCode >= 400) {
				try {
					message = readResponse(response);
				} catch (IOException e) {
				}
				Log.e(XCS.LOG.COMMUNICATE, "Error " + responseCode + " on '"
						+ request.getURI().toString() + "': " + message);
				throw new CommException(request.getURI(), responseCode);
			}
			break;
		}
	}
}
