package com.xebia.xcoss.axcv.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import android.os.Looper;
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

	private Timer notifyTimer;
	private Thread authorThread;
	private Thread markedThread;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			notifyChange(msg.getData());
		}
	};

	private Runnable notifyOnAuthor = new Runnable() {
		@Override
		public void run() {
			Looper.prepare();
			checkSessionsForChange(true);
		}
	};

	private Runnable notifyOnMarked = new Runnable() {
		@Override
		public void run() {
			Looper.prepare();
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
			endThreads();
			return super.cancel();
		}
	};

	@Override
	public void onStart(Intent intent, int startId) {
		if (notifyTimer == null) {
			try {
				notifyTimer = new Timer("ConferenceNotifier");
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//				Map<String, ?> all = sp.getAll();
//				for (String key : all.keySet()) {
//					Log.w(XCS.LOG.COMMUNICATE, " -- " + key + " '" + all.get(key) + "' = " + all.get(key).getClass());
//				}
//				Log.w(XCS.LOG.COMMUNICATE, " == " + XCS.PREF.NOTIFYINTERVAL);
//				Log.w(XCS.LOG.COMMUNICATE, " == " + sp.getString(XCS.PREF.NOTIFYINTERVAL, "<default>"));

				int delay = 900;
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
		}
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		if (notifyTimer != null) notifyTimer.cancel();
		Log.w(XCS.LOG.COMMUNICATE, "Service stopped.");
		notifyTask.cancel();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent paramIntent) {
		return null;
	}

	private void startThreads() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(NotificationService.this);
		boolean onOwned = sp.getBoolean(XCS.PREF.NOTIFYOWNED, false);
		boolean onMarked = sp.getBoolean(XCS.PREF.NOTIFYTRACK, false);

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
			Log.e(XCS.LOG.COMMUNICATE, "Notification service could not get server handle");
			Toast.makeText(this, "Notification check failed", Toast.LENGTH_SHORT).show();
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
				List<Session> nextSessions = server.getSessions(upcomingConference);
				for (Session session : nextSessions) {
					for (Author author : session.getAuthors()) {
						if (author.getUserId().equals(user)) {
							allOwnedSessions.add(session);
						}
					}
				}
			}
			for (Session ownedSession : allOwnedSessions) {
				boolean sessionFoundInMarkList = false;
				long lastNotification = ownedSession.getModificationHash();
				String id = ownedSession.getId();
				for (int i = 0; i < ids.length; i++) {
					if (id.equals(ids[i].sessionId)) {
						sessionFoundInMarkList = true;
						if (ids[i].hash != lastNotification) {
							Log.i(XCS.LOG.DATA, "Session changed: " + ownedSession.getTitle() + ", hash = "
									+ lastNotification + ", stored = " + ids[i].hash);
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
					Log.i(XCS.LOG.DATA, "Session not in mark list: " + ownedSession.getTitle() + ", hash = "
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
			for (int i = 0; i < ids.length; i++) {
				Session session = server.getSession(ids[i].sessionId, ids[i].conferenceId);
				if (session != null) {
					long modificationHash = session.getModificationHash();
					if (!session.isExpired() && ids[i].hash != modificationHash) {
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
			// TODO Encrypt/decrypt
			String password = /* SecurityUtils.decrypt( */sp.getString(XCS.PREF.PASSWORD, "")/* ) */;
			instance = ConferenceServer.createInstance(user, password, BaseActivity.getServerUrl(this), new NoCache(
					this));
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
