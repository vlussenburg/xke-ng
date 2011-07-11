package com.xebia.xcoss.axcv.logic;

import java.util.ArrayList;
import java.util.List;

import hirondelle.date4j.DateTime;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Remark;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;

public class ConferenceServerProxy extends ConferenceServer {

	private BaseActivity activity;

	protected ConferenceServerProxy(String base) {
		super(base);
	}

	public static ConferenceServerProxy getInstance(BaseActivity act) {
		ConferenceServer server = ConferenceServer.getInstance();
		if (server instanceof ConferenceServerProxy) {
			ConferenceServerProxy proxy = (ConferenceServerProxy) server;
			proxy.activity = act;
			return proxy;
		}
		return null;
	}

	@Override
	public void createLabel(String name) {
		try {
			super.createLabel(name);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "create label", e);
		}
	}

	@Override
	public void deleteConference(Conference conference) {
		try {
			super.deleteConference(conference);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "delete conference", e);
		}
	}

	@Override
	public void deleteSession(Session session, int conferenceId) {
		try {
			super.deleteSession(session, conferenceId);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "delete session", e);
		}
	}

	@Override
	public Author[] getAllAuthors() {
		try {
			return super.getAllAuthors();
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "get authors", e);
		}
		return new Author[0];
	}

	@Override
	public Conference getConference(DateTime date) {
		try {
			return super.getConference(date);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "get conference", e);
		}
		return null;
	}

	@Override
	public Conference getConference(int id) {
		try {
			return super.getConference(id);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "get conference", e);
		}
		return null;
	}

	@Override
	public List<Conference> getConferences(DateTime date) {
		try {
			return super.getConferences(date);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "get conferences", e);
		}
		return new ArrayList<Conference>();
	}

	@Override
	public List<Conference> getConferences(Integer year) {
		try {
			return super.getConferences(year);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "get conferences", e);
		}
		return new ArrayList<Conference>();
	}

	@Override
	public String[] getLabels() {
		try {
			return super.getLabels();
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "get labels", e);
		}
		return new String[0];
	}

	@Override
	public List<String> getLabels(Author author) {
		try {
			return super.getLabels(author);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "get labels", e);
		}
		return new ArrayList<String>();
	}

	@Override
	public Location[] getLocations(boolean defaultOnly) {
		try {
			return super.getLocations(defaultOnly);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "get locations", e);
		}
		return new Location[0];
	}

	@Override
	public double getRate(Session session) {
		try {
			return super.getRate(session);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "get rate", e);
		}
		return -1;
	}

	@Override
	public Remark[] getRemarks(Session session) {
		try {
			return super.getRemarks(session);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "get remarks", e);
		}
		return new Remark[0];
	}

	@Override
	public Session getSession(int id) {
		try {
			return super.getSession(id);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "get session", e);
		}
		return null;
	}

	@Override
	public List<Session> getSessions(Conference conference) {
		try {
			return super.getSessions(conference);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "get sessions", e);
		}
		return new ArrayList<Session>();
	}

	@Override
	public void registerRate(Session session, int rate) {
		try {
			super.registerRate(session, rate);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "register rate", e);
		}
	}

	@Override
	public void registerRemark(Session session, Remark remark) {
		try {
			super.registerRemark(session, remark);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "register remark", e);
		}
	}

	@Override
	public List<Author> searchAuthors(Search search) {
		try {
			return super.searchAuthors(search);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "search author", e);
		}
		return new ArrayList<Author>();
	}

	@Override
	public List<Session> searchSessions(Search search) {
		try {
			return super.searchSessions(search);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "search sessions", e);
		}
		return new ArrayList<Session>();
	}

	@Override
	public int storeConference(Conference conference, boolean update) {
		try {
			return super.storeConference(conference, update);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "store conference", e);
		}
		return -1;
	}

	@Override
	public int storeSession(Session session, int conferenceId, boolean update) {
		try {
			return super.storeSession(session, conferenceId, update);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "store session", e);
		}
		return -1;
	}

	@Override
	public int createLocation(String location) {
		try {
			return super.createLocation(location);
		}
		catch (CommException e) {
			BaseActivity.handleException(activity, "create location", e);
		}
		return -1;
	}
}
