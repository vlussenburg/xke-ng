package com.xebia.xcoss.axcv.logic;

import java.net.URI;

public class DataException extends Exception {

	public enum Code {
		NOT_ALLOWED, NOT_FOUND
	}

	public DataException(Code code, URI uri) {
		super("Data " + code.name() + " on " + uri.toString());
	}

}
