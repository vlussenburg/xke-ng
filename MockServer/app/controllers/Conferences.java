package controllers;


public class Conferences extends DefaultController {

    public static void show(int year, int month, int day) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(year);
    	if ( month > 0 ) {
    		sb.append("-");
    		sb.append(month);
    		if ( day > 0 ) {
    			sb.append("-");
    			sb.append(day);
    		}
    	}
    	handleResponse("conferences", sb.toString());
    }
}
