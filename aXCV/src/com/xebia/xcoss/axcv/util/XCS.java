package com.xebia.xcoss.axcv.util;

import java.util.TimeZone;

import android.view.Menu;

public class XCS {
	
	private static final int DIALOGID = 2348;

	public static final TimeZone TZ = TimeZone.getDefault();

	public class SETTING {
		public static final String URL = "http://10.0.2.2:9000";
	}

	public class DIALOG {
		public static final int ADD_RATING        = DIALOGID;
		public static final int CREATE_REVIEW     = DIALOGID + 1;
		public static final int CONNECT_FAILED    = DIALOGID + 2;
		public static final int SELECT_CONFERENCE = DIALOGID + 3;
		public static final int INPUT_TIME_START  = DIALOGID + 4;
		public static final int INPUT_DURATION    = DIALOGID + 5;
		public static final int INPUT_AUDIENCE    = DIALOGID + 6;
		public static final int INPUT_LOCATION    = DIALOGID + 7;
		public static final int INPUT_LIMIT       = DIALOGID + 8;
		public static final int INPUT_DESCRIPTION = DIALOGID + 9;
		public static final int INPUT_TIME_END    = DIALOGID + 10;
		public static final int INPUT_LANGUAGE    = DIALOGID + 11;
		public static final int INPUT_DATE        = DIALOGID + 12;
		public static final int INPUT_PREPARATION = DIALOGID + 13;
		public static final int INPUT_TITLE       = DIALOGID + 14;
		public static final int ERROR_SITUATION   = DIALOGID + 15;
	}
	
	public class LOG {
		public static final String SECURITY = "XCS";
		public static final String PROPERTIES = "XCS";
		public static final String ALL = "XCS";
		public static final String NAVIGATE = "XCS";
		public static final String COMMUNICATE = "XCS";
		public static final String CACHE = "XCS";
	}

	public class MENU {
		public static final int SETTINGS = Menu.FIRST;
		public static final int SEARCH   = Menu.FIRST+1;
		public static final int OVERVIEW = Menu.FIRST+2;
		public static final int ADD      = Menu.FIRST+3;
		public static final int EDIT 	 = Menu.FIRST+4;
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
		public static final String TIMEFORMAT = "TimeFormat";		
	}
}
