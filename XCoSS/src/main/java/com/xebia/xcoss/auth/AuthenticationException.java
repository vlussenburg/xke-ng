package com.xebia.xcoss.auth;

public class AuthenticationException extends Exception {

	private static final long serialVersionUID = 547400893117267983L;

	public AuthenticationException(String message) {
		super(message);
	}

	public AuthenticationException(String message, Exception exception) {
		super(message, exception);
	}

	public AuthenticationException(Exception e) {
		super(e);
	}
}
