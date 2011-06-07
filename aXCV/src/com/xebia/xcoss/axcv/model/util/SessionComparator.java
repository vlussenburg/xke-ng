package com.xebia.xcoss.axcv.model.util;

import java.io.Serializable;
import java.util.Comparator;

import com.xebia.xcoss.axcv.model.Session;

public class SessionComparator implements Comparator<Session>, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(Session s1, Session s2) {
		if (s1.getStartTime() == null) {
			return (s2.getStartTime() == null) ? 0 : -1;
		} else if (s2.getStartTime() == null ) {
			return 1;
		}
		int result = s1.getStartTime().compareTo(s2.getStartTime());
		if (result == 0) {
			result = s1.getLocation().toString().compareTo(s2.getLocation().toString());
		}
		return result;
	}

}
