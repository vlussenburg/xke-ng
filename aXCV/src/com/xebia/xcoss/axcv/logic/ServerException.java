package com.xebia.xcoss.axcv.logic;

import java.net.URI;

public class ServerException extends RuntimeException {

	private static final long serialVersionUID = 2056882476603190969L;

	private String title;

	public ServerException(String url) {
		super("Request '" + url + "' could not be handled by server.");
		this.title = "Server Error";
	}

	public ServerException(String url, Exception e) {
		super("Server failure on " + url + ": " + e.getMessage(), e);
		title = e.getMessage();
	}
	public String getTitle() {
		return title;
	}
}
