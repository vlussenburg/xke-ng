package com.xebia.xcoss.auth;

public interface Authenticator {

	public boolean validate(String token) throws AuthenticationException;

	public Profile getProfile(String token) throws AuthenticationException;

}
