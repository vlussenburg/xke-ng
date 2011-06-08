package com.xebia.xcoss.axcv.model;

public class Author {

	private String mail;
	private String name;
	private String userId;

	public Author(String id, String name, String mail) {
		this.userId = id;
		this.name = name;
		this.mail = mail;
	}

	public String getMail() {
		return mail;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUserId() {
		return userId;
	}
}
