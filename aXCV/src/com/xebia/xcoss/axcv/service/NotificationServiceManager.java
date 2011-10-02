package com.xebia.xcoss.axcv.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.xebia.xcoss.axcv.util.XCS;

public class NotificationServiceManager implements SignalRetriever {

	@Override
	public void onSignal(Context ctx) {
		Log.v(XCS.LOG.ALL, "Notification signal received");
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean onOwned = sp.getBoolean(XCS.PREF.NOTIFYOWNED, false);
		boolean onMarked = sp.getBoolean(XCS.PREF.NOTIFYTRACK, false);

		if ( onOwned || onMarked ) {
			ctx.startService(new Intent(ctx, NotificationService.class));
			Log.v(XCS.LOG.ALL, "Service started");
		} else {
			ctx.stopService(new Intent(ctx, NotificationService.class));
			Log.v(XCS.LOG.ALL, "Service stopped");
		}
	}

}
