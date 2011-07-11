package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.mvc.Controller;
import play.mvc.Http.Header;
import play.mvc.results.Forbidden;

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

	public static void printHeaders() {
		Map<String, Header> headers = request.get().headers;
		for (String key : headers.keySet()) {
			System.out.println("[header] " + key + " = " + headers.get(key).value());
		}
	}

	protected static void checkAuthorized() {
		try {
			Map<String, Header> headers = request.get().headers;
			String tokenInfo = headers.get("authorization").value();
			String[] tokenElements = tokenInfo.split(" ");
			// TODO Validate token!
			if ( StringUtils.isNotBlank(tokenElements[1])) {
				System.out.println("Found token: " + tokenElements[1]);
			}
			return;
		} catch (Exception e) {
			throw new Forbidden("Access denied");
		}
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
		}
		catch (Exception e) {
			System.out.println("Fault: " + e.toString());
			notFound(e.getMessage());
		}
	}

}
