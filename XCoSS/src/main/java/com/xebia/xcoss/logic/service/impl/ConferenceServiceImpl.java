package com.xebia.xcoss.logic.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xebia.xcoss.logic.service.ConferenceService;
import com.xebia.xcoss.model.Conference;
import com.xebia.xcoss.model.ConferenceList;
import com.xebia.xcoss.storage.dao.ConferenceDao;
import com.xebia.xcoss.util.DateRange;

@Service
@Transactional(rollbackFor = Throwable.class)
public class ConferenceServiceImpl implements ConferenceService {

	@Autowired
	private ConferenceDao dao;

	@Override
	public void setConferenceDao(ConferenceDao dao) {
		this.dao = dao;
	}

	@Override
	public Conference findConferenceByDate(Date cvDate) {
		if (cvDate != null) {
			java.sql.Date sqlDate = new java.sql.Date(cvDate.getTime());
			List<Conference> result = dao.findByDate(sqlDate, sqlDate);
			if (result != null && result.size() > 0) {
				return result.get(0);
			}
		}
		return null;
	}

	@Override
	public void insertConference(Conference conference) {
		dao.insert(conference);
	}

	@Override
	public ConferenceList getConferences(DateRange range) {
		if ( range == null ) {
			range = new DateRange(new Date(), DateRange.Block.YEAR, 1);
		}
		java.sql.Date sqlFrom = new java.sql.Date(range.getFromDate().getTime());
		java.sql.Date sqlTo = new java.sql.Date(range.getToDate().getTime());
		List<Conference> result = dao.findByDate(sqlFrom, sqlTo);
		if ( result != null ) {
			ConferenceList list = new ConferenceList();
			list.setConferenceList(result);
			return list;
		}
		return null;
	}

}
