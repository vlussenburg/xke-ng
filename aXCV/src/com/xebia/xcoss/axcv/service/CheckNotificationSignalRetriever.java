package com.xebia.xcoss.axcv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.xebia.xcoss.axcv.logic.DataException;
import com.xebia.xcoss.axcv.logic.ProfileManager;
import com.xebia.xcoss.axcv.logic.ProfileManager.Trackable;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Credential;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.model.util.SessionComparator;
import com.xebia.xcoss.axcv.util.SecurityUtils;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

import de.quist.app.errorreporter.ExceptionReporter;

public class CheckNotificationSignalRetriever extends BroadcastReceiver {

	protected static final String TAG_OWNED = "id-owned_ses";
	protected static final String TAG_TRACKED = "id-track-ses";

	@Override
	public void onReceive(Context ctx, Intent intent) {

		ExceptionReporter exceptionReporter = ExceptionReporter.register(ctx);
		Bundle bundle = intent.getExtras();
		boolean onMarked = bundle.getBoolean(XCS.PREF.NOTIFYTRACK);
		boolean onOwned = bundle.getBoolean(XCS.PREF.NOTIFYOWNED);

		if (onOwned || onMarked) {
			String path = null;
			try {
				ApplicationInfo ai = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(),
						PackageManager.GET_META_DATA);
				path = ai.metaData.getString("com.xebia.xcoss.serverUrl").trim();
			}
			catch (NameNotFoundException e) {
				Log.e(XCS.LOG.ALL, "Notification failure: " + StringUtil.getExceptionMessage(e));
			}
			if (StringUtil.isEmpty(path)) {
				Toast.makeText(ctx, "Update check failed. Server URL not found.", Toast.LENGTH_LONG).show();
				return;
			}

			try {
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
				String user = sp.getString(XCS.PREF.USERNAME, null);
				
				if ( StringUtil.isEmpty(user) ) {
					return;
				}
				
				Toast.makeText(ctx, "Checking for updates...", Toast.LENGTH_SHORT).show();

				if (!RestClient.isAuthenticated()) {
					String password = sp.getString(XCS.PREF.PASSWORD, "");
					Credential credential = new Credential(user, SecurityUtils.decrypt(password));
					RestClient.postObject(path + "/login", credential, void.class);
				}

				Handler handler = new CheckNotificationHandler(ctx);
				ProfileManager pm = new ProfileManager(ctx);

				Log.v(XCS.LOG.ALL, "Notification check on owned/tracked: " + onOwned + "/" + onMarked);

				if (onMarked) {
					ArrayList<String> sessions = getChangesInTrackedSessions(pm, user, path);
					reportOn(TAG_TRACKED, sessions, handler);
				}
				if (onOwned) {
					ArrayList<String> sessions = getChangesInOwnedSessions(pm, user, path);
					reportOn(TAG_OWNED, sessions, handler);
				}
			}
			catch (DataException e) {
				Log.w("notification", "Error on notification: " + e.getMessage());
			}
			catch (Exception e) {
				exceptionReporter.reportException(Thread.currentThread(), e, "Notification service failure");
			}
		}
	}

	private void reportOn(String type, ArrayList<String> sessionIds, Handler handler) {
		if (sessionIds.size() > 0) {
			Message message = Message.obtain(handler);
			Bundle data = new Bundle();
			data.putStringArrayList(type, sessionIds);
			message.setData(data);
			handler.sendMessage(message);
		}
	}

	private ArrayList<String> getChangesInOwnedSessions(ProfileManager pm, String user, String path) {
		ArrayList<String> modified = new ArrayList<String>();
		try {
			pm.openConnection();

			Trackable[] ids = pm.getOwnedSessions(user);
			// Search active sessions on the author
			Search search = new Search().onAuthor(new Author(user, null, null, null)).after(new Moment());
			List<Session> allOwnedSessions = RestClient.searchObjects(path + "/search/sessions", "sessions",
					Session.class, search);
			if (allOwnedSessions == null) {
				return new ArrayList<String>();
			}
			Collections.sort(allOwnedSessions, new SessionComparator());

			Log.v(XCS.LOG.COMMUNICATE, "Author based: found " + allOwnedSessions.size() + " sessions to check.");
			for (Session ownedSession : allOwnedSessions) {
				long currentHash = ownedSession.getModificationHash();
				String id = ownedSession.getId();
				Trackable trackable = null;
				for (int i = 0; i < ids.length; i++) {
					if (id.equals(ids[i].sessionId)) {
						trackable = ids[i];
						break;
					}
				}

				if (trackable == null || currentHash != trackable.hash) {
					Log.v(XCS.LOG.DATA, "Authored session changed: " + ownedSession.getTitle());
					if (trackable == null) {
						trackable = pm.new Trackable();
						trackable.sessionId = id;
						trackable.conferenceId = ownedSession.getConferenceId();
						trackable.userId = user;
					}
					trackable.date = ownedSession.getStartTime().asLong();
					trackable.hash = currentHash;
					pm.updateOwnedSession(trackable);
					modified.add(id);
				}
			}
			return modified;
		}
		finally {
			pm.closeConnection();
		}
	}

	private ArrayList<String> getChangesInTrackedSessions(ProfileManager pm, String user, String path) {
		ArrayList<String> modified = new ArrayList<String>();
		try {
			pm.openConnection();
			Trackable[] ids = pm.getMarkedSessions(user);

			Log.v(XCS.LOG.COMMUNICATE, "Tracked a total of " + ids.length + " sessions.");

			for (int i = 0; i < ids.length; i++) {
				Session session = RestClient.loadObject(path + "/session/" + ids[i].sessionId, Session.class);
				if (session != null) {
					long modificationHash = session.getModificationHash();
					if (!session.isExpired() && ids[i].hash != modificationHash) {
						Log.v(XCS.LOG.DATA, "Tracked session changed: " + session.getTitle());
						ids[i].hash = modificationHash;
						ids[i].date = session.getStartTime().asLong();
						pm.updateMarkedSession(ids[i]);
						modified.add(session.getId());
					}
				}
			}
			return modified;
		}
		finally {
			pm.closeConnection();
		}
	}

}