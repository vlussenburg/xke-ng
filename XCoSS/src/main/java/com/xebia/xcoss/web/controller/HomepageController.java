package com.xebia.xcoss.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.xebia.xcoss.web.model.HomepageModel;

@Controller
public class HomepageController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView getHomepage() {
        HomepageModel homepage = new HomepageModel();
        return new ModelAndView("homepage", "homepage", homepage);
    }

}
