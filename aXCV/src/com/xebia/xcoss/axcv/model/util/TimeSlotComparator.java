package com.xebia.xcoss.axcv.model.util;

import java.io.Serializable;
import java.util.Comparator;

import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.model.Conference.TimeSlot;

public class TimeSlotComparator implements Comparator<TimeSlot>, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(TimeSlot s1, TimeSlot s2) {
		int result = 0;
		if (s1.start == null) {
			result = (s2.start == null) ? 0 : -1;
		} else if (s2.start == null ) {
			result = 1;
		} else {
			result = s1.start.asMinutes() - s2.start.asMinutes();
		}
		if ( result == 0 ) {
			if (s1.location == null) {
				result = (s2.location == null) ? 0 : -1;
			} else if (s2.location == null ) {
				result = 1;
			} else {
				result = s1.location.toString().compareTo(s2.location.toString());
			}
		}
		if (result == 0) {
			if (s1.end == null) {
				result = (s2.end == null) ? 0 : -1;
			} else if (s2.end == null ) {
				result = 1;
			} else {
				result = s1.end.asMinutes() - s2.end.asMinutes();
			}
		}
		return result;
	}

}
