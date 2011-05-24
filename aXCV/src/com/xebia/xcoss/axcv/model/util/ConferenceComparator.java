package com.xebia.xcoss.axcv.model.util;

import java.util.Comparator;

import android.util.Log;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.util.XCS;

public class ConferenceComparator implements Comparator<Conference> {

	@Override
	public int compare(Conference paramT1, Conference paramT2) {
		long T1date = paramT1.getDate().getTime();
		long T2date = paramT2.getDate().getTime();

		if (T1date == T2date) {
			Log.i(XCS.LOG.ALL, "Items are same!");
			return 0;
		}
		return (T1date > T2date ? 1 : -1);
	}

}
