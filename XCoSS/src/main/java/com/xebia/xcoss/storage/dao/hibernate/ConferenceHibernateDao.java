package com.xebia.xcoss.storage.dao.hibernate;

import java.sql.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.xebia.xcoss.model.Conference;
import com.xebia.xcoss.storage.common.GenericHibernateDoa;
import com.xebia.xcoss.storage.dao.ConferenceDao;

@Repository
public class ConferenceHibernateDao extends GenericHibernateDoa<Conference, String> implements ConferenceDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Conference> findByDate(Date startDate, Date endDate) {
		String qs = "SELECT * FROM Conference where date > ? and date < ?";
        Query query = em.createNativeQuery(qs, Conference.class);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
		return query.getResultList();
	}

}
