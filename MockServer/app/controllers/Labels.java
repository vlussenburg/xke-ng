package controllers;



public class Labels extends DefaultController {

    public static void show() {
    	handleResponse("", "labels");
    }

    public static void create(String name) {
    	ok();
    }
}
