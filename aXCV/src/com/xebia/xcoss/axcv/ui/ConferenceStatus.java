package com.xebia.xcoss.axcv.ui;

import android.util.Log;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class ConferenceStatus {

	public static String getStatus(Conference cfr) {
		try {
			// TODO Implement
			int openSlots = 5; // cfr.getOpenSlots();
			int expectedSlots = 3*4; // cfr.getExpectedSlots();

			if ( expectedSlots == 0 ) {
				return "n/a";
			}
			if (openSlots <= 0) {
				return "fully booked";
			}
			if (openSlots < expectedSlots / 2) {
				return "" + cfr.getSessions().size() + " sessions scheduled";
			}
			return "" + openSlots + " slots available";
		}
		catch (Exception e) {
			Log.e(LOG.ALL, "Status fail: " + e.getMessage());
		}
		return "";
	}

}
