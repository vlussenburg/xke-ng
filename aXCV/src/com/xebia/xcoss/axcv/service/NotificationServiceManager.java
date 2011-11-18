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
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean onOwned = sp.getBoolean(XCS.PREF.NOTIFYOWNED, false);
		boolean onMarked = sp.getBoolean(XCS.PREF.NOTIFYTRACK, false);

		ctx.stopService(new Intent(ctx, NotificationService.class));
		Log.v(XCS.LOG.ALL, "Notification service stop send");

		if ( onOwned || onMarked ) {
			ctx.startService(new Intent(ctx, NotificationService.class));
			Log.v(XCS.LOG.ALL, "Notification service start send: " + onOwned + "/" + onMarked);
		}
	}

}
