package com.xebia.xcoss.axcv.logic;

import com.xebia.xcoss.axcv.R;
import java.net.URI;

public class DataException extends CommException {

	private static final long serialVersionUID = 539794302349294965L;

	private Code code;

	public enum Code {
		NOT_ALLOWED, NOT_FOUND, TIME_OUT, NOT_HANDLED, NO_NETWORK
	}

	public DataException(Code code, URI uri) {
		super(code.name() + " on " + uri.toString());
		this.code = code;
	}

	public boolean missing() {
		return (code == Code.NOT_FOUND);
	}

	public boolean denied() {
		return (code == Code.NOT_ALLOWED);
	}

	public boolean timedOut() {
		return (code == Code.TIME_OUT);
	}

	public boolean networkError() {
		return (code == Code.NO_NETWORK);
	}

}
