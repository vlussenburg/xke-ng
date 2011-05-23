package com.xebia.xcoss.axcv;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class CVSettings extends PreferenceActivity {

//	private static final String USERNAME = "account_name";
//	private static final String STOREPASS = "store_credentials";
//	private static final String JUMPTOFIRST = "jump_next";
//	private static final String NOTIFYSOUND = "notify_sound";
//	private static final String NOTIFYTRACK = "notify_track";
//	private static final String NOTIFYOWNED = "notify_owned";
//	
//	private String user;
//	private String notificationSound;
//	private boolean storePassword;
//	private boolean openUpcomming;
//	private boolean notifyOnTrack;
//	private boolean notifyOnOwned;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

//	private void loadSettings() {
//		SharedPreferences settings = getSettings();
//
//		user = settings.getString(USERNAME, "");
//		notificationSound = settings.getString(NOTIFYSOUND, "");
//		storePassword = settings.getBoolean(STOREPASS, true);
//		openUpcomming = settings.getBoolean(JUMPTOFIRST, false);
//		notifyOnTrack = settings.getBoolean(NOTIFYTRACK, false);
//		notifyOnOwned = settings.getBoolean(NOTIFYOWNED, false);
//	}

	
}
