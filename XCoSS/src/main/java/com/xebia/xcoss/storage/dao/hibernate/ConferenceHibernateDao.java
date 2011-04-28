package com.xebia.xcoss.storage.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.xebia.xcoss.model.Conference;
import com.xebia.xcoss.storage.common.GenericHibernateDoa;
import com.xebia.xcoss.storage.dao.ConferenceDao;

@Repository
public class ConferenceHibernateDao extends GenericHibernateDoa<Conference, String> implements ConferenceDao {

	@Override
	public List<Conference> getConferences(Date startDate, Date endDate) {
        return em.createNativeQuery("SELECT * FROM Conference", Conference.class).getResultList();
	}

}
