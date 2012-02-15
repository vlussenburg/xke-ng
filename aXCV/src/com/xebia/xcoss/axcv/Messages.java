package com.xebia.xcoss.axcv;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "com.xebia.xcoss.axcv.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static String getString(String key, Object... objects) {
		try {
			return MessageFormat.format(RESOURCE_BUNDLE.getString(key), objects);
		}
		catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
