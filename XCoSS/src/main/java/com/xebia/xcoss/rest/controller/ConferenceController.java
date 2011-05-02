package com.xebia.xcoss.rest.controller;

import java.text.ParseException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.xebia.xcoss.auth.Authenticator;
import com.xebia.xcoss.model.Conference;
import com.xebia.xcoss.model.ConferenceList;
import com.xebia.xcoss.rest.exception.InvalidArgumentException;
import com.xebia.xcoss.util.DateParser;

@Controller
public class ConferenceController {

    private static Logger log = LoggerFactory.getLogger(ConferenceController.class);

//	@Autowired
//	private ConferenceService conferenceService;

	@Autowired
	private Authenticator authenticator;

	@RequestMapping(value = "/xml/conference/{date}", method = RequestMethod.GET)
	public ModelAndView getConference(@PathVariable String date) throws InvalidArgumentException {
		// authenticator.validate(token);
		Date cvDate = null;
		try {
			cvDate = DateParser.parseDate(date);
		} catch (ParseException e) {
			log.warn("No such date" , e);
			throw new InvalidArgumentException();
		}
		Conference conference = new Conference();
		conference.setDate(new Date());
		// conferenceService.findConferenceByDate(cvDate);
		return new ModelAndView("xml", "conference", conference);
	}

    @RequestMapping(value = "/xml/conference", method = RequestMethod.POST)
    public ModelAndView addConference(@RequestBody Conference conference) {
    	// conferenceService.insertConference(conference);
        return new ModelAndView("xml", "conference", null);
    }
    
	@RequestMapping(value = "/xml/conferences", method = RequestMethod.GET)
	public ModelAndView getConferences(HttpServletRequest request) {
		ConferenceList list = new ConferenceList();
		// conferenceService.getConferences();
		return new ModelAndView("xml", "conference", list);
	}

}
