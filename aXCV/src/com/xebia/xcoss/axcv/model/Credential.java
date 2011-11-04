package com.xebia.xcoss.axcv.model;

import com.xebia.xcoss.axcv.util.SecurityUtils;
import com.xebia.xcoss.axcv.util.StringUtil;

public class Credential {
	@SuppressWarnings("unused")
	private String username;
	@SuppressWarnings("unused")
	private String password;
	@SuppressWarnings("unused")
	private String encryptedPassword;

	public Credential(String name, String plain) {
		this(name, plain, false);
	}

	public Credential(String name, String plain, boolean encryptedOnly) {
		this.username = StringUtil.isEmpty(name) ? "not_set" : name;
		if (!encryptedOnly) {
			this.password = StringUtil.isEmpty(plain) ? "not_set" : plain;
		}
		this.encryptedPassword = SecurityUtils.encrypt(password);
	}
}
