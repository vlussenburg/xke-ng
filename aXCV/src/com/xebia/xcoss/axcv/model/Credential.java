package com.xebia.xcoss.axcv.model;

import com.xebia.xcoss.axcv.util.StringUtil;

public class Credential {
	@SuppressWarnings("unused")
	private String username;
	@SuppressWarnings("unused")
	private String password;

	public Credential(String name, String password) {
		this.username = StringUtil.isEmpty(name) ? "not_set" : name;
		this.password = StringUtil.isEmpty(password) ? "?" : password;
	}
}
