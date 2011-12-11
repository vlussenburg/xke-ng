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

public class NotificationServiceManager implements SignalRetriever {

	private static final int SERVICE_ID = 873892;

	@Override
	public void onSignal(Context ctx) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean onOwned = sp.getBoolean(XCS.PREF.NOTIFYOWNED, false);
		boolean onMarked = sp.getBoolean(XCS.PREF.NOTIFYTRACK, false);
		String delay = sp.getString(XCS.PREF.NOTIFYINTERVAL, null);

		if ("off".equalsIgnoreCase(delay) == false && (onOwned || onMarked)) {
			long interval = Integer.parseInt(delay) * 1000;
			long startAt = SystemClock.elapsedRealtime() + interval;
			Intent intent = new Intent(ctx, CheckNotificationSignalRetriever.class);
			intent.putExtra(XCS.PREF.NOTIFYOWNED, onOwned);
			intent.putExtra(XCS.PREF.NOTIFYTRACK, onMarked);

			Toast.makeText(ctx, R.string.notification_on, Toast.LENGTH_SHORT).show();
			PendingIntent sender = PendingIntent.getBroadcast(ctx, SERVICE_ID, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startAt, interval, sender);
			Log.w(XCS.LOG.ALL, "Set alarm for every " + (interval/1000) + " sec.");
		} else {
			Toast.makeText(ctx, R.string.notification_off, Toast.LENGTH_SHORT).show();
		}
	}
}
