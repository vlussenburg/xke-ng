package com.xebia.xcoss.axcv.model.util;

import java.util.Comparator;

import android.util.Log;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.util.XCS;

public class ConferenceComparator implements Comparator<Conference> {

	@Override
	public int compare(Conference paramT1, Conference paramT2) {
		long T1date = paramT1.getDate().getModifiedJulianDayNumber();
		long T2date = paramT2.getDate().getModifiedJulianDayNumber();

		if (T1date == T2date) {
			Log.i(XCS.LOG.ALL, "Items are same!");
			return 0;
		}
		
		Log.i(XCS.LOG.ALL, paramT1.getDate() + " -> " + T1date);
		Log.i(XCS.LOG.ALL, paramT2.getDate() + " -> " + T2date);
		return (T1date > T2date ? 1 : -1);
	}

}
