package com.xebia.xcoss.axcv.logic;

import java.net.URI;

import com.xebia.xcoss.axcv.Messages;

public class CommException extends RuntimeException {

	private static final long serialVersionUID = 2550831166577996704L;

	public CommException(String message) {
		super(message);
	}

	public CommException(URI uri, int code) {
		super(Messages.getString("CommException.0", uri.getPath(), code));
	}

	public CommException(String message, Exception e) {
		super(message, e);
	}
}
