package com.xebia.xcoss.rest.controller.json;

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
import org.springframework.web.servlet.View;

import com.xebia.xcoss.auth.Authenticator;
import com.xebia.xcoss.logic.service.ConferenceService;
import com.xebia.xcoss.model.Conference;
import com.xebia.xcoss.model.ConferenceList;
import com.xebia.xcoss.rest.controller.ConferenceController;
import com.xebia.xcoss.rest.exception.InvalidArgumentException;
import com.xebia.xcoss.rest.view.JsonView;
import com.xebia.xcoss.util.DateParser;
import com.xebia.xcoss.util.DateRange;

@Controller
public class ConferenceJsonController extends ConferenceController {

	private static Logger log = LoggerFactory.getLogger(ConferenceJsonController.class);

	@Autowired
	private ConferenceService conferenceService;

	@Autowired
	private Authenticator authenticator;

	@RequestMapping(value = "/json/conference/{date}", method = RequestMethod.GET)
	public View getConference(HttpServletResponse response, @PathVariable String date) throws InvalidArgumentException {
		// authenticator.validate(token);
		Date cvDate = null;
		try {
			cvDate = DateParser.parseDate(date).getFromDate();
		}
		catch (ParseException e) {
			log.warn("No such date", e);
			throw new InvalidArgumentException();
		}
		Conference conference = conferenceService.findConferenceByDate(cvDate);
		if (conference == null) {
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			catch (IOException e) {
				log.warn("Could not set NOT_FOUND status.", e);
			}
			return null;
		}
		return new JsonView(conference);
	}

	@RequestMapping(value = "/json/conference", method = RequestMethod.POST)
	public View addConference(HttpServletResponse response, @RequestBody Conference conference) {
		conferenceService.insertConference(conference);
		try {
			response.sendError(HttpServletResponse.SC_NO_CONTENT);
		}
		catch (IOException e) {
			log.warn("Could not set NOT_FOUND status.", e);
		}
		return null;
	}

	@RequestMapping(value = "/json/conferences", method = RequestMethod.GET)
	public View getConferences() {
		ConferenceList list = conferenceService.getConferences(null);
		return new JsonView(list);
	}

	@RequestMapping(value = "/json/conferences/{startdate}", method = RequestMethod.GET)
	public View getConferences(@PathVariable String startdate) {
		DateRange period = null;
		try {
			period = DateParser.parseDate(startdate);
		}
		catch (ParseException e) {
			log.warn("No such date", e);
			throw new InvalidArgumentException();
		}
		ConferenceList list = conferenceService.getConferences(period);
		return new JsonView(list);
	}
}
