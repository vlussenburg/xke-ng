package com.xebia.xcoss.auth.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;

import com.atlassian.crowd.integration.rest.service.RestCrowdClient;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;
import com.xebia.xcoss.auth.AuthenticationException;
import com.xebia.xcoss.auth.Authenticator;
import com.xebia.xcoss.auth.Profile;
import com.xebia.xcoss.util.Config;

public class CrowdAuthenticator extends RestCrowdClient implements Authenticator {

	public CrowdAuthenticator() {
		super(getProperties());
	}

	private static ClientProperties getProperties() {
		try {
			Config cfg = new Config();
			cfg.loadFrom("crowd.properties");
			return ClientPropertiesImpl.newInstanceFromProperties(cfg);
		}
		catch (IOException e) {
			throw new RuntimeException("Couldn't load crowd.properties", e);
		}
	}

	@Override
	public boolean validate(String token) throws AuthenticationException {
		if (StringUtils.isBlank(token)) {
			throw new AuthenticationException("Invalid token.");
		}

		ValidationFactor[] vf = new ValidationFactor[1];
		vf[0] = new ValidationFactor(ValidationFactor.REMOTE_ADDRESS, "xcoss.xebia.com");
		try {
			validateSSOAuthentication(token, Arrays.asList(vf));
		}
		catch (Exception e) {
			throw new AuthenticationException(e);
		}
		return true;
	}

	@Override
	public Profile getProfile(String token) throws AuthenticationException {
		try {
			return new Profile(findUserFromSSOToken(token));
		}
		catch (Exception e) {
			throw new AuthenticationException(e);
		}
	}

}
