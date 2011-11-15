package com.xebia.xcoss.axcv.model.util;

import java.io.Serializable;
import java.util.Comparator;

import com.xebia.xcoss.axcv.model.Session;

public class SessionComparator implements Comparator<Session>, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(Session s1, Session s2) {
//		Log.w("XCS", "Compare result of:");
//		Log.w("XCS", "      1 : " + s1);
//		Log.w("XCS", "      2 : " + s2);
		int result = 0;
		if (s1.getStartTime() == null) {
			result = (s2.getStartTime() == null) ? 0 : -1;
		} else {
			result = s1.getStartTime().compare(s2.getStartTime());
		}
//		Log.w("XCS", "  Starttime: " + result);
		if ( result == 0 ) {
			if (s1.getEndTime() == null) {
				result = (s2.getEndTime() == null) ? 0 : -1;
			} else {
				result = s1.getEndTime().compare(s2.getEndTime());
			}
		}
//		Log.w("XCS", "  Endtime: " + result);
		if (result == 0) {
			if (s1.getLocation() == null) {
				result = (s2.getLocation() == null) ? 0 : -1;
			} else if (s2.getLocation() == null ) {
				result = 1;
			} else {
				result = s1.getLocation().toString().compareTo(s2.getLocation().toString());
			}
		}
//		Log.w("XCS", "  Location: " + result);
		return result;
	}

}
