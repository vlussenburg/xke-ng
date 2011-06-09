package controllers;

import java.util.Random;

public class Session extends DefaultController {

    public static void show(int id) {
    	handleResponse("session", "i-" + id);
    }

    public static void delete(int sessionId) {
    	ok();
    }

    public static void update(int conferenceId) {
    	ok();
    }

    public static void create(int conferenceId) {
    	renderText(new Random().nextInt());
    }
}
