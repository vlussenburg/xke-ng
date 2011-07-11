package com.xebia.xcoss.axcv.logic;

import java.net.URI;

public class DataException extends CommException {

	private Code code;
	
	public enum Code {
		NOT_ALLOWED, NOT_FOUND
	}

	public DataException(Code code, URI uri) {
		super("Data " + code.name() + " on " + uri.toString());
		this.code = code;
	}

	public boolean missing() {
		return ( code == Code.NOT_FOUND);
	}
	
	public boolean denied() {
		return ( code == Code.NOT_ALLOWED);
	}
}
