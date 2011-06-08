package com.xebia.xcoss.axcv.logic;

import java.util.List;

import android.util.Log;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;

public class ConferenceServerWrapper extends ConferenceServer {

	protected ConferenceServerWrapper(String base) {
		super(base);
	}
	
	@Override
	public List<Conference> getConferences(Integer year) {
		List<Conference> conferences = super.getConferences(year);
		for (Conference conference : conferences) {
			Log.d(XCS.LOG.ALL, "Loaded: " + conference);
		}
		return conferences;
	}
	
	@Override
	public List<Session> getSessions(Conference conference) {
		List<Session> sessions = super.getSessions(conference);
		for (Session session : sessions) {
			Log.d(XCS.LOG.ALL, "Loaded: " + session);
		}
		return sessions;
	}

}
