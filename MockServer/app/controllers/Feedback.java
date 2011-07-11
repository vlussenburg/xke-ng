package controllers;

import java.util.Random;

import com.google.gson.JsonObject;



public class Feedback extends DefaultController {
	
    public static void getRating(int sessionId) {
    	renderText(Math.round(100*new Random().nextFloat())/10.0);
    }

    public static void getComments(int sessionId) {
    	handleResponse("", "comments");
    }

    public static void createComment(int sessionId, JsonObject body) {
    	ok();
    }

    public static void createRating(int sessionId, double rate) {
    	ok();
    }
}
