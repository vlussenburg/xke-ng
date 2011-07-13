package com.xebia.xcoss.axcv.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class NotificationService extends Service {

	private Timer notifyTimer;
	private Handler handler = new Handler();
	private Thread authorThread;
	private Thread markedThread;

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

	private TimerTask notifyTask = new TimerTask() {
		@Override
		public void run() {
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

		@Override
		public boolean cancel() {
			if (authorThread != null) {
				authorThread.interrupt();
			}
			if (markedThread != null) {
				markedThread.interrupt();
			}
			return super.cancel();
		}
	};

	private Runnable notifyOnAuthor = new Runnable() {
		@Override
		public void run() {
			Log.v(XCS.LOG.ALL, "Notification on Author");
			handler.post(changeNotification);
		}
	};

	private Runnable notifyOnMarked = new Runnable() {
		@Override
		public void run() {
			Log.v(XCS.LOG.ALL, "Notification on Track");
			handler.post(changeNotification);
		}
	};

	private Runnable changeNotification = new Runnable() {
		@Override
		public void run() {
			Log.v(XCS.LOG.ALL, "Alert received....");
			Context ctx = getApplicationContext();
			if (ctx == null) ctx = NotificationService.this;
			Toast.makeText(ctx, "Alert received", Toast.LENGTH_LONG).show();
		}
	};

	@Override
	public IBinder onBind(Intent paramIntent) {
		// TODO Auto-generated method stub
		return null;
	}
}
