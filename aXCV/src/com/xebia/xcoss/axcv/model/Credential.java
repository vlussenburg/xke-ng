package com.xebia.xcoss.axcv.model;

import com.xebia.xcoss.axcv.util.StringUtil;

public class Credential {
	@SuppressWarnings("unused")
	private String username;
	@SuppressWarnings("unused")
	private String password;
	@SuppressWarnings("unused")
	private String encryptedPassword;

	public Credential(String name, String password, boolean isEncrypted) {
		this.username = StringUtil.isEmpty(name) ? "not_set" : name;
		if (isEncrypted) {
			this.encryptedPassword = StringUtil.isEmpty(password) ? "?" : password;
		} else {
			this.password = StringUtil.isEmpty(password) ? "?" : password;
		}
	}
}
