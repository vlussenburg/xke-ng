package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;


public class Error extends DefaultController {

	public static void report(String body) {
		System.out.println("[ERROR REPORT]");
	    Map<String, String[]> all = params.all();
	    for (String key : all.keySet()) {
	    	if ( "body".equals(key) ) continue;
		    for (String value : all.get(key)) {
				try {
					System.out.println(key + " = " + URLDecoder.decode(value, "UTF-8"));
				}
				catch (UnsupportedEncodingException e) {
					System.out.println("Unknown encoding....");
				}
			}
		}
	}
}