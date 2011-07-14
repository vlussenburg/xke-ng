package com.xebia.xcoss.axcv.model;

import com.xebia.xcoss.axcv.util.SecurityUtils;

public class Credential {
	@SuppressWarnings("unused")
	private String name;
	@SuppressWarnings("unused")
	private String cryptedPassword;
	
	public Credential(String name, String plain) {
		this.name = name;
		this.cryptedPassword = SecurityUtils.encrypt(plain);
	}
}
