package com.xebia.xcoss.axcv.model.util;

import java.io.Serializable;
import java.util.Comparator;

import com.xebia.xcoss.axcv.model.Session;

public class SessionComparator implements Comparator<Session>, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(Session s1, Session s2) {
		int result = 0;
		if (s1.getDate() == null) {
			result = (s2.getDate() == null) ? 0 : -1;
		} else if (s2.getDate() == null ) {
			result = 1;
		} else {
			result = s1.getDate().compareTo(s2.getDate());
		}
		if ( result == 0 ) {
			if (s1.getStartTime() == null) {
				result = (s2.getStartTime() == null) ? 0 : -1;
			} else if (s2.getStartTime() == null ) {
				result = 1;
			} else {
				result = s1.getStartTime().compareTo(s2.getStartTime());
			}
		}
		if (result == 0) {
			if (s1.getLocation() == null) {
				result = (s2.getLocation() == null) ? 0 : -1;
			} else if (s2.getLocation() == null ) {
				result = 1;
			} else {
				result = s1.getLocation().toString().compareTo(s2.getLocation().toString());
			}
		}
		return result;
	}

}
