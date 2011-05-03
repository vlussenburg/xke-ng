package com.xebia.xcoss.storage.dao;

import java.sql.Date;
import java.util.List;

import com.xebia.xcoss.model.Conference;
import com.xebia.xcoss.storage.common.GenericDao;

public interface ConferenceDao extends GenericDao<Conference, String> {

	public List<Conference> findByDate(Date startDate, Date endDate);

}
