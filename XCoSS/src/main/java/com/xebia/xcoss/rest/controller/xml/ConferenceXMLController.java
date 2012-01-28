package com.xebia.xcoss.rest.controller.xml;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

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
import com.xebia.xcoss.logic.service.ConferenceService;
import com.xebia.xcoss.model.Conference;
import com.xebia.xcoss.model.ConferenceList;
import com.xebia.xcoss.rest.controller.ConferenceController;
import com.xebia.xcoss.rest.exception.InvalidArgumentException;
import com.xebia.xcoss.util.DateParser;
import com.xebia.xcoss.util.DateRange;

@Controller
public class ConferenceXMLController extends ConferenceController {

    private static Logger log = LoggerFactory.getLogger(ConferenceXMLController.class);

	@Autowired
	private ConferenceService conferenceService;

	@Autowired
	private Authenticator authenticator;

	@RequestMapping(value = "/xml/conference/{date}", method = RequestMethod.GET)
	public ModelAndView getConference(HttpServletResponse response, @PathVariable String date) throws InvalidArgumentException {
		// authenticator.validate(token);
		Date cvDate = null;
		try {
			cvDate = DateParser.parseDate(date).getFromDate();
		} catch (ParseException e) {
			log.warn("No such date" , e);
			throw new InvalidArgumentException();
		}
		Conference conference = conferenceService.findConferenceByDate(cvDate);
		if ( conference == null ) {
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			catch (IOException e) {
				log.warn("Could not set NOT_FOUND status.", e);
			}
			return null;
		}
		return new ModelAndView("xml", "conference", conference);
	}

    @RequestMapping(value = "/xml/conference", method = RequestMethod.POST)
    public ModelAndView addConference(@RequestBody Conference conference) {
    	conferenceService.insertConference(conference);
        return new ModelAndView("xml", "conference", null);
    }
    
	@RequestMapping(value = "/xml/conferences", method = RequestMethod.GET)
	public ModelAndView getConferences() {
		ConferenceList list = conferenceService.getConferences(null);
		return new ModelAndView("xml", "conference", list);
	}

	@RequestMapping(value = "/xml/conferences/{startdate}", method = RequestMethod.GET)
	public ModelAndView getConferences(@PathVariable String startdate) {
		DateRange period = null;
		try {
			period = DateParser.parseDate(startdate);
		} catch (ParseException e) {
			log.warn("No such date" , e);
			throw new InvalidArgumentException();
		}
		ConferenceList list = conferenceService.getConferences(period);
		return new ModelAndView("xml", "conference", list);
	}

}
