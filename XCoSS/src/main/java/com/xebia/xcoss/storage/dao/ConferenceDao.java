package com.xebia.xcoss.storage.dao;

import java.util.Date;
import java.util.List;

import com.xebia.xcoss.model.Conference;
import com.xebia.xcoss.storage.common.GenericDao;

public interface ConferenceDao extends GenericDao<Conference, String> {

    public List<Conference> getConferences(Date startDate, Date endDate);

}
