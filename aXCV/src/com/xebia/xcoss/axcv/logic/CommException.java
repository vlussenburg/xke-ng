package com.xebia.xcoss.axcv.logic;

import java.net.URI;

public class CommException extends RuntimeException {

	public CommException(String message) {
		super(message);
	}

	public CommException(URI uri, int code) {
		super("Failed to retrieve '" + uri.getPath() + "' ("+code+").");
	}

	public CommException(String message, Exception e) {
		super(message, e);
	}
}
