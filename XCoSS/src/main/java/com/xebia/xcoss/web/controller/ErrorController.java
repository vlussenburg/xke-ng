package com.xebia.xcoss.web.controller;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

    @RequestMapping(value = "/error-500")
    public void error(HttpServletRequest request) {
        Enumeration names = request.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            Object value = request.getAttribute(name);
            System.out.println("Request attr: " + name + " = " + value);
        }
    }

}
