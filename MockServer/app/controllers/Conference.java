package controllers;

import java.util.Random;

import com.google.gson.JsonObject;

public class Conference extends DefaultController {

    public static void showOnDate(String yearmonthday) {
    	handleResponse("conference", "d-" + yearmonthday);
    }

    public static void showOnId(int id) {
    	handleResponse("conference", "i-" + id);
    }

    public static void sessions(int id) {
    	handleResponse("sessions", "" + id);
    }

    public static void update(JsonObject body) {
    	System.out.println("Update: " + body);
    	ok();
    }

    public static void create(JsonObject body) {
    	System.out.println("Create: " + body);
    	renderText(new Random().nextInt());
    }
}
