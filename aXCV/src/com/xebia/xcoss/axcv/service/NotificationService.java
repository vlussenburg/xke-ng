package com.xebia.xcoss.axcv.service;

import java.util.ArrayList;
import java.util.List;
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

import com.xebia.xcoss.axcv.CVSplashLoader;
import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.logic.ProfileManager;
import com.xebia.xcoss.axcv.logic.ProfileManager.Trackable;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class NotificationService extends Service {

	private static final int NOTIFICATION_PERIOD = 30 * 1000;
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
			endThreads();
			return super.cancel();
		}
	};

	@Override
	public void onStart(Intent intent, int startId) {
		if (notifyTimer == null) {
			try {
				notifyTimer = new Timer("ConferenceNotifier");
				notifyTimer.scheduleAtFixedRate(notifyTask, 0, NOTIFICATION_PERIOD);
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
		ArrayList<String> sessionIds = owned ? getChangesInOwnedSessions() : getChangesInTrackedSessions();
		if (sessionIds.size() > 0) {
			Message message = Message.obtain(handler);
			Bundle data = new Bundle();
			data.putStringArrayList(owned ? TAG_OWNED : TAG_TRACKED, sessionIds);
			message.setData(data);
			handler.sendMessage(message);
		}
	}

	protected ArrayList<String> getChangesInOwnedSessions() {
		ArrayList<String> modified = new ArrayList<String>();
		ProfileManager pm = new ProfileManager(this);
		try {
			pm.openConnection();
			ConferenceServer server = ConferenceServer.getInstance();

			// Not all owned sessions need to be locally available.
			Trackable[] ids = pm.getOwnedSessions(getUser());
			Search search = new Search().onAuthor(new Author(getUser(), null, null, null));
			List<Session> sessions = server.searchSessions(search);
			for (Session session : sessions) {
				boolean sessionFoundInMarkList = false;
				long lastNotification = session.getModificationHash();
				String id = session.getId();
				for (int i = 0; i < ids.length; i++) {
					if (id.equals(ids[i].sessionId)) {
						sessionFoundInMarkList = true;
						if ( ids[i].hash != lastNotification) {
							ids[i].hash = lastNotification;
							ids[i].date = session.getStartTime().getLong();
							pm.updateOwnedSession(ids[i]);
							modified.add(session.getId());
						}
						break;
					}
				}
				// What to do with sessions added elsewhere? We notify for now...
				if (!sessionFoundInMarkList) {
					Trackable add = pm.new Trackable();
					add.date = session.getStartTime().getLong();
					add.sessionId = session.getId();
					add.userId = getUser();
					add.hash = lastNotification;
					pm.updateOwnedSession(add);
					modified.add(session.getId());
				}
			}
			return modified;
		}
		finally {
			pm.closeConnection();
		}
	}

	protected ArrayList<String> getChangesInTrackedSessions() {
		ArrayList<String> modified = new ArrayList<String>();
		ConferenceServer server = ConferenceServer.getInstance();
		ProfileManager pm = new ProfileManager(this);
		try {
			pm.openConnection();
			Trackable[] ids = pm.getMarkedSessions(getUser());
			for (int i = 0; i < ids.length; i++) {
				Session session = server.getSession(ids[i].sessionId);
				if ( ids[i].hash != session.getModificationHash()) {
					ids[i].hash = session.getModificationHash();
					ids[i].date = session.getStartTime().getLong();
					pm.updateMarkedSession(ids[i]);
					modified.add(session.getId());
				}
			}
			return modified;
		}
		finally {
			pm.closeConnection();
		}
	}

	private void notifyChange(Bundle bundle) {
		Context ctx = NotificationService.this;
		long currentTimeMillis = System.currentTimeMillis();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(NotificationService.this);
		
		// This pending intent brings the current app to the front.
		Intent intent = new Intent(ctx, CVSplashLoader.class);
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		PendingIntent clickIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		String soundUri = sp.getString(XCS.PREF.NOTIFYSOUND, null);
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		boolean silent = (am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) || (am.getMode() != AudioManager.MODE_NORMAL);
		NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		ArrayList<String> sessionIds = bundle.getStringArrayList(TAG_TRACKED);
		if (sessionIds != null) {
			for (String sessionId : sessionIds) {
				String title = "Track rescheduled";
				String message = getSessionChange(sessionId);
				Toast.makeText(ctx, title, Toast.LENGTH_SHORT).show();
				Notification noty = new Notification(R.drawable.x_stat_track, title, currentTimeMillis);
				noty.setLatestEventInfo(ctx, title, message, clickIntent);
				if ( silent ) {
					noty.vibrate = VIBRATE_PATTERN;
				} else if ( !StringUtil.isEmpty(soundUri)) {
					noty.sound = Uri.parse(soundUri);
				}
				// Use session id for notifyCount - This way there is one per session.
				mgr.notify(sessionId.hashCode(), noty);
			}
		}

		sessionIds = bundle.getStringArrayList(TAG_OWNED);
		if (sessionIds != null) {
			for (String sessionId : sessionIds) {
				String title = "Session change!";
				String message = getSessionChange(sessionId);
				Toast.makeText(ctx, title, Toast.LENGTH_SHORT).show();
				Notification noty = new Notification(R.drawable.x_stat_owned, title, currentTimeMillis);
				noty.setLatestEventInfo(ctx, title, message, clickIntent);
				if ( silent ) {
					noty.vibrate = VIBRATE_PATTERN;
				} else if ( !StringUtil.isEmpty(soundUri)) {
					noty.sound = Uri.parse(soundUri);
				}
				// Use session id for notifyCount - This way there is one per session.
				mgr.notify(sessionId.hashCode(), noty);
			}
		}
	}

	private String getSessionChange(String id) {
		try {
			Session session = ConferenceServer.getInstance().getSession(id);
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
