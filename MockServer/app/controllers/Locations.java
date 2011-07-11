package controllers;

import java.util.Random;


public class Locations extends DefaultController {

    public static void show() {
    	handleResponse("", "locations");
    }

    public static void create() {
    	renderText(new Random().nextInt(100));
    }
}
