package controllers;

import com.google.gson.JsonObject;


public class Authors extends DefaultController {

    public static void show() {
    	handleResponse("", "authors");
    }

    public static void search(JsonObject body) {
    	System.out.println("Search: " + body);
    	handleResponse("", "authors");
    }
}
