package com.xebia.xcoss.axcv.model;

import com.xebia.xcoss.axcv.util.SecurityUtils;

public class Credential {
	@SuppressWarnings("unused")
	private String name;
	@SuppressWarnings("unused")
	private String password;
	@SuppressWarnings("unused")
	private String encryptedPassword;

	public Credential(String name, String plain) {
		this(name, plain, false);
	}

	public Credential(String name, String plain, boolean encryptedOnly) {
		this.name = name;
		if (!encryptedOnly) {
			this.password = plain;
		}
		this.encryptedPassword = SecurityUtils.encrypt(plain);
	}
}
