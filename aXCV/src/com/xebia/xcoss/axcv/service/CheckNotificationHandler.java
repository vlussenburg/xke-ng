package com.xebia.xcoss.axcv.service;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.BaseActivity.NotificationType;
import com.xebia.xcoss.axcv.CVSplashLoader;
import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class CheckNotificationHandler extends Handler {

	private static final long[] VIBRATE_PATTERN = new long[] { 1000, 200, 1000 };
	
	private Context context;
	private String soundUri;
	private boolean silent;
	private NotificationManager mgr;
	
	public CheckNotificationHandler(Context context) {
		this.context = context;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		soundUri = sp.getString(XCS.PREF.NOTIFYSOUND, null);
		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		silent = (am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) || (am.getMode() != AudioManager.MODE_NORMAL);
		mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public void handleMessage(Message msg) {
		Bundle bundle = msg.getData();

		Log.v(XCS.LOG.ALL, "Handling message");
		ArrayList<String> sessionIds = bundle.getStringArrayList(CheckNotificationSignalRetriever.TAG_TRACKED);
		if (sessionIds != null) {
			for (String sessionId : sessionIds) {
				notify("Track change!", R.drawable.x_stat_track, sessionId, BaseActivity.NotificationType.TRACKED);
			}
		}

		sessionIds = bundle.getStringArrayList(CheckNotificationSignalRetriever.TAG_OWNED);
		if (sessionIds != null) {
			for (String sessionId : sessionIds) {
				notify("Session change!", R.drawable.x_stat_owned, sessionId, BaseActivity.NotificationType.OWNED);
			}
		}
	}

	private void notify(String title, int xType, String sessionId, NotificationType type) {
		Log.v(XCS.LOG.ALL, "Notify: " + title);
		long currentTimeMillis = System.currentTimeMillis();
		Toast.makeText(context, title, Toast.LENGTH_SHORT).show();

		Intent intent = new Intent(context, CVSplashLoader.class);
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		intent.putExtra(BaseActivity.IA_NOTIFICATION_ID, sessionId);
		intent.putExtra(BaseActivity.IA_NOTIFICATION_TYPE, type);
		PendingIntent clickIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification noty = new Notification(xType, title, currentTimeMillis);
		String message = "Click on this item to show the session.";
		noty.setLatestEventInfo(context, title, message, clickIntent);
		if (silent) {
			noty.vibrate = VIBRATE_PATTERN;
			noty.defaults |= Notification.DEFAULT_VIBRATE;
		} else if (!StringUtil.isEmpty(soundUri)) {
			noty.sound = Uri.parse(soundUri);
		}
		mgr.notify(sessionId.hashCode(), noty);
	}
}
