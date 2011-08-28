package controllers;

import java.util.Random;

import com.google.gson.JsonObject;



public class Feedback extends DefaultController {
	
    public static void getRating(int sessionId) {
    	renderText(Math.round(100*new Random().nextFloat())/10.0);
    }

    public static void getComments(String sessionId) {
    	handleResponse("", "comments");
    }

    public static void createComment(String sessionId, JsonObject body) {
    	ok();
    }

    public static void createRating(String sessionId, double rate) {
    	ok();
    }
}
