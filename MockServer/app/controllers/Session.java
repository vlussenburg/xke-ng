package controllers;

import java.util.Random;

import com.google.gson.JsonObject;

public class Session extends DefaultController {

    public static void show(int id) {
    	handleResponse("session", "i-" + id);
    }

    public static void delete(int sessionId) {
    	ok();
    }

    public static void update(int conferenceId, JsonObject body) {
    	System.out.println("Update: " + body);
    	ok();
    }

    public static void create(int conferenceId, JsonObject body) {
    	System.out.println("Create: " + body);
    	renderText(new Random().nextInt());
    }

    public static void search(JsonObject body) {
    	System.out.println("Search: " + body);
    	handleResponse("sessions", "search");
    }
}