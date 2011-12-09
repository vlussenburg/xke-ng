package com.xebia.xcoss.axcv.model;

import java.io.Serializable;

public class Author implements Serializable, Comparable<Author> {

	private static final long serialVersionUID = -4692100737600425470L;

	private String userId;
	private String mail;
	private String name;
	// TODO No mapping yet from server
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mail == null) ? 0 : mail.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((phone == null) ? 0 : phone.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Author other = (Author) obj;
		if (mail == null) {
			if (other.mail != null) return false;
		} else if (!mail.equals(other.mail)) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (phone == null) {
			if (other.phone != null) return false;
		} else if (!phone.equals(other.phone)) return false;
		if (userId == null) {
			if (other.userId != null) return false;
		} else if (!userId.equals(other.userId)) return false;
		return true;
	}
}
