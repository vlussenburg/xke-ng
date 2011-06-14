package com.xebia.xcoss.axcv.logic;

import java.net.URI;

public class CommException extends RuntimeException {

	private String title;
	private String message;

	public CommException(URI uri, int code) {
		super();
		init(uri, code);
	}

	private void init(URI uri, int code) {
		switch (code) {
			case 403:
				this.title = "Not Authorized";
				this.message = "Access denied. Please check login credentials.";
			break;
			case 404:
				this.title = "Not Found";
				this.message = "Could not find '" + uri.getPath() + "'";
			break;
			case 500:
				this.title = "Server Error";
				this.message = "Request '" + uri.getPath() + "' could not be handled by server.";
			break;
			default:
				this.title = "Error " + code;
				this.message = "Failed to retrieve '" + uri.getPath() + "'.";
			break;

		}
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
