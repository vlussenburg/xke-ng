package com.xebia.xcoss.axcv.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.CVSplashLoader;
import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.logic.ProfileManager;
import com.xebia.xcoss.axcv.logic.ProfileManager.Trackable;
import com.xebia.xcoss.axcv.logic.cache.NoCache;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class NotificationService extends Service {

	private static final String TAG_OWNED = "id-owned_ses";
	private static final String TAG_TRACKED = "id-track-ses";
	private static final long[] VIBRATE_PATTERN = new long[] { 1000, 200, 1000 };
	private static final int SERVICE_ID = 873892;

	private Timer notifyTimer;
	private Thread authorThread;
	private Thread markedThread;

	private boolean onOwned;
	private boolean onMarked;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			notifyChange(msg.getData());
		}
	};

	private Runnable notifyOnAuthor = new Runnable() {
		@Override
		public void run() {
			checkSessionsForChange(true);
		}
	};

	private Runnable notifyOnMarked = new Runnable() {
		@Override
		public void run() {
			checkSessionsForChange(false);
		}
	};

	private TimerTask notifyTask = new TimerTask() {
		@Override
		public void run() {
			startThreads();
		}

		@Override
		public boolean cancel() {
			Log.w(XCS.LOG.COMMUNICATE, "Cancelling timer...");
			endThreads();
			return super.cancel();
		}
	};

	@Override
	public void onStart(Intent intent, int startId) {

		int delay = 900;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		onOwned = sp.getBoolean(XCS.PREF.NOTIFYOWNED, false);
		onMarked = sp.getBoolean(XCS.PREF.NOTIFYTRACK, false);

		if (notifyTimer == null && (onOwned || onMarked)) {
			try {
				notifyTimer = new Timer("ConferenceNotifier");
				try {
					delay = Integer.parseInt(sp.getString(XCS.PREF.NOTIFYINTERVAL, "900"));
				}
				catch (Exception e) {
					Log.w(XCS.LOG.COMMUNICATE, "Invalid notification interval: " + StringUtil.getExceptionMessage(e));
				}
				Log.w(XCS.LOG.COMMUNICATE, "Service started with interval: " + delay);
				notifyTimer.scheduleAtFixedRate(notifyTask, 0, delay * 1000);
			}
			catch (Exception e) {
				Log.w(XCS.LOG.COMMUNICATE, "Timer start issue: " + StringUtil.getExceptionMessage(e));
			}

			Notification notify = new Notification(R.drawable.x_stat_running, "XKE notification service",
					System.currentTimeMillis());
			Intent runIntent = new Intent(this, CVSplashLoader.class);
			intent.setAction("android.intent.action.MAIN");
			intent.addCategory("android.intent.category.LAUNCHER");
			// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pendIntent = PendingIntent.getActivity(this, 0, runIntent, 0);
			StringBuilder sb = new StringBuilder();
			sb.append("Poll every ");
			int time = delay / 60;
			if (time > 120) {
				sb.append(time / 24);
				sb.append(" hrs for ");
			} else {
				sb.append(time);
				sb.append(" mins for ");
			}
			if (onMarked) {
				sb.append("track");
			}
			if (onOwned) {
				if ( onMarked) {
					sb.append("/");
				}
				sb.append("own");
			}
			sb.append(".");
			notify.setLatestEventInfo(this, "XKE notification service", sb.toString(), pendIntent);
			notify.flags |= Notification.FLAG_NO_CLEAR;
			startForeground(SERVICE_ID, notify);
		}

		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		if (notifyTimer != null) {
			notifyTimer.cancel();
		}
		Log.w(XCS.LOG.COMMUNICATE, "Service stopped.");
		notifyTask.cancel();
		stopForeground(true);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent paramIntent) {
		Log.w(XCS.LOG.COMMUNICATE, "On bind ");
		return null;
	}

	private void startThreads() {
		Log.v(XCS.LOG.ALL, "Notification processing: " + onOwned + "/" + onMarked);

		if (onOwned) {
			authorThread = new Thread(null, notifyOnAuthor, "Notify Owned Sessions");
			authorThread.start();
		}
		if (onMarked) {
			markedThread = new Thread(null, notifyOnMarked, "Notify Marked Sessions");
			markedThread.start();
		}
	}

	private void endThreads() {
		if (authorThread != null) {
			authorThread.interrupt();
		}
		if (markedThread != null) {
			markedThread.interrupt();
		}
	}

	protected void checkSessionsForChange(boolean owned) {
		Log.i(XCS.LOG.COMMUNICATE, "Checking for changes: " + (owned ? "Owner" : "Track"));
		ConferenceServer server = getConferenceServer();
		if (server == null) {
			// TODO : Happens. Just return
			Log.e(XCS.LOG.COMMUNICATE, "Notification service could not get server handle");
			throw new IllegalStateException("Missing server on notification! " + BaseActivity.getServerUrl(this));
			// return;
		}
		ArrayList<String> sessionIds = owned ? getChangesInOwnedSessions(server) : getChangesInTrackedSessions(server);
		if (sessionIds.size() > 0) {
			Message message = Message.obtain(handler);
			Bundle data = new Bundle();
			data.putStringArrayList(owned ? TAG_OWNED : TAG_TRACKED, sessionIds);
			message.setData(data);
			handler.sendMessage(message);
		}
	}

	protected ArrayList<String> getChangesInOwnedSessions(ConferenceServer server) {
		ArrayList<String> modified = new ArrayList<String>();
		ProfileManager pm = new ProfileManager(this);
		try {
			pm.openConnection();

			// Not all owned sessions need to be locally available.
			String user = getUser();
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
				Log.v(XCS.LOG.DATA, "Check for author: " + user + "(" + nextSessions.size() + " sessions)");
				for (Session session : nextSessions) {
					Log.v(XCS.LOG.DATA, "Session '" + session.getTitle() + "' ...");
					for (Author author : session.getAuthors()) {
						Log.v(XCS.LOG.DATA, "    has author: " + author.getUserId());
						if (author.getUserId().equals(user)) {
							allOwnedSessions.add(session);
						}
					}
				}
			}
			Log.v(XCS.LOG.COMMUNICATE, "Author based: found " + allOwnedSessions.size() + " sessions to check.");
			for (Trackable session : ids) {
				Log.v(XCS.LOG.COMMUNICATE, " Database status: " + session.sessionId + ":" + session.hash);
			}
			for (Session ownedSession : allOwnedSessions) {
				boolean sessionFoundInMarkList = false;
				long lastNotification = ownedSession.getModificationHash();
				String id = ownedSession.getId();
				Log.v(XCS.LOG.COMMUNICATE, " Server status  : " + id + ":" + lastNotification);

				for (int i = 0; i < ids.length; i++) {
					if (id.equals(ids[i].sessionId)) {
						sessionFoundInMarkList = true;
						if (ids[i].hash != lastNotification) {
							Log.v(XCS.LOG.DATA, "Session changed: " + ownedSession.getTitle());
							ids[i].hash = lastNotification;
							ids[i].date = ownedSession.getStartTime().asLong();
							pm.updateOwnedSession(ids[i]);
							modified.add(ownedSession.getId());
						}
						break;
					}
				}
				// What to do with sessions added elsewhere? We notify for now...
				// If there is a second author, she/he is notified.
				if (!sessionFoundInMarkList) {
					Log.v(XCS.LOG.DATA, "Session not in mark list: " + ownedSession.getTitle() + ", hash = "
							+ lastNotification);
					Trackable add = pm.new Trackable();
					add.date = ownedSession.getStartTime().asLong();
					add.sessionId = ownedSession.getId();
					add.conferenceId = ownedSession.getConferenceId();
					add.userId = user;
					add.hash = lastNotification;
					pm.updateOwnedSession(add);
					modified.add(ownedSession.getId());
				}
			}
			return modified;
		}
		finally {
			pm.closeConnection();
		}
	}

	protected ArrayList<String> getChangesInTrackedSessions(ConferenceServer server) {
		ArrayList<String> modified = new ArrayList<String>();
		ProfileManager pm = new ProfileManager(this);
		try {
			pm.openConnection();
			Trackable[] ids = pm.getMarkedSessions(getUser());

			Log.v(XCS.LOG.COMMUNICATE, "Tracked a total of " + ids.length + " sessions.");

			for (int i = 0; i < ids.length; i++) {
				Session session = server.getSession(ids[i].sessionId, ids[i].conferenceId);
				if (session != null) {
					long modificationHash = session.getModificationHash();
					Log.v(XCS.LOG.COMMUNICATE, " Session status: " + session.getId() + ":" + modificationHash
							+ (session.isExpired() ? " / expired" : " / ok"));
					Log.v(XCS.LOG.COMMUNICATE, " Track status  : " + ids[i].sessionId + ":" + ids[i].hash);
					if (!session.isExpired() && ids[i].hash != modificationHash) {
						Log.v(XCS.LOG.DATA, "Tracked session changed.");
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

	private ConferenceServer getConferenceServer() {
		ConferenceServer instance = ConferenceServer.getInstance();
		if (instance == null || instance.isLoggedIn() == false) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			String user = sp.getString(XCS.PREF.USERNAME, null);
			String password = sp.getString(XCS.PREF.PASSWORD, "");
			instance = ConferenceServer.createInstance(user, password, BaseActivity.getServerUrl(this), new NoCache(
					this));
			// TODO Cn be null if not logged in.
		}
		return instance;
	}

	private void notifyChange(Bundle bundle) {
		Context ctx = NotificationService.this;
		long currentTimeMillis = System.currentTimeMillis();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

		// This pending intent brings the current app to the front.
		Intent intent = new Intent(ctx, CVSplashLoader.class);
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");

		String soundUri = sp.getString(XCS.PREF.NOTIFYSOUND, null);
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		boolean silent = (am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
				|| (am.getMode() != AudioManager.MODE_NORMAL);
		NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		ArrayList<String> sessionIds = bundle.getStringArrayList(TAG_TRACKED);
		if (sessionIds != null) {
			for (String sessionId : sessionIds) {
				Log.w("debug", "Change [1] on " + sessionId);
				String title = "Track change!";
				String message = getSessionChange(sessionId);
				Toast.makeText(ctx, title, Toast.LENGTH_SHORT).show();
				Notification noty = new Notification(R.drawable.x_stat_track, title, currentTimeMillis);
				Log.w("debug", "Notification - Set on " + sessionId);
				intent.putExtra(BaseActivity.IA_NOTIFICATION_ID, sessionId);
				intent.putExtra(BaseActivity.IA_NOTIFICATION_TYPE, BaseActivity.NotificationType.TRACKED);
				PendingIntent clickIntent = PendingIntent
						.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				noty.setLatestEventInfo(ctx, title, message, clickIntent);
				if (silent) {
					noty.vibrate = VIBRATE_PATTERN;
					noty.defaults |= Notification.DEFAULT_VIBRATE;
				} else if (!StringUtil.isEmpty(soundUri)) {
					noty.sound = Uri.parse(soundUri);
				}
				// Use session id for notifyCount - This way there is one per session.
				mgr.notify(sessionId.hashCode(), noty);
			}
		}

		sessionIds = bundle.getStringArrayList(TAG_OWNED);
		if (sessionIds != null) {
			for (String sessionId : sessionIds) {
				Log.w("debug", "Change [2] on " + sessionId);
				String title = "Session change!";
				String message = getSessionChange(sessionId);
				Toast.makeText(ctx, title, Toast.LENGTH_SHORT).show();
				Notification noty = new Notification(R.drawable.x_stat_owned, title, currentTimeMillis);
				intent.putExtra(BaseActivity.IA_NOTIFICATION_ID, sessionId);
				intent.putExtra(BaseActivity.IA_NOTIFICATION_TYPE, BaseActivity.NotificationType.OWNED);
				PendingIntent clickIntent = PendingIntent
						.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				noty.setLatestEventInfo(ctx, title, message, clickIntent);
				if (silent) {
					noty.vibrate = VIBRATE_PATTERN;
				} else if (!StringUtil.isEmpty(soundUri)) {
					noty.sound = Uri.parse(soundUri);
				}
				// Use session id for notifyCount - This way there is one per session.
				mgr.notify(sessionId.hashCode(), noty);
			}
		}
	}

	private String getSessionChange(String id) {
		try {
			Session session = getConferenceServer().getSession(id, null);
			if (session == null) {
				return "A session has been deleted";
			}
			return "Changed: " + session.getTitle();

		}
		catch (Exception e) {
			Log.w(XCS.LOG.ALL, "Could not retrieve session " + id + ": " + StringUtil.getExceptionMessage(e));
			return "No session information (" + id + ")";
		}
	}

	protected String getUser() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String user = sp.getString(XCS.PREF.USERNAME, null);
		return user;
	}
}
