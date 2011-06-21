package com.xebia.xcoss.axcv.model;

import java.io.Serializable;

public class Author implements Serializable, Comparable<Author> {

	private static final long serialVersionUID = -4692100737600425470L;

	private String mail;
	private String name;
	private String userId;
	private String phone;

	public Author(String id, String name, String mail, String phone) {
		this.userId = id;
		this.name = name;
		this.mail = mail;
		this.phone = phone;
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
	

	public String getPhone() {
		return phone;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(Author other) {
		return getUserId().compareTo(other.getUserId());
	}
}