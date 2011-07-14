package com.xebia.xcoss.axcv.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class NotificationService extends Service {

	private static final String TAG_OWNED = "id-owned_ses";
	private static final String TAG_TRACKED = "id-track-ses";

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
				notifyTimer.scheduleAtFixedRate(notifyTask, 0, 15 * 1000);
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
		int[] sessionIds = owned ? getChangesInOwnedSessions() : getChangesInTrackedSessions();
		if (sessionIds.length > 0) {
			Message message = Message.obtain(handler);
			Bundle data = new Bundle();
			data.putIntArray(owned ? TAG_OWNED : TAG_TRACKED, sessionIds);
			message.setData(data);
			handler.sendMessage(message);
		}
	}

	protected int[] getChangesInOwnedSessions() {
		// TODO Auto-generated method stub
		return new int[] { 8801 };
	}

	protected int[] getChangesInTrackedSessions() {
		// TODO Auto-generated method stub
		return new int[] { 8802 };
	}

	private void notifyChange(Bundle bundle) {
		// Context ctx = getApplicationContext();
		// if (ctx == null) ctx = NotificationService.this;

		Context ctx = NotificationService.this;
		long currentTimeMillis = System.currentTimeMillis();
		Intent intent = new Intent(ctx, CVSplashLoader.class);
		PendingIntent clickIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
		NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		int[] sessionIds = bundle.getIntArray(TAG_TRACKED);
		if (sessionIds != null) {
			for (int i = 0; i < sessionIds.length; i++) {
				String title = "Track changed";
				String message = getSessionChange(sessionIds[i]);
				Toast.makeText(ctx, title, Toast.LENGTH_SHORT).show();
				Notification noty = new Notification(R.drawable.x_stat_track, title, currentTimeMillis);
				noty.setLatestEventInfo(ctx, title, message, clickIntent);
				// Use session id for notifyCount - This way there is one per session.
				mgr.notify(sessionIds[i], noty);
			}
		}

		sessionIds = bundle.getIntArray(TAG_OWNED);
		if (sessionIds != null) {
			for (int i = 0; i < sessionIds.length; i++) {
				String title = "Session tamper!";
				String message = getSessionChange(sessionIds[i]);
				Toast.makeText(ctx, title, Toast.LENGTH_SHORT).show();
				Notification noty = new Notification(R.drawable.x_stat_owned, title, currentTimeMillis);
				noty.setLatestEventInfo(ctx, title, message, clickIntent);
				// Use session id for notifyCount - This way there is one per session.
				mgr.notify(sessionIds[i], noty);
			}
		}
	}

	private String getSessionChange(int id) {
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
}
