package com.xebia.xcoss.axcv.ui;

import java.text.MessageFormat;
import java.util.Set;

import android.util.Log;

import com.xebia.xcoss.axcv.Messages;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class ConferenceStatus {

	private static final int slotSize = 60;
	private static String[] status;
	
	public static void init(String[] input) {
		if ( input == null || input.length != 3) {
			throw new RuntimeException(Messages.getString("Exception.6"));
		}
		status = input;
	}
	
	public static String getStatus(Conference cfr) {
		if ( status == null ) throw new RuntimeException(Messages.getString("Exception.6"));
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
				return "";
			}
			if (openSlots <= 0) {
				return status[0];
			}
			if (openSlots < expectedSlots / 2) {
				return MessageFormat.format(status[1], cfr.getSessions().size());
			}
			return MessageFormat.format(status[2], openSlots);
		}
		catch (Exception e) {
			Log.e(LOG.ALL, "Status fail: " + e.getMessage(), e);
		}
		return "";
	}

}
