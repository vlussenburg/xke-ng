package controllers;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Login extends DefaultController {

	public class Account {
		public String name;
		@SerializedName("cryptedPassword") 
		public String password;
	}
	
    public static void authenticate(JsonObject body) {
    	try {
    		Account login = new Gson().fromJson(body, Account.class);
    		if ( StringUtils.isBlank(login.name) || StringUtils.isBlank(login.password) ) {
    			throw new Exception();
    		}
			System.out.println("Ok: " + login.name + "/" + login.password);
    		renderText("thisismytoken");
    	} catch (Exception e) {
			System.out.println("Invalid: " + body);
    		forbidden("Username/password combination incorrect");
    	}
    }
}