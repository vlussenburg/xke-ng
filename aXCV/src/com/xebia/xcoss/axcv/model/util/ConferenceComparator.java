package com.xebia.xcoss.axcv.model.util;

import java.util.Comparator;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Moment;

public class ConferenceComparator implements Comparator<Conference> {

	@Override
	public int compare(Conference c1, Conference c2) {
		Moment m1 = c1.getStartTime();
		Moment m2 = c2.getStartTime();
		if ( m1 == null ) {
			return m2 == null ? 0 : -1;
		}
		return m1.compare(m2);
	}

}
