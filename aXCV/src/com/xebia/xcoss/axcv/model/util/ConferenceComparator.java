package com.xebia.xcoss.axcv.model.util;

import java.util.Comparator;

import com.xebia.xcoss.axcv.model.Conference;

public class ConferenceComparator implements Comparator<Conference> {

	@Override
	public int compare(Conference paramT1, Conference paramT2) {
		long T1date = paramT1.getDate().getModifiedJulianDayNumber();
		long T2date = paramT2.getDate().getModifiedJulianDayNumber();

		if (T1date == T2date) {
			return 0;
		}
		return (T1date > T2date ? 1 : -1);
	}

}
