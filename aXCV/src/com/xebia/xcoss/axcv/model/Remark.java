package com.xebia.xcoss.axcv.model;

import java.io.Serializable;

public class Remark implements Serializable {
	private static final long serialVersionUID = -926508285490647721L;

	private String user;
	private String comment;
	private volatile String sessionId;

	public Remark(String user, String comment, String sessionId) {
		this.user = user;
		this.comment = comment;
		this.sessionId = sessionId;
	}

	public String getUser() {
		return user;
	}

	public String getComment() {
		return comment;
	}

	public String getSessionId() {
		return sessionId;
	}
}
