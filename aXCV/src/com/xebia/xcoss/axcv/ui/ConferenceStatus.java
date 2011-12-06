package com.xebia.xcoss.axcv.ui;

import java.util.Set;

import android.util.Log;

import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class ConferenceStatus {

	private static final int slotSize = 60;
	
	public static String getStatus(Conference cfr) {
		try {
			int locsize = cfr.getLocations().size();
			int trackDuration = cfr.getEndTime().asMinutes() - cfr.getStartTime().asMinutes();
			Set<Session> sessions = cfr.getSessions();
			int totalDuration = locsize * trackDuration;
			int expectedSlots = totalDuration/slotSize;

			int mandatoryDuration = 0;
			int bookedDuration = 0;
			for (Session session : sessions) {
				if (session.isMandatory()) {
					mandatoryDuration += session.getDuration();
				} else {
					bookedDuration += session.getDuration();
				}
			}
			int openSlots = (totalDuration-bookedDuration-locsize*mandatoryDuration)/slotSize;

			if (expectedSlots == 0) {
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
			Log.e(LOG.ALL, "Status fail: " + e.getMessage(), e);
		}
		return "";
	}

}
