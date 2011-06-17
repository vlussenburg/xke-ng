package com.xebia.xcoss.axcv.ui;

import java.util.Set;

import android.util.Log;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class ConferenceStatus {

	private static final int slotSize = 60;
	
	public static String getStatus(Conference cfr) {
		try {
			int locsize = cfr.getLocations().size();
			int trackDuration = cfr.getEndTime().getHour() * 60 + cfr.getEndTime().getMinute()
					- cfr.getStartTime().getHour() * 60 - cfr.getStartTime().getMinute();

			Set<Session> sessions = cfr.getSessions();
			int mandatoryDuration = 0;
			int bookedDuration = 0;
			for (Session session : sessions) {
				if (session.isMandatory()) {
					mandatoryDuration += session.getDuration();
				} else {
					bookedDuration += session.getDuration();
				}
			}
			int obsoleteCount = locsize == 1 ? 1 : locsize-1;
			int totalDuration = locsize * trackDuration - obsoleteCount * mandatoryDuration;
			int expectedSlots = totalDuration/slotSize;
			int openSlots = (totalDuration-bookedDuration)/slotSize;

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
			Log.e(LOG.ALL, "Status fail: " + e.getMessage());
			e.printStackTrace();
		}
		return "";
	}

}
