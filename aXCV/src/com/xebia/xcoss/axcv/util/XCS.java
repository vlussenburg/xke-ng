package com.xebia.xcoss.axcv.util;

import android.view.Menu;

public class XCS {

	public class LOG {
		public static final String SECURITY = "XCS";
		public static final String PROPERTIES = "XCS";
		public static final String ALL = "XCS";
	}

	public class MENU {
		public static final int SETTINGS = Menu.FIRST;
		public static final int SEARCH   = Menu.FIRST+1;
		public static final int OVERVIEW = Menu.FIRST+2;
		public static final int ADD      = Menu.FIRST+3;
	}
	
	public class PREF {
		// Should be the same as in preferences.xml
		public static final String USERNAME = "Username";
		public static final String PASSWORD = "Password";
		public static final String STOREPASS = "StorePassword";
		public static final String JUMPTOFIRST = "Upcoming";
		public static final String SESSIONLIST = "ListView";
		public static final String NOTIFYSOUND = "NotificationSound";
		public static final String NOTIFYTRACK = "NotifyTrack";
		public static final String NOTIFYOWNED = "NotifyOwned";		
		public static final String DATEFORMAT = "DateFormat";		
	}
}
