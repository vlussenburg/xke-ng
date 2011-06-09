package controllers;

import java.util.Random;

public class Conference extends DefaultController {

    public static void showOnDate(String date) {
    	handleResponse("conference", "d-" + date);
    }

    public static void showOnId(int id) {
    	handleResponse("conference", "i-" + id);
    }

    public static void sessions(int id) {
    	handleResponse("sessions", "" + id);
    }

    public static void update() {
    	ok();
    }

    public static void create() {
    	renderText(new Random().nextInt());
    }
}
