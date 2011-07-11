package com.xebia.xcoss.axcv.logic;

import android.util.Log;

import com.xebia.xcoss.axcv.util.XCS;

public class ProfileManager {

	public static boolean markSession(String user, int id) {
		Log.w(XCS.LOG.COMMUNICATE, "Marking session " + id + " for user " + user);
		return true;
	}

	public static boolean isMarked(String user, int id) {
		return false;
	}
	
	public static int[] getMarkedSessions(String user) {
		return new int[] {8801};
	}
}
