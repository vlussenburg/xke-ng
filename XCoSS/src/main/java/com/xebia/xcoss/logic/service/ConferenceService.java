package com.xebia.xcoss.logic.service;

import java.util.Date;

import com.xebia.xcoss.model.Conference;
import com.xebia.xcoss.model.ConferenceList;
import com.xebia.xcoss.storage.dao.ConferenceDao;
import com.xebia.xcoss.util.DateRange;

public interface ConferenceService {

    public void setConferenceDao(ConferenceDao dao);

	public Conference findConferenceByDate(Date date);

	public void insertConference(Conference conference);

	public ConferenceList getConferences(DateRange range);
}
