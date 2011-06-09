package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

import play.Play;
import play.mvc.Controller;

public class DefaultController extends Controller {

	private static String base = null;
	private static String NL = System.getProperty("line.separator");

	private static String getBase() {
		if (base == null) {
			String name = Play.configuration.getProperty("application.name", "data");
			base = new File(name, "resources").getAbsolutePath();
		}
		return base;
	}

	protected static String loadResource(String path, String name) throws Exception {
		StringBuilder builder = new StringBuilder();
		File file = new File(new File(getBase(), path), name + ".json");

		System.out.println("Loading " + file);
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileInputStream(file), "UTF-8");
			while (scanner.hasNextLine()) {
				builder.append(scanner.nextLine() + NL);
			}
		}
		finally {
			if (scanner != null) scanner.close();
		}
		return builder.toString();
	}


    protected static void handleResponse(String base, String param) {
    	try {
    		renderText(loadResource(base, param));
    	} catch (Exception e) {
    		System.out.println("Fault: " + e.toString());
    		notFound(e.getMessage());
    	}
	}

}
