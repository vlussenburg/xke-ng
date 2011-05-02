
package com.xebia.xcoss.logic.service.impl;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xebia.xcoss.logic.service.ConferenceService;
import com.xebia.xcoss.storage.dao.ConferenceDao;

@Service
@Transactional(rollbackFor=Throwable.class)
public class ConferenceServiceImpl implements ConferenceService {

    @Autowired
    private ConferenceDao dao;

    @Override
    public void setConferenceDao(ConferenceDao dao) {
    	this.dao = dao;
    }
 
}
