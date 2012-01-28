package com.xebia.xcoss.axcv.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.logic.ProfileManager;
import com.xebia.xcoss.axcv.logic.ProfileManager.Trackable;
import com.xebia.xcoss.axcv.logic.cache.NoCache;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;

public class CheckNotificationSignalRetriever extends BroadcastReceiver {

	protected static final String TAG_OWNED = "id-owned_ses";
	protected static final String TAG_TRACKED = "id-track-ses";

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show();
		Bundle bundle = intent.getExtras();
		boolean onMarked = bundle.getBoolean(XCS.PREF.NOTIFYTRACK);
		boolean onOwned = bundle.getBoolean(XCS.PREF.NOTIFYOWNED);

		ProfileManager pm = new ProfileManager(context);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String user = sp.getString(XCS.PREF.USERNAME, null);
		ConferenceServer server = ConferenceServer.getInstance();
		if (server == null || server.isLoggedIn() == false) {
			String password = sp.getString(XCS.PREF.PASSWORD, "");
			server = ConferenceServer.createInstance(user, password, BaseActivity.getServerUrl(context), new NoCache(context));
		}
		
		Handler handler = new CheckNotificationHandler(context);
		
		Log.v(XCS.LOG.ALL, "Notification check on owned/tracked: " + onOwned + "/" + onMarked);

		if (onMarked) {
			ArrayList<String> sessions = getChangesInTrackedSessions(pm, user, server);
			reportOn(TAG_TRACKED, sessions, handler);
		}
		if (onOwned) {
			ArrayList<String> sessions = getChangesInOwnedSessions(pm, user, server);
			reportOn(TAG_OWNED, sessions, handler);
		}
	}

	protected void reportOn(String type, ArrayList<String> sessionIds, Handler handler) {
		if (sessionIds.size() > 0) {
			Message message = Message.obtain(handler);
			Bundle data = new Bundle();
			data.putStringArrayList(type, sessionIds);
			message.setData(data);
			handler.sendMessage(message);
		}
	}

	protected ArrayList<String> getChangesInOwnedSessions(ProfileManager pm, String user, ConferenceServer server) {
		ArrayList<String> modified = new ArrayList<String>();
		try {
			pm.openConnection();

			Trackable[] ids = pm.getOwnedSessions(user);
			// Search active sessions on the author
			Search search = new Search().onAuthor(new Author(user, null, null, null)).after(new Moment());
			// TODO Search not implemented yet on server
			List<Session> allOwnedSessions = null; // server.searchSessions(search);
			if (allOwnedSessions == null || allOwnedSessions.isEmpty()) {
				allOwnedSessions = new ArrayList<Session>();
				Conference upcomingConference = server.getUpcomingConference();
				if (upcomingConference == null) {
					Log.e(XCS.LOG.DATA, "No upcomming conference!");
					return new ArrayList<String>();
				}
				Set<Session> nextSessions = upcomingConference.getSessions();
				for (Session session : nextSessions) {
					for (Author author : session.getAuthors()) {
						if (author.getUserId().equals(user)) {
							allOwnedSessions.add(session);
						}
					}
				}
			}
			// End of TODO
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

				if ( trackable == null || currentHash != trackable.hash ) {
					Log.v(XCS.LOG.DATA, "Authored session changed: " + ownedSession.getTitle());
					if ( trackable == null ) {
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

	protected ArrayList<String> getChangesInTrackedSessions(ProfileManager pm, String user, ConferenceServer server) {
		ArrayList<String> modified = new ArrayList<String>();
		try {
			pm.openConnection();
			Trackable[] ids = pm.getMarkedSessions(user);

			Log.v(XCS.LOG.COMMUNICATE, "Tracked a total of " + ids.length + " sessions.");

			for (int i = 0; i < ids.length; i++) {
				Session session = server.getSession(ids[i].sessionId, ids[i].conferenceId);
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