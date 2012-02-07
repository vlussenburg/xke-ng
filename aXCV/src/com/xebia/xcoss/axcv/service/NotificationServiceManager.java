package com.xebia.xcoss.axcv.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.util.XCS;

import de.quist.app.errorreporter.ExceptionReporter;

public class NotificationServiceManager implements SignalRetriever {

	private static final int SERVICE_ID = 873892;
	private static boolean hasStarted = false;

	@Override
	public void onSignal(Context ctx) {
		ExceptionReporter.register(ctx);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		String delay = sp.getString(XCS.PREF.NOTIFYINTERVAL, null);
		boolean onOwned = sp.getBoolean(XCS.PREF.NOTIFYOWNED, false);
		boolean onMarked = sp.getBoolean(XCS.PREF.NOTIFYTRACK, false);
		Intent intent = new Intent(ctx, CheckNotificationSignalRetriever.class);
		intent.putExtra(XCS.PREF.NOTIFYOWNED, onOwned);
		intent.putExtra(XCS.PREF.NOTIFYTRACK, onMarked);
		PendingIntent sender = PendingIntent.getBroadcast(ctx, SERVICE_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if ((onOwned || onMarked) && "off".equalsIgnoreCase(delay) == false) {
			long interval = Integer.parseInt(delay) * 1000;
			long startAt = SystemClock.elapsedRealtime() + interval;
			hasStarted = true;
			Toast.makeText(ctx, R.string.notification_on, Toast.LENGTH_SHORT).show();
			AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startAt, interval, sender);
			Log.w(XCS.LOG.ALL, "Set alarm for every " + (interval / 1000) + " sec.");
		} else {
			if (hasStarted) {
				Toast.makeText(ctx, R.string.notification_off, Toast.LENGTH_SHORT).show();
				hasStarted = false;
			}
			AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			am.cancel(sender);
		}
	}

	public PendingIntent createSender(Context ctx, SharedPreferences sp) {
		boolean onOwned = sp.getBoolean(XCS.PREF.NOTIFYOWNED, false);
		boolean onMarked = sp.getBoolean(XCS.PREF.NOTIFYTRACK, false);

		if (onOwned || onMarked) {
			Intent intent = new Intent(ctx, CheckNotificationSignalRetriever.class);
			intent.putExtra(XCS.PREF.NOTIFYOWNED, onOwned);
			intent.putExtra(XCS.PREF.NOTIFYTRACK, onMarked);
			PendingIntent sender = PendingIntent.getBroadcast(ctx, SERVICE_ID, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			return sender;
		}
		return null;
	}
}
