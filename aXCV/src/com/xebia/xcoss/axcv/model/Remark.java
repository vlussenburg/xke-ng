package com.xebia.xcoss.axcv.model;

public class Remark {
	private String user;
	private String comment;

	public Remark(String user, String comment) {
		this.user = user;
		this.comment = comment;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
