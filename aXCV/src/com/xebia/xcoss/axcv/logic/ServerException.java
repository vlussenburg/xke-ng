package com.xebia.xcoss.axcv.logic;


public class ServerException extends CommException {

	private static final long serialVersionUID = 2056882476603190969L;

	public ServerException(String url) {
		super("Request '" + url + "' could not be handled by server.");
	}

	public ServerException(String url, Exception e) {
		super("Server failure on " + url + ": " + e.getMessage(), e);
	}
}
